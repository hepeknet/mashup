package com.test.mashup;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 * some kind of microservice.
 *
 * @author borisa
 *
 */
public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	private static final int twitterSearchThreadPoolSize = ConfigurationUtil
			.getInt(Constants.TWITTER_SEARCH_THREAD_POOL_SIZE_PROPERTY_NAME);

	private static final int twitterSearchRetryMaxAttempts = ConfigurationUtil
			.getInt(Constants.TWITTER_SEARCH_RETRY_MAX_ATTEMPTS_PROPERTY_NAME);
	private static final int twitterSearchRetryBackoffMillis = ConfigurationUtil
			.getInt(Constants.TWITTER_SEARCH_RETRY_FIXED_BACKOFF_MILLIS_PROPERTY_NAME);

	private static GithubProjectFinder githubFinder = DependenciesFactory.createGithubProjectFinder();
	private static TweetFinder tweetFinder = DependenciesFactory.createTweetFinder();
	private static JsonParser parser = DependenciesFactory.createParser();

	private static Histogram appStats = DependenciesFactory.createMetrics()
			.getHistogram("MashupStatisticsExecutionTimeMs");

	public static void main1(String[] args) throws Exception {
		String line = null;
		do {
			printUsage();
			line = System.console().readLine();
			LOG.info("Entered keyword is " + line);
			searchAndPrintResult(line);
		} while (line != null && !line.isEmpty() && !line.equals("quit"));
	}

	public static void main(String[] args) throws Exception {
		searchAndPrintResult("reactive");
	}

	/*
	 * Separating statistics and outputting result from the main search
	 * functionality
	 */
	private static void searchAndPrintResult(String keyword) {
		final long start = System.currentTimeMillis();
		final OutputResult result = executeSearch(keyword);
		final String json = parser.toJson(result);
		final long totalMs = System.currentTimeMillis() - start;
		appStats.update(totalMs);
		System.out.println(json);
	}

	/*
	 * Decides what kind of search to perform (based on configuration values)
	 * and executes that search.
	 */
	private static OutputResult executeSearch(String keyword) {
		LOG.info("Creating mashup by keyword [" + keyword + "]");
		final List<GithubProject> projects = githubFinder.findProjects(keyword);
		final RetryPolicy<List<Tweet>> retryPolicy = new SimpleRetryPolicy<>("twitter-search",
				twitterSearchRetryMaxAttempts, twitterSearchRetryBackoffMillis);
		OutputResult result = null;
		if (twitterSearchThreadPoolSize > 0) {
			LOG.info("Will use " + twitterSearchThreadPoolSize + " for ");
			final ExecutorService es = Executors.newFixedThreadPool(twitterSearchThreadPoolSize,
					new NamedThreadFactory("twitter-search"));
			result = doExecuteParallel(projects, es, retryPolicy);
		} else {
			LOG.info("Using single-threaded execution");
			result = doExecute(projects, retryPolicy);
		}
		LOG.fine("Successfully found results " + result);
		return result;
	}

	/**
	 * Executes all searches in parallel fashion, using thread pool as per
	 * configuration
	 * 
	 * @param projects
	 *            Github projects found by keyword
	 * @param es
	 *            ExecutoService to be used for parallel execution
	 * @param rPolicy
	 *            - retry policy to be used for twitter search
	 * @return output results
	 */
	private static OutputResult doExecuteParallel(List<GithubProject> projects, ExecutorService es,
			RetryPolicy<List<Tweet>> rPolicy) {
		final List<GithubProjectWithTweets> allProjects = projects.stream()
				.map(p -> CompletableFuture.supplyAsync(() -> {
					// execute with retry policy
					final List<Tweet> tweets = rPolicy.execute(() -> tweetFinder.searchTwitter(p.getName()));
					final GithubProjectWithTweets gt = new GithubProjectWithTweets();
					gt.setProject(p);
					gt.setTweets(tweets);
					return gt;
				}, es)).map(CompletableFuture::join).collect(Collectors.toList());
		final OutputResult result = new OutputResult();
		result.setProjects(allProjects);
		return result;
	}

	/**
	 * Executes all searches in single thread, one by one
	 * 
	 * @param projects
	 *            Github projects found by keyword
	 * @param rPolicy
	 *            - retry policy to be used for twitter search
	 * @return output results
	 */
	private static OutputResult doExecute(List<GithubProject> projects, RetryPolicy<List<Tweet>> rPolicy) {
		final List<GithubProjectWithTweets> projectsWithTweets = new LinkedList<>();
		projects.forEach(p -> {
			// execute with retry policy
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
