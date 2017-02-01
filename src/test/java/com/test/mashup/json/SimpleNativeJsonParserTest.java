package com.test.mashup.json;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.test.mashup.json.JsonParser;
import com.test.mashup.json.SimpleNativeJsonParser;

public class SimpleNativeJsonParserTest {

	private final JsonParser parser = new SimpleNativeJsonParser();

	@Test
	public void testToJsonNative() {
		assertEquals("null", parser.toJson(null));
		assertEquals("1", parser.toJson(new Integer(1)));
		assertEquals("\"abc\"", parser.toJson("abc"));
		assertEquals("false", parser.toJson(false));
		assertEquals("12.2", parser.toJson(new Float(12.2)));
		assertEquals("[1, 2, 3, 4]", parser.toJson(Arrays.asList(new Integer[] { 1, 2, 3, 4 })));
	}

	@Test
	public void testToJsonPojo() {
		final SimplePojo sp = new SimplePojo();
		sp.setX(42);
		sp.setY("ratm");
		final String json = parser.toJson(sp);
		assertEquals("{\"x\" : 42, \"y\" : \"ratm\"}", json);
	}

}
