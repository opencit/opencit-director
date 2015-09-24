/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.ImageStoreManager;
import com.intel.director.api.ImageStoreRequest;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.images.rs.GlanceException;
import com.intel.director.images.rs.GlanceRsClient;
import com.intel.director.images.rs.GlanceRsClientBuilder;
import com.intel.mtwilson.configuration.ConfigurationFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author GS-0681
 */
public class GlanceImageStoreManager implements ImageStoreManager {

    GlanceRsClient glanceRsClient;
    String url;

    public GlanceImageStoreManager(Configuration configuration) throws IOException {
        glanceRsClient = GlanceRsClientBuilder.build(configuration);
    }

    public GlanceImageStoreManager() throws IOException {
        Configuration configuration = ConfigurationFactory.getConfiguration();
        glanceRsClient = GlanceRsClientBuilder.build(configuration);
    }

    @Override
    public void upload(File file, Map<String, String> imageProperties) throws GlanceException {
    	String glanceid;
        try {
            //this is a 2 step process
            //1) We will push the metadata and create an image id, which would be used later on for checking the status
            //http://docs.openstack.org/developer/glance/glanceapi.html#reserve-a-new-image
            glanceid = glanceRsClient.uploadImageMetaData(imageProperties);
          ///  glanceResponse.id = "123";
            //2) The actual transfer of image. This is an async process
            //http://docs.openstack.org/developer/glance/glanceapi.html#add-a-new-image
         
            ////startPolling(file.geimageProperties, glanceid);
        } catch (Exception e) {
            throw new GlanceException("Error while uploading image to Glance", e);
        }
      
    }

 /*   @Override
    public ImageStoreResponse searchImages(ImageStoreRequest imageStoreSearchRequest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ImageStoreResponse deleteImage(ImageStoreRequest imageStoreDeleteRequest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ImageStoreResponse fetchImageDetails(ImageStoreRequest imageStoreDeleteRequest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

  /*  private ImageStoreResponse mapGlanceResponseToImageStoreResponse(GlanceResponse glanceResponse) {
        Mapper m = new DozerBeanMapper();
        ImageStoreResponse imageStoreResponse = m.map(glanceResponse, ImageStoreResponse.class);
        return imageStoreResponse;
    }*/

}
