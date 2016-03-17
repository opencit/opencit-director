package com.intel.director.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DockerUtil;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.DockerActionService;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class DockerActionImpl implements DockerActionService {

	private IPersistService imagePersistenceManager;

	public DockerActionImpl() {
		imagePersistenceManager = new DbServiceImpl();
	}

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DockerActionImpl.class);

	public void dockerSave(String image_id) throws DirectorException {
		ImageInfo image = fetchImage(image_id);
		TrustPolicy trustPolicy;
		String displayName = null;

		if (image.getTrust_policy_id() == null) {
			displayName = image.getRepository() + ":" + image.getTag();
		} else {

			try {
				trustPolicy = imagePersistenceManager.fetchPolicyById(image.getTrust_policy_id());
				displayName = trustPolicy.getDisplay_name();
			} catch (DbException e1) {
				log.error("Error in fetching policy  ", e1);
				throw new DirectorException("No image found with id: " + image.getTrust_policy_id(), e1);
			}
		}
		try {
			int success = DockerUtil.dockerSave(image.repository, image.tag, "/mnt/images/",
					displayName + ".tar");
			if (success != 0) {
				throw new DirectorException("Docker save failed for imageid ::" + image_id);

			}
		} catch (Exception e) {
			log.error("Error in Docker image save  ", e);
			throw new DirectorException("Error in Docker image save", e);
		}
		log.info("Docker image saved successfully ::" + image.getImage_name());

	}

	public void dockerRMI(String image_id) throws DirectorException {
		ImageInfo image = fetchImage(image_id);
		try {
			int status = DockerUtil.dockerRMI(image.repository, image.tag);
			if (status != 0) {
				throw new DirectorException("Docker rmi failed for imageid ::" + image_id);
			}
		} catch (Exception e) {
			log.error("Error in Docker image  removed  ", e);
			throw new DirectorException("Error in Docker rmi with image : " + image.getImage_name(), e);
		}
		log.info("Docker image  removed successfully " + image.getImage_name());
	}

	public void dockerLoad(String image_id) throws DirectorException {
		ImageInfo image = fetchImage(image_id);

		try {
			log.info("Loading Docker image...!!!");

			int status = DockerUtil.dockerLoad(image.getLocation() + image.getImage_name());
			if (status != 0) {
				throw new DirectorException("Docker load failed for imageid ::" + image_id);
			}

		} catch (Exception e) {
			image.setStatus(Constants.INCOMPLETE);
			try {
				imagePersistenceManager.updateImage(image);
			} catch (DbException e1) {
				log.error("Error in Updating Image Status", e1);
				throw new DirectorException("Error in Updating Image Status", e1);
			}
			log.error("Error in loading  Docker image ", e);
			throw new DirectorException("Error in loading  Docker image ", e);
		}
		log.info("Docker image  loaded successfully...!!! ::" + image.getImage_name());

	}

	public void dockerTag(String image_id, String newRepository, String newTag) throws DirectorException {
		ImageInfo image = fetchImage(image_id);

		try {
			log.info("Tagging Docker image...!!!");
			int status = DockerUtil.dockerTag(image.repository, image.tag, newRepository, newTag);
			if (status != 0) {
				throw new DirectorException("Docker tag failed for imageid ::" + image_id);
			}
		} catch (Exception e) {
			log.error("Error in Docker Tagging image", e);
			throw new DirectorException("Error in Docker Tagging image", e);
		}
		log.info("Docker image  tagged successfully.. " + image.getImage_name());
	}

	public boolean doesRepoTagExist(String repository, String tag, String currentImageId) throws DirectorException {
		List<String> statusToBeCheckedList = new ArrayList<>(2);
		statusToBeCheckedList.add(Constants.IN_PROGRESS);
		statusToBeCheckedList.add(Constants.COMPLETE);
		try {
			List<ImageInfo> imagesList = imagePersistenceManager.fetchImages(null);
			boolean imageCheck = true;
			for (ImageInfo image : imagesList) {
				imageCheck = true;
				if (!currentImageId.equals("NO_IMAGE")) {
					imageCheck = !image.getId().equals(currentImageId);
				}
				if (!image.isDeleted() && imageCheck && statusToBeCheckedList.contains(image.status)
						&& Constants.DEPLOYMENT_TYPE_DOCKER.equals(image.image_deployments)
						&& repository.equalsIgnoreCase(image.repository) && tag.equalsIgnoreCase(image.tag)) {
					log.info("Docker image  with given Repo tag already exists.. image name::" + image.getImage_name()
							+ " reposotry:" + repository + " tag:" + tag);
					return true;
				}
			}
		} catch (DbException e) {
			throw new DirectorException("Unable to fetch Images", e);
		}
		return false;
	}

	@Override
	public boolean doesRepoTagExist(String repository, String tag) throws DirectorException {
		return doesRepoTagExist(repository, tag, "NO_IMAGE");
	}

	public void dockerPull(String repository, String tag) throws DirectorException {
		try {
			log.info("Going to Download docker image for repo: {} and tag: {}!!!", repository, tag);
			int status = DockerUtil.dockerPull(repository, tag);
			if (status != 0) {
				throw new DirectorException("Docker pull failed for repo:" + repository + " and tag: " + tag);
			}
		} catch (Exception e) {
			log.error("Error in loading  Docker image ", e);
			throw new DirectorException("Error in pulling  Docker image ", e);
		}

		log.info("dockerPull executed successfully for repo: {} and tag: {}...!!!", repository, tag);

	}

	
	private ImageInfo fetchImage(String image_id) throws DirectorException{
		ImageInfo image;
		try {
			image = imagePersistenceManager.fetchImageById(image_id);
		} catch (DbException e) {
			log.error("Error in fetching image {}",image_id,  e);
			throw new DirectorException("No image found with id: " + image_id, e);
		}

		if (image == null) {
			throw new DirectorException("No image found for id:" + image_id);
		}
		return image;
	}
	


}

