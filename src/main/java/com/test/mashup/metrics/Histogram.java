package com.test.mashup.metrics;

/**
 * Measures the distribution of values in a stream of data. Will expose to
 * external systems percentiles and other statistics about data.
 * 
 * @author borisa
 *
 */
public interface Histogram {

	void update(long val);

}