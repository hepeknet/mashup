package com.test.mashup;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	
	private static final JsonParser parser = new SimpleNativeJsonParser();

	static String key = "uh30lBHqcCKF1AHfhKMeHok51";
	static String secret = "Fnu5zuaZCo7yh2dSqKRh2WhQAz0HtHrkq9kpIjKp3ZE6SqKaMp";

	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) throws Exception {
		//parse();
		final String post = "https://api.twitter.com/oauth2/token";
		final String bearer = getCredentials(post);
		System.out.println("Bearer is " + bearer);
		executeHTTP("https://api.twitter.com/1.1/search/tweets.json?q=ReactiveX", bearer);
	}

	public static void parse() throws Exception {
		final URL url = new URL("https://api.github.com/search/repositories?q=reactive&sort=stars");
		final String page = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
		System.out.println(page);
		final Map contents = parser.parse(page);
		final Object o = contents.get("items");
		System.out.println(o);
		final Object[] res = (Object[]) o;
		for(final Object r : res){
			System.out.println(r);
		}
		// final Object obj = se.eval("var obj = " + page + ";");
		// System.out.println(obj);
	}

	public static void executeHTTP(String urlStr, String bearer) throws Exception {
		LOG.log(Level.INFO, urlStr);
		final URL url = new URL(urlStr);
		final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setRequestMethod("GET");
		final String base64 = Base64.getEncoder().encodeToString(bearer.getBytes());
		httpCon.setRequestProperty("Authorization", "Bearer " + bearer);
		httpCon.connect();
		try {
			final String page = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
			System.out.println(page);
		} catch (final Exception exc) {
			final String message = httpCon.getResponseMessage();
			System.out.println(message);
			if (httpCon.getErrorStream() != null) {
				final String err = new Scanner(httpCon.getErrorStream(), "UTF-8").useDelimiter("\\A").next();
				System.out.println("Det: " + err);
			}
		}
		if (httpCon.getInputStream() != null) {
			final String body = new Scanner(httpCon.getInputStream(), "UTF-8").useDelimiter("\\A").next();
			System.out.println("IS : " + body);
		}
	}

	public static String getCredentials(String urlStr) throws Exception {
		LOG.log(Level.INFO, urlStr);
		final URL url = new URL(urlStr);
		final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setRequestMethod("POST");
		final String enc = key + ":" + secret;
		final String base64 = Base64.getEncoder().encodeToString(enc.getBytes());
		System.out.println("Encoded is " + base64);
		httpCon.setRequestProperty("Authorization", "Basic " + base64);
		httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		final byte[] postData = "grant_type=client_credentials".getBytes(StandardCharsets.UTF_8);
		try (DataOutputStream wr = new DataOutputStream(httpCon.getOutputStream())) {
			wr.write(postData);
		}
		httpCon.connect();
		final String message = httpCon.getResponseMessage();
		System.out.println("code: " + httpCon.getResponseCode());
		System.out.println("Err: " + message);
		try {
			//final String page = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
			//System.out.println(page);
		} catch (final Exception exc) {
			exc.printStackTrace();
			
			if (httpCon.getErrorStream() != null) {
				final String err = new Scanner(httpCon.getErrorStream(), "UTF-8").useDelimiter("\\A").next();
				System.out.println("Det: " + err);
			}
		}
		if (httpCon.getInputStream() != null) {
			final String body = new Scanner(httpCon.getInputStream(), "UTF-8").useDelimiter("\\A").next();
			System.out.println("IS : " + body);
			final Map<String, Object> parsed = parser.parse(body);
			System.out.println(parsed);
			System.out.println(parsed.get("token_type"));
			final String bearer = (String) parsed.get("access_token");
			System.out.println(parsed.get("access_token"));
			return bearer;
		}
		return null;
	}

}
