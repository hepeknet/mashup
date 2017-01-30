package com.test.mashup;

import java.io.InputStream;
import java.util.Map;

/**
 * All implementations of this interface must be thread-safe.
 * 
 * @author borisa
 *
 */
public interface JsonParser {

	/**
	 * Parses given input String as JSON and returns <tt>Map</tt>
	 * 
	 * @param source - input JSON string. Must not be null.
	 * @return parsed JSON as Map object.Returns empty Map in case when input is empty String. 
	 */
	Map<String, Object> parse(String source);
	
	Map<String, Object> parse(InputStream is);
	
}