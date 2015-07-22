package com.intel.director.service;

import com.intel.director.api.ImageStoreUploadRequest;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.api.ImageStoreSearchResponse;
import com.intel.director.api.ImageStoreSearchRequest;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * 
 * this interface would be implemented by an implementation specific to 
 * an image store. Example. GlanceImageStoreManager would implement this interface
 * to invoke REST calls to Glance. Methods in this interface would then be invoked 
 * from the ImageService implementation.
 * 
 * @author GS-0681
 */


public interface ImageStoreManager {
    public ImageStoreUploadResponse uploadImage(ImageStoreUploadRequest imageStoreUploadRequest);
    public ImageStoreSearchResponse searchImages(ImageStoreSearchRequest imageStoreSearchRequest);
    public ImageStoreUploadResponse deleteImage(String imageId);
}
