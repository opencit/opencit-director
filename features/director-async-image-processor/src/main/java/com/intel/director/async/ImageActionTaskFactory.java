package com.intel.director.async;

import com.intel.director.async.task.CreateTarTask;
import com.intel.director.async.task.EncryptImageTask;
import com.intel.director.async.task.ImageActionTask;
import com.intel.director.async.task.UploadImageTask;
import com.intel.director.async.task.UploadPolicyTask;
import com.intel.director.async.task.UploadTarTask;
import com.intel.director.common.Constants;

public class ImageActionTaskFactory {

	public static ImageActionTask getImageActionTask(String taskName,
			String imageStoreName) {
		ImageActionTask actionTask = null;
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
		case Constants.TASK_NAME_UPLOAD_TAR:
			actionTask = new UploadTarTask(imageStoreName);
		}	
		
		return actionTask;
	}
}
