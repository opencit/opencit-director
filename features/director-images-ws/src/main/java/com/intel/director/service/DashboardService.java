package com.intel.director.service;

import java.util.ArrayList;
import java.util.List;

import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageUploadRequest;
import com.intel.director.api.ImagesReadyToDeployResponse;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftRequest;
import com.intel.director.api.User;
import com.intel.director.api.ui.ImageCountPieChart;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.images.exception.DirectorException;
import com.intel.mtwilson.director.db.exception.DbException;

public interface DashboardService {
	public List<TrustPolicyDraft> getRecentPolicy() throws DbException;

	public List<TrustPolicyDraftRequest> getRecentUserPolicy(
			String created_by_user_id) throws DbException;

	public List<ImageInfo> getImagesWithoutPolicy() throws DbException;

	public List<ImageUploadRequest> getRecentlyDeployedImages()
			throws DbException;

	public List<ImagesReadyToDeployResponse> getImagesReadyToDeploy()
			throws DbException, DirectorException;

	public List<ImageActionObject> uploadInProgress() throws DbException;

	public int countOfImagesWithPolicy() throws DbException;

	public int countOfImagesWithoutPolicy() throws DbException;

	public int countOfImagesWithPolicyNoDraft() throws DbException;

	public ArrayList<ImageCountPieChart> pieChart() throws DbException;

	public User fetchUserById(String id) throws DbException;

	public ImageInfo fetchImageById(String id) throws DbException;

}
