/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ArtifactUploadService;
import com.intel.director.service.impl.ArtifactUploadServiceImpl;

/**
 * Task to upload the tar of image and policy
 * 
 * @author GS-0681
 */
public class UploadTarTask extends GenericUploadTask {
	public UploadTarTask() throws DirectorException {
		super();
	}

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadTarTask.class);

	/**
	 * Entry method for running task. Checks if the previous task was completed
	 */
	@Override
	public boolean run() {
		boolean runFlag = false;
		if (previousTasksCompleted(taskAction.getTask_name())) {
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				runFlag = runUploadTarTask();
			}
		} else {
			log.info("Just returning");
		}
		return runFlag;

	}

	/**
	 * Actual implementation of the task
	 */
	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPLOAD_TAR;
	}

	public boolean runUploadTarTask() {
		boolean runFlag;
		log.info("Inside runUploadTarTask for ::" + imageActionObject.getImage_id());
		String tarName = trustPolicy.getDisplay_name().replace("/", "-") + ".tar";
		if (Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(imageInfo.image_deployments)) {
			ArtifactUploadService artifactUploadService = new ArtifactUploadServiceImpl();
			ImageStoreUploadTransferObject imageUploadByImageId = artifactUploadService
					.fetchImageUploadByImageId(imageInfo.getId());
			customProperties.put(Constants.GLANCE_ID, imageInfo.getId());
			if (imageUploadByImageId != null) {
				UUID uuid = new UUID();
				customProperties.put(Constants.GLANCE_ID, uuid.toString());
			}
		} else {
			String glanceId = DirectorUtil.fetchIdforUpload(trustPolicy);
			log.info("Inside Run Upload Tar task glanceId::" + glanceId);
			customProperties.put(Constants.GLANCE_ID, glanceId);
		}
		customProperties.put(Constants.NAME, trustPolicy.getDisplay_name());
		log.info("TAR name {}", tarName);
		String imageLocation = imageInfo.getLocation();
		String tarLocation = imageLocation + imageActionObject.getImage_id() + File.separator;

		customProperties.put(Constants.MTWILSON_TRUST_POLICY_LOCATION, "glance_image_tar");

		log.info("runUploadTarTask tarname::" + tarName + " ,tarLocation ::" + tarLocation);
		// File content = new File(tarLocation + tarName);
		customProperties.put(Constants.UPLOAD_TO_IMAGE_STORE_FILE, tarLocation + tarName);
		log.info("Before transferring to generic upload");
		runFlag = super.run();
		log.info("After transferring to generic upload");

		// Cleanup of folder
		cleanupDirectories(tarLocation);
		return runFlag;

	}
	
	private void cleanupDirectories(String tarLocation){
		File uuidFolder = new File(tarLocation);
		FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
		fileUtilityOperation.deleteFileOrDirectory(uuidFolder);

		String encImageFileName = imageInfo.getLocation() + File.separator
				+ imageInfo.getImage_name() + "-enc";
		File encImageFile = new File(encImageFileName);
		fileUtilityOperation.deleteFileOrDirectory(encImageFile);
	}
}
