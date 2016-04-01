package com.intel.director.common;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.trustpolicy.xml.DigestAlgorithm;

public class ImageDeploymentHashTypeCache extends DirectorPropertiesCache {
	private static final Logger log = LoggerFactory.getLogger(ImageDeploymentHashTypeCache.class);

	private static Map<String, String> deploymentTypeHashTypeMap = new HashMap<String, String>();

	public static void init() {
		getAllValues();
		deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_VM, getDigestAlgorithmForDeploymentTypeOrHashType(Constants.DEPLOYMENT_TYPE_VM, Constants.HASH_TYPE_VM).value());
		deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_BAREMETAL,
				getDigestAlgorithmForDeploymentTypeOrHashType(Constants.DEPLOYMENT_TYPE_BAREMETAL, Constants.HASH_TYPE_BAREMETAL).value());
		deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_DOCKER,
				getDigestAlgorithmForDeploymentTypeOrHashType(Constants.DEPLOYMENT_TYPE_DOCKER, Constants.HASH_TYPE_DOCKER).value());
	}

	public static Map<String, String> getAllHashTypes() {
		init();
		return deploymentTypeHashTypeMap;
	}

	public static String getHashTypesByDeploymentType(String deploymentType) {
		init();
		return deploymentTypeHashTypeMap.get(deploymentType);
	}

	public static DigestAlgorithm getDigestAlgorithmForHashType(String hashType) {
		if(hashType == null){
			return null;
		}
		DigestAlgorithm algorithm = null;
		try {
			algorithm = DigestAlgorithm.fromValue(hashType.toLowerCase());
		} catch (IllegalArgumentException argumentException) {
			log.error("No matching digest alg found for hashtype {}", hashType);
		}
		return algorithm;
	}

	public static DigestAlgorithm getDigestAlgorithmForDeploymentType(String deploymentType) {
		if(deploymentType == null){
			return null;
		}
		DigestAlgorithm algorithm = null;
		switch (deploymentType) {
		case Constants.DEPLOYMENT_TYPE_BAREMETAL:
			algorithm = DigestAlgorithm.SHA_1;
			break;
		case Constants.DEPLOYMENT_TYPE_DOCKER:
			algorithm = DigestAlgorithm.SHA_256;
			break;
		case Constants.DEPLOYMENT_TYPE_VM:
			algorithm = DigestAlgorithm.SHA_256;
			break;
		}
		return algorithm;
	}
	
	public static DigestAlgorithm getDigestAlgorithmForDeploymentTypeOrHashType(String deploymentType, String hashType) {
		DigestAlgorithm digestAlgorithm = getDigestAlgorithmForHashType(hashType) ;
		
		if(digestAlgorithm == null){
			digestAlgorithm = getDigestAlgorithmForDeploymentType(deploymentType);
		}
		return digestAlgorithm;
	}

}
