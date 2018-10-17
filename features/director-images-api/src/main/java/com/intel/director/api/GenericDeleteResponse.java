package com.intel.director.api;

public class GenericDeleteResponse extends GenericResponse {
	private boolean deleted;

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	
}
