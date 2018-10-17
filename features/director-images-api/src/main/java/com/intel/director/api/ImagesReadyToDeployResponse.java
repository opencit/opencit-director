package com.intel.director.api;


public class ImagesReadyToDeployResponse extends AuditFields {
	String id;
	String image_name;
	String image_format;
	String display_name;
	String user;

	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImage_name() {
		return image_name;
	}

	public void setImage_name(String image_name) {
		this.image_name = image_name;
	}

	public String getImage_format() {
		return image_format;
	}

	public void setImage_format(String image_format) {
		this.image_format = image_format;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
