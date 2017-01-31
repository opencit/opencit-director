/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang.StringUtils;

import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ImageActionService;
import com.intel.director.service.impl.ImageActionImpl;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

/**
 * Class to Injecting Policy To Docker Image
 * 
 * @author GS-0681
 */
public class InjectPolicy extends ImageActionAsync {

    public InjectPolicy() throws DirectorException {
	super();
    }

    public static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InjectPolicy.class);

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
	    log.debug("Running Inject Policy task for " + imageActionObject.getImage_id());
	    imageActionService.updateImageActionState(imageActionObject, taskAction, getTaskName(),
		    Constants.IN_PROGRESS, "Started");
	    try {
		injectPolicy();
		imageActionService.updateImageActionState(imageActionObject, taskAction, getTaskName(),
			Constants.COMPLETE, "Inject Policy complete");
		runFlag = true;
	    } catch (DirectorException e) {
		log.error("Error in InjectPolicyTask", e);
		imageActionService.updateImageActionState(imageActionObject, taskAction, getTaskName(), Constants.ERROR,
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
	    imageinfo = persistService.fetchImageById(imageActionObject.getImage_id());
	} catch (DbException e) {
	    log.error("Error Fetching Image :: " + imageActionObject.getImage_id(), e);
	    updateImageActionState(Constants.ERROR, "Error while injecting policy");
	    throw new DirectorException("Error Fetching Image :: " + imageActionObject.getImage_id(), e);
	}
	String imageLocation = imageinfo.getLocation();
	// Fetch the policy and write to a location. Move to common

	TrustPolicy trustPolicy;
	try {
	    trustPolicy = persistService.fetchActivePolicyForImage(imageActionObject.getImage_id());
	} catch (DbException e) {
	    log.error("Error Fetching trustPolicy for :: " + imageActionObject.getImage_id(), e);
	    updateImageActionState(Constants.ERROR, "Error while injecting policy");
	    throw new DirectorException("Error Fetching trustPolicy for :: " + imageActionObject.getImage_id(), e);
	}
	if (trustPolicy == null) {
	    log.error("No trust policy for image : " + imageActionObject.getImage_id());
	    updateImageActionState(Constants.ERROR, "Error while injecting policy");
	    throw new DirectorException("No trust policy for image : " + imageActionObject.getImage_id());
	}
	log.info("Image has a trust policy");

	String newLocation = imageLocation + imageActionObject.getImage_id() + File.separator;
	try {
	    DirectorUtil.callExec("mkdir -p " + newLocation);
	} catch (IOException e) {
	    log.error("Error In Creating trust Policy Location", e);
	    updateImageActionState(Constants.ERROR, "Error while injecting policy");
	    throw new DirectorException("Error In Creating trust Policy Location", e);
	}

	log.info("Inject Policy : Inject Policy start");
	String trustPolicyName = "trustpolicy.xml";

	FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
	fileUtilityOperation.createNewFile(newLocation + trustPolicyName);
	fileUtilityOperation.writeToFile(newLocation + trustPolicyName, trustPolicy.getTrust_policy());

	String display_name = trustPolicy.getDisplay_name();
	int tagStart = display_name.lastIndexOf(":") + 1;
	String repo = display_name.substring(0, tagStart - 1); 
	String tag = display_name.substring(tagStart);
	if (StringUtils.isBlank(tag) || StringUtils.isBlank(repo) || StringUtils.isBlank(imageinfo.repository)
		|| StringUtils.isBlank(imageinfo.tag)) {
	    throw new DirectorException("Tag or repo is invalid. Tag=" + tag + " and repo=" + repo);
	}

	log.info("Injecting Policy @ /trust and creating new image with :: " + repo + ":" + tag);
	// Create container
	log.info("Creating container");
	String command = "docker run -id " + imageinfo.repository + ":" + imageinfo.tag + Constants.SOURCE_TAG;
	log.info("Command to run the container: {}", command);
	String containerId = executeCommand(command);
	if (containerId.endsWith("\n")) {
	    containerId = containerId.replaceAll("\n", "");
	}
	log.info("Created container: {}", containerId);
	if (StringUtils.isBlank(containerId)) {
	    log.error("Error In creating container");
	    updateImageActionState(Constants.ERROR, "Error while injecting policy");
	    throw new DirectorException("Error In creating container");
	}
	command = "docker exec " + containerId + " mkdir -p /trust";
	log.info("Creating trust folder to store the policy");
	executeCommandWithExitCode(command);
	log.info("Created trust folder to store the policy");

	log.info("Stop the container");
	command = "docker stop " + containerId;
	executeCommandWithExitCode(command);
	log.info("Stopped the container");

	log.info("Before copying the policy");
	String trustPolicyPath = newLocation + "trustpolicy.xml";
	command = "docker cp \"" + trustPolicyPath + "\" \"" + containerId + ":/trust/trustpolicy.xml\"";
	executeCommandWithExitCode(command);
	log.info("After copying the policy");

	log.info("Before committing the changes");
	command = "docker commit " + containerId + " \"" + repo + ":" + tag + "\"";
	String imageId = executeCommand(command);
	log.info("After committing the changes, image id = {}", imageId);

	if (StringUtils.isBlank(imageId)) {
	    log.error("Error In committing the policy");
	    updateImageActionState(Constants.ERROR, "Error while injecting policy");
	    throw new DirectorException("Error In committing the policy");
	}

	log.info("Before rmi the container");
	command = "docker rm " + containerId;
	executeCommandWithExitCode(command);
	log.info("After rmi the container");

	log.info("Policy Injected Successfully");
    }

    /**
     * Task name identifier
     */
    @Override
    public String getTaskName() {
	return Constants.TASK_NAME_INJECT_POLICY;
    }

    private String executeCommand(String command) throws DirectorException {
	Result result;
	try {
	    result = ExecUtil.executeQuoted("/bin/sh", "-c", command);
	} catch (ExecuteException e) {
	    throw new DirectorException("Error In executing command: " + command, e);
	} catch (IOException e) {
	    throw new DirectorException("Error In executing command", e);
	}
	if (result.getStderr() != null && StringUtils.isNotEmpty(result.getStderr())) {
	    log.error(result.getStderr());
	}
	return result.getStdout();
    }

    private void executeCommandWithExitCode(String command) throws DirectorException {
	Result result;
	try {
	    result = ExecUtil.executeQuoted("/bin/sh", "-c", command);
	} catch (ExecuteException e) {
	    throw new DirectorException("Error In executing command: " + command, e);
	} catch (IOException e) {
	    throw new DirectorException("Error In executing command", e);
	}

	int exitCode = result.getExitCode();
	if (exitCode != 0) {
	    throw new DirectorException("Error executing command: " + command);
	}
    }

}
