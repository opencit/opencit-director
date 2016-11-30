package com.intel.director.async.task;

import java.util.List;

import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionTask;
import com.intel.director.async.ImageActionTaskFactory;
import com.intel.director.common.Constants;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.quartz.ImageActionPoller;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class ExecuteActions implements Runnable {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ExecuteActions.class);

	IPersistService persistService = new DbServiceImpl();

	ImageActionObject imageActionObj;

	public ExecuteActions(ImageActionObject imageActionObject) {
		this.imageActionObj = imageActionObject;
	}

	@Override
	public void run() {
		List<ImageActionTask> imageActions = imageActionObj.getActions();
		log.info("Number of tasks for this image action ("
				+ imageActionObj.getId() + "): "
				+ imageActionObj.getActions().size());
		// iterate over tasks from each image action object
		for (int i = 0; i < imageActions.size(); i++) {
			ImageActionTask taskToBeExecuted = getNextActionToBeExecuted(imageActions);
			if (taskToBeExecuted == null) {
				markImageActionWithError("Unable to find task");
				ImageActionPoller.removeEntryFromUniqueImageActionlist(imageActionObj.getImage_id());
				ImageActionPoller.removeEntryFromImageActionCountMap(imageActionObj.getId());
				return;
			}
			String task_name = taskToBeExecuted.getTask_name();
			log.info("Task being executed: " + task_name);

			ImageActionAsync task = null;
			try {
				task = ImageActionTaskFactory.getImageActionTask(
						task_name);
				if(task == null){
					markImageActionWithError(task);
					ImageActionPoller.removeEntryFromUniqueImageActionlist(imageActionObj.getImage_id());
					ImageActionPoller.removeEntryFromImageActionCountMap(imageActionObj.getId());
					return;
				}
				task.setImageActionObject(imageActionObj);
				task.setTaskAction(taskToBeExecuted);
				//Set the image id and the policy id in the customProperties map
				task.init();
			} catch (DirectorException e) {
				log.error("unable to get a task for {}",task_name);
			}
			
			log.info("Task instance from factory : " + task.getTaskName());
			if(!task.run()){
				markImageActionWithError(task);
				ImageActionPoller.removeEntryFromUniqueImageActionlist(imageActionObj.getImage_id());
				ImageActionPoller.removeEntryFromImageActionCountMap(imageActionObj.getId());
				log.info("Exiting because "+task.getTaskName()+" did not execute successfully");
				log.info("Processing stopped for action: "+imageActionObj.getId());
				return;
			}
			
			if(i == (imageActions.size() - 1)){
				imageActionObj.setCurrent_task_name(null);
				imageActionObj.setCurrent_task_status(null);
				ImageActionPoller.removeEntryFromImageActionCountMap(imageActionObj.getId());
				ImageActionPoller.removeEntryFromUniqueImageActionlist(imageActionObj.getImage_id());
			}
		}
	}

	private void markImageActionWithError(ImageActionAsync failedTask) {
		imageActionObj.setStatus(Constants.ERROR);
		imageActionObj.setDetails("Failed to execute task: "
				+ failedTask.getTaskName());
		imageActionObj.setCurrent_task_status(Constants.ERROR);

		List<ImageActionTask> actions = imageActionObj.getActions();
		if (failedTask != null) {
			failedTask.taskAction.setMessage("Failed to complete the task");
			for (ImageActionTask imageActionTask : actions) {
				if (imageActionTask.getTask_name().equals(
						failedTask.getTaskName())) {
					imageActionTask.setStatus(Constants.ERROR);
					break;
				}
			}
		}
		try {
			persistService.updateImageAction(imageActionObj);
		} catch (DbException e) {
			log.error("Erorr in ExecuteActionTask", e);
		}
		ImageActionPoller.removeEntryFromImageActionCountMap(imageActionObj
				.getId());
	}


	private void markImageActionWithError(String details) {
		imageActionObj.setStatus(Constants.ERROR);
		imageActionObj.setDetails(details);
		try {
			persistService.updateImageAction(imageActionObj);
		} catch (DbException e) {
			log.error("Erorr in ExecuteActionTask",e);
		}
		ImageActionPoller.removeEntryFromImageActionCountMap(imageActionObj.getId());
	}
	/**
	 * Find the next task to be executed from the array of tasks
	 * 
	 * @param list
	 *            List of tasks to be executed
	 * @return The task to be executed. This task is in INCOMPLETE status
	 */
	private ImageActionTask getNextActionToBeExecuted(
			List<ImageActionTask> list) {
		ImageActionTask ret = null;
		for (ImageActionTask imageActionsActions : list) {
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
