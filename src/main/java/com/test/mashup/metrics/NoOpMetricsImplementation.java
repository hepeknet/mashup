package com.test.mashup.metrics;

import java.util.logging.Logger;

/**
 * This implementation is no-op - does not do anything. Here is only to provide
 * API to users and show the idea. Implementation could be done by using some of
 * 3PPs or directly exposing data to logging system or JMX.
 * 
 * @author borisa
 *
 */
public class NoOpMetricsImplementation implements Metrics {

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
				log.info("Updating value of " + uniqueName + " to " + val);
			}
		};
	}

}
