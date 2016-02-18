package com.intel.director.async.task;

import java.io.File;

import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.MountImage;
import com.intel.director.dockerhub.DockerHubManager;
import com.intel.director.imagestore.ImageStoreManager;

public class UploadDockerHubTask extends ImageActionAsyncTask {
	


	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadDockerHubTask.class);

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
				log.info("Upload To Docker Hub is In Progress");
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				log.info("Upload To Docker Hub is being executed");
				runFlag = runUploadToDockeHub();
			}
		}
		return runFlag;

	}

	/**
	 * After the check in the run method this task creates a docker tar from an image
	 * and policy
	 */
	public boolean runUploadToDockeHub() {
		String trustPolicyName;
		boolean runFlag = false;
		ImageInfo imageinfo = null;
		try {

			log.info("Inside runCreateDockerTartask for ::"
					+ imageActionObject.getImage_id());
			imageinfo = persistService
					.fetchImageById(imageActionObject.getImage_id());
			String imageLocation = imageinfo.getLocation();
			// Fetch the policy and write to a location. Move to common

			TrustPolicy trustPolicy = persistService
					.fetchPolicyForImage(imageActionObject.getImage_id());

			if (trustPolicy != null) {
				log.info("Docker Image has a trust policy");

				String newLocation = imageLocation + imageinfo.id + File.separator;
				DirectorUtil.callExec("mkdir -p " + newLocation);

				log.info("Create policy file start");
				trustPolicyName = "trustpolicy.xml";

				FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
				fileUtilityOperation.createNewFile(newLocation + trustPolicyName);
				fileUtilityOperation.writeToFile(newLocation + trustPolicyName,
						trustPolicy.getTrust_policy());
				log.info("Creating Docker Tar ::tarName::" + imageinfo.repository
						+ ":" + imageinfo.tag + " tarLocation::" + newLocation
						+ " trustPolicyName::" + trustPolicyName
						+ " imageLocation::" + imageLocation);

				log.info("Injecting Policy @ /trust");
				DirectorUtil.executeCommandInExecUtil(Constants.dockerPolicyInject,
						imageinfo.repository, imageinfo.tag + "_source",
						imageinfo.repository, imageinfo.tag, newLocation);
			} else {
				MountImage.dockerTag(imageinfo.repository, imageinfo.tag
						+ "_source", imageinfo.repository, imageinfo.tag);
			}
			
			ImageStoreManager dockerHubManager = new DockerHubManager(imageinfo.id, null);
			dockerHubManager.upload(null, null);
			MountImage.dockerRMI(imageinfo.repository, imageinfo.tag);
			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
			runFlag = true;
		} catch (Exception e) {
			log.error(
					"UploadDockerHub task failed for"
							+ imageActionObject.getImage_id(), e);
			MountImage.dockerRMI(imageinfo.repository, imageinfo.tag);
			updateImageActionState(Constants.ERROR, e.getMessage());
		} 
		return runFlag;
	}
	

	/**
	 * Returns the task name
	 */
	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPLOAD_TO_HUB;
	}



}