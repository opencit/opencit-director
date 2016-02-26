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
				|| configuration.get(Constants.SWIFT_ACCOUNT_NAME) == null
				|| configuration.get(Constants.SWIFT_ACCOUNT_USERNAME) == null
				|| configuration.get(Constants.SWIFT_ACCOUNT_USER_PASSWORD) == null) {
			throw new SwiftException("No configuration provided for swift");
		}
		///String swiftIp = (String) configuration.get(Constants.SWIFT_IP);
		String swiftAPIEndpoint = (String) configuration
				.get(Constants.SWIFT_API_ENDPOINT);
		String accountName = (String) configuration
				.get(Constants.SWIFT_ACCOUNT_NAME);
		String accountUsername = (String) configuration
				.get(Constants.SWIFT_ACCOUNT_USERNAME);
		String accountUserPassword = (String) configuration
				.get(Constants.SWIFT_ACCOUNT_USER_PASSWORD);

		return new SwiftRsClient(swiftAPIEndpoint, accountName,
				accountUsername, accountUserPassword);

	}

}
