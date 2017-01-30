package com.test.mashup.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.test.mashup.JsonParser;
import com.test.mashup.SimpleNativeJsonParser;

public class GithubProjectFinder {

	private final Logger log = Logger.getLogger(getClass().getName());

	private static final String BASE_URI = "https://api.github.com/search/repositories?q=";
	private static final int DEFAULT_SEARCH_LIMIT = 10;
	private static final String DEFAULT_SORT_FIELD = "stars";

	private final JsonParser parser = new SimpleNativeJsonParser();

	public List<GithubProject> findProjects(String keyword) {
		return findProjects(keyword, DEFAULT_SEARCH_LIMIT);
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
		String searchUrl = BASE_URI + keyword;
		if (orderByField != null && !orderByField.isEmpty()) {
			log.info("Will sort github projects by field " + orderByField);
			searchUrl += "&sort=" + orderByField;
		}
		log.info("Github projects search URL is " + searchUrl);
		final List<GithubProject> results = new LinkedList<>();
		final int limitOutput = limit <= 0 ? DEFAULT_SEARCH_LIMIT : limit;
		try {
			final URL url = new URL(searchUrl);
			final Map<String, Object> searchResultsParsed = parser.parse(url.openStream());
			if (searchResultsParsed != null && !searchResultsParsed.isEmpty()) {
				final Object foundItems = searchResultsParsed.get("items");
				if (foundItems != null) {
					final List<Map<String, Object>> itemList = (List<Map<String, Object>>) foundItems;
					for (final Map<String, Object> item : itemList) {
						final Map<String, Object> itemMap = item;
						final String name = (String) itemMap.get("name");
						final String description = (String) itemMap.get("description");
						final GithubProject project = new GithubProject(name, description);
						results.add(project);
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
