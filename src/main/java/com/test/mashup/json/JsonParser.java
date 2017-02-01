package com.test.mashup.json;

import java.io.InputStream;
import java.util.Map;

/**
 * Implementation of this interface will be able to parse JSON into Java objects
 * and convert Java objects into JSON string representation. Ideally we should
 * not have to have this interface if we could use some of 3PPs for JSON
 * manipulation.
 * 
 * Implementations provided here will have limited support for conversion given
 * the amount of time provided for this task.
 * 
 * All implementations of this interface must be thread-safe.
 * 
 * Since we are using this parser for multiple different purposes we decided to
 * have generic capability to parse JSON object into Java Map - so that we can
 * use regardless of what POJO is really needed in the end. The downside is that
 * there must be intermediary translation code from Map into internal POJO. The
 * advantage is that we can use this parser for pretty much any JSON type.
 *
 * @author borisa
 *
 */
public interface JsonParser {

	/**
	 * Parses given input String as JSON and returns <tt>Map</tt>
	 *
	 * @param source
	 *            - input JSON string. Must not be null.
	 * @return parsed JSON as Map object.Returns empty Map in case when input is
	 *         empty String.
	 */
	Map<String, Object> parse(String source);

	/**
	 * Parses given input String as JSON and returns <tt>Map</tt>
	 * 
	 * @param is
	 *            - stream from which to read input JSON string. Must not be
	 *            null.
	 * @return parsed JSON as Map object.Returns empty Map in case when input is
	 *         empty String.
	 */
	Map<String, Object> parse(InputStream is);

	/**
	 * Converts given Java object to JSON string.
	 * 
	 * @param obj
	 *            the object to be converted.
	 * @return JSON representation of object
	 */
	String toJson(Object obj);

}