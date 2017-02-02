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
 * 3PP instead of this implementation (like Guava, EHCache, Hazelcast,
 * Infinispan...)
 * 
 * This implementation uses {@link DelayQueue} for expiration. There is no need
 * for background threads to clear the cache periodically since clearing is done
 * on every get request - which is a good compromise between speed and accuracy.
 * 
 * Also, cache will self-protect by limiting number of items that can be cached
 * at any point in time. This will have performance impact but will prevent OOM
 * exceptions and GC problems.
 * 
 * @author borisa
 *
 */
public class LocalExpiringCache<T> implements ExpiringCache<T> {

	private final Logger log = Logger.getLogger(getClass().getName());

	private final int maxCacheSize = ConfigurationUtil.getInt(Constants.LOCAL_CACHE_MAX_SIZE_PROPERTY_NAME);

	/*
	 * Map contains key-value mappings for this cache
	 */
	private final Map<String, T> cache = new ConcurrentHashMap<>();

	/*
	 * Delay queue knows when each key expires and is synchronized with the
	 * cache Map. Whenever key expires in this queue we also expire value in the
	 * map. Expiration check is done on each get request.
	 */
	private final DelayQueue<ExpiringElement> validKeys = new DelayQueue<>();

	@Override
	public void put(String key, T value, long expiration, TimeUnit unit) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key must not be null or empty");
		}
		if (unit == null) {
			throw new IllegalArgumentException("Time unit must not be null");
		}
		if (value == null) {
			throw new IllegalArgumentException("Value must not be null");
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

	/*
	 * In case when cache grows too big we clear it. This will hurt performance
	 * but will keep application healthy. Improvement would be to remove only
	 * oldest value from cache but that would require a lot of code and it is
	 * better to switch to 3PP to do this.
	 */
	private void checkMaxCacheSizeAndPurgeIfNeeded() {
		if (cache.size() > maxCacheSize) {
			log.info("Local cache exceeded max cache size of " + maxCacheSize + " will purge it...");
			cache.clear();
			validKeys.clear();
			log.info("Cleared local cache");
		}
	}

	@Override
	public T get(String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key must not be null or empty string");
		}
		// first remove expired keys/values
		removeExpiredKeys();
		return cache.get(key);
	}

	/*
	 * Find all expired keys and then remove cached values associated with those
	 * keys.
	 */
	private void removeExpiredKeys() {
		log.fine("Removing expired keys");
		final List<ExpiringElement> expiredKeys = new LinkedList<>();
		// atomically find expired keys and drain them to local collection
		validKeys.drainTo(expiredKeys);
		for (final ExpiringElement ee : expiredKeys) {
			final String eKey = ee.key;
			cache.remove(eKey);
		}
		log.info("In total removed " + expiredKeys.size() + " expired keys from cache...");
	}

	/**
	 * Internal implementation used for key expiration.
	 * 
	 * @author borisa
	 *
	 */
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