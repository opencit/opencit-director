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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.constants.Constants;

/**
 * 
 * @author Aakash
 */
public class GlanceRsClient {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(GlanceRsClient.class);
	public static final String GLANCE_API_VERSION = "/v2/images";
	public WebTarget webTarget;
	public Client client;
	public String authToken;

	public GlanceRsClient(WebTarget webTarget, Client client,
			String glanceKeystonePublicEndpoint, String tenanatName,
			String username, String password) throws GlanceException {
		this.webTarget = webTarget;
		this.client = client;

		createAuthToken(glanceKeystonePublicEndpoint, tenanatName, username,
				password);
	}

	public void uploadImage(Map<String, Object> customProperties)
			throws GlanceException {
		long start = new Date().getTime();
		// BufferedReader br = null;
		InputStream ist = null;
		HttpClient httpClient = null;
		String glanceId = null;

		httpClient = HttpClientBuilder.create().build();
		if (customProperties.get(Constants.GLANCE_ID) != null) {
			glanceId = (String) customProperties.get(Constants.GLANCE_ID);
		}

		HttpPut putRequest = new HttpPut(webTarget.getUri().toString()
				+ GLANCE_API_VERSION + "/" + glanceId + "/file");
		log.debug("upload Image  uri:: " + putRequest.getURI());
		putRequest.setHeader("X-Auth-Token", authToken);

		putRequest.setHeader("Content-Type", "application/octet-stream");
		File file = new File(
				(String) customProperties
						.get(com.intel.director.common.Constants.UPLOAD_TO_IMAGE_STORE_FILE));

		HttpResponse response = null;
		try {
			ist = new FileInputStream(file);

			HttpEntity inputHttp = new InputStreamEntity(ist);
			putRequest.setEntity(inputHttp);

			response = httpClient.execute(putRequest);

		} catch (Exception e) {
			log.error("Glance, uploadImage failed", e);
			throw new GlanceException("Glance, uploadImage failed", e);
		} finally {
			try {
				if (ist != null) {
					ist.close();
				}

			} catch (IOException e) {
				log.error("Unable close stream, Glance uploadImage method", e);

			}
		}
		log.info("###### uploadImage , Status:: "
				+ response.getStatusLine().getStatusCode() + " for glanceid:: "
				+ glanceId);
		int status = response.getStatusLine().getStatusCode();
		if ((status != 200) && (status != 204)) {
			log.error("Glance uploadImage failed,uploadImage method returned with status code status:: "
					+ response.getStatusLine());
			throw new GlanceException(
					"Glance uploadImage failed,uploadImage method returned with status code status:: "
							+ response.getStatusLine());
		}

		long end = new Date().getTime();
		printTimeDiff("uploadImage", start, end);
	}

