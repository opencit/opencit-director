package com.intel.director.async.task;

import java.util.List;

import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionTask;
import com.intel.director.async.ImageActionTaskFactory;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.quartz.ImageActionPoller;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class ExecuteActionsTask implements Runnable {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ExecuteActionsTask.class);

	IPersistService persistService = new DbServiceImpl();

	ImageActionObject imageActionObj;

	public ExecuteActionsTask(ImageActionObject imageActionObject) {
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
				markImageActionWithError("Unable to find a task for execution");
			}
			String task_name = taskToBeExecuted.getTask_name();
			log.info("Task being executed: " + task_name);

			ImageActionAsyncTask task = null;
			try {
				task = ImageActionTaskFactory.getImageActionTask(
						task_name);
				task.setImageActionObject(imageActionObj);
				task.setTaskAction(taskToBeExecuted);
				//Set the image id and the policy id in the customProperties map
				task.init();
			} catch (DirectorException e) {
				log.error("unable to get a task for {}",task_name);
			}
			if(task == null){
				markImageActionWithError("Unable to find a task for execution "+task_name);
			}
			log.info("Task instance from factory : " + task.getTaskName());
			if(!task.run()){
				markImageActionWithError("Unable to execute task "+task.getTaskName());
				log.info("Exiting because "+task.getTaskName()+" did not execute successfully");
				log.info("Processing stopped for action: "+imageActionObj.getId());
				return;
			}
			
			if(i == (imageActions.size() - 1)){
				imageActionObj.setCurrent_task_name(null);
				imageActionObj.setCurrent_task_status(null);
				ImageActionPoller.removeEntryFromImageActionCountMap(imageActionObj.getId());
			}
		}
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
