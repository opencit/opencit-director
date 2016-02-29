package com.intel.director.api;

import java.io.File;
import java.util.Map;

public class SwiftObjectResponse implements StoreResponse {

	File writtenFile = null;
	Map<String, String> metadataMap;
	String swiftUri;
	String objectName;
	String status;

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;		
	}

	public Map<String, String> getMetadataMap() {
		return metadataMap;
	}

	public void setMetadataMap(Map<String, String> metadataMap) {
		this.metadataMap = metadataMap;
	}

	public File getWrittenFile() {
		return writtenFile;
	}

	public void setWrittenFile(File writtenFile) {
		this.writtenFile = writtenFile;
	}

	public String getSwiftUri() {
		return swiftUri;
	}

	public void setSwiftUri(String swiftUri) {
		this.swiftUri = swiftUri;		
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public String getUri() {
		return swiftUri;
	}

	@Override
	public String getId() {
		return objectName;
	}

	@Override
	public void setId(String id) {
		this.objectName = id;
	}

}
