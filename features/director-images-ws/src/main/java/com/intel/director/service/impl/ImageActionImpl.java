package com.intel.director.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.intel.director.api.ImageActionTask;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionRequest;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageActionService;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class ImageActionImpl implements ImageActionService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageActionImpl.class);
	@Autowired
	private IPersistService persistService;

	public ImageActionImpl() {
		persistService = new DbServiceImpl();
	}

	public List<ImageActionObject> searchIncompleteImageAction(
			Integer count_of_action) throws DbException {

		List<ImageActionObject> actionObjectIncomplete = new ArrayList<ImageActionObject>();
		List<ImageActionObject> allActionObject = persistService
				.searchByAction();
		for (ImageActionObject img : allActionObject) {
			if ((img.getAction_completed() != img.getAction_count())
					&& !(img.getCurrent_task_status() != null && img
							.getCurrent_task_status().startsWith(
									Constants.ERROR))) {
				actionObjectIncomplete.add(img);
			}
		}

		if (count_of_action == null) {
			return actionObjectIncomplete;
		}

		if (count_of_action > actionObjectIncomplete.size()) {
			return actionObjectIncomplete;
		} else {
			return actionObjectIncomplete.subList(0, count_of_action);
		}

	}

	public ImageActionObject createImageAction(
			ImageActionObject imageActionObject) throws DbException {

		return persistService.createImageAction(imageActionObject);
	}

	public void updateImageAction(String id, ImageActionObject imageActionObject)
			throws DbException {
		persistService.updateImageAction(id, imageActionObject);

	}

	public List<ImageActionObject> getdata() throws DbException {

		return persistService.searchByAction();
	}

	@Override
	public ImageActionObject createImageAction(
			ImageActionRequest imageActionRequest) throws DirectorException {
		ImageInfo imageInfo = null;
		try {
			imageInfo = persistService
					.fetchImageById(imageActionRequest.image_id);
		} catch (DbException e1) {
			log.error("Unable to fetch image by id :"
					+ imageActionRequest.image_id, e1);
			throw new DirectorException("Unable to fetch image by id :"
					+ imageActionRequest.image_id, e1);
		}

		TrustPolicy trustPolicy = null;
		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = null;
		if (imageInfo.getTrust_policy_id() != null) {

			try {
				trustPolicy = persistService.fetchPolicyById(imageInfo
						.getTrust_policy_id());

				try {
					policy = TdaasUtil.getPolicy(trustPolicy.getTrust_policy());
				} catch (JAXBException e1) {
					log.error("Unable to convert policy string into object  :"
							+ trustPolicy.getTrust_policy(), e1);
					throw new DirectorException(
							"Unable to convert policy string into object  :"
									+ trustPolicy.getTrust_policy(), e1);
				}
			} catch (DbException e1) {
				log.error(
						"Unable to fetch trust policy by id :"
								+ imageInfo.getTrust_policy_id(), e1);
				throw new DirectorException(
						"Unable to fetch trust policy by id :"
								+ imageInfo.getTrust_policy_id(), e1);
			}
		}

		ImageActionObject imageAction = new ImageActionObject();
		imageAction.setImage_id(imageActionRequest.getImage_id());
		imageAction.setAction_completed(0);
		imageAction.setAction_count(imageActionRequest.getActions().size());
		imageAction.setCurrent_task_name(imageActionRequest.getActions().get(0)
				.getTask_name());
		imageAction.setCurrent_task_status(Constants.INCOMPLETE);
		imageAction.setAction_size(0);
		imageAction.setAction_size_max(1);

		List<ImageActionTask> list = new ArrayList<ImageActionTask>();
		if (policy != null && policy.getEncryption() != null) {
			ImageActionTask imageActions = new ImageActionTask();
			imageActions.setTask_name(Constants.TASK_NAME_ENCRYPT_IMAGE);
			imageActions.setStatus(Constants.INCOMPLETE);
			imageAction.setAction_count(imageAction.getAction_count() + 1);
			imageAction.setCurrent_task_name(Constants.TASK_NAME_ENCRYPT_IMAGE);
			list.add(imageActions);
		}

		for (ImageActionTask action : imageActionRequest.getActions()) {
			ImageActionTask imageActions = new ImageActionTask();
			if (action.getTask_name() != null) {
				imageActions.setTask_name(action.getTask_name());
			}
			if (action.getStatus() != null) {
				imageActions.setStatus(StringUtils.isNotBlank(action
						.getStatus()) ? action.getStatus()
						: Constants.INCOMPLETE);
			}
			if (action.getStorename() != null) {
				imageActions.setStorename(action.getStorename());
			}

			list.add(imageActions);
		}
		if (list.size() != 0) {
			imageAction.setActions(list);
		}
		try {
			return persistService.createImageAction(imageAction);
		} catch (DbException e) {
			log.error("Error while creating image action" + e);
			throw new DirectorException("Error while creating image action", e);
		}
	}

	@Override
	public ImageActionObject updateImageAction(
			ImageActionRequest imageActionRequest) throws DirectorException {
		ImageActionObject imageAction;
		try {
			imageAction = persistService
					.fetchImageActionById(imageActionRequest.action_id);
		} catch (DbException e1) {
			log.error("Error while fetching image action" + e1);
			throw new DirectorException("Error while fetching image action", e1);
		}
		imageAction.setAction_count(imageAction.getAction_count()
				+ imageActionRequest.getActions().size());
		List<ImageActionTask> list = new ArrayList<ImageActionTask>();
		for (ImageActionTask action : imageActionRequest.getActions()) {
			ImageActionTask imageActions = new ImageActionTask();
			imageActions.setTask_name(action.getTask_name());
			imageActions
					.setStatus(StringUtils.isNotBlank(action.getStatus()) ? action
							.getStatus() : Constants.INCOMPLETE);
			list.add(imageActions);
		}
		if (list.size() != 0) {
			imageAction.setActions(list);
		}
		try {
			persistService.updateImageAction(imageAction);
		} catch (DbException e) {
			log.error("Error while updating image action" + e);
			throw new DirectorException("Error while updating image action", e);
		}

		return imageAction;

	}

	@Override
	public void deleteImageAction(String actionId) throws DirectorException {
		try {
			persistService.deleteImageActionById(actionId);
		} catch (DbException e) {
			log.error("Error while deleting image action" + e);
			throw new DirectorException("Error while deleting image action", e);
		}
	}

	@Override
	public ImageActionObject fetchImageAction(String actionId)
			throws DirectorException {
		try {
			return persistService.fetchImageActionById(actionId);
		} catch (DbException e) {
			log.error("Error while fetching image action" + e);
			throw new DirectorException("Error while fetching image action", e);
		}
	}
}