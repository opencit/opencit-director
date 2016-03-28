package com.intel.director.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.api.ArtifactStoreDetails;
import com.intel.director.api.ImageActionHistoryResponse;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionRequest;
import com.intel.director.api.ImageActionTask;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageActionFields;
import com.intel.director.api.ui.ImageActionFilter;
import com.intel.director.api.ui.ImageActionOrderBy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadFilter;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ArtifactUploadService;
import com.intel.director.service.FetchActionsService;
import com.intel.director.service.ImageActionService;
import com.intel.director.service.ImageService;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public  class ImageActionImpl implements ImageActionService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageActionImpl.class);
	private boolean isEncrypted = false;
	@Autowired
	public IPersistService persistService;
	
	public FetchActionsService fetchActionService;
	public ImageService imageService;
	public ImageActionImpl() {
		persistService = new DbServiceImpl();
		imageService = new ImageServiceImpl();
	}
	
	public ImageActionImpl(FetchActionsService fetchService) {
		persistService = new DbServiceImpl();
		this.fetchActionService=fetchService;
		imageService = new ImageServiceImpl();
	}
	

	
	public List<ImageActionObject> searchIncompleteImageAction(
			Integer count_of_action) throws DbException {

		List<ImageActionObject> actionObjectIncomplete = new ArrayList<ImageActionObject>();
		List<ImageActionObject> allActionObject = persistService
				.searchByAction();
		Collections.sort(allActionObject);
		for (ImageActionObject img : allActionObject) {
			if ((img.getAction_completed() < img.getAction_count())  
					&& !(img.getCurrent_task_status() != null && (img
							.getCurrent_task_status().startsWith(
									Constants.ERROR) || img
									.getCurrent_task_status().equalsIgnoreCase(Constants.OBSOLETE)))) {
				actionObjectIncomplete.add(img);
			}
		}

		if (count_of_action == null) {
			return actionObjectIncomplete;
		}

		if (count_of_action > actionObjectIncomplete.size()) {
			return actionObjectIncomplete;
		} else {
			return actionObjectIncomplete.subList(0, count_of_action);
		}

	}

	public ImageActionObject createImageAction(
			ImageActionObject imageActionObject) throws DbException {

		return persistService.createImageAction(imageActionObject);
	}

	public void updateImageAction(String id, ImageActionObject imageActionObject)
			throws DbException {
		persistService.updateImageAction(id, imageActionObject);

	}

	public List<ImageActionObject> getdata() throws DbException {

		return persistService.searchByAction();
	}

	@Override
	public ImageActionObject createImageAction(
			ImageActionRequest imageActionRequest) throws DirectorException {
		

		ImageInfo imageInfo = null;
		try {
			imageInfo = persistService
					.fetchImageById(imageActionRequest.image_id);
		} catch (DbException e1) {
			log.error("Image with id :" + imageActionRequest.image_id
					+ " does'nt exist", e1);
			throw new DirectorException("Image with id :"
					+ imageActionRequest.image_id + " does'nt exist", e1);
		}
		if(imageInfo==null){
			throw new DirectorException("Image does not exist");
		}
		
		ArtifactUploadService artifactUploadService = new ArtifactUploadServiceImpl();
		
		

		TrustPolicy trustPolicy = null;
		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = null;
		if (imageInfo.getTrust_policy_id() != null) {

			try {
				trustPolicy = persistService.fetchPolicyById(imageInfo
						.getTrust_policy_id());

				try {
					policy = TdaasUtil.getPolicy(trustPolicy.getTrust_policy());
				} catch (JAXBException e1) {
					log.error("Unable to convert policy string into object  :"
							+ trustPolicy.getTrust_policy(), e1);
					throw new DirectorException(
							"Unable to convert policy string into object  :"
									+ trustPolicy.getTrust_policy(), e1);
				}
			} catch (DbException e1) {
				log.error(
						"Unable to fetch trust policy by id :"
								+ imageInfo.getTrust_policy_id(), e1);
				throw new DirectorException(
						"Unable to fetch trust policy by id :"
								+ imageInfo.getTrust_policy_id(), e1);
			}
		}
		if (policy != null && policy.getEncryption() != null) {
			isEncrypted = true;
		}
		
		Map<String,String> artifactStoreKeyMap= new HashMap<String,String>();
		artifactStoreKeyMap.put(Constants.ARTIFACT_IMAGE, Constants.STORE_IMAGE);
		artifactStoreKeyMap.put(Constants.ARTIFACT_POLICY, Constants.STORE_POLICY);
		artifactStoreKeyMap.put(Constants.ARTIFACT_TAR, Constants.STORE_TAR);
		artifactStoreKeyMap.put(Constants.ARTIFACT_DOCKER, Constants.STORE_DOCKER);
		artifactStoreKeyMap.put(Constants.ARTIFACT_DOCKER_IMAGE, Constants.STORE_DOCKER);
		artifactStoreKeyMap.put(Constants.ARTIFACT_DOCKER_WITH_POLICY, Constants.STORE_DOCKER);

		List<ArtifactStoreDetails> artifactStoreList=imageActionRequest.getArtifact_store_list();
		List<ImageActionTask> imageActionTaskList = new ArrayList<ImageActionTask>();
		Map<String,String> artifactStoreIdMap= new HashMap<String,String>();
		boolean isCorrectActionList=false;
		if (artifactStoreList.size() == 2) {
			if ((artifactStoreList.get(0).getArtifact_name()
					.equals(Constants.ARTIFACT_IMAGE)
					&& artifactStoreList.get(1).getArtifact_name()
							.equals(Constants.ARTIFACT_POLICY))) {
				isCorrectActionList=true;
				artifactStoreIdMap.put(Constants.STORE_IMAGE, artifactStoreList.get(0).getImage_store_id());
				artifactStoreIdMap.put(Constants.STORE_POLICY, artifactStoreList.get(1).getImage_store_id());
				imageActionTaskList.addAll(fetchActionService.getActionsByDeploymentArtifact(Constants.ARTIFACT_IMAGE_WITH_POLICY,artifactStoreIdMap,isEncrypted));
				
			}
		} else if (artifactStoreList.size() == 1) {
			isCorrectActionList=true;
			if(Constants.ARTIFACT_POLICY.equals(artifactStoreList.get(0).getArtifact_name())){
				String uploadId=DirectorUtil.fetchIdforUpload(trustPolicy);
				ImageStoreUploadOrderBy imgOrder = new ImageStoreUploadOrderBy();
				imgOrder.setImgStoreUploadFields(ImageStoreUploadFields.DATE);
				imgOrder.setOrderBy(OrderByEnum.DESC);
				ImageStoreUploadFilter imgUpFilter = new ImageStoreUploadFilter();
				imgUpFilter.setStoreArtifactId(uploadId);
				imgUpFilter.setEnableDeletedCheck(true);
				imgUpFilter.setDeleted(false);
				List<ImageStoreUploadTransferObject> fetchImageUploads = null;
				try {
					fetchImageUploads = persistService.fetchImageUploads(
							imgUpFilter, imgOrder);
					boolean uploadImageAgain=true;
					log.debug("ImageActionImpl, ARTIFACT_POLICY action, fetchingImageUploads for uploadId::"+uploadId);
					if ((fetchImageUploads != null && fetchImageUploads.size() > 0)) {
						String storeId= fetchImageUploads.get(0).getStoreId();
						if(StringUtils.isNotBlank(storeId)){
							uploadImageAgain=false;
							artifactStoreIdMap.put(Constants.STORE_IMAGE, storeId);	
						}
						
					}
					if(uploadImageAgain){
						throw new DirectorException("Please upload image before uploading policy");
					}
				} catch (DbException e) {
					log.error("Error fetching image uploads by storageArtifactId {}", uploadId, e);
					return null;
				}
				
				artifactStoreIdMap.put(artifactStoreKeyMap.get(artifactStoreList.get(0).getArtifact_name()), artifactStoreList.get(0).getImage_store_id());
				imageActionTaskList.addAll(fetchActionService.getActionsByDeploymentArtifact(artifactStoreList.get(0).getArtifact_name(),artifactStoreIdMap,isEncrypted));	
				
			}else if(Constants.ARTIFACT_IMAGE.equals(artifactStoreList.get(0).getArtifact_name()) && (imageInfo.getTrust_policy_id()!=null)){
				artifactStoreIdMap.put(artifactStoreKeyMap.get(artifactStoreList.get(0).getArtifact_name()), artifactStoreList.get(0).getImage_store_id());
				imageActionTaskList.addAll(fetchActionService.getActionsByDeploymentArtifact(Constants.ARTIFACT_IMAGE_WHEN_POLICY_EXISTS,artifactStoreIdMap,isEncrypted));
			}else{
			artifactStoreIdMap.put(artifactStoreKeyMap.get(artifactStoreList.get(0).getArtifact_name()), artifactStoreList.get(0).getImage_store_id());
			imageActionTaskList.addAll(fetchActionService.getActionsByDeploymentArtifact(artifactStoreList.get(0).getArtifact_name(),artifactStoreIdMap,isEncrypted));
			}
		}
		
		if(!isCorrectActionList){
			throw new DirectorException("Image action task list provided is not allowed");
		}
		
		ImageActionObject imageAction = new ImageActionObject();
		imageAction.setImage_id(imageActionRequest.getImage_id());
		imageAction.setAction_completed(0);
		imageAction.setAction_count(imageActionTaskList.size());
		imageAction.setCurrent_task_name(imageActionTaskList.get(0).getTask_name());
		imageAction.setCurrent_task_status(Constants.INCOMPLETE);
		imageAction.setAction_size(0);
		imageAction.setAction_size_max(0);


		if (imageActionTaskList.size() != 0) {
			imageAction.setActions(imageActionTaskList);
		}
		
		try {
			return persistService.createImageAction(imageAction);
		} catch (DbException e) {
			log.error("Error while creating image action" + e);
			throw new DirectorException("Error while creating image action", e);
		}
	
	}

	
	

	@Override
	public void deleteImageAction(String actionId) throws DirectorException {
		try {
			persistService.deleteImageActionById(actionId);
		} catch (DbException e) {
			log.error("Error in deleting image action" , e);
			throw new DirectorException("Error in deleting image action " , e);
		}
	}

	@Override
	public ImageActionObject fetchImageAction(String actionId)
			throws DirectorException {
		try {
			return persistService.fetchImageActionById(actionId);
		} catch (DbException e) {
			log.error("Error while fetching image action" + e);
			throw new DirectorException("Error while fetching image action", e);
		}
	}
	
	public void updateImageActionState(ImageActionObject imageActionObject, ImageActionTask taskAction, String taskName, String status, String details) {
		String currentTaskStatus = status;
			taskAction.setStatus(status);

			int count = imageActionObject.getAction_completed();
			int action_completed;
			if (Constants.COMPLETE.equals(status)) {
				action_completed = count + 1;
			} else {
				/*if (Constants.ERROR.equals(status)) {
					  currentTaskStatus += " : " + details;
					
				}*/
				action_completed = count;
			}
			if(StringUtils.isNotBlank(details)){
			taskAction.setMessage(details);
			}
			imageActionObject.setCurrent_task_status(currentTaskStatus);

			imageActionObject.setAction_completed(action_completed);
			imageActionObject.setCurrent_task_name(taskName);

			try {
				persistService.updateImageAction(imageActionObject);
			} catch (DbException e3) {
				e3.printStackTrace();
			}
	}

	public void updateImageActionContentSent(ImageActionObject imageActionObject, int sent, int size) {
			imageActionObject.setAction_size(sent);
			imageActionObject.setAction_size_max(size);

			try {
				persistService.updateImageAction(imageActionObject);
			} catch (DbException e3) {
				e3.printStackTrace();
			}
	}

	@Override
	public List<String> validateCreationOfImageAction(String imageId,
			List<ArtifactStoreDetails> stores) throws DirectorException {
		List<String> errors = new ArrayList<String>();
		
		if(stores == null ){
			errors.add("No stores selected for upload");
			return errors;
		}
		
		if(stores.size() == 1) {
			ArtifactStoreDetails artifactStoreDetails = stores.get(0);			
			if(!(ValidationUtil.isValidWithRegex(artifactStoreDetails.artifact_name, Constants.ARTIFACT_POLICY))){
				return errors;
			}
		}else{
			return errors;
		}
	
		ArtifactUploadService artifactUploadService = new ArtifactUploadServiceImpl();
		String uploadVar = null;
		ImageInfo imageById = null;
		try {
			imageById = persistService.fetchImageById(imageId);
			if(imageById == null){				
				throw new DirectorException("No image found for id "+imageId);
			}
		} catch (DbException e1) {
			log.error("Error fetching image", e1);
			throw new DirectorException("Error fetching image", e1);
		}
		
		String trustPolicyId = imageById.getTrust_policy_id();
		if(trustPolicyId == null){
			throw new DirectorException("No policy associated with the image");
		}
		TrustPolicy fetchPolicyById = null;
		try {
			fetchPolicyById = persistService.fetchPolicyById(trustPolicyId);
			if(fetchPolicyById == null){
				throw new DirectorException("No policy found for id "+trustPolicyId);
			}
		} catch (DbException e1) {
			throw new DirectorException("No policy found for id "+trustPolicyId, e1);
		}
		
		
		//Now check for the upload var
		com.intel.mtwilson.trustpolicy.xml.TrustPolicy trustPolicy = null;
		try {
			trustPolicy = TdaasUtil.getPolicy(fetchPolicyById.getTrust_policy());
		} catch (JAXBException e1) {
			throw new DirectorException("Error converting polocy string to object", e1);
		}
		
		String dekUrl = trustPolicy.getEncryption()!=null ? trustPolicy.getEncryption().getKey().getURL() : "";
		
		uploadVar = DirectorUtil.computeUploadVar(trustPolicy.getImage().getImageId(),dekUrl);
		
		
		List<ImageStoreUploadTransferObject> fetchImageUploadsByUploadVariable=null;
		try {
		
			fetchImageUploadsByUploadVariable= artifactUploadService.fetchImageUploadsByUploadVariable(uploadVar);
			log.info("Inside validateCreationOfImageAction, seraching image uploads for uploadva::"+uploadVar);
			if(!(fetchImageUploadsByUploadVariable != null && fetchImageUploadsByUploadVariable.size() > 0)){
				errors.add("No image has been uploaded to an image store for the trust policy. Please choose the "+ Constants.ARTIFACT_IMAGE_WITH_POLICY +" option");
			}
		} catch (DirectorException e) {
			throw new DirectorException("unable to fetch image upload entries for upload var : "+uploadVar, e);
		}
		return errors;
		
	}
	
	public List<ImageActionHistoryResponse> getImageActionHistory(String imageId)
			throws DirectorException {

		List<ImageActionHistoryResponse> imageActionResponseList = new ArrayList<ImageActionHistoryResponse>();
		List<ImageActionObject> imageActionObjectList = new ArrayList<ImageActionObject>();
		ImageActionFilter imageActionFilter = new ImageActionFilter();
		ImageActionOrderBy imageActionOrderBy = new ImageActionOrderBy();
		imageActionOrderBy.setImageActionFields(ImageActionFields.DATE);
		imageActionOrderBy.setOrderBy(OrderByEnum.DESC);
		imageActionFilter.setImage_id(imageId);

		try {
			imageActionObjectList = persistService.fetchImageActions(
					imageActionFilter, imageActionOrderBy);
		} catch (DbException e) {
			throw new DirectorException(
					"unable to fetch image upload entries for upload imageActionFilter : "
							+ imageActionFilter, e);
		}
		for (ImageActionObject imageActionObject : imageActionObjectList) {
			if(!Constants.OBSOLETE.equalsIgnoreCase(imageActionObject.getCurrent_task_status())){
			imageActionResponseList
					.add(fetchActionHistoryResponse(imageActionObject));
			}
		}
		return imageActionResponseList;

	}
	
	public ImageActionHistoryResponse fetchActionHistoryResponse(
			ImageActionObject imageActionObject) {
		String artifactName = null;
		List<ImageActionTask> imageActiontaskList = imageActionObject
				.getActions();
		ImageInfo image =null;
		try {
			 image = imageService.fetchImageById(imageActionObject.getImage_id());
			
		} catch (DirectorException e) {
			log.error("fetchActionHistoryResponse ,Unable to fetch image");
			
		}
		boolean isDockerImage=false;
		if(Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(image.getImage_deployments())){
			isDockerImage=true;
		}
		ImageActionHistoryResponse imageActionResponse= new ImageActionHistoryResponse();
		String previousTaskname=null;
		String displayMessage=null;
		for (ImageActionTask imageActionTask : imageActiontaskList) {
			if (imageActionTask.getTask_name().equals(
					Constants.TASK_NAME_UPLOAD_TAR)) {
				if(isDockerImage){
					artifactName = Constants.ARTIFACT_DOCKER_IMAGE_WITH_POLICY_DISPLAY_NAME;
				}else{
					artifactName = Constants.ARTIFACT_TAR_DISPLAY_NAME;
				}
				
				break;
			}
			if(previousTaskname!=null && previousTaskname.equals(Constants.TASK_NAME_UPLOAD_IMAGE_FOR_POLICY) && imageActionTask.getTask_name().equals(Constants.TASK_NAME_UPLOAD_POLICY)){
				artifactName = Constants.ARTIFACT_IMAGE_WITH_POLICY_DISPLAY_NAME;
				//displayMessage=imageActionTask.getMessage();
				break;
			}
			if (imageActionTask.getTask_name().equals(
					Constants.TASK_NAME_UPLOAD_IMAGE_FOR_POLICY)) {
				artifactName = Constants.ARTIFACT_IMAGE;
				
			}
			if (imageActionTask.getTask_name().equals(
					Constants.TASK_NAME_UPLOAD_POLICY)) {
				artifactName = Constants.ARTIFACT_POLICY;
				//displayMessage=imageActionTask.getMessage();
				
			}
			if (imageActionTask.getTask_name().equals(
					Constants.TASK_NAME_UPLOAD_IMAGE)) {
				if(isDockerImage){
					artifactName = Constants.ARTIFACT_DOCKER_IMAGE_DISPLAY_NAME;
				}else{
					artifactName = Constants.ARTIFACT_IMAGE;
				}

			}
			previousTaskname=imageActionTask.getTask_name();
		}

		ImageStoreTransferObject imageStoreDTO = null;
		List<ImageActionTask> imageActionTaskList = imageActionObject
				.getActions();
		Set<String> storeNames = new HashSet<String>();

		for (ImageActionTask imageActiontask : imageActionTaskList) {

			if (StringUtils.isNotBlank(imageActiontask.getStoreId()) && !Constants.TASK_NAME_UPDATE_METADATA.equals(imageActiontask.getTask_name())) {
				
				String storeId = imageActiontask.getStoreId();
				
				try {
					imageStoreDTO = persistService.fetchImageStorebyId(storeId);
					if (StringUtils.isNotBlank(imageStoreDTO.getName())) {
						storeNames.add(imageStoreDTO.getName());
					}

				} catch (DbException e) {
					log.error("No store exists for id {}", storeId);
				}
			}
			if (imageActiontask.getTask_name().equals(
					Constants.TASK_NAME_UPDATE_METADATA)) {
				displayMessage = imageActiontask.getMessage();
				
			}
			if(Constants.ERROR.equals(imageActiontask.getStatus())){
				displayMessage=imageActiontask.getMessage();
			}
			
		}

		if(imageActionObject.getDatetime()!=null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");    
			imageActionResponse.setDatetime(sdf.format(imageActionObject.getDatetime().getTime()));
			
		}
		String commaSeperatedStoreNames = StringUtils.join(storeNames, ", ");
		imageActionResponse.setArtifactName(artifactName);
		
		imageActionResponse.setId(imageActionObject.getId());
		if(StringUtils.isNotBlank(displayMessage) ){
			imageActionResponse.setExecutionStatus(imageActionObject.getCurrent_task_status()+":-"+displayMessage);
		}else{
		imageActionResponse.setExecutionStatus(imageActionObject.getCurrent_task_status());
		}
		
		imageActionResponse.setStoreNames(commaSeperatedStoreNames);
		return imageActionResponse;
	}

