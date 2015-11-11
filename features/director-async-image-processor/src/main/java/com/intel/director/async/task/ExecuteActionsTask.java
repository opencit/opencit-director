package com.intel.director.async.task;

import java.util.List;

import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ImageActionObject;
import com.intel.director.async.ImageActionTaskFactory;
import com.intel.director.common.Constants;

public class ExecuteActionsTask implements Runnable {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ExecuteActionsTask.class);

	ImageActionObject imageActionObj;

	public ExecuteActionsTask(ImageActionObject imageActionObject) {
		this.imageActionObj = imageActionObject;
	}

	@Override
	public void run() {
		List<ImageActionActions> imageActions = imageActionObj.getAction();
		log.info("Number of tasks for this image action ("
				+ imageActionObj.getId() + "): "
				+ imageActionObj.getAction().size());
		// iterate over tasks from each image action object
		for (ImageActionActions imageActionsActions : imageActions) {
			ImageActionActions taskToBeExecuted = getNextActionToBeExecuted(imageActions);
			if (taskToBeExecuted == null) {
				return;
			}
			String task_name = taskToBeExecuted.getTask_name();
			String imageStore = taskToBeExecuted.getStorename();
			log.info("Task being executed: " + task_name);

			ImageActionTask task = ImageActionTaskFactory.getImageActionTask(
					task_name, imageStore);
			log.info("Task instance from factory : " + task.getTaskName());
			task.setImageActionObject(imageActionObj);
			task.setTaskAction(taskToBeExecuted);
			if(!task.run()){
				log.info("Exiting because "+task.getTaskName()+" did not execute successfully");
				log.info("Processing stopped for action: "+imageActionObj.getId());
				return;
			}
		}
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
			log.info("imageActionsActions.getStatus() : "
					+ imageActionsActions.getStatus());
			if (imageActionsActions.getStatus().equals(Constants.INCOMPLETE)) {
				ret = imageActionsActions;
				break;
			}
		}
		return ret;
	}

}
