package com.intel.director.async.task;

import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ImageActionObject;
import com.intel.director.common.Constants;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

/**
 * 
 * Superclass for all image actions
 * 
 * @author GS-0681
 * 
 */
public abstract class ImageActionTask  {

	public ImageActionObject imageActionObject;
	public ImageActionActions taskAction;
	IPersistService persistService;

	public ImageActionTask() {
		persistService = new DbServiceImpl();
	}

	public ImageActionTask(ImageActionObject imageActionObject) {
		this.imageActionObject = imageActionObject;
	}

	public ImageActionObject getImageActionObject() {
		return imageActionObject;
	}

	public void setImageActionObject(ImageActionObject imageActionObject) {
		this.imageActionObject = imageActionObject;
	}

	public ImageActionActions getTaskAction() {
		return taskAction;
	}

	public void setTaskAction(ImageActionActions taskAction) {
		this.taskAction = taskAction;
	}

	public abstract String getTaskName();

	/**
	 * Convert the json array into ImageActionActions for an ImageAction
	 * 
	 * @return Objects containing list of tasks to be executed
	 */
	protected ImageActionActions getImageActionTaskFromArray() {
		ImageActionActions iat = null;
		for (ImageActionActions imageActionTask : imageActionObject.getAction()) {
			if (imageActionTask.getTask_name().equals(getTaskName())) {
				iat = imageActionTask;
				break;
			}
		}
		return iat;

	}

	/**
	 * Checks if the previous task of the passed task is completed
	 * 
	 * @param taskName
	 *            task which is to be executed
	 * @return true if the previous task is COMPLETED
	 */
	protected boolean previousTasksCompleted(String taskName) {
		boolean completed = true;
		for (ImageActionActions imageActionTask : imageActionObject.getAction()) {
			if (imageActionTask.getTask_name().equals(getTaskName())) {
				completed = true;
				break;
			} else {
				if (!Constants.COMPLETE.equals(imageActionTask.getStatus())) {
					completed = false;
					break;
				}
			}
		}
		return completed;
	}

	/**
	 * Method to update the image action status after task execution
	 * 
	 * 
	 * @param status
	 *            Status of the task execution
	 * @param details
	 *            details of the task execution. Contains error details in case
	 *            of error.
	 */
	protected void updateImageActionState(String status, String details) {
		String currentTaskStatus = status;
		synchronized (this) {
			taskAction.setStatus(status);
			taskAction.setExecutionDetails(details);
			int count = imageActionObject.getAction_completed();
			int action_completed;
			if (Constants.COMPLETE.equals(status)) {
				action_completed = count + 1;
			} else {
				if (Constants.ERROR.equals(status)) {
					currentTaskStatus += " : " + details;
				}
				action_completed = count;
			}
			imageActionObject.setCurrent_task_status(currentTaskStatus);
			
			imageActionObject.setAction_completed(action_completed);
			imageActionObject.setCurrent_task_name(getTaskName());

			try {
				persistService.updateImageAction(imageActionObject);
			} catch (DbException e3) {
				e3.printStackTrace();
			}
		}
	}
	
	
	protected void updateImageActionContentSent(int sent, int size) {
		synchronized (this) {

			
			imageActionObject.setAction_size(sent);
			imageActionObject.setAction_size_max(size);

			try {
				persistService.updateImageAction(imageActionObject);
			} catch (DbException e3) {
				e3.printStackTrace();
			}
		}
	}
	
	public abstract boolean run();

}
