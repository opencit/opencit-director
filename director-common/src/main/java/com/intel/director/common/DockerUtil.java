package com.intel.director.common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.director.constants.Constants;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

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
		int exitcode;
		if (args.length == 0) {
			log.error("docker command needs at least one argument");
			return 1;
		}

		log.info("\n Inside executeDockerCommands going to run docker " + args[0]);

		try {
			exitcode = DirectorUtil.executeCommandInExecUtil(Constants.DOCKER_EXECUTABLES, args);
		} catch (IOException e) {
			log.error("Error in executeDockerCommands docker " + args[0], e);
			exitcode = 1;
		}
		return exitcode;

	}

	public static boolean checkDockerHubConnection() throws ClientProtocolException, IOException {
		int result = -1;
		/// return 0 when able to connect hub and search even though serch
		/// result is empty
		result = executeDockerCommands("search", "busybox");
		if (result == 0) {
			return true;
		}
		return false;
	}

	public static boolean doesRepoTagExistInDockerHub(String repo, String tag)
			throws ClientProtocolException, IOException {
		String command = "docker search " + repo + ":" + tag;
		Result result = ExecUtil.executeQuoted("/bin/sh", "-c", command);
		log.info("doesRepoTagExistInDockerHub : Command executed for checking existence of repo and tab in hub : {}", command);
		if (result.getStderr() != null && StringUtils.isNotEmpty(result.getStderr())) {
			log.error(result.getStderr());
			return false;
		}
		
		String searchResult = result.getStdout();
		log.info("searchResult = {}", searchResult);
		String nl = System.getProperty("line.separator");
		String[] split = searchResult.split(nl);
		boolean repoTagExists = false;
		for (String string : split) {
			if (StringUtils.isNotBlank(string) && string.contains("NAME") && string.contains("DESCRIPTION") && string.contains("STARS") ) {
				log.info("Found the header: {}", string);
				continue;
			}
			log.info("Found the content: {}", string);
			repoTagExists = true;
			break;
		}

		return repoTagExists;

	}
}