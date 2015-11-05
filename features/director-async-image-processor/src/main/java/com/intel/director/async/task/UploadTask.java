package com.intel.director.async.task;

import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.BaseConfiguration;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.images.GlanceImageStoreManager;
import com.intel.director.imagestore.ImageStoreManager;
import com.intel.mtwilson.Folders;

/**
 * Superclass for all upload tasks
 * 
 * @author Siddharth
 * 
 */
public abstract class UploadTask extends ImageActionTask {
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
	public void run() {

		runUploadTask();

	}

	public void runUploadTask() {

		try {


			/*
			 * TODO:- Fetch classname from database based on imagestorename
			 * imageStoreManager=getImageStoreImpl(classname);
			 * 
			 * Fetch Glance parameters from property file
			 */

			File configfile = new File(
					Folders.configuration() + File.separator + "director.properties");
			org.apache.commons.configuration.Configuration apacheConfig = new BaseConfiguration();
			Configuration configuration = new CommonsConfiguration(apacheConfig);
			FileReader reader = new FileReader(configfile);

			Properties prop = new Properties();

			prop.load(reader);

			configuration.set(Constants.GLANCE_IP,
					prop.getProperty(Constants.GLANCE_IP));
			configuration.set(Constants.GLANCE_PORT,
					prop.getProperty(Constants.GLANCE_PORT));
			configuration.set(Constants.GLANCE_IMAGE_STORE_USERNAME,
					prop.getProperty(Constants.GLANCE_IMAGE_STORE_USERNAME));
			configuration.set(Constants.GLANCE_IMAGE_STORE_PASSWORD,
					prop.getProperty(Constants.GLANCE_IMAGE_STORE_PASSWORD));
			configuration.set(Constants.GLANCE_TENANT_NAME,
					prop.getProperty(Constants.GLANCE_TENANT_NAME));
			ImageStoreManager imageStoreManager = new GlanceImageStoreManager(configuration);

			String glanceId = null;

			log.debug("Inside runUpload Task filename::" + content.getName()
					+ " imageProperties::" + imageProperties);
			System.out.println("Inside runUpload Task filename::"
					+ content.getName() + " imageProperties::"
					+ imageProperties);
			ImageActionActions imageActionTask = getImageActionTaskFromArray();
			if (imageActionTask.getUri() == null) {
				glanceId = imageStoreManager.upload(content, imageProperties);

				log.debug("Upload process started");
			}
			ImageStoreUploadTransferObject imageUploadTransferObject = new ImageStoreUploadTransferObject();
			ImageStoreUploadResponse imageStoreUploadResponse = imageStoreManager
					.fetchDetails(null, glanceId);
			int size = (int) (content.length() / 1024);
			ImageAttributes imgAttrs;
			String uploadid = null;
			boolean firstTime = true;
			int sent = imageStoreUploadResponse.getSent() / 1024;
			while (sent != size) {

				log.debug("##################Inside while loop size::" + size
						+ " sent::" + sent);
				// / Date currentTime = new Date();
				imgAttrs = new ImageAttributes();
				imgAttrs.setId(imageActionObject.getImage_id());
				imageUploadTransferObject.setImg(imgAttrs);
				imageUploadTransferObject.setImage_size(size);

				imageUploadTransferObject.setSent(sent);
				updateImageActionContentSent(sent, size) ;
				imageUploadTransferObject.setStatus(Constants.IN_PROGRESS);

				imageUploadTransferObject.setDate(new Date());
				imageUploadTransferObject.setChecksum(imageStoreUploadResponse
						.getChecksum());
				imageUploadTransferObject.setImage_uri(imageStoreUploadResponse
						.getImage_uri());
				if (firstTime) {

					ImageStoreUploadTransferObject imgTransaferObject = persistService
							.saveImageUpload(imageUploadTransferObject);
					uploadid = imgTransaferObject.getId();
					firstTime = false;
				} else {

					imageUploadTransferObject.setId(uploadid);
					persistService.updateImageUpload(imageUploadTransferObject);
				}
				imageStoreUploadResponse = imageStoreManager.fetchDetails(null,
						glanceId);
				sent = imageStoreUploadResponse.getSent() / 1024;
				// / Thread.sleep(2000);

			}

			if (size == sent) {
				imgAttrs = new ImageAttributes();
				imgAttrs.setId(imageActionObject.getImage_id());
				imageUploadTransferObject.setImg(imgAttrs);
				imageUploadTransferObject.setImage_size(size);

				imageUploadTransferObject.setSent(sent);
				updateImageActionContentSent(sent, size) ;
				imageUploadTransferObject.setStatus(Constants.IN_PROGRESS);

				imageUploadTransferObject.setDate(new Date());
				imageUploadTransferObject.setChecksum(imageStoreUploadResponse
						.getChecksum());
				imageUploadTransferObject.setImage_uri(imageStoreUploadResponse
						.getImage_uri());
				imageUploadTransferObject.setStatus(Constants.COMPLETE);
				if (firstTime) {

					ImageStoreUploadTransferObject imgTransaferObject = persistService
							.saveImageUpload(imageUploadTransferObject);
					uploadid = imgTransaferObject.getId();
					// /firstTime = false;
				} else {

					imageUploadTransferObject.setId(uploadid);
					persistService.updateImageUpload(imageUploadTransferObject);
				}

			}
			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(
					"runUploadtask failed for::"
							+ imageActionObject.getImage_id(), e);
			updateImageActionState(Constants.ERROR, e.getMessage());
		}
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
