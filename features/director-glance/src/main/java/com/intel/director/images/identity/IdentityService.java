package com.intel.director.images.identity;

import com.intel.director.images.rs.GlanceException;

public interface IdentityService {
	
	public String createAuthToken(String glanceKeystonePublicEndpoint,
			String tenantOrProjectName, String userName, String password,String domainName)
			throws GlanceException ;
}
