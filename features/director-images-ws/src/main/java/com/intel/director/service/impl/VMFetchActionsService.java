package com.intel.director.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.intel.director.api.ImageActionTask;
import com.intel.director.common.Constants;
import com.intel.director.service.FetchActionsService;

public class VMFetchActionsService implements FetchActionsService {

	public List<ImageActionTask> getActionsByDeploymentArtifact(
			String artifactType, Map<String, String> artifactStoreIdMap,
			boolean isEncrypted) {

		List<ImageActionTask> imageActionsList = new ArrayList<ImageActionTask>();
		switch (artifactType) {
		case Constants.ARTIFACT_IMAGE:
			if (isEncrypted) {
				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						Constants.TASK_NAME_ENCRYPT_IMAGE));
			}
			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					artifactStoreIdMap.get(Constants.STORE_IMAGE),
					Constants.TASK_NAME_UPLOAD_IMAGE));

			break;
		case Constants.ARTIFACT_TAR:
			if (isEncrypted) {
				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						Constants.TASK_NAME_ENCRYPT_IMAGE));
			}
			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					Constants.TASK_NAME_RECREATE_POLICY));
			
			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					Constants.TASK_NAME_CREATE_TAR));
			
			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					artifactStoreIdMap.get(Constants.STORE_TAR),
					Constants.TASK_NAME_UPLOAD_TAR));
			break;
		case Constants.ARTIFACT_POLICY:

			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					artifactStoreIdMap.get(Constants.STORE_POLICY),
					Constants.TASK_NAME_UPLOAD_POLICY));
			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					artifactStoreIdMap.get(Constants.STORE_IMAGE),
					Constants.TASK_NAME_UPDATE_METADATA));
			break;

		case Constants.ARTIFACT_IMAGE_WITH_POLICY:
			if (isEncrypted) {
				imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
						Constants.TASK_NAME_ENCRYPT_IMAGE));
			}
			
			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					Constants.TASK_NAME_RECREATE_POLICY));
			
			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					artifactStoreIdMap.get(Constants.STORE_IMAGE),
					Constants.TASK_NAME_UPLOAD_IMAGE_FOR_POLICY));
			
			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					artifactStoreIdMap.get(Constants.STORE_POLICY),
					Constants.TASK_NAME_UPLOAD_POLICY));

			imageActionsList.add(new ImageActionTask(Constants.INCOMPLETE,
					artifactStoreIdMap.get(Constants.STORE_IMAGE),
					Constants.TASK_NAME_UPDATE_METADATA));
			break;
		}

		return imageActionsList;

	}

}
