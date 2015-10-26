package com.intel.director.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.intel.director.api.ui.ImageInfoFilter;
import com.intel.director.api.ui.ImageInfoOrderBy;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.SearchImageByPolicyCriteria;
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

	private IPersistService dashboardImplPersistenceManager;

	public DashboardImpl() {
		dashboardImplPersistenceManager = new DbServiceImpl();
	}

	// All user recent policy
	public List<TrustPolicyDraft> getRecentPolicy() throws DbException {
		Date threeDaysBackDate;
		Calendar calender = Calendar.getInstance();
		calender.setTime(new Date());
		calender.add(Calendar.DATE, -2);
		threeDaysBackDate = calender.getTime();
		TrustPolicyDraftFilter trustPolicyDraftFilter = new TrustPolicyDraftFilter();
		trustPolicyDraftFilter.setCreated_date(threeDaysBackDate);
		TrustPolicyDraftOrderBy tpOrderBy = new TrustPolicyDraftOrderBy();
		tpOrderBy.setTrustPolicyDraftFields(TrustPolicyDraftFields.EDITED_DATE);
		tpOrderBy.setOrderBy(OrderByEnum.DESC);

		List<TrustPolicyDraft> temp = dashboardImplPersistenceManager
				.fetchPolicyDrafts(trustPolicyDraftFilter, tpOrderBy);

		return temp;

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

	// New imported images without policy
	public List<ImageInfo> getImagesWithoutPolicy() throws DbException {
		List<ImageInfo> imagesWithoutPolicy = new ArrayList<ImageInfo>();
		List<ImageInfo> temp = new ArrayList<ImageInfo>();
		Date threeDaysBackDate;
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -3);
		threeDaysBackDate = c.getTime();
		ImageInfoFilter imageInfoFilter = new ImageInfoFilter();
		imageInfoFilter.setCreated_date(threeDaysBackDate);
		imageInfoFilter.setEdited_date(threeDaysBackDate);
		ImageInfoOrderBy imgOrderBy = new ImageInfoOrderBy();
		imgOrderBy.setImgFields(ImageInfoFields.CREATED_DATE);
		imgOrderBy.setOrderBy(OrderByEnum.DESC);
		imagesWithoutPolicy = dashboardImplPersistenceManager.fetchImages(
				imageInfoFilter, imgOrderBy);
		for (ImageInfo mwTPD : imagesWithoutPolicy) {
			if (mwTPD.getTrust_policy_draft_id() == null
					&& mwTPD.getTrust_policy_id() == null) {
				temp.add(mwTPD);
			}
		}
		return temp;
	}

	// Recently deployed Images
	public List<ImageUploadRequest> getRecentlyDeployedImages()
			throws DbException {
		ImageStoreUploadOrderBy orderBy = new ImageStoreUploadOrderBy();
		TdaasUtil tdaasUtil = new TdaasUtil();

		orderBy.setImgStoreUploadFields(ImageStoreUploadFields.DATE);
		orderBy.setOrderBy(OrderByEnum.DESC);
		List<ImageUploadRequest> imgUploadRequest = new ArrayList<ImageUploadRequest>();
		List<ImageStoreUploadTransferObject> list = dashboardImplPersistenceManager
				.fetchImageUploads(orderBy);
		for (ImageStoreUploadTransferObject imgUTO : list) {
			imgUploadRequest.add(tdaasUtil.toImageUpload(imgUTO));
		}
		return imgUploadRequest;

	}

	// Images ready to deploy
	public List<ImagesReadyToDeployResponse> getImagesReadyToDeploy()
			throws DbException, DirectorException {
		List<ImagesReadyToDeployResponse> resultList = new ArrayList<ImagesReadyToDeployResponse>();
		ImagesReadyToDeployResponse result = new ImagesReadyToDeployResponse();
		TdaasUtil tdaasUtil = new TdaasUtil();
		List<ImageInfo> images = dashboardImplPersistenceManager
				.fetchImages(null);
		
		if(images == null)
		{
			return resultList;
		}
		
		ImageActionImpl imgAction = new ImageActionImpl();
		List<ImageActionObject> imageactions = imgAction.getdata();
		List<ImageStoreUploadTransferObject> image_store_list = dashboardImplPersistenceManager
				.fetchImageUploads(null);
		int flag = 0;
		for (ImageInfo image : images) {

			if (image.getImage_format() != null) {
				if (imageactions != null) {
					for (ImageActionObject imageaction : imageactions) {
						if (imageaction.getImage_id().equals(image.getId())) {
							flag = 1;
							break;
						}
					}
				}

				if (flag == 1) {
					flag = 0;
					continue;
				}

				if (imageactions != null) {
					for (ImageStoreUploadTransferObject image_store : image_store_list) {
						if (image_store.getImg().getId().equals(image.getId())) {
							flag = 1;
							break;
						}
					}
				}
				if (flag == 1) {
					flag = 0;
					continue;
				} else {
					flag = 0;
					result = tdaasUtil.toImageReadyToDeploy(image);
					resultList.add(result);
					result = new ImagesReadyToDeployResponse();
				}

			}
		}

		return resultList;

	}

	// Upload in progress
	public List<ImageActionObject> uploadInProgress() throws DbException {
		List<ImageActionObject> imgAction = new ArrayList<ImageActionObject>();
		List<ImageActionObject> uploadInProgress = new ArrayList<ImageActionObject>();
		imgAction = dashboardImplPersistenceManager.searchByAction();
		String actions;
		CharSequence upload = "Upload";

		for (ImageActionObject imgObj : imgAction) {
			actions = imgObj.getCurrent_task_name();
			if (actions.contains(upload) || actions.contains(Constants.IN_PROGRESS)) {
				uploadInProgress.add(imgObj);
			}
		}
		String imgName;
		for (int it = 0; it < uploadInProgress.size(); it++) {
			imgName = fetchImageById(uploadInProgress.get(it).getImage_id())
					.getName();
			uploadInProgress.get(it).setImage_id(imgName);
		}

		return uploadInProgress;
	}

	public int countOfImagesWithPolicy() throws DbException {
		ImageInfoFilter imageInfoFilter = new ImageInfoFilter();
		imageInfoFilter.setPolicyCriteria(SearchImageByPolicyCriteria.WITH);
		return dashboardImplPersistenceManager
				.getTotalImagesCount(imageInfoFilter);

	}

	public int countOfImagesWithoutPolicy() throws DbException {
		ImageInfoFilter imageInfoFilter = new ImageInfoFilter();
		imageInfoFilter.setPolicyCriteria(SearchImageByPolicyCriteria.WITHOUT);
		return dashboardImplPersistenceManager
				.getTotalImagesCount(imageInfoFilter);

	}

	public int countOfImagesWithPolicyNoDraft() throws DbException {
		List<ImageInfo> imagesWithoutPolicy = new ArrayList<ImageInfo>();
		ImageInfoFilter imageInfoFilter = new ImageInfoFilter();
		imageInfoFilter.setPolicyCriteria(SearchImageByPolicyCriteria.WITH);
		ImageInfoOrderBy imgOrderBy = new ImageInfoOrderBy();
		imgOrderBy.setImgFields(ImageInfoFields.ID);
		imgOrderBy.setOrderBy(OrderByEnum.DESC);
		imagesWithoutPolicy = dashboardImplPersistenceManager.fetchImages(
				imageInfoFilter, imgOrderBy);
		int index = 0, count = 0;
		for (ImageInfo mwTPD : imagesWithoutPolicy) {
			if (imagesWithoutPolicy.get(index).getTrust_policy_draft_id() == null) {
				count++;
			}
			index++;
		}
		return count;
	}

	public ArrayList<ImageCountPieChart> pieChart() throws DbException {
		ImageCountPieChart imgWithPolicyNoDraft = new ImageCountPieChart();
		ImageCountPieChart imgWithoutPolicy = new ImageCountPieChart();
		ImageCountPieChart imgWithPolicy = new ImageCountPieChart();
		ArrayList<ImageCountPieChart> listImageCount = new ArrayList();
		imgWithPolicyNoDraft.setValue(countOfImagesWithPolicyNoDraft());
		imgWithPolicyNoDraft.setLabel("Images with Policy No Draft ");
		imgWithPolicyNoDraft.setColor("#F38630");
		listImageCount.add(imgWithPolicyNoDraft);
		imgWithoutPolicy.setValue(countOfImagesWithoutPolicy());
		imgWithoutPolicy.setColor("#E0E4CC");
		imgWithoutPolicy.setLabel("Images Without Policy ");
		listImageCount.add(imgWithoutPolicy);
		imgWithPolicy.setColor("#E0E4FF");
		imgWithPolicy.setLabel("Images with Policy ");
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
