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
	;
	public WebTarget webTarget;
	public Client client;
	public String authToken;

	public GlanceRsClient(WebTarget webTarget, Client client, String glanceIp,
			String tenanatName, String username, String password) {
		this.webTarget = webTarget;
		this.client = client;

		createAuthToken(glanceIp, tenanatName, username, password);
	}

	public void uploadImage(File file, Map<String, String> imageProperties,
			String glanceId) throws IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPut putRequest = new HttpPut(webTarget.getUri().toString()
				+ "/v1/images/" + glanceId);

		putRequest.setHeader("X-Auth-Token", authToken);
		// / postRequest.setHeader("x-image-meta-id", uuid);
		/*
		 * putRequest.setHeader("x-image-meta-container_format", "bare");
		 * putRequest.setHeader("x-image-meta-disk_format", "qcow2");
		 */
		putRequest.setHeader("Content-Type", "application/octet-stream");
		InputStream ist = new FileInputStream(file);

		HttpEntity inputHttp = new InputStreamEntity(ist);
		putRequest.setEntity(inputHttp);

		HttpResponse response = httpClient.execute(putRequest);

		// // Response response =
		// webTarget.path("/v1/images/"+glanceId).request().header("X-Auth-Token",
		// authToken).put(Entity.entity(f, MediaType.TEXT_PLAIN_TYPE));
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent())));

		String output;
		StringBuffer sb = new StringBuffer();

		while ((output = br.readLine()) != null) {
			sb.append(output);
			// log.debug(output);
		}
		JSONObject obj = new JSONObject(sb.toString());
	}

	public ImageStoreUploadResponse fetchDetails(
			Map<String, String> imageProperties, String glanceId)
			throws IOException {

		Response response = webTarget.path("/v1/images/" + glanceId).request()
				.header("X-Auth-Token", authToken).get();

		InputStream inputStream = (InputStream) response.getEntity();
		ImageStoreUploadResponse imageStoreResponse = new ImageStoreUploadResponse();
		imageStoreResponse.setId(glanceId);
		imageStoreResponse.setImage_uri(response
				.getHeaderString(Constants.GLANCE_HEADER_LOCATION));
		imageStoreResponse.setSent(new Integer(response
				.getHeaderString(Constants.CONTENT_LENGTH)));
		imageStoreResponse.setChecksum(response
				.getHeaderString(Constants.GLANCE_HEADER_CHECKSUM));
		// / imageStoreResponse.setImage_id(Constants.GL);
		// / String type = response.getHeaderString("Content-Type");
		/*
		 * BufferedReader br = new BufferedReader(new InputStreamReader(
		 * inputStream));
		 * 
		 * String output; StringBuffer sb = new StringBuffer();
		 * 
		 * while ((output = br.readLine()) != null) { sb.append(output); //
		 * log.debug(output); }
		 */

		return imageStoreResponse;
	}

	public String uploadImageMetaData(Map<String, String> imageProperties)
			throws IOException {

		/*
		 * File f = new File("C:/MysteryHill/DirectorAll/Docs/vm_launch.txt");
		 * InputStream is = new FileInputStream(f);
		 * 
		 * HttpEntity input = new InputStreamEntity(is);
		 */

		String uuid = (new UUID()).toString();
		Response response;
		if(imageProperties.get(Constants.MTWILSON_TRUST_POLICY_LOCATION)!=null){
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
							imageProperties.get(Constants.MTWILSON_TRUST_POLICY_LOCATION))		
					.post(Entity.json(null));
		}else{
			
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
		JSONObject obj = new JSONObject(sb.toString());
		JSONObject image = obj.getJSONObject("image");
		String id = image.getString("id");
		return id;
	}

	private void createAuthToken(String glanceIP, String tenantName,
			String userName, String password) {

		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
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
			if (response.getStatusLine().getStatusCode() != 200) {
				/*
				 * log.error(null, new
				 * RuntimeException("Failed : HTTP error code : " +
				 * response.getStatusLine().getStatusCode()));
				 */
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			String output;
			StringBuffer sb = new StringBuffer();

			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			JSONObject obj = new JSONObject(sb.toString());
			JSONObject property = obj.getJSONObject("access").getJSONObject(
					"token");
			authToken = property.getString("id");
			httpClient.getConnectionManager().shutdown();
			br.close();
		} catch (MalformedURLException e) {
			// / log.error(null, e);
		} catch (IOException e) {
			// / log.error(null, e);
		} /*
		 * catch (JSONException ex) { /// log.error(null, ex); }
		 */
		// / return authToken;
	}

	public void getImageMetaData() {
	}

	public void deleteImage() {
	}

}
