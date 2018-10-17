/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.common.Constants;
import com.intel.director.common.DockerUtil;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ArtifactUploadService;
import com.intel.director.service.impl.ArtifactUploadServiceImpl;
import com.intel.mtwilson.director.db.exception.DbException;

/**
 * 
 * Task to upload image to image store
 * 
 * @author Aakash
 */

public class UploadImage extends GenericUpload {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadTar.class);

	private boolean isDockerhubUplod = false;
	private String dockerTagToUse;
	public UploadImage() throws DirectorException {
		super();
	}

	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPLOAD_IMAGE;
	}
	
	
	public String fetchUploadImageId() {
		log.info("Inside UploadImageTask fetchUploadImageId()");
		String glanceId;
		ArtifactUploadService artifactUploadService = new ArtifactUploadServiceImpl();
		ImageStoreUploadTransferObject imageUploadByImageId = artifactUploadService
				.fetchImageUploadByImageId(imageInfo.getId());
		glanceId = imageInfo.getId();

		if (imageUploadByImageId != null) {
			UUID uuid = new UUID();
			glanceId = uuid.toString();
		}
		log.info("Inside UploadImageTask fetchUploadImageId() glanceId::" + glanceId);
		return glanceId;
	}
	
	/// This the name by which image will be uploaded to image Store. We upload image by original imageName , not by policy display name in case
	// of only image upload
	public String fetchUploadImageName() {
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
		boolean runFlag;

		log.info("Inside runUploadImageTask for ::" + imageActionObject.getImage_id());

		if (imageInfo.getImage_deployments().equals(Constants.DEPLOYMENT_TYPE_VM)) {
			try {
				setupUploadVmImage();
			} catch (DirectorException e) {
				updateImageActionState(Constants.ERROR, "Error in uploading Image");

			}
		} else if (imageInfo.getImage_deployments().equals(Constants.DEPLOYMENT_TYPE_DOCKER)) {
			try {
				setupUploadDockerImage();
			} catch (DirectorException e) {
				updateImageActionState(Constants.ERROR, "Error in uploading Image");
			}
		}
		runFlag = super.run();
	/*	if (isDockerhubUplod) {
			DockerUtil.dockerRMI(imageInfo.repository, dockerTagToUse);
		}*/

		return runFlag;

	}

	
	private void setupUploadVmImage() throws DirectorException {

		String imageLocation = imageInfo.getLocation();
		log.info("Inside setupUploadVmImage");

		String glanceId = fetchUploadImageId();
		customProperties.put(Constants.GLANCE_ID, glanceId);
		String uploadImageName = fetchUploadImageName();
		customProperties.put(Constants.NAME, uploadImageName);
		String imageFilePath = imageLocation + imageInfo.getImage_name();
		/*
		if (trustPolicy != null) {
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy;
			try {
				policy = TdaasUtil.getPolicy(trustPolicy.getTrust_policy());
			} catch (JAXBException e) {
				log.error("Unable to convert policy xml to object", e);
				updateImageActionState(Constants.ERROR, "Error in uploading Image for vm");
				throw new DirectorException("Unable to convert policy xml to object", e);
			}

			if (policy != null && policy.getEncryption() != null) {
				imageFilePath += "-enc";
			}

		}
		*/
		customProperties.put(Constants.UPLOAD_TO_IMAGE_STORE_FILE, imageFilePath);
	}

	private void setupUploadDockerImage() throws DirectorException {

		////customProperties.put(Constants.NAME, imageInfo.getRepository() + ":" + imageInfo.getTag());
		ImageStoreTransferObject storeTransferObject;
		try {
			storeTransferObject = persistService.fetchImageStorebyId(taskAction.getStoreId());
		} catch (DbException e) {
			log.error("Error fetching image store for id " + taskAction.getStoreId(), e);
			updateImageActionState(Constants.ERROR, "Upload Image Task for docker failed");
			throw new DirectorException("Error fetching image store for id " + taskAction.getStoreId(), e);
		}
		if (storeTransferObject.getConnector().equals(Constants.CONNECTOR_DOCKERHUB)) {
			
			isDockerhubUplod = true;
			if (imageActionObject.getActions().size() == 1) {  //Only image upload(Only one action, no inject policy action)
				dockerTagToUse = imageInfo.tag;
				DockerUtil.dockerTag(imageInfo.repository, imageInfo.tag + "_source", imageInfo.repository,
						dockerTagToUse);
			} else { //For image with policy, Inject policy already been called and had created new tag
				if(trustPolicy==null){
					throw new DirectorException("Policy do not exist");
				}
				String display_name = trustPolicy.getDisplay_name();
				int tagStart = display_name.lastIndexOf(":") + 1;
				String tag = display_name.substring(tagStart);
				dockerTagToUse = tag;
			}
			log.info("UploadImageTask , setupUploadDockerImage for "+Constants.CONNECTOR_DOCKERHUB+" dockerTagToUse::"+dockerTagToUse);
			customProperties.put(Constants.DOCKER_TAG_TO_USE, dockerTagToUse);
		} else {

			ArtifactUploadService artifactUploadService = new ArtifactUploadServiceImpl();
			ImageStoreUploadTransferObject imageUploadByImageId = artifactUploadService
					.fetchImageUploadByImageId(imageInfo.getId());
			customProperties.put(Constants.GLANCE_ID, imageInfo.getId());
			if (imageUploadByImageId != null) {
				UUID uuid = new UUID();
				customProperties.put(Constants.GLANCE_ID, uuid.toString());
			}
			
			String imageLocation = imageInfo.getLocation();
			String imageFilePath = imageLocation + imageInfo.getImage_name();
			// File content = new File(imageFilePath);
			customProperties.put(Constants.UPLOAD_TO_IMAGE_STORE_FILE, imageFilePath);
			log.info("UploadImageTask , setupUploadDocker name:"+imageInfo.getRepository() + ":" + imageInfo.getTag());
			customProperties.put(Constants.NAME, imageInfo.getRepository() + ":" + imageInfo.getTag());
		}

	}
}
