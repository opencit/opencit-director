package com.intel.director.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.Folders;

public class SettingFileProperties {
	InputStream inputStream;
	Properties prop;
	String propFileName;
	OutputStream output;

	public String writePropertiesToConfig(String folder_path,
			Map<String, String> data) throws IOException {
		prop = new Properties();
		String path = Folders.configuration() + File.separator + folder_path;
		File file = new File(path);
		if (!file.exists()) {
			file.createNewFile();
		}
		output = new FileOutputStream(file);
		Iterator<String> it = data.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			prop.setProperty((String) key, data.get(key));
		}
		prop.store(output, null);
		output.close();
		String result = readPropertiesFromConfig(folder_path);
		return result;
	}

	public String readPropertiesFromConfig(String folder_path)
			throws IOException {
		// TODO mention path
		Map<String, String> map = new HashMap<String, String>();
		prop = new Properties();
		String path = Folders.configuration() + File.separator + folder_path;
		inputStream = new FileInputStream(path);
		if (inputStream != null) {
			System.out.println("File read Successfully");
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + folder_path
					+ "' is empty");
		}
		for (String key : prop.stringPropertyNames()) {
			String value = prop.getProperty(key);
			map.put(key, value);
		}
		inputStream.close();
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(map));
		return mapper.writeValueAsString(map);
	}

}
