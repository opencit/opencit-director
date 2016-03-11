package com.intel.director.common;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerUtil {

	private static final Logger log = LoggerFactory.getLogger(DockerUtil.class);

	public static int dockerSave(String repository, String tag, String tarDestination, String name) {
		File dirForDockerTar = new File(tarDestination);
		if (!dirForDockerTar.exists()) {
			dirForDockerTar.mkdir();
		}
		return executeDockerCommands("save", "-o", tarDestination + File.separator + name, repository + ":" + tag);
	}

	public static int dockerPull(String repository, String tag) {
		return executeDockerCommands("pull", repository + ":" + tag);

	}

	public static int dockerRMI(String repository, String tag) {
		return executeDockerCommands("rmi", "-f", repository + ":" + tag);

	}

	public static int dockerTag(String repository, String tag, String newRepo, String newTag) {
		return executeDockerCommands("tag", repository + ":" + tag, newRepo + ":" + newTag);

	}

	public static int dockerLoad(String source_image) {
		return executeDockerCommands("load", "--input=" + source_image);

	}

	public static int executeDockerCommands(String... args) {
		int exitcode = 0;
		if (args.length == 0) {
			log.error("docker command needs at least one argument");
			return 1;
		}

		log.info("\n Inside executeDockerCommands going to run docker " + args[0]);

		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(Constants.DOCKER_EXECUTABLES, args);
		} catch (IOException e) {
			log.error("Error in executeDockerCommands docker " + args[0], e);
			if (exitcode == 0) {
				exitcode = 1;
			}
		}
		return exitcode;

	}
}