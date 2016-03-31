package com.intel.director.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.HashTypeObject;
import com.intel.director.api.ImageInfoResponse;
import com.intel.director.api.ImageListResponse;
import com.intel.director.api.ImportPolicyTemplateResponse;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.SshSettingRequest;
import com.intel.director.api.SshSettingResponse;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.TrustPolicyResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.api.UpdateTrustPolicyRequest;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.TrustPolicyDraftFilter;
import com.intel.director.api.ui.TrustPolicyDraftResponse;
import com.intel.director.images.exception.DirectorException;
import com.intel.mtwilson.director.db.exception.DbException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * this interface serves the purpose of wrapping all the methods that the web
 * service would need. This service implementation would internally call the
 * DAO, the XML generation class etc.
 * 
 * @author Siddharth
 */
public interface ImageService {

	public MountImageResponse mountImage(String imageId, String user)
			throws DirectorException;

	public UnmountImageResponse unMountImage(String imageId, String user)
			throws DirectorException;

	public TrustDirectorImageUploadResponse uploadImageToTrustDirector(
			String image_id, InputStream filInputStream)
			throws DirectorException;

	public SearchImagesResponse searchImages(
			SearchImagesRequest searchImagesRequest) throws DirectorException;

	public SearchFilesInImageResponse searchFilesInImage(
			SearchFilesInImageRequest searchFilesInImageRequest)
			throws DirectorException;;

	public String getTrustPolicyForImage(String imageId);

	public TrustPolicyDraftResponse editTrustPolicyDraft(
			TrustPolicyDraftEditRequest trustPolicyDraftEditRequest)
			throws DirectorException;

	public CreateTrustPolicyMetaDataResponse saveTrustPolicyMetaData(
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest)
			throws DirectorException;

	public CreateTrustPolicyMetaDataResponse getPolicyMetadata(String draftid)
			throws DirectorException;

	public ImageListResponse getImagesForVM(List<ImageInfo> images)
			throws DirectorException;

	public CreateTrustPolicyMetaDataResponse getPolicyMetadataForImage(
			String image_id) throws DirectorException;
	
	public String fetchImageIdByDraftOrPolicy(String draftOrPolicyId);

	public String createTrustPolicy(String trust_policy_draft_id)
			throws DirectorException;

	public TrustPolicy getTrustPolicyByTrustId(String trustId);

	public TrustPolicyDraftResponse createPolicyDraftFromPolicy(String imageId)
			throws DirectorException;

	public String getDisplayNameForImage(String image_id)
			throws DirectorException;

	ImageListResponse getImagesForBareMetal(List<ImageInfo> images)
			throws DirectorException;

	public ImageListResponse getBareMetalLive(List<ImageInfo> images)
			throws DirectorException;

	public ImageListResponse getImages(List<ImageInfo> images,
			String deployment_type) throws DirectorException;

	public String getFilepathForImage(String imageId, boolean isModified)
			throws DbException;

	public TrustPolicy getTrustPolicyByImageId(String imageId)
			throws DirectorException;

	public ImportPolicyTemplateResponse importPolicyTemplate(String imageId)
			throws DirectorException;

	public void deleteTrustPolicy(String imageId) throws DirectorException;

	public TrustDirectorImageUploadResponse createUploadImageMetadataImpl(
			String image_deployments, String image_format, String fileName,
			long fileSize, String repository, String tag)
			throws DirectorException;

	public File createTarballOfPolicyAndManifest(String imageId)
			throws DirectorException;

	public SshSettingRequest getBareMetalMetaData(String image_id)
			throws DirectorException;

	public void deletePasswordForHost(String image_id) throws DirectorException;

	public void deleteImage(String imageId) throws DirectorException;

	boolean doesPolicyNameExist(String display_name, String image_id)
			throws DirectorException;

	boolean doesImageNameExist(String fileName) throws DirectorException;

	void deleteTrustPolicyDraft(String trust_policy_draft_id)
			throws DirectorException;

	public TrustPolicyResponse getTrustPolicyMetaData(String trust_policy_id)
			throws DirectorException;

	public void updateTrustPolicy(
			UpdateTrustPolicyRequest updateTrustPolicyRequest,
			String trust_policy_id) throws DirectorException;

	public String getImageByTrustPolicyDraftId(String trustPolicydraftId)
			throws DirectorException;


	public ImageInfo fetchImageById(String id) throws DirectorException;

	public ImageInfoResponse getImageDetails(String imageId)
			throws DirectorException;
	
	public List<TrustPolicyDraft> getTrustPolicyDrafts(
			TrustPolicyDraftFilter trustPolicyDraftFilter)
			throws DirectorException;

	public List<SshSettingRequest> sshData() throws DirectorException;

	public SshSettingResponse addHost(SshSettingRequest sshSettingRequest)
			throws DirectorException;

	public void postSshData(SshSettingRequest sshSettingRequest)
			throws DirectorException;

	public SshSettingResponse updateSshData(SshSettingRequest sshSettingRequest)
			throws DirectorException;

	public void updateSshDataById(String sshId) throws DirectorException;

	public void deleteSshSetting(String sshId) throws DirectorException;

	public SshSettingRequest fetchSshInfoByImageId(String image_id)
			throws DirectorException;
			
	public TrustPolicyDraft fetchTrustpolicydraftById(String trustPolicyDraftId);
	
	public void dockerPull(String imageId)
			throws DirectorException;
	
	public void dockerSetup(String imageId)
			throws DirectorException;

	public List<ImageInfo> getStalledImages() throws DirectorException;

	public List<HashTypeObject> getImageHashType(String deploymentType) throws DirectorException;
	
}

