package com.intel.mtwilson.director.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionTask;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageStoreDetailsTransferObject;
import com.intel.director.api.ImageStoreSettings;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.PolicyTemplateInfo;
import com.intel.director.api.PolicyUploadTransferObject;
import com.intel.director.api.SshKey;
import com.intel.director.api.SshPassword;
import com.intel.director.api.SshSettingInfo;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.User;
import com.intel.director.api.ui.ImageActionFields;
import com.intel.director.api.ui.ImageAttributeFields;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageInfoFields;
import com.intel.director.api.ui.ImageStoreDetailsField;
import com.intel.director.api.ui.ImageStoreFields;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.PolicyUploadFields;
import com.intel.director.api.ui.TrustPolicyDraftFields;
import com.intel.director.api.ui.TrustPolicyFields;
import com.intel.director.api.ui.UserFields;
import com.intel.director.common.Constants;
import com.intel.mtwilson.director.data.MwHost;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.data.MwImageAction;
import com.intel.mtwilson.director.data.MwImageStore;
import com.intel.mtwilson.director.data.MwImageStoreDetails;
import com.intel.mtwilson.director.data.MwImageStoreSettings;
import com.intel.mtwilson.director.data.MwImageUpload;
import com.intel.mtwilson.director.data.MwPolicyTemplate;
import com.intel.mtwilson.director.data.MwPolicyUpload;
import com.intel.mtwilson.director.data.MwSshKey;
import com.intel.mtwilson.director.data.MwSshPassword;
import com.intel.mtwilson.director.data.MwTrustPolicy;
import com.intel.mtwilson.director.data.MwTrustPolicyDraft;
import com.intel.mtwilson.director.data.MwUser;

public class Mapper {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(Mapper.class);

	Map<ImageAttributeFields, String> imageAttributestoDataMapper;

	public Map<ImageAttributeFields, String> getImageAttributesToDataMapper() {

		if (imageAttributestoDataMapper == null) {
			imageAttributestoDataMapper = new EnumMap<ImageAttributeFields, String>(
					ImageAttributeFields.class);
			imageAttributestoDataMapper.put(ImageAttributeFields.ID, "id");
			imageAttributestoDataMapper.put(ImageAttributeFields.NAME, "name");
			imageAttributestoDataMapper.put(
					ImageAttributeFields.IMAGE_DEPLOYMENTS,
					"imageDeploymentType");
			imageAttributestoDataMapper.put(ImageAttributeFields.IMAGE_FORMAT,
					"imageFormat");
			imageAttributestoDataMapper.put(ImageAttributeFields.SENT, "sent");
			imageAttributestoDataMapper.put(ImageAttributeFields.STATUS,
					"status");
			imageAttributestoDataMapper.put(ImageAttributeFields.IMAGE_SIZE,
					"contentlength");
			imageAttributestoDataMapper.put(ImageAttributeFields.LOCATION,
					"location");
			imageAttributestoDataMapper.put(
					ImageAttributeFields.MOUNTED_BY_USER_ID, "mountedByUserId");
			imageAttributestoDataMapper.put(
					ImageAttributeFields.CREATED_BY_USER_ID, "createdByUserId");
			imageAttributestoDataMapper.put(ImageAttributeFields.CREATED_DATE,
					"createdDate");
			imageAttributestoDataMapper.put(
					ImageAttributeFields.EDITED_BY_USER_ID, "editedByUserId");
			imageAttributestoDataMapper.put(ImageAttributeFields.EDITED_DATE,
					"editedDate");
		}
		return imageAttributestoDataMapper;
	}

	Map<ImageInfoFields, String> imageInfotoDataMapper;

	public Map<ImageInfoFields, String> getImageInfoToDataColumnsMapper() {

		if (imageAttributestoDataMapper == null) {
			imageInfotoDataMapper = new EnumMap<ImageInfoFields, String>(
					ImageInfoFields.class);
			imageInfotoDataMapper.put(ImageInfoFields.ID, "id");
			imageInfotoDataMapper.put(ImageInfoFields.NAME, "name");
			imageInfotoDataMapper.put(ImageInfoFields.IMAGE_DEPLOYMENTS,
					"image_deployments");
			imageInfotoDataMapper.put(ImageInfoFields.IMAGE_FORMAT,
					"image_format");
			imageInfotoDataMapper.put(ImageInfoFields.SENT, "sent");
			imageInfotoDataMapper.put(ImageInfoFields.STATUS, "status");
			imageInfotoDataMapper.put(ImageInfoFields.IMAGE_SIZE,
					"content_length");
			imageInfotoDataMapper.put(ImageInfoFields.LOCATION, "location");
			imageInfotoDataMapper.put(ImageInfoFields.MOUNTED_BY_USER_ID,
					"mountedByUserId");
			imageInfotoDataMapper.put(ImageInfoFields.CREATED_BY_USER_ID,
					"created_by_user_id");
			imageInfotoDataMapper.put(ImageInfoFields.CREATED_DATE,
					"created_date");
			imageInfotoDataMapper.put(ImageInfoFields.EDITED_BY_USER_ID,
					"edited_by_user_id");
			imageInfotoDataMapper.put(ImageInfoFields.EDITED_DATE,
					"edited_date");
			imageInfotoDataMapper.put(ImageInfoFields.TRUST_POLICY_ID,
					"trust_policy_id");
			imageInfotoDataMapper.put(ImageInfoFields.TRUST_POLICY_NAME,
					"trust_policy_name");
			imageInfotoDataMapper.put(ImageInfoFields.TRUST_POLICY_DRAFT_ID,
					"trust_policy_draft_id");
			imageInfotoDataMapper.put(ImageInfoFields.TRUST_POLICY_DRAFT_NAME,
					"trust_policy_name");

		}
		return imageInfotoDataMapper;
	}

	Map<UserFields, String> userAttributestoDataMapper;

