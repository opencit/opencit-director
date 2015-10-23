package com.intel.director.api;

public class SshSetting{
	String ipAddress;
	String username;
	String password;
	String name;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password =  password;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
		
	@Override
	public String toString() {
		return "{username=" + username + ",password=" + password + ",name=" + name + ",ipAddress=" + ipAddress + "}";
	}
	
}
