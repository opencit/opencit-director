package com.intel.director.api;


public class SshSettingRequest extends AuditFields {
	String ip_address;
	String username;
	String password;
	String name;
	String id;
	String key;
	String image_id;
	String policy_name;

	public String getPolicy_name() {
		return policy_name;
	}

	public void setPolicy_name(String policy_name) {
		this.policy_name = policy_name;
	}

	public SshSettingRequest() {

	}

	public String getIpAddress() {
		return ip_address;
	}

	public void setIpAddress(String ipAddress) {
		this.ip_address = ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public String getImage_id() {
		return image_id;
	}

	public void setImage_id(String image_id) {
		this.image_id = image_id;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
