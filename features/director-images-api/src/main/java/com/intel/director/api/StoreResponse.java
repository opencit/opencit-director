package com.intel.director.api;

public interface StoreResponse {
	void setStatus(String status);
	String getStatus();
	String getUri();
	String getId();
	void setId(String id);
}
