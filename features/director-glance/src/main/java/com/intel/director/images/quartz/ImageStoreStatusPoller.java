/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.quartz;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author GS-0681
 */
public class ImageStoreStatusPoller implements Job {

    static {
        SchedulerFactory sf = new StdSchedulerFactory();
        try {
            Scheduler sched = sf.getScheduler();

            JobDetail job = JobBuilder.newJob()
                    .withIdentity("StatusPoller", "poller") // name "myJob", group "group1"
                    .ofType(ImageStoreStatusPoller.class)
                    .build();

            // Trigger the job to run now, and then every 40 seconds
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("PollerTrigger", "poller")
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
                    .build();
            // Tell quartz to schedule the job using our trigger
            sched.scheduleJob(job, trigger);

        } catch (SchedulerException ex) {
            Logger.getLogger(ImageStoreStatusPoller.class.getName()).log(Level.SEVERE, null, ex);
        }catch (Exception ex) {
            Logger.getLogger(ImageStoreStatusPoller.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {

        System.out.println("Inside execiute");
        try {
            Thread.sleep(1000 * 30);
        } catch (InterruptedException ex) {
            System.out.println("Error ");
            ex.printStackTrace();
        }

        System.out.println("COMPLETE");
    }
}
