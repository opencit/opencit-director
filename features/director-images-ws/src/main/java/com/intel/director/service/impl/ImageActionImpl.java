package com.intel.director.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.intel.director.api.ImageActionObject;
import com.intel.director.service.ImageActionService;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class ImageActionImpl implements ImageActionService {
	@Autowired
	private IPersistService ImageActionImplPersistenceManager;

	public ImageActionImpl() {
		ImageActionImplPersistenceManager = new DbServiceImpl();
	}
	
	public List<ImageActionObject> searchIncompleteImageAction(Integer count_of_action) throws DbException{
		List<ImageActionObject> allActionObject= new ArrayList<ImageActionObject>();
		List<ImageActionObject> actionObjectIncomplete= new ArrayList<ImageActionObject>();
		allActionObject=ImageActionImplPersistenceManager.searchByAction();
		for(ImageActionObject img :  allActionObject){
			if(img.getAction_completed() != img.getAction_count()){
				actionObjectIncomplete.add(img);
			}
		}
		
		if(count_of_action==null){
			return actionObjectIncomplete;
		}
		
		if(count_of_action > actionObjectIncomplete.size()){
		return actionObjectIncomplete.subList(0, actionObjectIncomplete.size());
		}else{
			return actionObjectIncomplete.subList(0, count_of_action);	
		}
		
	}

	public ImageActionObject createImageAction(ImageActionObject imageActionObject) throws DbException {
		
		return ImageActionImplPersistenceManager.createImageAction(imageActionObject);
	}

	public void updateImageAction(String id,ImageActionObject imageActionObject) throws DbException {
		ImageActionImplPersistenceManager.updateImageAction(id,imageActionObject);
		
	}

	public List<ImageActionObject> getdata() throws DbException {
		
		return ImageActionImplPersistenceManager.searchByAction();
	}
}