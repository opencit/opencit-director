package com.intel.director.async.task;

import java.util.List;

import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionTask;
import com.intel.director.async.ImageActionTaskFactory;
import com.intel.director.common.Constants;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class ExecuteActionsTask implements Runnable {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ExecuteActionsTask.class);

	ImageActionObject imageActionObj;
	IPersistService persistService = new DbServiceImpl();

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
				markImageActionWithError("No task found for execution");
				return;
			}
			String task_name = taskToBeExecuted.getTask_name();
			String imageStore = taskToBeExecuted.getStorename();
			log.info("Task being executed: " + task_name);

			ImageActionAsyncTask task = ImageActionTaskFactory.getImageActionTask(
					task_name, imageStore);
			if(task == null){
				markImageActionWithError("Null task");
				return;
			}
			log.info("Task instance from factory : " + task.getTaskName());
			task.setImageActionObject(imageActionObj);
			task.setTaskAction(taskToBeExecuted);
			if(!task.run()){
				log.info("Exiting because "+task.getTaskName()+" did not execute successfully");
				log.info("Processing stopped for action: "+imageActionObj.getId());
				markImageActionWithError("Exiting because "+task.getTaskName()+" did not execute successfully");
				return;
			}
			
			if(i == (imageActions.size() - 1)){
				imageActionObj.setCurrent_task_name(null);
				imageActionObj.setCurrent_task_status(null);
			}
		}
	}
	

	private void markImageActionWithError(String details) {
		imageActionObj.setStatus(Constants.ERROR);
		imageActionObj.setDetails(details);
		try {
			persistService.updateImageAction(imageActionObj);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
