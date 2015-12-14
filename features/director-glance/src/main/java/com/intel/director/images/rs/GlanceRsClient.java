/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.rs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.constants.Constants;

/**
 * 
 * @author GS-0681
 */
public class GlanceRsClient {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(GlanceRsClient.class);

	public WebTarget webTarget;
	public Client client;
	public String authToken;

	public GlanceRsClient(WebTarget webTarget, Client client, String glanceIp,
			String tenanatName, String username, String password) throws GlanceException {
		this.webTarget = webTarget;
		this.client = client;

		createAuthToken(glanceIp, tenanatName, username, password);
	}

	public void uploadImage(File file, Map<String, String> imageProperties,
			String glanceId) throws IOException {
		long start = new Date().getTime();
		BufferedReader br = null;
		InputStream ist =  null;
		DefaultHttpClient httpClient = null;
		try {
			httpClient = new DefaultHttpClient();
			HttpPut putRequest = new HttpPut(webTarget.getUri().toString()
					+ "/v1/images/" + glanceId);

			putRequest.setHeader("X-Auth-Token", authToken);
			// / postRequest.setHeader("x-image-meta-id", uuid);
			/*
			 * putRequest.setHeader("x-image-meta-container_format", "bare");
			 * putRequest.setHeader("x-image-meta-disk_format", "qcow2");
			 */
			putRequest.setHeader("Content-Type", "application/octet-stream");
			ist = new FileInputStream(file);

			HttpEntity inputHttp = new InputStreamEntity(ist);
			putRequest.setEntity(inputHttp);

			HttpResponse response = httpClient.execute(putRequest);

			// // Response response =
			// webTarget.path("/v1/images/"+glanceId).request().header("X-Auth-Token",
			// authToken).put(Entity.entity(f, MediaType.TEXT_PLAIN_TYPE));
			br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			String output;
			StringBuffer sb = new StringBuffer();

			while ((output = br.readLine()) != null) {
				sb.append(output);
				// log.debug(output);
			}
			log.info("Response from glance : "+sb.toString());
			JSONObject obj = new JSONObject(sb.toString());
			log.debug("obj::" + obj);
			
		} catch (IOException e) {
			throw e;
		} finally {
			
			if (httpClient != null) {
				httpClient.close();
			}
			if (ist != null) {
				ist.close();
			}
			try {
				

				if (br != null) {
					br.close();
				}

				
			} catch (IOException e) {
				log.error("Error closing streams ");
			}
		}
		long end = new Date().getTime();
		printTimeDiff("uploadImage", start, end);
	}

	public ImageStoreUploadResponse fetchDetails(
			Map<String, String> imageProperties, String glanceId)
			throws IOException {
		long start = new Date().getTime();

		Response response = webTarget.path("/v1/images/" + glanceId).request()
				.header("X-Auth-Token", authToken).get();

		ImageStoreUploadResponse imageStoreResponse = new ImageStoreUploadResponse();
		imageStoreResponse.setId(glanceId);
		imageStoreResponse.setImage_uri(response
				.getHeaderString(Constants.GLANCE_HEADER_LOCATION));
		imageStoreResponse.setSent(new Integer(response
				.getHeaderString(Constants.CONTENT_LENGTH)));
		imageStoreResponse.setChecksum(response
				.getHeaderString(Constants.GLANCE_HEADER_CHECKSUM));

		long end = new Date().getTime();
		printTimeDiff("fetchDetails", start, end);

		return imageStoreResponse;
	}

	public String uploadImageMetaData(Map<String, String> imageProperties)
			throws IOException {
		long start = new Date().getTime();

		String uuid = (new UUID()).toString();
		Response response;
		if (imageProperties.get(Constants.MTWILSON_TRUST_POLICY_LOCATION) != null) {
			response = webTarget
					.path("/v1/images")
					.request()
					.header("X-Auth-Token", authToken)
					.header("x-image-meta-id", uuid)
					.header("x-image-meta-container_format",
							imageProperties.get(Constants.CONTAINER_FORMAT))
					.header("x-image-meta-disk_format",
							imageProperties.get(Constants.DISK_FORMAT))
					.header("x-image-meta-is_public",
							imageProperties.get(Constants.IS_PUBLIC))
					.header("x-image-meta-name",
							imageProperties.get(Constants.NAME))
					.header("x-image-meta-property-mtwilson_trustpolicy_location",
							imageProperties
									.get(Constants.MTWILSON_TRUST_POLICY_LOCATION))
					.post(Entity.json(null));
		} else {

			response = webTarget
					.path("/v1/images")
					.request()
					.header("X-Auth-Token", authToken)
					.header("x-image-meta-id", uuid)
					.header("x-image-meta-container_format",
							imageProperties.get(Constants.CONTAINER_FORMAT))
					.header("x-image-meta-disk_format",
							imageProperties.get(Constants.DISK_FORMAT))
					.header("x-image-meta-is_public",
							imageProperties.get(Constants.IS_PUBLIC))
					.header("x-image-meta-name",
							imageProperties.get(Constants.NAME))
					.post(Entity.json(null));

		}

		InputStream inputStream = (InputStream) response.getEntity();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));

		String output;
		StringBuffer sb = new StringBuffer();

		while ((output = br.readLine()) != null) {
			sb.append(output);
			// log.debug(output);
		}
		log.info("Metadata Response: "+sb.toString());
		JSONObject obj = new JSONObject(sb.toString());
		JSONObject image = obj.getJSONObject("image");
		String id = image.getString("id");
		long end = new Date().getTime();
		printTimeDiff("uploadImageMetaData", start, end);

		return id;
	}

	private void createAuthToken(String glanceIP, String tenantName,
			String userName, String password) {
		long start = new Date().getTime();
		//TODO not used after being assigned
		DefaultHttpClient httpClient = null;
		BufferedReader br = null;
		boolean responseHasError = false;

		try {
			httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost("http://" + glanceIP
					+ ":5000/v2.0/tokens");

			String body = "{\"auth\": {\"tenantName\": \"" + tenantName
					+ "\", \"passwordCredentials\": {\"username\": \""
					+ userName + "\", \"password\": \"" + password + "\"}}}";
			HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));

			postRequest.setEntity(entity);
			postRequest.setHeader("Content-Type", "application/json");
			postRequest.setHeader("Accept", "application/json");
			HttpResponse response = httpClient.execute(postRequest);
			br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			String output;
			StringBuffer sb = new StringBuffer();

			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			JSONObject obj = new JSONObject(sb.toString());
			if(obj.has("access")){
				JSONObject jsonObjectAccess = obj.getJSONObject("access");
				if(jsonObjectAccess.has("token")){
					JSONObject property = jsonObjectAccess.getJSONObject(
					"token");
					authToken = property.getString("id");
				}else{
					responseHasError = true;
				}
			}else{
				responseHasError = true;
			}
			httpClient.getConnectionManager().shutdown();
			
		} catch (MalformedURLException e) {
			log.error("Error while creating auth token", e);
		} catch (IOException e) {
			log.error("Error while creating auth token", e);
		}
		finally{
			if(httpClient != null){
				httpClient.close();
			}		
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					log.error("Error closing reader", e);
				}
			}

		}
		long end = new Date().getTime();
		printTimeDiff("createAuthToken", start, end);
		if(responseHasError){
			throw new GlanceException("Unable to communicate with Glance at "+glanceIP);
		}
	}

	public void getImageMetaData() {
	}

	public void deleteImage() {
	}

	private void printTimeDiff(String method, long start, long end) {
		log.info(method + " took " + (end - start) + " ms");
	}
}
