package com.intel.director.api.ui;

import java.util.Date;

public class ImageInfoFilter extends ImageInfo {

    protected Date from_created_date;
    protected Date to_created_date;
    protected SearchImageByPolicyCriteria policyCriteria;
    protected SearchImageByUploadCriteria uploadCriteria;

    public ImageInfoFilter() {
        super();
    }

    public SearchImageByPolicyCriteria getPolicyCriteria() {
        return policyCriteria;
    }

    public void setPolicyCriteria(SearchImageByPolicyCriteria policyCriteria) {
        this.policyCriteria = policyCriteria;
    }

    public SearchImageByUploadCriteria getUploadCriteria() {
        return uploadCriteria;
    }

    public void setUploadCriteria(SearchImageByUploadCriteria uploadCriteria) {
        this.uploadCriteria = uploadCriteria;
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

    @Override
    public String toString() {
        return "ImageInfoFilter [from_created_date=" + from_created_date
                + ", to_created_date=" + to_created_date + ", trust_policy_id="
                + trust_policy_id + ", trust_policy_name=" + trust_policy_name
                + ", trust_policy_draft_name=" + trust_policy_draft_name
                + ", trust_policy_draft_id=" + trust_policy_draft_id
                + ", uploads_count=" + uploads_count + ", id=" + id + ", image_name="
                + image_name + ", format=" + image_format + ", image_deployments="
                + image_deployments + ", status=" + status + ", image_size="
                + image_size + ", sent=" + sent + ", mounted_by_user_id="
                + mounted_by_user_id + ", deleted=" + deleted + ", location="
                + location + ", created_by_user_id=" + created_by_user_id
                + ", created_date=" + created_date + ", edited_by_user_id="
                + edited_by_user_id + ", edited_date=" + edited_date + "]";
    }

}
