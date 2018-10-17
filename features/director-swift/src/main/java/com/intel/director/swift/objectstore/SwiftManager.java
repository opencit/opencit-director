package com.intel.director.swift.objectstore;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.intel.director.store.StoreManager;
import com.intel.director.store.exception.StoreException;
import com.intel.director.swift.api.SwiftObject;
import com.intel.director.swift.rs.SwiftException;

/**
*
* @author Aakash
*/
public interface SwiftManager extends StoreManager {

	public int createContainer(String containerName) throws StoreException;

	public String createOrReplaceObject(File file,
			Map<String, String> objectProperties, String containerName,
			String objectName) throws StoreException;

	public String createOrReplaceObjectMetadata(
			Map<String, String> objectProperties, String containerName,
			String objectName) throws StoreException;

	public List<SwiftObject> getObjectsList(String containerName) throws StoreException;

	public Map<String, String> downloadObjectToFile(String containerName,
			String objectName, String writeToFilePath) throws StoreException;

	public Map<String, String> getObjectMetadata(String containerName,
			String objectName) throws StoreException, SwiftException;

	public int deleteObject(String containerName, String objectName)
			throws StoreException;

}
