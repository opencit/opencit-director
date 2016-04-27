package com.intel.director.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ImageStoreDetailsTransferObject implements Comparable<ImageStoreDetailsTransferObject> {

	public String id;
	public String image_store_id;
	public String key;
	public String value;
	private String keyDisplayValue;
	private String placeHolderValue;
	@JsonIgnore
	public int seqNo;

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
	
	

	public String getKeyDisplayValue() {
		return keyDisplayValue;
	}

	public void setKeyDisplayValue(String keyDisplayValue) {
		this.keyDisplayValue = keyDisplayValue;
	}

	
	
	public String getPlaceHolderValue() {
		return placeHolderValue;
	}

	public void setPlaceHolderValue(String placeHolderValue) {
		this.placeHolderValue = placeHolderValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageStoreDetailsTransferObject other = (ImageStoreDetailsTransferObject) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(ImageStoreDetailsTransferObject o) {
		return seqNo - o.seqNo; 
	}

	
	
}
