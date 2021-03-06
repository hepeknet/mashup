package com.test.mashup.retry;

/**
 * The exception that is thrown by {@link RetryPolicy} when we failed to recover
 * and do not want to attempt recovery any more.
 * 
 * Code using {@link RetryPolicy} can decide to try further recovery actions
 * based on this exception.
 * 
 * @author borisa
 * @see RetryPolicy
 *
 */
public class RetryFailedException extends RuntimeException {

	public RetryFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}