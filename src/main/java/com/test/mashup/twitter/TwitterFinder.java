package com.test.mashup.twitter;

import java.util.List;

public interface TwitterFinder {

	/**
	 * Searches twitter based on given keyword.
	 * 
	 * TODO: add pagination support based on Twitter API and expose pagination
	 * support to users of this class.
	 * 
	 * @param keyword
	 *            to use for search. Must not be null or empty string.
	 * @return a list of tweets containing given keyword. List will have a
	 *         limited size based on configuration.
	 */
	List<Tweet> searchTwitter(String keyword);

}