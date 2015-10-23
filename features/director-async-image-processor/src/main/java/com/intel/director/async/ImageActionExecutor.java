/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.dnault.xmlpatch.internal.Log;
import com.intel.director.async.task.ImageActionTask;

/**
 *
 * @author GS-0681
 */
public class ImageActionExecutor {

    public static ExecutorService executorService = null;

    static {
        executorService = Executors.newFixedThreadPool(10);
    }

    
    /**
     * The method that accepts a task instance for submitting to the executor
     * 
     * 
     * @param actionTask ImageAction task instance
     */
    public static void submitTask(ImageActionTask actionTask) {
              
        executorService.execute(actionTask);
        Log.debug("Completed submitting task "+actionTask);
    }

}
