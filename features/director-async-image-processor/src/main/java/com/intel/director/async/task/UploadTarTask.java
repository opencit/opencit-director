/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.intel.director.common.Constants;


/**
 * Task to upload the tar of image and policy
 * 
 * @author GS-0681
 */
public class UploadTarTask extends UploadTask {
	public UploadTarTask() {
		super();
		uploadType = Constants.TASK_NAME_UPLOAD_TAR;
	}

	public UploadTarTask(String imageStoreName) {
		super(imageStoreName);
	}

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadTarTask.class);

	/**
	 * Entry method for running task. Checks if the previous task was completed
	 */
	@Override
	public boolean run() {
		boolean runFlag = false;
		if (previousTasksCompleted(taskAction.getTask_name())) {
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				super.initProperties();
				runFlag  = runUploadTarTask();
			}
		}
		return runFlag;

	}

	/**
	 * Actual implementation of the task
	 */
	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_UPLOAD_TAR;
	}

	public boolean runUploadTarTask() {
		boolean runFlag = false;
		log.debug("Inside runUploadTarTask for ::"
				+ imageActionObject.getImage_id());
		try {
			String imageLocation = imageInfo.getLocation();
			JAXBContext jaxbContext = JAXBContext.newInstance(com.intel.mtwilson.trustpolicy.xml.TrustPolicy.class);
			Unmarshaller unmarshaller = (Unmarshaller) jaxbContext
					.createUnmarshaller();
			String tarName = "";
			if(imageInfo.getImage_deployments().equalsIgnoreCase(Constants.DEPLOYMENT_TYPE_DOCKER)){
				imageProperties.put(Constants.NAME, imageInfo.getRepository() + ":" + imageInfo.getTag());			
			} else {	
				imageProperties.put(Constants.NAME, trustPolicy.getDisplay_name());
			}
			tarName = trustPolicy.getDisplay_name() + ".tar";
			String policyXml= trustPolicy.getTrust_policy();
			log.debug("Inside Run Upload Tar task policyXml::"+policyXml);
			StringReader reader = new StringReader(policyXml);
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = (com.intel.mtwilson.trustpolicy.xml.TrustPolicy) unmarshaller.unmarshal(reader);
			String glanceId=policy.getImage().getImageId();
			log.info("Inside Run Upload Tar task glanceId::"+glanceId);
			imageProperties.put(Constants.GLANCE_ID, glanceId);
			
			imageProperties.put(Constants.MTWILSON_TRUST_POLICY_LOCATION, "glance_image_tar");
			String tarLocation = imageLocation+imageActionObject.getImage_id()+File.separator;
			log.debug("runUploadTarTask tarname::" + tarName
					+ " ,tarLocation ::" + tarLocation);
			content = new File(tarLocation + tarName);
			boolean parentRunStatus = super.run();
			runFlag = parentRunStatus;
			//Cleanup of folder
			File uuidFolder = new File(tarLocation);
			File[] listFiles = uuidFolder.listFiles();
			boolean deleteFileFlag = true;
			for (File file : listFiles) {
				log.info("Deleteing file "+file.getAbsolutePath()+" after successful upload");
				if(!file.delete()){
					log.info("!!!!! File could not be deleted");
					deleteFileFlag = false;
				}
				
			}
			if(deleteFileFlag){
				deleteFileFlag = uuidFolder.delete();
				log.info("Is folder deleted == "+deleteFileFlag);
			}else{
				log.info("UUID : "+tarLocation +" cannot be cleaned up");
			}
			String encImageFileName = imageInfo.getLocation()+File.separator+imageInfo.getImage_name() + "-enc";
			File encImageFile = new File(encImageFileName);
			if(encImageFile.exists()){
				encImageFile.delete();
			}
			return runFlag;
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(
					"Upload tar task fail for::"
							+ imageActionObject.getImage_id(), e);

		}
		return runFlag;

	}

}
