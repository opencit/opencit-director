/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.intel.director.api.StoreResponse;
import com.intel.director.common.Constants;
import com.intel.director.images.rs.GlanceException;
import com.intel.director.images.rs.GlanceRsClient;
import com.intel.director.images.rs.GlanceRsClientBuilder;
import com.intel.director.store.exception.StoreException;
import com.intel.director.store.impl.StoreManagerImpl;

/**
 * 
 * @author Aakash
 */
public class GlanceImageStoreManager extends StoreManagerImpl {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(GlanceImageStoreManager.class);

	private GlanceRsClient glanceRsClient;

	private void uploadImage(Map<String, Object> imageProperties)
			throws StoreException {

		try {
			glanceRsClient.uploadImage(imageProperties);
		} catch (GlanceException e) {
			throw new StoreException("Error in uploadImage to Glance", e);
		}

	}

	public void update(Map<String, Object> imageProperties)
			throws StoreException {
		try {
			glanceRsClient.updateMetadata(imageProperties);
		} catch (GlanceException e) {
			throw new StoreException("Error in update to Glance", e);
		}
	}

	@Override
	public String upload() throws StoreException {
		String glanceid = null;
		try {
			log.info("uploading metadata");
			 glanceid = glanceRsClient
					.uploadImageMetaData(objectProperties);
			log.info("uploading image");
			uploadImage(objectProperties);
			return glanceid;
			// //startPolling(file.geimageProperties, glanceid);
		} catch (GlanceException e) {
			log.error("Error  upload to Glance", e);
			if(StringUtils.isNotBlank(glanceid)){
				//delete
				throw new StoreException(Constants.ARTIFACT_ID+":"+glanceid, e);
			}
			throw new StoreException("Error  upload to Glance", e);
		}

	}

	public void uploadImageMetadata() throws StoreException {
		try {
			glanceRsClient.uploadImage(objectProperties);
		} catch (GlanceException e) {
			throw new StoreException(
					e);
		}
	}

	public void uploadImageMetadataPOST() throws StoreException {
		try {
			glanceRsClient.uploadImageMetaData(objectProperties);
		} catch (GlanceException e) {
			throw new StoreException(
					"Error while uploadImageMetadataPOST in Glance", e);
		}
	}

	@Override
	public <T extends StoreResponse> T fetchDetails() throws StoreException {
		try {
			return (T) glanceRsClient.fetchDetails(objectProperties);
		} catch (GlanceException e) {
			log.error("Error  fetchDetails in Glance", e);
			throw new StoreException(
					"Error while fetchDetails if upload from Glance", e);

		}
	}

	@Override
	public void build(Map<String, String> configuration) throws StoreException {
		super.build(configuration);
		try {
			glanceRsClient = GlanceRsClientBuilder.build(configuration);
		} catch (GlanceException e) {
			log.error("Error in creating GlanceRsclient", e);
			throw new StoreException("Error in creating GlanceRsclient", e);
		}

	}

	@Override
	public void update() throws StoreException {
		// TODO Auto-generated method stub
		// /System.out.println("objectProperties::"+objectProperties);
		try {
			glanceRsClient.updateMetadata(objectProperties);
		} catch (GlanceException e) {
			log.error("Error  in updateMetadataGlance in Glance", e);
			throw new StoreException("Error in updateMetadataGlance", e);
		}
	}

	@Override
	public void delete(URL url) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends StoreResponse> List<T> fetchAllImages() {
		// TODO Auto-generated method stub
		return null;
	}

}
