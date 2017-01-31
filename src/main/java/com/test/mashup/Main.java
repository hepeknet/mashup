package com.test.mashup;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.test.mashup.github.GithubProject;
import com.test.mashup.github.GithubProjectFinder;
import com.test.mashup.json.JsonParser;
import com.test.mashup.twitter.Tweet;
import com.test.mashup.twitter.TweetFinder;

/**
 * Main entry point into application. Ideally this should be replaced with some
 * kind of REST endpoint (Jetty, Spring boot...) if this is to be exposed as
 * some kind of microservice.
 *
 * @author borisa
 *
 */
public class Main {

	static {
		setupLogging();
	}

	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	private static GithubProjectFinder githubFinder = DependenciesFactory.createGithubProjectFinder();
	private static TweetFinder tweetFinder = DependenciesFactory.createTweetFinder();
	private static JsonParser parser = DependenciesFactory.createParser();

	private static void setupLogging() {
		final InputStream configFile = Main.class.getResourceAsStream("/logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(configFile);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void main1(String[] args) throws Exception {
		String line = null;
		do {
			printUsage();
			line = System.console().readLine();
			LOG.info("Entered keyword is " + line);
			doExecute(line);
		} while (line != null && !line.isEmpty() && !line.equals("quit"));
	}

	public static void main(String[] args) throws Exception {
		// doExecute("reactive");
		// doExecute("reactive");
		doExecuteAsync("reactive");
	}

	private static void doExecuteAsync(String keyword) {
		final List<GithubProject> projects = githubFinder.findProjects(keyword);
		final List<GithubProjectWithTweets> allProjects = projects.stream()
				.map(p -> CompletableFuture.supplyAsync(() -> {
					final List<Tweet> tweets = tweetFinder.searchTwitter(p.getName());
					final GithubProjectWithTweets gt = new GithubProjectWithTweets();
					gt.setProject(p);
					gt.setTweets(tweets);
					return gt;
				})).map(CompletableFuture::join).collect(Collectors.toList());
		final OutputResult result = new OutputResult();
		result.setProjects(allProjects);
		final String json = parser.toJson(result);
		System.out.println(json);
	}

	private static void doExecute(String keyword) {
		final List<GithubProject> projects = githubFinder.findProjects(keyword);
		final List<GithubProjectWithTweets> projectsWithTweets = new LinkedList<>();
		projects.forEach(p -> {
			final List<Tweet> tweets = tweetFinder.searchTwitter(p.getName());
			final GithubProjectWithTweets gt = new GithubProjectWithTweets();
			gt.setProject(p);
			gt.setTweets(tweets);
			projectsWithTweets.add(gt);
		});
		final OutputResult result = new OutputResult();
		result.setProjects(projectsWithTweets);
		final String json = parser.toJson(result);
		System.out.println(json);
	}

	private static void printUsage() {
		System.out.println("Enter keyword to search for (empty to exit):");
	}

}
