package com.test.mashup.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.test.mashup.ConfigurationUtil;
import com.test.mashup.Constants;
import com.test.mashup.JsonParser;
import com.test.mashup.SimpleNativeJsonParser;

//TODO: add max limit
//TODO: externalize config
//TODO: error handling
//TODO: http code handling and retries
public class GithubProjectFinder {

	private final Logger log = Logger.getLogger(getClass().getName());

	private static final String DEFAULT_SORT_FIELD = "stars";

	private final JsonParser parser = new SimpleNativeJsonParser();
	private final String baseUrl = ConfigurationUtil.getString(Constants.GITHUB_SEARCH_BASE_URL_PROPERTY_NAME);
	private final int projectSearchLimit = ConfigurationUtil
			.getInt(Constants.GITHUB_SEARCH_MAX_PROJECTS_LIMIT_PROPERTY_NAME);

	public List<GithubProject> findProjects(String keyword) {
		return findProjects(keyword, projectSearchLimit);
	}

	public List<GithubProject> findProjects(String keyword, int limit) {
		return findProjects(keyword, limit, DEFAULT_SORT_FIELD);
	}

	public List<GithubProject> findProjects(String keyword, int limit, String orderByField) {
		if (keyword == null) {
			throw new IllegalArgumentException("Keyword must not be null");
		}
		if (keyword.isEmpty()) {
			throw new IllegalArgumentException("Keyword must not be empty string");
		}
		String searchUrl = baseUrl + keyword;
		if (orderByField != null && !orderByField.isEmpty()) {
			log.info("Will sort github projects by field " + orderByField);
			searchUrl += "&sort=" + orderByField;
		}
		log.info("Github projects search URL is " + searchUrl);
		final List<GithubProject> results = new LinkedList<>();
		final int limitOutput = (limit <= 0 || limit > projectSearchLimit) ? projectSearchLimit : limit;
		try {
			final Map<String, Object> searchResultsParsed = parser.parse(new URL(searchUrl).openStream());
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
						// unfortunately github search does not support limit so
						// we have to do it manually here
						if (results.size() == limitOutput) {
							log.info("Limiting number of github projects to " + limitOutput);
							break;
						}
					}
				} else {
					log.severe("Was not able to find [items] in parsed search results. Did GitHub API change?");
				}
			} else {
				log.severe("Was not able to parse search results when accessing " + searchUrl);
			}
		} catch (final MalformedURLException mue) {
			log.severe("Malformed URL " + searchUrl);
		} catch (final IOException ie) {
			log.log(Level.SEVERE, "IO exception while searching for projects on " + searchUrl, ie);
		}
		if (log.isLoggable(Level.FINE)) {
			log.fine("Search results are " + results);
		}
		return results;
	}

}
