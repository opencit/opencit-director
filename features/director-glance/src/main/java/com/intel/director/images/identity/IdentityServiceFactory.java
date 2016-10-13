package com.intel.director.images.identity;

import com.intel.director.common.Constants;


public class IdentityServiceFactory {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdentityServiceFactory.class);

	public static IdentityService getIdentityService(String versionType)  {
		IdentityService identityService = null;
		log.info("Finding identity service");
		if (versionType.equals(Constants.VERSION_V2)) {
			identityService = new V2IdentityServiceImpl();
			log.debug("V2 identity service");
		} else if (versionType.equals(Constants.VERSION_V3)) {
			identityService = new V3IdentityServiceImpl();
			log.debug("V3 identity service");
		} 
	
		return identityService;

	}
	
}
