/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common;

import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MountVMImage {

    private static final Logger log = LoggerFactory.getLogger(MountVMImage.class);

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
        String command = Constants.mountRemoteFileSystemScript + " " + ipAddress + " " + userName + " " + password + " " + mountpath;
        log.info("\n" + "Mounting the The remote System : " + ipAddress);
        return executeCommandInExecUtil(Constants.mountRemoteFileSystemScript, ipAddress, userName, password, mountpath);
    }

    public static int unmountRemoteSystem(String mountPath) throws Exception {
        log.debug("Unmounting the Remote File System in mount path : " + mountPath);
        return executeCommandInExecUtil(Constants.mountRemoteFileSystemScript, mountPath);
    }

    private static int executeCommandInExecUtil(String command, String... args) throws IOException {
        Result result = ExecUtil.execute(Constants.mountScript, args);
        return result.getExitCode();
    }
}
