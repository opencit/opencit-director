/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;
import java.io.IOException;

import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ImageActionService;
import com.intel.director.service.impl.ImageActionImpl;
import com.intel.mtwilson.director.db.exception.DbException;

/**
 * Class to Injecting Policy To Docker Image
 * 
 * @author GS-0681
 */
public class InjectPolicyTask extends ImageActionAsyncTask {

	public InjectPolicyTask() throws DirectorException {
		super();
	}

	public static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(InjectPolicyTask.class);

	private ImageActionService imageActionService = new ImageActionImpl();

	/**
	 * Entry method for executing the task
	 */
	@Override
	public boolean run() {
		boolean runFlag = false;
		// Call to update the task status
		log.debug("Running Inject Policy task : " + taskAction.getStatus());
		if (taskAction.getStatus().equals(Constants.INCOMPLETE)) {
			log.debug("Running Inject Policy task for "
					+ imageActionObject.getImage_id());
			imageActionService
					.updateImageActionState(imageActionObject, taskAction,
							getTaskName(), Constants.IN_PROGRESS, "Started");
			try {
				injectPolicy();
				imageActionService.updateImageActionState(imageActionObject,
						taskAction, getTaskName(), Constants.COMPLETE,
						"Inject Policy complete");
				runFlag = true;
			} catch (DirectorException e) {
				log.error("Error in InjectPolicyTask", e);
				imageActionService.updateImageActionState(
						imageActionObject,
						taskAction,
						getTaskName(),
						Constants.ERROR,
						"Error while Injecting Policy ");
			}

		}
		return runFlag;
	}

	/**
	 * Task to inject policy
	 * 
	 * @throws DirectorException
	 */
	private void injectPolicy() throws DirectorException {

		log.info("Inside injectPolicy for ::" + imageActionObject.getImage_id());
		ImageInfo imageinfo;
		try {
			imageinfo = persistService.fetchImageById(imageActionObject
					.getImage_id());
		} catch (DbException e) {
			log.error(
					"Error Fetching Image :: "
							+ imageActionObject.getImage_id(), e);
			updateImageActionState(Constants.ERROR,
					"Error while injecting policy");
			throw new DirectorException("Error Fetching Image :: "
					+ imageActionObject.getImage_id(), e);
		}
		String imageLocation = imageinfo.getLocation();
		// Fetch the policy and write to a location. Move to common

		TrustPolicy trustPolicy;
		try {
			trustPolicy = persistService.fetchActivePolicyForImage(imageActionObject
					.getImage_id());
		} catch (DbException e) {
			log.error(
					"Error Fetching trustPolicy for :: "
							+ imageActionObject.getImage_id(), e);
			updateImageActionState(Constants.ERROR,
					"Error while injecting policy");
			throw new DirectorException("Error Fetching trustPolicy for :: "
					+ imageActionObject.getImage_id(), e);
		}
		if (trustPolicy == null) {
			log.error("No trust policy for image : "
					+ imageActionObject.getImage_id());
			updateImageActionState(Constants.ERROR,
					"Error while injecting policy");
			throw new DirectorException("No trust policy for image : "
					+ imageActionObject.getImage_id());
		}
		log.info("Image has a trust policy");

		String newLocation = imageLocation + imageActionObject.getImage_id()
				+ File.separator;
		try {
			DirectorUtil.callExec("mkdir -p " + newLocation);
		} catch (IOException e) {
			log.error("Error In Creating trust Policy Location", e);
			updateImageActionState(Constants.ERROR,
					"Error while injecting policy");
			throw new DirectorException(
					"Error In Creating trust Policy Location", e);
		}

		log.info("Inject Policy : Inject Policy start");
		String trustPolicyName = "trustpolicy.xml";

		FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
		fileUtilityOperation.createNewFile(newLocation + trustPolicyName);
		fileUtilityOperation.writeToFile(newLocation + trustPolicyName,
				trustPolicy.getTrust_policy());

		String display_name = trustPolicy.getDisplay_name();
		int tagStart = display_name.lastIndexOf(":") + 1;
		String repo = display_name.substring(0, tagStart - 1);
		String tag = display_name.substring(tagStart);
		log.info("Injecting Policy @ /trust and creating new image with :: " + repo + ":" +  tag);
		try {
			DirectorUtil.executeCommandInExecUtil(Constants.dockerPolicyInject,
					imageinfo.repository, imageinfo.tag + "_source",
					repo, tag, newLocation);
		} catch (IOException e) {
			log.error("Error In Injecting Policy", e);
			updateImageActionState(Constants.ERROR,
					"Error while injecting policy");
			fileUtilityOperation.deleteFileOrDirectory(new File(newLocation));
			throw new DirectorException("Error In Injecting Policy", e);
		}
		log.info("Policy Injected Successfully");
	}

	/**
	 * Task name identifier
	 */
	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_INJECT_POLICY;
	}
}
