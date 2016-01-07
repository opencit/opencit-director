package com.intel.director.service;

import java.util.List;

import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionRequest;
import com.intel.director.images.exception.DirectorException;
import com.intel.mtwilson.director.db.exception.DbException;

public interface ImageActionService {
	
	public List<ImageActionObject> searchIncompleteImageAction(Integer count_of_action) throws DbException;
	
	public ImageActionObject createImageAction(ImageActionObject imageActionObject) throws DbException;
	
	public void updateImageAction(String id,ImageActionObject imageActionObject) throws DbException;
	
	public List<ImageActionObject> getdata() throws DbException;

	public ImageActionObject createImageAction(ImageActionRequest imageActionRequest)
			throws DirectorException;

	public ImageActionObject updateImageAction(
			ImageActionRequest imageActionRequest) throws DirectorException;

	public void deleteImageAction(String actionId)
			throws DirectorException;

	ImageActionObject fetchImageAction(String actionId)
			throws DirectorException;

}