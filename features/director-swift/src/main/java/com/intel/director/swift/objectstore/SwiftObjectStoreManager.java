/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.swift.objectstore;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.intel.director.api.GenericResponse;
import com.intel.director.api.StoreResponse;
import com.intel.director.api.SwiftObjectResponse;
import com.intel.director.store.exception.StoreException;
import com.intel.director.store.impl.StoreManagerImpl;
import com.intel.director.swift.api.SwiftObject;
import com.intel.director.swift.constants.Constants;
import com.intel.director.swift.rs.SwiftException;
import com.intel.director.swift.rs.SwiftRsClient;
import com.intel.director.swift.rs.SwiftRsClientBuilder;

/**
 * 
 * @author Aakash
 */
public class SwiftObjectStoreManager extends StoreManagerImpl implements
		SwiftManager {

	private SwiftRsClient swiftRsClient;
	String url;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(SwiftObjectStoreManager.class);

	public int createContainer(String containerName) throws StoreException {
		try {
			return swiftRsClient.createContainer(containerName);
		} catch (SwiftException e) {
			throw new StoreException(e);
		}
	}

	public String createOrReplaceObject(File file,
			Map<String, String> objectProperties, String containerName,
			String objectName) throws StoreException {
		try {
			return swiftRsClient.createOrReplaceObject(file, objectProperties,
					containerName, objectName);
		} catch (SwiftException e) {
			throw new StoreException(e);
		}
	}

	public String createOrReplaceObjectMetadata(
			Map<String, String> objectProperties, String containerName,
			String objectName) throws StoreException {
		try {
			return swiftRsClient.createOrReplaceObjectMetadata(objectProperties,
					containerName, objectName);
		} catch (SwiftException e) {
			// TODO Auto-generated catch block
			throw new StoreException(e);
		}

	}

	public List<SwiftObject> getObjectsList(String containerName) throws StoreException {
		try {
			return swiftRsClient.getObjectsList(containerName);
		} catch (SwiftException e) {
			throw new StoreException(e);
		}
	}

	public Map<String, String> downloadObjectToFile(String containerName,
			String objectName, String writeToFilePath) throws StoreException {
		try {
			return swiftRsClient.downloadObjectToFile(containerName, objectName,
					writeToFilePath);
		} catch (SwiftException e) {
			throw new StoreException(e);
		}
	}

	public Map<String, String> getObjectMetadata(String containerName,
			String objectName) throws SwiftException {
		return swiftRsClient.getObjectMetadata(containerName, objectName);
	}

	public int deleteObject(String containerName, String objectName)
			throws StoreException {
		try{
		return swiftRsClient.deleteObject(containerName, objectName);
		}catch(SwiftException e){
			throw new StoreException(e);
		}
	}

	@Override
	public String upload() throws StoreException {
		log.info("Before uploading data to Swift");
		String containerName = (String) objectProperties
				.get(Constants.SWIFT_CONTAINER_NAME);
		String objectName = (String) objectProperties
				.get(Constants.SWIFT_OBJECT_NAME);
		try {
			swiftRsClient.createContainer(containerName);
		} catch (SwiftException e) {
			throw new StoreException(e);
		}

		Map<String, String> metadata = new HashMap<String, String>();
		for (Map.Entry<String, Object> entry : objectProperties.entrySet()) {
			if (!Constants.SWIFT_PATH.equals(entry.getKey())
					&& !Constants.SWIFT_CONTAINER_NAME.equals(entry.getKey())
					&& !Constants.SWIFT_OBJECT_NAME.equals(entry.getKey())) {
				if(entry.getValue() instanceof String){
				metadata.put(entry.getKey(), (String) entry.getValue());
				}
			}
		}

		log.info("image upload to glance complete");
		try {
			return swiftRsClient.createOrReplaceObject((File) objectProperties
					.get(Constants.UPLOAD_TO_IMAGE_STORE_FILE), metadata,
					containerName, objectName);
		} catch (SwiftException e) {
			throw new StoreException(e);
		}

	}

	@Override
	public <T extends StoreResponse> T fetchDetails() throws StoreException {
		log.info("Fetch details from swift");
		SwiftObjectResponse swiftObjectResponse = new SwiftObjectResponse();
		String containerName = (String) objectProperties
				.get(Constants.SWIFT_CONTAINER_NAME);
		String objectName = (String) objectProperties
				.get(Constants.SWIFT_OBJECT_NAME);
		String writeToFilePath = (String) objectProperties
				.get(Constants.SWIFT_DOWNLOAD_FILE_PATH);
		try {
			swiftObjectResponse.setMetadataMap(swiftRsClient.downloadObjectToFile(
					containerName, objectName, writeToFilePath));
		} catch (SwiftException e) {
			throw new StoreException(e);
		}
		if (writeToFilePath != null) {
			File f = (new File(writeToFilePath));
			if (f.exists() && !f.isDirectory()) {
				swiftObjectResponse.setWrittenFile(f);
			}

		}
		swiftObjectResponse.setObjectName(objectName);
		String uri = swiftRsClient.storageUrl + "/" + containerName + "/" + objectName;
		swiftObjectResponse.setSwiftUri(uri);
		swiftObjectResponse.setStatus(com.intel.director.common.Constants.COMPLETE);
		log.info("Fetch complete from swift");
		return (T) swiftObjectResponse;
	}

	@Override
	public void build(Map<String, String> configuration) throws StoreException {
		super.build(configuration);
		try {
			swiftRsClient = SwiftRsClientBuilder.build(configuration);
		} catch (SwiftException e) {
			throw new StoreException(e.getMessage(),e);
		}
		log.info("RS client for swift ready for use");
	}

	@Override
	public void update() throws StoreException {
		upload();
		
	}

	@Override
	public void delete(URL url) throws StoreException {
		try {
			swiftRsClient.deleteObject(url);
		} catch (SwiftException e) {
			throw new StoreException(e);
		}
	}

	@Override
	public <T extends StoreResponse> List<T> fetchAllImages() {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public GenericResponse validate() throws StoreException {
		GenericResponse response = new GenericResponse();
		try {
			swiftRsClient.getContainersList();
		} catch (SwiftException e) {
			response.setError("Invalid API Endpoint");
		}
		return response;
	}





}
