package com.intel.director.images.identity;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.intel.director.common.ValidationUtil;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.images.rs.GlanceException;

public abstract class  AbstractIdentityService implements IdentityService {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(AbstractIdentityService.class);
	
	public abstract String getAuthEndpoint(String glanceKeystonePublicEndpoint);
		
	
	
	public abstract String getAuthRequestBody(String tenantOrDomainName, String userName, String password, String domainName) throws GlanceException;
	
	public abstract String getAuthTokenFromResponse(HttpResponse response) throws GlanceException;
	
	
	
	public String createAuthToken(String glanceKeystonePublicEndpoint,
			String tenantOrProjectName, String userName, String password , String domainName)
			throws GlanceException {
		String authToken=null;
		long start = new Date().getTime();
		HttpClient httpClient = HttpClientBuilder.create().build();

		String authEndpoint = getAuthEndpoint(glanceKeystonePublicEndpoint);

		try {
			ValidationUtil.validateUrl(glanceKeystonePublicEndpoint, "AUTH");
		} catch (DirectorException e3) {
			throw new GlanceException(e3.getMessage());
		}
	
		HttpPost postRequest = new HttpPost(authEndpoint);
		
		String  body=getAuthRequestBody(tenantOrProjectName,userName,password,domainName);
		
		HttpEntity entity;
		try {
			entity = new ByteArrayEntity(body.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			log.error("Error while creating auth token", e2);
			throw new GlanceException("Error while creating auth token", e2);
		}
		
		postRequest.setEntity(entity);
		postRequest.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		postRequest.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
		HttpResponse response;
		try {
			response = httpClient.execute(postRequest);
		} catch (ClientProtocolException e1) {
			log.error("Error while creating auth token", e1);
			throw new GlanceException("Error while creating auth token", e1);
		} catch (Exception e1) {
			log.error("Error while creating auth token", e1);
			throw new GlanceException("Error while creating auth token", e1);
		}
		if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 201)) {
			log.info("Unable to fetch token by "+authEndpoint+", statusline:" + response.getStatusLine());
			throw new GlanceException("Unable to authenticate with glance");
			
		}
		authToken =getAuthTokenFromResponse(response);
		long end = new Date().getTime();
		printTimeDiff("createAuthToken", start, end);
		
		return authToken;
	}
	
	private void printTimeDiff(String method, long start, long end) {
		log.debug(method + " took " + (end - start) + " ms");
	}
	
	public  void validateParams(String tenantName, String userName, String password) throws GlanceException{
		if(StringUtils.isBlank(tenantName)){
			throw new GlanceException("Tenant/Project Name cannot be blank");
		}
		if(StringUtils.isBlank(userName)){
			throw new GlanceException("Tenant Name cannot be blank");
		}
		if(StringUtils.isBlank(password)){
			throw new GlanceException("Tenant Name cannot be blank");
		}	
		
	}
}
