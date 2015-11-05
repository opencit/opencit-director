/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.async;

import java.io.File;
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
    private final File uploadfile;
    private final Map<String, String> imageProperties;
  

    public ImageTransferTask(File file, Map<String, String> imageProperties, String id, GlanceRsClient glanceRsClient) {
        this.glanceid = id;
        this.glanceRsClient = glanceRsClient;
        this.imageProperties=imageProperties;
        this.uploadfile=file;
    }

    @Override
    public void run() {
        //glanceRsClient.uploadImage(imageStoreRequest);
        System.out.println("RUN inside task");
        try {
        	System.out.println("Before glance response..");
            glanceRsClient.uploadImage(uploadfile,imageProperties,glanceid);
            System.out.println("Uploading .... ");
            Thread.sleep(1000 * 30);
        } catch (InterruptedException ex) {
            Logger.getLogger(ImageTransferTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
			// TODO Handle Error
			log.error("Error in ImageTransferTask" + e);
		}
        System.out.println("Uploading complete");
    }

}
