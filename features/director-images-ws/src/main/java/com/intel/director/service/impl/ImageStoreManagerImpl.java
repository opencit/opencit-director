/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import com.intel.dcsg.cpg.extensions.Plugins;
import com.intel.director.api.ImageStoreRequest;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.exception.ImageStoreException;
import com.intel.director.images.GlanceImageStoreManager;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageStoreManager;
import com.intel.director.util.DirectorUtil;
import java.io.IOException;

/**
 *
 * @author Siddharth
 */
public class ImageStoreManagerImpl implements ImageStoreManager {

    protected ImageStoreManager getImageStoreManager(ImageStoreRequest imageStoreRequest) throws DirectorException {
        return Plugins.findByAttribute(ImageStoreManager.class, "class", DirectorUtil.getImageStoreManager(imageStoreRequest.image_store_name));
    }

    @Override
    public ImageStoreResponse uploadImage(ImageStoreRequest imageStoreUploadRequest) throws DirectorException, ImageStoreException {
        //Get the image store implementation for the store requested        
        ImageStoreResponse imageStoreUploadResponse = getImageStoreManager(imageStoreUploadRequest).uploadImage(imageStoreUploadRequest);
        return imageStoreUploadResponse;
    }

    @Override
    public ImageStoreResponse searchImages(ImageStoreRequest imageStoreSearchRequest) throws DirectorException, ImageStoreException {
        //Get the image store implementation for the store requested
        ImageStoreResponse imageStoreResponse = getImageStoreManager(imageStoreSearchRequest).searchImages(imageStoreSearchRequest);
        return imageStoreResponse;
    }

    @Override
    public ImageStoreResponse deleteImage(ImageStoreRequest imageStoreDeleteRequest) throws DirectorException, ImageStoreException {
        ImageStoreResponse imageStoreSearchResponse = getImageStoreManager(imageStoreDeleteRequest).searchImages(imageStoreDeleteRequest);
        return imageStoreSearchResponse;
    }

    @Override
    public ImageStoreResponse fetchImageDetails(ImageStoreRequest imageStoreDeleteRequest) throws DirectorException, ImageStoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
