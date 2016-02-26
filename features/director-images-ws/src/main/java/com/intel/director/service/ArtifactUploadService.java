package com.intel.director.service;

import java.util.List;

import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.images.exception.DirectorException;

public interface ArtifactUploadService {
	
	public List<ImageStoreUploadTransferObject> fetchImageUploadsByUploadVariable(String uploadVar) throws DirectorException;
	
	public ImageStoreUploadTransferObject fetchImageUploadByImageId(String imageId);
	
	
	public void removeOrphanPolicies() throws DirectorException; 

}
