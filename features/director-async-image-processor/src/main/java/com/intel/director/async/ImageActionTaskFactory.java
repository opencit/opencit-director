package com.intel.director.async;

import com.intel.director.async.task.CreateTar;
import com.intel.director.async.task.EncryptImage;
import com.intel.director.async.task.ImageActionAsync;
import com.intel.director.async.task.InjectPolicy;
import com.intel.director.async.task.RecreatePolicy;
import com.intel.director.async.task.UpdateMetadata;
import com.intel.director.async.task.UploadImageForPolicy;
import com.intel.director.async.task.UploadImage;
import com.intel.director.async.task.UploadPolicy;
import com.intel.director.async.task.UploadTar;
import com.intel.director.common.Constants;
import com.intel.director.common.exception.DirectorException;

public class ImageActionTaskFactory {

	/**
	 * Get the task name class depending on the identifier passed
	 * 
	 * @param taskName
	 *            identifier to initialize the task
	 * @return
	 * @throws DirectorException 
	 */
	public static ImageActionAsync getImageActionTask(String taskName) throws DirectorException {
		ImageActionAsync actionTask = null;
		switch (taskName) {
		case Constants.TASK_NAME_CREATE_TAR:
			actionTask = new CreateTar();
			break;
		case Constants.TASK_NAME_ENCRYPT_IMAGE:
			actionTask = new EncryptImage();
			break;
		case Constants.TASK_NAME_UPLOAD_IMAGE:
			actionTask = new UploadImage();
			break;
		case Constants.TASK_NAME_UPLOAD_POLICY:
			actionTask = new UploadPolicy();
			break;
		case Constants.TASK_NAME_INJECT_POLICY:
			actionTask = new InjectPolicy();
			break;
		case Constants.TASK_NAME_RECREATE_POLICY:
			actionTask = new RecreatePolicy();
			break;
		case Constants.TASK_NAME_UPDATE_METADATA:
			actionTask = new UpdateMetadata();
			break;
		case Constants.TASK_NAME_UPLOAD_TAR:
			actionTask = new UploadTar();
			break;
		case Constants.TASK_NAME_UPLOAD_IMAGE_FOR_POLICY:
			actionTask = new UploadImageForPolicy();
		
		}

		return actionTask;
	}
}
