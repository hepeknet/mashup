package com.test.mashup.util;

import java.io.InputStream;
import java.util.Scanner;

public abstract class StringUtil {

	public static String inputStreamToString(InputStream is) {
		if (is == null) {
			throw new IllegalArgumentException("Input stream must not be null");
		}
		try (Scanner sc = new Scanner(is, "UTF-8")) {
			// read everything at once
			final String text = sc.useDelimiter("\\Z").next();
			return text;
		}
	}

}
