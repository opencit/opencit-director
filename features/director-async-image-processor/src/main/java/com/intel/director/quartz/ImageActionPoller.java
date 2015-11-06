/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.quartz;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ImageActionObject;
import com.intel.director.async.ImageActionExecutor;
import com.intel.director.async.ImageActionTaskFactory;
import com.intel.director.async.task.ExecuteActionsTask;
import com.intel.director.async.task.ImageActionTask;
import com.intel.director.common.Constants;
import com.intel.director.service.ImageActionService;
import com.intel.director.service.impl.ImageActionImpl;
import com.intel.mtwilson.director.db.exception.DbException;

/**
 * Poller that runs every one minute. Check the entries in the Action table
 * 
 * @author Siddharth
 */
public class ImageActionPoller {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageActionPoller.class);

	/**
	 * Fetches the entries from the MW_ACTION table for processing
	 */
	public void execute() {

		// Fetch the 10 records from DB
		// Iterate over them
		// Fetch the ImageActionObject
		// Get the first task to work on
		// Inastantiate the task by ImageActionTaskFactory.getImageActionTask
		// Set the ImageActionObject
		// Submit the task to the executor
		ImageActionService imageActionImpl = new ImageActionImpl();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		log.info("*** Executing poller at : "+dateFormat.format(new Date()));
		List<ImageActionObject> incompleteImageActionObjects = new ArrayList<ImageActionObject>();
		// Fetching the 10 records from DB
		try {
			incompleteImageActionObjects = imageActionImpl
					.searchIncompleteImageAction(5);
			log.debug("*** Got " + incompleteImageActionObjects.size()
					+ " to process");
		} catch (DbException e) {
			// TODO Handle Error
			log.error("searchIncompleteImageAction failed", e);
		}
		// iterate over list of image action objects
		log.debug("incompleteImageActionObjects::"
				+ incompleteImageActionObjects);

		for (ImageActionObject imageActionObj : incompleteImageActionObjects) {
			log.info("ImageAction Object in poller (" + imageActionObj.getId()
					+ "): Number of tasks: "
					+ imageActionObj.getAction().size());
			log.info("Current status of image action object:"+imageActionObj.getCurrent_task_status());
			if (imageActionObj.getCurrent_task_status() != null && imageActionObj.getCurrent_task_status().equals(Constants.INCOMPLETE)) {
				ExecuteActionsTask task = new ExecuteActionsTask(
						imageActionObj);
				ImageActionExecutor.submitTask(task);
				log.info("Submitted task for ExecuteActions for id: "
						+ imageActionObj.getId());
			}

		}

	}
}
