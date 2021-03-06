package com.test.mashup.github;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.test.mashup.DependenciesFactory;
import com.test.mashup.metrics.Counter;
import com.test.mashup.util.ConfigurationUtil;
import com.test.mashup.util.Constants;
import com.test.mashup.util.ExpiringCache;

/**
 * Caching decorator around {@link GithubProjectFinderImpl}. It will cache
 * results of search and keep them in memory for configured number of seconds.
 * 
 * Again, we expect that higher level code will perform retry policy when
 * invoking methods of this class.
 * 
 * @author borisa
 *
 */
public class GithubProjectFinderWithCachingImpl extends GithubProjectFinderImpl {

	private final int itemExpirationSeconds = ConfigurationUtil.getInt(Constants.GITHUB_SEARCH_CACHE_TIMEOUT_SECONDS_PROPERTY_NAME);

	private final ExpiringCache<List<GithubProject>> cache = DependenciesFactory.createCache("GithubProjects");

	private final Counter cacheHitCounter = DependenciesFactory.createMetrics().getCounter("GithubProjectSearchCacheHitsCount");
	private final Counter cacheMissCounter = DependenciesFactory.createMetrics().getCounter("GithubProjectSearchCacheMissesCount");

	@Override
	public List<GithubProject> findProjects(String keyword, int limit, String orderByField) {
		if (keyword == null) {
			throw new IllegalArgumentException("Keyword must not be null");
		}
		if (keyword.isEmpty()) {
			throw new IllegalArgumentException("Keyword must not be empty string");
		}
		final int limitOutput = normalizeLimit(limit);
		final String searchUrl = buildUrl(keyword, limitOutput, orderByField);
		final List<GithubProject> cachedResult = cache.get(searchUrl);
		if (cachedResult != null) {
			log.info("Found cached value for github project search " + searchUrl);
			cacheHitCounter.inc();
			return cachedResult;
		} else {
			cacheMissCounter.inc();
			final List<GithubProject> result = super.findProjects(keyword, limitOutput, orderByField);
			cache.put(searchUrl, result, itemExpirationSeconds, TimeUnit.SECONDS);
			log.fine("Cached found projects for search url " + searchUrl + " and will keep them there for " + itemExpirationSeconds + " seconds");
			return result;
		}
	}

}
