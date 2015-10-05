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
public class UploadImageTask extends UploadTask {

	public UploadImageTask() {
		super();
		uploadType = Constants.TASK_NAME_UPLOAD_IMAGE;
	}

	public UploadImageTask(String imageStoreName) {
		super(imageStoreName);
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return Constants.TASK_NAME_UPLOAD_IMAGE;
	}

	@Override
	public void run() {
		
	
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				runCreateImageTask();
			}
		
	}
	
	public void runCreateImageTask(){
		 String imagePathDelimiter = "/";
		try {
			String imageFilePath = null;
			String imageLocation = imageInfo.getLocation();
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = DirectorUtil
					.getPolicy(trustPolicy.getTrust_policy());
			if (policy != null && policy.getEncryption() != null) {
				imageFilePath = imageLocation + imagePathDelimiter + imageInfo.getName() + "-enc";
			} else {
				imageFilePath = imageLocation + imagePathDelimiter + imageInfo.getName();
			}

			content = new File(imageFilePath);
			
		} catch (Exception e) {
			updateImageActionState(Constants.ERROR, e.getMessage());
		}
		super.run();
		updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
	
	}
}
