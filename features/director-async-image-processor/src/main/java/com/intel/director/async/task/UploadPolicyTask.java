/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.intel.director.common.Constants;

/**
 * Task to upload the policy to image store
 * 
 * @author GS-0681
 */
public class UploadPolicyTask extends UploadTask {
	public static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadPolicyTask.class);

	public UploadPolicyTask() {
		super();
		uploadType = Constants.TASK_NAME_UPLOAD_POLICY;
	}

	public UploadPolicyTask(String imageStoreName) {
		super(imageStoreName);
	}

	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPLOAD_POLICY;
	}

	/**
	 * Entry method for uploading policy
	 */
	@Override
	public boolean run() {
		boolean runFlag = false;
		if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
			updateImageActionState(Constants.IN_PROGRESS, "Started");
			runFlag = runUploadPolicyTask();
		}
		return runFlag;
	}

	/**
	 * Actual implementation of policy upload task
	 */
	public boolean runUploadPolicyTask() {
		boolean runFlag = false;
		File trustPolicyFile = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {

			String imageLocation = imageInfo.getLocation();
			if (trustPolicy != null) {

				String trustPolicyName = "trustpolicy-" + trustPolicy.getId()
						+ ".xml";

				String trustPolicyLocation = imageLocation;
				trustPolicyFile = new File(trustPolicyLocation
						+ trustPolicyName);

				if (!trustPolicyFile.exists()) {
					trustPolicyFile.createNewFile();
				}

				fw = new FileWriter(trustPolicyFile.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				bw.write(trustPolicy.getTrust_policy());
			}

			content = trustPolicyFile;
			super.run();
			runFlag = true;

		} catch (Exception e) {
			updateImageActionState(Constants.ERROR, e.getMessage());
		} finally {
			if (trustPolicyFile != null && trustPolicyFile.exists()) {
				trustPolicyFile.delete();
			}
			try {
				if (bw != null) {
					bw.close();
				}
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				log.error("Error closing streams ");
			}
		}
		return runFlag;

	}

}
