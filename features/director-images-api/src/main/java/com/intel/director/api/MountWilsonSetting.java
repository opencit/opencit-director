package com.intel.director.api;

public class MountWilsonSetting {
	public String mtwilson_api_url;
	public String mtwilson_api_username;
	public String mtwilson_api_password;
	public String mtwilson_api_tls_policy_certificate_sha1;
	public String mtwilson_server;
	public String mtwilson_server_port;
	public String mtwilson_username;
	public String mtwilson_password;
	
	@Override
	public String toString() {
		return "{\"mtwilson_api_url\":\"" + mtwilson_api_url
				+ "\",\"mtwilson_api_username\":\"" + mtwilson_api_username
				+ "\",\"mtwilson_api_password\":\"" + mtwilson_api_password
				+ "\",\"mtwilson_api_tls_policy_certificate_sha1\":\"" + mtwilson_api_tls_policy_certificate_sha1
				+ "\",\"mtwilson_server\":\"" + mtwilson_server
				+ "\",\"mtwilson_server_port\":\"" + mtwilson_server_port
				+ "\",\"mtwilson_username\":\"" + mtwilson_username
				+ "\",\"mtwilson_password\":\"" + mtwilson_password + "\"}";
	}
	public String getMtwilson_api_url() {
		return mtwilson_api_url;
	}
	public void setMtwilson_api_url(String mtwilson_api_url) {
		this.mtwilson_api_url = mtwilson_api_url;
	}
	public String getMtwilson_api_username() {
		return mtwilson_api_username;
	}
	public void setMtwilson_api_username(String mtwilson_api_username) {
		this.mtwilson_api_username = mtwilson_api_username;
	}
	public String getMtwilson_api_password() {
		return mtwilson_api_password;
	}
	public void setMtwilson_api_password(String mtwilson_api_password) {
		this.mtwilson_api_password = mtwilson_api_password;
	}
	public String getMtwilson_api_tls_policy_certificate_sha1() {
		return mtwilson_api_tls_policy_certificate_sha1;
	}
	public void setMtwilson_api_tls_policy_certificate_sha1(
			String mtwilson_api_tls_policy_certificate_sha1) {
		this.mtwilson_api_tls_policy_certificate_sha1 = mtwilson_api_tls_policy_certificate_sha1;
	}
	public String getMtwilson_server() {
		return mtwilson_server;
	}
	public void setMtwilson_server(String mtwilson_server) {
		this.mtwilson_server = mtwilson_server;
	}
	public String getMtwilson_server_port() {
		return mtwilson_server_port;
	}
	public void setMtwilson_server_port(String mtwilson_server_port) {
		this.mtwilson_server_port = mtwilson_server_port;
	}
	public String getMtwilson_username() {
		return mtwilson_username;
	}
	public void setMtwilson_username(String mtwilson_username) {
		this.mtwilson_username = mtwilson_username;
	}
	public String getMtwilson_password() {
		return mtwilson_password;
	}
	public void setMtwilson_password(String mtwilson_password) {
		this.mtwilson_password = mtwilson_password;
	}
	



}
