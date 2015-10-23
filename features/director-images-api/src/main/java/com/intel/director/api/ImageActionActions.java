package com.intel.director.api;

public class ImageActionActions {
	private String status,storename,task_name,location,uri,executionDetails;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStorename() {
		return storename;
	}

	public void setStorename(String storename) {
		this.storename = storename;
	}

	public String getTask_name() {
		return task_name;
	}

	public void setTask_name(String task_name) {
		this.task_name = task_name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getExecutionDetails() {
		return executionDetails;
	}

	public void setExecutionDetails(String executionDetails) {
		this.executionDetails = executionDetails;
	}

	@Override
	public String toString() {
		return "ImageActionActions [status=" + status + ", storename="
				+ storename + ", task_name=" + task_name + ", location="
				+ location + ", uri=" + uri + ", executionDetails="
				+ executionDetails + "]";
	}
	
	
	
}
