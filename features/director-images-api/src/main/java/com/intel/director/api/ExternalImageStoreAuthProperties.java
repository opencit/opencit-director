package com.intel.director.api;

public class ExternalImageStoreAuthProperties {
	private String authUrl;
	private String apiUrl;
	private String userName;
	private String password;
	private String tenantOrDomain;
	private String version;

	public ExternalImageStoreAuthProperties() {
	}

	/**
	 * @param userName
	 * @param password
	 * @param tenantOrDomain
	 */
	public ExternalImageStoreAuthProperties(String userName, String password, String tenantOrDomain) {
		super();
		this.userName = userName;
		this.password = password;
		this.tenantOrDomain = tenantOrDomain;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTenantOrDomain() {
		return tenantOrDomain;
	}

	public void setTenantOrDomain(String tenantOrDomain) {
		this.tenantOrDomain = tenantOrDomain;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAuthUrl() {
		return authUrl;
	}

	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

}
