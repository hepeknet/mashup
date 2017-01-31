package com.test.mashup;

import java.util.List;

/**
 * POJO used for final results presented as output of our search.
 * 
 * @author borisa
 *
 */
public class OutputResult {

	private List<GithubProjectWithTweets> projects;

	public List<GithubProjectWithTweets> getProjects() {
		return projects;
	}

	public void setProjects(List<GithubProjectWithTweets> projects) {
		this.projects = projects;
	}

}