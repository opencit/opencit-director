package com.intel.director.api.ui;

import com.intel.director.api.ImageAttributes;

public class ImageInfo extends ImageAttributes {

    protected String trust_policy_id;
    protected String trust_policy_name;
    protected String trust_policy_draft_name;
    protected String trust_policy_draft_id;
    protected Integer uploads_count;
    protected String policy_name;

    public String getPolicy_name() {
		return policy_name;
	}

	public void setPolicy_name(String policy_name) {
		this.policy_name = policy_name;
	}

	public String getTrust_policy_id() {
        return trust_policy_id;
    }

    public void setTrust_policy_id(String trust_policy_id) {
        this.trust_policy_id = trust_policy_id;
    }

    public String getTrust_policy_draft_id() {
        return trust_policy_draft_id;
    }

    public void setTrust_policy_draft_id(String trust_policy_draft_id) {
        this.trust_policy_draft_id = trust_policy_draft_id;
    }

    public Integer getUploads_count() {
        return uploads_count;
    }

    public void setUploads_count(Integer uploads_count) {
        this.uploads_count = uploads_count;
    }

    public String getTrust_policy_name() {
        return trust_policy_name;
    }

    public void setTrust_policy_name(String trust_policy_name) {
        this.trust_policy_name = trust_policy_name;
    }

    public String getTrust_policy_draft_name() {
        return trust_policy_draft_name;
    }

    public void setTrust_policy_draft_name(String trust_policy_draft_name) {
        this.trust_policy_draft_name = trust_policy_draft_name;
    }

    @Override
    public String toString() {
        return "ImageInfo [trust_policy_id=" + trust_policy_id
                + ", trust_policy_name=" + trust_policy_name
                + ", trust_policy_draft_name=" + trust_policy_draft_name
                + ", trust_policy_draft_id=" + trust_policy_draft_id
                + ", uploads_count=" + uploads_count + ", id=" + id + ", name="
                + name + ", format=" + image_format + ", image_deployments="
                + image_deployments + ", status=" + status + ", image_size="
                + image_size + ", sent=" + sent + ", mounted_by_user_id="
                + mounted_by_user_id + ", deleted=" + deleted + ", location="
                + location + ", created_by_user_id=" + created_by_user_id
                + ", created_date=" + created_date + ", edited_by_user_id="
                + edited_by_user_id + ", edited_date=" + edited_date + "]";
    }

}
