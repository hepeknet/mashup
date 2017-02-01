package com.test.mashup.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration utility. If we used 3PP for configuration this would not be
 * necessary to exist.
 * 
 * @author borisa
 *
 */
public abstract class ConfigurationUtil {

	private static final Logger LOG = Logger.getLogger(ConfigurationUtil.class.getName());

	private static Properties PROPS = null;

	static {
		PROPS = loadConfiguration();
	}

	private static Properties loadConfiguration() {
		LOG.info("Loading configuration...");
		final Properties props = new Properties();
		try {
			final String configuredPropertiesLocation = System
					.getProperty(Constants.CONFIGURATION_LOCATION_SYS_PROPERTY_NAME);
			if (configuredPropertiesLocation != null && !configuredPropertiesLocation.isEmpty()) {
				LOG.info("Configuration will be loaded from " + configuredPropertiesLocation);
				try (InputStream is = new FileInputStream(configuredPropertiesLocation)) {
					props.load(is);
				}
			} else {
				LOG.info("Configuration will be loaded from classpath");
				props.load(ConfigurationUtil.class.getResourceAsStream("/mashup.properties"));
			}
		} catch (final IOException ie) {
			throw new IllegalStateException("Was not able to load configuration", ie);
		}
		return props;
	}

	/**
	 * Returns the value of configuration property as String.
	 * 
	 * @param propName
	 *            the name for which configuration value is needed. Must not be
	 *            null or empty.
	 * @return the value associated with configuration property name or null if
	 *         none exists.
	 */
	public static String getString(String propName) {
		if (propName == null || propName.isEmpty()) {
			throw new IllegalArgumentException("Configuration property name must not be null or empty");
		}
		LOG.fine("Getting value for configuration property [" + propName + "]");
		return PROPS.getProperty(propName);
	}

	/**
	 * Returns the value of configuration property as int.
	 * 
	 * @param propName
	 *            the name for which configuration value is needed. Must not be
	 *            null or empty.
	 * @return the value associated with configuration property name. Throws
	 *         exception in case configuration property does not exist or can
	 *         not be converted into integer.
	 */
	public static int getInt(String propName) {
		final String strValue = getString(propName);
		LOG.fine("Trying to get configuration value [" + strValue + "] for [" + propName + "] as Integer");
		return Integer.parseInt(strValue);
	}

}