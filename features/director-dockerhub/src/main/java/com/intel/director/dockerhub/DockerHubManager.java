package com.intel.director.dockerhub;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.intel.director.api.GenericResponse;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.api.StoreResponse;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.DirectorUtil;
import com.intel.director.store.exception.StoreException;
import com.intel.director.store.impl.StoreManagerImpl;

public class DockerHubManager extends StoreManagerImpl {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DockerHubManager.class);
	private static final String SPACE = " ";

	@Override
	public String upload() throws StoreException {
		ImageInfo imageinfo = (ImageInfo) objectProperties
				.get(ImageInfo.class.getName());

		log.info("Executing /opt/director/bin/dockerhubUpload.sh" + SPACE
				+ (String) objectProperties.get(Constants.DOCKER_HUB_USERNAME)
				+ SPACE
				+ (String) objectProperties.get(Constants.DOCKER_HUB_PASSWORD)
				+ SPACE
				+ (String) objectProperties.get(Constants.DOCKER_HUB_EMAIL)
				+ SPACE + imageinfo.getRepository() + SPACE
				+ imageinfo.getTag());

		try {
			DirectorUtil.executeCommandInExecUtil(
					"/opt/director/bin/dockerhubUpload.sh",
					(String) objectProperties
							.get(Constants.DOCKER_HUB_USERNAME),
					(String) objectProperties
							.get(Constants.DOCKER_HUB_PASSWORD),
					(String) objectProperties.get(Constants.DOCKER_HUB_EMAIL),
					imageinfo.repository, imageinfo.tag);
		} catch (IOException e) {
			e.printStackTrace();
			throw new StoreException();
		}
		log.info("Pushed...!!!");
		return null;
	}

	@Override
	public <T extends StoreResponse> T fetchDetails() throws StoreException {
		StoreResponse imageStoreResponse = new ImageStoreUploadResponse();
		imageStoreResponse.setStatus(com.intel.director.common.Constants.COMPLETE);
		ImageInfo  imageInfo = (ImageInfo) objectProperties.get(ImageInfo.class.getName());
		imageStoreResponse.setId(imageInfo.id);
		return (T) imageStoreResponse;
	}

	@Override
	public void build(Map<String, String> map) throws StoreException {
		super.build(map);
	}

	@Override
	public void update() throws StoreException {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public void delete(URL url) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
		
	}

	@Override
	public <T extends StoreResponse> List<T> fetchAllImages() {
		throw new NotImplementedException();
	}

	@Override
	public GenericResponse validate() throws StoreException {
		GenericResponse response = new GenericResponse();
		int exitCode = 0;
		try {
			exitCode = DirectorUtil.executeCommandInExecUtil("docker", "login", "-u",(String) objectProperties
								.get(Constants.DOCKER_HUB_USERNAME), "-e",(String) objectProperties
								.get(Constants.DOCKER_HUB_EMAIL), "-p",(String) objectProperties
								.get(Constants.DOCKER_HUB_PASSWORD));
			if(exitCode != 0){
				response.setError("Invalid Credentials");
			}
		} catch (IOException e) {
			log.error("Error in executing docker login command");
			response.setError(e.getMessage());
		}
		finally{
			try {
				DirectorUtil.executeCommandInExecUtil("docker", "logout");
			} catch (IOException e) {
				log.error("Error in executing docker logout command");
			}
		}
		return response;
	}

}
