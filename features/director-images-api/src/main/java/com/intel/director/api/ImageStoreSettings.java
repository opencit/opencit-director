package com.intel.director.api;

public class ImageStoreSettings {

	protected String id;
	
	protected String name;
	
	protected String provider_class;
	
	
	

	public ImageStoreSettings() {
		super();
	}

	public ImageStoreSettings(String name, String provider_class) {
		super();
		this.name = name;
		this.provider_class = provider_class;
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

	public String getProvider_class() {
		return provider_class;
	}

	public void setProvider_class(String provider_class) {
		this.provider_class = provider_class;
	}
	
	
	
}
