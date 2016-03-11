package com.intel.director.common;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerUtil {

	private static final String SPACE = " ";
	private static final Logger log = LoggerFactory.getLogger(DockerUtil.class);
	
	
	public static int dockerSave(String repository, String tag,
			String tarDestination, String name) {
		int exitcode = 0;
		String command = Constants.DOCKER_EXECUTABLES + SPACE + "save" + SPACE
				+ "-o" + SPACE + tarDestination + File.separator + name + SPACE
				+ repository + ":" + tag;
		log.info("\n" + "creating tar using docker save at : " + tarDestination
				+ " with command: " + command);
		try {

			File dirForDockerTar = new File(tarDestination);
			if (!dirForDockerTar.exists()) {
				dirForDockerTar.mkdir();
			}
			log.info("\n" + "Running docker save");
			exitcode = DirectorUtil.executeCommandInExecUtil(
					Constants.DOCKER_EXECUTABLES, "save", "-o", tarDestination
							+ File.separator + name, repository + ":" + tag);
		} catch (IOException e) {
			if (exitcode == 0) {
				exitcode = 1;
			}
			log.error("Error in creating  docker tar" + e);
		}
		return exitcode;
	}
	
	
	public static int dockerPull(String repository, String tag) {
		int exitcode = -1;
		String command = Constants.DOCKER_EXECUTABLES + SPACE + "pull" + SPACE
				+ repository + ":" + tag;
		log.info("\n" + "running dockerPush" 
				+ " with command: " + command);
		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(Constants.DOCKER_EXECUTABLES , "pull" ,repository+ ":" + tag);
		} catch (IOException e) {
			log.error("Error in pulling docker image" + e);
			
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
	
	public static int dockerTag(String repository, String tag, String newRepo, String newTag) {
		int exitcode = 0;
		String command = Constants.DOCKER_EXECUTABLES + SPACE + "tag" + SPACE + repository + ":" + tag + SPACE + repository + ":" + newTag;
		log.info("\n" + "tagging docker image  : "
				+ "with command: " + command);
		try {
			exitcode = DirectorUtil
					.executeCommandInExecUtil(Constants.DOCKER_EXECUTABLES,
							"tag", repository + ":" + tag, newRepo + ":" + newTag);
		} catch (IOException e) {
			log.error("Error in tagging docker image" + e);
			if(exitcode == 0){
				exitcode = 1;
			}
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
}
