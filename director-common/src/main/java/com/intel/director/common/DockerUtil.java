package com.intel.director.common;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.director.constants.Constants;

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
	
	
	public static boolean doesRepoTagExistInDockerHub(String repo, String tag){
		
		if(StringUtils.isBlank(repo) || StringUtils.isBlank(tag)){
			return false;
		}
		String url = "https://registry.hub.docker.com/v1/repositories/" + repo
				+ "/tags/" + tag;
		
		URL searchRepoTag = null;
		
		try {
			searchRepoTag = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Client client = ClientBuilder.newBuilder().build();
		WebTarget target = client.target(searchRepoTag.toExternalForm());
		Response response = target.request().get();
		
		
		if (response.getStatus() == 200) {
			return true;
		}
		
		return false;
		
	}
}