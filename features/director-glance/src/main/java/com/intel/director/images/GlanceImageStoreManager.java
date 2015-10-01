/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.exception.ImageStoreException;
import com.intel.director.images.rs.GlanceRsClient;
import com.intel.director.images.rs.GlanceRsClientBuilder;
import com.intel.director.imagestore.ImageStoreManager;
import com.intel.mtwilson.configuration.ConfigurationFactory;

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
    public String upload(File file, Map<String, String> imageProperties) throws ImageStoreException {
    	String glanceid;
        try {
            //this is a 2 step process
            //1) We will push the metadata and create an image id, which would be used later on for checking the status
            //http://docs.openstack.org/developer/glance/glanceapi.html#reserve-a-new-image
            glanceid = glanceRsClient.uploadImageMetaData(imageProperties);
          ///  glanceResponse.id = "123";
            //2) The actual transfer of image. This is an async process
            //http://docs.openstack.org/developer/glance/glanceapi.html#add-a-new-image
            uploadImage(file,imageProperties, glanceid);
          return glanceid;
            ////startPolling(file.geimageProperties, glanceid);
        } catch (Exception e) {
            throw new ImageStoreException("Error while uploading image to Glance", e);
        }
      
    }


    
    public ImageStoreUploadResponse fetchDetails(Map<String, String> imageProperties,String glanceId) throws ImageStoreException {
    	
    	try{
    	return glanceRsClient.fetchDetails(imageProperties, glanceId);
    	
    	}catch(Exception e){
    	 
             throw new ImageStoreException("Error while fetchDetails if upload from Glance", e);
         
    	}
    }
    
    

    private void uploadImage(File file, Map<String, String> imageProperties, String id) throws IOException {
    	  glanceRsClient.uploadImage(file,imageProperties,id);
    	
      /*  System.out.println("Inside async uploadImage");
        ImageTransferTask imageTransferTask = new ImageTransferTask(file,imageProperties,id, glanceRsClient);
        System.out.println("Created TASK");
        GlanceImageExecutor.submitTask(imageTransferTask);
        System.out.println("Task submitted");*/
    }
    
    
    
  /*  private void startPolling(Map<String, String> imageProperties,String glanceid){
    	 
        System.out.println("Inside start polling method");

		System.out.println("Inside static block of ImageStoreStatusPoller");
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			Scheduler sched = sf.getScheduler();

			JobDetail job = JobBuilder.newJob()
					.withIdentity("StatusPoller", "poller") // name "myJob",
															// group "group1"
					.ofType(ImageStoreStatusPoller.class).build();
			job.getJobDataMap().put("glanceid", glanceid);
			job.getJobDataMap().put("glancersclient", glanceRsClient);
			job.getJobDataMap().put("imagepropertiesmap", imageProperties);

			// Trigger the job to run now, and then every 40 seconds
			Trigger trigger = TriggerBuilder
					.newTrigger()
					.withIdentity("PollerTrigger", "poller")
					.withSchedule(
							CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
					.build();
			// Tell quartz to schedule the job using our trigger
			sched.start();
			sched.scheduleJob(job, trigger);
			System.out
					.println("Inside static block of ImageStoreStatusPoller  sched.start()");
		} catch (SchedulerException ex) {
			Logger.getLogger(ImageStoreStatusPoller.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (Exception ex) {
			Logger.getLogger(ImageStoreStatusPoller.class.getName()).log(
					Level.SEVERE, null, ex);
		}

	
        	/// ImageStoreStatusPoller imgPoller=new ImageStoreStatusPoller(glanceRsClient,imageProperties,glanceid);

        

    }*/
}
