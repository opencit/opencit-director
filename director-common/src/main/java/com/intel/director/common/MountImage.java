/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountImage {

	private static final String SPACE = " ";
	private static final Logger log = LoggerFactory.getLogger(MountImage.class);

	public static int mountImage(String imagePath, String mountpath) {
		int exitcode = 0; 
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
		int exitcode = 0; 
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
		int exitcode = 0; 
		String command = Constants.mountRemoteFileSystemScript + SPACE
				+ ipAddress + SPACE + userName + SPACE + password + SPACE + mountpath;
		log.info("\n" + "Mounting the The remote System : " + ipAddress
				+ "with command: " + command);
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
		int exitcode = 0; 

		log.debug("Unmounting the Remote File System in mount path : "
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
	
	public static int dockerLoad(String source_image) {
		int exitcode = 0;
		String command = Constants.DOCKER_EXECUTABLES + SPACE + "load" + " --input=" + source_image;
		log.info("\n" + "Loading docker image to System : " + source_image
				+ "with command: " + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(Constants.DOCKER_EXECUTABLES, "load", "--input=" + source_image);
		} catch (IOException e) {
			log.error("Error in loading docker image" + e);
			if(exitcode == 0){
				exitcode = 1;
			}
		}
		return exitcode;
	}
	
	public static int mountDocker(String mountpath,String repository,String tag) {
		int exitcode = 0; 
		String command = Constants.mountDockerScript + SPACE
				+ "mount" + SPACE + mountpath + SPACE + repository + SPACE + tag;
		log.info("\n" + "Mounting the docker image : " + mountpath
				+ "with command: " + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(
					Constants.mountDockerScript, "mount", mountpath,
					repository, tag);
		} catch (IOException e) {
			if(exitcode == 0){
				exitcode = 1;
			}

			log.error("Error in mounting docker image" + e);
		}
		return exitcode;
	}
	
	public static int unmountDocker(String mountpath,String repository,String tag) {
		int exitcode = 0; 
		String command = Constants.mountDockerScript + SPACE
				+ "unmount" + SPACE + mountpath + SPACE + repository + SPACE + tag;
		log.info("\n" + "Unmounting the docker image : " + mountpath
				+ "with command: " + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(
					Constants.mountDockerScript, "unmount", mountpath,
					repository, tag);
		} catch (IOException e) {
			if(exitcode == 0){
				exitcode = 1;
			}
			log.error("Error in mounting docker image" + e);
		}
		return exitcode;
	}
	
	public static int dockerSave(String repository,String tag,String tarDestination,String name) {
		int exitcode = 0; 
		String command = Constants.DOCKER_EXECUTABLES + SPACE
				 + "save" + SPACE + "-o" + SPACE + tarDestination + File.separator + name + SPACE + repository + ":" + tag;
		log.info("\n" + "creating tar using docker save at : " + tarDestination
				+ " with command: " + command);
		try {
			
			File dirForDockerTar = new File(tarDestination);
			if (!dirForDockerTar.exists()) {
				dirForDockerTar.mkdir();
			}
			log.info("\n" + "Running docker save");
			exitcode = DirectorUtil.executeCommandInExecUtil(
					Constants.DOCKER_EXECUTABLES, "save", "-o", tarDestination + File.separator + name, repository + ":" + tag);
		} catch (IOException e) {
			if(exitcode == 0){
				exitcode = 1;
			}
			log.error("Error in creating  docker tar" + e);
		}
		return exitcode;
	}
	
	public static int dockerRMI(String repository, String tag) {
		int exitcode = 0;
		String command = Constants.DOCKER_EXECUTABLES + SPACE + "rmi" + SPACE + "-f" + SPACE + repository + ":" + tag;
		log.info("\n" + "removing docker image from docker repo : "
				+ "with command: " + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(Constants.DOCKER_EXECUTABLES, "rmi", "-f", repository+ ":" + tag);
		} catch (IOException e) {
			log.error("Error in removing docker image" + e);
			if(exitcode == 0){
				exitcode = 1;
			}
		}
		return exitcode;
	}
	
	public static int dockerTag(String repository, String tag, String newTag) {
		int exitcode = 0;
		String command = Constants.DOCKER_EXECUTABLES + SPACE + "tag" + SPACE + repository + ":" + tag + SPACE + repository + ":" + newTag;
		log.info("\n" + "tagging docker image  : "
				+ "with command: " + command);
		try {
			exitcode = DirectorUtil
					.executeCommandInExecUtil(Constants.DOCKER_EXECUTABLES,
							"tag", repository + ":" + tag, repository + ":" + newTag);
		} catch (IOException e) {
			log.error("Error in tagging docker image" + e);
			if(exitcode == 0){
				exitcode = 1;
			}
		}
		return exitcode;
	}

}
