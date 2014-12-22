/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trust.director.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.intel.mtwilson.trust.director.ui.Constants;
import com.intel.mtwilson.trust.director.ui.UserConfirmation;

/**
 *
 * @author admkrushnakant
 */
public class MountVMImage {
    private static final Logger logger = Logger.getLogger(MountVMImage.class.getName());
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }
    private static final String mountScript = "./resources/mount_vm_image.sh";
    
    public static int mountImage(String imagePath) {
        
        String command = mountScript + " " + imagePath;
        logger.info("\n" + "Mounting the vm image : " + imagePath);
        
        int exitCode = callExec(command);
        logger.info("\n Exit code is : " + exitCode);
        return exitCode;
    }
    
    public static int unmountImage(String mountPath) {
        
        logger.info("Unmounting the vm image with mount path : " + mountPath);
        int exitCode = callExec(mountScript);
        logger.info("\n Exit code is : " + exitCode);
        return exitCode;
    }
    
    public static int callExec(String command) {
        
        StringBuilder output = new StringBuilder();
        int exitCode = 12345;
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            exitCode = p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (InterruptedException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        //System.out.println(output.toString());
        logger.info(output.toString());
        new FileUtilityOperation().writeToFile(new File(Constants.EXEC_OUTPUT_FILE), output.toString(), false);
        logger.info("Exec command output : " + output.toString());
        return exitCode;
        
    }
    
}
