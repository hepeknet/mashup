package com.test.mashup.github;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class GithubProjectFinderTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNull() {
		new GithubProjectFinder().findProjects(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmpty() {
		new GithubProjectFinder().findProjects(" ");
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidJsonFormat() {
		final String keyword = "key1";
		final GithubProjectFinder finder = spy(GithubProjectFinder.class);
		when(finder.buildUrl(keyword, 10, "star")).thenReturn("http://google.com");
		finder.findProjects(keyword, 10, "star");
	}

	@Test(expected = IllegalStateException.class)
	public void testNonExistentURL() {
		final String keyword = "key2";
		final GithubProjectFinder finder = spy(GithubProjectFinder.class);
		when(finder.buildUrl(keyword, 10, "star")).thenReturn("http://does-not-exist-website.www/aaa");
		finder.findProjects(keyword, 10, "star");
	}

	@Test(expected = RuntimeException.class)
	public void testParsedResultsNull() throws IOException {
		final String keyword = "key3";
		final GithubProjectFinder finder = spy(GithubProjectFinder.class);
		doReturn(null).when(finder).getParsedResults(any(String.class));
		finder.findProjects(keyword, 10, "star");
	}

	@Test(expected = RuntimeException.class)
	public void testParsedResultsEmpty() throws IOException {
		final String keyword = "key4";
		final GithubProjectFinder finder = spy(GithubProjectFinder.class);
		doReturn(new HashMap<>()).when(finder).getParsedResults(any(String.class));
		finder.findProjects(keyword, 10, "star");
	}

	@Test(expected = RuntimeException.class)
	public void testParsedResultsNoItems() throws IOException {
		final Map<String, Object> result = new HashMap<>();
		result.put("k", "v");
		final String keyword = "key5";
		final GithubProjectFinder finder = spy(GithubProjectFinder.class);
		doReturn(result).when(finder).getParsedResults(any(String.class));
		finder.findProjects(keyword, 10, "star");
	}

	@Test
	public void testParsedResultsPartial() throws IOException {
		final Map<String, Object> project = new HashMap<>();
		project.put("name", "p1");
		project.put("forks", new Integer(100));
		final List<Map<String, Object>> projects = new LinkedList<>();
		projects.add(project);
		final Map<String, Object> results = new HashMap<>();
		results.put("items", projects);
		final String keyword = "key6";
		final GithubProjectFinder finder = spy(GithubProjectFinder.class);
		doReturn(results).when(finder).getParsedResults(any(String.class));
		final List<GithubProject> found = finder.findProjects(keyword, 10, "star");
		assertEquals(1, found.size());
		assertEquals("p1", found.get(0).getName());
		assertEquals(new Integer(100), found.get(0).getForks());
	}

	@Test
	public void testLimitWorks() throws IOException {
		final int limit = 5;
		final List<Map<String, Object>> projects = new LinkedList<>();
		for (int i = 0; i < 100; i++) {
			final Map<String, Object> project = new HashMap<>();
			project.put("name", "p" + i);
			project.put("forks", new Integer(i));
			projects.add(project);
		}
		final Map<String, Object> results = new HashMap<>();
		results.put("items", projects);
		final String keyword = "key6";
		final GithubProjectFinder finder = spy(GithubProjectFinder.class);
		doReturn(results).when(finder).getParsedResults(any(String.class));
		final List<GithubProject> found = finder.findProjects(keyword, limit, "star");
		assertEquals(limit, found.size());
	}

}
