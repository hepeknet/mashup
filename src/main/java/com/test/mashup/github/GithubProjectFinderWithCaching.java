package com.test.mashup.github;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.test.mashup.DependenciesFactory;
import com.test.mashup.util.ConfigurationUtil;
import com.test.mashup.util.Constants;
import com.test.mashup.util.ExpiringCache;

/**
 * Caching decorator around {@link GithubProjectFinder}. It will cache results
 * of search and keep them in memory for configured number of seconds.
 * 
 * @author borisa
 *
 */
public class GithubProjectFinderWithCaching extends GithubProjectFinder {

	private final int itemExpirationSeconds = ConfigurationUtil
			.getInt(Constants.GITHUB_SEARCH_CACHE_TIMEOUT_SECONDS_PROPERTY_NAME);
	private final ExpiringCache<List<GithubProject>> cache = DependenciesFactory.createCache();

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
			log.fine("Found cached value for github project search " + searchUrl);
			return cachedResult;
		} else {
			final List<GithubProject> result = super.findProjects(keyword, limitOutput, orderByField);
			cache.put(searchUrl, result, itemExpirationSeconds, TimeUnit.SECONDS);
			log.fine("Cached found projects for search url " + searchUrl + " and will keep them there for "
					+ itemExpirationSeconds + " seconds");
			return result;
		}
	}

}
