/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.util.List;

import javax.xml.bind.JAXBException;

import com.intel.director.api.ImageActionTask;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.PolicyUploadTransferObject;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadFilter;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.PolicyUploadFilter;
import com.intel.director.common.Constants;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.store.StoreManager;
import com.intel.director.store.StoreManagerFactory;
import com.intel.director.store.exception.StoreException;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy;

/**
 * Task to upload the policy to image store
 * 
 * @author GS-0681
 */
public class UpdateMetadataTask extends GenericUploadTask {
	public static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UpdateMetadataTask.class);

	public UpdateMetadataTask() throws DirectorException {
		super();
	}

	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPDATE_METADATA;
	}

	/**
	 * Entry method for uploading policy
	 */
	@Override
	public boolean run() {
		boolean runFlag = false;
		if (previousTasksCompleted(taskAction.getTask_name())) {
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				runFlag = runUpdateMetadataTask();
			}
		}
		return runFlag;
	}

	/**
	 * Actual implementation of policy upload task
	 */
	public boolean runUpdateMetadataTask() {
		boolean runFlag = true;

		try {
			trustPolicy = persistService.fetchPolicyById(imageInfo.getTrust_policy_id());
		} catch (DbException e1) {
			log.error("Unable to fetch trustpolicy for tp id::" + imageInfo.getTrust_policy_id(), e1);
			updateImageActionState(Constants.ERROR, "Error in Updating metadata in imagestore");
			return false;
		}


		TrustPolicy policy;
		try {
			policy = TdaasUtil.getPolicy(trustPolicy.getTrust_policy());
		} catch (JAXBException e2) {
			log.error("Error converting policy xml to object ", e2);
			return false;
		}
		String glanceId = policy.getImage().getImageId();

		ImageStoreUploadTransferObject imageStoreTranserObject = null;
		ImageStoreUploadFilter imgUpFilter = new ImageStoreUploadFilter();
		imgUpFilter.setImage_id(imageInfo.getId());
		List<ImageStoreUploadTransferObject> fetchImageUploads;
		
		ImageStoreUploadOrderBy imageStoreUploadOrderBy = new ImageStoreUploadOrderBy();
		imageStoreUploadOrderBy.setOrderBy(OrderByEnum.ASC);
		imageStoreUploadOrderBy.setImgStoreUploadFields(ImageStoreUploadFields.DATE);
		try {
			fetchImageUploads = persistService.fetchImageUploads(imgUpFilter, imageStoreUploadOrderBy);
			if ((fetchImageUploads != null && fetchImageUploads.size() > 0)) {
				imageStoreTranserObject = fetchImageUploads.get(fetchImageUploads.size() - 1);
				if(imageStoreTranserObject!=null){
					log.info("Last uploaded image to store : {},  glance id : {}", imageStoreTranserObject.getStoreId(),
							imageStoreTranserObject.getStoreArtifactId());
				}
			
				log.info("Glance id from policy {}", glanceId);
			} 
		} catch (DbException e) {
			log.error("Error fetching image uploads {}", e);
			updateImageActionState(Constants.ERROR, "Error in update metadata task");
			return false;
		}
		if(imageStoreTranserObject==null){
			updateImageActionState(Constants.ERROR, "Error in Updating metadata in imagestore");
			return false;
		}
		
		String storeId = imageStoreTranserObject.getStoreId();
		if (!imageStoreTranserObject.getStoreArtifactId().equals(glanceId)) {
			glanceId = imageStoreTranserObject.getStoreArtifactId();
		}
		String updatedArtifactName = imageStoreTranserObject.getStoreArtifactName();
		PolicyUploadFilter policyUploadFilter = new PolicyUploadFilter();
		policyUploadFilter.setTrust_policy_id(trustPolicy.getId());
			
		List<PolicyUploadTransferObject> policyUploads;
		try {
			policyUploads = persistService.fetchPolicyUploads(policyUploadFilter, null);
		} catch (DbException e1) {
			log.error("Error fetchPolicyUploads", e1);
			updateImageActionState(Constants.ERROR, "Error in Updating metadata in imagestore");
			return false;
		}
		
		if(policyUploads == null){
			log.error("No policy upload entry found");
			updateImageActionState(Constants.ERROR,
					"Error in Updating metadata in imagestore because No policy upload entry found");
			return false;
		}
		
		PolicyUploadTransferObject policyUploadTransferObject = policyUploads.get(policyUploads.size() - 1);
		String policyUri = policyUploadTransferObject.getPolicy_uri();
		String previousStoreId = policyUploadTransferObject.getStoreId();
		ImageStoreTransferObject previousUploadImageStoreDTO = null;
		ImageStoreTransferObject currentImageStoreDTO = null;
		try {
			currentImageStoreDTO = persistService.fetchImageStorebyId(storeId);
			previousUploadImageStoreDTO = persistService.fetchImageStorebyId(previousStoreId);
			
		} catch (DbException e) {
			log.error("No store exists for id {}", previousStoreId);
		}
		
		if(previousUploadImageStoreDTO == null){
			log.error("No previous upload Image store exists");
			return false;
		}

		String updateMessage = "Policy attached to Image " + updatedArtifactName + " in store "
				+ currentImageStoreDTO.getName();
		String connectorName = previousUploadImageStoreDTO.getConnector();
		String trustPolicyLocationTag = connectorName.toLowerCase() + ":" + policyUri;
		customProperties.put(Constants.MTWILSON_TRUST_POLICY_LOCATION, trustPolicyLocationTag);
		customProperties.put(Constants.GLANCE_ID, glanceId);
		
		StoreManager imageStoreManager;
		try {
			imageStoreManager = StoreManagerFactory.getStoreManager(taskAction.getStoreId());
		} catch (StoreException e1) {
			log.error("Error in getting image store manager", e1);
			updateImageActionState(Constants.ERROR, "Error in Updating metadata in imagestore");
			return false;
		}
		ImageActionTask imageActionTask = getImageActionTaskFromArray();

		try {
			imageStoreManager.addCustomProperties(customProperties);
		} catch (StoreException e1) {
			log.error("Error in addCustomProperties for image store manager", e1);
			updateImageActionState(Constants.ERROR, "Error in Updating metadata in imagestore");
			return false;
		}

		if (imageActionTask != null && imageActionTask.getUri() == null) {

			try {
				imageStoreManager.update();
			} catch (StoreException e) {
				log.error("Error updating imageuploads", e);
				updateImageActionState(Constants.ERROR, "Error in Updating metadata task");
				return false;
			}

		}

		try {
			imageStoreTranserObject.setPolicyUploadId(trustPolicy.getId());
			persistService.updateImageUpload(imageStoreTranserObject);
		} catch (DbException e) {
			log.error("Error fetching image uploads {}", e);
			runFlag = false;
			updateImageActionState(Constants.ERROR, "Error in update metadata task");

		}

		if (runFlag) {
			updateImageActionState(Constants.COMPLETE, updateMessage);
		}
		return runFlag;

	}

}
