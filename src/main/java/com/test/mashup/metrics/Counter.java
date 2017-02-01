package com.test.mashup.metrics;

/**
 * A counter is a simple incrementing and decrementing 64-bit integer. This can
 * count, for example, number of cache misses, number of failures etc.
 * 
 * @author borisa
 *
 */
public interface Counter {

	void inc();

	void dec();

	void inc(int val);

	void dec(int val);

}