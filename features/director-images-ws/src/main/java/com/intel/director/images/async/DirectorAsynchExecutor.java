package com.intel.director.images.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectorAsynchExecutor {

	  public static ExecutorService executorService = null;

	    static {
	        executorService = Executors.newFixedThreadPool(10);
	    }

	    public static void submitTask(DockerPullTask dockerPullTask) {
	        System.out.println("Inside DirectorAsynchExecutor.submitTask");
	        executorService.execute(dockerPullTask);
	        System.out.println("Completed submitting task");
	    }

	
}
