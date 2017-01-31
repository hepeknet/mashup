package com.test.mashup;

import java.util.List;

import com.test.mashup.github.GithubProject;
import com.test.mashup.twitter.Tweet;

/**
 * POJO used by {@code OutputResult} for presenting final results of our search.
 * 
 * @author borisa
 *
 */
public class GithubProjectWithTweets {

	private GithubProject project;
	private List<Tweet> tweets;

	public GithubProject getProject() {
		return project;
	}

	public void setProject(GithubProject project) {
		this.project = project;
	}

	public List<Tweet> getTweets() {
		return tweets;
	}

	public void setTweets(List<Tweet> tweets) {
		this.tweets = tweets;
	}

	@Override
	public String toString() {
		return "GithubProjectWithTweets [" + (project != null ? "project=" + project + ", " : "")
				+ (tweets != null ? "tweets=" + tweets : "") + "]";
	}

}