/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

public class ImageUtil {

    private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);

    public static int mountImage(String imagePath, String mountpath) throws Exception {
        String command = Constants.mountScript + " " + imagePath + " " + mountpath;
        log.debug("\n" + "Mounting the vm image : " + imagePath);
        log.trace("Command:" + command);
        return executeCommandInExecUtil(Constants.mountScript, imagePath, mountpath);
    }

    public static int unmountImage(String mountPath) throws Exception {
        String command = Constants.mountScript + " " + mountPath;
        log.debug("Unmounting the vm image with mount path : " + mountPath);
        log.debug("\n" + "unmounting the vm image : " + mountPath);
        log.trace("Command:" + command);
        return executeCommandInExecUtil(Constants.mountScript, mountPath);
    }

    public static int mountRemoteSystem(String ipAddress, String userName, String password, String mountpath) throws Exception {
        log.info("\n" + "Mounting the The remote System : " + ipAddress);
        return executeCommandInExecUtil(Constants.mountRemoteFileSystemScript, ipAddress, userName, password, mountpath);
    }

    public static int unmountRemoteSystem(String mountPath) throws Exception {
        log.debug("Unmounting the Remote File System in mount path : " + mountPath);
        return executeCommandInExecUtil(Constants.mountRemoteFileSystemScript, mountPath);
    }

    public static String encryptFile(String location, String password) throws Exception {
        String command = "openssl enc -aes-128-ofb -in " + location + " -out " + location + "-enc" + " -pass pass:" + password;
        int exitCode = executeCommandInExecUtil(command);
        if (exitCode != 0) {
            log.error("Error while encrypting the file .....");
            throw new Exception("Can not encrypt image");
        }
        return location + "-enc";
    }
    
    public static int createTar(String imageDir, String imageName, String trustPolicyName, String tarLocation, String tarName) throws IOException {
        String imagePathDelimiter = "/";
    
    	String command = "tar -cf " + tarLocation + imagePathDelimiter + tarName  + " -C " + imageDir + " " + imageName + " " + trustPolicyName;
    	 ///  String tarName = imageName + "-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".tar";
         ///  log.debug(genrateTP.executeShellCommand("tar -cf " + imageTPDir + imagePathDelimiter + tarName + " -C " + imageTPDir + " " + imageName + " " + trustpolicyName));
    	
        return executeCommandInExecUtil(command);
    }


    private static int executeCommandInExecUtil(String command, String... args) throws IOException {
        Result result = ExecUtil.execute(Constants.mountScript, args);
        return result.getExitCode();
    }
    
    public static void main(String[] args) throws IOException{
    	createTar("/temp/","abc.txt","xyz.txt","/temp/","mytar.tar");
    }
}
