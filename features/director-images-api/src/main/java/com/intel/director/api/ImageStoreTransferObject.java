package com.intel.director.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.constants.Constants;

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
	
	public Collection<ConnectorCompositeItem> connectorCompositeItemsList;

	public Collection<ImageStoreDetailsTransferObject> getImage_store_details() {
		if (this.image_store_details == null) {
			return null;
		}
		List<ImageStoreDetailsTransferObject> listImageStoreDetailsTransferObject = new ArrayList<ImageStoreDetailsTransferObject>(
				image_store_details);

		ConnectorProperties connectorByName = ConnectorProperties.getConnectorByName(connector);
		if (connectorByName != null) {
			for (ImageStoreDetailsTransferObject imageStoreDetailsTransferObject : listImageStoreDetailsTransferObject) {
				imageStoreDetailsTransferObject.seqNo = connectorByName
						.fetchSeqNoOfProperty(imageStoreDetailsTransferObject.key);
			}
		}

		Collections.sort(listImageStoreDetailsTransferObject);
		return listImageStoreDetailsTransferObject;
	}

	public void setImage_store_details(Collection<ImageStoreDetailsTransferObject> image_store_details) {
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
		return "ImageStoreTransferObject [id=" + id + ", name=" + name + ", artifact_types="
				+ Arrays.toString(artifact_types) + ", connector=" + connector + ", deleted=" + deleted
				+ ", deploymentFormat=" + deploymentFormat + ", image_store_details=" + image_store_details + "]";
	}

	public ImageStoreDetailsTransferObject fetchPasswordConfiguration() {
		ImageStoreDetailsTransferObject detailsTransferObject = null;

		Collection<ImageStoreDetailsTransferObject> imageStoreDetails = getImage_store_details();
		ConnectorProperties connectorByName = ConnectorProperties.getConnectorByName(getConnector());
		String passwordElement = null;
		if (connectorByName != null) {
			if (connectorByName.equals(ConnectorProperties.DOCKER)) {
				passwordElement = Constants.DOCKER_HUB_PASSWORD;
			} else if (connectorByName.equals(ConnectorProperties.GLANCE)) {
				passwordElement = Constants.GLANCE_IMAGE_STORE_PASSWORD;
			} /*
				 * else if (connectorByName.equals(ConnectorProperties.SWIFT)) {
				 * passwordElement = Constants.SWIFT_ACCOUNT_USER_PASSWORD; }
				 */
		}
		if (imageStoreDetails != null) {
			for (ImageStoreDetailsTransferObject imageStoreDetailsTransferObject : imageStoreDetails) {
				if (imageStoreDetailsTransferObject.getKey().equals(passwordElement)) {
					detailsTransferObject = imageStoreDetailsTransferObject;
					break;
				}
			}
		}
		return detailsTransferObject;

	}

	public Collection<ConnectorCompositeItem> getConnectorCompositeItemsList() {
		return connectorCompositeItemsList;
	}

	public void setConnectorCompositeItemsList(
			Collection<ConnectorCompositeItem> connectorCompositeItemsList) {
		this.connectorCompositeItemsList = connectorCompositeItemsList;
	}

	public String validateFields(){
		String domainName=null;
		String version="";
		
		for(ImageStoreDetailsTransferObject image_store_detail_Object: image_store_details){
			if((Constants.KEYSTONE_VERSION).equalsIgnoreCase(image_store_detail_Object.key)){
				version=image_store_detail_Object.value;
				if (!((StringUtils.isNotBlank(version)
						&& ValidationUtil.isValidWithRegex(version, Constants.VERSION_V2+"|"+Constants.VERSION_V3)))) {
					return "Invalid version specified. Allowed keystone version:"+ Constants.VERSION_V2+"|"+Constants.VERSION_V3;
				}
				
			}
			if(Constants.GLANCE_DOMAIN_NAME.endsWith(image_store_detail_Object.key)){
				domainName=image_store_detail_Object.value;
			}
			
		}
		
		if(Constants.VERSION_V3.equalsIgnoreCase(version) && StringUtils.isBlank(domainName)){
			return "Domain Name is mandatory for Mitaka";
		}
		return "";
	}
	
}
