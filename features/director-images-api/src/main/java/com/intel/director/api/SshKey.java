package com.intel.director.api;

public class SshKey {
	String id;
	String sshKey;

	public SshKey(String id, String sshKey) {
		super();
		this.id = id;
		this.sshKey = sshKey;
	}

	public SshKey() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSshKey() {
		return sshKey;
	}

	public void setSshKey(String sshKey) {
		this.sshKey = sshKey;
	}

}