	public Map<UserFields, String> getUserAttributestoDataMapper() {

		if (userAttributestoDataMapper == null) {
			userAttributestoDataMapper = new EnumMap<UserFields, String>(
					UserFields.class);
			userAttributestoDataMapper.put(UserFields.ID, "id");
			userAttributestoDataMapper.put(UserFields.USERNAME, "username");
			userAttributestoDataMapper.put(UserFields.DISPLAYNAME,
					"displayname");
			userAttributestoDataMapper.put(UserFields.EMAIL, "email");

		}
		return userAttributestoDataMapper;
	}

	Map<ImageStoreFields, String> imageStoreAttributestoDataMapper;

	public Map<ImageStoreFields, String> getImageStoreAttributesMapper() {

		if (imageStoreAttributestoDataMapper == null) {
			imageStoreAttributestoDataMapper = new EnumMap<ImageStoreFields, String>(
					ImageStoreFields.class);
			imageStoreAttributestoDataMapper.put(ImageStoreFields.ID, "id");
			imageStoreAttributestoDataMapper.put(
					ImageStoreFields.ARTIFACT_TYPE, "artifact_type");
			imageStoreAttributestoDataMapper.put(ImageStoreFields.DELETED,
					"deleted");
			imageStoreAttributestoDataMapper.put(ImageStoreFields.NAME, "name");
			imageStoreAttributestoDataMapper.put(ImageStoreFields.CONNECTOR,
					"connector");
		}
		return imageStoreAttributestoDataMapper;
	}
	
	Map<ImageActionFields, String> imageActionsAttributestoDataMapper;

	public Map<ImageActionFields, String> getImageActionsAttributesMapper() {

		if (imageActionsAttributestoDataMapper == null) {
			imageActionsAttributestoDataMapper = new EnumMap<ImageActionFields, String>(
					ImageActionFields.class);
			imageActionsAttributestoDataMapper.put(ImageActionFields.ID, "id");
			imageActionsAttributestoDataMapper.put(
					ImageActionFields.CURRENT_TASK_NAME, "current_task_name");
			imageActionsAttributestoDataMapper.put(
					ImageActionFields.CURRENT_TASK_STATUS, "current_task_status");
			imageActionsAttributestoDataMapper.put(
					ImageActionFields.IMAGE_ID, "image_id");
			imageActionsAttributestoDataMapper.put(
					ImageActionFields.DATE, "executionTime");
		
		}
		return imageActionsAttributestoDataMapper;
	}

	Map<ImageStoreDetailsField, String> imageStoreDetailsAttributestoDataMapper;

	public Map<ImageStoreDetailsField, String> getImageStoreDetailsAttributesMapper() {

		if (imageStoreDetailsAttributestoDataMapper == null) {
			imageStoreDetailsAttributestoDataMapper = new EnumMap<ImageStoreDetailsField, String>(
					ImageStoreDetailsField.class);
			imageStoreDetailsAttributestoDataMapper.put(
					ImageStoreDetailsField.ID, "id");
			imageStoreDetailsAttributestoDataMapper.put(
					ImageStoreDetailsField.IMAGE_STORE_ID, "image_store_id");
			imageStoreDetailsAttributestoDataMapper.put(
					ImageStoreDetailsField.KEY, "key");
			imageStoreDetailsAttributestoDataMapper.put(
					ImageStoreDetailsField.VALUE, "value");
		}
		return imageStoreDetailsAttributestoDataMapper;
	}

	Map<TrustPolicyFields, String> policyAttributestoDataMapper;

	public Map<TrustPolicyFields, String> getPolicyAttributestoDataMapper() {

		if (policyAttributestoDataMapper == null) {
			policyAttributestoDataMapper = new EnumMap<TrustPolicyFields, String>(
					TrustPolicyFields.class);
			policyAttributestoDataMapper.put(TrustPolicyFields.ID, "id");
			policyAttributestoDataMapper.put(TrustPolicyFields.DESCRIPTION,
					"description");
			policyAttributestoDataMapper.put(TrustPolicyFields.NAME, "name");
			policyAttributestoDataMapper.put(TrustPolicyFields.TRUST_POLICY,
					"trustPolicy");
			policyAttributestoDataMapper.put(
					TrustPolicyFields.CREATED_BY_USER_ID, "createdByUserId");
			policyAttributestoDataMapper.put(TrustPolicyFields.CREATED_DATE,
					"createdDate");
			policyAttributestoDataMapper.put(
					TrustPolicyFields.EDITED_BY_USER_ID, "editedByUserId");
			policyAttributestoDataMapper.put(TrustPolicyFields.EDITED_DATE,
					"editedDate");

			policyAttributestoDataMapper.put(TrustPolicyFields.IMAGE_ID, "id");
			policyAttributestoDataMapper.put(TrustPolicyFields.IMAGE_NAME,
					"name");
			policyAttributestoDataMapper.put(
					TrustPolicyFields.IMAGE_CREATION_DATE, "createdDate");
			policyAttributestoDataMapper.put(TrustPolicyFields.IMAGE_FORMAT,
					"imageFormat");
			policyAttributestoDataMapper.put(
					TrustPolicyFields.IMAGE_DEPLOYMENT, "imageDeploymentType");
		}
		return policyAttributestoDataMapper;
	}

	Map<TrustPolicyDraftFields, String> policyDraftAttributestoDataMapper;

