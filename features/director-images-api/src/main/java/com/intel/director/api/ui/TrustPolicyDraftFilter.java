package com.intel.director.api.ui;

import java.util.Date;

import com.intel.director.api.TrustPolicyDraft;

public class TrustPolicyDraftFilter extends TrustPolicyDraft {

    protected Date from_created_date;

    protected Date to_created_date;

    protected Date from_edited_date;

    protected Date to_edited_date;

    public String image_id;
    
    public String host_id;

    public String image_name;

    public String image_format;

    public String image_deployments;

    protected Date from_image_created_date;

    protected Date to_image_created_date;

    public TrustPolicyDraftFilter() {
        super();
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }
    
    public String getHostId() {
        return host_id;
    }
    
    public void setHostId(String host_id) {
        this.host_id = host_id;
    }

    public Date getFrom_created_date() {
        return from_created_date;
    }

    public void setFrom_created_date(Date from_created_date) {
        this.from_created_date = from_created_date;
    }

    public Date getTo_created_date() {
        return to_created_date;
    }

    public void setTo_created_date(Date to_created_date) {
        this.to_created_date = to_created_date;
    }

    public Date getFrom_edited_date() {
        return from_edited_date;
    }

    public void setFrom_edited_date(Date from_edited_date) {
        this.from_edited_date = from_edited_date;
    }

    public Date getTo_edited_date() {
        return to_edited_date;
    }

    public void setTo_edited_date(Date to_edited_date) {
        this.to_edited_date = to_edited_date;
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

    public Date getFrom_image_created_date() {
        return from_image_created_date;
    }

    public void setFrom_image_created_date(Date from_image_created_date) {
        this.from_image_created_date = from_image_created_date;
    }

    public Date getTo_image_created_date() {
        return to_image_created_date;
    }

    public void setTo_image_created_date(Date to_image_created_date) {
        this.to_image_created_date = to_image_created_date;
    }

}
