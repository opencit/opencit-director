/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service;

import com.intel.director.api.ImageStoreRequest;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.exception.ImageStoreException;
import com.intel.director.images.exception.DirectorException;

/**
 *
 * @author GS-0681
 */
public interface ImageStoreManager {

    public ImageStoreResponse uploadImage(ImageStoreRequest imageStoreUploadRequest) throws ImageStoreException, DirectorException;

    public ImageStoreResponse searchImages(ImageStoreRequest imageStoreSearchRequest) throws DirectorException, ImageStoreException;

    public ImageStoreResponse deleteImage(ImageStoreRequest imageStoreDeleteRequest) throws DirectorException, ImageStoreException;

    public ImageStoreResponse fetchImageDetails(ImageStoreRequest imageStoreDeleteRequest) throws DirectorException, ImageStoreException;
}
