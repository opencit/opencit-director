package com.intel.director.service;

import java.util.List;

import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.PolicyUploadTransferObject;
import com.intel.director.common.exception.DirectorException;

public interface ArtifactUploadService {

	public List<ImageStoreUploadTransferObject> fetchImageUploadsByUploadVariable(String uploadVar)
			throws DirectorException;

	public ImageStoreUploadTransferObject fetchImageUploadByImageId(String imageId);

	public List<PolicyUploadTransferObject> removeOrphanPolicies() throws DirectorException;

}
