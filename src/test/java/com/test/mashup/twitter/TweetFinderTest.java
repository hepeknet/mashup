package com.test.mashup.twitter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Test;

public class TweetFinderTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNull() {
		final TweetFinder finder = new TweetFinder();
		finder.searchTwitter(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmpty() {
		final TweetFinder finder = new TweetFinder();
		finder.searchTwitter(" ");
	}

	@Test
	public void testBearerCanNotBeFound() throws Exception {
		final TweetFinder finder = spy(TweetFinder.class);
		doThrow(IOException.class).when(finder).getBearer(any(String.class));
		try {
			finder.searchTwitter("abc");
		} catch (final IllegalStateException ise) {
			assertTrue(true);
		}
		verify(finder, never()).tryToGetTweets(any(String.class), eq(false));
		verify(finder, times(1)).tryToGetTweets(any(String.class), eq(true));
	}

	@Test
	public void testInvalidBearerWithRecovery() throws Exception {
		final TweetFinder finder = spy(TweetFinder.class);
		doReturn("Invalid-bearer").when(finder).getBearer(any(String.class));
		try {
			finder.searchTwitter("abc");
		} catch (final RuntimeException re) {
			assertTrue(true);
		}
		verify(finder, times(1)).tryToGetTweets(any(String.class), eq(false));
		verify(finder, times(1)).tryToGetTweets(any(String.class), eq(true));
	}

	@Test(expected = RuntimeException.class)
	public void testBearerWrongURL() throws IOException {
		final TweetFinder finder = spy(TweetFinder.class);
		finder.getBearer("http://google.com");
	}

	@Test(expected = RuntimeException.class)
	public void testBearerNonExistentURL() throws IOException {
		final TweetFinder finder = spy(TweetFinder.class);
		finder.getBearer("http://does-not-exist-url.wwwa/a/b");
	}

}
