package com.intel.director.async.task;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionTask;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageActionService;
import com.intel.director.service.impl.ImageActionImpl;
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
public abstract class ImageActionAsyncTask {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadTarTask.class);

	public ImageActionObject imageActionObject;
	public ImageActionTask taskAction;
	public IPersistService persistService;
	public Map<String, Object> customProperties;
	public ImageInfo imageInfo;
	public TrustPolicy trustPolicy;
	public ImageActionService imageActionService ;

	public ImageActionAsyncTask()  {
		customProperties = new HashMap<String, Object>();
		persistService = new DbServiceImpl();
		imageActionService = new ImageActionImpl();
	}
	
	
	
	public void init() throws DirectorException {
		try {
			imageInfo = persistService.fetchImageById(imageActionObject
					.getImage_id());
			if(imageInfo == null){
				return;
			}
			try {
				if(imageInfo
						.getTrust_policy_id()!=null){
				trustPolicy = persistService.fetchPolicyById(imageInfo
						.getTrust_policy_id());
				}
			} catch (DbException e) {
				log.error("Unable to fetch policy for the image id {}",
						imageActionObject.getImage_id(), e);
			}
			initProperties();
		} catch (DbException e) {
			log.error("Unable to fetch image for the id {}",
					imageActionObject.getImage_id(), e);
			throw new DirectorException("Invalid image id for the upload task "
					+ imageActionObject.getImage_id(), e);
		}
		
	}


	public ImageActionAsyncTask(ImageActionObject imageActionObject) {
		super();
		this.imageActionObject = imageActionObject;
	}

	public ImageActionObject getImageActionObject() {
		return imageActionObject;
	}

	public void setImageActionObject(ImageActionObject imageActionObject) {
		this.imageActionObject = imageActionObject;
	}

	public ImageActionTask getTaskAction() {
		return taskAction;
	}

	public void setTaskAction(ImageActionTask taskAction) {
		this.taskAction = taskAction;
	}

	public abstract String getTaskName();

	/**
	 * Convert the json array into ImageActionActions for an ImageAction
	 * 
	 * @return Objects containing list of tasks to be executed
	 */
	protected ImageActionTask getImageActionTaskFromArray() {
		ImageActionTask iat = null;
		for (ImageActionTask imageActionTask : imageActionObject.getActions()) {
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
		for (ImageActionTask imageActionTask : imageActionObject.getActions()) {
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

	public abstract boolean run();

	public void initProperties() {
		String diskFormat = null, containerFormat = null;

		switch (imageInfo.image_format) {
		case "ami":
			diskFormat = "ami";
			containerFormat = "ami";
			break;
		case "qcow2":
			diskFormat = "qcow2";
			containerFormat = "bare";
			break;
		case "vhd":
			diskFormat = "vhd";
			containerFormat = "bare";
			break;
		case "raw":
			diskFormat = "raw";
			containerFormat = "bare";
			break;
		case "tar":
			diskFormat = "raw";
			containerFormat = "docker";
			break;
		}

		customProperties.put(Constants.DISK_FORMAT, diskFormat);
		customProperties.put(Constants.CONTAINER_FORMAT, containerFormat);
		customProperties.put(Constants.VISIBILITY, "public");
		customProperties.put(ImageInfo.class.getName(), imageInfo);
		if (trustPolicy != null) {
			customProperties.put(TrustPolicy.class.getName(), trustPolicy);
		}
	}
	
	public void updateImageActionState(String status, String details) {
		Calendar cal = Calendar.getInstance();
		imageActionObject.setDatetime(cal );
		
		imageActionService.updateImageActionState(imageActionObject,
				taskAction, getTaskName(), status, details);
	}


}
