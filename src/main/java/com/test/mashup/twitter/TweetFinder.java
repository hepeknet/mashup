package com.test.mashup.twitter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.test.mashup.ConfigurationUtil;
import com.test.mashup.Constants;
import com.test.mashup.JsonParser;
import com.test.mashup.SimpleNativeJsonParser;
import com.test.mashup.StringUtil;

// TODO: add pagination & max number of tweets
// TODO: fix error handlings
//TODO: externalize config
//TODO: http code handling and retries
public class TweetFinder {

	private static final String REQUIRED_BEARER_TOKEN_TYPE = "bearer";

	private final String baseSearchUrl = ConfigurationUtil.getString(Constants.TWITTER_SEARCH_BASE_URL_PROPERTY_NAME);
	private final String bearerUrl = ConfigurationUtil.getString(Constants.TWITTER_BEARER_URL_PROPERTY_NAME);

	private final String key = ConfigurationUtil.getString(Constants.TWITTER_AUTH_KEY_PROPERTY_NAME);
	private final String secret = ConfigurationUtil.getString(Constants.TWITTER_AUTH_SECRET_PROPERTY_NAME);

	private final Logger log = Logger.getLogger(getClass().getName());

	private static final JsonParser parser = new SimpleNativeJsonParser();

	public List<Tweet> searchTwitter(String keyword) {
		if (keyword == null) {
			throw new IllegalArgumentException("Keyword must not be null");
		}
		final List<Tweet> tweets = new LinkedList<>();
		final String searchUrl = baseSearchUrl + keyword;
		try {
			final String bearer = getBearer(bearerUrl);
			final URL url = new URL(searchUrl);
			final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setDoInput(true);
			httpCon.setRequestMethod("GET");
			httpCon.setRequestProperty("Authorization", "Bearer " + bearer);
			httpCon.connect();
			try {
				final int httpCode = httpCon.getResponseCode();
				log.info("Response code for twitter search [" + keyword + "] is " + httpCode);
			} catch (final Exception exc) {
				handleHttpConnectionError(httpCon, "twitter search for [" + keyword + "]");
			}
			final InputStream is = httpCon.getInputStream();
			if (is != null) {
				final String responseBody = StringUtil.inputStreamToString(is);
				if (log.isLoggable(Level.FINE)) {
					log.fine("Search by [" + keyword + "] returned response " + responseBody);
				}
				final Map<String, Object> mapTweets = parser.parse(responseBody);
				final Object statuses = mapTweets.get("statuses");
				if (statuses != null) {
					final List<Map<String, Object>> itemList = (List<Map<String, Object>>) statuses;
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
				}
			} else {
				log.warning("Did not find any body when searching twitter for [" + keyword + "]");
			}
		} catch (final IOException ie) {
			log.log(Level.SEVERE, "IOException while searching twitter for [" + keyword + "]", ie);
		}
		return tweets;
	}

	private String getBearer(String urlStr) throws IOException {
		final URL url = new URL(urlStr);
		final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setRequestMethod("POST");
		final String enc = key + ":" + secret;
		final String base64 = Base64.getEncoder().encodeToString(enc.getBytes());
		httpCon.setRequestProperty("Authorization", "Basic " + base64);
		httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		final byte[] postData = "grant_type=client_credentials".getBytes(StandardCharsets.UTF_8);
		try (DataOutputStream wr = new DataOutputStream(httpCon.getOutputStream())) {
			wr.write(postData);
		}
		String bearer = null;
		try {
			httpCon.connect();
			final String message = httpCon.getResponseMessage();
			final int code = httpCon.getResponseCode();
			log.info("Got http code " + code + " and message " + message + " while performing bearer search");
			if (httpCon.getInputStream() != null) {
				final String body = StringUtil.inputStreamToString(httpCon.getInputStream());
				final Map<String, Object> parsed = parser.parse(body);
				log.info("Checking token type");
				final String tokenType = (String) parsed.get("token_type");
				if (REQUIRED_BEARER_TOKEN_TYPE.equals(tokenType)) {
					bearer = (String) parsed.get("access_token");
				} else {
					log.warning("Got wrong token type for bearer [" + tokenType + "]. Expected "
							+ REQUIRED_BEARER_TOKEN_TYPE);
				}
			}
		} catch (final Exception exc) {
			handleHttpConnectionError(httpCon, "bearer fetch");
		}
		return bearer;
	}

	private void handleHttpConnectionError(HttpURLConnection conn, String phase) throws IOException {
		final String message = conn.getResponseMessage();
		String errorDescription = null;
		if (conn.getErrorStream() != null) {
			errorDescription = StringUtil.inputStreamToString(conn.getErrorStream());
		}
		log.severe(
				"Caught exception performing " + phase + ". Error: " + message + ", description: " + errorDescription);
	}

}
