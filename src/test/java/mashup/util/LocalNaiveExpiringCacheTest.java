package mashup.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.test.mashup.util.ExpiringCache;
import com.test.mashup.util.LocalNaiveExpiringCache;

public class LocalNaiveExpiringCacheTest {

	private final ExpiringCache<String> cache = new LocalNaiveExpiringCache<String>();

	@Test(expected = IllegalArgumentException.class)
	public void testNullKey() {
		cache.put(null, "", 0, TimeUnit.SECONDS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullUnit() {
		cache.put("abc", "abc", 0, null);
	}

	@Test
	public void testSimple() throws InterruptedException {
		final String key = "key";
		final String val = "val";
		cache.put(key, val, 1, TimeUnit.SECONDS);
		assertEquals(val, cache.get(key));
		TimeUnit.SECONDS.sleep(2);
		assertNull(cache.get(key));
	}

	@Test
	public void testMultiple() throws InterruptedException {
		final String key1 = "key1";
		final String val1 = "val1";

		final String key2 = "key2";
		final String val2 = "val2";
		cache.put(key1, val1, 1, TimeUnit.SECONDS);
		cache.put(key2, val2, 5, TimeUnit.SECONDS);
		assertEquals(val1, cache.get(key1));
		assertEquals(val2, cache.get(key2));
		TimeUnit.SECONDS.sleep(2);
		assertNull(cache.get(key1));
		assertEquals(val2, cache.get(key2));
		TimeUnit.SECONDS.sleep(4);
		assertNull(cache.get(key2));
	}

}
