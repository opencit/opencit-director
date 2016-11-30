package com.intel.director.common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.director.common.exception.ConnectionFailException;
import com.intel.director.common.exception.DirectorException;
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
	// / return 0 when able to connect hub and search even though serch
	// / result is empty
	result = executeDockerCommands("search", "busybox");
	if (result == 0) {
	    return true;
	}
	return false;
    }

    public static boolean doesRepoTagExistInDockerHub(String repo, String tag)
	    throws DirectorException, ConnectionFailException {

	boolean repoTagExists = false;
	log.info("Checking using docker search command");

	// In cases lie ngnix
	String command = "docker search " + repo;
	Result result = null;
	try {
	    result = ExecUtil.executeQuoted("/bin/sh", "-c", command);
	} catch (ExecuteException e) {
	    log.error("Error in executing command:" + command);
	    throw new DirectorException("Error in executing command:" + command, e);
	} catch (IOException e) {
	    log.error("Error in executing command:" + command);
	    throw new DirectorException("Error in executing command:" + command, e);
	}
	log.info("doesRepoTagExistInDockerHub : Command executed for checking existence of repo and tab in hub : {}",
		command);
	int exitCode = -1;
	exitCode = result.getExitCode();
	if (exitCode != 0) {
	    throw new ConnectionFailException("Unable to connect to docker hub");
	}
	if (result.getStderr() != null && StringUtils.isNotEmpty(result.getStderr())) {
	    log.error(result.getStderr());
	    return false;
	}

	String searchResult = result.getStdout();
	log.info("searchResult = {}", searchResult);
	String nl = System.getProperty("line.separator");
	String[] split = searchResult.split(nl);
	for (String string : split) {
	    if (StringUtils.isNotBlank(string) && string.contains("NAME") && string.contains("DESCRIPTION")
		    && string.contains("STARS")) {
		log.info("Found the header: {}", string);
		continue;
	    }
	    log.info("Found the content: {}", string);
	    if ("latest".equalsIgnoreCase(tag)) {
		String[] split2 = string.split(" ");		
		if (split2.length > 0 && split2[0].equalsIgnoreCase(repo)) {
		    repoTagExists = true;
		}
		break;
	    } else {
		repoTagExists = string.contains(tag + "/");
		if (repoTagExists) {
		    break;
		}
	    }
	}

	if (repoTagExists) {
	    return repoTagExists;
	}
	log.info("Checking with REST endpoint for repo: {} and tag: {}", repo, tag);

	/*
	 * 
	 * command =
	 * "curl -i -X GET https://registry.hub.docker.com/v1/repositories/" +
	 * repo + "/tags/" + tag; log.info(
	 * "doesRepoTagExistInDockerHub, running command::" + command); try {
	 * result = DirectorUtil.executeCommand("curl", "-i", "-X", "GET",
	 * "https://registry.hub.docker.com/v1/repositories/" + repo + "/tags/"
	 * + tag); } catch (ExecuteException e) { log.error(
	 * "Error in executing command:" + command); throw new
	 * DirectorException("Error in executing command:" + command, e); }
	 * catch (IOException e) { log.error("Error in executing command:" +
	 * command); throw new DirectorException("Error in executing command:" +
	 * command, e); } String resultOutput = result.getStdout();
	 * log.info("result:" + result + " console output::" + resultOutput); if
	 * (StringUtils.isNotBlank(resultOutput)) { if (resultOutput.contains(
	 * "200 OK")) { repoTagExists = true; } }
	 */

	String url = "https://registry.hub.docker.com/v1/repositories/" + repo + "/tags/" + tag;
	HttpClient httpClient = HttpClientBuilder.create().build();
	HttpGet get = new HttpGet(url);

	try {
	    HttpResponse execute = httpClient.execute(get);
	    log.info("URL for searhcing repo tag in docker hub: {}", url);
	    log.info("Response code: {}", execute.getStatusLine().getStatusCode());
	    repoTagExists = (execute.getStatusLine().getStatusCode() == HttpStatus.SC_OK);

	} catch (ClientProtocolException e) {
	    log.error("Error in using http client to find repo tag existence in Docker hub: {}", url, e);
	} catch (IOException e) {
	    log.error("Error in using http client to find repo tag existence in Docker hub: {}", url, e);
	}
	return repoTagExists;

    }
}