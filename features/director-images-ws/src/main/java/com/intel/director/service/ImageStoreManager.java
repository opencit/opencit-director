/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service;

import java.io.File;
import java.util.Map;

import com.intel.director.api.ImageStoreRequest;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.exception.ImageStoreException;
import com.intel.director.images.exception.DirectorException;

/**
 *
 * @author GS-0681
 */
public interface ImageStoreManager {

    public ImageStoreResponse upload(File file, Map<String, String> imageProperties) throws  DirectorException;
    
    

   public ImageStoreResponse searchImages(ImageStoreRequest imageStoreSearchRequest) throws DirectorException;

    public ImageStoreResponse deleteImage(ImageStoreRequest imageStoreDeleteRequest) throws DirectorException;

    public ImageStoreResponse fetchImageDetails(ImageStoreRequest imageStoreDeleteRequest) throws DirectorException;
}
