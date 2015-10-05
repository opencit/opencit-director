package com.intel.mtwilson.director.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table(name = "MW_IMAGE_STORE_SETTINGS")
public class MwImageStoreSettings {
	
	
	@Id
	@UuidGenerator(name="UUID")
	@GeneratedValue(generator="UUID")
  	@Column(name = "ID", length = 36)
	protected String id;
	
	
	
	@Column(name = "NAME",unique=true)
	private String name;
	
	@Column(name = "PROVIDER_CLASS")
	private String provider_class;

	
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

	public String getProvider_class() {
		return provider_class;
	}

	public void setProvider_class(String provider_class) {
		this.provider_class = provider_class;
	}
	
	
	
	
}
