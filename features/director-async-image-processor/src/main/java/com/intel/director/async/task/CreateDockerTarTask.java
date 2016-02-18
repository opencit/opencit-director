package com.intel.director.async.task;

import java.io.File;

import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.MountImage;

public class CreateDockerTarTask extends ImageActionAsyncTask {
	


	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(CreateDockerTarTask.class);

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
			log.info("Previous task was completed which was Create Docker Tar");
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				log.info("Create Docker Tar is In Progress");
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				log.info("Create Docker  Tar is being executed");
				runFlag = runCreateDockerTarTask();
			}
		}
		return runFlag;

	}

	/**
	 * After the check in the run method this task creates a docker tar from an image
	 * and policy
	 */
	public boolean runCreateDockerTarTask() {
		String trustPolicyName;
		boolean runFlag = false;

		try {

			log.info("Inside runCreateDockerTartask for ::"
					+ imageActionObject.getImage_id());
			ImageInfo imageinfo = persistService
					.fetchImageById(imageActionObject.getImage_id());
			String imageLocation = imageinfo.getLocation();
			// Fetch the policy and write to a location. Move to common

			TrustPolicy trustPolicy = persistService
					.fetchPolicyForImage(imageActionObject.getImage_id());
			if (trustPolicy == null) {
				log.error("No trust policy for image : "
						+ imageActionObject.getImage_id());
				return false;
			}
			log.info("Create Docker Tar has a trust policy");

			String newLocation = imageLocation
					+ imageActionObject.getImage_id() + File.separator;
			DirectorUtil.callExec("mkdir -p " + newLocation);

			log.info("Create Tar : Create policy file start");
			trustPolicyName = "trustpolicy.xml";
		
			FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
			fileUtilityOperation.createNewFile(newLocation + trustPolicyName);
			fileUtilityOperation.writeToFile(newLocation + trustPolicyName, trustPolicy.getTrust_policy());
			log.info("Creating Docker Tar ::tarName::" + imageinfo.repository + ":" + imageinfo.tag + " tarLocation::"
					+ newLocation + " trustPolicyName::" + trustPolicyName
					+ " imageLocation::" + imageLocation);
			
			log.info("Injecting Policy @ /trust");
			DirectorUtil.executeCommandInExecUtil(Constants.dockerPolicyInject,
					imageinfo.repository, imageinfo.tag + "_source",
					imageinfo.repository, imageinfo.tag, newLocation);
			log.info("Policy Injected Successfully");
			log.info("Creating Docker tar...!!!");
			MountImage.dockerSave(imageinfo.repository, imageinfo.tag,
					newLocation, trustPolicy.getDisplay_name() + ".tar");
			log.info("Docker tar created Successfully");
			log.info("Removing Image :: " + imageinfo.repository + imageinfo.tag);
			MountImage.dockerRMI(imageinfo.repository, imageinfo.tag);
			log.info("Image removed" + imageinfo.repository + imageinfo.tag);
			log.info("Create Docker Tar : commplete");

			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
			runFlag = true;
		} catch (Exception e) {
			log.error(
					"CreateDockerTar task failed for"
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
		return Constants.TASK_NAME_CREATE_DOCKER_TAR;
	}



}
