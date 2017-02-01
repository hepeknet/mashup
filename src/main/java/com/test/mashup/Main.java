package com.test.mashup;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.test.mashup.github.GithubProject;
import com.test.mashup.github.GithubProjectFinder;
import com.test.mashup.json.JsonParser;
import com.test.mashup.metrics.Histogram;
import com.test.mashup.retry.RetryPolicy;
import com.test.mashup.retry.SimpleRetryPolicy;
import com.test.mashup.twitter.Tweet;
import com.test.mashup.twitter.TweetFinder;
import com.test.mashup.util.ConfigurationUtil;
import com.test.mashup.util.Constants;
import com.test.mashup.util.NamedThreadFactory;

/**
 * Main entry point into application. Ideally this should be replaced with some
 * kind of REST endpoint (Jetty, Spring boot...) if this is to be exposed as
 * some kind of microservice and deployed in distributed fashion.
 *
 * @author borisa
 *
 */
public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	/*
	 * Configuration properties
	 */
	private static final int twitterSearchThreadPoolSize = ConfigurationUtil
			.getInt(Constants.TWITTER_SEARCH_THREAD_POOL_SIZE_PROPERTY_NAME);

	private static final int twitterSearchRetryMaxAttempts = ConfigurationUtil
			.getInt(Constants.TWITTER_SEARCH_RETRY_MAX_ATTEMPTS_PROPERTY_NAME);
	private static final int twitterSearchRetryBackoffMillis = ConfigurationUtil
			.getInt(Constants.TWITTER_SEARCH_RETRY_FIXED_BACKOFF_MILLIS_PROPERTY_NAME);

	private static final int githubSearchRetryMaxAttempts = ConfigurationUtil
			.getInt(Constants.GITHUB_SEARCH_RETRY_MAX_ATTEMPTS_PROPERTY_NAME);
	private static final int githubSearchRetryBackoffMillis = ConfigurationUtil
			.getInt(Constants.GITHUB_SEARCH_RETRY_FIXED_BACKOFF_MILLIS_PROPERTY_NAME);

	/*
	 * Dependencies needed
	 */
	private static GithubProjectFinder githubFinder = DependenciesFactory.createGithubProjectFinder();
	private static TweetFinder tweetFinder = DependenciesFactory.createTweetFinder();
	private static JsonParser parser = DependenciesFactory.createParser();

	/*
	 * Tracking and exposing internal statistics
	 */
	private static Histogram appStats = DependenciesFactory.createMetrics()
			.getHistogram("MashupStatisticsExecutionTimeMs");

	public static void main1(String[] args) throws Exception {
		String line = null;
		do {
			printUsage();
			line = System.console().readLine();
			LOG.info("Entered keyword is " + line);
			searchAndPrintResult(line);
		} while (line != null && !line.isEmpty());
	}

	public static void main(String[] args) throws Exception {
		searchAndPrintResult("basdfasdgdasgashadshsahashas");
	}

	/*
	 * Separating statistics and outputting result from the main search
	 * functionality. This method is responsible to invoke main functionality
	 * and then print result in appropriate form.
	 */
	private static void searchAndPrintResult(String keyword) {
		final long start = System.currentTimeMillis();
		final OutputResult result = executeSearch(keyword);
		final String json = parser.toJson(result);
		final long totalMs = System.currentTimeMillis() - start;
		appStats.update(totalMs);
		System.out.println("========================================================");
		System.out.println("In total found " + result.getProjects().size() + " projects for keyword [" + keyword + "]");
		System.out.println("Mashup for keyword [" + keyword + "] is:");
		System.out.println("========================================================");
		System.out.println(json);
		System.out.println("========================================================");
	}

	/*
	 * Decides what kind of search to perform (based on configuration values)
	 * and executes that search. Creates appropriate retry policy that will be
	 * used by main search functionality.
	 */
	private static OutputResult executeSearch(String keyword) {
		LOG.info("Creating mashup by keyword [" + keyword + "]");
		final RetryPolicy<List<GithubProject>> githubSearchRetryPolicy = new SimpleRetryPolicy<>("github-search",
				githubSearchRetryMaxAttempts, githubSearchRetryBackoffMillis);
		final List<GithubProject> projects = githubSearchRetryPolicy.execute(() -> githubFinder.findProjects(keyword));

		OutputResult result = new OutputResult();

		final RetryPolicy<List<Tweet>> twitterSearchRetryPolicy = new SimpleRetryPolicy<>("twitter-search",
				twitterSearchRetryMaxAttempts, twitterSearchRetryBackoffMillis);
		if (twitterSearchThreadPoolSize > 0) {
			LOG.info("Will use " + twitterSearchThreadPoolSize + " for ");
			final ExecutorService es = Executors.newFixedThreadPool(twitterSearchThreadPoolSize,
					new NamedThreadFactory("twitter-search"));
			result = doExecuteParallel(projects, es, twitterSearchRetryPolicy);
		} else {
			LOG.info("Using single-threaded execution");
			result = doExecute(projects, twitterSearchRetryPolicy);
		}
		LOG.fine("Successfully found results " + result);
		return result;
	}

	/*
	 * Executes all searches in parallel fashion, using thread pool as per
	 * configuration provided.
	 */
	private static OutputResult doExecuteParallel(List<GithubProject> projects, ExecutorService es,
			RetryPolicy<List<Tweet>> rPolicy) {
		final List<GithubProjectWithTweets> allProjects = projects.stream()
				.map(p -> CompletableFuture.supplyAsync(() -> {
					if (LOG.isLoggable(Level.INFO)) {
						// for better visibility and just because JUL does not
						// allow us to use thread name in log output
						LOG.info("Executing parallel twitter search - thread " + Thread.currentThread().getName());
					}
					// execute twitter search with retry policy
					final List<Tweet> tweets = rPolicy.execute(() -> tweetFinder.searchTwitter(p.getName()));
					final GithubProjectWithTweets gt = new GithubProjectWithTweets();
					gt.setProject(p);
					gt.setTweets(tweets);
					return gt;
				}, es)).map(CompletableFuture::join).collect(Collectors.toList());
		final OutputResult result = new OutputResult();
		result.setProjects(allProjects);
		es.shutdown();
		return result;
	}

	/*
	 * Executes all searches in a single thread, one by one.
	 */
	private static OutputResult doExecute(List<GithubProject> projects, RetryPolicy<List<Tweet>> rPolicy) {
		final List<GithubProjectWithTweets> projectsWithTweets = new LinkedList<>();
		projects.forEach(p -> {
			// execute twitter search with retry policy
			final List<Tweet> tweets = rPolicy.execute(() -> tweetFinder.searchTwitter(p.getName()));
			final GithubProjectWithTweets gt = new GithubProjectWithTweets();
			gt.setProject(p);
			gt.setTweets(tweets);
			projectsWithTweets.add(gt);
		});
		final OutputResult result = new OutputResult();
		result.setProjects(projectsWithTweets);
		return result;
	}

	private static void printUsage() {
		System.out.println("Enter keyword to search for (enter to exit):");
	}

}
