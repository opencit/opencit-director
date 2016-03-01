/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.swift.rs;

import java.util.Map;

import com.intel.director.swift.constants.Constants;

/**
 * 
 * @author Aakash
 */
public class SwiftRsClientBuilder {

	public static SwiftRsClient build(Map<String, String> configuration)
			throws SwiftException {

		if (configuration == null
				|| configuration.get(Constants.SWIFT_API_ENDPOINT) == null
				|| configuration.get(Constants.SWIFT_TENANT_NAME) == null
				|| configuration.get(Constants.SWIFT_ACCOUNT_USERNAME) == null
				|| configuration.get(Constants.SWIFT_AUTH_ENDPOINT) == null
				|| configuration.get(Constants.SWIFT_KEYSTONE_SERVICE_NAME) == null
				|| configuration.get(Constants.SWIFT_ACCOUNT_USER_PASSWORD) == null
) {
			throw new SwiftException("All configurations not provided for swift");
		}
		///String swiftIp = (String) configuration.get(Constants.SWIFT_IP);
		String swiftApiEndpoint = (String) configuration
				.get(Constants.SWIFT_API_ENDPOINT);
		String swiftAuthEndpoint = (String) configuration
				.get(Constants.SWIFT_AUTH_ENDPOINT);
		String tenantName = (String) configuration
				.get(Constants.SWIFT_TENANT_NAME);
		String accountUsername = (String) configuration
				.get(Constants.SWIFT_ACCOUNT_USERNAME);
		String accountUserPassword = (String) configuration
				.get(Constants.SWIFT_ACCOUNT_USER_PASSWORD);
		String keystoneServiceName = (String) configuration
				.get(Constants.SWIFT_KEYSTONE_SERVICE_NAME);

		return new SwiftRsClient(swiftApiEndpoint,swiftAuthEndpoint, tenantName,
				accountUsername, accountUserPassword,keystoneServiceName);

	}

}
