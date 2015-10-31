package com.intel.director.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.intel.director.api.SshSettingInfo;
import com.intel.director.api.SshSettingRequest;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class SettingImpl {
	@Autowired
	private IPersistService settingsPersistenceManager;

	@Autowired
	// private ImageStoreManager imageStoreManager;
	public SettingImpl() {
		settingsPersistenceManager = new DbServiceImpl();
	}

	public List<SshSettingRequest> sshData() throws DbException {
		TdaasUtil tdaasUtil = new TdaasUtil();
		List<SshSettingInfo> fetchSsh;
		List<SshSettingRequest> responseSsh = new ArrayList<SshSettingRequest>();
		fetchSsh = settingsPersistenceManager.showAllSsh();

		for (SshSettingInfo sshSettingInfo : fetchSsh) {

			responseSsh.add(tdaasUtil.toSshSettingRequest(sshSettingInfo));
		}

		return responseSsh;

	}

	public void postSshData(SshSettingRequest sshSettingRequest)
			throws DbException, IOException {




		TdaasUtil tdaasUtil = new TdaasUtil();
		SshSettingInfo sshSetingInfo = tdaasUtil

				.fromSshSettingRequest(sshSettingRequest);
		settingsPersistenceManager.saveSshMetadata(sshSetingInfo);
		
		boolean status = TdaasUtil.addSshKey(sshSettingRequest.getIpAddress(), sshSettingRequest.getUsername(), sshSettingRequest.getPassword());
	
		System.out.println(status);
		
	}

	public void updateSshData(SshSettingRequest sshSettingRequest)
			throws DbException, IOException {
		// SshSettingInfo updateSsh=new SshSettingInfo();

		TdaasUtil tdaasUtil = new TdaasUtil();

		// sshPersistenceManager.destroySshById(sshSettingRequest.getId());
		settingsPersistenceManager.updateSsh(tdaasUtil
				.fromSshSettingRequest(sshSettingRequest));
		TdaasUtil.addSshKey(sshSettingRequest.getIpAddress(), sshSettingRequest.getUsername(), sshSettingRequest.getPassword());
		
	}

	public void updateSshDataById(String sshId) throws DbException {
		// SshSettingInfo updateSsh=new SshSettingInfo();
		settingsPersistenceManager.updateSshById(sshId);
	}

	public void deleteSshSetting(String sshId) throws DbException {

		settingsPersistenceManager.destroySshById(sshId);

	}

	public SshSettingRequest fetchSshInfoByImageId(String image_id)
			throws DbException {

		TdaasUtil tdaasUtil = new TdaasUtil();
		SshSettingInfo sshInfo = settingsPersistenceManager
				.fetchSshByImageId(image_id);

		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println(tdaasUtil.toSshSettingRequest(sshInfo));
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		return tdaasUtil.toSshSettingRequest(sshInfo);
	}
	
}
