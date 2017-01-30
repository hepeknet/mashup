package com.test.mashup;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Simple native JSON parser able to parse JSON input into <tt>Map</tt> using only JDK classes.
 * 
 * @author borisa
 *
 */
public class SimpleNativeJsonParser implements JsonParser {
	
	private static final String JAVASCRIPT_ENGINE_NAME = "javascript";

	private final Logger log = Logger.getLogger(getClass().getName());

	/*
	 * Cache engine - performance optimization
	 */
	private final ScriptEngine se = new ScriptEngineManager().getEngineByName(JAVASCRIPT_ENGINE_NAME);

	@Override
	public Map<String, Object> parse(String source) {
		if (source == null) {
			throw new IllegalArgumentException("Unable to parse null string into JSON");
		}
		if (source.isEmpty()) {
			return new HashMap<>();
		}
		if (log.isLoggable(Level.FINE)) {
			log.fine("Will try to parse " + source + " as JSON");
		}
		// provided as part of standard JDK as of 1.8 update 60
		final String script = "Java.asJSONCompatible(" + source + ")";
		Map<String, Object> result = null;
		try {
			final Object evalResult = se.eval(script);
			result = (Map<String, Object>) evalResult;
			if (log.isLoggable(Level.FINE)) {
				log.fine("Successfully parsed " + source + " into " + result);
			}
		} catch (final ScriptException se) {
			log.log(Level.SEVERE, "Problem while parsing " + source + " into JSON.", se);
		}
		return result;
	}

	@Override
	public Map<String, Object> parse(InputStream is) {
		final String text = StringUtil.inputStreamToString(is);
		return parse(text);
	}

}
