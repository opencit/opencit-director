package com.intel.director.api;

import java.util.Date;

import com.intel.director.api.ui.ImageInfo;

public class ImageListResponseInfo extends ImageInfo{

	public String image_name;
	public String image_format;
	public String trust_policy;
	public String image_upload;
	public Date created_date;
	
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
	public Date getCreated_date() {
		return created_date;
	}
	public void setCreated_date(Date created_date) {
		this.created_date = created_date;
	}

}
