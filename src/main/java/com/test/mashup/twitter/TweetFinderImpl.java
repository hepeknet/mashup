package com.test.mashup.twitter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.test.mashup.DependenciesFactory;
import com.test.mashup.json.JsonParser;
import com.test.mashup.metrics.Counter;
import com.test.mashup.metrics.Histogram;
import com.test.mashup.util.ConfigurationUtil;
import com.test.mashup.util.Constants;
import com.test.mashup.util.StringUtil;

// TODO: use Twitter pagination - not needed in current release

/**
 *
 * Responsible for searching twitter. Ideally this would use some external
 * library like tweeter4j but since we are not allowed to do that then we have
 * to write a lot of plumbing code ourselves.
 * 
 * This class does some basic caching (like bearer) and basic recovery based on
 * returned HTTP codes. It assumes that some higher level code will perform
 * retry and recovery.
 * 
 * Implementation looks ugly because we had to use {@link HttpURLConnection} to
 * access Twitter API.
 * 
 * Implementation exposes some basic statistics.
 *
 * @author borisa
 *
 */
public class TweetFinderImpl implements TwitterFinder {

	private final Logger log = Logger.getLogger(getClass().getName());

	/*
	 * Configuration parameters
	 */
	private final String baseSearchUrl = ConfigurationUtil.getStringRequired(Constants.TWITTER_SEARCH_BASE_URL_PROPERTY_NAME);
	private final String bearerUrl = ConfigurationUtil.getStringRequired(Constants.TWITTER_BEARER_URL_PROPERTY_NAME);
	private final String bearerRequiredTokenType = ConfigurationUtil.getStringRequired(Constants.TWITTER_BEARER_REQUIRED_TOKEN_TYPE_PROPERTY_NAME);
	private final int maxTweetsPerSearch = ConfigurationUtil.getInt(Constants.TWITTER_SEARCH_MAX_TWEETS_PROPERTY_NAME);

	private final int httpConnectionTimeoutMillis = ConfigurationUtil.getInt(Constants.HTTP_CONNECTION_TIMEOUT_MILLIS_PROPERTY_NAME);

	private final String key = ConfigurationUtil.getStringRequired(Constants.TWITTER_AUTH_KEY_PROPERTY_NAME);
	private final String secret = ConfigurationUtil.getStringRequired(Constants.TWITTER_AUTH_SECRET_PROPERTY_NAME);

	/*
	 * Parser for JSON
	 */
	private final JsonParser parser = DependenciesFactory.createParser();

	/*
	 * Used for exposing metrics to external world
	 */
	private final Histogram tweetSearchStats = DependenciesFactory.createMetrics().getHistogram("TwitterSearchExecutionTimeStats");
	private final Counter tweetSearchFailures = DependenciesFactory.createMetrics().getCounter("TwitterSearchFailures");

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
	@Override
	public List<Tweet> searchTwitter(String keyword) {
		if (keyword == null) {
			throw new IllegalArgumentException("Keyword must not be null");
		}
		if (keyword.trim().isEmpty()) {
			throw new IllegalArgumentException("Keyword must not be empty string");
		}
		try {
			final long start = System.currentTimeMillis();
			final boolean shouldRetryGettingBearerInCaseOfFailure = true;
			final InputStream is = tryToGetTweets(keyword, shouldRetryGettingBearerInCaseOfFailure);
			if (is != null) {
				final String responseBody = StringUtil.inputStreamToString(is);
				if (log.isLoggable(Level.FINE)) {
					log.fine("Search by [" + keyword + "] returned response " + responseBody);
				}
				/*
				 * Ideally all this Map handling would not be needed if we had
				 * more time or were allowed to use 3PP for JSON parsing
				 */
				final Map<String, Object> mapTweets = parser.parse(responseBody);
				final Object statuses = mapTweets.get("statuses");
				if (log.isLoggable(Level.FINE)) {
					log.fine("Statuses object is " + statuses);
				}
				if (statuses != null) {
					final List<Map<String, Object>> itemList = (List<Map<String, Object>>) statuses;
					final List<Tweet> tweets = new LinkedList<>();
					for (final Map<String, Object> item : itemList) {
						final Map<String, Object> itemMap = item;
						final String text = (String) itemMap.get("text");
						final Integer rtCount = (Integer) itemMap.get("retweet_count");
						final String idStr = (String) itemMap.get("id_str");
						String userName = null;
						final Map<String, Object> userObj = (Map<String, Object>) itemMap.get("user");
						if (userObj != null) {
							userName = (String) userObj.get("name");
						}
						final Tweet tweet = new Tweet(idStr, text, userName, rtCount);
						tweets.add(tweet);
					}
					final long totalMs = System.currentTimeMillis() - start;
					// expose some statistics
					tweetSearchStats.update(totalMs);
					return tweets;
				} else {
					tweetSearchFailures.inc();
					throw new IllegalStateException("Did not find [statuses] in twitter response. Possibly API change?");
				}
			} else {
				tweetSearchFailures.inc();
				throw new IllegalStateException("Did not find any body when searching twitter for [" + keyword + "]");
			}
		} catch (final IOException ie) {
			tweetSearchFailures.inc();
			throw new IllegalStateException("IOException while searching twitter for [" + keyword + "]", ie);
		}
	}

