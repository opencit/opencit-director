/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.quartz;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.intel.director.api.ImageActionObject;
import com.intel.director.async.ImageActionExecutor;
import com.intel.director.async.task.ExecuteActionsTask;
import com.intel.director.common.Constants;
import com.intel.director.service.ImageActionService;
import com.intel.director.service.impl.ImageActionImpl;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

/**
 * Poller that runs every one minute. Check the entries in the Action table
 * 
 * @author Siddharth
 */
public class ImageActionPoller {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageActionPoller.class);
	public static Map<String, Integer> imageActionToRunCountMap = new HashMap();
	IPersistService persistService = new DbServiceImpl();
	ImageActionService imageActionImpl = new ImageActionImpl();
	public static Set<String> imageIdsInProcess = new HashSet<>();


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
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss");
		log.info("*** Executing poller at : " + dateFormat.format(new Date()));
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
					+ imageActionObj.getActions().size());
			log.info("Current status of image action object:"
					+ imageActionObj.getCurrent_task_status());
			
			if(imageActionToRunCountMap.containsKey(imageActionObj.getId())){
				Integer currentCount = imageActionToRunCountMap.get(imageActionObj.getId());
				if(currentCount < 10){
					addEntryFromImageActionCountMap(imageActionObj.getId(), ++currentCount);
				}else{
					//mark error
					log.info("mark image action {} to error after 10 tries", imageActionObj.getId());
					imageIdsInProcess.remove(imageActionObj.getImage_id());
					imageActionObj.setCurrent_task_status(Constants.ERROR);
					try {
						persistService.updateImageAction(imageActionObj);
						

						continue;
					} catch (DbException e) {
						log.error("Error in Poller",e);
					}	
					finally{
						removeEntryFromImageActionCountMap(imageActionObj.getId());
					}
				}
			}else{				
				addEntryFromImageActionCountMap(imageActionObj.getId(), 1);
			}

			if (imageIdsInProcess.contains(imageActionObj.getImage_id())) {
				log.info("Image already in process. Skipping");
				continue;
			}

			imageIdsInProcess.add(imageActionObj.getImage_id());

			if (imageActionObj.getCurrent_task_status() != null
					&& imageActionObj.getCurrent_task_status().equals(
							Constants.INCOMPLETE)) {
				ExecuteActionsTask task = new ExecuteActionsTask(imageActionObj);
				ImageActionExecutor.submitTask(task);
				log.info("Submitted task for ExecuteActions for id: "
						+ imageActionObj.getId());
			}

		}

	}
	
	
	public static void removeEntryFromImageActionCountMap(String imageActionId){
		imageActionToRunCountMap.remove(imageActionId);
		log.info("Number of elements in map after remove {}", imageActionToRunCountMap.size());
	}

	public static void removeEntryFromUniqueImageActionlist(String imageId){
		imageIdsInProcess.remove(imageId);
	}
	


	public static void addEntryFromImageActionCountMap(String imageActionId, Integer count){
		imageActionToRunCountMap.put(imageActionId, count);
		log.info("Elements in map after add/update {}:{}", imageActionId, count);
		log.info("Number of elements in map: {}", imageActionToRunCountMap.size());
	}
	
	

	private void markImageActionWithError(ImageActionObject  imageActionObj) {
		imageActionObj.setCurrent_task_status(Constants.ERROR);
		try {
			persistService.updateImageAction(imageActionObj);
		} catch (DbException e) {
			log.error("Erorr in ExecuteActionTask",e);
		}
		ImageActionPoller.removeEntryFromImageActionCountMap(imageActionObj.getId());
	}
}
