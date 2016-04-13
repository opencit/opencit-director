package com.intel.director.api;

public class DockerRequestObject extends GenericRequest {

	public String repository;
	public String tag;

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
