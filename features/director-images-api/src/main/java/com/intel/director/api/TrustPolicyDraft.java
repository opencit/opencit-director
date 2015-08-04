package com.intel.director.api;

public class TrustPolicyDraft extends AuditFields {

    protected String id;

    protected String trust_policy_draft;

    protected String name;

    protected ImageAttributes imgAttributes;

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

}
