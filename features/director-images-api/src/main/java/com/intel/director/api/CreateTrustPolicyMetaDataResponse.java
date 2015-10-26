package com.intel.director.api;

public class CreateTrustPolicyMetaDataResponse {

	  public String id; // UUID of image
	  public String trustPolicy;//XML 
	  public String status;
	  public String details;
	  
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTrustPolicy() {
		return trustPolicy;
	}
	public void setTrustPolicy(String trustPolicy) {
		this.trustPolicy = trustPolicy;
	}
	  
	  
	  
}
