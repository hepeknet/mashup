package com.test.mashup.github;

public class GithubProject {

	private final String name;
	private final String description;

	public GithubProject(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "GithubProject [name=" + name + ", description=" + description + "]";
	}

}
