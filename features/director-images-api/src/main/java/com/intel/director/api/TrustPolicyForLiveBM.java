package com.intel.director.api;

import java.util.Calendar;

public class TrustPolicyForLiveBM extends AuditFields {

    protected String id;

    protected String description;

    protected String trust_policy;

    protected String name;

    protected ImageAttributes imgAttributes;
    
    protected String display_name;

    public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public TrustPolicyForLiveBM() {
        super();
    }

    public TrustPolicyForLiveBM(String created_by_user_id, Calendar created_date,
            String edited_by_user_id, Calendar edited_date, String id,
            String description, String trust_policy, String name,
            ImageAttributes imgAttributes) {
        super(created_by_user_id, created_date, edited_by_user_id, edited_date);
        this.id = id;
        this.description = description;
        this.trust_policy = trust_policy;
        this.name = name;
        this.imgAttributes = imgAttributes;
    }

    public TrustPolicyForLiveBM(String created_by_user_id, Calendar created_date,
            String edited_by_user_id, Calendar edited_date,
            String description, String trust_policy, String name,
            ImageAttributes imgAttributes) {
        super(created_by_user_id, created_date, edited_by_user_id, edited_date);
        this.description = description;
        this.trust_policy = trust_policy;
        this.name = name;
        this.imgAttributes = imgAttributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTrust_policy() {
        return trust_policy;
    }

    public void setTrust_policy(String trust_policy) {
        this.trust_policy = trust_policy;
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
        return "TrustPolicy [id=" + id + ", description=" + description
                + ", trust_policy=" + trust_policy + ", name=" + name
                + ", imgAttributes=" + imgAttributes + ", created_by_user_id="
                + created_by_user_id + ", created_date=" + created_date
                + ", edited_by_user_id=" + edited_by_user_id + ", edited_date="
                + edited_date + "]";
    }

}
