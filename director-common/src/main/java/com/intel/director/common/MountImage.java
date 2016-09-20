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

	private static final String SPACE = " ";
	private static final Logger log = LoggerFactory.getLogger(MountImage.class);

	public static int mountImage(String imagePath, String mountpath) {
		int exitcode; 
		String command = Constants.mountScript + SPACE + imagePath + SPACE
				+ mountpath;
		log.debug("\n" + "Mounting the vm image : " + imagePath);
		log.trace("Command:" + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(Constants.mountScript,
					imagePath, mountpath);
		} catch (IOException e) {
			exitcode = 1;
			log.error("Error in unmounting image" + e);
		}
		return exitcode;
	}

	public static int unmountImage(String mountPath) {
		int exitcode; 
		String command = Constants.mountScript + SPACE + mountPath;
		log.debug("Unmounting the vm image with mount path : " + mountPath);
		log.debug("\n" + "unmounting the vm image : " + mountPath);
		log.trace("Command:" + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(Constants.mountScript,
					mountPath);
		} catch (IOException e) {
			exitcode = 1;
			log.error("Error in unmounting image" + e);
		}
		return exitcode;
	}

	public static int mountRemoteSystem(String ipAddress, String userName,
			String password, String mountpath) {
		int exitcode; 
		String command = Constants.mountRemoteFileSystemScript + SPACE
				+ ipAddress + SPACE + userName + SPACE + password + SPACE + mountpath;
		log.debug("Executing command " + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(
					Constants.mountRemoteFileSystemScript, ipAddress, userName,
					password, mountpath);
		} catch (IOException e) {
			exitcode = 1;
			log.error("Error in mounting remote host" + e);
		}
		return exitcode;
	}

	public static int unmountRemoteSystem(String mountPath) {
		int exitcode; 

		log.info("Unmounting the Remote File System in mount path : "
				+ mountPath);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(
					Constants.mountRemoteFileSystemScript, mountPath);
		} catch (IOException e) {
			exitcode = 1;
			log.error("Error in unmounting remote host" + e);
		}
		return exitcode;
	}
	
	
	public static int mountDocker(String mountpath,String repository,String tag) {
		int exitcode = 1; 
		String command = Constants.mountDockerScript + SPACE
				+ "mount" + SPACE + mountpath + SPACE + repository + SPACE + tag;
		log.info("\n" + "Mounting the docker image : " + mountpath
				+ "with command: " + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(
					Constants.mountDockerScript, "mount", mountpath,
					repository, tag);
		} catch (IOException e) {
			if (exitcode == 0) {
				exitcode = 1;
			}
			log.error("Error in mounting docker image" + e);
		}
		return exitcode;
	}
	
	public static int unmountDocker(String mountpath, String repository, String tag) {
		int exitcode;
		String command = Constants.mountDockerScript + SPACE + "unmount" + SPACE + mountpath + SPACE + repository
				+ SPACE + tag;
		log.info("\n" + "Unmounting the docker image : " + mountpath + "with command: " + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(Constants.mountDockerScript, "unmount", mountpath,
					repository, tag);
		} catch (IOException e) {
			exitcode = 1;
			log.error("Error in mounting docker image" + e);
		}
		return exitcode;
	}	
	
	public static int mountWindowsRemoteSystem(String ipAddress, String userName, String password, String mountpath,
			String partition, String fileMode, String DirMode) {
		int exitcode;
		String command = Constants.mountWindowsRemoteFileSystemScript + SPACE + ipAddress + SPACE + partition + SPACE
				+ mountpath + SPACE + userName + SPACE + fileMode + SPACE + DirMode;

		String domain = "";

		if (userName.contains("@")) {
			String[] splitUserName = userName.split("@");
			userName = splitUserName[0];
			domain = splitUserName[1];
		}

		if (partition.contains(":")) {
			partition = partition.replace(":", "");
		}

		log.info("MOunting To Windows Remote Host Using :: " + command);
		try {
			DirectorUtil.callExec("mkdir -p " + mountpath);
		} catch (IOException e) {
			String msg = "Error creating mount directory " + mountpath;
			log.error(msg);
		}
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(Constants.mountWindowsRemoteFileSystemScript, ipAddress,
					partition, mountpath, userName, password, fileMode, DirMode, domain);
		} catch (Exception e) {
			exitcode = 1;
			log.error("Error in mounting remote host" + e);
		}
		return exitcode;
	}
	
	public static int unmountWindowsRemoteSystem(String mountPath) {
		int exitcode;
		log.debug("Unmounting the Remote File System in mount path : " + mountPath);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil("umount", mountPath);
		} catch (IOException e) {
			exitcode = 1;
			log.error("Error in unmounting remote host" + e);
		}
		return exitcode;
	}

}
