package com.test.mashup.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.test.mashup.DependenciesFactory;
import com.test.mashup.json.JsonParser;
import com.test.mashup.metrics.Counter;
import com.test.mashup.metrics.Histogram;
import com.test.mashup.util.ConfigurationUtil;
import com.test.mashup.util.Constants;

/**
 * Implementation of Github project search by keyword. Very basic form of error
 * handling here. We expect that retry policy will be applied on invocation of
 * this search and that someone with more visibility into overall application
 * structure will perform sensible retries and recovery.
 * 
 * @author borisa
 *
 */
public class GithubProjectFinder {

	protected final Logger log = Logger.getLogger(getClass().getName());

	/*
	 * Simulate DI for parser - in case we switch to more robust 3PP based
	 * implementation for JSON parsing
	 */
	private final JsonParser parser = DependenciesFactory.createParser();

	private final Histogram searchStatistics = DependenciesFactory.createMetrics()
			.getHistogram("GithubProjectSearchTimeStats");

	private final Counter githubSearchFailures = DependenciesFactory.createMetrics()
			.getCounter("GithubProjectSearchFailuresCount");

	/*
	 * Configuration properties
	 */
	private final String baseUrl = ConfigurationUtil.getStringRequired(Constants.GITHUB_SEARCH_BASE_URL_PROPERTY_NAME);
	private final int projectSearchLimit = ConfigurationUtil
			.getInt(Constants.GITHUB_SEARCH_MAX_PROJECTS_LIMIT_PROPERTY_NAME);
	private final String sortField = ConfigurationUtil
			.getString(Constants.GITHUB_SEARCH_DEFAULT_SORT_FIELD_PROPERTY_NAME);

	public List<GithubProject> findProjects(String keyword) {
		return findProjects(keyword, projectSearchLimit);
	}

	private List<GithubProject> findProjects(String keyword, int limit) {
		return findProjects(keyword, limit, sortField);
	}

	/**
	 * Builds search HTTP URL based on parameters provided
	 * 
	 * @param keyword
	 *            search keyword - must not be null or empty string
	 * @param limit
	 *            the number of projects to be returned. Must be greater than 0.
	 * @param orderByField
	 *            the name of field to be used for sorting results
	 * @return valid HTTP URL for searching Github projects
	 */
	protected String buildUrl(String keyword, int limit, String orderByField) {
		String searchUrl = baseUrl + URLEncoder.encode(keyword);
		if (orderByField != null && !orderByField.isEmpty()) {
			log.info("Will sort github projects by field " + orderByField);
			searchUrl += "&sort=" + orderByField;
		}
		log.info("Github projects search URL is " + searchUrl);
		return searchUrl;
	}

	protected int normalizeLimit(int limit) {
		final int limitOutput = (limit <= 0 || limit > projectSearchLimit) ? projectSearchLimit : limit;
		return limitOutput;
	}

	public List<GithubProject> findProjects(String keyword, int limit, String orderByField) {
		if (keyword == null) {
			throw new IllegalArgumentException("Keyword must not be null");
		}
		if (keyword.trim().isEmpty()) {
			throw new IllegalArgumentException("Keyword must not be empty string");
		}
		final int limitOutput = normalizeLimit(limit);
		final String searchUrl = buildUrl(keyword, limitOutput, orderByField);
		// make sure limit is within acceptable range
		final List<GithubProject> results = new ArrayList<GithubProject>(limitOutput);
		try {
			final long startTime = System.currentTimeMillis();
			/*
			 * Ideally we would not have to do this if we had 3PP doing Json
			 * parsing for us. But because of limited time to develop this
			 * project and because of limitation that we can not use any 3PP we
			 * had to create JSON parser that returns Map.
			 */
			final Map<String, Object> searchResultsParsed = parser.parse(new URL(searchUrl).openStream());
			if (log.isLoggable(Level.FINE)) {
				log.fine("Parsed search results are " + searchResultsParsed);
			}
			if (searchResultsParsed != null && !searchResultsParsed.isEmpty()) {
				final Object foundItems = searchResultsParsed.get("items");
				if (foundItems != null) {
					final List<Map<String, Object>> itemList = (List<Map<String, Object>>) foundItems;
					for (final Map<String, Object> item : itemList) {
						final Map<String, Object> itemMap = item;
						final String name = (String) itemMap.get("name");
						final String description = (String) itemMap.get("description");
						final String url = (String) itemMap.get("url");
						final Integer forks = (Integer) itemMap.get("forks");
						final Integer watchers = (Integer) itemMap.get("watchers");
						final GithubProject project = new GithubProject(name, description, forks, watchers, url);
						results.add(project);
						// unfortunately github search API does not support
						// limit so we have to do it manually here
						if (results.size() == limitOutput) {
							log.info("Limiting number of github projects to " + limitOutput);
							break;
						}
					}
					final long totalTimeMs = System.currentTimeMillis() - startTime;
					searchStatistics.update(totalTimeMs);
				} else {
					githubSearchFailures.inc();
					throw new RuntimeException(
							"Was not able to find field [items] in parsed search results. Did GitHub API change?");
				}
			} else {
				githubSearchFailures.inc();
				throw new RuntimeException("Was not able to parse search results when accessing " + searchUrl
						+ ". Check log for more details!");
			}
		} catch (final MalformedURLException mue) {
			throw new IllegalStateException("Malformed URL for github project search [" + searchUrl + "]", mue);
		} catch (final IOException ie) {
			githubSearchFailures.inc();
			throw new IllegalStateException("IO exception while searching for projects on " + searchUrl, ie);
		}
		if (log.isLoggable(Level.FINE)) {
			log.fine("Github project search results are " + results);
		}
		return results;
	}

}
