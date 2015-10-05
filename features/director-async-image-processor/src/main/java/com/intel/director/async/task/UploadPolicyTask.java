/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.intel.director.common.Constants;

/**
 * 
 * @author GS-0681
 */
public class UploadPolicyTask extends UploadTask {

	public UploadPolicyTask() {
		super();
		uploadType = Constants.TASK_NAME_UPLOAD_POLICY;
	}

	public UploadPolicyTask(String imageStoreName) {
		super(imageStoreName);
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return Constants.TASK_NAME_UPLOAD_POLICY;
	}

	@Override
	public void run() {

	
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				runUploadPolicyTask();
			}
		

	}

	public void runUploadPolicyTask() {

		File trustPolicyFile = null;
		 String imagePathDelimiter = "/";
		try {

			String imageLocation = imageInfo.getLocation();
			if (trustPolicy != null) {

				String trustPolicyName = "policy_" + trustPolicy.getId()
						+ ".xml";

				String trustPolicyLocation = imageLocation;
				trustPolicyFile = new File(trustPolicyLocation + imagePathDelimiter
						+ trustPolicyName);

				if (!trustPolicyFile.exists()) {
					trustPolicyFile.createNewFile();
				}

				FileWriter fw = new FileWriter(
						trustPolicyFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(trustPolicy.getTrust_policy());
				bw.close();
			}

			content = trustPolicyFile;

			super.run();
			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);

		} catch (Exception e) {
			updateImageActionState(Constants.ERROR, e.getMessage());
		} finally {
			if (trustPolicyFile != null && trustPolicyFile.exists()) {
				trustPolicyFile.delete();
			}
		}

	}

}
