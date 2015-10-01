/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;

import com.intel.director.common.Constants;
import com.intel.director.util.DirectorUtil;

/**
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

	@Override
	public void run() {

		
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				runUploadTarTask();
			}
		

	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return Constants.TASK_NAME_UPLOAD_TAR;
	}

	public void runUploadTarTask() {

		try {
			String imagePathDelimiter = "/";
			String imageLocation = imageInfo.getLocation();
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = DirectorUtil
					.getPolicy(trustPolicy.getTrust_policy());
			String imageName;
			if (policy != null && policy.getEncryption() != null) {
				imageName = imageInfo.getName() + "-enc";

			} else {
				imageName = imageInfo.getName();
			}

			String tarName = imageName + "_" + trustPolicy.getId() + ".tar";

			String tarLocation = imageLocation;

			content = new File(tarLocation + imagePathDelimiter + tarName);

		} catch (Exception e) {

		}
		super.run();
		updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);

	}

}
