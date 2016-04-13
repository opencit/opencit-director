package com.intel.director.service.impl;

import java.util.List;
import java.util.Map;

import com.intel.director.api.ImageActionTask;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.FetchActionsService;

public class BMFetchActionsService implements FetchActionsService {

	@Override
	public List<ImageActionTask> getActionsByDeploymentArtifact(
			String artifactType, Map<String, String> artifactStoreIdMap,
			boolean isEncrypted) throws DirectorException {
		// TODO Auto-generated method stub
		return null;
	}



}
