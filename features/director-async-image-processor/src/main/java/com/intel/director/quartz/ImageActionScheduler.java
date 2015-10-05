package com.intel.director.quartz;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class ImageActionScheduler {
	public static void main(String[] args) throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

		JobDetail job = newJob(ImageActionPoller.class).withIdentity("ImagePoller",
				"PollerGroup").build();

		
		// Trigger the job to run now, and then repeat every 40 seconds
		SimpleScheduleBuilder withIntervalInMinutes = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1).repeatForever();
		Trigger trigger = newTrigger().withIdentity("OnMinTrigger", "PollerGroup").startNow().withSchedule(withIntervalInMinutes).build();

		// Tell quartz to schedule the job using our trigger
		scheduler.start();
		scheduler.scheduleJob(job, trigger);

	}
}
