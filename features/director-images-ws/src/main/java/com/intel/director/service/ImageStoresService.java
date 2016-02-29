package com.intel.director.service;

import java.util.List;

import com.intel.director.api.GenericDeleteResponse;
import com.intel.director.api.ImageStoreFilter;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.images.exception.DirectorException;

public interface ImageStoresService {
	public ImageStoreTransferObject createImageStore(
			ImageStoreTransferObject imageStoreTransferObject) throws DirectorException;

	public ImageStoreTransferObject getImageStoreById(String imageStoreId) throws DirectorException;

	public List<ImageStoreTransferObject> getImageStores(
			ImageStoreFilter imageStoreFilter) throws DirectorException;

	public GenericDeleteResponse deleteImageStore(String imageStoreId)
			throws DirectorException;

	public ImageStoreTransferObject updateImageStore(
			ImageStoreTransferObject imageStoreTransferObject)
			throws DirectorException;

	public boolean doesImageStoreNameExist(String name, String imageStoreId) throws DirectorException;

	public boolean validateConnectorArtifacts(String[] artifact_types,
			String connector);
}
