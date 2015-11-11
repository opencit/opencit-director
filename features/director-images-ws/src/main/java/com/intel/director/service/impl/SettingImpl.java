package com.intel.director.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.intel.director.api.SshSettingInfo;
import com.intel.director.api.SshSettingRequest;
import com.intel.director.api.SshSettingResponse;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
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

	public List<SshSettingRequest> sshData() throws DirectorException {
		TdaasUtil tdaasUtil = new TdaasUtil();
		List<SshSettingInfo> fetchSsh;
		List<SshSettingRequest> responseSsh = new ArrayList<SshSettingRequest>();

		try {

			fetchSsh = settingsPersistenceManager.showAllSsh();

			for (SshSettingInfo sshSettingInfo : fetchSsh) {

				responseSsh.add(tdaasUtil.toSshSettingRequest(sshSettingInfo));
			}

		} catch (Exception e) {
			log.error("ssddata method failed", e);
			throw new DirectorException(e);
		}

		return responseSsh;

	}

	public void postSshData(SshSettingRequest sshSettingRequest)
			throws DirectorException {

		TdaasUtil tdaasUtil = new TdaasUtil();
		SshSettingInfo sshSetingInfo = tdaasUtil.fromSshSettingRequest(sshSettingRequest);

		TdaasUtil.addSshKey(sshSettingRequest.getIpAddress(),
				sshSettingRequest.getUsername(),
				sshSettingRequest.getPassword());

		log.debug("Going to save sshSetting info in database");
		try {
			settingsPersistenceManager.saveSshMetadata(sshSetingInfo);
		} catch (DbException e) {
			log.error("unable to save ssh info in database", e);
			throw new DirectorException(
					"Unable to save sshsetting info in database", e);
		}

	}

	public SshSettingRequest addHost(SshSettingRequest sshSettingRequest)
			throws DirectorException {

		TdaasUtil tdaasUtil = new TdaasUtil();

		SshSettingInfo sshSettingInfo = tdaasUtil
				.fromSshSettingRequest(sshSettingRequest);
		log.info("Inside addHost, going to addSshKey ");
		TdaasUtil.addSshKey(sshSettingRequest.getIpAddress(),
				sshSettingRequest.getUsername(),
				sshSettingRequest.getPassword());
		log.info("Inside addHost,After execution of addSshKey ");
		log.debug("Going to save sshSetting info in database");
		SshSettingInfo info=null;
		if( StringUtils.isNotBlank(sshSettingRequest.getImage_id())){
			
			SshSettingInfo existingSsh;
			try {
				existingSsh = settingsPersistenceManager
						.fetchSshByImageId(sshSettingRequest.getImage_id());
				if (existingSsh.getId() != null && !"".equals(existingSsh)) {
					sshSettingInfo.setId(existingSsh.getId());
					sshSettingInfo.setImage_id(existingSsh.getImage_id());
					sshSettingInfo.setName(sshSettingRequest.getIpAddress());
					///sshSettingRequest.setId(existingSsh.getId());
				}
				settingsPersistenceManager.updateSsh(sshSettingInfo);
				info=sshSettingInfo;
			} catch (DbException e) {
				log.error("Inside addHost, unable to update older ssh info in database", e);
				throw new DirectorException(
						"Unable to create policy draft", e);
			}
		
			
			
		}else{
			try {
				
				 info = settingsPersistenceManager
						.saveSshMetadata(sshSettingInfo);
		
				
			} catch (DbException e) {
				log.error("unable to save ssh info in database", e);
				throw new DirectorException(
						"Unable to create policy draft", e);
			}
		}
		return TdaasUtil.convertSshInfoToRequest(info);

	}

	public void updateSshData(SshSettingRequest sshSettingRequest)
			throws DirectorException {
		// SshSettingInfo updateSsh=new SshSettingInfo();

		TdaasUtil tdaasUtil = new TdaasUtil();

		// sshPersistenceManager.destroySshById(sshSettingRequest.getId());

		log.info("Inside updateSshData, going to addSshKey ");
		TdaasUtil.addSshKey(sshSettingRequest.getIpAddress(),
				sshSettingRequest.getUsername(),
				sshSettingRequest.getPassword());
		log.info("Inside updateSshData,After execution of addSshKey ");
		try {

			SshSettingInfo sshSettingInfo = tdaasUtil
					.fromSshSettingRequest(sshSettingRequest);
			SshSettingInfo existingSsh = settingsPersistenceManager
					.fetchSshByImageId(sshSettingRequest.getImage_id());
			if (existingSsh.getId() != null && !"".equals(existingSsh)) {
				sshSettingInfo.setId(existingSsh.getId());
				sshSettingInfo.setImage_id(existingSsh.getImage_id());
				///sshSettingRequest.setId(existingSsh.getId());
			}
			settingsPersistenceManager.updateSsh(sshSettingInfo);
		} catch (DbException e) {
			log.error("unable to update ssh info in database", e);
			throw new DirectorException(
					"Unable to update sshsetting info in database", e);
		}catch(Exception e){
			throw new DirectorException(
					"updateSshdata failed", e);
		}

	}

	public void updateSshDataById(String sshId) throws DbException {
		// SshSettingInfo updateSsh=new SshSettingInfo();
		settingsPersistenceManager.updateSshById(sshId);
	}

	public void deleteSshSetting(String sshId) throws DirectorException {
		try {
			settingsPersistenceManager.destroySshById(sshId);
		} catch (DbException e) {
			log.error("unable to delete ssh info in database", e);
			throw new DirectorException(
					"Unable to delete sshsetting info in database", e);
		}

	}

	public SshSettingRequest fetchSshInfoByImageId(String image_id)
			throws DbException {

		TdaasUtil tdaasUtil = new TdaasUtil();
		SshSettingInfo sshInfo = settingsPersistenceManager
				.fetchSshByImageId(image_id);

		return tdaasUtil.toSshSettingRequest(sshInfo);
	}

}
