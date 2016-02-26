/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.swift.rs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.intel.director.swift.api.SwiftContainer;
import com.intel.director.swift.api.SwiftObject;
import com.intel.director.swift.constants.Constants;

/**
 * 
 * @author Aakash
 */
public class SwiftRsClient {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(SwiftRsClient.class);

	public WebTarget webTarget;
	public Client client;
	public String authToken;
	public String storageUrl;

	public SwiftRsClient(String swiftApiEndpoint, String accountName,
			String accountUsername, String accountUserPassword)
			throws SwiftException {

		client = ClientBuilder.newBuilder().build();
		URL url = null;
		try {
			url = new URL(swiftApiEndpoint);
		} catch (MalformedURLException e) {
			log.error("Initialize SwiftRsClient failed");
			throw new SwiftException("Initialize SwiftRsClient failed", e);
		}
		webTarget = client.target(url.toExternalForm());

		createAuthToken(accountName, accountUsername, accountUserPassword);

	}

	public List<SwiftContainer> getContainersList() throws SwiftException {
		List<SwiftContainer> containersList = new ArrayList<SwiftContainer>();
		long start = new Date().getTime();
		Response response = webTarget.path("?format=json").request()
				.header(Constants.AUTH_TOKEN, authToken).get();

		InputStream inputStream = (InputStream) response.getEntity();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));

		String output;
		StringBuffer sb = new StringBuffer();

		try {
			while ((output = br.readLine()) != null) {
				sb.append(output);
				// log.debug(output);
			}
		} catch (IOException e) {
			log.error("Unable to read json, getContainersList", e);
			
			throw new SwiftException("Unable to read json, getContainersList", e);
		}finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e1) {
					log.error("Unable to close stream",e1);
				}
			}
		}
		JSONArray jArray = new JSONArray(sb.toString());

		for (int i = 0; i < jArray.length(); i++) {
			JSONObject containerJsonObj = jArray.getJSONObject(i);
			if (containerJsonObj != null) {
				SwiftContainer swiftContainer = new SwiftContainer();
				swiftContainer.setBytes(containerJsonObj.getLong("bytes"));
				swiftContainer.setName(containerJsonObj.getString("name"));
				swiftContainer.setCount(containerJsonObj.getInt("count"));
				containersList.add(swiftContainer);

			}

		}
		long end = new Date().getTime();
		printTimeDiff("Swift, getContainerList", start, end);
		return containersList;

	}

	public List<SwiftObject> getObjectsList(String containerName) throws SwiftException {
		List<SwiftObject> objectsList = new ArrayList<SwiftObject>();
		long start = new Date().getTime();
		URL url = null;
		try {
			url = new URL(storageUrl + "/" + containerName + "?format=json");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WebTarget target = client.target(url.toExternalForm());
		Response response=null;
		try{
		 response = target.request()
				.header(Constants.AUTH_TOKEN, authToken).get();
		}catch(Exception e){
			throw new SwiftException("Swift, getObjectsList fialed", e);	
		}
		if((response.getStatus()!=200) && (response.getStatus()!=204)){
			log.error("Swift getObjectsList failed,"+response.getStatus());
			throw new SwiftException("Swift getObjectsList failed,"+response.getStatus());
		}
		
		InputStream inputStream = (InputStream) response.getEntity();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));

		String output;
		StringBuffer sb = new StringBuffer();

		try {
			while ((output = br.readLine()) != null) {
				sb.append(output);
				// log.debug(output);
			}
		} catch (IOException e) {
			log.error("Unable to read json ,swift getObjectList method", e);
			throw new SwiftException(
					"Unable to read json ,swift getObjectList method", e);
		}finally{
			
				try {
					if(br!=null){
					br.close();
					}
					if(inputStream!=null){
						inputStream.close();
					}
				} catch (IOException e1) {
					log.error("Unable to close stream",e1);
				}
			
		}

		// /log.debug("json array::" + sb.toString());
		try {
			JSONArray jArray = new JSONArray(sb.toString());

			for (int i = 0; i < jArray.length(); i++) {
				JSONObject containerJsonObj = jArray.getJSONObject(i);
				if (containerJsonObj != null) {
					SwiftObject swiftObject = new SwiftObject(
							containerJsonObj.getString("name"),
							containerJsonObj.getString("hash"),
							containerJsonObj.getString("content_type"),
							containerJsonObj.getLong("bytes"));
					objectsList.add(swiftObject);
				}

			}
		} catch (Exception e) {
			log.error("Unable to read json ,swift getObjectList method sb.toString()::"+sb.toString(), e);
			throw new SwiftException(
					"Unable to read json ,swift getObjectList method", e);
		}
		long end = new Date().getTime();
		printTimeDiff("Swift, getObjectList", start, end);
		return objectsList;

	}

	public Map<String, String> downloadObjectToFile(String containerName,
			String objectName, String writeToFilePath) throws SwiftException {
		Map<String, String> objectMetadata = new HashMap<String, String>();
		long start = new Date().getTime();
		URL url = null;
		try {
			url = new URL(storageUrl + "/" + containerName + "/" + objectName);
		} catch (MalformedURLException e) {
			throw new SwiftException("getObject malformed url", e);
		}
		WebTarget target = client.target(url.toExternalForm());
		Response response=null;
		try {
		response = target.request()
				.header(Constants.AUTH_TOKEN, authToken).get();
		} catch (Exception e) {
			throw new SwiftException(
					"downloadObjectToFile, getObject writing to file failed ",
					e);
		}
		if (writeToFilePath != null) {
			InputStream inputStream = null;
			OutputStream outputStream = null;

			try {

				inputStream = (InputStream) response.getEntity();

				// write the inputStream to a FileOutputStream
				outputStream = new FileOutputStream(new File(writeToFilePath));

				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}

				System.out.println("Done!");

			} catch (IOException e) {
				throw new SwiftException(
						"downloadObjectToFile, getObject writing to file failed ",
						e);
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						log.error(
								"downloadObjectToFile, Unable to close input stream",
								e);
					}
				}
				if (outputStream != null) {
					try {
						// outputStream.flush();
						outputStream.close();
					} catch (IOException e) {
						log.error("Unable to close output stream", e);
					}

				}
			}

		}
		if((response.getStatus()<200) && (response.getStatus()>204)){
			log.error("Swift downloadObjectToFile failed,"+response.getStatus());
			throw new SwiftException("Swift downloadObjectToFile failed,"+response.getStatus());
		}
		
		MultivaluedMap<String, Object> headerMap = response.getHeaders();
		for (Entry<String, List<Object>> entry : headerMap.entrySet()) {
			if (entry.getKey().startsWith("X-Object-Meta-")) {
				List<Object> objList = entry.getValue();
				String headerValue = (String) objList.get(0);
				objectMetadata.put(entry.getKey(), headerValue);
			}

		}
		long end = new Date().getTime();
		printTimeDiff("Swift, getObject with write file", start, end);
		return objectMetadata;
	}

	public Map<String, String> getObjectMetadata(String containerName,
			String objectName) throws SwiftException {
		Map<String, String> objectMetadata = new HashMap<String, String>();
		long start = new Date().getTime();
		URL url = null;
		try {
			url = new URL(storageUrl + "/" + containerName + "/" + objectName);
		} catch (MalformedURLException e) {
			throw new SwiftException("getObjectMetadata Failed ", e);
		}
		WebTarget target = client.target(url.toExternalForm());
		Response response=null;
		try {
		 response = target.request()
				.header(Constants.AUTH_TOKEN, authToken).get();
		} catch (Exception e) {
			throw new SwiftException("getObjectMetadata Failed ", e);
		}
		if((response.getStatus()!=200) && (response.getStatus()!=204) ){
			log.error("Swift getObjectMetadata failed,"+response.getStatus());
			throw new SwiftException("Swift getObjectMetadata failed,"+response.getStatus());
		}
		MultivaluedMap<String, Object> headerMap = response.getHeaders();
		for (Entry<String, List<Object>> entry : headerMap.entrySet()) {
			if (entry.getKey().startsWith("X-Object-Meta-")) {
				List<Object> objList = entry.getValue();
				String headerValue = (String) objList.get(0);
				objectMetadata.put(entry.getKey(), headerValue);
			}

		}
		
		long end = new Date().getTime();
		printTimeDiff("Swift, getObjectMetadata", start, end);
		return objectMetadata;
	}

	public int deleteObject(String containerName, String objectName)
			throws SwiftException {

		URL url = null;
		try {
			url = new URL(storageUrl + "/" + containerName + "/" + objectName);
		} catch (MalformedURLException e) {
			throw new SwiftException("getObjectMetadata Failed ", e);
		}
		return deleteObject(url);
	}

	public int deleteObject(URL url) throws SwiftException {

		long start = new Date().getTime();
		WebTarget target = client.target(url.toExternalForm());
		Response response=null;
		try {
		response = target.request()
				.header(Constants.AUTH_TOKEN, authToken).delete();
		} catch (Exception e) {
			throw new SwiftException("deleteObject Failed ", e);
		}
		int status = response.getStatus();
		log.info("createContainer , deleteObject::"+status);
		if((response.getStatus()!=200) && (response.getStatus()!=204) ){
			log.error("Swift deleteObject failed, status::"+response.getStatus());
			throw new SwiftException("Swift deleteObject failed,"+response.getStatus());
		}
		long end = new Date().getTime();
		printTimeDiff("Swift, delete Object", start, end);
		return status;
	}
	
	public int createContainer(String containerName)throws SwiftException{

		long start = new Date().getTime();
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPut putRequest = new HttpPut(storageUrl + "/" + containerName);

		putRequest.setHeader(Constants.AUTH_TOKEN, authToken);

		putRequest.setEntity(null);
		int status = -1;
		HttpResponse response;
		try {
			response = httpClient.execute(putRequest);

			status = response.getStatusLine().getStatusCode();
			log.info("createContainer , status::"+status);

		} catch (ClientProtocolException e1) {
			log.error("create container failed",e1);
			throw new SwiftException("createcontainer Failed", e1);
		} catch (IOException e) {
			log.error("create container failed",e);
			throw new SwiftException("createcontainer Failed", e);
		}
		long end = new Date().getTime();
		printTimeDiff("createContainer swift", start, end);
		return status;
	}

	public String createOrReplaceObject(File file,
			Map<String, String> objectProperties, String containerName,
			String objectName) throws SwiftException {

		long start = new Date().getTime();
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPut putRequest = new HttpPut(storageUrl + "/" + containerName + "/"
				+ objectName);

		putRequest.setHeader(Constants.AUTH_TOKEN, authToken);
		// putRequest.setHeader("Content-Type", "text/html");

		
		HttpResponse response;
		try {
			InputStream ist = new FileInputStream(file);

			HttpEntity inputHttp = new InputStreamEntity(ist);

			putRequest.setEntity(inputHttp);
			if (objectProperties != null) {
				for (Map.Entry<String, String> entry : objectProperties
						.entrySet()) {
					if(entry.getKey().startsWith("X-Object-Meta-")){
					putRequest.addHeader(entry.getKey(),
							entry.getValue());
					}
				}
			}
			response = httpClient.execute(putRequest);

		
		} catch (ClientProtocolException e1) {
			log.error("createOrReplaceObject Failed", e1);
			throw new SwiftException("createOrReplaceObject Failed", e1);
		} catch (Exception e) {
			log.error("createOrReplaceObject I/O Failed ", e);
			throw new SwiftException("createOrReplaceObject I/O Failed ", e);
		}
		if((response.getStatusLine().getStatusCode()<200) && (response.getStatusLine().getStatusCode()>204) ){
			log.error("Swift createOrReplaceObject failed,"+response.getStatusLine());
			throw new SwiftException("Swift createOrReplaceObject failed,"+response.getStatusLine());
		}
		long end = new Date().getTime();
		printTimeDiff("createOrReplaceObject swift", start, end);
		return storageUrl + "/" + containerName + "/"
		+ objectName;
		
	}

	public String createOrReplaceObjectMetadata(
			Map<String, String> objectProperties, String containerName,
			String objectName) throws SwiftException {

		long start = new Date().getTime();
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost postRequest = new HttpPost(storageUrl + "/" + containerName
				+ "/" + objectName);

		for (Map.Entry<String, String> entry : objectProperties.entrySet()) {
			System.out.println("map values::" + entry.getKey() + " : "
					+ entry.getValue());
			postRequest.addHeader("X-Object-Meta-" + entry.getKey(),
					entry.getValue());
		}
		postRequest.setHeader(Constants.AUTH_TOKEN, authToken);

		postRequest.setEntity(null);
		
		HttpResponse response;
		try {
			response = httpClient.execute(postRequest);
			
		} catch (ClientProtocolException e1) {
			log.error("createOrReplaceObjectMetadata Failed", e1);
			throw new SwiftException("createOrReplaceObjectMetadata Failed", e1);
		} catch (IOException e) {
			log.error("createOrReplaceObjectMetadata I/O Failed ", e);
			throw new SwiftException(
					"createOrReplaceObjectMetadata I/O Failed ", e);
		}
		if((response.getStatusLine().getStatusCode()<200) && (response.getStatusLine().getStatusCode()>204) ){
			log.error("Swift createOrReplaceObjectMetadata failed,"+response.getStatusLine());
			throw new SwiftException("Swift createOrReplaceObjectMetadata failed,"+response.getStatusLine());
		}
		
		long end = new Date().getTime();
		printTimeDiff("createOrReplaceObjectMetadata swift", start, end);
		return storageUrl + "/" + containerName + "/"
		+ objectName;
	}

	private void createAuthToken(String accountName, String accountUsername,
			String accountUserPassword) throws SwiftException {
		
		long start = new Date().getTime();
		Response response=null;
		
		try {
			response = webTarget
					.path("/auth/" + Constants.SWIFT_API_VERSION)
					.request()
					.header(Constants.SWIFT_STORAGE_USER,
							accountName + ":" + accountUsername)
					.header(Constants.SWIFT_STORAGE_PASSWORD,
							accountUserPassword).get();
		}catch (Exception e) {
			throw new SwiftException("createAuthToken Failed ", e);
		}
		
		if((response.getStatus()<200) && (response.getStatus()>204)){
			log.error("Swift createAuthToken failed,"+response.getStatus());
			throw new SwiftException("Swift createAuthToken failed,"+response.getStatus());
		}
		
		authToken = response.getHeaderString(Constants.AUTH_TOKEN);
		storageUrl = response.getHeaderString(Constants.SWIFT_STORAGE_URL);

		System.out.println("Inside authToken::" + authToken);
		long end = new Date().getTime();
		printTimeDiff("createAuthToken swift", start, end);
	}

	private void printTimeDiff(String method, long start, long end) {
		log.info(method + " took " + (end - start) + " ms");
	}
}
