package com.intel.director.api;

public class ValidationResponse {

	public boolean valid=false;
	
	public String error;



	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	
}
