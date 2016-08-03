package com.intel.director.images.identity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.director.images.rs.GlanceException;

public class V2IdentityServiceImpl extends AbstractIdentityService{

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(V2IdentityServiceImpl.class);
	
	public  void validateParams(String tenantName, String userName, String password, String domainName) throws GlanceException{
		if(StringUtils.isBlank(tenantName)){
			throw new GlanceException("Tenant Name cannot be blank");
		}
		if(StringUtils.isBlank(userName)){
			throw new GlanceException("Tenant Name cannot be blank");
		}
		if(StringUtils.isBlank(password)){
			throw new GlanceException("Tenant Name cannot be blank");
		}	
		
	}
	
	public String getAuthRequestBody(String tenantName, String userName, String password, String domainName) throws GlanceException{
		/*	
		 * Example json request :-
		 * 
		 * {"auth": {"tenantName":"admin", "passwordCredentials": {"username": "admin", "password": "intelmh"}}}
		 * 
		 * */
		validateParams(tenantName,userName,password);
		AuthTokenBody authTokenBody = new AuthTokenBody();
		authTokenBody.auth = new Auth();
		authTokenBody.auth.tenantName = tenantName;
		authTokenBody.auth.passwordCredentials = new PasswordCredentials();
		authTokenBody.auth.passwordCredentials.username = userName;
		authTokenBody.auth.passwordCredentials.password = password;
		
		ObjectMapper mapper = new ObjectMapper();
		String body;
		try {
			body = mapper.writeValueAsString(authTokenBody);
		} catch (JsonProcessingException e2) {
			log.error("Error while creating auth token", e2);
			throw new GlanceException("Error while creating auth token", e2);
		}
		
		return body;
	}
	
	public String getAuthTokenFromResponse(HttpResponse response) throws GlanceException{
		String authToken=null;
	
	
		BufferedReader br = null;
		boolean responseHasError = false;

		try {
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

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
					JSONObject property = jsonObjectAccess.getJSONObject("token");
					authToken = property.getString("id");
				} else {
					responseHasError = true;
				}
			} else {
				responseHasError = true;
			}
			// httpClient.getConnectionManager().shutdown();

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
		if (responseHasError) {
			throw new GlanceException("Unable to communicate with Glance at ");
		}
		
		return authToken;
	}
	
	public String getAuthEndpoint(String glanceKeystonePublicEndpoint){
		return glanceKeystonePublicEndpoint + "/v2.0/tokens";
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
	
}

