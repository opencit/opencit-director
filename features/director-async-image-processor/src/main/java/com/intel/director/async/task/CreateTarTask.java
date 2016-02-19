/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.service.ImageService;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.director.util.TdaasUtil;

/**
 * Class to create a TAR from the image and policy
 * 
 * @author GS-0681
 */
public class CreateTarTask extends ImageActionAsyncTask {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(CreateTarTask.class);

	/**
	 * 
	 * Entry method to run the task
	 * 
	 */
	@Override
	public boolean run() {

		// Call to update the task status
		boolean runFlag = false;
		if (previousTasksCompleted(taskAction.getTask_name())) {
			log.info("Previous task was completed which was Create Tar");
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				log.info("Create Tar is In Progress");
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				log.info("Create Tar is being executed");
				runFlag = runCreateTarTask();
			}
		}
		return runFlag;

	}

	/**
	 * After the check in the run method this task creates a tar from an image
	 * and policy
	 */
	public boolean runCreateTarTask() {
		String trustPolicyName;
		boolean runFlag = false;

		try {

			log.info("Inside runCreateTartask for ::"
					+ imageActionObject.getImage_id());
			ImageInfo imageinfo = persistService
					.fetchImageById(imageActionObject.getImage_id());
			String trust_policy_id = imageinfo.getTrust_policy_id();
			log.info("POLICY :: {}", trust_policy_id);
			TrustPolicy trustPolicyApi = persistService.fetchPolicyById(trust_policy_id);
			if (trustPolicyApi == null) {
				log.error("No trust policy for image : "
						+ imageActionObject.getImage_id());
				return false;
			}

			com.intel.mtwilson.trustpolicy.xml.TrustPolicy trustPolicy = TdaasUtil.getPolicy(trustPolicyApi.getTrust_policy());
			
			String imageLocation = imageinfo.getLocation();
			// Fetch the policy and write to a location. Move to common
			
			ImageStoreUploadOrderBy  imgOrder= new ImageStoreUploadOrderBy();
			imgOrder.setImgStoreUploadFields(ImageStoreUploadFields.DATE);		
			imgOrder.setOrderBy(OrderByEnum.DESC);
			boolean regenPolicy = false;
			List<ImageStoreUploadTransferObject> imageUploads = persistService.fetchImageUploads(imgOrder);
			for(ImageStoreUploadTransferObject imageTransfer : imageUploads){
				if(imageTransfer.getImg().getId().equals(imageActionObject.getImage_id())){
					log.info("image upload entry found for the image id {}", imageTransfer.getImage_uri());
					log.info("policy's image id : "+trustPolicy.getImage().getImageId());
					if(imageTransfer.getImage_uri().contains(trustPolicy.getImage().getImageId())){
						log.info("Found an image in glance");
						regenPolicy = true;
						break;
					}
				}
			}
			
		
			if(regenPolicy){
				log.info("Regen for image {}",imageActionObject.getImage_id());
				ImageInfo image = persistService.fetchImageById(imageActionObject.getImage_id());
				log.info("Image has policy id {}", image.getTrust_policy_id());

				ImageService imageService = new ImageServiceImpl();
				MountImageResponse mountImageResponse = imageService.mountImage(imageActionObject.getImage_id(), "admin");
				log.info("Mounting complete");
				if(mountImageResponse.getMounted_by_user_id() != null){
					String createTrustPolicyId = imageService.createTrustPolicy(imageActionObject.getImage_id(), trust_policy_id);
					log.info("Regen complete. Updating image");
					image.setTrust_policy_id(createTrustPolicyId);
					image.setEdited_date(new Date());
					persistService.updateImage(image);
					log.info("Updating image complete with new policy id {}", createTrustPolicyId);
					trustPolicyApi = persistService.fetchPolicyById(createTrustPolicyId);				
					trustPolicy = TdaasUtil.getPolicy(trustPolicyApi.getTrust_policy()); 
					MountImageResponse unmountImageResponse = imageService.unMountImage(imageActionObject.getImage_id(), "admin");
				}else{
					log.info("Exception ");
					return false;
				}
			}
			

			String imageName = imageinfo.getImage_name();
			log.info("Create Tar has a trust policy");


			if (trustPolicy != null && trustPolicy.getEncryption() != null) {
				log.info("Create Tar has a trust policy which is encrypted");
				imageName = imageinfo.getImage_name() + "-enc";
			}

			String newLocation = imageLocation
					+ imageActionObject.getImage_id() + File.separator;
			DirectorUtil.callExec("mkdir -p " + newLocation);
			DirectorUtil.createCopy(imageLocation + imageName, newLocation
					+ imageName);
			

			log.info("Create Tar : Create policy file start");
			trustPolicyName = "trustpolicy.xml";
		
			FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
			fileUtilityOperation.createNewFile(newLocation + trustPolicyName);
			fileUtilityOperation.writeToFile(newLocation + trustPolicyName, trustPolicyApi.getTrust_policy());
			String tarLocation = newLocation;
			String tarName = trustPolicyApi.getDisplay_name() + ".tar";
			log.info("Create Tar ::tarName::" + tarName + " tarLocation::"
					+ tarLocation + " trustPolicyName::" + trustPolicyName
					+ " imageLocation::" + imageLocation);
			DirectorUtil.createTar(newLocation, imageName, trustPolicyName,
					tarLocation, tarName);
			log.info("Create Tar : commplete");

			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
			runFlag = true;
		} catch (Exception e) {
			log.error(
					"CreateTar task failed for"
							+ imageActionObject.getImage_id(), e);
			updateImageActionState(Constants.ERROR, e.getMessage());
		} 
		return runFlag;
	}
	

	/**
	 * Returns the task name
	 */
	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_CREATE_TAR;
	}

}
