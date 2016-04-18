package com.intel.director.common;

import java.util.HashMap;
import java.util.Map;

import com.intel.mtwilson.trustpolicy.xml.DigestAlgorithm;

public class ImageDeploymentHashTypeCache extends DirectorPropertiesCache {

	public static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageDeploymentHashTypeCache.class);
	private static Map<String, String> deploymentTypeHashTypeMap = new HashMap<String, String>();

	public static void init() {
		getAllValues();
		if (getDigestAlgorithmForDeploymentType(Constants.DEPLOYMENT_TYPE_VM) != null) {
			deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_VM,
					getDigestAlgorithmForDeploymentType(Constants.DEPLOYMENT_TYPE_VM).value());
		}
		if (getDigestAlgorithmForDeploymentType(Constants.DEPLOYMENT_TYPE_BAREMETAL) != null) {
			deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_BAREMETAL,
					getDigestAlgorithmForDeploymentType(Constants.DEPLOYMENT_TYPE_BAREMETAL).value());
		}
		if (getDigestAlgorithmForDeploymentType(Constants.DEPLOYMENT_TYPE_DOCKER) != null) {
			deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_DOCKER,
					getDigestAlgorithmForDeploymentType(Constants.DEPLOYMENT_TYPE_DOCKER).value());
		}
	}

	public static Map<String, String> getAllHashTypes() {
		init();
		return deploymentTypeHashTypeMap;
	}

	public static String getHashTypeByDeploymentType(String deploymentType) {
		init();
		return deploymentTypeHashTypeMap.get(deploymentType);
	}

	public static DigestAlgorithm getDigestAlgorithmForDeploymentType(String deploymentType) {
		if (deploymentType == null) {
			return null;
		}
		DigestAlgorithm algorithm = null;
		switch (deploymentType) {
		case Constants.DEPLOYMENT_TYPE_BAREMETAL:
			algorithm = getHashType(Constants.HASH_TYPE_BAREMETAL, DigestAlgorithm.SHA_1);
			break;
		case Constants.DEPLOYMENT_TYPE_DOCKER:
			algorithm = getHashType(Constants.HASH_TYPE_DOCKER, DigestAlgorithm.SHA_256);
			break;
		case Constants.DEPLOYMENT_TYPE_VM:
			algorithm = getHashType(Constants.HASH_TYPE_VM, DigestAlgorithm.SHA_256);
			break;
		}
		return algorithm;
	}

	private static DigestAlgorithm getHashType(String ht, DigestAlgorithm defaultDigestAlg) {
		String property = properties.getProperty(ht);
		if(property == null){
			return defaultDigestAlg;
		}
		DigestAlgorithm algorithm = null;
		try{
			algorithm = DigestAlgorithm.fromValue(property.toLowerCase());
		}catch(IllegalArgumentException iae){
			log.error("Invalid input {}", property);
		}
		if (algorithm == null) {
			algorithm = defaultDigestAlg;
		}
		return algorithm;
	}
}
