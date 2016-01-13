package com.intel.director.service;

import java.io.IOException;
import java.util.Map;

public interface Setting {

	
	
	public String editProperties(String string, Map<String,String> data) throws IOException;
	
	public String getProperties(String path) throws IOException;
	
}
