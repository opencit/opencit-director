package com.intel.director.api;

public class ImageStoreDetailsTransferObject {

	public String id;
	public String image_store_id;
	public String key;
	public String value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getImage_store_id() {
		return image_store_id;
	}

	public void setImage_store_id(String image_store_id) {
		this.image_store_id = image_store_id;
	}

}
