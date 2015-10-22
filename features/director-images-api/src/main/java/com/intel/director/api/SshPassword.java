package com.intel.director.api;

public class SshPassword {
	String id;
	String key;

	public SshPassword(String id, String key) {
		super();
		this.id = id;
		this.key = key;
	}

	public SshPassword() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
