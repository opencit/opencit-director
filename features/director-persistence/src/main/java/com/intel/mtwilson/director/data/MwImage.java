package com.intel.mtwilson.director.data;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "MW_IMAGE")
public class MwImage extends MwAuditable {

	@Column(name = "NAME")
	private String name;

	@Column(name = "IMAGE_DEPLOYMENTS", length = 20)
	private String imageDeploymentType;

	@Column(name = "IMAGE_FORMAT")
	private String imageFormat;

	@Column(name = "LOCATION")
	private String location;

	@Column(name = "MOUNTED_BY_USER_ID", length = 36)
	private String mountedByUserId;

	@Column(name = "DELETED")
	private boolean deleted;
	

	@OneToMany(mappedBy = "image")
	private Collection<MwImageUpload> tblMwimageUploadCollection;

	@OneToOne(optional = true,cascade = CascadeType.ALL)
	@JoinColumn(name = "TRUST_POLICY_ID", referencedColumnName = "ID")
	private MwTrustPolicy trustPolicy;


	@OneToOne(optional = true,cascade = CascadeType.ALL)
	@JoinColumn(name = "TRUST_POLICY_DRAFT_ID", referencedColumnName = "ID")
	private MwTrustPolicyDraft trustPolicyDraft;

	@Column(name = "STATUS")
	public String status;

	@Column(name = "CONTENT_LENGTH")
	public Long contentlength;
	
	@Column(name = "SENT")
	public Long sent;
	
	@Column(name = "repository")
	private String repository;
	
	@Column(name = "tag")
	private String tag;

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public MwImage() {
		super();
	}

	public String getImageDeploymentType() {
		return imageDeploymentType;
	}

	public void setImageDeploymentType(String imageDeploymentType) {
		this.imageDeploymentType = imageDeploymentType;
	}

	public String getImageFormat() {
		return imageFormat;
	}

	public void setImageFormat(String imageFormat) {
		this.imageFormat = imageFormat;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMountedByUserId() {
		return mountedByUserId;
	}

	public void setMountedByUserId(String mountedByUserId) {
		this.mountedByUserId = mountedByUserId;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<MwImageUpload> getTblMwimageUploadCollection() {
		return tblMwimageUploadCollection;
	}

	public void setTblMwimageUploadCollection(
			Collection<MwImageUpload> tblMwimageUploadCollection) {
		this.tblMwimageUploadCollection = tblMwimageUploadCollection;
	}

	public Long getContentlength() {
		return contentlength;
	}

	public void setContentlength(Long contentlength) {
		this.contentlength = contentlength;
	}

	public MwTrustPolicy getTrustPolicy() {
		return trustPolicy;
	}

	public void setTrustPolicy(MwTrustPolicy trustPolicy) {
		this.trustPolicy = trustPolicy;
	}

	public MwTrustPolicyDraft getTrustPolicyDraft() {
		return trustPolicyDraft;
	}

	public void setTrustPolicyDraft(MwTrustPolicyDraft trustPolicyDraft) {
		this.trustPolicyDraft = trustPolicyDraft;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getSent() {
		return sent;
	}

	public void setSent(Long sent) {
		this.sent = sent;
	}

	
}
