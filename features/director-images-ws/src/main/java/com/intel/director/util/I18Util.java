package com.intel.director.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18Util {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(I18Util.class);

	static final String language = "en";
	static final String country = "US";
	static final Locale currentLocale = new Locale(language, country);
	static final ResourceBundle bundle = ResourceBundle.getBundle("ImageStoreKeys", currentLocale);

	public static String format(String key) {
		String value;
		try {
			value = bundle.getString(key);
		} catch (MissingResourceException exception) {
			value = key;
			log.error("No value found for key {}", key);
		}
		return value;
	}

	public static String format(String key, String resourceBundle) {
		String value;
		try {
			ResourceBundle _bundle = ResourceBundle.getBundle(resourceBundle,
					currentLocale);
			value = _bundle.getString(key);
		} catch (MissingResourceException exception) {
			value = key;
			log.error("No value found for key {} in bundle {}", key,
					resourceBundle);
		}
		return value;
	}
}
