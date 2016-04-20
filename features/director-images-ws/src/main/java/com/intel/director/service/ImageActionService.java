package com.intel.director.service;

import java.util.List;

import com.intel.director.api.ArtifactStoreDetails;
import com.intel.director.api.ImageActionHistoryResponse;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionRequest;
import com.intel.director.api.ImageActionTask;
import com.intel.director.common.exception.DirectorException;
import com.intel.mtwilson.director.db.exception.DbException;

public interface ImageActionService {

	public List<ImageActionObject> searchIncompleteImageAction(
			Integer count_of_action) throws DbException;

	public ImageActionObject createImageAction(
			ImageActionObject imageActionObject) throws DbException;

	public void updateImageAction(String id,ImageActionObject imageActionObject) throws DbException;

	public List<ImageActionObject> getdata() throws DbException;

	public ImageActionObject createImageAction(
			ImageActionRequest imageActionRequest) throws DirectorException;
	


	public void deleteImageAction(String actionId) throws DirectorException;

	ImageActionObject fetchImageAction(String actionId)
			throws DirectorException;
	
	void updateImageActionState(ImageActionObject imageActionObject, ImageActionTask taskAction, String taskName, String status, String details) ;
	
	void updateImageActionContentSent(ImageActionObject imageActionObject, int sent, int size) ;

	List<String> validateCreationOfImageAction(String imageId, List<ArtifactStoreDetails> list) throws DirectorException;
	
	List<ImageActionHistoryResponse> getImageActionHistory(String imageId)throws DirectorException;

}