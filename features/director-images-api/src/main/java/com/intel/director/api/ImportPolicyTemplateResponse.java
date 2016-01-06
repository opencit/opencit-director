package com.intel.director.api;

public class ImportPolicyTemplateResponse {

	public String status;
	public String details;
	public String trust_policy;
	public String error;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}

	public String trust_policy;
	public String error;
	
	public String getTrust_policy() {
		return trust_policy;
	}
	public void setTrust_policy(String trust_policy) {
		this.trust_policy = trust_policy;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	
}
