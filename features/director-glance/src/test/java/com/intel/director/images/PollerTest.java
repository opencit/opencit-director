/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.director.images;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.ImageStoreRequest;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import java.io.IOException;
import org.apache.commons.configuration.BaseConfiguration;

public class PollerTest {

    public static void main(String[] args) throws IOException {
        org.apache.commons.configuration.Configuration apacheConfig = new BaseConfiguration();
        Configuration configuration = new CommonsConfiguration(apacheConfig);
        configuration.set("glance.endpoint.url", "http://www.google.com");
        GlanceImageStoreManager glanceImageStoreManager = new GlanceImageStoreManager(configuration);
        ImageStoreRequest imageStoreRequest = new ImageStoreRequest();
        imageStoreRequest.image_store_name = "glance";
        imageStoreRequest.image_store_id = "123";

        glanceImageStoreManager.uploadImage(imageStoreRequest);
    }
}
