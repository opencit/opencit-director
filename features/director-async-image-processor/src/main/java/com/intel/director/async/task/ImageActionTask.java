package com.intel.director.async.task;

import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ImageActionObject;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public abstract class ImageActionTask implements Runnable{

	public ImageActionObject imageActionObject;
	public ImageActionActions taskAction;
	IPersistService persistService;
	
	public ImageActionTask(){
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
	
	protected ImageActionActions getImageActionTaskFromArray(){
		ImageActionActions  iat = null;
		for (ImageActionActions imageActionTask : imageActionObject.getAction()) {
			if (imageActionTask.getTask_name().equals(getTaskName())) {
				iat = imageActionTask;
				break;
			}
		}
		return iat;

	}
	
	protected void updateImageActionState(String status, String details){
		taskAction.setStatus(status);
		taskAction.setExecutionDetails(details);
		imageActionObject.setCurrent_task_name(getTaskName());
		imageActionObject.setCurrent_task_status(taskAction.getStatus() );
		
		try {
			persistService.updateImageAction(imageActionObject);
		} catch (DbException e3) {
			e3.printStackTrace();
		}
	}
}
