package com.intel.director.quartz;

import com.intel.dcsg.cpg.console.AbstractCommand;

/**
 * Entry class for kicking off the poller. This class is started from director
 * start command
 * 
 * @author SIddharth
 * 
 */
public class ImageActionScheduler extends AbstractCommand {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageActionScheduler.class);

	static ImageActionScheduler actionScheduler;

	@Override
	public void execute(String[] args) {
		try {
			main(args);
		} catch (InterruptedException e) {
			log.error("Error starting scheduler : " + e);
		}
	}

	/**
	 * Depending on the flag, either starts or stops the scheduler
	 * 
	 * @param args
	 *            contains start or stop
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		log.info("**** Inside Scheduler Start/Stop");
		ImageActionPoller actionPoller = new ImageActionPoller();
		while (true) {
			actionPoller.execute();
			Thread.sleep(60000);
		}
	}

}
