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

public class TweetFinderImplTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNull() {
		final TwitterFinder finder = new TweetFinderImpl();
		finder.searchTwitter(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmpty() {
		final TwitterFinder finder = new TweetFinderImpl();
		finder.searchTwitter(" ");
	}

	@Test
	public void testBearerCanNotBeFoundNoRecoveryAttempted() throws Exception {
		final TweetFinderImpl finder = spy(TweetFinderImpl.class);
		doThrow(IOException.class).when(finder).getBearer(any(String.class));
		try {
			finder.searchTwitter("abc");
		} catch (final IllegalStateException ise) {
			assertTrue(true);
		}
		// verify no recovery attempted because failed bearer search
		verify(finder, never()).tryToGetTweets(any(String.class), eq(false));
		verify(finder, times(1)).tryToGetTweets(any(String.class), eq(true));
	}

	@Test
	public void testInvalidBearerWithRecovery() throws Exception {
		final TweetFinderImpl finder = spy(TweetFinderImpl.class);
		doReturn("Invalid-bearer").when(finder).getBearer(any(String.class));
		try {
			finder.searchTwitter("abc");
		} catch (final RuntimeException re) {
			assertTrue(true);
		}
		// verify we did recovery because AUTH failed
		verify(finder, times(1)).tryToGetTweets(any(String.class), eq(false));
		verify(finder, times(1)).tryToGetTweets(any(String.class), eq(true));
	}

	@Test(expected = RuntimeException.class)
	public void testBearerWrongURL() throws IOException {
		final TweetFinderImpl finder = spy(TweetFinderImpl.class);
		finder.getBearer("http://google.com");
	}

	@Test(expected = RuntimeException.class)
	public void testBearerNonExistentURL() throws IOException {
		final TweetFinderImpl finder = spy(TweetFinderImpl.class);
		finder.getBearer("http://does-not-exist-url.wwwa/a/b");
	}

}
