package com.test.mashup.metrics;

import java.util.logging.Logger;

/**
 * This implementation is no-op - does not do anything except logging
 * information. This implementation is here only to provide API to users and
 * show the idea. Implementation could be done by using some of 3PPs (like the
 * one provided by Dropwizard.io) or directly exposing data to logging system or
 * JMX.
 * 
 * I did not have enough time to provide more useful implementation.
 * 
 * @author borisa
 *
 */
public class LoggingOnlyMetricsImplementation implements Metrics {

	private final Logger log = Logger.getLogger(getClass().getName());

	@Override
	public Counter getCounter(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name must not be null or empty");
		}
		return new Counter() {

			String uniqueName = name;

			@Override
			public void inc(int val) {
				log.info("Increasing value for " + uniqueName + " by " + val);
			}

			@Override
			public void inc() {
				log.info("Increasing value for " + uniqueName + " by 1");
			}

			@Override
			public void dec(int val) {
				log.info("Decreasing value for " + uniqueName + " by " + val);
			}

			@Override
			public void dec() {
				log.info("Decreasing value for " + uniqueName + " by 1");
			}
		};
	}

	@Override
	public Histogram getHistogram(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name must not be null or empty");
		}
		return new Histogram() {

			String uniqueName = name;

			@Override
			public void update(long val) {
				log.info("Recording new value for " + uniqueName + "=" + val);
			}
		};
	}

}
