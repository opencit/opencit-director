/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.async;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.intel.director.images.rs.GlanceRsClient;

/**
 *
 * @author GS-0681
 */
public class ImageTransferTask implements Runnable {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageTransferTask.class);

    private final String glanceid;
    private final GlanceRsClient glanceRsClient;
    private final Map<String, Object> imageProperties;
  

    public ImageTransferTask(Map<String, Object> imageProperties, String id, GlanceRsClient glanceRsClient) {
        this.glanceid = id;
        this.glanceRsClient = glanceRsClient;
        this.imageProperties=imageProperties;
    }

    @Override
    public void run() {
        log.info("Inside ImageTransferTask run()");        
        try {
        	log.info("Before uploading image using glance client ");
            glanceRsClient.uploadImage(imageProperties);
            log.info("Uploading .... ");
            Thread.sleep(1000 * 30);
        } catch (InterruptedException ex) {
            Logger.getLogger(ImageTransferTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
			log.error("Error in ImageTransferTask" , e);
		}
        System.out.println("Uploading complete");
    }

}