	/*
	 * We can cache bearer once we obtain it. It is valid until it is
	 * invalidated manually - as per Twitter API. We detect that in our code and
	 * try to obtain it again.
	 */
	private volatile String cachedBearer = null;

	InputStream tryToGetTweets(String keyword, boolean shouldRetry) throws IOException {
		final String searchUrl = baseSearchUrl + URLEncoder.encode(keyword) + "&count=" + maxTweetsPerSearch;
		log.info("Tweet search url is " + searchUrl);
		/*
		 * As per https://dev.twitter.com/oauth/application-only
		 */
		final String bearer = getBearer(bearerUrl);
		log.info("Found bearer token - proceeding with tweet search...");
		final URL url = new URL(searchUrl);
		final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setConnectTimeout(httpConnectionTimeoutMillis);
		httpCon.setReadTimeout(httpConnectionTimeoutMillis);
		httpCon.setRequestMethod("GET");
		httpCon.setRequestProperty("Authorization", "Bearer " + bearer);
		httpCon.connect();
		try {
			final int httpCode = httpCon.getResponseCode();
			log.info("Response code for twitter search [" + keyword + "] is " + httpCode);
			/*
			 * As per twitter API if bearer has been invalidated we will get 401
			 * code back, so we invalidate cached bearer and try to obtain new
			 * one.
			 */
			if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				log.info("Got unauthorized response when searching for tweets. Will try to obtain bearer again!");
				cachedBearer = null;
				if (shouldRetry) {
					return tryToGetTweets(keyword, false);
				}
			}
		} catch (final IOException exc) {
			handleHttpConnectionError(httpCon, "twitter search for [" + keyword + "] - url for search is [" + searchUrl + "]");
		}
		return httpCon.getInputStream();
	}

	/*
	 * As per https://dev.twitter.com/oauth/application-only
	 */
	String getBearer(String urlStr) throws IOException {
		// if cached then use that, in case when cached does not work anymore it
		// will be invalidated and then we will ask twitter for new bearer token
		if (cachedBearer == null) {
			log.info("Was not able to find cached bearer - will try to obtain it from url [" + urlStr + "]");
			final URL url = new URL(urlStr);
			final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setDoInput(true);
			httpCon.setConnectTimeout(httpConnectionTimeoutMillis);
			httpCon.setReadTimeout(httpConnectionTimeoutMillis);
			httpCon.setRequestMethod("POST");
			final String enc = key + ":" + secret;
			final String base64 = Base64.getEncoder().encodeToString(enc.getBytes());
			httpCon.setRequestProperty("Authorization", "Basic " + base64);
			httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			final byte[] postData = "grant_type=client_credentials".getBytes(StandardCharsets.UTF_8);
			try {
				try (DataOutputStream wr = new DataOutputStream(httpCon.getOutputStream())) {
					wr.write(postData);
				}
				log.info("Trying to connect to get bearer...");
				httpCon.connect();
				final String message = httpCon.getResponseMessage();
				final int code = httpCon.getResponseCode();
				log.info("Got http code " + code + " and message " + message + " while performing bearer search");
				if (httpCon.getInputStream() != null) {
					final String body = StringUtil.inputStreamToString(httpCon.getInputStream());
					final Map<String, Object> parsedBody = parser.parse(body);
					log.info("Checking token type...");
					final String tokenType = (String) parsedBody.get("token_type");
					if (bearerRequiredTokenType.equals(tokenType)) {
						log.info("Successfully got bearer token and cached it");
						cachedBearer = (String) parsedBody.get("access_token");
						log.info("Found bearer and cached its value for further use...");
					} else {
						throw new IllegalStateException("Got wrong token type for bearer [" + tokenType + "]. Expected it to be " + bearerRequiredTokenType
								+ ". Either configuration is bad or Twitter API changed!");
					}
				} else {
					throw new IllegalStateException("Did not find appropriate data in response - unable to get bearer token!");
				}
			} catch (final IOException exc) {
				handleHttpConnectionError(httpCon, "bearer fetch from url [" + urlStr + "]");
			}
		}
		return cachedBearer;
	}

	/*
	 * Have to make sure we can not fail in this method whatever happens. For
	 * example connection might not be in good state, but we still want to
	 * report as much information to user as we can.
	 */
	private void handleHttpConnectionError(HttpURLConnection conn, String phase) throws IOException {
		String errMessage = null;
		try {
			final String message = conn.getResponseMessage();
			log.info("Processing error message for " + conn + " and phase " + phase + ", response message is " + message);
			String errorDescription = "";
			if (conn.getErrorStream() != null) {
				errorDescription = StringUtil.inputStreamToString(conn.getErrorStream());
			}
			final int code = conn.getResponseCode();
			errMessage = "Caught exception performing " + phase + ". Error: " + message + ", error code:" + code + ", description: " + errorDescription;
		} catch (final Exception exc) {
			/*
			 * this can happen if there is no network connectivity or URL set in
			 * configuration does not exist. We still want to report something
			 * to user.
			 */
			final String msg = "Was not able to create connection for " + phase + ". Network or configuration problem!";
			log.severe(msg);
			throw new RuntimeException(msg, exc);
		}
		log.severe(errMessage);
		throw new RuntimeException(errMessage);
	}

}
