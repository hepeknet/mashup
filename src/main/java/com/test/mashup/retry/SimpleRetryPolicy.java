package com.test.mashup.retry;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Simple retry policy with backoff and maximum number of attempts. New policies
 * can be added using exponential backoff for example, compensation actions in
 * case of failure etc.
 * 
 * Ideally we would use some external library for this - like Spring.
 * 
 * @author borisa
 *
 * @param <V>
 */
public class SimpleRetryPolicy<V> implements RetryPolicy<V> {

	private final Logger log = Logger.getLogger(getClass().getName());

	private final String name;
	private final int maxAttempts;
	private final long fixedBackOffPeriodMillis;

	public SimpleRetryPolicy(String name, int maxAttempts, long fixedBackOffPeriodMillis) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name must not be null or empty");
		}
		if (maxAttempts <= 0) {
			throw new IllegalArgumentException("Max attempts must be greater than 0");
		}
		if (fixedBackOffPeriodMillis < 0) {
			throw new IllegalArgumentException("Backoff period must be >= 0");
		}
		this.name = name;
		this.maxAttempts = maxAttempts;
		this.fixedBackOffPeriodMillis = fixedBackOffPeriodMillis;
		log.info("Retry policy for " + name + ", maxAttempts=" + maxAttempts + ", fixedBackoffPeriodMs="
				+ fixedBackOffPeriodMillis);
	}

	@Override
	public V execute(Callable<V> e) {
		if (e == null) {
			throw new IllegalArgumentException("Execution must not be null");
		}
		log.fine("Executing call for " + name + ", maxAttempts=" + maxAttempts + ", backoff="
				+ fixedBackOffPeriodMillis);
		V v = null;
		Exception lastThrownException = null;
		// try to recover and either return result in case of success or last
		// thrown Exception in case of failure
		for (int i = 1; i <= maxAttempts; i++) {
			try {
				v = e.call();
				log.fine("Successfully executed " + name + ". Total attempts = " + i);
				return v;
			} catch (final Exception exc) {
				log.info("Caught exception for " + name + ", maxAttempts=" + maxAttempts + ", currentAttempt=" + i);
				lastThrownException = exc;
				if (fixedBackOffPeriodMillis > 0) {
					try {
						log.fine("Waiting for " + fixedBackOffPeriodMillis + " ms - retry policy " + name
								+ ", attemptNumber = " + i);
						TimeUnit.MILLISECONDS.sleep(fixedBackOffPeriodMillis);
					} catch (final InterruptedException e1) {
						// do nothing here
					}
				}
			}
		}
		throw new RetryFailedException(
				"Failed to execute retry policy " + name + " after " + maxAttempts + " attempts!", lastThrownException);
	}

}
