package com.intel.director.quartz;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Entry class for kicking off the poller. This class is started from director
 * start command
 * 
 * @author SIddharth
 * 
 */
public class ImageActionScheduler {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageActionScheduler.class);

	Scheduler scheduler = null;
	static ImageActionScheduler actionScheduler;

	private ImageActionScheduler() throws SchedulerException {
		scheduler = StdSchedulerFactory.getDefaultScheduler();
	}

	public static ImageActionScheduler getinstance() throws SchedulerException {
		if (actionScheduler == null) {
			actionScheduler = new ImageActionScheduler();
		}
		return actionScheduler;
	}

	/**
	 * Depending on the flag, either starts or stops the scheduler
	 * 
	 * @param args
	 *            contains start or stop
	 * @throws SchedulerException
	 */
	public static void main(String[] args) throws SchedulerException {
		log.info("**** Inside Scheduler Start/Stop");

		if (args[0].equals("start")) {
			ImageActionScheduler.getinstance().start();
		} else {
			ImageActionScheduler.getinstance().stop();
		}
	}

	/**
	 * Starts the scheduler after setting the poll interval for the poller
	 * 
	 * @throws SchedulerException
	 */
	public void start() throws SchedulerException {
		log.info("***** Scheduler Started ");
		JobDetail job = newJob(ImageActionPoller.class).withIdentity(
				"ImagePoller", "PollerGroup").build();

		// Trigger the job to run now, and then repeat every 40 seconds
		SimpleScheduleBuilder withIntervalInMinutes = SimpleScheduleBuilder
				.simpleSchedule().withIntervalInMinutes(1).repeatForever();
		Trigger trigger = newTrigger()
				.withIdentity("OnMinTrigger", "PollerGroup").startNow()
				.withSchedule(withIntervalInMinutes).build();

		// Tell quartz to schedule the job using our trigger
		scheduler.start();
		scheduler.scheduleJob(job, trigger);

	}

	
	/**
	 * Stops the scheduler
	 * @throws SchedulerException
	 */
	public void stop() throws SchedulerException {
		log.info("***** Scheduler Stopped");

		scheduler.shutdown(true);
	}

	public void blockUntilHttpServerShutdown() {
		try {
			if (!scheduler.isStarted()) {
				scheduler.start();
			}
		} catch (Exception e) {
			log.error("Error while running jetty", e);
		} finally {
			try {
				scheduler.shutdown(true);
			} catch (Exception e) {
				log.error("Error while stopping jetty", e);
			}
		}
	}
}
