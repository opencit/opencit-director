/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.director.images;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.BaseConfiguration;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.images.glance.constants.Constants;

public class PollerTest {

    public static void main(String[] args) throws Exception {
        org.apache.commons.configuration.Configuration apacheConfig = new BaseConfiguration();
        Configuration configuration = new CommonsConfiguration(apacheConfig);
        configuration.set(Constants.GLANCE_API_ENDPOINT, "http://10.35.35.136:9292");
        configuration.set(Constants.GLANCE_IMAGE_STORE_USERNAME,"admin");
        configuration.set(Constants.GLANCE_IMAGE_STORE_PASSWORD,"intelmh");
        configuration.set(Constants.GLANCE_TENANT_NAME,"admin");
        
        GlanceImageStoreManager glanceImageStoreManager = new GlanceImageStoreManager(configuration);
        /*ImageStoreRequest imageStoreRequest = new ImageStoreRequest();
        imageStoreRequest.disk_format = "qcow2";*/
        Map<String, String> imageProperties = new HashMap<>();
        imageProperties.put(com.intel.director.constants.Constants.NAME, "test_upload");
        imageProperties.put(com.intel.director.constants.Constants.DISK_FORMAT, "qcow2");
        imageProperties.put(com.intel.director.constants.Constants.CONTAINER_FORMAT,"bare");
        imageProperties.put(com.intel.director.constants.Constants.IS_PUBLIC, "true");
        File file = new File("C:/MysteryHill/DirectorAll/Docs/vm_launch.txt");
        String id=glanceImageStoreManager.upload(file ,imageProperties);
        ImageStoreUploadResponse resp=glanceImageStoreManager.fetchDetails(null, id);
        System.out.println("ImageStoreUploadResponse::"+resp);
        
        
    }
}
