package com.intel.director.api;

public class HashTypeObject {

	private String deploymentType;
	private String hashType;

	public HashTypeObject(String deploymentType, String hashType) {
		super();
		this.deploymentType = deploymentType;
		this.hashType = hashType;
	}

	public String getDeploymentType() {
		return deploymentType;
	}

	public void setDeploymentType(String deploymentType) {
		this.deploymentType = deploymentType;
	}

	public String getHashType() {
		return hashType;
	}

	public void setHashType(String hashtype) {
		this.hashType = hashtype;
	}

}
