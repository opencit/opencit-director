/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.quartz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ImageActionObject;
import com.intel.director.async.ImageActionExecutor;
import com.intel.director.async.ImageActionTaskFactory;
import com.intel.director.async.task.ImageActionTask;
import com.intel.director.service.ImageActionService;
import com.intel.director.service.impl.ImageActionImpl;
import com.intel.mtwilson.director.db.exception.DbException;


/**
 * 
 * @author GS-0681
 */
public class ImageActionPoller implements Job {
	
	public ImageActionPoller() {
	
	}

	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		
		//Fetch the 10 records from DB
		//Iterate over them
		//Fetch the ImageActionObject
		//Get the first task to work on
		//Inastantiate the task by ImageActionTaskFactory.getImageActionTask
		//Set the ImageActionObject
		//Submit the task to the executor
		ImageActionService imageActionImpl = new ImageActionImpl();
		List<ImageActionObject> incompleteImageActionObjects = new ArrayList<ImageActionObject>();
		//Fetching the 10 records from DB
		try {
			incompleteImageActionObjects = imageActionImpl.searchIncompleteImageAction(5);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// iterate over list of image action objects
		for( ImageActionObject imageActionObj: incompleteImageActionObjects){
			List<ImageActionActions> imageActions = imageActionObj.getAction();
			// iterate over tasks from each image action object
			for(ImageActionActions imageActionsActions: imageActions){
				String task_name = imageActionsActions.getTask_name();
				String imageStore=imageActionsActions.getStorename();
				ImageActionTask task = ImageActionTaskFactory.getImageActionTask(task_name,imageStore);
				task.setImageActionObject(imageActionObj);
				task.setTaskAction(imageActionsActions);
				ImageActionExecutor.submitTask(task);
				
			}
		}
		System.out.println("Running the poller "+new Date());
			
	}
	
	public static void main(String[] args){
		// uncomment following lines of code to add ImageAction data into DB;
		/*
		ImageActionImpl imageActionImpl = new ImageActionImpl();
		ImageActionObject imgActionObj = new ImageActionObject();
		List<ImageActionActions> imgActionActionsList = new ArrayList<ImageActionActions>();
		ImageActionActions imgActionsObj = new ImageActionActions();
		imgActionsObj.setTask_name(Constants.TASK_NAME_UPLOAD_TAR);
		imgActionsObj.setStatus(Constants.INCOMPLETE);
		imgActionActionsList.add(imgActionsObj);
		
		imgActionObj.setImage_id("img33-eeaf3-efdeqfeq");
		imgActionObj.setAction_size(1);
		imgActionObj.setAction_count(1);
		imgActionObj.setAction_completed(0);
		imgActionObj.setAction_size_max(4);
		imgActionObj.setAction(imgActionActionsList);
		
		try {
			ImageActionObject test = imageActionImpl.createImageAction(imgActionObj);
		} catch (DbException e) {
			e.printStackTrace();
		}
		*/
		
		
	}
}
