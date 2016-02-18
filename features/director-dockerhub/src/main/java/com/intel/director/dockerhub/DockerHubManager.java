package com.intel.director.dockerhub;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.DirectorUtil;
import com.intel.director.exception.ImageStoreException;
import com.intel.director.imagestore.ImageStoreManager;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class DockerHubManager implements ImageStoreManager{

	private static final String SPACE = " ";
	private String imageStoreId;
	private String imageId;
	private IPersistService ipersistenceManager;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DockerHubManager.class);

	public DockerHubManager(String imageId, String imageStoreId) {
		this.imageId = imageId;
		this.imageStoreId = imageStoreId;
		ipersistenceManager = new DbServiceImpl();
	}
	
	@Override
	public String upload(File file, Map<String, String> imageProperties)
			throws ImageStoreException {
		ImageInfo imageinfo;
		try {
			imageinfo = ipersistenceManager.fetchImageById(imageId);
		} catch (DbException e) {
			throw new ImageStoreException("unable to fetch Image ::" + imageId, e);
		}
		log.info("Fetch Details for image store ::" + imageStoreId);
		/*
		 * ImageStoreSettings fetchImageStoreSettingsById =
		 * ipersistenceManager.fetchImageStoreSettingsById(imageStoreId);
		 */

		Configuration configuration;
		try {
			configuration = ConfigurationFactory.getConfiguration();
		} catch (IOException e1) {
			throw new ImageStoreException();
		}
		log.info("Executing /opt/director/bin/dockerhubUpload.sh"
				+ SPACE + configuration.get(Constants.USERNAME)
				+ SPACE + configuration.get(Constants.PASSWORD)
				+ SPACE + configuration.get(Constants.EMAIL)
				+ SPACE + configuration.get(Constants.REGISTRY_ADDRESS)
				+ SPACE + imageinfo.getRepository() + SPACE + imageinfo.getTag());
		
		try {
			DirectorUtil.executeCommandInExecUtil(
					"/opt/director/bin/dockerhubUpload.sh",
					configuration.get(Constants.REGISTRY_ADDRESS),
					configuration.get(Constants.USERNAME),
					configuration.get(Constants.PASSWORD),
					configuration.get(Constants.EMAIL),
					imageinfo.repository, imageinfo.tag);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ImageStoreException();
		}
		log.info("Pushed...!!!");
		return "";
	}



	@Override
	public ImageStoreUploadResponse fetchDetails(
			Map<String, String> imageProperties, String glanceId)
			throws ImageStoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
