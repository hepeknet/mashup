package com.test.mashup.github;

import java.util.List;

public interface GithubProjectFinder {

	List<GithubProject> findProjects(String keyword);

	List<GithubProject> findProjects(String keyword, int limit, String orderByField);

}