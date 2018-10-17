package com.intel.director.api;

import com.intel.director.api.ui.ImageInfo;

public class ImageInfoResponse extends ImageInfo{

	public String ip_address;
	public String username;
	public String error;
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
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}

	
}
