package com.intel.director.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.intel.director.constants.Constants;

public enum ConnectorProperties {

	DOCKER("DOCKER", "com.intel.director.dockerhub.DockerHubManager",
			new ConnectorKey[] {
					new ConnectorKey(1, Constants.DOCKER_HUB_USERNAME),
					new ConnectorKey(3, Constants.DOCKER_HUB_PASSWORD),
					new ConnectorKey(2, Constants.DOCKER_HUB_EMAIL) },
			new HashMap<String, String>() {
				{
					put(Constants.ARTIFACT_DOCKER, "Docker");
				}
			}), 
	/*SWIFT("SWIFT", "com.intel.director.swift.objectstore.SwiftObjectStoreManager",
			new ConnectorKey[] {
					new ConnectorKey(1, Constants.SWIFT_API_ENDPOINT),
					new ConnectorKey(2, Constants.SWIFT_AUTH_ENDPOINT),
					new ConnectorKey(3, Constants.SWIFT_TENANT_NAME),
					new ConnectorKey(4, Constants.SWIFT_ACCOUNT_USERNAME),
					new ConnectorKey(5, Constants.SWIFT_ACCOUNT_USER_PASSWORD),
					new ConnectorKey(6, Constants.SWIFT_CONTAINER_NAME),
					new ConnectorKey(7, Constants.SWIFT_KEYSTONE_SERVICE_NAME) },
			new HashMap<String, String>() {
				{
					put(Constants.ARTIFACT_POLICY, "Policy");
				}
			}), */
	GLANCE("GLANCE", "com.intel.director.images.GlanceImageStoreManager",
			new ConnectorKey[] {
					new ConnectorKey(1, Constants.GLANCE_API_ENDPOINT),
					new ConnectorKey(2,
							Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT),
					new ConnectorKey(3, Constants.GLANCE_TENANT_NAME),
					new ConnectorKey(4, Constants.GLANCE_IMAGE_STORE_USERNAME),
					new ConnectorKey(5, Constants.GLANCE_IMAGE_STORE_PASSWORD),
					new ConnectorKey(6, Constants.GLANCE_VISIBILITY) },
			new HashMap<String, String>() {
				{
					put(Constants.ARTIFACT_IMAGE, "Image");
					put(Constants.ARTIFACT_TAR, "Tarball");
					put(Constants.ARTIFACT_DOCKER, "Docker");
				}
			}

	);

	private String name;
	private String driver;

	public void setName(String name) {
		this.name = name;
	}

	private ConnectorKey[] properties;

	private Map<String, String> supportedArtifacts;

	ConnectorProperties(String name, String driver, ConnectorKey[] properties,
			Map<String, String> supportedArtifacts) {
		this.name = name;
		this.driver = driver;
		this.properties = properties;
		this.supportedArtifacts = supportedArtifacts;
	}

	public ConnectorKey[] getProperties() {
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

	public int fetchSeqNoOfProperty(String propKey) {
		int tempSeqNo = 0;
		if (StringUtils.isBlank(propKey)) {
			return tempSeqNo;
		}
		for (ConnectorKey tempProp : properties) {
			if (propKey.equals(tempProp.getKey())) {
				tempSeqNo = tempProp.getSeqNo();
				break;
			}
		}
		return tempSeqNo;
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