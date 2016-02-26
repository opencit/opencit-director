package com.intel.mtwilson.director.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;


@Entity
@Table(name = "MW_IMAGE_STORE_DETAILS")
public class MwImageStoreDetails {

	@Id
	@UuidGenerator(name="UUID")
	@GeneratedValue(generator="UUID")
  	@Column(name = "id", length = 36)
	protected String id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "IMAGE_STORE_ID", referencedColumnName = "ID")
	private MwImageStore image_store_id;
	
	@Column(name = "key", nullable = false)
	private String key;
	
	@Column(name = "value", nullable = false)
	private String value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public MwImageStore getImage_store() {
		return image_store_id;
	}

	public void setImage_store(MwImageStore image_store) {
		this.image_store_id = image_store;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
