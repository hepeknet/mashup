package com.test.mashup.util;

import java.util.concurrent.TimeUnit;

/**
 * Cache with expiration.
 * 
 * Ideally we would use some kind of 3PP cache like EHCache, Guava, Infinispan
 * or Hazelcast. But since we are not allowed to use 3PP we create our own
 * interface and simple implementation. This allows us to later swap to better
 * implementation without disrupting the rest of our code too much.
 * 
 * Implementations of this cache must always be thread safe.
 * 
 * @author borisa
 *
 */
public interface ExpiringCache<T> {

	/**
	 * Caches value and associates expiration period with it. After expiration
	 * period key and value will not be available, any retrieval by given key
	 * will return null value.
	 * 
	 * @param key
	 *            under which to remember cached value. Must not be null or
	 *            empty string.
	 * @param value
	 *            the value to cache
	 * @param expiration
	 *            after what period cached value will be removed from cache. If
	 *            this value is <=0 then value will not be cached at all
	 * @param unit
	 *            then unit of time used for expiration. Must not be null.
	 */
	void put(String key, T value, long expiration, TimeUnit unit);

	/**
	 * Returns the value associated with given key. If the key/value par was not
	 * cached returns null. If the key/value pair expired returns null.
	 * 
	 * @param key
	 *            under which value has been cached. Must not be null or empty
	 *            string.
	 * @return the associated value or null in case value expired or never was
	 *         cached at all.
	 */
	T get(String key);

}
