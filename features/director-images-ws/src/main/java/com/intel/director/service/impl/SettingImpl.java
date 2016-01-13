package com.intel.director.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class SettingImpl {
	@Autowired
	private IPersistService settingsPersistenceManager;

	@Autowired
	// private ImageStoreManager imageStoreManager;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(SettingImpl.class);

	public SettingImpl() {
		settingsPersistenceManager = new DbServiceImpl();
	}


}
