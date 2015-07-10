/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author admkrushnakant
 */
public class MountVMImage {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MountVMImage.class);
    private static final String mountScript = "/opt/director/bin/mount_vm_image.sh";
    private static final String mountRemoteFileSystemScript="/opt/director/bin/mount_remote_system.sh";
    
    public static int mountImage(String imagePath, String mountpath, String mountType)  throws Exception{
        
        String command = mountScript + " " + imagePath+" "+mountpath+" "+mountType;
        log.debug("\n" + "Mounting the vm image : " + imagePath );
        log.trace("Command:" + command);
        int exitCode = callExec(command);
        return exitCode;
    }
    
    public static int unmountImage(String mountPath) throws Exception {
        
        log.debug("Unmounting the vm image with mount path : " + mountPath);
        int exitCode = callExec(mountScript+" "+mountPath);
        if(exitCode != 0)
            throw new UnmountException(Integer.toString(exitCode));
        return exitCode;
    }
    
    public static int mountRemoteSystem(String ipAddress, String userName, String password, String mountpath) throws Exception
    {
        String command = mountRemoteFileSystemScript + " " + ipAddress + " " + userName + " " + password + " "+mountpath;
        log.info("\n" + "Mounting the The remote System : " + ipAddress);
        
        int exitCode = callExec(command);
        return exitCode;
        
    }
    
    public static int unmountRemoteSystem(String mountPath) throws Exception
    {
        log.debug("Unmounting the Remote File System in mount path : " + mountPath);
        int exitCode = callExec(mountRemoteFileSystemScript+" "+mountPath);
        return exitCode;
        
    }
    
    public static int callExec(String command) throws IOException, InterruptedException {
        log.debug("Command to execute is: "+command);
        StringBuilder output = new StringBuilder();
        Process p;
        p = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        int exitCode = p.waitFor();
        log.debug(output.toString());
        log.trace("Exec command output : " + output.toString());
        return exitCode;
        
    }
    
}
