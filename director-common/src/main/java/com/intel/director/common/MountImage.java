/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountImage {

	private static final Logger log = LoggerFactory.getLogger(MountImage.class);

	public static int mountImage(String imagePath, String mountpath) {
		String command = Constants.mountScript + " " + imagePath + " "
				+ mountpath;
		log.debug("\n" + "Mounting the vm image : " + imagePath);
		log.trace("Command:" + command);
		try {
			return DirectorUtil.executeCommandInExecUtil(Constants.mountScript,
					imagePath, mountpath);
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error in unmounting image" + e);
		}
		return 0;
	}

	public static int unmountImage(String mountPath) {
		String command = Constants.mountScript + " " + mountPath;
		log.debug("Unmounting the vm image with mount path : " + mountPath);
		log.debug("\n" + "unmounting the vm image : " + mountPath);
		log.trace("Command:" + command);
		try {
			return DirectorUtil.executeCommandInExecUtil(Constants.mountScript,
					mountPath);
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error in unmounting image" + e);
		}
		return 0;
	}

	public static int mountRemoteSystem(String ipAddress, String userName,
			String password, String mountpath) {
		int exitcode = 0; 
		String command = Constants.mountRemoteFileSystemScript + " "
				+ ipAddress + " " + userName + " " + password + " " + mountpath;
		log.info("\n" + "Mounting the The remote System : " + ipAddress
				+ "with command: " + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(
					Constants.mountRemoteFileSystemScript, ipAddress, userName,
					password, mountpath);
		} catch (IOException e) {
			if(exitcode == 0){
				exitcode = 1;
			}

			log.error("Error in mounting remote host" + e);
		}
		log.error("Error in mounting remote host");
		return exitcode;
	}

	public static int unmountRemoteSystem(String mountPath) {
		log.debug("Unmounting the Remote File System in mount path : "
				+ mountPath);
		try {
			return DirectorUtil.executeCommandInExecUtil(
					Constants.mountRemoteFileSystemScript, mountPath);
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error in unmounting remote host" + e);
		}
		return 0;
	}

}