	public ImageStoreUploadResponse fetchDetails(
			Map<String, Object> customProperties) throws GlanceException {
		long start = new Date().getTime();
		log.debug("# Inside fetchDetails::" + customProperties);
		String glanceId = (String) customProperties.get(Constants.GLANCE_ID);
		ImageStoreUploadResponse imageImageStoreUploadResponse = new ImageStoreUploadResponse();
		// / System.out.println("Fetach details glanceId::" + glanceId);
		InputStream inputStream = null;
		BufferedReader br = null;
		Response response = null;

		response = webTarget.path(GLANCE_API_VERSION + "/" + glanceId)
				.request().header("X-Auth-Token", authToken).get();

		// /System.out.println("Inside fetch details reponse::" + response);
		log.info("###### fetch details status::" + response.getStatus());
		if (response.getStatus() < 200 && response.getStatus() > 204) {
			log.error("fetchDetails failed," + response.getStatus());

			throw new GlanceException("fetchDetails failed,"
					+ response.getStatus());
		}

		try {
			inputStream = (InputStream) response.getEntity();

			br = new BufferedReader(new InputStreamReader(inputStream));

			String output;
			StringBuffer sb = new StringBuffer();

			while ((output = br.readLine()) != null) {
				sb.append(output);
				// log.debug(output);
			}
			log.debug("=========fetch details sb::" + sb);
			JSONObject obj = new JSONObject(sb.toString());
			// /log.debug("Glance, FetchDetails json obj::" + obj);
			imageImageStoreUploadResponse.setId(glanceId);
			imageImageStoreUploadResponse.setImage_uri(webTarget.getUri()
					+ obj.getString(Constants.SELF));
			// / imageImageStoreUploadResponse.setSent(new Integer(obj
			// .getInt(Constants.SIZE)));
			imageImageStoreUploadResponse.setChecksum(obj
					.getString(Constants.CHECKSUM));
			if (obj.has(Constants.MTWILSON_TRUST_POLICY_LOCATION)) {
				imageImageStoreUploadResponse
						.setMtwilson_trust_policy_location(obj
								.getString(Constants.MTWILSON_TRUST_POLICY_LOCATION));
			}
		} catch (Exception e) {
			log.error("Exception in fetchDetails, glance", e);
			throw new GlanceException("fetchDetails failed", e);

		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				log.error("Unable to close stream ,Glance fetchDetails method",
						e);

			}
		}
		long end = new Date().getTime();
		printTimeDiff("fetchDetails", start, end);
		imageImageStoreUploadResponse.setStatus(Constants.COMPLETE);
		return imageImageStoreUploadResponse;
	}

	public String uploadImageMetaData(Map<String, Object> imageProperties)
			throws GlanceException {
		String uuid = null;
		log.info("Inside glance upload image metadata glanceid:: "
				+ imageProperties.get(Constants.GLANCE_ID));
		if (imageProperties.get(Constants.GLANCE_ID) != null) {
			log.info("IMage id is provided : {}",
					(String) imageProperties.get(Constants.GLANCE_ID));
			uuid = (String) imageProperties.get(Constants.GLANCE_ID);
		} else {
			log.info("Generating new uuid in Glance  uploadImageMetaData");
			uuid = (new UUID()).toString();
		}

		log.info("No mtw TP location");
		HttpClient httpClient = HttpClientBuilder.create().build();
		String id = null;
		BufferedReader br = null;

		String glanceId = null;
		if (imageProperties.get(Constants.GLANCE_ID) != null) {
			glanceId = (String) imageProperties.get(Constants.GLANCE_ID);

			log.info(("Inside upload image metadata glanceId: " + glanceId));
		} else {
			glanceId = (new UUID()).toString();
			log.info("Inside upload image metadata no glanceId set in properties so creating new: "
					+ glanceId);
		}
		HttpPost postRequest = new HttpPost(webTarget.getUri()
				+ GLANCE_API_VERSION);

		GlanceImageUploadBody glanceImageUploadBody = new GlanceImageUploadBody();
		glanceImageUploadBody.container_format = (String) imageProperties
				.get(Constants.CONTAINER_FORMAT);
		glanceImageUploadBody.disk_format = (String) imageProperties
				.get(Constants.DISK_FORMAT);
		glanceImageUploadBody.name = (String) imageProperties
				.get(Constants.NAME);
		glanceImageUploadBody.id = glanceId;
		glanceImageUploadBody.visibility = (String) imageProperties
				.get(Constants.VISIBILITY);
		if (imageProperties.get(Constants.MTWILSON_TRUST_POLICY_LOCATION) != null) {
			glanceImageUploadBody.mtwilson_trustpolicy_location = (String) imageProperties
					.get(Constants.MTWILSON_TRUST_POLICY_LOCATION);
		}

		ObjectMapper mapper = new ObjectMapper();
		String uploadBody = null;
		try {
			uploadBody = mapper.writeValueAsString(glanceImageUploadBody);
		} catch (JsonProcessingException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		log.info("Metadata body {} and authtoken {}", uploadBody, authToken);
		HttpEntity entity = null;
		try {
			entity = new ByteArrayEntity(uploadBody.toString()
					.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			log.error("uploadimgeMetadata failed",e2);
			throw new GlanceException("uploadimgeMetadata failed",e2);
		}

		postRequest.setEntity(entity);
		postRequest.setHeader("Content-Type", "application/json");

		postRequest.setHeader("X-Auth-Token", authToken);

		HttpResponse httpResponse = null;

		try {
			httpResponse = httpClient.execute(postRequest);
		} catch (Exception e1) {
			log.error("uploadimgeMetadata failed",e1);
			throw new GlanceException("uploadimgeMetadata failed",e1);
		}
		log.info("###### uploadmetadata Status::"
				+ httpResponse.getStatusLine().getStatusCode()
				+ " for glanceid::" + glanceId);
		if (httpResponse.getStatusLine().getStatusCode() != 200
				&& httpResponse.getStatusLine().getStatusCode() != 201) {
			log.error("uploadimgeMetadata failed,"
					+ httpResponse.getStatusLine());
			throw new GlanceException("uploadimgeMetadata failed,"
					+ httpResponse.getStatusLine());
		}

		try {
			br = new BufferedReader(new InputStreamReader(
					(httpResponse.getEntity().getContent())));

			String output;
			StringBuffer sb = new StringBuffer();

			while ((output = br.readLine()) != null) {
				sb.append(output);
				// log.debug(output);
			}
			log.info("uploadImageMetaData response::" + sb);
			JSONObject obj = new JSONObject(sb.toString());

			id = obj.getString("id");
		} catch (IOException e) {
			log.error("uploadimgeMetadata failed",e);
			throw new GlanceException("uploadimgeMetadata failed",e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return id;
	}

	public void updateMetadata(Map<String, Object> imageProperties)
			throws GlanceException {

		HttpClient httpClient = HttpClientBuilder.create().build();
		// String id = null;
		BufferedReader br = null;

		String glanceId = null;

		ImageStoreUploadResponse storeUploadResponse = null;

		storeUploadResponse = fetchDetails(imageProperties);

		if (imageProperties.get(Constants.GLANCE_ID) != null) {
			glanceId = (String) imageProperties.get(Constants.GLANCE_ID);
		}
		HttpPatch patchRequest = new HttpPatch(webTarget.getUri()
				+ "/v2/images/" + glanceId);

		String op = "add";
		;

		if (storeUploadResponse.getMtwilson_trust_policy_location() != null) {
			op = "replace";
		}
		log.info("Glance updateMetadata operation:: " + op);

		GlanceUpateMetadataBody glanceUpateMetadataBody = new GlanceUpateMetadataBody();
		glanceUpateMetadataBody.op = op;
		glanceUpateMetadataBody.path = "/"
				+ Constants.MTWILSON_TRUST_POLICY_LOCATION;
		glanceUpateMetadataBody.value = (String) imageProperties
				.get(Constants.MTWILSON_TRUST_POLICY_LOCATION);

		List<GlanceUpateMetadataBody> updatesList = new ArrayList<GlanceUpateMetadataBody>(
				1);
		updatesList.add(glanceUpateMetadataBody);
		ObjectMapper objectMapper = new ObjectMapper();
		String body = null;
		try {
			body = objectMapper.writeValueAsString(updatesList);
		} catch (JsonProcessingException e1) {
			log.error("updateMetadata failed", e1);
			throw new GlanceException("updateMetadata failed", e1);
		}

		log.info("Glance updateMetadata operation:: {}", body);

		HttpEntity entity = null;
		try {
			entity = new ByteArrayEntity(body.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			log.error("updateMetadata failed", e2);
			throw new GlanceException("updateMetadata failed", e2);

		}
		log.debug("Glance updateMetadata body:: " + body);
		patchRequest.setEntity(entity);
		patchRequest.setHeader("Content-Type",
				"application/openstack-images-v2.1-json-patch");

		patchRequest.setHeader("X-Auth-Token", authToken);

		HttpResponse response;

		try {
			response = httpClient.execute(patchRequest);
		} catch (IOException e1) {
			log.error("updateMetadata failed", e1);
			throw new GlanceException("updateMetadata failed", e1);
		}
		log.info("###### update metadata status::"
				+ response.getStatusLine().getStatusCode() + " for glanceId::"
				+ glanceId);
		if (response.getStatusLine().getStatusCode() != 200) {
			log.error("updatemetadata failed," + response.getStatusLine());
			throw new GlanceException("updateMetadata failed,"
					+ response.getStatusLine());
		}
		try {
			br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			String output;
			StringBuffer sb = new StringBuffer();

			while ((output = br.readLine()) != null) {
				sb.append(output);
				// log.debug(output);
			}
			log.debug("Glance updateMetadata response::" + sb);

		} catch (Exception e) {
			log.error("Glance , updateMetadata failed", e);
			throw new GlanceException("Glance , updateMetadata failed", e);
		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("Error closing reader", e);
				}
			}
		}

	}

	private void createAuthToken(String glanceKeystonePublicEndpoint,
			String tenantName, String userName, String password)
			throws GlanceException {
		long start = new Date().getTime();
		// TODO not used after being assigned
		HttpClient httpClient = null;
		BufferedReader br = null;
		boolean responseHasError = false;
		String authEndpoint = glanceKeystonePublicEndpoint + "/v2.0/tokens";

		
			httpClient = HttpClientBuilder.create().build();
			HttpPost postRequest = new HttpPost(authEndpoint);

			AuthTokenBody authTokenBody = new AuthTokenBody();
			authTokenBody.auth = new Auth();
			authTokenBody.auth.tenantName = tenantName;
			authTokenBody.auth.passwordCredentials = new PasswordCredentials();
			authTokenBody.auth.passwordCredentials.username = userName;
			authTokenBody.auth.passwordCredentials.password = password;

			ObjectMapper mapper = new ObjectMapper();
			String body = null;
			try {
				body = mapper.writeValueAsString(authTokenBody);
			} catch (JsonProcessingException e2) {
				log.error("Error while creating auth token", e2);
				throw new GlanceException("Error while creating auth token", e2);
			}
			// log.info("Auth token body {}", body);
			HttpEntity entity =null;
			try {
				entity = new ByteArrayEntity(body.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e2) {
				log.error("Error while creating auth token", e2);
				throw new GlanceException("Error while creating auth token", e2);
			}

			postRequest.setEntity(entity);
			postRequest.setHeader("Content-Type", "application/json");
			postRequest.setHeader("Accept", "application/json");
			HttpResponse response = null;
			try {
				response = httpClient.execute(postRequest);
			} catch (ClientProtocolException e1) {
				log.error("Error while creating auth token", e1);
				throw new GlanceException("Error while creating auth token", e1);
			} catch (Exception e1) {
				log.error("Error while creating auth token", e1);
				throw new GlanceException("Error while creating auth token", e1);
			}
			
			try{
			br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			String output;
			StringBuffer sb = new StringBuffer();

			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			// /log.info("createAuthToken response::" + sb.toString());
			JSONObject obj = new JSONObject(sb.toString());
			if (obj.has("access")) {
				JSONObject jsonObjectAccess = obj.getJSONObject("access");
				if (jsonObjectAccess.has("token")) {
					JSONObject property = jsonObjectAccess
							.getJSONObject("token");
					authToken = property.getString("id");
				} else {
					responseHasError = true;
				}
			} else {
				responseHasError = true;
			}
			// httpClient.getConnectionManager().shutdown();

		} catch (IOException e) {
			log.error("Error while creating auth token", e);
		} catch (Exception e) {
			log.error("Error while creating auth token", e);
			throw new GlanceException("Error while creating auth token", e);
		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("Error closing reader", e);
				}
			}

		}
		long end = new Date().getTime();
		printTimeDiff("createAuthToken", start, end);
		if (responseHasError) {
			throw new GlanceException("Unable to communicate with Glance at "
					+ authEndpoint);
		}
	}

	public void getImageMetaData() {
	}

	public void deleteImage() {
	}

	public List<ImageStoreUploadResponse> fetchAllImages(
			Map<String, Object> configuration) throws GlanceException {
		List<ImageStoreUploadResponse> list = new ArrayList<ImageStoreUploadResponse>();
		String glanceIP = (String) (configuration
				.get(Constants.GLANCE_API_ENDPOINT) == null ? ""
				: configuration.get(Constants.GLANCE_API_ENDPOINT));
		URL url = null;
		try {
			if (glanceIP.contains("http")) {
				url = new URL(glanceIP);
			} else {
				url = new URL("http://" + glanceIP);
			}
		} catch (MalformedURLException e) {
			log.error("Unable to create valid URL", e);
			throw new GlanceException("Unable to create valid URL", e);
		}

		Client client = ClientBuilder.newBuilder().build();
		WebTarget target = client.target(url.toExternalForm());
		Response response = target.path("/v1/images/detail").request()
				.header("X-Auth-Token", authToken).get();

		InputStream inputStream = (InputStream) response.getEntity();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));

		String output;
		StringBuffer sb = new StringBuffer();

		try {
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject obj = new JSONObject(sb.toString());
		JSONArray images = obj.getJSONArray("images");
		for (int i = 0; i < images.length(); i++) {
			ImageStoreUploadResponse object = new ImageStoreUploadResponse();
			JSONObject json = (JSONObject) images.get(i);
			object.setId(json.getString("id"));
			list.add(object);
		}

		return list;

	}

	private void printTimeDiff(String method, long start, long end) {
		log.debug(method + " took " + (end - start) + " ms");
	}

}

@JsonInclude(value = Include.NON_NULL)
class GlanceImageUploadBody {
	public String container_format;
	public String disk_format;
	public String name;
	public String id;
	public String visibility;
	public String mtwilson_trustpolicy_location;
}

@JsonInclude(value = Include.NON_NULL)
class AuthTokenBody {
	public Auth auth;

}

@JsonInclude(value = Include.NON_NULL)
class Auth {
	public String tenantName;
	public PasswordCredentials passwordCredentials;

}

@JsonInclude(value = Include.NON_NULL)
class PasswordCredentials {
	public String username;
	public String password;
}

@JsonInclude(value = Include.NON_NULL)
class GlanceUpateMetadataBody {
	public String op;
	public String path;
	public String value;
}