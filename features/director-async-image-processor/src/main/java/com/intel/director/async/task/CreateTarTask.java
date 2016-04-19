/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.DockerUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.util.TdaasUtil;

/**
 * Class to create a TAR from the image and policy
 * 
 * @author GS-0681
 */
public class CreateTarTask extends ImageActionAsyncTask {

	public CreateTarTask() throws DirectorException {
		super();
	}

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
			String imageLocation = imageInfo.getLocation();
			// Fetch the policy and write to a location. Move to common
			if (Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(imageInfo
					.getImage_deployments())) {
				String tarDestination = imageLocation + imageActionObject.getImage_id();
				DirectorUtil.callExec("mkdir -p " + tarDestination);
				String display_name = trustPolicy.getDisplay_name();
				int tagStart = display_name.lastIndexOf(":") + 1;
				String repo = display_name.substring(0, tagStart - 1);
				String tag = display_name.substring(tagStart);
				int exitCode = DockerUtil.dockerSave(repo, tag, tarDestination,
						trustPolicy.getDisplay_name().replace("/", "-") + ".tar");				
				DockerUtil.dockerRMI(repo, tag);
				if (exitCode != 0) {
					new FileUtilityOperation().deleteFileOrDirectory(new File(tarDestination));
					log.error("Docker save for image {} failed", imageInfo.id);
					return false;
				}
			} else {

				log.info("Inside runCreateTartask for ::"
						+ imageActionObject.getImage_id());

				String imageName = imageInfo.getImage_name();
				if (trustPolicy == null) {
					log.error("No trust policy for image : "
							+ imageActionObject.getImage_id());
					return false;
				}
				log.debug("Create Tar has a trust policy");

				com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil
						.getPolicy(trustPolicy.getTrust_policy());

				if (policy != null && policy.getEncryption() != null) {
					log.info("Create Tar has a trust policy which is encrypted");
					imageName = imageInfo.getImage_name() + "-enc";
				}

				String newLocation = imageLocation
						+ imageActionObject.getImage_id() + File.separator;
				DirectorUtil.callExec("mkdir -p " + newLocation);

				log.debug("Create Tar : Create policy file start");
				trustPolicyName = "trustpolicy.xml";

				FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
				fileUtilityOperation.createNewFile(newLocation
						+ trustPolicyName);
				fileUtilityOperation.writeToFile(newLocation + trustPolicyName,
						trustPolicy.getTrust_policy());
				String tarLocation = newLocation;
				String tarName = trustPolicy.getDisplay_name().replace("/", "-") + ".tar";
				List<String> filePaths = new ArrayList<>(2);
				filePaths.add(imageLocation + imageName);
				filePaths.add(newLocation + trustPolicyName);
				int ret = new FileUtilityOperation().createTar(tarLocation
						+ "/" + tarName, filePaths);
				if (ret == 1) {
					log.error("CreateTar task failed.");
					updateImageActionState(Constants.ERROR,
							"TAR Utility failed");
					return false;
				}
			}

			log.info("Create Tar : complete");

			updateImageActionState(Constants.COMPLETE,"Create Tar completed");
			runFlag = true;
		} catch (Exception e) {
			log.error(
					"CreateTar task failed for"
							+ imageActionObject.getImage_id(), e);
			updateImageActionState(Constants.ERROR, "Create Tar Task failed");
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
