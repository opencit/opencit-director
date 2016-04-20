package com.intel.director.service;

import java.util.List;
import java.util.Map;

import com.intel.director.api.ImageActionTask;
import com.intel.director.common.exception.DirectorException;

public interface FetchActionsService {

	public List<ImageActionTask> getActionsByDeploymentArtifact(
			String artifactType, Map<String,String> artifactStoreIdMap,boolean isEncrypted) throws DirectorException ;
}
