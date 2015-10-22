/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountImage {

	private static final Logger log = LoggerFactory.getLogger(MountImage.class);

	public static int mountImage(String imagePath, String mountpath)
			throws Exception {
		String command = Constants.mountScript + " " + imagePath + " "
				+ mountpath;
		log.debug("\n" + "Mounting the vm image : " + imagePath);
		log.trace("Command:" + command);
		return DirectorUtil.executeCommandInExecUtil(Constants.mountScript, imagePath,
				mountpath);
	}

	public static int unmountImage(String mountPath) throws Exception {
		String command = Constants.mountScript + " " + mountPath;
		log.debug("Unmounting the vm image with mount path : " + mountPath);
		log.debug("\n" + "unmounting the vm image : " + mountPath);
		log.trace("Command:" + command);
		return DirectorUtil.executeCommandInExecUtil(Constants.mountScript, mountPath);
	}

	public static int mountRemoteSystem(String ipAddress, String userName,
			String password, String mountpath) throws Exception {
		String command = Constants.mountRemoteFileSystemScript + " "
				+ ipAddress + " " + userName + " " + password + " " + mountpath;
		log.info("\n" + "Mounting the The remote System : " + ipAddress
				+ "with command: " + command);
		return DirectorUtil.executeCommandInExecUtil(Constants.mountRemoteFileSystemScript,
				ipAddress, userName, password, mountpath);
	}

	public static int unmountRemoteSystem(String mountPath) throws Exception {
		log.debug("Unmounting the Remote File System in mount path : "
				+ mountPath);
		return DirectorUtil.executeCommandInExecUtil(Constants.mountRemoteFileSystemScript,
				mountPath);
	}

	
}
