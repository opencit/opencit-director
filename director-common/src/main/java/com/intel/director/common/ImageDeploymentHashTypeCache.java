package com.intel.director.common;

import java.util.HashMap;
import java.util.Map;

import com.intel.mtwilson.trustpolicy.xml.DigestAlgorithm;

public class ImageDeploymentHashTypeCache extends DirectorPropertiesCache {
	private static Map<String, String> deploymentTypeHashTypeMap = new HashMap<String, String>();

	public static void init() {
		getAllValues();
		deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_VM,
				properties.getProperty(Constants.HASH_TYPE_VM));
		deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_BAREMETAL,
				properties.getProperty(Constants.HASH_TYPE_BAREMETAL));
		deploymentTypeHashTypeMap.put(Constants.DEPLOYMENT_TYPE_DOCKER,
				properties.getProperty(Constants.HASH_TYPE_DOCKER));
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
		DigestAlgorithm algorithm = null;
		switch (hashType) {
		case "SHA-1":
			algorithm = DigestAlgorithm.SHA_1;
			break;
		case "SHA-256":
			algorithm = DigestAlgorithm.SHA_256;
			break;
		case "MD5":
			algorithm = DigestAlgorithm.MD_5;
			break;
		}
		return algorithm;
	}
}
