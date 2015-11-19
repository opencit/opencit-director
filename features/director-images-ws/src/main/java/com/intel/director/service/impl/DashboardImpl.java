package com.intel.director.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.ImageUploadRequest;
import com.intel.director.api.ImagesReadyToDeployResponse;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftRequest;
import com.intel.director.api.User;
import com.intel.director.api.ui.ImageCountPieChart;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageInfoFields;
import com.intel.director.api.ui.ImageInfoOrderBy;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.TrustPolicyDraftFields;
import com.intel.director.api.ui.TrustPolicyDraftFilter;
import com.intel.director.api.ui.TrustPolicyDraftOrderBy;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.DashboardService;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

/**
 * @author Sayee
 *
 */
/**
 * @author GS-0999
 * 
 */
public class DashboardImpl implements DashboardService {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DashboardImpl.class);
	private IPersistService dashboardImplPersistenceManager;

	public DashboardImpl() {
		dashboardImplPersistenceManager = new DbServiceImpl();
	}

	// Recent policy for All User
	public List<TrustPolicyDraft> getRecentPolicy() {

		List<ImageInfo> images = null;
		try {
			images = dashboardImplPersistenceManager.fetchImages(null);
		} catch (DbException e) {
			log.error("Error in fetching images ", e);
		}
		if (images == null) {
			return new ArrayList<TrustPolicyDraft>();
		}
		TrustPolicyDraftOrderBy tpOrderBy = new TrustPolicyDraftOrderBy();
		tpOrderBy.setTrustPolicyDraftFields(TrustPolicyDraftFields.EDITED_DATE);
		tpOrderBy.setOrderBy(OrderByEnum.DESC);
		List<TrustPolicyDraft> tpdlists = null;
		try {
			tpdlists = dashboardImplPersistenceManager
					.fetchPolicyDrafts(tpOrderBy);

		} catch (DbException e) {
			log.error("Error in fetching Policy Drafts ", e);
			return new ArrayList<TrustPolicyDraft>();
		}
		if (tpdlists == null) {
			return new ArrayList<TrustPolicyDraft>();
		}
		List<TrustPolicyDraft> result = new ArrayList<TrustPolicyDraft>();
		for (TrustPolicyDraft tpd : tpdlists) {
			String image_format;
			try {
				image_format = dashboardImplPersistenceManager.fetchImageById(
						tpd.getImgAttributes().getId()).getImage_format();
				if (image_format != null) {
					result.add(tpd);
				}
			} catch (DbException e) {
				log.error("Error in fetching image ::  "
						+ tpd.getImgAttributes().getId(), e);
			}

		}

		return tpdlists;

	}

	// current user recent policy
	public List<TrustPolicyDraftRequest> getRecentUserPolicy(
			String created_by_user_id) throws DbException {

		TdaasUtil tdaasUtil = new TdaasUtil();

		TrustPolicyDraftFilter trustPolicyDraftFilter = new TrustPolicyDraftFilter();
		List<TrustPolicyDraftRequest> trustPolicyDraftRequest = new ArrayList<TrustPolicyDraftRequest>();
		trustPolicyDraftFilter.setCreated_by_user_id(created_by_user_id);
		TrustPolicyDraftOrderBy tpOrderBy = new TrustPolicyDraftOrderBy();
		tpOrderBy.setTrustPolicyDraftFields(TrustPolicyDraftFields.EDITED_DATE);
		tpOrderBy.setOrderBy(OrderByEnum.DESC);
		List<TrustPolicyDraft> list = dashboardImplPersistenceManager
				.fetchPolicyDrafts(trustPolicyDraftFilter, tpOrderBy);
		for (TrustPolicyDraft tpDR : list) {
			trustPolicyDraftRequest.add(tdaasUtil.toTrustPolicyDraft(tpDR));
		}
		return trustPolicyDraftRequest;
	}

	// New imported images without policy Done
	public List<ImageInfo> getImagesWithoutPolicy() throws DbException {

		ImageInfoOrderBy imgOrderBy = new ImageInfoOrderBy();
		imgOrderBy.setImgFields(ImageInfoFields.CREATED_DATE);
		imgOrderBy.setOrderBy(OrderByEnum.DESC);
		List<ImageInfo> images = dashboardImplPersistenceManager.fetchImages(
				null, imgOrderBy);
		if (images == null) {
			return new ArrayList<ImageInfo>();
		}

		List<ImageInfo> result = new ArrayList<ImageInfo>();

		for (ImageInfo mwTPD : images) {
			if (mwTPD.getTrust_policy_draft_id() == null
					&& mwTPD.getTrust_policy_id() == null
					&& mwTPD.getImage_format() != null) {
				result.add(mwTPD);
			}
		}
		return result;
	}

	// Recently deployed Images Done
	public List<ImageUploadRequest> getRecentlyDeployedImages()
			throws DbException {
		ImageStoreUploadOrderBy orderBy = new ImageStoreUploadOrderBy();
		TdaasUtil tdaasUtil = new TdaasUtil();

		orderBy.setImgStoreUploadFields(ImageStoreUploadFields.DATE);
		orderBy.setOrderBy(OrderByEnum.DESC);
		List<ImageUploadRequest> imgUploadRequest = new ArrayList<ImageUploadRequest>();
		List<ImageStoreUploadTransferObject> list = dashboardImplPersistenceManager
				.fetchImageUploads(orderBy);
		if (list == null) {
			return null;
		}
		for (ImageStoreUploadTransferObject imgUTO : list) {
			imgUploadRequest.add(tdaasUtil.toImageUpload(imgUTO));
		}
		return imgUploadRequest;

	}

	// Images ready to deploy Done
	public List<ImagesReadyToDeployResponse> getImagesReadyToDeploy()
			throws DbException, DirectorException {
		List<ImagesReadyToDeployResponse> resultList = new ArrayList<ImagesReadyToDeployResponse>();
		TdaasUtil tdaasUtil = new TdaasUtil();
		List<ImageInfo> images = dashboardImplPersistenceManager
				.fetchImages(null);

		if (images == null) {
			return resultList;
		}

		ImageActionImpl imgAction = new ImageActionImpl();
		List<ImageActionObject> imageactions = imgAction.getdata();
		List<ImageStoreUploadTransferObject> images_upload = dashboardImplPersistenceManager
				.fetchImageUploads(null);

		for (ImageInfo image : images) {
			if(image.getTrust_policy_id()==null || image.getImage_format()==null){
				continue;
			}
			if (image.getImage_format() != null
					&& image.getTrust_policy_id() != null) {
				if (imageactions != null) {
					for (ImageActionObject imageaction : imageactions) {
						if ((imageaction.getImage_id().equals(image.getId())) && imageaction.getAction_count()>1) {
							continue;
						}
					}
				}

				if (images_upload != null) {
					for (ImageStoreUploadTransferObject image_upload : images_upload) {
						if (image_upload.getImg().getId().equals(image.getId())) {
							continue;
						}
					}
				}

				ImagesReadyToDeployResponse result = tdaasUtil.toImageReadyToDeploy(image);
				resultList.add(result);

			}
		}
		return resultList;
	}

	// Upload in progress Done
	public List<ImageActionObject> uploadInProgress() throws DbException {
		List<ImageActionObject> uploadInProgress = new ArrayList<ImageActionObject>();
		List<ImageActionObject> imgAction = dashboardImplPersistenceManager
				.searchByAction();
		if(imgAction!=null){
		for (ImageActionObject imgObj : imgAction) {
	
			
			ImageInfo image_info;
			String policy_id, display_name;
			
				image_info = dashboardImplPersistenceManager.fetchImageById(imgObj.getImage_id());
				if (image_info.getTrust_policy_draft_id() != null) {
					policy_id = image_info.getTrust_policy_draft_id();
					display_name = dashboardImplPersistenceManager.fetchPolicyDraftById(
							policy_id).getDisplay_name();
				} else if (image_info.getTrust_policy_id() != null) {
					policy_id = image_info.getTrust_policy_id();
					display_name = dashboardImplPersistenceManager.fetchPolicyById(
							policy_id).getDisplay_name();
				} else {
					display_name = image_info.name;
				}

			
			
			imgObj.setImage_id(display_name);
			if(Constants.TASK_NAME_UPLOAD_TAR.equals(imgObj.getCurrent_task_name()) || Constants.TASK_NAME_UPLOAD_IMAGE.equals(imgObj.getCurrent_task_name() )|| Constants.TASK_NAME_UPLOAD_POLICY.equals(imgObj.getCurrent_task_name() )){
			
			uploadInProgress.add(imgObj);
			}
		}
		}
		return uploadInProgress;
	}

	public int countOfImagesWithPolicy() throws DbException {
		List<ImageInfo> images = dashboardImplPersistenceManager
				.fetchImages(null);
		if (images == null) {
			return 0;
		}
		int count = 0;
		for (ImageInfo image : images) {
			if (image.getTrust_policy_id() != null
					&& image.getTrust_policy_draft_id() == null) {
				count++;
			}
		}
		return count;

	}

	public int countOfImagesWithoutPolicy() throws DbException {
		List<ImageInfo> images = dashboardImplPersistenceManager
				.fetchImages(null);
		if (images == null) {
			return 0;
		}
		int count = 0;
		for (ImageInfo image : images) {
			if (image.getTrust_policy_id() == null
					&& image.getTrust_policy_draft_id() != null) {
				count++;
			}
		}
		return count;

	}

	public int countOfImagesWithPolicyNoDraft() throws DbException {
		List<ImageInfo> images = dashboardImplPersistenceManager
				.fetchImages(null);
		if (images == null) {
			return 0;
		}
		int count = 0;
		for (ImageInfo image : images) {
			if (image.getTrust_policy_id() == null
					&& image.getTrust_policy_draft_id() == null) {
				count++;
			}
		}
		return count;
	}

	public ArrayList<ImageCountPieChart> pieChart() throws DbException {
		ImageCountPieChart imgWithPolicyNoDraft = new ImageCountPieChart();
		ImageCountPieChart imgWithoutPolicy = new ImageCountPieChart();
		ImageCountPieChart imgWithPolicy = new ImageCountPieChart();
		ArrayList<ImageCountPieChart> listImageCount = new ArrayList<ImageCountPieChart>();
		imgWithPolicyNoDraft.setValue(countOfImagesWithPolicyNoDraft());
		imgWithPolicyNoDraft.setLabel("Images without Policy and Draft ");
		imgWithPolicyNoDraft.setColor("#F38630");
		listImageCount.add(imgWithPolicyNoDraft);
		imgWithoutPolicy.setValue(countOfImagesWithoutPolicy());
		imgWithoutPolicy.setColor("#E0E4CC");
		imgWithoutPolicy.setLabel("Images having draft");
		listImageCount.add(imgWithoutPolicy);
		imgWithPolicy.setColor("#E0E4FF");
		imgWithPolicy.setLabel("Images having Policy ");
		imgWithPolicy.setValue(countOfImagesWithPolicy());
		listImageCount.add(imgWithPolicy);
		return listImageCount;
	}

	public User fetchUserById(String id) throws DbException {
		return dashboardImplPersistenceManager.fetchUserById(id);

	}

	public ImageInfo fetchImageById(String id) throws DbException {
		return dashboardImplPersistenceManager.fetchImageById(id);
	}
}
