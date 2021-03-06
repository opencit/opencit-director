package com.intel.director.api;

import java.util.Calendar;

import com.intel.director.api.ui.ImageInfo;

public class ImageListResponseInfo extends ImageInfo{

	public String image_name;
	public String image_format;
	public String trust_policy;
	public String image_upload;
	public String image_delete;

	public Calendar created_date;
	
	public String display_name;
	public String getDisplay_name() {
		return display_name;
	}
	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}
	
	
	public String getImage_name() {
		return image_name;
	}
	public void setImage_name(String image_name) {
		this.image_name = image_name;
	}
	public String getImage_format() {
		return image_format;
	}
	public void setImage_format(String image_format) {
		this.image_format = image_format;
	}
	public String getTrust_policy() {
		return trust_policy;
	}
	public void setTrust_policy(String trust_policy) {
		this.trust_policy = trust_policy;
	}
	public String getImage_upload() {
		return image_upload;
	}
	public void setImage_upload(String image_upload) {
		this.image_upload = image_upload;
	}
	public Calendar getCreated_date() {
		return created_date;
	}
	public void setCreated_date(Calendar created_date) {
		this.created_date = created_date;
	}
	public String getImage_delete() {
		return image_delete;
	}
	public void setImage_delete(String image_delete) {
		this.image_delete = image_delete;
	}
}
