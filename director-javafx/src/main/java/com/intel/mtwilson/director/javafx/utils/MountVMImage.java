/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.intel.mtwilson.director.javafx.ui.Constants;

/**
 *
 * @author admkrushnakant
 */
public class MountVMImage {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MountVMImage.class);
    private static final String mountScript = "/opt/director/bin/mount_vm_image.sh";
    private static final String mountRemoteFileSystemScript="/opt/director/bin/mount_remote_system.sh";
    
    public static int mountImage(String imagePath) {
        
        String command = mountScript + " " + imagePath;
        log.debug("\n" + "Mounting the vm image : " + imagePath);
        log.trace("Command:" + command);
        int exitCode = callExec(command);
        log.trace("\n Exit code is : " + exitCode);
        return exitCode;
    }
    
    public static int unmountImage(String mountPath) {
        
        log.debug("Unmounting the vm image with mount path : " + mountPath);
        int exitCode = callExec(mountScript);
        log.trace("\n Exit code is : " + exitCode);
        return exitCode;
    }
    
    public static int mountRemoteSystem(String ipAddress, String userName, String password)
    {
        String command = mountRemoteFileSystemScript + " " + ipAddress + " " + userName + " " + password;
        log.info("\n" + "Mounting the The remote System : " + ipAddress);
        
        int exitCode = callExec(command);
        log.trace("\n Exit code is : " + exitCode);
        return exitCode;
        
    }
    
    public static int unmountRemoteSystem(String mountPath)
    {
        log.debug("Unmounting the Remote File System in mount path : " + mountPath);
        int exitCode = callExec(mountRemoteFileSystemScript);
        log.debug("\n Exit code is : " + exitCode);
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
            log.error(null, ex);
        }
        log.debug(output.toString());
        log.trace("Exec command output : " + output.toString());
        return exitCode;
        
    }
    
}
