package com.intel.director.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.intel.director.api.SshSettingRequest;
import com.intel.mtwilson.director.db.exception.DbException;

public interface Setting {

	public List<SshSettingRequest> sshData() throws DbException;

	public void postSshData(SshSettingRequest sshSettingRequest)
			throws DbException;

	public void updateSshData(SshSettingRequest sshSettingRequest)
			throws DbException;

	public void updateSshDataById(String sshId) throws DbException;

	public void deleteSshSetting(String sshId) throws DbException;

	public SshSettingRequest fetchSshInfoByImageId(String image_id)
			throws DbException;
	
	public String editProperties(String string, Map<String,String> data) throws IOException;
	
	public String getProperties(String path) throws IOException;
	
}
