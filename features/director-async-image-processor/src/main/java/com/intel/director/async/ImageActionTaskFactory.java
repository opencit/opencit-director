package com.intel.director.async;

import com.intel.director.async.task.CreateTarTask;
import com.intel.director.async.task.EncryptImageTask;
import com.intel.director.async.task.ImageActionAsyncTask;
import com.intel.director.async.task.InjectPolicyTask;
import com.intel.director.async.task.RecreatePolicyTask;
import com.intel.director.async.task.UpdateMetadataTask;
import com.intel.director.async.task.UploadImageForPolicyTask;
import com.intel.director.async.task.UploadImageTask;
import com.intel.director.async.task.UploadPolicyTask;
import com.intel.director.async.task.UploadTarTask;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;

public class ImageActionTaskFactory {

	/**
	 * Get the task name class depending on the identifier passed
	 * 
	 * @param taskName
	 *            identifier to initialize the task
	 * @return
	 * @throws DirectorException 
	 */
	public static ImageActionAsyncTask getImageActionTask(String taskName) throws DirectorException {
		ImageActionAsyncTask actionTask = null;
		switch (taskName) {
		case Constants.TASK_NAME_CREATE_TAR:
			actionTask = new CreateTarTask();
			break;
		case Constants.TASK_NAME_ENCRYPT_IMAGE:
			actionTask = new EncryptImageTask();
			break;
		case Constants.TASK_NAME_UPLOAD_IMAGE:
			actionTask = new UploadImageTask();
			break;
		case Constants.TASK_NAME_UPLOAD_POLICY:
			actionTask = new UploadPolicyTask();
			break;
		case Constants.TASK_NAME_INJECT_POLICY:
			actionTask = new InjectPolicyTask();
			break;
		case Constants.TASK_NAME_RECREATE_POLICY:
			actionTask = new RecreatePolicyTask();
			break;
		case Constants.TASK_NAME_UPDATE_METADATA:
			actionTask = new UpdateMetadataTask();
			break;
		case Constants.TASK_NAME_UPLOAD_TAR:
			actionTask = new UploadTarTask();
			break;
		case Constants.TASK_NAME_UPLOAD_IMAGE_FOR_POLICY:
			actionTask = new UploadImageForPolicyTask();
		
		}

		return actionTask;
	}
}
