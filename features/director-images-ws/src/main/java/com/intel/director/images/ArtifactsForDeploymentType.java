package com.intel.director.images;

import java.util.HashMap;
import java.util.Map;

import com.intel.director.common.Constants;

public enum ArtifactsForDeploymentType {

	DOCKER(Constants.DEPLOYMENT_TYPE_DOCKER), VM(Constants.DEPLOYMENT_TYPE_VM);

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private Map<String, String> artifacts;

	public Map<String, String> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(Map<String, String> artifacts) {
		this.artifacts = artifacts;
	}

	ArtifactsForDeploymentType(String name) {
		artifacts = new HashMap<String, String>();
		if (Constants.DEPLOYMENT_TYPE_VM.equals(name)) {
			artifacts.put(Constants.ARTIFACT_IMAGE, "Image");
			artifacts.put(Constants.ARTIFACT_POLICY, "Policy");
			artifacts.put(Constants.ARTIFACT_TAR, "Image With Policy" );
			artifacts.put(Constants.ARTIFACT_IMAGE_WITH_POLICY, "Image with Policy Separated");
		} else if (Constants.DEPLOYMENT_TYPE_DOCKER.equals(name)) {
			artifacts.put(Constants.ARTIFACT_DOCKER_IMAGE, "Docker Image");
			artifacts.put(Constants.ARTIFACT_DOCKER_WITH_POLICY, "Docker Image With Policy");
		}
	}

}
