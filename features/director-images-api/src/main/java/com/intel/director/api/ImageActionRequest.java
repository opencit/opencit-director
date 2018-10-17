package com.intel.director.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.constants.Constants;

public class ImageActionRequest {
	public String image_id;

	List<ArtifactStoreDetails> artifact_store_list ;

	public String getImage_id() {
		return image_id;
	}

	public void setImage_id(String image_id) {
		this.image_id = image_id;
	}

	public List<ArtifactStoreDetails> getArtifact_store_list() {
		return artifact_store_list;
	}

	public void setArtifact_store_list(
			List<ArtifactStoreDetails> artifact_store_list) {
		this.artifact_store_list = artifact_store_list;
	}

	public String validate() {
		List<String> errors = new ArrayList<String>();
	
		if(!ValidationUtil.isValidWithRegex(getImage_id(),RegexPatterns.UUID)){
			errors.add("Image Id is empty or not in uuid format");
		}
	
	/*	if (artifact_store_list.size() == 2) {
			if (!(artifact_store_list.get(0).getArtifact_name()
					.equals(Constants.ARTIFACT_IMAGE)
					&& artifact_store_list.get(1).getArtifact_name()
							.equals(Constants.ARTIFACT_POLICY))) {		
			
				errors.add("Invalid list of artifacts selected for upload. Image should be followed by Policy");
			}
		} else if (artifact_store_list.size() > 2) {
			errors.add("Artifacts can' t be greater than 2 ");
		} else*/ if (artifact_store_list.size() == 1) {
			if (!ValidationUtil.isValidWithRegex(artifact_store_list.get(0)
					.getArtifact_name(), Constants.ARTIFACT_IMAGE + "|"
					+ Constants.ARTIFACT_DOCKER_IMAGE + "|"
					 + Constants.ARTIFACT_TAR
					+ "|" + Constants.ARTIFACT_DOCKER_WITH_POLICY)) {
				errors.add("Invalid artifact provided. It should be "
						+ Constants.ARTIFACT_IMAGE + "|"
						+ Constants.ARTIFACT_DOCKER_IMAGE + "|"
						+ Constants.ARTIFACT_TAR + "|"
						+ Constants.ARTIFACT_DOCKER_WITH_POLICY);

			}

		}else{
			errors.add("Only single artifact supported in this release");
		}
		
		for(ArtifactStoreDetails artifactStoreDetails: artifact_store_list){
			if(!ValidationUtil.isValidWithRegex(artifactStoreDetails.getImage_store_id(),RegexPatterns.UUID)){
				errors.add("Image Store Id is empty or not in uuid format");
				break;
			} 
		}
			
		return StringUtils.join(errors, ", ");
	}
	
	
}
