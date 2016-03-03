package com.intel.director.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.intel.director.api.ImageStoreFilter;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.PolicyUploadTransferObject;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadFilter;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.PolicyUploadFilter;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ArtifactUploadService;
import com.intel.director.store.StoreManager;
import com.intel.director.store.StoreManagerFactory;
import com.intel.director.store.exception.StoreException;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class ArtifactUploadServiceImpl implements ArtifactUploadService {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ArtifactUploadServiceImpl.class);
	
	
	public String authToken;
	
	IPersistService persistenceManager;
	
	public ArtifactUploadServiceImpl() {
		persistenceManager = new DbServiceImpl();
	}

	
	@Override
	public List<ImageStoreUploadTransferObject> fetchImageUploadsByUploadVariable(
			String uploadVar) throws DirectorException {
		ImageStoreUploadOrderBy  imgOrder= new ImageStoreUploadOrderBy();
		imgOrder.setImgStoreUploadFields(ImageStoreUploadFields.DATE);		
		imgOrder.setOrderBy(OrderByEnum.DESC);
		ImageStoreUploadFilter imgUpFilter = new ImageStoreUploadFilter();

		imgUpFilter.setUploadVariableMD5(uploadVar);

		List<ImageStoreUploadTransferObject> imageUploads = null;
		try {
			imageUploads = persistenceManager.fetchImageUploads(imgUpFilter, imgOrder);
		} catch (DbException e) {
			throw new DirectorException("Error fetching the image uploads", e);
		}
		
		return imageUploads;

	}
	@Override
	public ImageStoreUploadTransferObject fetchImageUploadByImageId(
			String imageId) {
		ImageStoreUploadOrderBy imgOrder = new ImageStoreUploadOrderBy();
		imgOrder.setImgStoreUploadFields(ImageStoreUploadFields.DATE);
		imgOrder.setOrderBy(OrderByEnum.DESC);
		ImageStoreUploadFilter imgUpFilter = new ImageStoreUploadFilter();
		imgUpFilter.setImage_id(imageId);
		List<ImageStoreUploadTransferObject> fetchImageUploads = null;
		try {
			fetchImageUploads = persistenceManager.fetchImageUploads(
					imgUpFilter, imgOrder);
			if (!(fetchImageUploads != null && fetchImageUploads.size() > 0)) {
				return null;
			}
		} catch (DbException e) {
			log.error("Error fetching image uploads by image id {}", imageId, e);
			return null;
		}
		return fetchImageUploads.get(0);
	}

	@Override
	public void removeOrphanPolicies() throws DirectorException {
		List<String> idFromImageStores = new ArrayList<String>();

		ImageStoreFilter imageStoreFilter = new ImageStoreFilter();
		imageStoreFilter.setConnector(Constants.CONNECTOR_GLANCE);
		List<ImageStoreTransferObject> fetchImageStores = null;
		try {
			fetchImageStores = persistenceManager
					.fetchImageStores(imageStoreFilter);
		} catch (DbException e) {
			log.error("Error in fetchImageStores ", e);
			throw new DirectorException(
					"Error in fetchImageStores for Glance Connector", e);
		}
		if (fetchImageStores == null) {
			return;
		}

		for (ImageStoreTransferObject imageStoreTransferObject : fetchImageStores) {
			StoreManager imageStoreManager =null;
			try {
				imageStoreManager = StoreManagerFactory
						.getStoreManager(imageStoreTransferObject.getId());
			} catch (StoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<ImageStoreUploadResponse> fetchAllImages = null;
			try {
				fetchAllImages = imageStoreManager
						.fetchAllImages();
			} catch (StoreException e) {
				log.error("Error in fetchAllImagesFromGlance ", e);
				throw new DirectorException("Error in fetchAllImagesFromGlance", e);
			}
			for (ImageStoreUploadResponse storeResponse : fetchAllImages) {
				idFromImageStores.add(storeResponse.getId());
			}

		}

		List<ImageStoreUploadTransferObject> fetchImageUploads = null;
		try {
			ImageStoreUploadFilter uploadFilter = new ImageStoreUploadFilter();
			uploadFilter.setEnableDeletedCheck(true);
			uploadFilter.setDeleted(false);
			fetchImageUploads = persistenceManager.fetchImageUploads(
					uploadFilter, null);
		} catch (DbException e) {
			log.error("Error in fetchImageUploads ", e);
			throw new DirectorException("Error in fetchImageUploads", e);
		}

		if (fetchImageUploads == null) {
			return;
		}

		List<String> imageStoreUploadArtifactsIdList = new ArrayList<String>();
		for (ImageStoreUploadTransferObject imageStoreUploadTransferObject : fetchImageUploads) {
			// TODO Change to Image Store Object Id
			String id = imageStoreUploadTransferObject.getStoreArtifactId();
			imageStoreUploadArtifactsIdList.add(id);
		}

		imageStoreUploadArtifactsIdList.removeAll(idFromImageStores);

		List<PolicyUploadTransferObject> policyUploadTransferObjects = null;
		try {
			PolicyUploadFilter uploadFilter = new PolicyUploadFilter();
			uploadFilter.setEnableDeletedCheck(true);
			uploadFilter.setDeleted(false);
			policyUploadTransferObjects = persistenceManager
					.fetchPolicyUploads(uploadFilter, null);
		} catch (DbException e) {
			log.error("Error in fetchImageUploads ", e);
			throw new DirectorException("Error in fetchImageUploads", e);
		}

		if (policyUploadTransferObjects == null) {
			return;
		}

		Map<String, StoreManager> storeManagerMap = new HashMap<>();
		for (PolicyUploadTransferObject policyUploadTransferObject : policyUploadTransferObjects) {
			if (imageStoreUploadArtifactsIdList
					.contains(policyUploadTransferObject.getStoreArtifactId())) {
				StoreManager imageStoreManager = null;
				if (storeManagerMap
						.get(policyUploadTransferObject.getStoreId()) != null) {
					imageStoreManager = storeManagerMap
							.get(policyUploadTransferObject.getStoreId());
				} else {
					try {
						imageStoreManager = StoreManagerFactory
								.getStoreManager(policyUploadTransferObject
										.getStoreId());
					} catch (StoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					storeManagerMap.put(
							policyUploadTransferObject.getStoreId(),
							imageStoreManager);
				}

				try {
					try {
						imageStoreManager.delete(new URL(policyUploadTransferObject
								.getPolicy_uri()));
					} catch (StoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} catch (MalformedURLException e) {
					log.error("Unable to delete Policy "
							+ policyUploadTransferObject.getPolicy_uri(), e);
				}
				
				//Setting Delete Flag to true for deleted policies
				policyUploadTransferObject.setDeleted(true);
				try {
					persistenceManager
							.updatePolicyUpload(policyUploadTransferObject);
				} catch (DbException e) {
					log.error("Error  in updating policy upload"
							+ policyUploadTransferObject.getId(), e);
				}
			}
		}

		
		//Setting Delete Flag to true for deleted images 
		for (ImageStoreUploadTransferObject imageStoreUploadTransferObject : fetchImageUploads) {
			if (imageStoreUploadArtifactsIdList
					.contains(imageStoreUploadTransferObject
							.getStoreArtifactId())) {
				imageStoreUploadTransferObject.setDeleted(true);
				try {
					persistenceManager
							.updateImageUpload(imageStoreUploadTransferObject);
				} catch (DbException e) {
					log.error("Error  in updating image upload"
							+ imageStoreUploadTransferObject.getId(), e);
				}
			}
		}
		

	}
	

}
