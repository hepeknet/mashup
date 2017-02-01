package com.test.mashup.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom thread factory used to name threads properly - makes it easier to find
 * deadlocks and find concurrency problems.
 * 
 * @author borisa
 *
 */
public class NamedThreadFactory implements ThreadFactory {

	private final String namePrefix;
	final AtomicLong count = new AtomicLong(0);

	public NamedThreadFactory(String prefix) {
		if (prefix == null || prefix.isEmpty()) {
			throw new IllegalArgumentException("Prefix must not be null or empty string");
		}
		this.namePrefix = prefix;
	}

	@Override
	public Thread newThread(Runnable r) {
		final Thread t = new Thread(r);
		t.setName(this.namePrefix + "-" + count.getAndIncrement());
		return t;
	}

}
