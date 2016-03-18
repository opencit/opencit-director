/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import javax.xml.bind.JAXBException;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.common.Constants;
import com.intel.director.common.DockerUtil;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ArtifactUploadService;
import com.intel.director.service.impl.ArtifactUploadServiceImpl;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;

/**
 * 
 * Task to upload image to image store
 * 
 * @author Aakash
 */

public class UploadImageTask extends GenericUploadTask {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadTarTask.class);

	private boolean isDockerhubUplod = false;

	public UploadImageTask() throws DirectorException {
		super();
	}

	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPLOAD_IMAGE;
	}
	
	
	public String fetchUploadImageId(){
		log.info("Inside UploadImageTask fetchUploadImageId()");
		String glanceId=null;
		ArtifactUploadService artifactUploadService = new ArtifactUploadServiceImpl();
		ImageStoreUploadTransferObject imageUploadByImageId = artifactUploadService
				.fetchImageUploadByImageId(imageInfo.getId());
		glanceId= imageInfo.getId();
		
		if (imageUploadByImageId != null) {
			UUID uuid = new UUID();
			glanceId= uuid.toString();
		}
		log.info("Inside UploadImageTask fetchUploadImageId() glanceId::"+glanceId);
		return glanceId;
	}
	
	public String fetchUploadImageName(){
		return imageInfo.getImage_name();
	}

	/**
	 * Entry method for running the task
	 */
	@Override
	public boolean run() {

		boolean runFlag = false;
		if (previousTasksCompleted(taskAction.getTask_name())) {
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				runFlag = runUploadImageTask();
			}
		}
		return runFlag;

	}

	/**
	 * Actual implementation of task for uploading image
	 */
	public boolean runUploadImageTask() {
		boolean runFlag = false;

		log.info("Inside runUploadImageTask for ::"
				+ imageActionObject.getImage_id());
		
			if (imageInfo.getImage_deployments().equals(
					Constants.DEPLOYMENT_TYPE_VM)) {
				try {
					setupUploadVmImage();
				} catch (DirectorException e) {
					updateImageActionState(Constants.ERROR, "Error in uploading Image");
					
				}
			} else if (imageInfo.getImage_deployments().equals(
					Constants.DEPLOYMENT_TYPE_DOCKER)) {
				try {
					setupUploadDockerImage();
				} catch (DirectorException e) {
					updateImageActionState(Constants.ERROR, "Error in uploading Image");
				}
			}
			runFlag = super.run();
			if (isDockerhubUplod) {
				DockerUtil.dockerRMI(imageInfo.repository, imageInfo.tag);
			}
		
		return runFlag;

	}

	
	private void setupUploadVmImage() throws DirectorException {
		String imageFilePath = null;
		String imageLocation = imageInfo.getLocation();
		boolean encrypt = false;
		log.info("Inside setupUploadVmImage");
		
		String glanceId=fetchUploadImageId();
		customProperties.put(Constants.GLANCE_ID,glanceId);
		String uploadImageName=fetchUploadImageName();
		customProperties.put(Constants.NAME, uploadImageName);
		imageFilePath = imageLocation + imageInfo.getImage_name();
		if (trustPolicy != null) {
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = null;
			try {
				policy = TdaasUtil.getPolicy(trustPolicy.getTrust_policy());
			} catch (JAXBException e) {
				log.error("Unable to convert policy xml to object", e);
				updateImageActionState(Constants.ERROR, "Error in uploading Image for vm");
				throw new DirectorException(
						"Unable to convert policy xml to object", e);
			}

			
			if (policy != null && policy.getEncryption() != null) {
				imageFilePath += "-enc";
			}
			 
		}

		customProperties.put(Constants.UPLOAD_TO_IMAGE_STORE_FILE, imageFilePath);
	}

	private void setupUploadDockerImage() throws DirectorException {
		
		ArtifactUploadService artifactUploadService = new ArtifactUploadServiceImpl();
		ImageStoreUploadTransferObject imageUploadByImageId = artifactUploadService
				.fetchImageUploadByImageId(imageInfo.getId());
		customProperties.put(Constants.GLANCE_ID, imageInfo.getId());
		if (imageUploadByImageId != null) {
			UUID uuid = new UUID();
			customProperties.put(Constants.GLANCE_ID, uuid.toString());
		}
		customProperties.put(Constants.NAME, imageInfo.getRepository() + ":"
				+ imageInfo.getTag());
		ImageStoreTransferObject storeTransferObject = null;
		try {
			storeTransferObject = persistService.fetchImageStorebyId(taskAction
					.getStoreId());
		} catch (DbException e) {
			log.error(
					"Error fetching image store for id "
							+ taskAction.getStoreId(), e);
			updateImageActionState(Constants.ERROR, "Upload Image Task for docker failed");
			throw new DirectorException("Error fetching image store for id "
					+ taskAction.getStoreId(), e);
		}
		if (storeTransferObject.getConnector().equals(
				Constants.CONNECTOR_DOCKERHUB)) {
			// Its a simple image upload - DI
			if (imageActionObject.getActions().size() == 1) {
				DockerUtil.dockerTag(imageInfo.repository, imageInfo.tag
						+ "_source", imageInfo.repository, imageInfo.tag);
				isDockerhubUplod = true;
			}
		} else {
			String imageLocation = imageInfo.getLocation();
			String imageFilePath = null;
			imageFilePath = imageLocation + imageInfo.getImage_name();
			//File content = new File(imageFilePath);
			customProperties.put(Constants.UPLOAD_TO_IMAGE_STORE_FILE, imageFilePath);
			customProperties.put(Constants.NAME, imageInfo.getRepository()
					+ ":" + imageInfo.getTag());
		}

	}
}
