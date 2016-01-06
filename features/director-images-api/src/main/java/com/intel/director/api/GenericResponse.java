package com.intel.director.api;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
public class GenericResponse {
	
	public String error;
	
	@JsonInclude(Include.NON_NULL)
	public String status; //Needs to be removed, but some api still using it
	@JsonInclude(Include.NON_NULL)
	public String details; //Needs to be removed, but some api still using it
	private boolean deleted;
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}


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
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
}
