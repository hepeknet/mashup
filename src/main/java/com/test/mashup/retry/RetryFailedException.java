package com.test.mashup.retry;

public class RetryFailedException extends RuntimeException {

	public RetryFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
