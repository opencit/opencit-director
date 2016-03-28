/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.async;

import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.DockerActionService;
import com.intel.director.service.impl.DockerActionImpl;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

/**
 * 
 * @author Aakash
 */
public class DockerPullTask implements Runnable {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DockerPullTask.class);

	private String imageId;

	private DockerActionService dockerActionService;
	private IPersistService persistenceManager;

	public DockerPullTask() {

	}

	public DockerPullTask(String imageId) {
		this.imageId = imageId;

		persistenceManager = new DbServiceImpl();
		dockerActionService = new DockerActionImpl();

	}

	@Override
	public void run() {
		ImageInfo image = null;
		log.info("Inside DockerPullTask run()");
		try {
			image = persistenceManager.fetchImageById(imageId);
		} catch (DbException e) {
			log.error("Error in fetching image  ", e);
			return;
		}
		if (image == null) {
			log.error("DockerPullTask, no image found for imageId::" + imageId);
			return;
		}

		String repository = image.getRepository();
		String tag = image.getTag();

		log.info("Inside DockerPullTask, repository::" + repository + " tag::" + tag);

		try {
			image.setStatus(Constants.IN_PROGRESS);
			try {
				persistenceManager.updateImage(image);
			} catch (DbException e) {
				log.error("Unable to update image ,set downloading status in DockerPullTask", e);
			}
			dockerActionService.dockerPull(repository, tag);

			dockerActionService.dockerSave(imageId);

			String newTag = tag + Constants.SOURCE_TAG;
			dockerActionService.dockerTag(imageId, repository, newTag);
			dockerActionService.dockerRMI(imageId);
			image.setStatus(Constants.COMPLETE);
		} catch (DirectorException e1) {
			log.error("Error in pull task", e1);
			image.setStatus(Constants.ERROR);
			// In case of any exception, remove the docker image
			try {
				dockerActionService.dockerRMI(imageId);
			} catch (DirectorException e) {
				log.error("Error removing docker image ", e);
			}
		}
		try {
			persistenceManager.updateImage(image);
		} catch (DbException e) {
			log.error("Unable to update image action, set status in DockerPullTask", e);
		}

	}

}
