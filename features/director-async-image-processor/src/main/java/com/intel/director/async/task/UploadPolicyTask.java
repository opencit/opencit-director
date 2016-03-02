/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.intel.director.api.ImageStoreDetailsTransferObject;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.images.exception.DirectorException;
import com.intel.mtwilson.director.db.exception.DbException;

/**
 * Task to upload the policy to image store
 * 
 * @author GS-0681
 */
public class UploadPolicyTask extends GenericUploadTask {
	public static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadPolicyTask.class);
	
	
	public UploadPolicyTask() throws DirectorException {
		super();
	}

	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPLOAD_POLICY;
	}

	/**
	 * Entry method for uploading policy
	 */
	@Override
	public boolean run() {
		boolean runFlag = false;
		if (previousTasksCompleted(taskAction.getTask_name())) {
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				runFlag = runUploadPolicyTask();
			}
		}
		return runFlag;
	}

	/**
	 * Actual implementation of policy upload task
	 */
	public boolean runUploadPolicyTask() {
		boolean runFlag = false;
		File file = null;
		//try {

			String imageLocation = imageInfo.getLocation();
			if (trustPolicy != null) {

				String trustPolicyName = "trustpolicy.xml" ;

				String trustPolicyLocation = imageLocation;
				new FileUtilityOperation().writeToFile(trustPolicyLocation + trustPolicyName, trustPolicy.getTrust_policy());
				file = new File(trustPolicyLocation + trustPolicyName);
			}
			String storeId=taskAction.getStoreId();
			String containerName=null;
			//ImageStoreSettings imageStoreSettings=persistService.fetchImageStoreSettingsById(storeId);
			ImageStoreTransferObject imgStoreTransferObject = null;
			try {
				imgStoreTransferObject = persistService.fetchImageStorebyId(storeId);
			} catch (DbException e) {
				log.error("Error in fetchImageStorebyId",e);
				updateImageActionState(Constants.ERROR,"Error in uploading policy");
				return false;
			}
			Collection<ImageStoreDetailsTransferObject> imgStoreDetails=imgStoreTransferObject.image_store_details;
			log.info("ASYNCH UPLOADER, Inside upload Policy Task, imgStoreDetails::"+imgStoreDetails+" for storeid::"+storeId);
			for(ImageStoreDetailsTransferObject detailsRecord:imgStoreDetails){
				if(Constants.SWIFT_CONTAINER_NAME.equals(detailsRecord.getKey())){
					containerName=detailsRecord.getValue();
					break;
				}
			}
			if(StringUtils.isBlank(containerName)){
				log.error("containerName is null during UploadPolicyTask");
				updateImageActionState(Constants.ERROR,
						"containerName is null during UploadPolicyTask");
				return false;
			}
			String glanceId = DirectorUtil.fetchIdforUpload(trustPolicy);
		
		
			customProperties.put(Constants.SWIFT_CONTAINER_NAME, containerName);
			customProperties.put(Constants.SWIFT_OBJECT_NAME, glanceId);
			customProperties.put(Constants.UPLOAD_TO_IMAGE_STORE_FILE, file);
			log.info("ASYNCH UPLOADER, Inside upload Policy Task, customProperties"+customProperties);
			runFlag = super.run();

		if(!runFlag){			
			updateImageActionState(Constants.ERROR,
					"Error in  Uploading Policy");
		}
		return runFlag;

	}

}
