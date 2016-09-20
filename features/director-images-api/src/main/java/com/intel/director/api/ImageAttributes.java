/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author soakx
 */
public class ImageAttributes extends AuditFields {

	public String id;
	public String image_name;

  
    public String image_format;
    public String image_deployments;
    @JsonProperty("image_upload_status")
    public String status;
    public Long image_size;
    public Long sent;
    public String mounted_by_user_id;
    public boolean deleted;
    @JsonProperty("image_Location")
    public String location;
    
    public String repository;
    public String tag;
    public String uploadVariableMD5;
    public String tmpLocation;
    
	public String partition;
	
	
	
	public String getPartition() {
		return partition;
	}

	public void setPartition(String partition) {
		this.partition = partition;
	}

	
    
    
    public ImageAttributes() {
        super();
    }

	public String repository;
	public String tag;
	public String uploadVariableMD5;
	public String tmpLocation;

	public ImageAttributes() {
		super();
	}

	public ImageAttributes(String created_by_user_id, Calendar created_date, String edited_by_user_id,
			Calendar edited_date, String id, String image_name, String format, String image_deployments, String status,
			Long image_size, Long sent, String mounted_by_user_id, boolean deleted, String location) {
		super(created_by_user_id, created_date, edited_by_user_id, edited_date);
		this.id = id;
		this.image_name = image_name;
		this.image_format = format;
		this.image_deployments = image_deployments;
		this.status = status;
		this.image_size = image_size;
		this.sent = sent;
		this.mounted_by_user_id = mounted_by_user_id;
		this.deleted = deleted;
		this.location = location;

	}

	public ImageAttributes(String created_by_user_id, Calendar created_date, String edited_by_user_id,
			Calendar edited_date, String image_name, String format, String image_deployments, String status,
			Long image_size, Long sent, String mounted_by_user_id, boolean deleted, String location) {
		super(created_by_user_id, created_date, edited_by_user_id, edited_date);

		this.image_name = image_name;
		this.image_format = format;
		this.image_deployments = image_deployments;
		this.status = status;
		this.image_size = image_size;
		this.sent = sent;
		this.mounted_by_user_id = mounted_by_user_id;
		this.deleted = deleted;
		this.location = location;

	}

	@Override
	public String toString() {
		return "ImageAttributes [id=" + id + ", image_name=" + image_name + ", image_format=" + image_format
				+ ", image_deployments=" + image_deployments + ", status=" + status + ", image_size=" + image_size
				+ ", sent=" + sent + ", mounted_by_user_id=" + mounted_by_user_id + ", deleted=" + deleted
				+ ", location=" + location + ", repository=" + repository + ", tag=" + tag + ", uploadVariableMD5="
				+ uploadVariableMD5 + ", tmpLocation=" + tmpLocation + "]";
	}

	@JsonIgnore
	public String getUploadVariableMD5() {
		return uploadVariableMD5;
	}

	public void setUploadVariableMD5(String uploadVariableMD5) {
		this.uploadVariableMD5 = uploadVariableMD5;
	}

	@JsonIgnore
	public String getTmpLocation() {
		return tmpLocation;
	}

	public void setTmpLocation(String tmpLocation) {
		this.tmpLocation = tmpLocation;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getImage_deployments() {
		return image_deployments;
	}

	public void setImage_deployments(String image_deployments) {
		this.image_deployments = image_deployments;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Long getImage_size() {
		return image_size;
	}

	public void setImage_size(Long sizeInBytes) {
		this.image_size = sizeInBytes;
	}

	public Long getSent() {
		return sent;
	}

	public void setSent(Long sent) {
		this.sent = sent;
	}

	public String getMounted_by_user_id() {
		return mounted_by_user_id;
	}

	public void setMounted_by_user_id(String mounted_by_user_id) {
		this.mounted_by_user_id = mounted_by_user_id;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getRepository() {
		return repository == null ? null : repository.trim();
	}

	public void setRepository(String repository) {
		this.repository = repository == null ? null : repository.trim();
	}

	public String getTag() {
		return tag == null ? null : tag.trim();
	}

	public void setTag(String tag) {
		this.tag = tag == null ? null : tag.trim();
	}

}
