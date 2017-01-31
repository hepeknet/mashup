package com.test.mashup.json;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.test.mashup.util.StringUtil;

/**
 * Simple native JSON parser able to parse JSON input into <tt>Map</tt> using
 * only JDK classes. Ideally this should be replaced with some more robust 3PP
 * like Jackson
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

	/*
	 * Ideally this would be implemented as part of 3PP. We support only limited
	 * number of types required for current needs. This can be either expanded
	 * later on or we will switch to 3PP to provide more robust implementation.
	 */
	@Override
	public String toJson(Object obj) {
		if (obj == null) {
			return "null";
		}
		if (obj instanceof String) {
			return "\"" + obj + "\"";
		}
		if (obj instanceof Number) {
			return obj.toString();
		}
		if (obj instanceof Boolean) {
			return obj.toString();
		}
		if (obj instanceof List) {
			final StringBuilder sb = new StringBuilder("[");
			final List<Object> lst = (List<Object>) obj;
			for (int i = 0; i < lst.size(); i++) {
				if (i != 0) {
					sb.append(", ");
				}
				sb.append(toJson(lst.get(i)));
			}
			sb.append("]");
			return sb.toString();
		}
		// we want to inspect POJOs and convert fields to JSON
		// but we do not want to do this blindly for other classes, especially
		// for JDK built-in classes.
		// in case we want to support more built-in types (like Maps, Sets,
		// Arrays etc)
		// we should add that when needed as shown above for other native types.
		final String packageName = obj.getClass().getPackage().getName();
		final boolean isBuiltInJavaClass = packageName.startsWith("java");
		if (isBuiltInJavaClass) {
			throw new UnsupportedOperationException("Unable to convert type " + obj.getClass().getName() + " to JSON!");
		} else {
			return pojoToJson(obj);
		}
	}

	/**
	 * Simple and naive implementation of converting Java POJO to JSON string.
	 * No need to support wide variety of options and all corner cases since we
	 * only have to support our own POJOs.
	 * 
	 * @param obj
	 * @return
	 */
	private String pojoToJson(Object obj) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Converting " + obj + " pojo to JSON");
		}
		final StringBuilder sb = new StringBuilder("{");
		try {

			final Map<String, Object> fieldsAndValues = Arrays
					.asList(Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors()).stream()
					// filter out properties with setters only
					.filter(pd -> Objects.nonNull(pd.getReadMethod()))
					// filter out getClass method from java.lang.Object
					.filter(pd -> pd.getReadMethod().getName() != "getClass")
					.collect(Collectors.toMap(pd -> pd.getDisplayName(), pd -> {
						try {
							return pd.getReadMethod().invoke(obj);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							throw new IllegalStateException(e);
						}
					}));
			fieldsAndValues.forEach((k, v) -> {
				final String valueToJson = toJson(v);
				sb.append("\"").append(k).append("\" : ").append(valueToJson).append(", ");
			});
			// remove last unneeded comma character and space
			sb.delete(sb.length() - 2, sb.length());
		} catch (final IntrospectionException e) {
			throw new IllegalStateException("Exception while introspecting for json generation", e);
		}
		sb.append("}");
		return sb.toString();
	}

}
