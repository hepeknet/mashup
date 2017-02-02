package com.test.mashup.retry;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Used for retrying execution. Does not use its own threading but uses whatever
 * thread was given to it.
 * 
 * Implementations of this class are not thread safe. Ideally should be replaced
 * by some 3PP APIs - like Spring.
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

	/**
	 * Attempts to execute given execution according to specified policy
	 * implementation. Always uses calling thread for execution and does not
	 * spawn its own threads.
	 * 
	 * @param e
	 *            the execution to be attempted
	 * @param retryOnlyFor
	 *            the list of Exception considered for retry. All other
	 *            exceptions will be propagated to caller.
	 * 
	 * @return the result of execution or throw last caught exception in case
	 *         when no more execution attempts should be performed
	 */
	V execute(Callable<V> e, List<Exception> retryOnlyFor);

}