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
import com.intel.director.service.ImageStoreManager;
import com.intel.director.util.DirectorUtil;
import org.slf4j.Logger;
import java.util.Map;
import java.io.File;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Siddharth
 */
public class ImageStoreManagerImpl implements ImageStoreManager {

    private static final Logger log = LoggerFactory.getLogger(ImageStoreManagerImpl.class);
    public ImageStoreResponse upload(File file, Map<String, String> imageProperties) throws  DirectorException{return null;}
    
    

   public ImageStoreResponse searchImages(ImageStoreRequest imageStoreSearchRequest) throws DirectorException{return null;}

    public ImageStoreResponse deleteImage(ImageStoreRequest imageStoreDeleteRequest) throws DirectorException{return null;}

    public ImageStoreResponse fetchImageDetails(ImageStoreRequest imageStoreDeleteRequest) throws DirectorException{return null;}

}
