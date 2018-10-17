package com.intel.director.api;



public class TrustPolicyDraftRequest extends AuditFields {
	String id;
	String display_name;
	String image_format;
	String name;
	

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImage_name() {
		return display_name;
	}

	public void setImage_name(String image_name) {
		this.display_name = image_name;
	}

	public String getImage_format() {
		return image_format;
	}

	public void setImage_format(String image_format) {
		this.image_format = image_format;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
