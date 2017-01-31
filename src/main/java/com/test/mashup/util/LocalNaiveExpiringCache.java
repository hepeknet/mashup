package com.test.mashup.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Very naive implementation of local expiring cache. Ideally we would use some
 * 3PP instead of this.
 * 
 * This implementation uses {@code DelayQueue} for expiration. No need for
 * background threads to clear the cache since clearing is done on every get
 * request.
 * 
 * @author borisa
 *
 */
public class LocalNaiveExpiringCache implements ExpiringCache {

	private final int maxCacheSize = ConfigurationUtil.getInt(Constants.LOCAL_CACHE_MAX_SIZE_PROPERTY_NAME);

	private final Logger log = Logger.getLogger(getClass().getName());

	/*
	 * Map contains key-value mappings for this cache
	 */
	private final Map<String, Object> cache = new ConcurrentHashMap<>();

	/*
	 * Delay queue knows when each key expires and is synchronized with the
	 * cache Map
	 */
	private final DelayQueue<ExpiringElement> validKeys = new DelayQueue<>();

	@Override
	public void put(String key, Object value, long expiration, TimeUnit unit) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key must not be null or empty");
		}
		if (unit == null) {
			throw new IllegalArgumentException("Time unit must not be null");
		}
		// for expiration <=0 we do not cache, expire immediately
		if (expiration > 0) {
			checkMaxCacheSizeAndPurgeIfNeeded();
			final long expirationMillis = unit.toMillis(expiration);
			final ExpiringElement exp = new ExpiringElement(key, expirationMillis);
			validKeys.put(exp);
			cache.put(key, value);
		}
	}

	private void checkMaxCacheSizeAndPurgeIfNeeded() {
		if (cache.size() > maxCacheSize) {
			log.info("Local cache exceeded max cache size of " + maxCacheSize + " will purge it...");
			cache.clear();
			validKeys.clear();
			log.info("Cleared local cache");
		}
	}

	@Override
	public Object get(String key) {
		if (key == null || key.isEmpty()) {
			return null;
		}
		removeExpiredKeys();
		return cache.get(key);
	}

	private void removeExpiredKeys() {
		log.fine("Removing expired keys");
		final List<ExpiringElement> expiredKeys = new LinkedList<>();
		validKeys.drainTo(expiredKeys);
		for (final ExpiringElement ee : expiredKeys) {
			final String eKey = ee.key;
			cache.remove(eKey);
		}
		log.info("In total removed " + expiredKeys.size() + " expired keys from cache...");
	}

	class ExpiringElement implements Delayed {

		private final String key;
		private final long startTime;

		public ExpiringElement(String data, long delay) {
			this.key = data;
			this.startTime = System.currentTimeMillis() + delay;
		}

		@Override
		public int compareTo(Delayed o) {
			if (this.startTime < ((ExpiringElement) o).startTime) {
				return -1;
			}
			if (this.startTime > ((ExpiringElement) o).startTime) {
				return 1;
			}
			return 0;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			final long diff = startTime - System.currentTimeMillis();
			return unit.convert(diff, TimeUnit.MILLISECONDS);
		}

	}

}