	public Map<TrustPolicyDraftFields, String> getPolicyDraftAttributestoDataMapper() {

		if (policyDraftAttributestoDataMapper == null) {
			policyDraftAttributestoDataMapper = new EnumMap<TrustPolicyDraftFields, String>(
					TrustPolicyDraftFields.class);
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.ID,
					"id");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.TRUST_POLICY_DRAFT,
					"trustPolicyDraft");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.NAME,
					"name");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.CREATED_BY_USER_ID,
					"createdByUserId");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.CREATED_DATE, "createdDate");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.EDITED_BY_USER_ID, "editedByUserId");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.EDITED_DATE, "editedDate");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.IMAGE_ID, "id");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.IMAGE_NAME, "name");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.IMAGE_CREATION_DATE, "createdDate");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.IMAGE_FORMAT, "imageFormat");
			policyDraftAttributestoDataMapper.put(
					TrustPolicyDraftFields.IMAGE_DEPLOYMENT,
					"imageDeploymentType");
		}
		return policyDraftAttributestoDataMapper;
	}

	Map<ImageStoreUploadFields, String> imageUploadtAttributestoDataMapper;

	public Map<ImageStoreUploadFields, String> getImageUploadtAttributestoDataMapper() {

		if (imageUploadtAttributestoDataMapper == null) {
			imageUploadtAttributestoDataMapper = new EnumMap<ImageStoreUploadFields, String>(
					ImageStoreUploadFields.class);
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.ID,
					"id");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.DATE,
					"date");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.IMAGE_URI, "imageUri");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.ACTION_ID, "actionId");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.STORE_ARTIFACT_NAME, "storeArtifactName");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.CHECKSUM, "checksum");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.TMP_LOCATION, "tmpLocation");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.STATUS, "status");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.IMAGE_SIZE, "contentlength");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.SENT,
					"sent");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.IMAGE_ID, "id");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.IMAGE_NAME, "name");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.IMAGE_CREATION_DATE, "createdDate");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.IMAGE_FORMAT, "imageFormat");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.IMAGE_DEPOLYMENT,
					"imageDeploymentType");
			imageUploadtAttributestoDataMapper
					.put(ImageStoreUploadFields.STORE_ARTIFACT_ID,
							"storeArtifactId");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.IS_DELETED, "isDeleted");
			imageUploadtAttributestoDataMapper.put(
					ImageStoreUploadFields.UPLOAD_VARIABLES_MD5, "uploadVariablesMd5");

		}
		return imageUploadtAttributestoDataMapper;
	}

	Map<PolicyUploadFields, String> policyUploadtAttributestoDataMapper;

	public Map<PolicyUploadFields, String> getPolicyUploadtAttributestoDataMapper() {

		if (policyUploadtAttributestoDataMapper == null) {
			policyUploadtAttributestoDataMapper = new EnumMap<PolicyUploadFields, String>(
					PolicyUploadFields.class);
			policyUploadtAttributestoDataMapper
					.put(PolicyUploadFields.ID, "id");
			policyUploadtAttributestoDataMapper.put(PolicyUploadFields.DATE,
					"date");
			policyUploadtAttributestoDataMapper.put(
					PolicyUploadFields.POLICY_URI, "policyUri");

			policyUploadtAttributestoDataMapper.put(PolicyUploadFields.STATUS,
					"status");
			policyUploadtAttributestoDataMapper.put(
					PolicyUploadFields.STORE_ARTIFACT_ID, "storeArtifactId");
			policyUploadtAttributestoDataMapper.put(
					PolicyUploadFields.IS_DELETED, "isDeleted");
			policyUploadtAttributestoDataMapper.put(
					PolicyUploadFields.TRUST_POLICY_ID, "id");

		}
		return policyUploadtAttributestoDataMapper;
	}

	public MwImage toData(ImageAttributes imgAttributes) {
		MwImage mwImage = new MwImage();
		mwImage.setImageDeploymentType(imgAttributes.getImage_deployments());
		mwImage.setContentLength(imgAttributes.getImage_size());
		mwImage.setImageFormat(imgAttributes.getImage_format());
		mwImage.setLocation(imgAttributes.getLocation());
		mwImage.setMountedByUserId(imgAttributes.getMounted_by_user_id());
		mwImage.setName(imgAttributes.getImage_name());
		mwImage.setId(imgAttributes.getId());
		mwImage.setStatus(imgAttributes.getStatus());
		mwImage.setDeleted(imgAttributes.isDeleted());
		mwImage.setSent(imgAttributes.getSent());
		mwImage.setCreatedByUserId(imgAttributes.getCreated_by_user_id());
		mwImage.setEditedByUserId(imgAttributes.getEdited_by_user_id());
		mwImage.setTmpLocation(imgAttributes.getTmpLocation());
		mwImage.setUploadVariablesMd5(imgAttributes.getUploadVariableMD5());
		mwImage.setPartition(imgAttributes.getPartition());
		if (imgAttributes.getCreated_date() != null) {
			mwImage.setCreatedDate(imgAttributes.getCreated_date());
		}
		if (imgAttributes.getEdited_date() != null) {
			mwImage.setEditedDate(imgAttributes.getEdited_date());
		}
		if (imgAttributes.getRepository() != null) {
			mwImage.setRepository(imgAttributes.getRepository());
		}
		if (imgAttributes.getTag() != null) {
			mwImage.setTag(imgAttributes.getTag());
		}
		return mwImage;
	}

	public ImageInfo toTransferObject(MwImage mwImage) {
		if (mwImage == null) {
			return null;
		}
		ImageInfo imgInfo = new ImageInfo();
		imgInfo.setId(mwImage.getId());
		imgInfo.setImage_deployments(mwImage.getImageDeploymentType());
		imgInfo.setImage_format(mwImage.getImageFormat());
		imgInfo.setImage_name(mwImage.getName());
		imgInfo.setMounted_by_user_id(mwImage.getMountedByUserId());
		imgInfo.setLocation(mwImage.getLocation());
		imgInfo.setDeleted(mwImage.isDeleted());
		imgInfo.setCreated_by_user_id(mwImage.getCreatedByUserId());
		imgInfo.setEdited_by_user_id(mwImage.getEditedByUserId());
		imgInfo.setCreated_date(mwImage.getCreatedDate());
		imgInfo.setEdited_date(mwImage.getEditedDate());
		imgInfo.setPartition(mwImage.getPartition());
		if (mwImage.getContentLength() != null) {
			imgInfo.setImage_size(mwImage.getContentLength());
		}
		imgInfo.setStatus(mwImage.getStatus());
		if (mwImage.getSent() != null) {
			imgInfo.setSent(mwImage.getSent());
		}
		imgInfo.setUploadVariableMD5(mwImage.getUploadVariablesMd5());
		imgInfo.setTmpLocation(mwImage.getTmpLocation());
		if (mwImage.getRepository() != null) {
			imgInfo.setRepository(mwImage.getRepository());
		}
		if (mwImage.getTag() != null) {
			imgInfo.setTag(mwImage.getTag());
		}
		return imgInfo;
	}

	public MwUser toData(User user) {
		MwUser mwUser = new MwUser();

		mwUser.setId(user.getId());
		mwUser.setUsername(user.getUsername());
		mwUser.setDisplayname(user.getDisplayname());
		mwUser.setEmail(user.getEmail());

		return mwUser;
	}

	public User toTransferObject(MwUser mwUser) {

		User user = new User();
		user.setId(mwUser.getId());
		user.setDisplayname(mwUser.getDisplayname());
		user.setUsername(mwUser.getUsername());
		user.setEmail(mwUser.getEmail());

		return user;
	}

	public MwTrustPolicy toData(TrustPolicy trustPolicy) {
		MwTrustPolicy mwTrustPolicy = new MwTrustPolicy();
		mwTrustPolicy.setId(trustPolicy.getId());

		mwTrustPolicy.setName(mwTrustPolicy.getName());
		MwImage mwImage = toData(trustPolicy.getImgAttributes());
		mwTrustPolicy.setTrustPolicy(toCharacterArray((trustPolicy
				.getTrust_policy())));
		mwTrustPolicy.setImage(mwImage);
		mwTrustPolicy.setDescription(trustPolicy.getDescription());
		mwTrustPolicy.setCreatedByUserId(trustPolicy.getCreated_by_user_id());
		mwTrustPolicy.setEditedByUserId(trustPolicy.getEdited_by_user_id());
		mwTrustPolicy.setArchive(trustPolicy.isArchive());
		if (trustPolicy.getCreated_date() != null) {
			mwTrustPolicy.setCreatedDate(trustPolicy.getCreated_date());
		}
		if (trustPolicy.getEdited_date() != null) {
			mwTrustPolicy.setEditedDate(trustPolicy.getEdited_date());
		}
		mwTrustPolicy.setDisplay_name(trustPolicy.getDisplay_name());
		return mwTrustPolicy;
	}

	public TrustPolicy toTransferObject(MwTrustPolicy mwTrustPolicy) {
		TrustPolicy trustPolicy = new TrustPolicy();
		trustPolicy.setId(mwTrustPolicy.getId());
		trustPolicy.setDescription(mwTrustPolicy.getDescription());
		trustPolicy.setTrust_policy(fromCharacterArray(mwTrustPolicy
				.getTrustPolicy()));
		trustPolicy.setName(mwTrustPolicy.getName());
		ImageAttributes imgAttributes = toTransferObject(mwTrustPolicy
				.getImage());
		trustPolicy.setImgAttributes(imgAttributes);
		trustPolicy.setArchive(mwTrustPolicy.isArchive());
		trustPolicy.setCreated_by_user_id(mwTrustPolicy.getCreatedByUserId());
		trustPolicy.setEdited_by_user_id(mwTrustPolicy.getEditedByUserId());
		trustPolicy.setCreated_date(mwTrustPolicy.getCreatedDate());
		trustPolicy.setEdited_date(mwTrustPolicy.getEditedDate());
		trustPolicy.setDisplay_name(mwTrustPolicy.getDisplay_name());
		return trustPolicy;
	}

	public MwTrustPolicyDraft toData(TrustPolicyDraft trustPolicyDraft) {
		MwTrustPolicyDraft mwTrustPolicyDraft = new MwTrustPolicyDraft();
		mwTrustPolicyDraft.setId(trustPolicyDraft.getId());

		mwTrustPolicyDraft.setName(trustPolicyDraft.getName());
		MwImage mwImage = toData(trustPolicyDraft.getImgAttributes());
		mwTrustPolicyDraft
				.setTrustPolicyDraft(toCharacterArray(trustPolicyDraft
						.getTrust_policy_draft()));
		mwTrustPolicyDraft.setImage(mwImage);

		mwTrustPolicyDraft.setCreatedByUserId(trustPolicyDraft
				.getCreated_by_user_id());
		mwTrustPolicyDraft.setEditedByUserId(trustPolicyDraft
				.getEdited_by_user_id());
		if (trustPolicyDraft.getCreated_date() != null) {
			mwTrustPolicyDraft.setCreatedDate(trustPolicyDraft
					.getCreated_date());
		}
		if (trustPolicyDraft.getEdited_date() != null) {
			mwTrustPolicyDraft.setEditedDate(trustPolicyDraft.getEdited_date());
		}
		mwTrustPolicyDraft.setDisplay_name(trustPolicyDraft.getDisplay_name());
		return mwTrustPolicyDraft;
	}

	public TrustPolicyDraft toTransferObject(
			MwTrustPolicyDraft mwTrustPolicyDraft) {
		if (mwTrustPolicyDraft != null) {
			TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
			trustPolicyDraft.setId(mwTrustPolicyDraft.getId());
			trustPolicyDraft
					.setTrust_policy_draft(fromCharacterArray(mwTrustPolicyDraft
							.getTrustPolicyDraft()));
			trustPolicyDraft.setName(mwTrustPolicyDraft.getName());
			ImageAttributes imgAttributes = toTransferObject(mwTrustPolicyDraft
					.getImage());
			trustPolicyDraft.setImgAttributes(imgAttributes);
			trustPolicyDraft.setCreated_by_user_id(mwTrustPolicyDraft
					.getCreatedByUserId());
			trustPolicyDraft.setEdited_by_user_id(mwTrustPolicyDraft
					.getEditedByUserId());
			trustPolicyDraft.setCreated_date(mwTrustPolicyDraft
					.getCreatedDate());
			trustPolicyDraft.setEdited_date(mwTrustPolicyDraft.getEditedDate());
			trustPolicyDraft.setDisplay_name(mwTrustPolicyDraft
					.getDisplay_name());
			return trustPolicyDraft;
		}
		return null;
	}

	public MwImageUpload toData(
			ImageStoreUploadTransferObject imageStoreUploadTO) {
		MwImageUpload mwImageUpload = new MwImageUpload();
		mwImageUpload.setId(imageStoreUploadTO.getId());
		mwImageUpload.setChecksum(imageStoreUploadTO.getChecksum());
		mwImageUpload.setContentlength(imageStoreUploadTO.getImage_size());
		mwImageUpload.setStoreArtifactId(imageStoreUploadTO
				.getStoreArtifactId());
		mwImageUpload.setStoreArtifactName(imageStoreUploadTO.getStoreArtifactName());
		mwImageUpload.setActionId(imageStoreUploadTO.getActionId());
		mwImageUpload.setDeleted(imageStoreUploadTO.isDeleted());
		MwImage mwImage = toData(imageStoreUploadTO.getImg());
		mwImageUpload.setImage(mwImage);
		if (imageStoreUploadTO.getPolicyUploadId() != null) {
			mwImageUpload.setPolicyUploadId(imageStoreUploadTO
					.getPolicyUploadId());
		}
		mwImageUpload.setTmpLocation(imageStoreUploadTO.getTmp_location());
		mwImageUpload.setUploadVariablesMd5(imageStoreUploadTO
				.getUploadVariableMD5());
		mwImageUpload.setSent(imageStoreUploadTO.getSent());
		if (imageStoreUploadTO.getImage_uri() != null) {
			mwImageUpload.setImageUri(toCharacterArray(imageStoreUploadTO
					.getImage_uri()));
		}
		mwImageUpload.setTmpLocation(imageStoreUploadTO.getTmp_location());
		mwImageUpload.setStatus(imageStoreUploadTO.getStatus());
		if (imageStoreUploadTO.getDate() != null) {
			mwImageUpload.setDate(imageStoreUploadTO
					.getDate());
		}

		return mwImageUpload;
	}

	public MwPolicyUpload toData(PolicyUploadTransferObject policyUploadTO) {
		MwPolicyUpload mwPolicyUpload = new MwPolicyUpload();
		mwPolicyUpload.setId(policyUploadTO.getId());

		MwTrustPolicy mwPolicy = toData(policyUploadTO.getTrust_policy());
		mwPolicyUpload.setTrustPolicy(mwPolicy);

		if (policyUploadTO.getPolicy_uri() != null) {
			mwPolicyUpload.setPolicyUri(toCharacterArray(policyUploadTO
					.getPolicy_uri()));
		}

		mwPolicyUpload.setStatus(policyUploadTO.getStatus());
		if (policyUploadTO.getDate() != null) {
			mwPolicyUpload.setDate(policyUploadTO.getDate()					);
		}
		mwPolicyUpload.setStoreArtifactId(policyUploadTO.getStoreArtifactId());

		mwPolicyUpload.setDeleted(policyUploadTO.isDeleted());
		mwPolicyUpload.setUploadVariablesMd5(policyUploadTO
				.getUploadVariableMD5());

		return mwPolicyUpload;
	}

	public ImageStoreUploadTransferObject toTransferObject(
			MwImageUpload mwImageUpload) {
		ImageStoreUploadTransferObject imageStoreUploadTO = new ImageStoreUploadTransferObject();
		imageStoreUploadTO.setId(mwImageUpload.getId());
		imageStoreUploadTO.setChecksum(mwImageUpload.getChecksum());
		imageStoreUploadTO.setDate(mwImageUpload.getDate());
		imageStoreUploadTO.setImage_size(mwImageUpload.getContentlength());
		imageStoreUploadTO.setSent(mwImageUpload.getSent());
		imageStoreUploadTO.setStatus(mwImageUpload.getStatus());
		imageStoreUploadTO.setPolicyUploadId(mwImageUpload.getPolicyUploadId());
		imageStoreUploadTO.setUploadVariableMD5(mwImageUpload
				.getUploadVariablesMd5());
		imageStoreUploadTO.setTmp_location(mwImageUpload.getTmpLocation());
		imageStoreUploadTO.setDeleted(mwImageUpload.isDeleted());
		imageStoreUploadTO.setStoreArtifactId(mwImageUpload
				
				.getStoreArtifactId());
		imageStoreUploadTO.setActionId(mwImageUpload.getActionId());
		imageStoreUploadTO.setStoreArtifactName(mwImageUpload.getStoreArtifactName());
		if (mwImageUpload.getImageUri() != null) {
			imageStoreUploadTO.setImage_uri(fromCharacterArray(mwImageUpload
					.getImageUri()));
		}

		if (mwImageUpload.getStore() != null) {
			imageStoreUploadTO.setStoreId(mwImageUpload.getStore().getId());
			imageStoreUploadTO.setStoreName(mwImageUpload.getStore().getName());
		}
		ImageAttributes imgAttributes = toTransferObject(mwImageUpload
				.getImage());
		imageStoreUploadTO.setImg(imgAttributes);
		return imageStoreUploadTO;
	}

	public PolicyUploadTransferObject toTransferObject(
			MwPolicyUpload mwPolicyUpload) {
		PolicyUploadTransferObject policyUploadTO = new PolicyUploadTransferObject();
		policyUploadTO.setId(mwPolicyUpload.getId());

		policyUploadTO.setDate(mwPolicyUpload.getDate());

		policyUploadTO.setStatus(mwPolicyUpload.getStatus());
		policyUploadTO.setStoreArtifactId(mwPolicyUpload.getStoreArtifactId());
		policyUploadTO.setDeleted(mwPolicyUpload.isDeleted());
		policyUploadTO.setUploadVariableMD5(mwPolicyUpload
				.getUploadVariablesMd5());
		if (mwPolicyUpload.getPolicyUri() != null) {
			policyUploadTO.setPolicy_uri(fromCharacterArray(mwPolicyUpload
					.getPolicyUri()));
		}
		TrustPolicy tp = toTransferObject(mwPolicyUpload.getTrustPolicy());
		policyUploadTO.setTrust_policy(tp);
		if (mwPolicyUpload.getStore() != null) {
			policyUploadTO.setStoreId(mwPolicyUpload.getStore().getId());
			policyUploadTO.setStoreName(mwPolicyUpload.getStore().getName());
		}
		// / policyUploadTO.setImg(tp);
		return policyUploadTO;
	}

	public MwImageStoreSettings toData(ImageStoreSettings imgSettings) {
		MwImageStoreSettings mwSettings = new MwImageStoreSettings();
		mwSettings.setId(imgSettings.getId());
		mwSettings.setName(imgSettings.getName());
		mwSettings.setProvider_class(imgSettings.getProvider_class());
		return mwSettings;
	}

	public ImageStoreSettings toTransferObject(MwImageStoreSettings mwSettings) {
		ImageStoreSettings imgStoreSettings = new ImageStoreSettings();
		imgStoreSettings.setId(mwSettings.getId());
		imgStoreSettings.setName(mwSettings.getName());
		imgStoreSettings.setProvider_class(mwSettings.getProvider_class());
		return imgStoreSettings;
	}

	public Character[] toCharacterArray(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		char[] chars = str.toCharArray();

		Character[] characters = new Character[chars.length];
		for (int i = 0; i < chars.length; i++) {
			characters[i] = chars[i];

		}
		return characters;
	}

	public String fromCharacterArray(Character[] chars) {
		if (ArrayUtils.isEmpty(chars)) {
			return null;
		}
		StringBuilder sb = new StringBuilder(chars.length);
		for (Character c : chars)
			sb.append(c.charValue());

		String str = sb.toString();
		return str;
	}

	public MwImageAction toData(ImageActionObject imageAction) {
		MwImageAction mwImageAction = new MwImageAction();
		Gson gson = new Gson();
		mwImageAction.setImage_id(imageAction.getImage_id());
		mwImageAction.setAction_size_max(imageAction.getAction_size_max());
		mwImageAction.setAction_size(imageAction.getAction_size());
		mwImageAction.setAction_count(imageAction.getAction_count());
		mwImageAction.setAction_completed(imageAction.getAction_completed());
		if (imageAction.getActions() != null) {
			mwImageAction.setAction(gson.toJson(imageAction.getActions()));
		}
		mwImageAction.setCurrent_task_name(imageAction.getCurrent_task_name());
		mwImageAction.setCurrent_task_status(imageAction
				.getCurrent_task_status());
		if (imageAction.getDatetime() != null) {
			mwImageAction.setExecutionTime(imageAction.getDatetime());
		}
		if (imageAction.getCreatedDateTime() != null) {
			mwImageAction.setCreatedTime(imageAction.getCreatedDateTime());
		}

		return mwImageAction;
	}

	public ImageActionObject toTransferObject(MwImageAction mwImageAction) {
		if (mwImageAction == null) {
			return null;
		}
		ImageActionObject imageActionObject = new ImageActionObject();
		imageActionObject.setId(mwImageAction.getId());
		imageActionObject.setImage_id(mwImageAction.getImage_id());
		imageActionObject.setAction_count(mwImageAction.getAction_count());
		imageActionObject.setAction_completed(mwImageAction
				.getAction_completed());
		imageActionObject.setAction_size(mwImageAction.getAction_size());
		imageActionObject
				.setAction_size_max(mwImageAction.getAction_size_max());
		imageActionObject.setCurrent_task_name(mwImageAction
				.getCurrent_task_name());
		imageActionObject.setCurrent_task_status(mwImageAction
				.getCurrent_task_status());
		if (mwImageAction.getExecutionTime() != null) {
			imageActionObject.setDatetime(mwImageAction.getExecutionTime());
		}
		if (mwImageAction.getCreatedTime() != null) {
			imageActionObject
					.setCreatedDateTime(mwImageAction.getCreatedTime());
		}
		List<ImageActionTask> taskList = new ArrayList<>();
		if (!Constants.OBSOLETE.equals(mwImageAction.getCurrent_task_status())) {
			String actions = mwImageAction.getAction();
			if (actions != null) {
				actions = actions.replace("[", "").replace("]", "");
				String[] split = actions.split("},");
				ObjectMapper mapper = new ObjectMapper();
				for (String s : split) {
					if (!s.endsWith("}")) {
						s = s.concat("}");
					}
					ImageActionTask fromJson;
					try {
						fromJson = mapper.readValue(s, ImageActionTask.class);
						log.debug("TASK CREATED : " + fromJson.toString());
						taskList.add(fromJson);

					} catch (Exception e) {
						log.error("Error in action mapping ", e);
					}

				}
			}
		}
		log.debug("No of tasks : " + taskList.size());
		//
		imageActionObject.setActions(taskList);
		return imageActionObject;
	}

	public MwImageAction toDataUpdate(ImageActionObject imageActionObject) {
		MwImageAction mwImageAction = new MwImageAction();
		Gson gson = new Gson();
		mwImageAction.setId(imageActionObject.getId());
		mwImageAction.setImage_id(imageActionObject.getImage_id());
		mwImageAction.setAction(gson.toJson(imageActionObject.getActions()));
		mwImageAction.setAction_completed(imageActionObject
				.getAction_completed());
		mwImageAction.setAction_count(imageActionObject.getAction_count());
		mwImageAction.setAction_size(imageActionObject.getAction_size());
		mwImageAction
				.setAction_size_max(imageActionObject.getAction_size_max());
		mwImageAction.setCurrent_task_name(imageActionObject
				.getCurrent_task_name());
		mwImageAction.setCurrent_task_status(imageActionObject
				.getCurrent_task_status());
		if (imageActionObject.getDatetime() != null) {
			mwImageAction.setExecutionTime(imageActionObject.getDatetime());
		}
		return mwImageAction;
	}

	public MwHost toData(SshSettingInfo sshSetting) {
		MwHost mwHost = new MwHost();
		mwHost.setIpAdsress(sshSetting.getIpAddress());
		mwHost.setName(sshSetting.getName());
		mwHost.setUsername(sshSetting.getUsername());
		mwHost.setSshPassword(toData(sshSetting.getPassword()));
		mwHost.setSshKey(toData(sshSetting.getSshKeyId()));
		mwHost.setImageId(toData(sshSetting.getImage()));
		mwHost.getImageId().setName(sshSetting.getName());
		if (sshSetting.getCreated_date() != null) {
			mwHost.setCreatedByUserId(sshSetting.getCreated_by_user_id());
		}
		if (sshSetting.getEdited_date() != null) {
			mwHost.setEditedByUserId(sshSetting.getEdited_by_user_id());
		}

		mwHost.setCreatedDate(Calendar.getInstance());
		mwHost.setEditedDate(Calendar.getInstance());
		return mwHost;
	}

	public SshSettingInfo toTransferObject(MwHost mwHost) {
		SshSettingInfo sshSetting = new SshSettingInfo();
		sshSetting.setId(mwHost.getId());
		sshSetting.setIpAddress(mwHost.getIpAdsress());
		sshSetting.setName(mwHost.getName());
		sshSetting.setUsername(mwHost.getUsername());
		sshSetting.setPassword(toTransferObject(mwHost.getSshPassword()));
		sshSetting.setSshKeyId(toTransferObject(mwHost.getSshKey()));
		sshSetting.setCreated_by_user_id(mwHost.getCreatedByUserId());
		sshSetting.setEdited_by_user_id(mwHost.getEditedByUserId());
		sshSetting.setCreated_date(mwHost.getCreatedDate());
		sshSetting.setEdited_date(mwHost.getEditedDate());
		sshSetting.setImage(toTransferObject(mwHost.getImageId()));
		return sshSetting;
	}

	public MwHost toDataUpdate(SshSettingInfo sshSetting) {
		MwHost mwHost = new MwHost();
		// ssh = toMw(sshSetting.getPassword());
		mwHost.setId(sshSetting.getId());
		mwHost.setIpAdsress(sshSetting.getIpAddress());
		mwHost.setName(sshSetting.getName());
		mwHost.setUsername(sshSetting.getUsername());
		mwHost.setSshPassword(toData(sshSetting.getPassword()));
		mwHost.setSshKey(toData(sshSetting.getSshKeyId()));
		if (sshSetting.getCreated_date() != null) {
			mwHost.setCreatedByUserId(sshSetting.getCreated_by_user_id());
		}
		if (sshSetting.getEdited_date() != null) {
			mwHost.setEditedByUserId(sshSetting.getEdited_by_user_id());
		}
		mwHost.setImageId(toData(sshSetting.getImage()));
		mwHost.getImageId().setName(sshSetting.getName());
		mwHost.setCreatedDate(Calendar.getInstance());
		mwHost.setEditedDate(Calendar.getInstance());
		return mwHost;
	}

	public MwSshPassword toData(SshPassword sshPassword) {
		if (sshPassword == null) {
			return null;
		}
		MwSshPassword mwSshPassword = new MwSshPassword();
		mwSshPassword.setId(sshPassword.getId());
		if (sshPassword == null || sshPassword.getKey() == null) {
			mwSshPassword.setSshKey(null);
		} else if (sshPassword.getKey() != null) {
			mwSshPassword.setSshKey(toCharacterArray(sshPassword.getKey()));
		}

		return mwSshPassword;
	}

	public SshPassword toTransferObject(MwSshPassword mwSshPassword) {
		if (mwSshPassword == null) {
			return null;
		}
		SshPassword sshPassword = new SshPassword();
		sshPassword.setId(mwSshPassword.getId());
		if (mwSshPassword.getSshKey() != null) {
			sshPassword.setKey(fromCharacterArray(mwSshPassword.getSshKey()));
		} else {
			sshPassword.setKey(null);
		}

		return sshPassword;

	}

	public char[] toCharacterArrayKey(String str) {
		char[] chars = str.toCharArray();

		char[] characters = new char[chars.length];
		for (int i = 0; i < chars.length; i++) {
			characters[i] = chars[i];

		}
		return characters;
	}

	public String fromCharacterArrayKey(char[] chars) {
		StringBuilder sb = new StringBuilder(chars.length);
		for (char c : chars)
			sb.append(c);
		String str = sb.toString();
		return str;
	}

	public MwSshKey toData(SshKey sshKey) {
		MwSshKey mwSshKey = new MwSshKey();
		mwSshKey.setId(sshKey.getId());
		if (sshKey.getSshKey() != null) {
			mwSshKey.setSshKey(toCharacterArrayKey(sshKey.getSshKey()));
		} else {
			mwSshKey.setSshKey(null);
		}

		return mwSshKey;
	}

	public SshKey toTransferObject(MwSshKey mwSshKey) {
		if (mwSshKey == null) {
			return null;
		}
		SshKey sshKey = new SshKey();
		sshKey.setId(mwSshKey.getId());
		if (mwSshKey.getSshKey() != null) {
			sshKey.setSshKey(fromCharacterArrayKey(mwSshKey.getSshKey()));
		} else {
			sshKey.setSshKey(null);
		}

		return sshKey;

	}

	public MwPolicyTemplate toData(PolicyTemplateInfo policytemplate) {
		MwPolicyTemplate mwpolicytemplate = new MwPolicyTemplate();
		if (policytemplate.getId() != null) {
			mwpolicytemplate.setId(policytemplate.getId());
		}
		mwpolicytemplate.setContent(toCharacterArray(policytemplate
				.getContent()));

		mwpolicytemplate
				.setDeployment_type(policytemplate.getDeployment_type());
		mwpolicytemplate.setActive(policytemplate.isActive());
		mwpolicytemplate.setName(policytemplate.getName());
		mwpolicytemplate.setPolicy_type(policytemplate.getPolicy_type());
		mwpolicytemplate.setDeployment_type_identifier(policytemplate
				.getDeployment_type_identifier());
		return mwpolicytemplate;
	}

	public PolicyTemplateInfo toTransferObject(MwPolicyTemplate mwpolicytemplate) {
		PolicyTemplateInfo policytemplate = new PolicyTemplateInfo();
		policytemplate.setId(mwpolicytemplate.getId());
		policytemplate.setContent(fromCharacterArray(mwpolicytemplate
				.getContent()));
		policytemplate
				.setDeployment_type(mwpolicytemplate.getDeployment_type());
		policytemplate.setActive(mwpolicytemplate.isActive());
		policytemplate.setName(mwpolicytemplate.getName());
		policytemplate.setPolicy_type(mwpolicytemplate.getPolicy_type());
		policytemplate.setDeployment_type_identifier(mwpolicytemplate
				.getDeployment_type_identifier());
		return policytemplate;
	}

	public MwImageStore toData(MwImageStore existingImageStore, ImageStoreTransferObject imageStoreTO) {
		if (imageStoreTO == null) {
			return null;
		}
		MwImageStore mwImageStore = null;
		if(existingImageStore != null){
			mwImageStore = existingImageStore; 
		}else{
			mwImageStore = new MwImageStore();
			if (imageStoreTO.getId() != null) {
				mwImageStore.setId(imageStoreTO.getId());
			}
		}
		String[] artifact_types = imageStoreTO.getArtifact_types();
		Arrays.sort(artifact_types);
		if (artifact_types.length != 0) {
			StringBuffer bufferForArtifactString = new StringBuffer("");
			for (String artifact : artifact_types) {
				bufferForArtifactString = bufferForArtifactString.append(artifact).append(",");
			}
			String artifactString = bufferForArtifactString.toString().substring(0,
					bufferForArtifactString.length() - 1);
			mwImageStore.setArtifact_type(artifactString);
		}
		mwImageStore.setConnector(imageStoreTO.getConnector());
		mwImageStore.setName(imageStoreTO.getName());
		mwImageStore.setDeleted(imageStoreTO.isDeleted());

		Collection<MwImageStoreDetails> mwImageStoreDetailsCollection = new ArrayList<MwImageStoreDetails>();;
		Collection<MwImageStoreDetails> existingMwImageStoreDetailsCollection = mwImageStore.getMwImageStoreDetailsCollection();


		if (imageStoreTO.getImage_store_details() != null
				&& imageStoreTO.getImage_store_details().size() != 0) {
			Collection<ImageStoreDetailsTransferObject> image_store_details = imageStoreTO
					.getImage_store_details();
			for (ImageStoreDetailsTransferObject imageStoreDetailsTransferObject : image_store_details) {
				MwImageStoreDetails existingMwImageStoreDetails = null;
				if(existingMwImageStoreDetailsCollection != null){
					for (MwImageStoreDetails mwImageStoreDetails : existingMwImageStoreDetailsCollection) {
						if(mwImageStoreDetails.getKey().equals(imageStoreDetailsTransferObject.getKey())){
							existingMwImageStoreDetails = mwImageStoreDetails;
							break;
						}
					}
				}
				MwImageStoreDetails data = toData(
						imageStoreDetailsTransferObject, existingMwImageStoreDetails, mwImageStore);
				mwImageStoreDetailsCollection.add(data);
			}
		}
		mwImageStore
				.setMwImageStoreDetailsCollection(mwImageStoreDetailsCollection);
		return mwImageStore;
	}

	private MwImageStoreDetails toData(
			ImageStoreDetailsTransferObject imageStoreDetailsTO, MwImageStoreDetails existingMwImageStoreDetails,
			MwImageStore mwImageStore) {
		if (imageStoreDetailsTO == null) {
			return null;
		}
		MwImageStoreDetails mwImageStoreDetails;
		if(existingMwImageStoreDetails == null){
			mwImageStoreDetails = new MwImageStoreDetails();

			if (imageStoreDetailsTO.getId() != null) {
				mwImageStoreDetails.setId(imageStoreDetailsTO.getId());
			}
		}else{
			mwImageStoreDetails = existingMwImageStoreDetails;
		}
		
		mwImageStoreDetails.setKey(imageStoreDetailsTO.getKey());
		mwImageStoreDetails.setValue(imageStoreDetailsTO.getValue());
		mwImageStoreDetails.setImage_store(mwImageStore);
		return mwImageStoreDetails;
	}

	public ImageStoreTransferObject toTransferObject(MwImageStore mwImageStore) {
		if (mwImageStore == null) {
			return null;
		}
		Collection<ImageStoreDetailsTransferObject> imageStoreDetailsTransferObjects = new ArrayList<ImageStoreDetailsTransferObject>();
		ImageStoreTransferObject imageStoreTO = new ImageStoreTransferObject();
		imageStoreTO.setId(mwImageStore.getId());
		imageStoreTO.setArtifact_types(mwImageStore.getArtifact_type().split(
				","));
		imageStoreTO.setConnector(mwImageStore.getConnector());
		imageStoreTO.setName(mwImageStore.getName());
		imageStoreTO.setDeleted(mwImageStore.isDeleted());
		Collection<MwImageStoreDetails> mwImageStoreDetailsCollection = mwImageStore
				.getMwImageStoreDetailsCollection();
		if (mwImageStoreDetailsCollection != null) {
			for (MwImageStoreDetails mwISD : mwImageStoreDetailsCollection) {
				imageStoreDetailsTransferObjects.add(toTransferObject(mwISD,
						imageStoreTO));
			}
		}
		imageStoreTO.setImage_store_details(imageStoreDetailsTransferObjects);
		return imageStoreTO;
	}

	public ImageStoreDetailsTransferObject toTransferObject(
			MwImageStoreDetails mwImageStoreDetails,
			ImageStoreTransferObject imageStoreTransferObject) {
		if (mwImageStoreDetails == null) {
			return null;
		}
		ImageStoreDetailsTransferObject imageStoreDetailsTO = new ImageStoreDetailsTransferObject();
		if (mwImageStoreDetails.getId() != null) {
			imageStoreDetailsTO.setId(mwImageStoreDetails.getId());
		}
		imageStoreDetailsTO.setKey(mwImageStoreDetails.getKey());
		imageStoreDetailsTO.setValue(mwImageStoreDetails.getValue());
		imageStoreDetailsTO.setImage_store_id(imageStoreTransferObject.id);
		return imageStoreDetailsTO;
	}

}
