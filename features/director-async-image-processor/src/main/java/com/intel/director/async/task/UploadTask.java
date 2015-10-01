package com.intel.director.async.task;

import java.io.File;
import java.util.Date;
import java.util.Map;

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

public abstract class UploadTask extends ImageActionTask {

	public File content = null;
	public Map<String, String> imageProperties;
	public String uploadType = null;
	public ImageInfo imageInfo = null;
	public TrustPolicy trustPolicy =null;
	public String imageStoreName=null;
	public UploadTask() {
		super();
		initProperties();
	}

	
	
	


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
		initProperties();
		this.imageStoreName=imageStoreName;
	}



	public void initProperties() {

		try {
			imageInfo = persistService.fetchImageById(imageActionObject
					.getImage_id());
			 trustPolicy = persistService
					.fetchPolicyForImage(imageActionObject.getImage_id());
			
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

			imageProperties.put(Constants.NAME, "test_upload");
			imageProperties.put(Constants.DISK_FORMAT, diskFormat);
			imageProperties.put(Constants.CONTAINER_FORMAT, containerFormat);
			imageProperties.put(Constants.IS_PUBLIC, "true");
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

			ImageStoreManager imageStoreManager = null;
			
			/*
			 TODO:-
			 Fetch classname from database based on imagestorename
			 imageStoreManager=getImageStoreImpl(classname);
			 */
			 org.apache.commons.configuration.Configuration apacheConfig = new BaseConfiguration();
		        Configuration configuration = new CommonsConfiguration(apacheConfig);
		        configuration.set(Constants.GLANCE_IP, "10.35.35.136");
		        configuration.set(Constants.GLANCE_PORT,"9292");
		        configuration.set(Constants.GLANCE_IMAGE_STORE_USERNAME,"admin");
		        configuration.set(Constants.GLANCE_IMAGE_STORE_PASSWORD,"intelmh");
		        configuration.set(Constants.GLANCE_TENANT_NAME,"admin");
			imageStoreManager= new GlanceImageStoreManager(configuration);

			String glanceId = null;

			ImageActionActions imageActionTask = getImageActionTaskFromArray();
			if (imageActionTask.getUri() == null) {

				glanceId = imageStoreManager.upload(content, imageProperties);

			}
			ImageStoreUploadTransferObject imageUploadTransferObject = new ImageStoreUploadTransferObject();
			ImageStoreUploadResponse imageStoreUploadResponse = imageStoreManager
					.fetchDetails(null, glanceId);
			int size = imageStoreUploadResponse.getImage_size();
			int sent = 0;
			ImageAttributes imgAttrs;
			while (sent != size) {
				sent = imageStoreUploadResponse.getSent();

				// / Date currentTime = new Date();
				imgAttrs = new ImageAttributes();
				imgAttrs.setId(imageActionObject.getImage_id());
				imageUploadTransferObject.setImg(imgAttrs);
				imageUploadTransferObject.setImage_size(size);

				imageUploadTransferObject.setSent(sent);
				imageUploadTransferObject.setStatus(Constants.IN_PROGRESS);
				imageUploadTransferObject.setDate(new Date());
				imageUploadTransferObject.setChecksum(imageStoreUploadResponse
						.getChecksum());

				persistService.updateImageUpload(imageUploadTransferObject);

				imageStoreUploadResponse = imageStoreManager.fetchDetails(null,
						glanceId);

				Thread.sleep(10000);

			}

			if (size == sent) {
				imageUploadTransferObject.setStatus(Constants.COMPLETE);
				imageUploadTransferObject.setSent(size);
				imageUploadTransferObject.setDate(new Date());
				imgAttrs = new ImageAttributes();
				imgAttrs.setId(imageActionObject.getImage_id());
				imageUploadTransferObject.setImg(imgAttrs);
				imageUploadTransferObject.setChecksum(imageStoreUploadResponse
						.getChecksum());
				persistService.updateImageUpload(imageUploadTransferObject);
			}
		///	updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);

		} catch (Exception e) {
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

