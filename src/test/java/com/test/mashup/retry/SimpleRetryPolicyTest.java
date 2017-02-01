package com.test.mashup.retry;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.junit.Test;

public class SimpleRetryPolicyTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNullName() {
		new SimpleRetryPolicy<>(null, 0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadAttempts() {
		new SimpleRetryPolicy<>("ab", 0, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadBackoff() {
		new SimpleRetryPolicy<>("ab", 10, -1);
	}

	@Test(expected = RetryFailedException.class)
	public void testSimple() {
		final Execution e = new Execution(3, "res1");
		final SimpleRetryPolicy<String> p = new SimpleRetryPolicy<>("p1", 1, 0);
		p.execute(e);
	}

	@Test
	public void testSimple2() {
		final Execution e = new Execution(3, "res2");
		final SimpleRetryPolicy<String> p = new SimpleRetryPolicy<>("p2", 5, 10);
		final String res = p.execute(e);
		assertEquals("res2", res);
	}

	private static class Execution implements Callable<String> {

		private final int throwExceptionsUntilAttempt;
		private final String result;
		private int attempts = 0;

		public Execution(int throwExceptionsUntilAttempt, String result) {
			this.throwExceptionsUntilAttempt = throwExceptionsUntilAttempt;
			this.result = result;
		}

		@Override
		public String call() throws Exception {
			if (attempts < throwExceptionsUntilAttempt) {
				attempts++;
				throw new RuntimeException("Must throw exception");
			} else {
				return result;
			}
		}

	}

}
