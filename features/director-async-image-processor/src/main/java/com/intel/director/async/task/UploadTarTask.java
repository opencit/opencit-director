/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;

import com.intel.director.common.Constants;
import com.intel.director.util.TdaasUtil;

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
	public void run() {

		if (previousTasksCompleted(taskAction.getTask_name())) {
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				super.initProperties();
				runUploadTarTask();
			}
		}

	}

	/**
	 * Actual implementation of the task
	 */
	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return Constants.TASK_NAME_UPLOAD_TAR;
	}

	public void runUploadTarTask() {
		log.debug("Inside runUploadTarTask for ::"
				+ imageActionObject.getImage_id());
		try {
			String imagePathDelimiter = "/";
			String imageName;
			String imageLocation = imageInfo.getLocation();
			boolean encrypt = false;
			if (trustPolicy != null) {
				com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil
						.getPolicy(trustPolicy.getTrust_policy());
				if (policy != null && policy.getEncryption() != null) {
					imageName = imageInfo.getName() + "-enc";
					encrypt = true;
				}
			}

			if (!encrypt) {
				imageName = imageInfo.getName();
			}

			String tarName = "tar_" + trustPolicy.getDisplay_name();

			imageProperties.put(Constants.NAME, tarName);
			imageProperties.put(Constants.MTWILSON_TRUST_POLICY_LOCATION, "glance_image_tar");
			String tarLocation = imageLocation+imageActionObject.getImage_id()+File.separator;
			log.debug("runUploadTarTask tarname::" + tarName
					+ " ,tarLocation ::" + tarLocation);
			content = new File(tarLocation + tarName);

		} catch (Exception e) {
			e.printStackTrace();
			log.debug(
					"Upload tar task fail for::"
							+ imageActionObject.getImage_id(), e);

		}
		super.run();

	}

}
