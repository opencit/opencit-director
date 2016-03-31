/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.TrustPolicy;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
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

	public static Properties getPropertiesFile(String propFileName) {
		Properties prop = new Properties();
		File customFile = new File(Constants.configurationPath + propFileName);
		ConfigurationProvider provider;
		try {
			provider = ConfigurationFactory.createConfigurationProvider(customFile);
			Configuration loadedConfiguration = provider.load();
			Set<String> keys = loadedConfiguration.keys();
			for(String key : keys){
				prop.put(key, loadedConfiguration.get(key));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Error while getting the file .....");
		}
		return prop;
	}

	public static String getProperties(String path) throws IOException {
		File customFile = new File(Constants.configurationPath + path);
		ConfigurationProvider provider = ConfigurationFactory.createConfigurationProvider(customFile);
		Configuration loadedConfiguration = provider.load();
		Map<String, String> map = new HashMap<String, String>();
		for (String key : loadedConfiguration.keys()) {
			String value = loadedConfiguration.get(key);
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

	public static void writeToFile(Map<String, Object> map, String fileName) throws IOException {
		File customFile = new File(fileName);
		ConfigurationProvider provider = ConfigurationFactory.createConfigurationProvider(customFile);
		Set<String> set = map.keySet();
		Iterator<String> itr = set.iterator();
		Configuration loadedConfiguration = provider.load();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			String value = (String) map.get(key);
			loadedConfiguration.set(key.replace('_', '.'), value);
		}
		provider.save(loadedConfiguration);
	}

	
	public static String computeUploadVar(String uuid, String dekUrl){
		if(uuid == null){
			return null;
		}
		String uploadvar = null;
		StringBuilder builder = new StringBuilder(uuid);
		if(StringUtils.isNotBlank(dekUrl)){
			builder.append(dekUrl);
		}
		try {
			uploadvar = computeHash(MessageDigest.getInstance("MD5"), builder.toString());
		} catch (NoSuchAlgorithmException e) {
			log.error("Error Calcaulating upload var for {}", uuid);
			return null;
		}
		return uploadvar;
	}

	public static String computeHash(MessageDigest md, File file)
			throws IOException {
		if (!file.exists()) {
			return null;
		}
	
		md.reset();
		byte[] bytes = new byte[2048];
		int numBytes;
		FileInputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			log.error("error :: input file doesn't exists", e);
			throw new IOException("input file doesn't exists", e);
		}
	
		try {
			while ((numBytes = is.read(bytes)) != -1) {
				md.update(bytes, 0, numBytes);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("error in reading from file", e);
	
			throw new IOException("error in reading from file", e);
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				log.error("error in closing stream", ioe);
				throw new IOException("error in closing stream", ioe);
			}
		}
		byte[] digest = md.digest();
		String result = new String(Hex.encodeHex(digest));
	
		return result;
	
	}
	

	public static String computeHash(MessageDigest md, String str){
		if(str == null){
			return null;
		}
		md.update(str.getBytes());
		byte[] digest = md.digest();
		String result = new String(Hex.encodeHex(digest));	
		return result;	
	}

	public static String fetchIdforUpload(TrustPolicy trustPolicy){	
		String policyXml = trustPolicy.getTrust_policy();
		log.debug("Inside Run Upload Policy task policyXml::" + policyXml);
		StringReader reader = new StringReader(policyXml);
		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = null;
		JAXBContext jaxbContext = null;
		try {
			jaxbContext = JAXBContext
					.newInstance(com.intel.mtwilson.trustpolicy.xml.TrustPolicy.class);
		} catch (JAXBException e) {
			log.error("Unable to instantiate the jaxbcontext", e);
			return null;
			
		}
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = (Unmarshaller) jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			log.error("Unable to instantiate the unmarshaller", e);
			return null;
		}
		try {
			policy = (com.intel.mtwilson.trustpolicy.xml.TrustPolicy) unmarshaller
					.unmarshal(reader);
		} catch (JAXBException e) {
			log.error("Unable to unmarshall the policy", e);
			return null;	
		}
		return policy.getImage().getImageId();
	}

	public static String prettifyXml(String xml) {
		final InputSource src = new InputSource(new StringReader(xml));
		Node document;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			log.error("Error parsing string {}", xml, e);
			return null;
		}
		final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

		DOMImplementationRegistry registry = null;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
			log.error("Error initializing DOMImplementationRegistry {}", e);
			return null;
		}
		final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
		final LSSerializer writer = impl.createLSSerializer();

		// Set this to true if the output needs to be beautified.
		writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		// Set this to true if the declaration is needed to be outputted.
		writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

		return writer.writeToString(document);
	}

	
	public static String fetchDekUrl(TrustPolicy policy){
		if(policy==null){
			return "";
		}
		com.intel.mtwilson.trustpolicy.xml.TrustPolicy trustPolicy = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(com.intel.mtwilson.trustpolicy.xml.TrustPolicy.class);
			Unmarshaller unmarshaller = (Unmarshaller) jaxbContext
					.createUnmarshaller();

			StringReader reader = new StringReader(policy.getTrust_policy());
			trustPolicy = (com.intel.mtwilson.trustpolicy.xml.TrustPolicy) unmarshaller.unmarshal(reader);
			///trustPolicy = TdaasUtil.getPolicy(policy.getTrust_policy());
		} catch (JAXBException e1) {
			log.error("Directorutil fetchDekUrl failed",e1);
		}
		
		return trustPolicy.getEncryption()!=null ? trustPolicy.getEncryption().getKey().getURL() : "";
		
	}
	
	public static Result executeCommand(String command, String... args)
			throws ExecuteException, IOException {
		Result result = ExecUtil.executeQuoted(command, args);
		if (result.getStderr() != null
				&& StringUtils.isNotEmpty(result.getStderr())) {
			log.error(result.getStderr());
		}
		return result;
	}
}
