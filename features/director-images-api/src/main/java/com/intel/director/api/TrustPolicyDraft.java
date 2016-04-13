package com.intel.director.api;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
public class TrustPolicyDraft extends AuditFields {

    protected String id;

    protected String trust_policy_draft;

    protected String name;

    @JsonProperty("image_attributes")
    protected ImageAttributes imgAttributes;

    protected String display_name;
    
    protected String error;
    
    public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public TrustPolicyDraft() {
        super();
    }

    public TrustPolicyDraft(String created_by_user_id, Calendar created_date,
            String edited_by_user_id, Calendar edited_date, String id,
            String trust_policy_draft, String name,
            ImageAttributes imgAttributes) {
        super(created_by_user_id, created_date, edited_by_user_id, edited_date);
        this.id = id;
        this.trust_policy_draft = trust_policy_draft;
        this.name = name;
        this.imgAttributes = imgAttributes;
    }

    public TrustPolicyDraft(String created_by_user_id, Calendar created_date,
            String edited_by_user_id, Calendar edited_date,
            String trust_policy_draft, String name,
            ImageAttributes imgAttributes) {
        super(created_by_user_id, created_date, edited_by_user_id, edited_date);

        this.trust_policy_draft = trust_policy_draft;
        this.name = name;
        this.imgAttributes = imgAttributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrust_policy_draft() {
        return trust_policy_draft;
    }

    public void setTrust_policy_draft(String trust_policy_draft) {
        this.trust_policy_draft = trust_policy_draft;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageAttributes getImgAttributes() {
        return imgAttributes;
    }

    public void setImgAttributes(ImageAttributes imgAttributes) {
        this.imgAttributes = imgAttributes;
    }

    @Override
    public String toString() {
        return "TrustPolicyDraft [id=" + id + ", trust_policy_draft="
                + trust_policy_draft + ", name=" + name + ", imgAttributes="
                + imgAttributes + ", created_by_user_id=" + created_by_user_id
                + ", created_date=" + created_date + ", edited_by_user_id="
                + edited_by_user_id + ", edited_date=" + edited_date + "]";
    }

}
