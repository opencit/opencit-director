/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import com.intel.dcsg.cpg.extensions.Plugins;
import com.intel.director.api.ImageStoreRequest;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.api.ImageStoreManager;
import com.intel.director.util.DirectorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Siddharth
 */
public class ImageStoreManagerImpl implements ImageStoreManager {

    private static final Logger log = LoggerFactory.getLogger(ImageStoreManagerImpl.class);

    protected ImageStoreManager getImageStoreManager(ImageStoreRequest imageStoreRequest) throws DirectorException {
        return Plugins.findByAttribute(ImageStoreManager.class, "class", DirectorUtil.getImageStoreManager(imageStoreRequest.image_store_name));
    }

    @Override
    public ImageStoreResponse uploadImage(ImageStoreRequest imageStoreUploadRequest) {
        //Get the image store implementation for the store requested        
        ImageStoreResponse imageStoreUploadResponse = null;

        try {
            imageStoreUploadResponse = getImageStoreManager(imageStoreUploadRequest).uploadImage(imageStoreUploadRequest);
        } catch (DirectorException ex) {
            log.error("Error uploading image", ex);
        }

        return imageStoreUploadResponse;
    }

    @Override
    public ImageStoreResponse searchImages(ImageStoreRequest imageStoreSearchRequest) {
        //Get the image store implementation for the store requested
        ImageStoreResponse imageStoreResponse = null;
        try {
            imageStoreResponse = getImageStoreManager(imageStoreSearchRequest).searchImages(imageStoreSearchRequest);
        } catch (DirectorException ex) {
            log.error("Error searching image", ex);
        }
        return imageStoreResponse;
    }

    @Override
    public ImageStoreResponse deleteImage(ImageStoreRequest imageStoreDeleteRequest) {
        ImageStoreResponse imageStoreSearchResponse = null;
        try {
            imageStoreSearchResponse = getImageStoreManager(imageStoreDeleteRequest).searchImages(imageStoreDeleteRequest);
        } catch (DirectorException ex) {
            log.error("Error deleting image", ex);
        }
        return imageStoreSearchResponse;
    }

    @Override
    public ImageStoreResponse fetchImageDetails(ImageStoreRequest imageStoreDeleteRequest) {
        ImageStoreResponse imageStoreSearchResponse = null;
        try {
            imageStoreSearchResponse = getImageStoreManager(imageStoreDeleteRequest).fetchImageDetails(imageStoreDeleteRequest);
        } catch (DirectorException ex) {
            log.error("Error fetching image", ex);
        }
        return imageStoreSearchResponse;
    }

}
