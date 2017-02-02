package com.test.mashup;

import com.test.mashup.github.GithubProjectFinder;
import com.test.mashup.github.GithubProjectFinderImpl;
import com.test.mashup.github.GithubProjectFinderWithCachingImpl;
import com.test.mashup.json.JsonParser;
import com.test.mashup.json.SimpleNativeJsonParser;
import com.test.mashup.metrics.LoggingOnlyMetricsImplementation;
import com.test.mashup.metrics.Metrics;
import com.test.mashup.twitter.TweetFinderImpl;
import com.test.mashup.twitter.TwitterFinder;
import com.test.mashup.util.ConfigurationUtil;
import com.test.mashup.util.Constants;
import com.test.mashup.util.ExpiringCache;
import com.test.mashup.util.LocalExpiringCache;

/**
 * Since we can not use dependency injection framework (like Guice, Spring,
 * ServiceLoader or CDI) we will use this factory to make sure we can easily
 * switch from one implementation to another one, without the need to change
 * other parts of our code.
 * 
 * Ideally our project should use some kind of DI.
 * 
 * @author borisa
 *
 */
public abstract class DependenciesFactory {

	public static JsonParser createParser() {
		return new SimpleNativeJsonParser();
	}

	/**
	 * Returns appropriate implementation of {@code GithubProjectFinder} based
	 * on configuration.
	 * 
	 * @return
	 */
	public static GithubProjectFinder createGithubProjectFinder() {
		final int githubProjectCacheTimeoutSeconds = ConfigurationUtil.getInt(Constants.GITHUB_SEARCH_CACHE_TIMEOUT_SECONDS_PROPERTY_NAME);
		final boolean isCachingTurnedOn = githubProjectCacheTimeoutSeconds > 0;
		if (isCachingTurnedOn) {
			return new GithubProjectFinderWithCachingImpl();
		} else {
			return new GithubProjectFinderImpl();
		}
	}

	public static TwitterFinder createTweetFinder() {
		return new TweetFinderImpl();
	}

	/**
	 * Creates appropriate cache implementation.
	 * 
	 * @param cacheName
	 *            the name of cache
	 * @return
	 */
	public static <T> ExpiringCache<T> createCache(String cacheName) {
		return new LocalExpiringCache<T>(cacheName);
	}

	public static Metrics createMetrics() {
		return new LoggingOnlyMetricsImplementation();
	}

}
