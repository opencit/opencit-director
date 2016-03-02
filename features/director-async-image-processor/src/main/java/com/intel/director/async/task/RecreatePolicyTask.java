/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.util.List;

import javax.xml.bind.JAXBException;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.TrustPolicyService;
import com.intel.director.service.impl.TrustPolicyServiceImpl;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;

/**
 * Task to upload the policy to image store
 * 
 * @author GS-0681
 */
public class RecreatePolicyTask extends GenericUploadTask {
	public static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadPolicyTask.class);

	public RecreatePolicyTask() throws DirectorException {
		super();
	}

	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_RECREATE_POLICY;
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
				runFlag = runRecreatePolicyTask();
			}
		}
		return runFlag;
	}

	/**
	 * Actual implementation of policy upload task
	 */
	public boolean runRecreatePolicyTask() {
		boolean runFlag = true;

		// try {
		log.info("Inside runRecreatePolicyTask for ::"
				+ imageActionObject.getImage_id());

		com.intel.mtwilson.trustpolicy.xml.TrustPolicy trustPolicyObj = null;
		try {
			trustPolicyObj = TdaasUtil.getPolicy(trustPolicy.getTrust_policy());
		} catch (JAXBException e) {
			updateImageActionState(Constants.ERROR, "Error in recreating policy");
			log.error("JAXBException , Recreate task", e);
			return false;
		}

		// / imageLocation = imageInfo.getLocation();
		// Fetch the policy and write to a location. Move to common

		ImageStoreUploadOrderBy imgOrder = new ImageStoreUploadOrderBy();
		imgOrder.setImgStoreUploadFields(ImageStoreUploadFields.DATE);
		imgOrder.setOrderBy(OrderByEnum.DESC);
		boolean regenPolicy = false;
		List<ImageStoreUploadTransferObject> imageUploads = null;
		try {
			imageUploads = persistService.fetchImageUploads(imgOrder);
		} catch (DbException e) {
			updateImageActionState(Constants.ERROR,"Error in recreating policy");
			log.error("Error in fetchImageUploads , Recreate task", e);
			return false;
		}
		for (ImageStoreUploadTransferObject imageTransfer : imageUploads) {
			if (imageTransfer.getStoreArtifactId().equals(
					trustPolicyObj.getImage().getImageId())) {
				log.info("Found an image in glance");
				regenPolicy = true;
				break;
			}
		}

		if (regenPolicy) {
			log.info("Regen for image {}", imageActionObject.getImage_id());
			log.info("Image has policy id {}", imageInfo.getTrust_policy_id());
		
			UUID uuid = new UUID(); 
			trustPolicyObj.getImage().setImageId(uuid.toString());
			trustPolicyObj.setSignature(null);
			TrustPolicyService trustPolicyService = null;
			try {
				trustPolicyService = new TrustPolicyServiceImpl(imageInfo.getId());
			} catch (DirectorException e1) {
				log.error("Error init TrustPolicyService", e1);
				return false;
			}
			String policyXml = null;
			try {
				policyXml = TdaasUtil.convertTrustPolicyToString(trustPolicyObj);
			} catch (JAXBException e) {
				log.error("Error converting policy object to string", e);
				return false;
			}
			try {
				policyXml = trustPolicyService.signTrustPolicy(policyXml);
			} catch (DirectorException e) {
				log.error("Error signing trust policy", e);
				return false;			}
			trustPolicy.setTrust_policy(policyXml);
			try {
				trustPolicyService.archiveAndSaveTrustPolicy(policyXml, trustPolicy.getCreated_by_user_id());
			} catch (DirectorException e) {
				log.error("Error saving trust policy", e);
				return false;
			}

			//TODO: Remove after test start
			try {
				imageInfo = persistService.fetchImageById(imageActionObject
						.getImage_id());
				if(imageInfo != null){
					String trust_policy_id = imageInfo.getTrust_policy_id();
					com.intel.director.api.TrustPolicy fetchPolicyById = persistService.fetchPolicyById(trust_policy_id);
					log.info("Updated trust policy with new generated image id {}",fetchPolicyById.getTrust_policy());
				}
			}catch(DbException e){
				log.error("Error fetching image", e);
			}			
			log.info("Image NOW has policy id {}", imageInfo.getTrust_policy_id());
			
			//TODO: Remove after test end

		}

		updateImageActionState(Constants.COMPLETE, "recreate task completed");

		/*
		 * } catch (Exception e) { runFlag = false;
		 * updateImageActionState(Constants.ERROR, e.getMessage()); }
		 */
		return runFlag;

	}

}
