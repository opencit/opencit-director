package com.intel.director.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by GS-0681 on 21-02-2017.
 */
public class FileFormatExecutor {

    public static ExecutorService executorService = null;

    static {
        executorService = Executors.newFixedThreadPool(10);
    }

    public static void submitTask(UpdateImageFormatTask updateImageFormatTask) {
        executorService.execute(updateImageFormatTask);
    }

}
