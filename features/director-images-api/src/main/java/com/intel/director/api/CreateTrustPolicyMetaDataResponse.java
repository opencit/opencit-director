package com.intel.director.api;

public class CreateTrustPolicyMetaDataResponse extends
		CreateTrustPolicyMetaDataRequest {
	public String id;
	public String trustPolicy;// XML
	public String status;
	public String details;
	
	//Changes for BM
	public String ip_address;
	public String username;
	

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

	public String getTrustPolicy() {
		return trustPolicy;
	}

	public void setTrustPolicy(String trustPolicy) {
		this.trustPolicy = trustPolicy;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	

}
