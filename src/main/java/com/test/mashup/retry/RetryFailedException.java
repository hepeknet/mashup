package com.test.mashup.retry;

/**
 * The exception that is thrown by {@code RetryPolicy} when we failed to recover
 * and do not want to attempt recovery any more.
 * 
 * @author borisa
 *
 */
public class RetryFailedException extends RuntimeException {

	public RetryFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
