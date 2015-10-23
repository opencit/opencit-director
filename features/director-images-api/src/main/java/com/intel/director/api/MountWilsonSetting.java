package com.intel.director.api;

public class MountWilsonSetting {
	String username;
	String password;
	String ipAddress;
	String port;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return "{username=" + username + ",password=" + password + ",ipAddress=" + ipAddress + ",port="+port+"}";
	}


}
