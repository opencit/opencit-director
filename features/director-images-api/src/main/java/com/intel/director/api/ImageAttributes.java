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
public class ImageAttributes {
    public String name;
    public String id;
    public String location;
    public String image_format;
    public String image_deployments;
    public String status;
    public Long image_size;
    public Integer sent;
    public Boolean mounted;
    public String mountedBy;

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImage_format(String image_format) {
        this.image_format = image_format;
    }

    public void setImage_deployments(String image_deployments) {
        this.image_deployments = image_deployments;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImage_size(Long image_size) {
        this.image_size = image_size;
    }

    public void setSent(Integer sent) {
        this.sent = sent;
    }

    public void setMounted(Boolean mounted) {
        this.mounted = mounted;
    }

    public void setMountedBy(String mountedBy) {
        this.mountedBy = mountedBy;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getImage_format() {
        return image_format;
    }

    public String getImage_deployments() {
        return image_deployments;
    }

    public String getStatus() {
        return status;
    }

    public Long getImage_size() {
        return image_size;
    }

    public Integer getSent() {
        return sent;
    }

    public Boolean getMounted() {
        return mounted;
    }

    public String getMountedBy() {
        return mountedBy;
    }
    
    
    
}
