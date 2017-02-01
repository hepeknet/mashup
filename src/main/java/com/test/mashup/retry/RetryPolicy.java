package com.test.mashup.retry;

import java.util.concurrent.Callable;

/**
 * Used for retrying execution. Does not use its own threading but uses whatever
 * thread was given to it.
 * 
 * Implementations are not thread safe.
 * 
 * @author borisa
 *
 * @param <V>
 */
public interface RetryPolicy<V> {

	/**
	 * Attempts to execute given execution according to specified policy
	 * implementation. Always uses calling thread for execution and does not
	 * spawn its own threads.
	 * 
	 * @param e
	 *            the execution to be attempted
	 * @return the result of execution or throw last caught exception in case
	 *         when no more execution attempts should be performed
	 */
	V execute(Callable<V> e);

}