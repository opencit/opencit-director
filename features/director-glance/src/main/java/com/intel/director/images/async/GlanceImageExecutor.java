/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author GS-0681
 */
public class GlanceImageExecutor {

    public static ExecutorService executorService = null;

    static {
        executorService = Executors.newFixedThreadPool(10);
    }

    public static void submitTask(ImageTransferTask imageTransferTask) {
        System.out.println("Inside GlanceImageExecutor.submitTask");
        executorService.execute(imageTransferTask);
        System.out.println("Completed submitting task");
    }

}
