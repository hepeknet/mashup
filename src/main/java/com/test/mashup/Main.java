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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Main {

	static String key = "uh30lBHqcCKF1AHfhKMeHok51";
	static String secret = "Fnu5zuaZCo7yh2dSqKRh2WhQAz0HtHrkq9kpIjKp3ZE6SqKaMp";

	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) throws Exception {
		parse();
		final String post = "https://api.twitter.com/oauth2/token";
		// executeHTTP("https://api.twitter.com/1.1/search/tweets.json?q=ReactiveX");
		getCredentials(post);
	}

	public static void parse() throws Exception {
		final URL url = new URL("https://api.github.com/search/repositories?q=reactive&sort=stars");
		final String page = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
		System.out.println(page);
		new ScriptEngineManager().getEngineFactories().forEach(a -> System.out.print(a.getEngineName() + "\n"));
		final ScriptEngine se = new ScriptEngineManager().getEngineByName("javascript");
		final String script = "Java.asJSONCompatible(" + page + ")";
		final Object result = se.eval(script);
		final Map contents = (Map) result;
		contents.forEach((t, u) -> {
			System.out.println(t + " -> " + u);
		});
		final Object o = contents.get("items");
		System.out.println(o);
		// final Object obj = se.eval("var obj = " + page + ";");
		// System.out.println(obj);
	}

	public static void executeHTTP(String urlStr) throws Exception {
		LOG.log(Level.INFO, urlStr);
		final URL url = new URL(urlStr);
		final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("GET");
		final String enc = key + ":" + secret;
		final String base64 = Base64.getEncoder().encodeToString(enc.getBytes());
		System.out.println("Encoded is " + base64);
		httpCon.setRequestProperty("Authorization", "Basic " + urlStr);
		httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		final byte[] postData = "grant_type=client_credentials".getBytes(StandardCharsets.UTF_8);
		try (DataOutputStream wr = new DataOutputStream(httpCon.getOutputStream())) {
			wr.write(postData);
		}
		httpCon.connect();
		try {
			final String page = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
			System.out.println(page);
		} catch (final Exception exc) {
			final String message = httpCon.getResponseMessage();
			System.out.println(message);
		}
	}

	public static void getCredentials(String urlStr) throws Exception {
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
		try {
			final String page = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
			System.out.println(page);
		} catch (final Exception exc) {
			exc.printStackTrace();
			final String message = httpCon.getResponseMessage();
			System.out.println("code: " + httpCon.getResponseCode());
			System.out.println("Err: " + message);
			if (httpCon.getErrorStream() != null) {
				final String err = new Scanner(httpCon.getErrorStream(), "UTF-8").useDelimiter("\\A").next();
				System.out.println("Det: " + err);
			}
			if (httpCon.getInputStream() != null) {
				final String err = new Scanner(httpCon.getInputStream(), "UTF-8").useDelimiter("\\A").next();
				System.out.println("IS : " + err);
			}
		}
	}

}
