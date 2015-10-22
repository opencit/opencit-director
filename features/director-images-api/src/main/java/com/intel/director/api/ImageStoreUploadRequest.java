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
	


	public String image_action_id;
    public String image_id;
    public boolean check_image_action_id;
    public String store_name_for_image_upload ;
    public String store_name_for_policy_upload ;
    public String store_name_for_tarball_upload ;
    public String display_name;
    
	public String getImage_id() {
		return image_id;
	}
	public String getDisplay_name() {
		return display_name;
	}
	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
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
	public String getImage_action_id() {
		return image_action_id;
	}
	public void setImage_action_id(String image_action_id) {
		this.image_action_id = image_action_id;
	}
	public boolean isCheck_image_action_id() {
		return check_image_action_id;
	}
	public void setCheck_image_action_id(boolean check_image_action_id) {
		this.check_image_action_id = check_image_action_id;
	}
}
