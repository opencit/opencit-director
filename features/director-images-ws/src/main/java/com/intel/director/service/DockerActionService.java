package com.intel.director.service;

import com.intel.director.images.exception.DirectorException;

public interface DockerActionService {

	public void dockerSave(String image_id) throws DirectorException;

	public void dockerRMI(String image_id) throws DirectorException;

	public void dockerLoad(String image_id) throws DirectorException;

	public void dockerTag(String image_id, String newRepository, String newTag) throws DirectorException;

	public boolean doesRepoTagExist(String repository, String tag, String currentImageId) throws DirectorException;

	public void dockerPull(String repository, String tag) throws DirectorException;

}
