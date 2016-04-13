package com.intel.director.api;

public class ImageActionHistoryResponse  {


	private String storeNames;
	private String executionStatus;
	private String id;
	private String artifactName;
	private String datetime;
	
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	public String getStoreNames() {
		return storeNames;
	}
	public void setStoreNames(String storeNames) {
		this.storeNames = storeNames;
	}
	public String getExecutionStatus() {
		return executionStatus;
	}
	public void setExecutionStatus(String executionStatus) {
		this.executionStatus = executionStatus;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getArtifactName() {
		return artifactName;
	}
	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}
	
	
}
