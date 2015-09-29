package com.intel.mtwilson.director.mapper;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.intel.director.api.ImageAttributeFields;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ImageInfoFields;
import com.intel.director.api.ImageStoreSettings;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ui.ImageActionObject;
import com.intel.director.api.ui.TrustPolicyDraftFields;
import com.intel.director.api.ui.TrustPolicyFields;
import com.intel.director.api.User;
import com.intel.director.api.ui.UserFields;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.data.MwImageAction;
import com.intel.mtwilson.director.data.MwImageStoreSettings;
import com.intel.mtwilson.director.data.MwImageUpload;
import com.intel.mtwilson.director.data.MwTrustPolicy;
import com.intel.mtwilson.director.data.MwTrustPolicyDraft;
import com.intel.mtwilson.director.data.MwUser;

public class Mapper {

	Map<ImageAttributeFields, String> imageAttributestoDataMapper;
	
	public Map<ImageAttributeFields, String>  getImageAttributesToDataMapper(){
		
		if(imageAttributestoDataMapper==null){
			 imageAttributestoDataMapper= new EnumMap<ImageAttributeFields,String>(ImageAttributeFields.class);
			 imageAttributestoDataMapper.put(ImageAttributeFields.ID, "id");
			 imageAttributestoDataMapper.put(ImageAttributeFields.NAME, "name");
			 imageAttributestoDataMapper.put(ImageAttributeFields.IMAGE_DEPLOYMENTS, "imageDeploymentType");
			 imageAttributestoDataMapper.put(ImageAttributeFields.IMAGE_FORMAT, "imageFormat");
			 imageAttributestoDataMapper.put(ImageAttributeFields.SENT, "sent");
			 imageAttributestoDataMapper.put(ImageAttributeFields.STATUS, "status");
			 imageAttributestoDataMapper.put(ImageAttributeFields.IMAGE_SIZE, "contentlength");
			 imageAttributestoDataMapper.put(ImageAttributeFields.LOCATION, "location");
			 imageAttributestoDataMapper.put(ImageAttributeFields.MOUNTED_BY_USER_ID, "mountedByUserId");
			 imageAttributestoDataMapper.put(ImageAttributeFields.CREATED_BY_USER_ID, "createdByUserId");
			 imageAttributestoDataMapper.put(ImageAttributeFields.CREATED_DATE, "createdDate");
			 imageAttributestoDataMapper.put(ImageAttributeFields.EDITED_BY_USER_ID, "editedByUserId");
			 imageAttributestoDataMapper.put(ImageAttributeFields.EDITED_DATE, "editedDate");
		}
		return imageAttributestoDataMapper;
	}
	
	Map<ImageInfoFields, String> imageInfotoDataMapper;
	
public Map<ImageInfoFields, String>  getImageInfoToDataColumnsMapper(){

		if(imageAttributestoDataMapper==null){
			imageInfotoDataMapper= new EnumMap<ImageInfoFields,String>(ImageInfoFields.class);
			 imageInfotoDataMapper.put(ImageInfoFields.ID, "id");
			 imageInfotoDataMapper.put(ImageInfoFields.NAME, "name");
			 imageInfotoDataMapper.put(ImageInfoFields.IMAGE_DEPLOYMENTS, "image_deployments");
			 imageInfotoDataMapper.put(ImageInfoFields.IMAGE_FORMAT, "image_format");
			 imageInfotoDataMapper.put(ImageInfoFields.SENT, "sent");
			 imageInfotoDataMapper.put(ImageInfoFields.STATUS, "status");
			 imageInfotoDataMapper.put(ImageInfoFields.IMAGE_SIZE, "content_length");
			 imageInfotoDataMapper.put(ImageInfoFields.LOCATION, "location");
			 imageInfotoDataMapper.put(ImageInfoFields.MOUNTED_BY_USER_ID, "mountedByUserId");
			 imageInfotoDataMapper.put(ImageInfoFields.CREATED_BY_USER_ID, "created_by_user_id");
			 imageInfotoDataMapper.put(ImageInfoFields.CREATED_DATE, "created_date");
			 imageInfotoDataMapper.put(ImageInfoFields.EDITED_BY_USER_ID, "edited_by_user_id");
			 imageInfotoDataMapper.put(ImageInfoFields.EDITED_DATE, "edited_date");
			 imageInfotoDataMapper.put(ImageInfoFields.TRUST_POLICY_ID, "trust_policy_id");
			 imageInfotoDataMapper.put(ImageInfoFields.TRUST_POLICY_NAME, "trust_policy_name");
			 imageInfotoDataMapper.put(ImageInfoFields.TRUST_POLICY_DRAFT_ID, "trust_policy_draft_id");
			 imageInfotoDataMapper.put(ImageInfoFields.TRUST_POLICY_DRAFT_NAME, "trust_policy_name");
			 
		}
		return imageInfotoDataMapper;
	}
	
	
	
Map<UserFields, String> userAttributestoDataMapper;
	
