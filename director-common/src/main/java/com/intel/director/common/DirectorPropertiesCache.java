package com.intel.director.common;

import java.util.Properties;


public class DirectorPropertiesCache {
	static Properties properties = new Properties();	
	
	public static String getValue(String key){
		properties = DirectorUtil.getPropertiesFile(Constants.DIRECTOR_PROPERTIES_FILE);
		return properties.getProperty(key);
	}
	

	public static Properties getAllValues(){
		properties = DirectorUtil.getPropertiesFile(Constants.DIRECTOR_PROPERTIES_FILE);
		return properties;
	}
	
}
