/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageService;
import com.intel.director.service.impl.ImageServiceImpl;
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
			if (imageTransfer.getImg().getId()
					.equals(imageActionObject.getImage_id())) {
				log.info("image upload entry found for the image id {}",
						imageTransfer.getImage_uri());
				log.info("policy's image id : "
						+ trustPolicyObj.getImage().getImageId());
				if (imageTransfer.getImage_uri().contains(
						trustPolicyObj.getImage().getImageId())) {
					log.info("Found an image in glance");
					regenPolicy = true;
					break;
				}
			}
		}

		if (regenPolicy) {
			log.info("Regen for image {}", imageActionObject.getImage_id());
			log.info("Image has policy id {}", imageInfo.getTrust_policy_id());

			ImageService imageService = new ImageServiceImpl();
			try {
				imageService.mountImage(imageActionObject.getImage_id(),
						"admin");
			} catch (DirectorException e) {
				updateImageActionState(Constants.ERROR, "Error in recreating policy");
				log.error("Error in mountImage , Recreate task", e);
				return false;
			}
			log.info("Mounting complete");

			String createTrustPolicyId = null;
			try {
				createTrustPolicyId = imageService
						.createTrustPolicy(trustPolicy.getId());
			} catch (DirectorException e) {
				updateImageActionState(Constants.ERROR, "Error in recreating policy");
				log.error("Error in createTrustPolicy , Recreate task", e);
				return false;
			}
			log.info("Regen complete. Updating image");
			imageInfo.setTrust_policy_id(createTrustPolicyId);
			imageInfo.setEdited_date(new Date());
			try {
				persistService.updateImage(imageInfo);
			} catch (DbException e) {
				updateImageActionState(Constants.ERROR, "Error in recreating policy");
				log.error("Error in updateImage , Recreate task", e);
				return false;
			}
			log.info("Updating image complete with new policy id {}",
					createTrustPolicyId);
			try {
				imageService.unMountImage(imageActionObject.getImage_id(),
						"admin");
			} catch (DirectorException e) {
				updateImageActionState(Constants.ERROR,"Error in recreating policy");
				log.error("Error in unmounting image", e);
				return false;
			}
		}

		updateImageActionState(Constants.COMPLETE, "recreate task completed");

		/*
		 * } catch (Exception e) { runFlag = false;
		 * updateImageActionState(Constants.ERROR, e.getMessage()); }
		 */
		return runFlag;

	}

}
