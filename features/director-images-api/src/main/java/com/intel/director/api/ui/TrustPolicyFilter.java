package com.intel.director.api.ui;

import com.intel.director.api.TrustPolicy;
import java.util.Date;

public class TrustPolicyFilter extends TrustPolicy {

    protected Date from_created_date;

    protected Date to_created_date;

    protected Date from_edited_date;

    protected Date to_edited_date;

    protected String imageName;

    public String name;

    public String format;

    public String image_deployments;

    protected Date from_image_created_date;

    protected Date to_image_created_date;

    public TrustPolicyFilter() {
        super();
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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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
