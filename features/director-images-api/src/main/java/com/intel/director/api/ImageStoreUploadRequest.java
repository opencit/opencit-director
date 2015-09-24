/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;

/**
 *
 * @author soakx
 */
public class ImageStoreUploadRequest {
	


	  
    public String image_id;
    public String store_name_for_image_upload ;
    public String store_name_for_policy_upload ;
    public String store_name_for_tarball_upload ;
    
	public String getImage_id() {
		return image_id;
	}
	public void setImage_id(String image_id) {
		this.image_id = image_id;
	}
	public String getStore_name_for_image_upload() {
		return store_name_for_image_upload;
	}
	public void setStore_name_for_image_upload(String store_name_for_image_upload) {
		this.store_name_for_image_upload = store_name_for_image_upload;
	}
	public String getStore_name_for_policy_upload() {
		return store_name_for_policy_upload;
	}
	public void setStore_name_for_policy_upload(String store_name_for_policy_upload) {
		this.store_name_for_policy_upload = store_name_for_policy_upload;
	}
	public String getStore_name_for_tarball_upload() {
		return store_name_for_tarball_upload;
	}
	public void setStore_name_for_tarball_upload(
			String store_name_for_tarball_upload) {
		this.store_name_for_tarball_upload = store_name_for_tarball_upload;
	}
    
    
    
    

	
}
