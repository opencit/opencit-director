/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ArtifactUploadService;
import com.intel.director.service.impl.ArtifactUploadServiceImpl;

/**
 * 
 * Task to upload image to image store
 * 
 * @author Aakash
 */

public class UploadImageForPolicyTask extends UploadImageTask {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadImageForPolicyTask.class);

	public UploadImageForPolicyTask() throws DirectorException {
		super();
	}

	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPLOAD_IMAGE_FOR_POLICY;
	}
	
	public String fetchUploadImageName(){
		return trustPolicy.getDisplay_name();
	}

	@Override
	public String fetchUploadImageId() {
		log.info("Inside UploadImageForPolicyTask fetchUploadImageId()");
		ArtifactUploadService artifactUploadService = new ArtifactUploadServiceImpl();
		ImageStoreUploadTransferObject imageUploadByImageId = artifactUploadService
				.fetchImageUploadByImageId(imageInfo.getId());
		String glanceId = imageInfo.getId();
		customProperties.put(Constants.GLANCE_ID, imageInfo.getId());
		customProperties.put(Constants.MTWILSON_TRUST_POLICY_LOCATION, "NA");
		if (imageUploadByImageId != null) {
			String uuid = DirectorUtil.fetchIdforUpload(trustPolicy);
			log.info("Inside UploadImageForPolicyTask fetchUploadImageId() uuid from policy for upload::" + uuid);
			glanceId = uuid;
		}
		return glanceId;
	}

}
