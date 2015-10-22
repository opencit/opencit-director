/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.util.TdaasUtil;

/**
 * Class to create a TAR from the image and policy
 * 
 * @author GS-0681
 */
public class CreateTarTask extends ImageActionTask {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadTarTask.class);

	/**
	 * 
	 * Entry method to run the task
	 * 
	 */
	@Override
	public void run() {

		// Call to update the task status

		if (previousTasksCompleted(taskAction.getTask_name())) {
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				runCreateTarTask();
			}
		}

	}

	/**
	 * After the check in the run method this task creates a tar from an image and policy
	 */
	public void runCreateTarTask() {
		String imageLocation = null;
		String trustPolicyLocation = null;
		String trustPolicyName = null;
		File trustPolicyFile = null;

		try {
			log.debug("Inside runCreateTartask for ::"
					+ imageActionObject.getImage_id());
			ImageInfo imageinfo = persistService
					.fetchImageById(imageActionObject.getImage_id());
			imageLocation = imageinfo.getLocation();
			// Fetch the policy and write to a location. Move to common
			String imageName = null;
			TrustPolicy trustPolicy = persistService
					.fetchPolicyForImage(imageActionObject.getImage_id());
			boolean encrypt = false;
			if (trustPolicy != null) {
				com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil
						.getPolicy(trustPolicy.getTrust_policy());

				if (policy != null && policy.getEncryption() != null) {
					imageName = imageinfo.getName() + "-enc";
					encrypt = true;
				}

			}
			if (!encrypt) {
				imageName = imageinfo.getName();
			}

			if (trustPolicy != null) {

				trustPolicyName = "policy_" + trustPolicy.getDisplay_name()
						+ ".xml";
				trustPolicyLocation = imageLocation;
				trustPolicyFile = new File(trustPolicyLocation
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

			String tarLocation = imageLocation;
			String tarName = "tar_" + trustPolicy.getDisplay_name() + ".tar";
			log.debug("Create Tar ::tarName::" + tarName + " tarLocation::"
					+ tarLocation + " trustPolicyName::" + trustPolicyName
					+ " imageLocation::" + imageLocation);
			DirectorUtil.createTar(imageLocation, imageName, trustPolicyName,
					tarLocation, tarName);
			// log.debug("//////////////tar::"+tar);
			// //Thread.sleep(60000);

			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(
					"CreateTar task failed for"
							+ imageActionObject.getImage_id(), e);
			updateImageActionState(Constants.ERROR, e.getMessage());
		} finally {

		}

	}

	/**
	 * Returns the task name
	 */
	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return Constants.TASK_NAME_CREATE_TAR;
	}

}
