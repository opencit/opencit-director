package com.intel.director.api;

public class SettingsKMSObject {
	private String KMServerIP;
	private String port;
	private String username;
	private String password;
	private String rsakey;
	

	@Override
	public String toString() {
		return "{\"kMServerIP\" : "+ getKMServerIP() + ", \"port\" : " + getPort() + ", \"username\" : " + getUsername() + ", \"password\" : " + getPassword() + ", \"rsakey\" : " + getRsakey() + "}";
	}

	public String getKMServerIP() {
		return KMServerIP;
	}

	public void setKMServerIP(String kMServerIP) {
		this.KMServerIP = kMServerIP;
	}

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

	public String getRsakey() {
		return rsakey;
	}

	public void setRsakey(String rsakey) {
		this.rsakey = rsakey;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}
