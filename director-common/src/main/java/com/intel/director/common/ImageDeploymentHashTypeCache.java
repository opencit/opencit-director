package com.intel.director.common;

import java.util.HashMap;
import java.util.Map;

import com.intel.mtwilson.trustpolicy2.xml.DigestAlgorithm;

public class ImageDeploymentHashTypeCache extends DirectorPropertiesCache {

	public static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImageDeploymentHashTypeCache.class);
	private static Map<String, String> deploymentTypeHashTypeMap = new HashMap<String, String>();

	public static void init() {
		getAllValues();
		DigestAlgorithm digestAlgorithmForDeploymentType = getDigestAlgorithmForDeploymentType(
				Constants.DEPLOYMENT_TYPE_VM);
		if (digestAlgorithmForDeploymentType != null) {
			deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_VM, digestAlgorithmForDeploymentType.value());
		}
		digestAlgorithmForDeploymentType = getDigestAlgorithmForDeploymentType(Constants.DEPLOYMENT_TYPE_BAREMETAL);
		if (digestAlgorithmForDeploymentType != null) {
			deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_BAREMETAL,
					digestAlgorithmForDeploymentType.value());
		}
		digestAlgorithmForDeploymentType = getDigestAlgorithmForDeploymentType(Constants.DEPLOYMENT_TYPE_DOCKER);
		if (digestAlgorithmForDeploymentType != null) {
			deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_DOCKER, digestAlgorithmForDeploymentType.value());
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
		if (property == null) {
			return defaultDigestAlg;
		}
		DigestAlgorithm algorithm = null;
		try {
			algorithm = DigestAlgorithm.fromValue(property.toLowerCase());
		} catch (IllegalArgumentException iae) {
			log.error("Invalid input {}", property);
		}
		if (algorithm == null) {
			algorithm = defaultDigestAlg;
		}
		return algorithm;
	}
}
