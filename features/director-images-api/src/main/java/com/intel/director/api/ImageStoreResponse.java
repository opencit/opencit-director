package com.intel.director.api;

import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * 
 * @author GS-0681
 */
public class ImageStoreResponse extends GenericResponse {

	public List<ImageStoreTransferObject> image_stores;

	public List<ImageStoreTransferObject> getImage_stores() {
		return image_stores;
	}

	public void setImage_stores(List<ImageStoreTransferObject> image_stores) {
		this.image_stores = image_stores;
	}
}
