package com.intel.director.api;

import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ImageStoreTransferObject extends GenericResponse {

	public String id;
	public String name;
	
	public String[] artifact_types;
	
	
	public String connector;
	
	public boolean deleted;
	
	public String deploymentFormat;
	
	public Boolean isValid = null;
	
	public Collection<ImageStoreDetailsTransferObject> image_store_details;
	

    
	public Collection<ImageStoreDetailsTransferObject> getImage_store_details() {
		return image_store_details;
	}

	public void setImage_store_details(
			Collection<ImageStoreDetailsTransferObject> image_store_details) {
		this.image_store_details = image_store_details;
	}

	public String[] getArtifact_types() {
		return artifact_types;
	}

	public void setArtifact_types(String[] artifact_types) {
		this.artifact_types = artifact_types;
	}

	

	public String getDeploymentFormat() {
		return deploymentFormat;
	}

	public void setDeploymentFormat(String deploymentFormat) {
		this.deploymentFormat = deploymentFormat;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConnector() {
		return connector;
	}

	public void setConnector(String connector) {
		this.connector = connector;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	

	public Boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	@Override
	public String toString() {
		return "ImageStoreTransferObject [id=" + id + ", name=" + name
				+ ", artifact_types=" + Arrays.toString(artifact_types)
				+ ", connector=" + connector + ", deleted=" + deleted
				+ ", deploymentFormat=" + deploymentFormat
				+ ", image_store_details=" + image_store_details + "]";
	}
	


}
