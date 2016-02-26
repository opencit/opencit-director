package com.intel.director.api;

import java.util.HashMap;
import java.util.Map;

import com.intel.director.constants.Constants;

public enum ConnectorProperties {

	DOCKER("DOCKER", "com.intel.director.dockerhub.DockerHubManager",
			new String[] { Constants.DOCKER_HUB_USERNAME,
					Constants.DOCKER_HUB_PASSWORD, Constants.DOCKER_HUB_EMAIL }, 
					new HashMap<String, String>(){{put(Constants.ARTIFACT_DOCKER, "Docker");}}), 
					
	SWIFT(
			"SWIFT", "com.intel.director.swift.objectstore.SwiftObjectStoreManager",
			new String[] { Constants.SWIFT_API_ENDPOINT,
					Constants.SWIFT_ACCOUNT_NAME,
					Constants.SWIFT_ACCOUNT_USERNAME,
					Constants.SWIFT_ACCOUNT_USER_PASSWORD,
					Constants.SWIFT_CONTAINER_NAME},
			new HashMap<String, String>(){{put(Constants.ARTIFACT_POLICY, "Policy");}}), 
	GLANCE("GLANCE",
			"com.intel.director.images.GlanceImageStoreManager", new String[] {
					Constants.GLANCE_API_ENDPOINT, Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT,
					Constants.GLANCE_IMAGE_STORE_USERNAME,
					Constants.GLANCE_IMAGE_STORE_PASSWORD,
					Constants.GLANCE_TENANT_NAME },
					new HashMap<String, String>(){{
						put(Constants.ARTIFACT_IMAGE, "Image");
						put(Constants.ARTIFACT_TAR, "Tarball");
						put(Constants.ARTIFACT_DOCKER, "Docker");
					}}
	);

	private String name;
	private String driver;

	public void setName(String name) {
		this.name = name;
	}

	private String[] properties;

	private Map<String, String> supportedArtifacts;

	ConnectorProperties(String name, String driver, String[] properties, Map<String, String> supportedArtifacts) {
		this.name = name;
		this.driver = driver;
		this.properties = properties;
		this.supportedArtifacts = supportedArtifacts;
	}

	public String[] getProperties() {
		return properties;
	}

	public String getName() {
		return name;
	}

	public String getDriver() {
		return driver;
	}

	public Map<String, String> getSupported_artifacts() {
		return supportedArtifacts;
	}

	public static ConnectorProperties getConnectorByName(String name) {
		ConnectorProperties connector = null;
		for (ConnectorProperties connectorProperties : ConnectorProperties
				.values()) {
			if (connectorProperties.getName().equalsIgnoreCase(name)) {
				connector = connectorProperties;
				break;
			}
		}
		return connector;
	}

}
