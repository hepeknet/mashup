package com.test.mashup.metrics;

/**
 * Factory for metric objects. All metrics are automatically exposed to external
 * system (for example JMX). How exposing works is hidden from users of metrics
 * and is pluggable (configurable).
 * 
 * Ideally we would use 3PP for this (for example
 * http://metrics.dropwizard.io/3.1.0/manual/core/)
 * 
 * All implementations of this class must be thread-safe.
 * 
 * @author borisa
 *
 */
public interface Metrics {

	/**
	 * Creates instance of {@code Counter} whose values will be automatically
	 * exposed to external systems.
	 * 
	 * @param name
	 *            the name of counter. Should be unique and descriptive. Must
	 *            not be null or empty.
	 * @return the counter instance
	 */
	Counter getCounter(String name);

	/**
	 * Creates instance of {@code Histogram} whose values will be automatically
	 * exposed to external systems.
	 * 
	 * @param name
	 *            the name of histogram. Should be unique and descriptive. Must
	 *            not be null or empty.
	 * @return the histogram instance
	 */
	Histogram getHistogram(String name);

}
