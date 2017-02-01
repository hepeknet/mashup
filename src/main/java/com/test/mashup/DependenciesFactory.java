package com.test.mashup;

import com.test.mashup.github.GithubProjectFinder;
import com.test.mashup.github.GithubProjectFinderWithCaching;
import com.test.mashup.json.JsonParser;
import com.test.mashup.json.SimpleNativeJsonParser;
import com.test.mashup.metrics.Metrics;
import com.test.mashup.metrics.NoOpMetricsImplementation;
import com.test.mashup.twitter.TweetFinder;
import com.test.mashup.util.ConfigurationUtil;
import com.test.mashup.util.Constants;
import com.test.mashup.util.ExpiringCache;
import com.test.mashup.util.LocalNaiveExpiringCache;

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
		final int githubProjectCacheTimeoutSeconds = ConfigurationUtil
				.getInt(Constants.GITHUB_SEARCH_CACHE_TIMEOUT_SECONDS_PROPERTY_NAME);
		final boolean isCachingTurnedOn = githubProjectCacheTimeoutSeconds > 0;
		if (isCachingTurnedOn) {
			return new GithubProjectFinderWithCaching();
		} else {
			return new GithubProjectFinder();
		}
	}

	public static TweetFinder createTweetFinder() {
		return new TweetFinder();
	}

	public static <T> ExpiringCache<T> createCache() {
		return new LocalNaiveExpiringCache<T>();
	}

	public static Metrics createMetrics() {
		return new NoOpMetricsImplementation();
	}

}
