package com.intel.director.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.intel.director.api.ImageActionTask;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.FetchActionsService;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class DockerFetchActionsService implements FetchActionsService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DockerFetchActionsService.class);
	
	
	public List<ImageActionTask> getActionsByDeploymentArtifact(
			String artifactType, Map<String,String> storeidMap, boolean isEncrypted) throws DirectorException {
		
		IPersistService imagePersistService = new DbServiceImpl();
		ImageStoreTransferObject imageStoreObj;
		try {
			imageStoreObj = imagePersistService.fetchImageStorebyId(storeidMap.get(Constants.STORE_DOCKER));
		} catch (DbException e) {
			log.error("Error in Fetching Image Store @ DockerFetchActionsService",e);
			throw new DirectorException("Error in Fetching Image Store @ DockerFetchActionsService",e);
		}
			
		List<ImageActionTask> imageActionsList = new ArrayList<ImageActionTask>();
		
		if (Constants.CONNECTOR_DOCKERHUB.equalsIgnoreCase(imageStoreObj
				.getConnector())) {
			
			switch (artifactType) {
			case Constants.ARTIFACT_DOCKER_IMAGE:
				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						storeidMap.get(Constants.STORE_DOCKER), Constants.TASK_NAME_UPLOAD_IMAGE));

				break;
			case Constants.ARTIFACT_DOCKER_WITH_POLICY:

				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						Constants.TASK_NAME_INJECT_POLICY));
				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						storeidMap.get(Constants.STORE_DOCKER), Constants.TASK_NAME_UPLOAD_IMAGE));
				break;
			}
			

		} else {
			switch (artifactType) {
			case Constants.ARTIFACT_DOCKER_IMAGE:

				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						storeidMap.get(Constants.STORE_DOCKER), Constants.TASK_NAME_UPLOAD_IMAGE));

				break;
			case Constants.ARTIFACT_DOCKER_WITH_POLICY:

				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						Constants.TASK_NAME_INJECT_POLICY));
				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						Constants.TASK_NAME_CREATE_TAR));
				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						storeidMap.get(Constants.STORE_DOCKER), Constants.TASK_NAME_UPLOAD_TAR));
				break;
			}
		}
		return imageActionsList;
		
		
	}
}
