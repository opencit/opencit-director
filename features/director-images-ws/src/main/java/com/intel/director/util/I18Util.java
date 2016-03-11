package com.intel.director.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18Util {
	
	static final String language = "en";
	static final String country = "US";
	static final Locale currentLocale = new Locale(language, country);
	static final ResourceBundle bundle = ResourceBundle.getBundle("ImageStoreKeys", currentLocale);

	public static String format(String key){
		return bundle.getString(key);
	}
}
