package com.intel.director.api.ui;

public class SshSettingFilter {
	protected String ipAddress;
	protected String username;
	protected String sshPassword;
	protected String name;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return sshPassword;
	}

	public void setPassword(String ssh_password_id) {
		this.sshPassword = ssh_password_id;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
}
