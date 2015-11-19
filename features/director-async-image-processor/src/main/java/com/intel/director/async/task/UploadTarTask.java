/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;

import com.intel.director.common.Constants;

/**
 * Task to upload the tar of image and policy
 * 
 * @author GS-0681
 */
public class UploadTarTask extends UploadTask {
	public UploadTarTask() {
		super();
		uploadType = Constants.TASK_NAME_UPLOAD_TAR;
	}

	public UploadTarTask(String imageStoreName) {
		super(imageStoreName);
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
				super.initProperties();
				runFlag  = runUploadTarTask();
			}
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
		log.debug("Inside runUploadTarTask for ::"
				+ imageActionObject.getImage_id());
		try {
			String imageLocation = imageInfo.getLocation();

			String tarName = trustPolicy.getDisplay_name() + ".tar";

			imageProperties.put(Constants.NAME, trustPolicy.getDisplay_name());
			imageProperties.put(Constants.MTWILSON_TRUST_POLICY_LOCATION, "glance_image_tar");
			String tarLocation = imageLocation+imageActionObject.getImage_id()+File.separator;
			log.debug("runUploadTarTask tarname::" + tarName
					+ " ,tarLocation ::" + tarLocation);
			content = new File(tarLocation + tarName);
			super.run();
			runFlag = true;
			return runFlag;
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(
					"Upload tar task fail for::"
							+ imageActionObject.getImage_id(), e);

		}
		return runFlag;

	}

}
