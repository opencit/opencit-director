package com.intel.director.store.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.director.api.ExternalImageStoreAuthProperties;
import com.intel.director.exception.ImageStoreException;

public class ExternalImageStoreAuthUtil {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExternalImageStoreAuthUtil.class);

	public static String getAuthUrl(ExternalImageStoreAuthProperties authProperties) {
		String authEndpoint = null;
		if (authProperties.getVersion().equals("2")) {
			authEndpoint = authProperties.getAuthUrl() + "/v2.0/tokens";
		} else if (authProperties.getVersion().equals("3")) {
			authEndpoint = authProperties.getAuthUrl() + "/v3/auth/tokens";
		}
		log.info("AUTH Url:  {}", authEndpoint);
		return authEndpoint;

	}

	public static String getAuthBody(ExternalImageStoreAuthProperties authProperties) throws ImageStoreException {
		String authBody = null;
		if (authProperties.getVersion().equals("2")) {
			authBody = getV2AuthBody(authProperties);
		} else if (authProperties.getVersion().equals("3")) {
			authBody = getV3AuthBody(authProperties);
		}		
		return authBody;

	}

	private static String getV2AuthBody(ExternalImageStoreAuthProperties authProperties) throws ImageStoreException {

		AuthTokenBody authTokenBody = new AuthTokenBody();
		authTokenBody.auth = new Auth();
		authTokenBody.auth.tenantName = authProperties.getTenantOrDomain();
		authTokenBody.auth.passwordCredentials = new PasswordCredentials();
		authTokenBody.auth.passwordCredentials.username = authProperties.getUserName();
		authTokenBody.auth.passwordCredentials.password = authProperties.getPassword();

		ObjectMapper mapper = new ObjectMapper();
		String body;
		try {
			body = mapper.writeValueAsString(authTokenBody);
		} catch (JsonProcessingException e2) {
			log.error("Error while creating auth token", e2);
			throw new ImageStoreException("Error while creating auth token", e2);
		}
		return body;
	}

	private static String getV3AuthBody(ExternalImageStoreAuthProperties authProperties) throws ImageStoreException {
		V3AuthTokenBody authTokenBody = new V3AuthTokenBody();
		authTokenBody.auth = new V3Auth();
		authTokenBody.auth.identity = new Identity();
		authTokenBody.auth.identity.methods = new ArrayList<>();
		authTokenBody.auth.identity.methods.add("password");
		authTokenBody.auth.identity.password = new Password();
		authTokenBody.auth.identity.password.user = new User();
		authTokenBody.auth.identity.password.user.name = authProperties.getUserName();
		authTokenBody.auth.identity.password.user.password = authProperties.getPassword();
		authTokenBody.auth.identity.password.user.domain = new Domain();
		authTokenBody.auth.identity.password.user.domain.name = authProperties.getTenantOrDomain();
		ObjectMapper mapper = new ObjectMapper();
		String body;
		try {
			body = mapper.writeValueAsString(authTokenBody);
		} catch (JsonProcessingException e2) {
			log.error("Error while creating auth token", e2);
			throw new ImageStoreException("Error while creating auth token", e2);
		}
		return body;
	}

	public static String getAuthTokenFromResponse(ExternalImageStoreAuthProperties authProperties,
			HttpResponse response) {
		String authToken = null;
		if (authProperties.getVersion().equals("2")) {
			authToken = getV2AuthTokenFromResponse(authProperties, response);
		} else if (authProperties.getVersion().equals("3")) {
			authToken = getV3AuthTokenFromResponse(authProperties, response);
		}
		return authToken;
	}

	private static String getV3AuthTokenFromResponse(ExternalImageStoreAuthProperties authProperties,
			HttpResponse response) {
		log.info("Getting token for auth 3 ");
		Header header = response.getFirstHeader("X-Subject-Token");
		return header.getValue();
	}

	private static String getV2AuthTokenFromResponse(ExternalImageStoreAuthProperties authProperties,
			HttpResponse response) {
		log.info("Getting token for auth 2 ");
		String authToken = null;
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		} catch (IllegalStateException e) {
			log.error("Erorr reading data from response", e);
			return null;
		} catch (IOException e) {
			log.error("Erorr reading data from response", e);
			return null;
		}

		String output;
		StringBuffer sb = new StringBuffer();

		try {
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
		} catch (IOException e) {
			log.error("Erorr reading data from response", e);
			return null;
		}
		// /log.info("createAuthToken response::" + sb.toString());
		JSONObject obj = new JSONObject(sb.toString());
		if (obj.has("access")) {
			JSONObject jsonObjectAccess = obj.getJSONObject("access");
			if (jsonObjectAccess.has("token")) {
				JSONObject property = jsonObjectAccess.getJSONObject("token");
				authToken = property.getString("id");
			}
		}
		return authToken;
	}

}

// V2

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

// V3

@JsonInclude(value = Include.NON_NULL)
class V3AuthTokenBody {
	public V3Auth auth;

}

@JsonInclude(value = Include.NON_NULL)
class V3Auth {
	public Identity identity;

}

@JsonInclude(value = Include.NON_NULL)
class Password {
	public User user;
}

@JsonInclude(value = Include.NON_NULL)
class User {
	public String name;
	public String password;
	public Domain domain;
}

@JsonInclude(value = Include.NON_NULL)
class Identity {
	public List<String> methods;
	public Password password;
}

@JsonInclude(value = Include.NON_NULL)
class Domain {
	public String name;

}