	public Map<UserFields, String>  getUserAttributestoDataMapper(){
		
		if(userAttributestoDataMapper==null){
			userAttributestoDataMapper= new EnumMap<UserFields,String>(UserFields.class);
			userAttributestoDataMapper.put(UserFields.ID, "id");
			userAttributestoDataMapper.put(UserFields.USERNAME, "username");
			userAttributestoDataMapper.put(UserFields.DISPLAYNAME, "displayname");
			userAttributestoDataMapper.put(UserFields.EMAIL, "email");
			
		}
		return userAttributestoDataMapper;
	}
	

	Map<TrustPolicyFields, String> policyAttributestoDataMapper;
	
	public Map<TrustPolicyFields, String>  getPolicyAttributestoDataMapper(){
		
		if(policyAttributestoDataMapper==null){
			policyAttributestoDataMapper= new EnumMap<TrustPolicyFields,String>(TrustPolicyFields.class);
			policyAttributestoDataMapper.put(TrustPolicyFields.ID, "id");
			policyAttributestoDataMapper.put(TrustPolicyFields.DESCRIPTION, "description");
			policyAttributestoDataMapper.put(TrustPolicyFields.NAME, "name");
			policyAttributestoDataMapper.put(TrustPolicyFields.TRUST_POLICY, "trustPolicy");
			policyAttributestoDataMapper.put(TrustPolicyFields.CREATED_BY_USER_ID, "createdByUserId");
			policyAttributestoDataMapper.put(TrustPolicyFields.CREATED_DATE, "createdDate");
			policyAttributestoDataMapper.put(TrustPolicyFields.EDITED_BY_USER_ID, "editedByUserId");
			policyAttributestoDataMapper.put(TrustPolicyFields.EDITED_DATE, "editedDate");
			
			policyAttributestoDataMapper.put(TrustPolicyFields.IMAGE_ID,"id");
			policyAttributestoDataMapper.put(TrustPolicyFields.IMAGE_NAME, "name");
			policyAttributestoDataMapper.put(TrustPolicyFields.IMAGE_CREATION_DATE, "createdDate");
			policyAttributestoDataMapper.put(TrustPolicyFields.IMAGE_FORMAT, "imageFormat");
			policyAttributestoDataMapper.put(TrustPolicyFields.IMAGE_DEPLOYMENT, "imageDeploymentType");
		}
		return policyAttributestoDataMapper;
	}
	
	Map<TrustPolicyDraftFields, String> policyDraftAttributestoDataMapper;
	
