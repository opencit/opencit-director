/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;

import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.util.TdaasUtil;

/**
 * Class to create a TAR from the image and policy
 * 
 * @author GS-0681
 */
public class CreateTarTask extends ImageActionAsyncTask {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(CreateTarTask.class);

	/**
	 * 
	 * Entry method to run the task
	 * 
	 */
	@Override
	public boolean run() {

		// Call to update the task status
		boolean runFlag = false;
		if (previousTasksCompleted(taskAction.getTask_name())) {
			log.info("Previous task was completed which was Create Tar");
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				log.info("Create Tar is In Progress");
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				log.info("Create Tar is being executed");
				runFlag = runCreateTarTask();
			}
		}
		return runFlag;

	}

	/**
	 * After the check in the run method this task creates a tar from an image
	 * and policy
	 */
	public boolean runCreateTarTask() {
		String trustPolicyName;
		boolean runFlag = false;

		try {

			log.info("Inside runCreateTartask for ::"
					+ imageActionObject.getImage_id());
			ImageInfo imageinfo = persistService
					.fetchImageById(imageActionObject.getImage_id());
			String imageLocation = imageinfo.getLocation();
			// Fetch the policy and write to a location. Move to common

			String imageName = imageinfo.getImage_name();
			TrustPolicy trustPolicy = persistService
					.fetchPolicyForImage(imageActionObject.getImage_id());
			if (trustPolicy == null) {
				log.error("No trust policy for image : "
						+ imageActionObject.getImage_id());
				return false;
			}
			log.info("Create Tar has a trust policy");

			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil
					.getPolicy(trustPolicy.getTrust_policy());

			if (policy != null && policy.getEncryption() != null) {
				log.info("Create Tar has a trust policy which is encrypted");
				imageName = imageinfo.getImage_name() + "-enc";
			}

			String newLocation = imageLocation
					+ imageActionObject.getImage_id() + File.separator;
			DirectorUtil.callExec("mkdir -p " + newLocation);
			DirectorUtil.createCopy(imageLocation + imageName, newLocation
					+ imageName);
			

			log.info("Create Tar : Create policy file start");
			trustPolicyName = "trustpolicy.xml";
		
			FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
			fileUtilityOperation.createNewFile(newLocation + trustPolicyName);
			fileUtilityOperation.writeToFile(newLocation + trustPolicyName, trustPolicy.getTrust_policy());
			String tarLocation = newLocation;
			String tarName = trustPolicy.getDisplay_name() + ".tar";
			log.info("Create Tar ::tarName::" + tarName + " tarLocation::"
					+ tarLocation + " trustPolicyName::" + trustPolicyName
					+ " imageLocation::" + imageLocation);
			DirectorUtil.createTar(newLocation, imageName, trustPolicyName,
					tarLocation, tarName);
			log.info("Create Tar : commplete");

			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
			runFlag = true;
		} catch (Exception e) {
			log.error(
					"CreateTar task failed for"
							+ imageActionObject.getImage_id(), e);
			updateImageActionState(Constants.ERROR, e.getMessage());
		} 
		return runFlag;
	}
	

	/**
	 * Returns the task name
	 */
	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_CREATE_TAR;
	}

}
