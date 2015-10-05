/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.imagestore;

import java.io.File;
import java.util.Map;

import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.exception.ImageStoreException;


/**
 *
 * @author GS-0681
 */
public interface ImageStoreManager {

	  public String upload(File file, Map<String, String> imageProperties) throws ImageStoreException;
	    public ImageStoreUploadResponse fetchDetails(Map<String, String> imageProperties,String glanceId) throws ImageStoreException ;
}
