package com.intel.mtwilson.director.data;

import java.util.Collection;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Cacheable(false)
@Table(name = "MW_TRUST_POLICY")
public class MwTrustPolicy extends MwAuditable {


	@ManyToOne(optional = false)
	@JoinColumn(name = "IMAGE_ID", referencedColumnName = "ID")
	private MwImage image;
	
	@OneToMany(mappedBy = "trustPolicy")
	private Collection<MwPolicyUpload> tblMwPolicyUploadCollection;

	@ManyToOne
	@JoinColumn(name = "HOST_ID", referencedColumnName = "ID")
	private MwHost host;

	@Column(name = "NAME")
	private String name;

	@Column(name = "TRUST_POLICY")
	private Character[] trustPolicy;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "DISPLAY_NAME")
	private String display_name;
	
	@Column(name = "ARCHIVE")
	private boolean archive;

	public MwTrustPolicy() {
		super();
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public MwImage getImage() {
		return image;
	}

	public void setImage(MwImage image) {
		this.image = image;
	}

	public Collection<MwPolicyUpload> getTblMwPolicyUploadCollection() {
		return tblMwPolicyUploadCollection;
	}

	public void setTblMwPolicyUploadCollection(
			Collection<MwPolicyUpload> tblMwPolicyUploadCollection) {
		this.tblMwPolicyUploadCollection = tblMwPolicyUploadCollection;
	}

	public MwHost getHost() {
		return host;
	}

	public void setHost(MwHost host) {
		this.host = host;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Character[] getTrustPolicy() {
		return trustPolicy;
	}

	public void setTrustPolicy(Character[] trustPolicy) {
		this.trustPolicy = trustPolicy;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isArchive() {
		return archive;
	}

	public void setArchive(boolean archive) {
		this.archive = archive;
	}
	
	

}
