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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import com.intel.director.common.Constants;
import com.intel.director.common.ValidationUtil;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.images.identity.IdentityService;
import com.intel.director.images.identity.IdentityServiceFactory;
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
			String glanceApiEndpoint,
			String glanceKeystonePublicEndpoint, String tenanatOrProjectName,
			String username, String password,String domainName ,String version) throws GlanceException {
		this.webTarget = webTarget;
		this.client = client;
		validateUrl(glanceApiEndpoint, "API");
		createAuthToken(glanceKeystonePublicEndpoint, tenanatOrProjectName, username,
				password,domainName, version);
	}

	public void uploadImage(Map<String, Object> customProperties)
			throws GlanceException {
		long start = new Date().getTime();
		// BufferedReader br = null;
		InputStream ist = null;
		HttpClient httpClient = HttpClientBuilder.create().build();
		String glanceId = null;
		
		if (customProperties.get(Constants.GLANCE_ID) != null) {
			glanceId = (String) customProperties.get(Constants.GLANCE_ID);
		}

		HttpPut putRequest = new HttpPut(webTarget.getUri().toString()
				+ GLANCE_API_VERSION + "/" + glanceId + "/file");
		log.debug("upload Image  uri:: " + putRequest.getURI());
		putRequest.setHeader(Constants.AUTH_TOKEN, authToken);

		putRequest.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
		File file = new File(
				(String) customProperties
						.get(com.intel.director.common.Constants.UPLOAD_TO_IMAGE_STORE_FILE));

		HttpResponse response;
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
		InputStream inputStream = null;
		BufferedReader br = null;
		Response response = webTarget.path(GLANCE_API_VERSION + "/" + glanceId).request()
				.header(Constants.AUTH_TOKEN, authToken).get();

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
		log.info("Inside glance upload image metadata glanceid:: " + imageProperties.get(Constants.GLANCE_ID));
		HttpClient httpClient = HttpClientBuilder.create().build();
		String id;
		BufferedReader br = null;

		String glanceId;
		if (imageProperties.get(Constants.GLANCE_ID) != null) {
			glanceId = (String) imageProperties.get(Constants.GLANCE_ID);
			log.info(("Inside upload image metadata glanceId: " + glanceId));
		} else {
			glanceId = (new UUID()).toString();
			log.info("Inside upload image metadata no glanceId set in properties so creating new: " + glanceId);
		}
		HttpPost postRequest = new HttpPost(webTarget.getUri() + GLANCE_API_VERSION);

		GlanceImageUploadBody glanceImageUploadBody = new GlanceImageUploadBody();
		glanceImageUploadBody.container_format = (String) imageProperties.get(Constants.CONTAINER_FORMAT);
		glanceImageUploadBody.disk_format = (String) imageProperties.get(Constants.DISK_FORMAT);
		glanceImageUploadBody.name = (String) imageProperties.get(Constants.NAME);
		glanceImageUploadBody.id = glanceId;
		glanceImageUploadBody.visibility = (String) imageProperties.get(Constants.GLANCE_VISIBILITY);
		if (imageProperties.get(Constants.MTWILSON_TRUST_POLICY_LOCATION) != null) {
			glanceImageUploadBody.mtwilson_trustpolicy_location = (String) imageProperties
					.get(Constants.MTWILSON_TRUST_POLICY_LOCATION);
		}

		ObjectMapper mapper = new ObjectMapper();
		String uploadBody = null;
		try {
			uploadBody = mapper.writeValueAsString(glanceImageUploadBody);
		} catch (JsonProcessingException e3) {
			String msg = "Error converting json upload data to string";
			log.error(msg, e3);
			throw new GlanceException(msg, e3);
		}
		log.info("Metadata body {} and authtoken {}", uploadBody, authToken);
		HttpEntity entity;
		if(uploadBody==null){
			throw new GlanceException("uploadimgeMetadata failed");
		}
		try {
			entity = new ByteArrayEntity(uploadBody.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			log.error("uploadimgeMetadata failed", e2);
			throw new GlanceException("uploadimgeMetadata failed", e2);
		}

		postRequest.setEntity(entity);
		postRequest.setHeader("Content-Type", "application/json");

		postRequest.setHeader("X-Auth-Token", authToken);

		HttpResponse httpResponse;

		try {
			httpResponse = httpClient.execute(postRequest);
		} catch (Exception e1) {
			log.error("uploadimgeMetadata failed", e1);
			throw new GlanceException("uploadimgeMetadata failed", e1);
		}
		log.info("###### uploadmetadata Status::" + httpResponse.getStatusLine().getStatusCode() + " for glanceid::"
				+ glanceId);
		if (httpResponse.getStatusLine().getStatusCode() != 200
				&& httpResponse.getStatusLine().getStatusCode() != 201) {
			log.error("uploadimgeMetadata failed," + httpResponse.getStatusLine());
			throw new GlanceException(com.intel.director.common.Constants.ARTIFACT_ID + ":" + glanceId);
		}

		try {
			br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

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
			log.error("uploadimgeMetadata failed", e);
			throw new GlanceException("uploadimgeMetadata failed", e);
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

		ImageStoreUploadResponse storeUploadResponse = fetchDetails(imageProperties);

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
		String body;
		try {
			body = objectMapper.writeValueAsString(updatesList);
		} catch (JsonProcessingException e1) {
			log.error("updateMetadata failed", e1);
			throw new GlanceException("updateMetadata failed", e1);
		}

		log.info("Glance updateMetadata operation:: {}", body);

		HttpEntity entity;
		try {
			entity = new ByteArrayEntity(body.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			log.error("updateMetadata failed", e2);
			throw new GlanceException("updateMetadata failed", e2);
		}
		log.debug("Glance updateMetadata body:: " + body);
		patchRequest.setEntity(entity);
		patchRequest.setHeader(HttpHeaders.CONTENT_TYPE,
				"application/openstack-images-v2.1-json-patch");

		patchRequest.setHeader(Constants.AUTH_TOKEN,  authToken);

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
			String tenantOrProjectName, String userName, String password, String domainName,String version)
 throws GlanceException {
		IdentityService identityService = null;
		

		// We first try getting token by v2.0 identity Service and if it fails
		// we use v3 identity service
		
        if (Constants.VERSION_V2.equalsIgnoreCase(version)) {
            identityService = IdentityServiceFactory
                    .getIdentityService(Constants.VERSION_V2);

        } else if (Constants.VERSION_V3.equalsIgnoreCase(version)) {

            identityService = IdentityServiceFactory
                    .getIdentityService(Constants.VERSION_V3);
        }
        if (identityService == null) {
            log.error("Identity service value is null");
	        throw new GlanceException("Identity service value is null");
        }
        authToken = identityService.createAuthToken(
                glanceKeystonePublicEndpoint, tenantOrProjectName, userName,
                password, domainName);
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
		URL url;
		try {
			if (glanceIP.contains("http")) {
				url = new URL(glanceIP);
			} else {
				url = new URL("http://" + glanceIP);
			}
			String path = url.getPath();
			if(StringUtils.isNotBlank(path)){
				throw new GlanceException("Please provide the API endpoint in format http(s)://<HOST>:<PORT>");
			}

		} catch (MalformedURLException e) {
			log.error("Unable to create valid URL", e);
			throw new GlanceException("Unable to create valid URL", e);
		}

		Client client = ClientBuilder.newBuilder().build();
		WebTarget target = client.target(url.toExternalForm());
		Response response;
		try {
			response = target.path("/v1/images/detail").request().header(Constants.AUTH_TOKEN, authToken).get();
		} catch (ProcessingException pe) {
			log.error("Unable to create valid URL", pe);
			throw new GlanceException("Invalid API endpoint", pe);
		}
		
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
			log.error("Unable to read response from Glance");
			throw new GlanceException("Unable to read response from Glance");
		}
		log.debug("Response from glance {}", sb.toString());
		JSONObject obj = new JSONObject(sb.toString());
		if(!obj.has("images")){
			throw new GlanceException("Unable to fetch images from store");
		}
		JSONArray images = obj.getJSONArray("images");
		for (int i = 0; i < images.length(); i++) {
			ImageStoreUploadResponse object = new ImageStoreUploadResponse();
			JSONObject json = (JSONObject) images.get(i);
			object.setId(json.getString("id"));
			if (json.has("properties")) {
				JSONObject jsonPropObject = json.getJSONObject("properties");
				if (jsonPropObject.has("mtwilson_trustpolicy_location")) {
						object.setMtwilson_trust_policy_location(jsonPropObject
								.getString("mtwilson_trustpolicy_location"));
				}
			}
			list.add(object);
		}

		return list;

	}
	
	
	private void validateUrl(String urlStr, String type) throws GlanceException{
		try {
			ValidationUtil.validateUrl(urlStr, type);
		} catch (DirectorException e) {
			throw new GlanceException(e.getMessage());
		}
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