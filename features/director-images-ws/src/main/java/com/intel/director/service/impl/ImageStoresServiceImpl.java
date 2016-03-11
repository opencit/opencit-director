package com.intel.director.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.intel.director.api.ConnectorProperties;
import com.intel.director.api.GenericDeleteResponse;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.ImageStoreDetailsTransferObject;
import com.intel.director.api.ImageStoreFilter;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageStoresService;
import com.intel.director.store.StoreManager;
import com.intel.director.store.StoreManagerFactory;
import com.intel.director.store.exception.StoreException;
import com.intel.director.store.util.ImageStorePasswordUtil;
import com.intel.director.util.I18Util;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class ImageStoresServiceImpl implements ImageStoresService {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageStoresServiceImpl.class);
	
	private IPersistService imagePersistenceManager;

	public ImageStoresServiceImpl() {
		imagePersistenceManager = new DbServiceImpl();
	}
	
	private String[] propertiesForConnector(String connector) {
		switch (connector) {
		case Constants.CONNECTOR_DOCKERHUB:
			String returnString[] = ConnectorProperties.DOCKER.getProperties();
			return returnString;
		case Constants.CONNECTOR_GLANCE:
			String returnString1[] = ConnectorProperties.GLANCE.getProperties();
			return returnString1;
		case Constants.CONNECTOR_SWIFT:
			String returnString2[] = ConnectorProperties.SWIFT.getProperties();
			return returnString2;
		}	
		return null;
	}
	
	@Override
	public ImageStoreTransferObject createImageStore(ImageStoreTransferObject imageStoreTransferObject) throws DirectorException {
		ImageStoreTransferObject savedImageStore = null;
		Collection<ImageStoreDetailsTransferObject> image_store_details_final = new ArrayList<ImageStoreDetailsTransferObject>();

		String[] propertiesForConnector = propertiesForConnector(imageStoreTransferObject.getConnector());
		List<String> listOfPropertiesForConnector = new ArrayList<String>(Arrays.asList(propertiesForConnector));
		if(imageStoreTransferObject.getImage_store_details() != null && imageStoreTransferObject.getImage_store_details().size() != 0) {
			Collection<ImageStoreDetailsTransferObject> image_store_details = imageStoreTransferObject.getImage_store_details();
			for (ImageStoreDetailsTransferObject imageStoreDetailsTransferObject : image_store_details) {
				String key = imageStoreDetailsTransferObject.getKey();
				if(listOfPropertiesForConnector.contains(key)){
					listOfPropertiesForConnector.remove(key);
				}
				image_store_details_final.add(imageStoreDetailsTransferObject);
			}
		}
		for (String props : listOfPropertiesForConnector) {
			ImageStoreDetailsTransferObject storeDetailsTransferObject = new ImageStoreDetailsTransferObject();
			storeDetailsTransferObject.setKey(props);
			storeDetailsTransferObject.setValue(null);
			image_store_details_final.add(storeDetailsTransferObject);
		}
		
		imageStoreTransferObject.setImage_store_details(image_store_details_final);
		
		try {
			savedImageStore = imagePersistenceManager.saveImageStore(imageStoreTransferObject);
		} catch (DbException e) {
			log.error("Error in creating ImageStore",e);
			throw new DirectorException("Error in creating ImageStore",e);
		}	
		return savedImageStore;
	}

	@Override
	public ImageStoreTransferObject getImageStoreById(String imageStoreId)
			throws DirectorException {

		ImageStoreTransferObject fetchImageStorebyId = null;
		try {
			fetchImageStorebyId = imagePersistenceManager
					.fetchImageStorebyId(imageStoreId);
			if(fetchImageStorebyId == null){
				return null;
			}
			ImageStorePasswordUtil imageStorePasswordUtil = new ImageStorePasswordUtil();
			ImageStoreDetailsTransferObject passwordConfiguration = imageStorePasswordUtil.getPasswordConfiguration(fetchImageStorebyId);
			if(StringUtils.isNotBlank(passwordConfiguration.getValue())){
				String passwordForImageStore = imageStorePasswordUtil.decryptPasswordForImageStore(passwordConfiguration.getValue());			
				passwordConfiguration.setValue(passwordForImageStore);
			}
			for(ImageStoreDetailsTransferObject detailsTransferObject : fetchImageStorebyId.image_store_details){
				detailsTransferObject.setKeyDisplayValue(I18Util.format(detailsTransferObject.getKey()));
			}
		} catch (DbException e) {
			log.error("Error in fetching ImageStore :: " + imageStoreId);
			throw new DirectorException("Error in fetching ImageStore :: "
					+ imageStoreId, e);
		}
		return fetchImageStorebyId;
	}
	
	@Override
	public List<ImageStoreTransferObject> getImageStores(
			ImageStoreFilter imageStoreFilter) throws DirectorException {

		List<ImageStoreTransferObject> fetchedImageStore;
		try {
			fetchedImageStore = imagePersistenceManager
					.fetchImageStores(imageStoreFilter);
		} catch (DbException e) {
			log.error("Error in fetching ImageStores", e);
			throw new DirectorException("Error in fetching ImageStores", e);
		}
		return fetchedImageStore;
	}
	
	@Override
	public GenericDeleteResponse deleteImageStore(String imageStoreId) throws DirectorException {
		ImageStoreTransferObject fetchImageStorebyId = null;
		try {
			fetchImageStorebyId = imagePersistenceManager.fetchImageStorebyId(imageStoreId);
					
		} catch (DbException e) {
			log.error("Error in fetching ImageStore @ deleteImageStore", e);
			throw new DirectorException("Error in fetching ImageStore @ deleteImageStore", e);
		}
		if(fetchImageStorebyId == null){
			return null;
		}
		fetchImageStorebyId.setDeleted(true);
		try {
			imagePersistenceManager.updateImageStore(fetchImageStorebyId);
		} catch (DbException e) {
			log.error("Error in updating ImageStore @ deleteImageStore", e);
			throw new DirectorException("Error in updating ImageStore @ deleteImageStore", e);
		}
		return new GenericDeleteResponse();
	}
	
	@Override
	public ImageStoreTransferObject updateImageStore(ImageStoreTransferObject imageStoreTransferObject) throws DirectorException {
		try {
			
			imagePersistenceManager.updateImageStore(imageStoreTransferObject);
		} catch (DbException e) {
			log.error("Error in Updating ImageStore @ updateImageStore", e);
			throw new DirectorException("Error in Updating ImageStore @ updateImageStore", e);
		}
		return imageStoreTransferObject;
	}
	
	@Override
	public boolean doesImageStoreNameExist(String name,String imageStoreId) throws DirectorException{
		List<ImageStoreTransferObject> fetchImageStores;
		try {
			fetchImageStores = imagePersistenceManager.fetchImageStores(null);
		} catch (DbException e) {
			log.error("Error in Fetching ImageStore @ doesImageStoreNameExist", e);
			throw new DirectorException("Error in Fetching ImageStore @ doesImageStoreNameExist", e);
		}
		for (ImageStoreTransferObject imageStoreTO : fetchImageStores) {
			if(!imageStoreTO.deleted && imageStoreTO.name.equalsIgnoreCase(name) && !imageStoreTO.id.equalsIgnoreCase(imageStoreId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean validateConnectorArtifacts(String[] artifact_types,
			String connector) {
		boolean isValidated = true;
		switch (connector) {
		case Constants.CONNECTOR_DOCKERHUB:

			for (String artifact : artifact_types) {
				Map<String, String> supported_artifacts = ConnectorProperties.DOCKER
						.getSupported_artifacts();
				if (!supported_artifacts.containsKey(artifact)) {
					isValidated = false;
				}
			}
			return isValidated;

		case Constants.CONNECTOR_GLANCE:

			for (String artifact : artifact_types) {
				Map<String, String> supported_artifacts = ConnectorProperties.GLANCE
						.getSupported_artifacts();
				if (!supported_artifacts.containsKey(artifact)) {
					isValidated = false;
				}
			}
			return isValidated;
			
		case Constants.CONNECTOR_SWIFT:

			for (String artifact : artifact_types) {
				Map<String, String> supported_artifacts = ConnectorProperties.SWIFT
						.getSupported_artifacts();
				if (!supported_artifacts.containsKey(artifact)) {
					isValidated = false;
				}
			}
			return isValidated;

			default:
				return false;
		}
	}

	@Override
	public void validateImageStore(String imageStoreId) throws DirectorException {
		StoreManager manager = null;
		try {
			manager = StoreManagerFactory
					.getStoreManager(imageStoreId);
		} catch (StoreException e) {
			log.error("Error in Initializing ImageStore @ validateImageStore", e);
			throw new DirectorException("Not a valid auth URL or username or password");
		}
		try {
			GenericResponse validate = manager.validate();
			if(validate.getError() != null){
				throw new DirectorException(validate.getError());
			}
		} catch (StoreException e) {
			log.error(e.getMessage());
			throw new DirectorException(e.getMessage());
		}
	}
}
