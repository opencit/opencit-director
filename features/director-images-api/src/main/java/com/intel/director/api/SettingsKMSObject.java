package com.intel.director.api;

public class SettingsKMSObject {
	
	
	public String kms_endpoint_url;
	public String kms_login_basic_username;
	public String kms_login_basic_password;
	public String kms_tls_policy_certificate_sha1;
	public String getKms_endpoint_url() {
		return kms_endpoint_url;
	}
	public void setKms_endpoint_url(String kms_endpoint_url) {
		this.kms_endpoint_url = kms_endpoint_url;
	}
	public String getKms_login_basic_username() {
		return kms_login_basic_username;
	}
	public void setKms_login_basic_username(String kms_login_basic_username) {
		this.kms_login_basic_username = kms_login_basic_username;
	}
	public String getKms_login_basic_password() {
		return kms_login_basic_password;
	}
	public void setKms_login_basic_password(String kms_login_basic_password) {
		this.kms_login_basic_password = kms_login_basic_password;
	}
	public String getKms_tls_policy_certificate_sha1() {
		return kms_tls_policy_certificate_sha1;
	}
	public void setKms_tls_policy_certificate_sha1(
			String kms_tls_policy_certificate_sha1) {
		this.kms_tls_policy_certificate_sha1 = kms_tls_policy_certificate_sha1;
	}
	@Override
	public String toString() {
		return "{\"kms_endpoint_url\":\"" + kms_endpoint_url
				+ "\", \"kms_login_basic_username\" : \"" + kms_login_basic_username
				+ "\",\"kms_login_basic_password\":\"" + kms_login_basic_password
				+ "\",\"kms_tls_policy_certificate_sha1\":\""
				+ kms_tls_policy_certificate_sha1 + "\"}";
	}
	


}
