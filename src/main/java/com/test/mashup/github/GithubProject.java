package com.test.mashup.github;

/**
 * POJO used internally by our application to represent subset of Github project
 * datastructure.
 * 
 * @author borisa
 *
 */
public class GithubProject {

	private final String name;
	private final String description;
	private final Integer forks;
	private final Integer watchers;
	private final String url;

	public GithubProject(String name, String description, Integer forks, Integer watchers, String url) {
		this.name = name;
		this.description = description;
		this.forks = forks;
		this.watchers = watchers;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Integer getForks() {
		return forks;
	}

	public Integer getWatchers() {
		return watchers;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return "GithubProject [" + (name != null ? "name=" + name + ", " : "")
				+ (description != null ? "description=" + description + ", " : "") + "forks=" + forks + ", watchers="
				+ watchers + ", " + (url != null ? "url=" + url : "") + "]";
	}

}
