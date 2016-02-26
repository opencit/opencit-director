package com.intel.mtwilson.director.data;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table(name = "MW_IMAGE_STORE")
public class MwImageStore {

	@Id
	@UuidGenerator(name = "UUID")
	@GeneratedValue(generator = "UUID")
	@Column(name = "id", length = 36)
	protected String id;

	@OneToMany(mappedBy = "image_store_id", cascade=CascadeType.ALL)
	private Collection<MwImageStoreDetails> mwImageStoreDetailsCollection;

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "artifact_type", nullable = false)
	private String artifact_type;

	@Column(name = "connector", nullable = false)
	private String connector;
	
	@OneToMany(mappedBy = "store")
	private Collection<MwPolicyUpload> tblMwPolicyUploadCollection;
	
	@OneToMany(mappedBy = "store")
	private Collection<MwImageUpload> tblMwImageUploadCollection;
	
	@Column(name = "deleted")
	private boolean deleted;

	public MwImageStore() {
		
	}


	public Collection<MwImageUpload> getTblMwImageUploadCollection() {
		return tblMwImageUploadCollection;
	}



	public void setTblMwImageUploadCollection(
			Collection<MwImageUpload> tblMwImageUploadCollection) {
		this.tblMwImageUploadCollection = tblMwImageUploadCollection;
	}



	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtifact_type() {
		return artifact_type;
	}

	public void setArtifact_type(String artifact_type) {
		this.artifact_type = artifact_type;
	}

	public String getConnector() {
		return connector;
	}

	public void setConnector(String connector) {
		this.connector = connector;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Collection<MwImageStoreDetails> getMwImageStoreDetailsCollection() {
		return mwImageStoreDetailsCollection;
	}

	public void setMwImageStoreDetailsCollection(
			Collection<MwImageStoreDetails> mwImageStoreDetailsCollection) {
		this.mwImageStoreDetailsCollection = mwImageStoreDetailsCollection;
	}

	public Collection<MwPolicyUpload> getTblMwPolicyUploadCollection() {
		return tblMwPolicyUploadCollection;
	}

	public void setTblMwPolicyUploadCollection(
			Collection<MwPolicyUpload> tblMwPolicyUploadCollection) {
		this.tblMwPolicyUploadCollection = tblMwPolicyUploadCollection;
	}
	
	

}
