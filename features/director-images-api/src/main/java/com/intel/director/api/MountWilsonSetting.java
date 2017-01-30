package com.intel.director.api;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class MountWilsonSetting {
	public String mtwilson_api_url;
	public String mtwilson_api_username;
	public String mtwilson_api_password;
	public String mtwilson_api_tls_policy_certificate_sha256;
//	public String mtwilson_server;
//	public String mtwilson_server_port;
//	public String mtwilson_username;
//	public String mtwilson_password;

	@Override
	public String toString() {
		return "{\"mtwilson_api_url\":\"" + mtwilson_api_url + "\",\"mtwilson_api_username\":\"" + mtwilson_api_username
				+ "\",\"mtwilson_api_password\":\"" + mtwilson_api_password
				+ "\",\"mtwilson_api_tls_policy_certificate_sha256\":\"" + mtwilson_api_tls_policy_certificate_sha256 + "\"}";
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

	public String getMtwilson_api_tls_policy_certificate_sha256() {
		return mtwilson_api_tls_policy_certificate_sha256;
	}

	public void setMtwilson_api_tls_policy_certificate_sha56(String mtwilson_api_tls_policy_certificate_sha56) {
		this.mtwilson_api_tls_policy_certificate_sha256 = mtwilson_api_tls_policy_certificate_sha56;
	}


	public String validate() {
		List<String> errors = new ArrayList<>();

		for (Field f : this.getClass().getFields()) {
			f.setAccessible(true);
			try {
				String value = (String) f.get(this);
				if (StringUtils.isBlank(value)) {
					errors.add(f.getName() + " is invalid");
				}
			} catch (IllegalArgumentException e) {
				errors.add("Invalid field");
			} catch (IllegalAccessException e) {
				errors.add("Invalid field");
			}
		}
		return StringUtils.join(errors, ",");
	}

}