	public Map<TrustPolicyDraftFields, String>  getPolicyDraftAttributestoDataMapper(){
		
		if(policyDraftAttributestoDataMapper==null){
			policyDraftAttributestoDataMapper= new EnumMap<TrustPolicyDraftFields,String>(TrustPolicyDraftFields.class);
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.ID, "id");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.TRUST_POLICY_DRAFT, "trustPolicyDraft");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.NAME, "name");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.CREATED_BY_USER_ID, "createdByUserId");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.CREATED_DATE, "createdDate");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.EDITED_BY_USER_ID, "editedByUserId");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.EDITED_DATE, "editedDate");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.IMAGE_ID,"id");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.IMAGE_NAME, "name");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.IMAGE_CREATION_DATE, "createdDate");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.IMAGE_FORMAT, "imageFormat");
			policyDraftAttributestoDataMapper.put(TrustPolicyDraftFields.IMAGE_DEPLOYMENT, "imageDeploymentType");
		}
		return policyDraftAttributestoDataMapper;
	}
	
	
	Map<ImageStoreUploadFields, String> imageUploadtAttributestoDataMapper;
	
	public Map<ImageStoreUploadFields, String>  getImageUploadtAttributestoDataMapper(){
		
		if(imageUploadtAttributestoDataMapper==null){
			imageUploadtAttributestoDataMapper= new EnumMap<ImageStoreUploadFields,String>(ImageStoreUploadFields.class);
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.ID, "id");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.DATE, "date");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.IMAGE_URI, "imageUri");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.CHECKSUM, "checksum");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.TMP_LOCATION, "tmpLocation");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.STATUS, "status");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.IMAGE_SIZE, "contentlength");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.SENT, "sent");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.IMAGE_ID, "id");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.IMAGE_NAME, "name");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.IMAGE_CREATION_DATE, "createdDate");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.IMAGE_FORMAT, "imageFormat");
			imageUploadtAttributestoDataMapper.put(ImageStoreUploadFields.IMAGE_DEPOLYMENT, "imageDeploymentType");
			
			
		}
		return imageUploadtAttributestoDataMapper;
	}
	
	
	 public  MwImage toData(ImageAttributes imgAttributes){
		 MwImage mwImage = new MwImage();
		 mwImage.setImageDeploymentType(imgAttributes.getImage_deployments());
		 mwImage.setContentlength(imgAttributes.getImage_size());
		 mwImage.setImageFormat(imgAttributes.getImage_format());
		 mwImage.setLocation(imgAttributes.getLocation());
		 mwImage.setMountedByUserId(imgAttributes.getMounted_by_user_id());
		 mwImage.setName(imgAttributes.getName());
		 mwImage.setId(imgAttributes.getId());
		 mwImage.setStatus(imgAttributes.getStatus());
		 mwImage.setSent(imgAttributes.getSent());
		 mwImage.setCreatedByUserId(imgAttributes.getCreated_by_user_id());
		 mwImage.setEditedByUserId(imgAttributes.getEdited_by_user_id());
		 if(imgAttributes.getCreated_date()!=null){
			 mwImage.setCreatedDate(new java.sql.Date(imgAttributes.getCreated_date().getTime()));
		 }
		 if(imgAttributes.getEdited_date()!=null){
			 mwImage.setEditedDate(new java.sql.Date(imgAttributes.getEdited_date().getTime()));
		 }
 
		 return mwImage;
	 }
	 
	 

	 
	 
	 public  ImageInfo toTransferObject(MwImage mwImage){
		 ImageInfo imgInfo= new ImageInfo();
		 imgInfo.setId(mwImage.getId());
		 imgInfo.setImage_deployments(mwImage.getImageDeploymentType());
		 imgInfo.setImage_format(mwImage.getImageFormat());
		 imgInfo.setName(mwImage.getName());;
		 imgInfo.setMounted_by_user_id(mwImage.getMountedByUserId());
		 imgInfo.setLocation(mwImage.getLocation());
		 imgInfo.setDeleted(mwImage.isDeleted());
		 imgInfo.setCreated_by_user_id(mwImage.getCreatedByUserId());
		 imgInfo.setEdited_by_user_id(mwImage.getEditedByUserId());
		 imgInfo.setCreated_date(mwImage.getCreatedDate());
		 imgInfo.setEdited_date(mwImage.getEditedDate());
		 imgInfo.setImage_size(mwImage.getContentlength());
		 imgInfo.setStatus(mwImage.getStatus());
		 imgInfo.setSent(mwImage.getSent());
		 return imgInfo;
	 }
	 
	 
	 public  MwUser toData(User user){
		 MwUser mwUser = new MwUser();
		 
		 mwUser.setId(user.getId());
		 mwUser.setUsername(user.getUsername());
		 mwUser.setDisplayname(user.getDisplayname());
		 mwUser.setEmail(user.getEmail());
 
		 return mwUser;
	 }
	 
	 
	 public  User toTransferObject(MwUser mwUser){
	
		
		 User user= new User();
		 user.setId(mwUser.getId());
		 user.setDisplayname(mwUser.getDisplayname());
		 user.setUsername(mwUser.getUsername());
		 user.setEmail(mwUser.getEmail());

		 return user;
	 }
	 
	 

	 public  MwTrustPolicy toData(TrustPolicy trustPolicy){
		 MwTrustPolicy mwTrustPolicy= new MwTrustPolicy();
		 mwTrustPolicy.setId(trustPolicy.getId());
	
		 mwTrustPolicy.setName(mwTrustPolicy.getName());
		 MwImage mwImage= toData(trustPolicy.getImgAttributes());
		 mwTrustPolicy.setTrustPolicy(toCharacterArray((trustPolicy.getTrust_policy())));
		 mwTrustPolicy.setImage(mwImage);
		 mwTrustPolicy.setDescription(trustPolicy.getDescription());
		 mwTrustPolicy.setCreatedByUserId(trustPolicy.getCreated_by_user_id());
		 mwTrustPolicy.setEditedByUserId(trustPolicy.getEdited_by_user_id());
		 if(trustPolicy.getCreated_date()!=null){
			 mwTrustPolicy.setCreatedDate(new java.sql.Date(trustPolicy.getCreated_date().getTime()));
		 }
		 if(trustPolicy.getEdited_date()!=null){
			 mwTrustPolicy.setEditedDate(new java.sql.Date(trustPolicy.getEdited_date().getTime()));
		 }
 
		 return mwTrustPolicy;
	 }
	 
	 
	 public  TrustPolicy toTransferObject(MwTrustPolicy mwTrustPolicy){
		 TrustPolicy trustPolicy= new TrustPolicy();
		 trustPolicy.setId(mwTrustPolicy.getId());
		 trustPolicy.setDescription(mwTrustPolicy.getDescription());
		 trustPolicy.setTrust_policy(fromCharacterArray(mwTrustPolicy.getTrustPolicy()));
		 trustPolicy.setName(mwTrustPolicy.getName());
		 ImageAttributes imgAttributes = toTransferObject(mwTrustPolicy.getImage());
		 trustPolicy.setImgAttributes(imgAttributes);
		 trustPolicy.setCreated_by_user_id(mwTrustPolicy.getCreatedByUserId());
		 trustPolicy.setEdited_by_user_id(mwTrustPolicy.getEditedByUserId());
		 trustPolicy.setCreated_date(mwTrustPolicy.getCreatedDate());
		 trustPolicy.setEdited_date(mwTrustPolicy.getEditedDate());

		 return trustPolicy;
	 }
	 
	 
	 public  MwTrustPolicyDraft toData(TrustPolicyDraft trustPolicyDraft){
		 MwTrustPolicyDraft mwTrustPolicyDraft= new MwTrustPolicyDraft();
		 mwTrustPolicyDraft.setId(trustPolicyDraft.getId());
	
		 mwTrustPolicyDraft.setName(trustPolicyDraft.getName());
		 MwImage mwImage= toData(trustPolicyDraft.getImgAttributes());
		 mwTrustPolicyDraft.setTrustPolicyDraft(toCharacterArray(trustPolicyDraft.getTrust_policy_draft()));
		 mwTrustPolicyDraft.setImage(mwImage);

		 mwTrustPolicyDraft.setCreatedByUserId(trustPolicyDraft.getCreated_by_user_id());
		 mwTrustPolicyDraft.setEditedByUserId(trustPolicyDraft.getEdited_by_user_id());
		 if(trustPolicyDraft.getCreated_date()!=null){
			 mwTrustPolicyDraft.setCreatedDate(new java.sql.Date(trustPolicyDraft.getCreated_date().getTime()));
		 }
		 if(trustPolicyDraft.getEdited_date()!=null){
			 mwTrustPolicyDraft.setEditedDate(new java.sql.Date(trustPolicyDraft.getEdited_date().getTime()));
		 }
 
		 return mwTrustPolicyDraft;
	 }
	 
	 
	 public  TrustPolicyDraft toTransferObject(MwTrustPolicyDraft mwTrustPolicyDraft){
		 TrustPolicyDraft trustPolicyDraft= new TrustPolicyDraft();
		 trustPolicyDraft.setId(mwTrustPolicyDraft.getId());
		 trustPolicyDraft.setTrust_policy_draft(fromCharacterArray(mwTrustPolicyDraft.getTrustPolicyDraft()));
		 trustPolicyDraft.setName(mwTrustPolicyDraft.getName());
		 ImageAttributes imgAttributes = toTransferObject(mwTrustPolicyDraft.getImage());
		 trustPolicyDraft.setImgAttributes(imgAttributes);
		 trustPolicyDraft.setCreated_by_user_id(mwTrustPolicyDraft.getCreatedByUserId());
		 trustPolicyDraft.setEdited_by_user_id(mwTrustPolicyDraft.getEditedByUserId());
		 trustPolicyDraft.setCreated_date(mwTrustPolicyDraft.getCreatedDate());
		 trustPolicyDraft.setEdited_date(mwTrustPolicyDraft.getEditedDate());

		 return trustPolicyDraft;
	 }
	 
	 
	 
	 
	 public  MwImageUpload toData(ImageStoreUploadTransferObject imageStoreUploadTO){
		 MwImageUpload mwImageUpload= new MwImageUpload();
		 mwImageUpload.setId(imageStoreUploadTO.getId());
		 mwImageUpload.setChecksum(imageStoreUploadTO.getChecksum());
		 mwImageUpload.setContentlength(imageStoreUploadTO.getImage_size());
		 MwImage mwImage= toData(imageStoreUploadTO.getImg());
		 mwImageUpload.setImage(mwImage);
	
		 mwImageUpload.setSent(imageStoreUploadTO.getSent());
		 mwImageUpload.setImageUri(toCharacterArray(imageStoreUploadTO.getImage_uri()));
		 mwImageUpload.setTmpLocation(imageStoreUploadTO.getTmp_location());
		 mwImageUpload.setStatus(imageStoreUploadTO.getStatus());
		 if(imageStoreUploadTO.getDate()!=null){
			 mwImageUpload.setDate(new java.sql.Date(imageStoreUploadTO.getDate().getTime()));
		 }
		 
		 return mwImageUpload;
	 }
	 
	 
	 public  ImageStoreUploadTransferObject toTransferObject(MwImageUpload mwImageUpload){
		 ImageStoreUploadTransferObject imageStoreUploadTO= new ImageStoreUploadTransferObject();
		 imageStoreUploadTO.setId(mwImageUpload.getId());
		 imageStoreUploadTO.setChecksum(mwImageUpload.getChecksum());
		 imageStoreUploadTO.setDate(mwImageUpload.getDate());
		 imageStoreUploadTO.setImage_size(mwImageUpload.getContentlength());
		 imageStoreUploadTO.setSent(mwImageUpload.getSent());
		 imageStoreUploadTO.setStatus(mwImageUpload.getStatus());
	
		 imageStoreUploadTO.setImage_uri(fromCharacterArray(mwImageUpload.getImageUri()));
		 ImageAttributes imgAttributes = toTransferObject(mwImageUpload.getImage());
		 imageStoreUploadTO.setImg(imgAttributes);
		 return imageStoreUploadTO;
	 }
	 
	 
	 
	 public MwImageStoreSettings toData(ImageStoreSettings imgSettings ){
		 MwImageStoreSettings mwSettings = new MwImageStoreSettings();
		 mwSettings.setId(imgSettings.getId());
		 mwSettings.setName(imgSettings.getName());
		 mwSettings.setProvider_class(imgSettings.getProvider_class());
		 return mwSettings;
	 }
	 
	 public ImageStoreSettings toTransferObject(MwImageStoreSettings mwSettings){
		 ImageStoreSettings imgStoreSettings= new ImageStoreSettings();
		 imgStoreSettings.setId(mwSettings.getId());
		 imgStoreSettings.setName(mwSettings.getName());
		 imgStoreSettings.setProvider_class(mwSettings.getProvider_class());
		 return imgStoreSettings;
		 
	 }
	public Character[] toCharacterArray(String str){
		 char[] chars=str.toCharArray();

		    Character[] characters=new Character[chars.length];
		    for (int i = 0; i < chars.length; i++) {
		        characters[i]=chars[i];
		       
		    }
		    return characters;
	}
	
	public String fromCharacterArray(Character[] chars){
		StringBuilder sb = new StringBuilder(chars.length);
		for (Character c : chars)
		    sb.append(c.charValue());

		String str = sb.toString();
		return str;
	}
	
	public  MwImageAction toData(ImageActionObject imageaction){
		   MwImageAction mwImageAction= new MwImageAction();
		   Gson gson=new Gson();
		   mwImageAction.setImage_id(imageaction.getImage_id());
		   mwImageAction.setAction_size_max(imageaction.getAction_size_max());
		   mwImageAction.setAction_size(imageaction.getAction_size());
		   mwImageAction.setAction_count(imageaction.getAction_count());
		   mwImageAction.setAction_completed(imageaction.getAction_completed());
		   mwImageAction.setAction(gson.toJson(imageaction.getAction()));
		   mwImageAction.setCurrent_task_name(imageaction.getCurrent_task_name());
		   mwImageAction.setCurrent_task_status(imageaction.getCurrent_task_status());
		   return mwImageAction;
		  }
		  
		 
		 
		 public  ImageActionObject toTransferObject(MwImageAction mwImageAction){
		   Gson gson = new Gson();
		   ImageActionObject imageActionObject= new ImageActionObject();
		   imageActionObject.setId(mwImageAction.getId());
		   imageActionObject.setImage_id(mwImageAction.getImage_id());
		   imageActionObject.setAction_count(mwImageAction.getAction_count());
		   imageActionObject.setAction_completed(mwImageAction.getAction_completed());
		   imageActionObject.setAction_size(mwImageAction.getAction_size());
		   imageActionObject.setAction_size_max(mwImageAction.getAction_size_max());
		   imageActionObject.setCurrent_task_name(mwImageAction.getCurrent_task_name());
		   imageActionObject.setCurrent_task_status(mwImageAction.getCurrent_task_status());
		   JSONArray actionlist = new JSONArray(mwImageAction.getAction());
		   ObjectMapper mapper = new  ObjectMapper();
		   List<ImageActionActions> someClassList = null;
		   try {
			   someClassList =
					    mapper.readValue(mwImageAction.getAction(), mapper.getTypeFactory().constructCollectionType(List.class, ImageActionActions.class));
		   } catch (IOException e) {
			// TODO Auto-generated catch block
			   e.printStackTrace();
		   }
		   return imageActionObject;
		}
		 
		 
		 public  MwImageAction toDataUpdate(ImageActionObject imageActionObject){
			 MwImageAction mwImageAction = new MwImageAction();
			 Gson gson = new Gson();
			 mwImageAction.setImage_id(imageActionObject.getImage_id());
			 mwImageAction.setAction(gson.toJson(imageActionObject.getAction()));
			 mwImageAction.setAction_completed(imageActionObject.getAction_completed());
			 mwImageAction.setAction_count(imageActionObject.getAction_count());
			 mwImageAction.setAction_size(imageActionObject.getAction_size());
			 mwImageAction.setAction_size_max(imageActionObject.getAction_size_max());
			 mwImageAction.setCurrent_task_name(imageActionObject.getCurrent_task_name());
			 mwImageAction.setCurrent_task_status(imageActionObject.getCurrent_task_status());
			 return mwImageAction;
		 }


	
}
