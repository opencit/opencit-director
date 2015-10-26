/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.quartz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ImageActionObject;
import com.intel.director.async.ImageActionExecutor;
import com.intel.director.async.ImageActionTaskFactory;
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

	public ImageActionPoller() {

	}

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
		log.info("*** Executing poller");
		List<ImageActionObject> incompleteImageActionObjects = new ArrayList<ImageActionObject>();
		// Fetching the 10 records from DB
		try {
			incompleteImageActionObjects = imageActionImpl
					.searchIncompleteImageAction(5);
			log.debug("*** Got " + incompleteImageActionObjects.size()
					+ " to process");
		} catch (DbException e) {
			// TODO Auto-generated catch block
			log.error("searchIncompleteImageAction failed", e);
		}
		// iterate over list of image action objects
		log.debug("incompleteImageActionObjects::"
				+ incompleteImageActionObjects);

		for (ImageActionObject imageActionObj : incompleteImageActionObjects) {
			log.info("ImageAction in poller: " + imageActionObj.getId());
			List<ImageActionActions> imageActions = imageActionObj.getAction();
			log.info("ImageAction task list in poller: " + imageActionObj.getAction().size());
			// iterate over tasks from each image action object
			ImageActionActions imageActionsActions = getNextActionToBeExecuted(imageActions);
			if (imageActionsActions == null) {
				continue;
			}
			log.info("ImageAction: " + imageActionObj.getId());
			// for (ImageActionActions imageActionsActions : imageActions) {
			String task_name = imageActionsActions.getTask_name();
			String imageStore = imageActionsActions.getStorename();
			log.info("task_name: " + task_name);

			ImageActionTask task = ImageActionTaskFactory.getImageActionTask(
					task_name, imageStore);
			log.info("task : " + task.getTaskName());
			task.setImageActionObject(imageActionObj);
			task.setTaskAction(imageActionsActions);
			ImageActionExecutor.submitTask(task);
			log.info("Submitted task : " + task.getTaskName());

			// }
		}
		log.debug("Running the poller " + new Date());

	}

	/**
	 * Find the next task to be executed from the array of tasks
	 * 
	 * @param list
	 *            List of tasks to be executed
	 * @return The task to be executed. This task is in INCOMPLETE status
	 */
	private ImageActionActions getNextActionToBeExecuted(
			List<ImageActionActions> list) {
		ImageActionActions ret = null;
		for (ImageActionActions imageActionsActions : list) {
			log.info("imageActionsActions.getStatus() : "+imageActionsActions.getStatus());
			if (imageActionsActions.getStatus().equals(Constants.INCOMPLETE)) {
				ret = imageActionsActions;
				break;
			}
		}
		return ret;
	}
}
