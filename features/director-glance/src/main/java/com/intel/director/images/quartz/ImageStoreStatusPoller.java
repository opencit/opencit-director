/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.quartz;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.intel.director.api.ImageStoreManager;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.images.rs.GlanceRsClient;

/**
 * 
 * @author GS-0681
 */
public class ImageStoreStatusPoller implements Job {
	GlanceRsClient glanceRsClient;
	Map<String, String> imageProperties;
	String glanceid;
///	ImageStoreUploadResponse imgResponse= new ImageStoreResponse();


	
	
	public ImageStoreStatusPoller() {
	
	}

	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
			
		JobDataMap jobMap=jec.getMergedJobDataMap();
		 glanceid=(String) jobMap.get("glanceid");
		 imageProperties=(Map<String, String>) jobMap.get("imagepropertiesmap");
		 glanceRsClient=(GlanceRsClient) jobMap.get("glancersclient");
		System.out.println("Inside execute");
		try {
			
			glanceRsClient.fetchDetails(imageProperties, glanceid);
			
			
			Thread.sleep(1000 * 30);
		} catch (InterruptedException | IOException ex) {
			throw new JobExecutionException(ex);
		}

		System.out.println("COMPLETE");
	}
}
