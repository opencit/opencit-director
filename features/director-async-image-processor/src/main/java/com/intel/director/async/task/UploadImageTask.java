/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;

import com.intel.director.common.Constants;
import com.intel.director.util.TdaasUtil;

;

/**
 * 
 * Task to upload image to image store
 * 
 * @author GS-0681
 */
public class UploadImageTask extends UploadTask {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadTarTask.class);

	public UploadImageTask() {
		super();
		uploadType = Constants.TASK_NAME_UPLOAD_IMAGE;
	}

	public UploadImageTask(String imageStoreName) {
		super(imageStoreName);
	}

	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPLOAD_IMAGE;
	}

	/**
	 * Entry method for running the task
	 */
	@Override
	public void run() {

		if (previousTasksCompleted(taskAction.getTask_name())) {
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				super.initProperties();
				runCreateImageTask();
			}
		}

	}

	/**
	 * Actual implementation of task for uploading image
	 */
	public void runCreateImageTask() {
		log.debug("Inside runUploadImageTask for ::"
				+ imageActionObject.getImage_id());
		try {
			String imageFilePath = null;
			String imageLocation = imageInfo.getLocation();
			boolean encrypt = false;
			if (trustPolicy != null) {
				com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil
						.getPolicy(trustPolicy.getTrust_policy());
				if (policy != null && policy.getEncryption() != null) {
					imageFilePath = imageLocation + imageInfo.getName()
							+ "-enc";
					imageProperties.put(Constants.NAME, imageInfo.getName()
							+ "-enc");
					encrypt = true;
				}

			}
			if (!encrypt) {
				imageFilePath = imageLocation + imageInfo.getName();
				imageProperties.put(Constants.NAME, imageInfo.getName());
			}

			content = new File(imageFilePath);

		} catch (Exception e) {
			e.printStackTrace();
			log.error(
					" runCreateImageTask failed for ::"
							+ imageActionObject.getImage_id(), e);
			updateImageActionState(Constants.ERROR, e.getMessage());
		}
		super.run();

	}
}
