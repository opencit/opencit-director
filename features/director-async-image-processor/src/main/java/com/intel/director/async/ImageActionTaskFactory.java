package com.intel.director.async;

import com.github.dnault.xmlpatch.internal.Log;
import com.intel.director.async.task.CreateDockerTarTask;
import com.intel.director.async.task.CreateTarTask;
import com.intel.director.async.task.EncryptImageTask;
import com.intel.director.async.task.ImageActionAsyncTask;
import com.intel.director.async.task.UploadDockerHubTask;
import com.intel.director.async.task.UploadImageTask;
import com.intel.director.async.task.UploadPolicyTask;
import com.intel.director.async.task.UploadTarTask;
import com.intel.director.common.Constants;

public class ImageActionTaskFactory {

	/**
	 * Get the task name class depending on the identifier passed
	 * 
	 * @param taskName identifier to initialize the task
	 * @param imageStoreName THe store name used for upload tasks
	 * @return
	 */
	public static ImageActionAsyncTask getImageActionTask(String taskName,
			String imageStoreName) {
		ImageActionAsyncTask actionTask = null;
		switch (taskName) {
		case Constants.TASK_NAME_CREATE_TAR:
			actionTask = new CreateTarTask();
			break;
		case Constants.TASK_NAME_ENCRYPT_IMAGE:
			actionTask = new EncryptImageTask();
			break;
		case Constants.TASK_NAME_UPLOAD_IMAGE:
			actionTask = new UploadImageTask(imageStoreName);
			break;
		case Constants.TASK_NAME_UPLOAD_POLICY:
			actionTask = new UploadPolicyTask(imageStoreName);
			break;			
		case Constants.TASK_NAME_CREATE_DOCKER_TAR:
			actionTask = new CreateDockerTarTask();
			break;
		case Constants.TASK_NAME_UPLOAD_TO_HUB:
			actionTask = new UploadDockerHubTask();
			break;
		case Constants.TASK_NAME_UPLOAD_TAR:
			actionTask = new UploadTarTask(imageStoreName);
		}	
		
		return actionTask;
	}
}
