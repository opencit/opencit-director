package com.intel.director.common;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.director.common.exception.ConnectionFailException;
import com.intel.director.common.exception.DirectorException;
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
		//Find by API: https://registry.hub.docker.com/v2/repositories/library/redis/tags/
		String url = "https://registry.hub.docker.com/v2/repositories/library/" + repo + "/tags/" ;
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet(url);

		try {
			HttpResponse execute = httpClient.execute(get);
			log.info("URL for searhcing repo tag in docker hub using v2 API: {}", url);
			log.info("Response code: {}", execute.getStatusLine().getStatusCode());
			if(execute.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				ObjectMapper objectMapper = new ObjectMapper();
				ResponseHandler<String> handler = new BasicResponseHandler();
				String handleResponse = handler.handleResponse(execute);
				RepoSearchResult readValue = objectMapper.readValue(handleResponse, RepoSearchResult.class);
				log.info("tags returned: {}", readValue.getResults().size());
				List<TagDetail> results = readValue.getResults();
				for (TagDetail tagDetail : results) {
					if(tag.equals(tagDetail.getName())){
						repoTagExists = true;
						log.info("Found the image with repo : {} and tag : {}", repo, tag);
						break;
					}
				}
			}

		} catch (ClientProtocolException e) {
			log.error("Error in using http client to find repo tag existence in Docker hub: {}", url, e);
		} catch (IOException e) {
			log.error("Error in using http client to find repo tag existence in Docker hub: {}", url, e);
		}
		return repoTagExists;

	}
	
}

@JsonIgnoreProperties(ignoreUnknown = true)
class RepoSearchResult{
	List<TagDetail> results;

	public List<TagDetail> getResults() {
		return results;
	}

	public void setResults(List<TagDetail> results) {
		this.results = results;
	}
	
}

@JsonIgnoreProperties(ignoreUnknown = true)
class TagDetail{
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}