package com.intel.director.quartz;

import com.intel.dcsg.cpg.console.AbstractCommand;
import com.intel.director.util.UnmountImageHandler;

/**
 * Entry class for kicking off the poller. This class is started from director
 * start command
 * 
 * @author SIddharth
 * 
 */
public class TrustDirectorScheduler extends AbstractCommand {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(TrustDirectorScheduler.class);

	static TrustDirectorScheduler actionScheduler;
	private static ImageActionPoller actionPoller = new ImageActionPoller();
	private static UnmountImageHandler unmountImageHandler = new UnmountImageHandler();

	@Override
	public void execute(String[] args) {
		while (true) {
			try {
				runImageActionPoller();
				runUnmountPoller();
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				log.error("Error in thread running the scheduler tasks", e);
			}

		}
	}


	/**
	 * Depending on the flag, either starts or stops the scheduler
	 * 
	 * @param args
	 *            contains start or stop
	 * @throws InterruptedException
	 */
	public static void runImageActionPoller() throws InterruptedException {
		log.info("*** Executing task to process the image actions");
		actionPoller.execute();
	}

	public static void runUnmountPoller() throws InterruptedException {
		log.info("*** Executing task to unmount remote hosts that are not in use");
		unmountImageHandler.unmountUnusedImages();
	}
}
