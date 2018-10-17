package com.intel.director.api;

public class ImageActionTask {
	private String status, task_name, location, uri,
			executionDetails, message, storeId;

	
	
	public ImageActionTask(String status, String storeId, String task_name) {
		super();
		this.status = status;
		this.task_name = task_name;
		this.storeId = storeId;
	}
	
	public ImageActionTask(String status,  String task_name) {
		super();
		this.status = status;
		this.task_name = task_name;
	}
	
	public ImageActionTask(){
		
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}



	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
		return "ImageActionActions [status=" + status + ", storeId="
				+ storeId + ", task_name=" + task_name + ", location="
				+ location + ", uri=" + uri + ", executionDetails="
				+ executionDetails + "]";
	}

}