/*	public String getDisplayMessageForAction(String imageId) {
		ImageStoreUploadTransferObject imageStoreTranserObject = null;
		ImageStoreUploadFilter imgUpFilter = new ImageStoreUploadFilter();
		imgUpFilter.setImage_id(imageId);
		// /imgUpFilter.setImage_id(imageId);
		List<ImageStoreUploadTransferObject> fetchImageUploads = null;
		try {
			fetchImageUploads = persistService.fetchImageUploads(imgUpFilter,
					null);
			if ((fetchImageUploads != null && fetchImageUploads.size() > 0)) {
				imageStoreTranserObject = fetchImageUploads
						.get(fetchImageUploads.size() - 1);

			}
		} catch (DbException e) {
			log.error("Error fetching image uploads {}", e);
		}
		if (imageStoreTranserObject == null) {
			return null;
		}
		try {
			ImageStoreTransferObject imageStoreDTO = persistService
					.fetchImageStorebyId(imageStoreTranserObject.getStoreId());
			if (StringUtils.isNotBlank(imageStoreDTO.getName())) {
				return "Image:"
						+ imageStoreTranserObject.getStoreArtifactName()
						+ " in store:" + imageStoreDTO.getName()
						+ " updated for policy";
			}

		} catch (DbException e) {
			log.error("No store exists for id {}",
					imageStoreTranserObject.getStoreId());
		}
		return "Image:" + imageStoreTranserObject.getStoreArtifactName()
				+ " updated for policy";

	}
	*/

	

	
	
}