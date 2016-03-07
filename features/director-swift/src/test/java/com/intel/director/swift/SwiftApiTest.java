/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.director.swift;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.intel.director.swift.constants.Constants;
import com.intel.director.swift.objectstore.SwiftManager;
import com.intel.director.swift.objectstore.SwiftObjectStoreManager;

public class SwiftApiTest {

	Map<String, String> apacheConfig = new HashMap<String,String>();

	@Before
	public void setup() throws Exception {
		//apacheConfig = new HashMap<String,String>();
		apacheConfig.put(Constants.SWIFT_API_ENDPOINT,"http://10.35.35.117:8080");
	/*	//apacheConfig.put(Constants.SWIFT_ACCOUNT_NAME, "test");
	//	apacheConfig.put(Constants.SWIFT_ACCOUNT_USERNAME, "tester");
		//apacheConfig.put(Constants.SWIFT_ACCOUNT_USER_PASSWORD,
				"testing");*/

	}

	@Test
	public void swiftApiTest() throws Exception {
		//apacheConfig.put(Constants.SWIFT_IP, "10.35.35.117");
		apacheConfig.put(Constants.SWIFT_API_ENDPOINT,"http://10.35.35.35:8080");
		apacheConfig.put(Constants.SWIFT_AUTH_ENDPOINT,"http://10.35.35.35:5000");
		apacheConfig.put(Constants.SWIFT_TENANT_NAME, "admin");
		apacheConfig.put(Constants.SWIFT_ACCOUNT_USERNAME, "admin");
		apacheConfig.put(Constants.SWIFT_ACCOUNT_USER_PASSWORD,
				"intelmh");
		apacheConfig.put(Constants.SWIFT_KEYSTONE_SERVICE_NAME,
				"swift");

		SwiftManager swiftImageStoreManager = new SwiftObjectStoreManager();
		swiftImageStoreManager.build(apacheConfig);
		swiftImageStoreManager.createContainer("MyContainer");
		File file = new File("C:/Temp/sss.txt");
		Map<String, String> objectProperties = new HashMap<String, String>();
		objectProperties.put("director_image_id", "12345");
		objectProperties.put("director_image_name", "my_first_image");
		swiftImageStoreManager.createOrReplaceObject(file, objectProperties,
				"MyContainer", "policy1");
		Map<String, String> objectProperties2 = new HashMap<String, String>();
		File file2 = new File("C:/MysteryHill/DirectorAll/Docs/vm_launch.txt");
		objectProperties2.put("director_image_id", "98765");
		objectProperties2.put("director_image_name", "my_second_image");
		swiftImageStoreManager.createOrReplaceObject(file2, objectProperties2,
				"MyContainer", "policy2");
		Map<String, String> metadata = swiftImageStoreManager
				.downloadObjectToFile("MyContainer", "policy2",
						"C:/MysteryHill/DirectorAll/Docs/dowload_policy2.txt");
		System.out.println("policy2 metadata::" + metadata);
		System.out.println("objects inside container  ::"
				+ swiftImageStoreManager.getObjectsList("MyContainer"));
		swiftImageStoreManager.deleteObject("MyContainer", "policy1");
		
	
	}
	
	
/*	@Test
	public void swiftApiTest1() throws Exception {
		System.out.println("######################################################################################################################Inside Test1");
		//apacheConfig.put(Constants.SWIFT_IP, "10.35.35.117");
		apacheConfig.put(Constants.SWIFT_API_ENDPOINT,"http://10.35.35.117:8080");
		apacheConfig.put(Constants.SWIFT_ACCOUNT_NAME, "admin");
		apacheConfig.put(Constants.SWIFT_ACCOUNT_USERNAME, "admin");
		apacheConfig.put(Constants.SWIFT_ACCOUNT_USER_PASSWORD,
				"admin");

		SwiftManager swiftImageStoreManager = new SwiftObjectStoreManager();
		swiftImageStoreManager.build(apacheConfig);
		swiftImageStoreManager.createContainer("MyContainer");
		File file = new File("C:/Temp/sss.txt");
		Map<String, String> objectProperties = new HashMap<String, String>();
		objectProperties.put("director_image_id", "12345");
		objectProperties.put("director_image_name", "my_first_image");
		swiftImageStoreManager.createOrReplaceObject(file, objectProperties,
				"MyContainer", "policy1");
		Map<String, String> objectProperties2 = new HashMap<String, String>();
		File file2 = new File("C:/MysteryHill/DirectorAll/Docs/vm_launch.txt");
		objectProperties2.put("director_image_id", "98765");
		objectProperties2.put("director_image_name", "my_second_image");
		swiftImageStoreManager.createOrReplaceObject(file2, objectProperties2,
				"MyContainer", "policy2");
		Map<String, String> metadata = swiftImageStoreManager
				.downloadObjectToFile("MyContainer", "policy2",
						"C:/MysteryHill/DirectorAll/Docs/dowload_policy2.txt");
		System.out.println("policy2 metadata::" + metadata);
		System.out.println("objects inside container  ::"
				+ swiftImageStoreManager.getObjectsList("MyContainer"));
		swiftImageStoreManager.deleteObject("MyContainer", "policy1");
	}*/

/*	@Test
	public void storeManageTest() throws Exception {

		StoreManager storeManager = new SwiftObjectStoreManager();
		Map<String, String> objectProperties = new HashMap<String, String>();
		objectProperties.put(Constants.SWIFT_CONTAINER_NAME, "MyContainer1");
		objectProperties.put(Constants.SWIFT_OBJECT_NAME, "policy3");
		objectProperties.put("director_image_id", "33333");
		objectProperties.put("director_image_name", "my_third_image");
		File file3 = new File("C:/MysteryHill/DirectorAll/Docs/vm_launch.txt");
		System.out.println("Upload operation success response::"
				+ storeManager.upload());
		Map<String, String> objectProperties3 = new HashMap<String, String>();
		objectProperties3.put(Constants.SWIFT_CONTAINER_NAME, "MyContainer1");
		objectProperties3.put(Constants.SWIFT_OBJECT_NAME, "policy3");
		objectProperties3.put(Constants.SWIFT_DOWNLOAD_FILE_PATH, "C:/MysteryHill/DirectorAll/Docs/my_download2.txt");
		SwiftObjectResponse swiftResponse = (SwiftObjectResponse) storeManager
				.fetchDetails();
		System.out.println("fetch details method response for policy3 object::"
				+ swiftResponse);

	}*/
}
