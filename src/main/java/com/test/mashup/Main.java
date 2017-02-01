package com.test.mashup;

import java.util.logging.Logger;

import com.test.mashup.json.JsonParser;
import com.test.mashup.metrics.Histogram;

/**
 * Main entry point into application. Ideally this should be replaced with some
 * kind of REST endpoint (Jetty, Spring boot...) if this is to be exposed as
 * some kind of microservice and deployed in distributed fashion.
 *
 * @author borisa
 *
 */
public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	private static JsonParser parser = DependenciesFactory.createParser();

	/*
	 * Tracking and exposing internal statistics
	 */
	private static Histogram appStats = DependenciesFactory.createMetrics()
			.getHistogram("MashupStatisticsExecutionTimeMs");

	public static void main(String[] args) throws Exception {
		final MashupApp mashupApp = new MashupApp();
		do {
			printUsage();
			final String line = System.console().readLine();
			if (line != null && !line.trim().isEmpty()) {
				final String trimmed = line.trim();
				LOG.info("Entered keyword is [" + trimmed + "]");
				searchAndPrintResult(mashupApp, trimmed);
			} else {
				break;
			}
		} while (true);
		System.out.println("Exiting...");
		mashupApp.shutdown();
	}

	/*
	 * Separating statistics and outputting result from the main search
	 * functionality. This method is responsible to invoke main functionality
	 * and then print result in appropriate form.
	 */
	private static void searchAndPrintResult(MashupApp app, String keyword) {
		final long start = System.currentTimeMillis();
		final OutputResult result = app.executeSearch(keyword);
		final String json = parser.toJson(result);
		final long totalMs = System.currentTimeMillis() - start;
		appStats.update(totalMs);
		System.out.println("========================================================");
		System.out.println("In total found " + result.getProjects().size() + " projects for keyword [" + keyword + "]");
		System.out.println("Mashup for keyword [" + keyword + "] is:");
		System.out.println("========================================================");
		System.out.println(json);
		System.out.println("========================================================");
	}

	private static void printUsage() {
		System.out.println("\n\nEnter keyword to search for (enter to exit):");
	}

}
