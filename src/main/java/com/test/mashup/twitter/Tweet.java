package com.test.mashup.twitter;

public class Tweet {

	private final String id;
	private final String text;
	private final String user;
	private final Integer retweetCount;

	public Tweet(String id, String text, String user, Integer retweetCount) {
		this.id = id;
		this.text = text;
		this.user = user;
		this.retweetCount = retweetCount;
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getUser() {
		return user;
	}

	public Integer getRetweetCount() {
		return retweetCount;
	}

	@Override
	public String toString() {
		return "Tweet [" + (id != null ? "id=" + id + ", " : "") + (text != null ? "text=" + text + ", " : "")
				+ (user != null ? "user=" + user + ", " : "") + "retweetCount=" + retweetCount + "]";
	}

}
