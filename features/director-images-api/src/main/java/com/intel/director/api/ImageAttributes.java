/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;

import java.util.Date;

/**
 *
 * @author soakx
 */
public class ImageAttributes extends AuditFields {

    public String id;
    public String name;

  
    public String image_format;
    public String image_deployments;
    public String status;
    public Integer image_size;
    public Integer sent;
    public String mounted_by_user_id;
    public boolean deleted;
    public String location;

    public ImageAttributes() {
        super();
    }

    public ImageAttributes(String created_by_user_id, Date created_date,
            String edited_by_user_id, Date edited_date, String id, String name,
            String format, String image_deployments, String status,
            Integer image_size, Integer sent, String mounted_by_user_id,
            boolean deleted, String location) {
        super(created_by_user_id, created_date, edited_by_user_id, edited_date);
        this.id = id;
        this.name = name;
        this.image_format = format;
        this.image_deployments = image_deployments;
        this.status = status;
        this.image_size = image_size;
        this.sent = sent;
        this.mounted_by_user_id = mounted_by_user_id;
        this.deleted = deleted;
        this.location = location;
        

    }

    public ImageAttributes(String created_by_user_id, Date created_date,
            String edited_by_user_id, Date edited_date, String name,
            String format, String image_deployments, String status,
            Integer image_size, Integer sent, String mounted_by_user_id,
            boolean deleted, String location) {
        super(created_by_user_id, created_date, edited_by_user_id, edited_date);

        this.name = name;
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
        return "ImageAttributes [id=" + id + ", name=" + name + ", format="
                + image_format + ", image_deployments=" + image_deployments
                + ", status=" + status + ", image_size=" + image_size
                + ", sent=" + sent + ", mounted_by_user_id="
                + mounted_by_user_id + ", deleted=" + deleted + ", location="
                + location + ", content_length="
                + ", created_by_user_id=" + created_by_user_id
                + ", created_date=" + created_date + ", edited_by_user_id="
                + edited_by_user_id + ", edited_date=" + edited_date + "]";
    }

    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getImage_size() {
        return image_size;
    }

    public void setImage_size(Integer image_size) {
        this.image_size = image_size;
    }

    public Integer getSent() {
        return sent;
    }

    public void setSent(Integer sent) {
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

}
