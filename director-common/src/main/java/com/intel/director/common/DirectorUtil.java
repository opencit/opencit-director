/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

public class DirectorUtil {

	private static final Logger log = LoggerFactory
			.getLogger(DirectorUtil.class);

	public static String createTar(String imageDir, String imageName,
			String trustPolicyName, String tarLocation, String tarName)
			throws IOException {

		String command = "tar -cf " + tarLocation + tarName + " -C " + imageDir
				+ " " + imageName + " " + trustPolicyName;
		// / String tarName = imageName + "-" + new
		// SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".tar";
		// / log.debug(genrateTP.executeShellCommand("tar -cf " + imageTPDir +
		// imagePathDelimiter + tarName + " -C " + imageTPDir + " " + imageName
		// + " " + trustpolicyName));
		System.out.println("Inside create Tar...................command::"
				+ command);
		log.debug(executeShellCommand("cd " + imageDir));
		log.debug(executeShellCommand("pwd "));
		log.debug(executeShellCommand(command));

		return tarLocation + tarName;
	}

	public static void createCopy(String absoluteImagePath,
			String absoluteModifiedImagePath) throws IOException {

		String command = "cp " + absoluteImagePath + " "
				+ absoluteModifiedImagePath;

		System.out.println("Inside createCopy..................command::"
				+ command);
		log.debug("Inside createCopy..................command::" + command);

		log.debug(executeShellCommand(command));

	}

	public static String getMountPath(String imageId) {
		StringBuilder sb = new StringBuilder(Constants.mountPath);
		sb.append(imageId);
		return sb.toString();
	}

	public static String getDirectorId() {
		Configuration configuration;
		try {
			configuration = ConfigurationFactory.getConfiguration();
			return configuration.get(Constants.DIRECTOR_ID);
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error while getting Director Id .....");
		}
		return null;
	}

	public static String computeHash(MessageDigest md, File file) {
		return null;

	}

	public static String getSymbolicLink(String filePath) {
		return filePath;

	}

	public static String executeShellCommand(String command) {
		log.debug("Command to execute is:" + command);
		String[] cmd = { "/bin/sh", "-c", command };
		Process p = null;
		BufferedReader reader = null ;
		// / int exitCode=1;
		String excludeList = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				result.append(line + "\n");
			}

			if (StringUtils.isNotEmpty(result.toString())) {
				excludeList = result.toString();
				excludeList = excludeList.replaceAll("\\n$", "");
			}
			// log.debug("Result of execute command: "+result);

		} catch (InterruptedException | IOException  ex) {
			log.error(null, ex);
		} finally{
			if(reader != null)
			{
				try {
					reader.close();
				} catch (IOException e) {
					log.error("error in closing reader in executeShellCommand()",e);
				}
			}
			if(p != null && p.getInputStream() != null)
			{
				try {
					p.getInputStream().close();
				} catch (IOException e) {
					log.error("error in closing p.getInputStream() in executeShellCommand()",e);
				}
			}
		}
		return excludeList;
	}

	public static int callExec(String command) throws IOException {

		StringBuilder output = new StringBuilder();
		BufferedReader reader = null;
		int exitCode = 12345;
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command);
			exitCode = p.waitFor();
			reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			

		} catch (InterruptedException | IOException ex) {
			log.error(null, ex);
		} finally {
			if(reader != null){
				reader.close();
			}
			
			if (p != null && p.getInputStream() != null) {
				p.getInputStream().close();
			}

		}
		log.debug(output.toString());
		log.trace("Exec command output : " + output.toString());
		return exitCode;

	}

	public static int executeCommandInExecUtil(String command, String... args)
			throws IOException {
		Result result = ExecUtil.execute(command, args);
		return result.getExitCode();
	}

	public static Properties getPropertiesFile(String path) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(Constants.configurationPath + path);
			File file = new File(Constants.configurationPath + path);
			if (!file.exists()) {
				file.createNewFile();
			}
			prop.load(input);
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error while getting the file .....");
		}
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error while closing the stream at getPropertiesFile() .....");
		}
		return prop;
	}

	public static String getProperties(String path) {
		Properties prop = getPropertiesFile(path);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : prop.stringPropertyNames()) {
			String value = prop.getProperty(key);
			map.put(key.replace(".", "_"), value);
		}
		JSONObject json = new JSONObject(map);
		return json.toString();
	}

	public static String editProperties(String path, String data)
			throws JsonMappingException, JsonParseException {
		Map<String, Object> map = new Gson().fromJson(data,
				new TypeToken<HashMap<String, Object>>() {
				}.getType());
		try {
			File file = new File(Constants.configurationPath + path);
			if (!file.exists()) {
				file.createNewFile();
			}
			writeToFile(map, Constants.configurationPath + path);
		} catch (Exception e) {
			// TODO Handle Error
			log.error("Error while editing the file .....");
		}
		return data;
	}

	public static void writeToFile(Map<String, Object> map, String fileName) {
		Properties properties = new Properties();
		Set<String> set = map.keySet();
		Iterator<String> itr = set.iterator();
		OutputStream output = null;
		while (itr.hasNext()) {
			String key = (String) itr.next();
			String value = (String) map.get(key);
			properties.setProperty(key.replace('_', '.'), value);
		}
		try {
			output = new FileOutputStream(fileName);
			properties.store(output, fileName);
			
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error while writing into the file in writeToFile().....");
		}finally{
			if(output != null){
				try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Error closing stream", e);
				}
			}
		}
		
	}

	public static String getKeyIdFromUrl(String url) {
		log.debug("URL :: " + url);
		String[] split = url.split("/");
		int index = 0;
		for (int i = 0; i < split.length; i++) {
			if (split[i].equals("keys")) {
				log.debug("Keys index :: " + i);
				index = ++i;
				break;
			}
		}

		log.debug("Index :: " + index);

		if (index != 0) {
			return split[index];
		} else {
			return null;
		}

	}

}
