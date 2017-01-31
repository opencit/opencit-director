package com.intel.director.store.impl;

import java.util.HashMap;
import java.util.Map;

import com.intel.director.store.StoreManager;
import com.intel.director.store.exception.StoreException;

public abstract class StoreManagerImpl implements StoreManager {

	public Map<String, Object> objectProperties;

	@Override
	public void build(Map<String, String> map) throws StoreException {		
		objectProperties = new HashMap<String, Object>(map.size());
		objectProperties.putAll(map);
	}
	
	@Override
	public void addCustomProperties(Map<String, Object> map){
		objectProperties.putAll(map);
	}


}
