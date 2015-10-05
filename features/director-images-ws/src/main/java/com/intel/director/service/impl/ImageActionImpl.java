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
		for(int index=0;index<allActionObject.size();index++){
			if(allActionObject.get(index).getAction_size()>allActionObject.get(index).getAction_size_max()){
				ImageActionObject img=new ImageActionObject();
				img.setAction(allActionObject.get(index).getAction());
				img.setId((allActionObject.get(index).getId()));
				img.setAction_completed((allActionObject.get(index).getAction_completed()));
				img.setAction_count(allActionObject.get(index).getAction_count());
				img.setAction_size(allActionObject.get(index).getAction_size());
				img.setAction_size_max(allActionObject.get(index).getAction_size_max());
				img.setImage_id(allActionObject.get(index).getImage_id());
				actionObjectIncomplete.add(img);
			}
		}
		
		if(count_of_action==null){
			return actionObjectIncomplete;
		}
		System.out.println(actionObjectIncomplete);
		return actionObjectIncomplete.subList(0, count_of_action);
		
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