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
import com.test.mashup.retry.RetryPolicy;
import com.test.mashup.retry.SimpleRetryPolicy;
import com.test.mashup.twitter.Tweet;
import com.test.mashup.twitter.TweetFinder;
import com.test.mashup.util.ConfigurationUtil;
import com.test.mashup.util.Constants;
import com.test.mashup.util.NamedThreadFactory;

/**
 * Entry point to our functionality. Orchestrates other parts of application,
 * deals with recovery and retries, decides whether to use parallel search or
 * not.
 * 
 * Main method (or potentially REST endpoint) use this to do all the work.
 * 
 * @author borisa
 *
 */
public class MashupApp {

	private final Logger log = Logger.getLogger(getClass().getName());

	/*
	 * Configuration properties
	 */
	private final int twitterSearchThreadPoolSize = ConfigurationUtil
			.getInt(Constants.TWITTER_SEARCH_THREAD_POOL_SIZE_PROPERTY_NAME);

	private final int twitterSearchRetryMaxAttempts = ConfigurationUtil
			.getInt(Constants.TWITTER_SEARCH_RETRY_MAX_ATTEMPTS_PROPERTY_NAME);
	private final int twitterSearchRetryBackoffMillis = ConfigurationUtil
			.getInt(Constants.TWITTER_SEARCH_RETRY_FIXED_BACKOFF_MILLIS_PROPERTY_NAME);

	private final int githubSearchRetryMaxAttempts = ConfigurationUtil
			.getInt(Constants.GITHUB_SEARCH_RETRY_MAX_ATTEMPTS_PROPERTY_NAME);
	private final int githubSearchRetryBackoffMillis = ConfigurationUtil
			.getInt(Constants.GITHUB_SEARCH_RETRY_FIXED_BACKOFF_MILLIS_PROPERTY_NAME);

	/*
	 * Dependencies needed
	 */
	private final GithubProjectFinder githubFinder = DependenciesFactory.createGithubProjectFinder();
	private final TweetFinder tweetFinder = DependenciesFactory.createTweetFinder();

	private final ExecutorService twitterExecService;

	public MashupApp() {
		if (twitterSearchThreadPoolSize > 0) {
			twitterExecService = Executors.newFixedThreadPool(twitterSearchThreadPoolSize,
					new NamedThreadFactory("twitter-search"));
		} else {
			twitterExecService = null;
		}
	}

	/**
	 * Decides what kind of search to perform (based on configuration values)
	 * and executes that search. Creates appropriate retry policy that will be
	 * used by main search functionality.
	 * 
	 * @param keyword
	 *            the keyword used for search. Must not be null or empty.
	 * @return output result. Never returns null. Throws exception in case it
	 *         was not able to retrieve results after all retries.
	 */
	public OutputResult executeSearch(String keyword) {
		if (keyword == null || keyword.isEmpty()) {
			throw new IllegalArgumentException("Keyword must not be null or empty");
		}
		final String trimmed = keyword.trim();
		log.info("Creating mashup by keyword [" + trimmed + "]");
		final RetryPolicy<List<GithubProject>> githubSearchRetryPolicy = new SimpleRetryPolicy<>("github-search",
				githubSearchRetryMaxAttempts, githubSearchRetryBackoffMillis);
		final List<GithubProject> projects = githubSearchRetryPolicy.execute(() -> githubFinder.findProjects(trimmed));

		OutputResult result = new OutputResult();

		final RetryPolicy<List<Tweet>> twitterSearchRetryPolicy = new SimpleRetryPolicy<>("twitter-search",
				twitterSearchRetryMaxAttempts, twitterSearchRetryBackoffMillis);
		if (twitterExecService != null) {
			log.info("Will use " + twitterSearchThreadPoolSize + " threads for twitter search execution in parallel");
			result = doExecuteParallel(projects, twitterSearchRetryPolicy);
		} else {
			log.info("Using single-threaded execution");
			result = doExecute(projects, twitterSearchRetryPolicy);
		}
		log.fine("Successfully found results " + result);
		return result;
	}

	/*
	 * Executes all searches in parallel fashion, using thread pool as per
	 * configuration provided.
	 */
	private OutputResult doExecuteParallel(List<GithubProject> projects, RetryPolicy<List<Tweet>> rPolicy) {
		final List<GithubProjectWithTweets> allProjects = projects.stream()
				.map(p -> CompletableFuture.supplyAsync(() -> {
					if (log.isLoggable(Level.INFO)) {
						// for better visibility and just because JUL does not
						// allow us to use thread name in log output
						log.info("Executing parallel twitter search - thread " + Thread.currentThread().getName());
					}
					// execute twitter search with retry policy
					final List<Tweet> tweets = rPolicy.execute(() -> tweetFinder.searchTwitter(p.getName()));
					final GithubProjectWithTweets gt = new GithubProjectWithTweets();
					gt.setProject(p);
					gt.setTweets(tweets);
					return gt;
				}, twitterExecService)).map(CompletableFuture::join).collect(Collectors.toList());
		final OutputResult result = new OutputResult();
		result.setProjects(allProjects);
		return result;
	}

	/*
	 * Executes all searches in a single thread, one by one.
	 */
	private OutputResult doExecute(List<GithubProject> projects, RetryPolicy<List<Tweet>> rPolicy) {
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

	/**
	 * Shutdown all internal resources. Must be invoked before shutting down
	 * application (JVM).
	 */
	public void shutdown() {
		if (twitterExecService != null) {
			twitterExecService.shutdown();
		}
	}

}