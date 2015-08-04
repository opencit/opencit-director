package com.intel.mtwilson.director.data;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table(name = "MW_TRUST_POLICY_DRAFT")
public class MwTrustPolicyDraft extends MwAuditable {

    @OneToOne(optional = false)
    @JoinColumn(name = "IMAGE_ID", referencedColumnName = "ID")
    private MwImage image;

    @Column(name = "TRUST_POLICY_DRAFT")
    private Character[] trustPolicyDraft;

    @Column(name = "NAME")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MwImage getImage() {
        return image;
    }

    public void setImage(MwImage image) {
        this.image = image;
    }

    public Character[] getTrustPolicyDraft() {
        return trustPolicyDraft;
    }

    public void setTrustPolicyDraft(Character[] trustPolicyDraft) {
        this.trustPolicyDraft = trustPolicyDraft;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
