package com.intel.director.async.task;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.BaseConfiguration;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.ImageActionTask;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.images.GlanceImageStoreManager;
import com.intel.director.imagestore.ImageStoreManager;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;

/**
 * Superclass for all upload tasks
 * 
 * @author Siddharth
 * 
 */
public abstract class UploadTask extends ImageActionAsyncTask {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UploadTask.class);
	public File content = null;
	public Map<String, String> imageProperties = new HashMap<String, String>();
	public String uploadType = null;
	public ImageInfo imageInfo = null;
	public TrustPolicy trustPolicy = null;
	public String imageStoreName = null;

	public UploadTask() {
		super();
		initProperties();
	}

	/**
	 * Constructs the task details with store details and upload artifacts
	 * 
	 * @param content
	 * @param imageProperties
	 * @param imageInfo
	 * @param trustPolicy
	 * @param imageStoreName
	 */
	public UploadTask(File content, Map<String, String> imageProperties,
			ImageInfo imageInfo, TrustPolicy trustPolicy, String imageStoreName) {
		super();
		this.content = content;
		this.imageProperties = imageProperties;
		this.imageInfo = imageInfo;
		this.trustPolicy = trustPolicy;
		this.imageStoreName = imageStoreName;
	}

	public UploadTask(String imageStoreName) {
		super();
		// // initProperties();
		this.imageStoreName = imageStoreName;
	}

	/**
	 * Initializes the props for Image store
	 */
	public void initProperties() {

		try {
			imageInfo = persistService.fetchImageById(imageActionObject
					.getImage_id());
			trustPolicy = persistService.fetchPolicyForImage(imageActionObject
					.getImage_id());

			String diskFormat = null, containerFormat = null;

			switch (imageInfo.image_format) {
			case "ami":
				diskFormat = "ami";
				containerFormat = "ami";
				break;
			case "qcow2":
				diskFormat = "qcow2";
				containerFormat = "bare";
				break;
			case "vhd":
				diskFormat = "vhd";
				containerFormat = "bare";
				break;
			case "raw":
				diskFormat = "raw";
				containerFormat = "bare";
				break;
			case "tar":
				diskFormat = "raw";
				containerFormat = "docker";
				break;
			}

			// / imageProperties.put(Constants.NAME, "test_upload");
			imageProperties.put(Constants.DISK_FORMAT, diskFormat);
			imageProperties.put(Constants.CONTAINER_FORMAT, containerFormat);
			imageProperties.put(Constants.IS_PUBLIC, "true");
		} catch (Exception e) {
			log.debug("exception in initproperties ", e);
			updateImageActionState(Constants.ERROR, e.getMessage());
		}

	}

	public File getContent() {
		return content;
	}

	public void setContent(File content) {
		this.content = content;
	}

	public Map<String, String> getImageProperties() {
		return imageProperties;
	}

	public void setImageProperties(Map<String, String> imageProperties) {
		this.imageProperties = imageProperties;
	}

	@Override
	public boolean run() {
		return runUploadTask();
	}

	private boolean runUploadTask() {
		boolean runFlag = false;
		FileReader reader = null;

		/*
		 * TODO:- Fetch classname from database based on imagestorename
		 * imageStoreManager=getImageStoreImpl(classname);
		 * 
		 * Fetch Glance parameters from property file
		 */

		org.apache.commons.configuration.Configuration apacheConfig = new BaseConfiguration();
		Configuration configuration = new CommonsConfiguration(apacheConfig);

		File customFile = new File(Folders.configuration() + File.separator
				+ "director.properties");
		ConfigurationProvider provider;

		try {
			provider = ConfigurationFactory
					.createConfigurationProvider(customFile);
			com.intel.dcsg.cpg.configuration.Configuration loadedConfiguration = provider
					.load();

			configuration.set(Constants.GLANCE_API_ENDPOINT,
					loadedConfiguration.get(Constants.GLANCE_API_ENDPOINT));
			configuration.set(Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT,
					loadedConfiguration.get(Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT));
			configuration.set(Constants.GLANCE_IMAGE_STORE_USERNAME,
					loadedConfiguration
							.get(Constants.GLANCE_IMAGE_STORE_USERNAME));
			configuration.set(Constants.GLANCE_IMAGE_STORE_PASSWORD,
					loadedConfiguration
							.get(Constants.GLANCE_IMAGE_STORE_PASSWORD));
			configuration.set(Constants.GLANCE_TENANT_NAME,
					loadedConfiguration.get(Constants.GLANCE_TENANT_NAME));
		} catch (IOException e1) {
			log.error(
					"Failed to fetch glance properties form director.properties",
					e1);
			updateImageActionState(Constants.ERROR,
					"Failed to fetch glance properties form director.properties");
			return false;
		}

		try {
			ImageStoreManager imageStoreManager = new GlanceImageStoreManager(
					configuration);

			String glanceId = null;

			log.debug("Inside runUpload Task filename::" + content.getName()
					+ " imageProperties::" + imageProperties);
			System.out.println("Inside runUpload Task filename::"
					+ content.getName() + " imageProperties::"
					+ imageProperties);
			ImageActionTask imageActionTask = getImageActionTaskFromArray();
			if (imageActionTask != null && imageActionTask.getUri() == null) {
				glanceId = imageStoreManager.upload(content, imageProperties);

				log.debug("Upload process started");
			}
			ImageStoreUploadTransferObject imageUploadTransferObject = new ImageStoreUploadTransferObject();
			ImageStoreUploadResponse imageStoreUploadResponse = imageStoreManager
					.fetchDetails(null, glanceId);
			long size = (long) content.length() ;
			long dataSize = content.length() ;
			ImageAttributes imgAttrs;
			String uploadid = null;
			boolean firstTime = true;
			long sent = (long) imageStoreUploadResponse.getSent() ;
			long dataSent = imageStoreUploadResponse.getSent() ;
			while (dataSent != dataSize) {

				log.debug("##################Inside while loop size::" + size
						+ " sent::" + sent);
				imgAttrs = new ImageAttributes();
				imgAttrs.setId(imageActionObject.getImage_id());
				updateImageActionContentSent(sent, size);
	
				imageStoreUploadResponse = imageStoreManager.fetchDetails(null,
						glanceId);
				dataSent = (long)imageStoreUploadResponse.getSent() ;
				sent = (long) imageStoreUploadResponse.getSent() ;
			}

			imgAttrs = new ImageAttributes();
			imgAttrs.setId(imageActionObject.getImage_id());

			updateImageActionContentSent(sent, size);
		///	imageUploadTransferObject.setStatus(Constants.IN_PROGRESS);

			
			imageUploadTransferObject.setStatus(Constants.COMPLETE);
			if (size == sent) {
				log.info("Saving to MW_IMAGE_UPLOAD. Size of image={}. Uploaded size={}", size, sent);
				imageUploadTransferObject.setImg(imgAttrs);
				imageUploadTransferObject.setImage_size(size);

				imageUploadTransferObject.setSent(sent);
				imageUploadTransferObject.setDate(new Date());
				imageUploadTransferObject.setChecksum(imageStoreUploadResponse
						.getChecksum());
				log.info("URI {}",imageStoreUploadResponse
						.getImage_uri() );
				imageUploadTransferObject.setImage_uri(glanceId);
				log.info("Image upload date to be saved : {}", imageUploadTransferObject);
				ImageStoreUploadTransferObject imgTransaferObject = persistService
						.saveImageUpload(imageUploadTransferObject);
				// /uploadid = imgTransaferObject.getId();
				// /firstTime = false;
			} /*
			 * else {
			 * 
			 * imageUploadTransferObject.setId(uploadid);
			 * persistService.updateImageUpload(imageUploadTransferObject); }
			 */

			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
			runFlag = true;
		} catch (Exception e) {
			log.error(
					"Error in uploading artifact to Glance for image action :"
							+ imageActionObject.getImage_id(), e);
			updateImageActionState(Constants.ERROR, e.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				log.error("Error closing streams ");
			}
		}
		return runFlag;
	}

	public ImageStoreManager getImageStoreImpl(String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		ImageStoreManager imgManager;
		Class c;

		c = Class.forName(className);

		imgManager = (ImageStoreManager) c.newInstance();

		return imgManager;

	}

}
