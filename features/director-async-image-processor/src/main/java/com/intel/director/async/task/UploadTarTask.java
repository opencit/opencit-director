/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.images.exception.DirectorException;

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
		boolean runFlag = false;
		log.info("Inside runUploadTarTask for ::"
				+ imageActionObject.getImage_id());
		String tarName = trustPolicy.getDisplay_name() + ".tar";
		if (Constants.DEPLOYMENT_TYPE_DOCKER
				.equalsIgnoreCase(imageInfo.image_deployments)) {
			customProperties.put(Constants.NAME, imageInfo.repository + ":" + imageInfo.tag);
		} else {	
			customProperties.put(Constants.NAME, trustPolicy.getDisplay_name());
		}
		
		log.info("TAR name {}", tarName);
		String imageLocation = imageInfo.getLocation();
		String tarLocation = imageLocation + imageActionObject.getImage_id()
				+ File.separator;
		JAXBContext jaxbContext = null;
		try {
			jaxbContext = JAXBContext
					.newInstance(com.intel.mtwilson.trustpolicy.xml.TrustPolicy.class);
		} catch (JAXBException e) {
			updateImageActionState(Constants.ERROR,"Error in Uploading Tar");
			log.error("Unable to instantiate the jaxbcontext", e);
			return false;
		}
	
		customProperties.put(Constants.MTWILSON_TRUST_POLICY_LOCATION,
				"glance_image_tar");

	
		String glanceId = DirectorUtil.fetchIdforUpload(trustPolicy);
		log.info("Inside Run Upload Tar task glanceId::" + glanceId);
		customProperties.put(Constants.GLANCE_ID, glanceId);
		log.info("runUploadTarTask tarname::" + tarName + " ,tarLocation ::"
				+ tarLocation);
		//File content = new File(tarLocation + tarName);
		customProperties.put(Constants.UPLOAD_TO_IMAGE_STORE_FILE, tarLocation + tarName);
		log.info("Before transferring to generic upload");
		super.run();
		log.info("After transferring to generic upload");
		runFlag = true;

		// Cleanup of folder
		File uuidFolder = new File(tarLocation);
		File[] listFiles = uuidFolder.listFiles();
		boolean deleteFileFlag = true;
		for (File file : listFiles) {
			log.info("Deleteing file " + file.getAbsolutePath()
					+ " after successful upload");
			if (!file.delete()) {
				log.info("!!!!! File could not be deleted");
				deleteFileFlag = false;
			}

		}
		if (deleteFileFlag) {
			deleteFileFlag = uuidFolder.delete();
			log.info("Is folder deleted == " + deleteFileFlag);
		} else {
			log.info("UUID : " + tarLocation + " cannot be cleaned up");
		}
		String encImageFileName = imageInfo.getLocation() + File.separator
				+ imageInfo.getImage_name() + "-enc";
		File encImageFile = new File(encImageFileName);
		if (encImageFile.exists()) {
			encImageFile.delete();
		}
		return runFlag;

	}
}
