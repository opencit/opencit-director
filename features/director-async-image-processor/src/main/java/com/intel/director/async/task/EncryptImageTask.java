/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.EncryptImage;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.features.director.kms.KmsUtil;

/**
 * Class to encrypt an image with a key from KMS
 * 
 * @author GS-0681
 */
public class EncryptImageTask extends ImageActionAsyncTask {

	public static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(EncryptImageTask.class);

	/**
	 * Entry method for executing the task
	 */
	@Override
	public boolean run() {
		boolean runFlag = false;
		// Call to update the task status
		log.debug("Running encrypt image task : " + taskAction.getStatus());
		if (taskAction.getStatus().equals(Constants.INCOMPLETE)) {
			log.debug("Running encrypt task for "
					+ imageActionObject.getImage_id());
			updateImageActionState(Constants.IN_PROGRESS, "Started");
			try {
				encryptImage();
				updateImageActionState(
						Constants.COMPLETE,
						"Completed encrypting image : "
								+ imageActionObject.getImage_id());
				runFlag = true;
			} catch (DirectorException e) {
				log.error("Error in EnryptImageTask", e);
				updateImageActionState(
						Constants.ERROR,
						"Error while encrypting image : "
								+ imageActionObject.getImage_id() + " Error: "
								+ e.getMessage());
			}
			
		}
		return runFlag;
	}

	/**
	 * Task to encrypt an image
	 * 
	 * @throws DirectorException
	 */
	private void encryptImage() throws DirectorException {
		TrustPolicy tp = null;
		try {
			tp = persistService.fetchPolicyForImage(imageActionObject
					.getImage_id());
			log.debug("EncryptImageTask: Got the trust policy ");
		} catch (DbException e1) {
			log.error("Error while fetching Trustpolicy for image : "+imageActionObject.getImage_id(), e1);
			throw new DirectorException(
					"Error while encrypting image. DB Error while fetching policy from DB",
					e1);
		}
		log.debug("Policy in DB for image : " + tp.getId());

		try {
			ImageInfo imageInfo = persistService
					.fetchImageById(imageActionObject.getImage_id());
			log.debug("EncryptImageTask: Got the image info");
			String imageLocation = imageInfo.getLocation();
			if (!imageLocation.endsWith(File.separator)) {
				imageLocation += File.separator;
			}
			
			imageLocation += imageInfo.getName();
			log.debug("EncryptImageTask: Image location is : "+imageLocation);
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil.getPolicy(tp.getTrust_policy());
			if (policy.getEncryption() == null) {
				log.debug("EncryptImageTask: No encryption required,. retun");
				return;
			}

			
			String url = policy.getEncryption().getKey()
					.getValue();
			String keyId = TdaasUtil.getKeyIdFromUrl(url);
			log.debug("EncryptImageTask: Key id for KMS : "+keyId);
			if (keyId == null) {
				log.error("Null key retrieved for url : " + url);
				throw new DirectorException("Null key retrieved for url : " + url);
			}

			String encryptFileName = imageInfo.getName();
			//TODO: Get actual key from KMS
			String keyFromKMS = new KmsUtil().getKeyFromKMS(keyId);
			if(keyFromKMS == null){
				log.error("Null key retrieved for keyId : " + keyId);
				throw new DirectorException("Null key retrieved for keyId : " + keyId);
			}
			log.debug("EncryptImageTask: Key from  KMS : "+keyFromKMS);
			log.debug("Got the trust policy with encrypt URL : "
					+ policy.getEncryption().getKey().getURL());
			EncryptImage.encryptFile(
					imageInfo.getLocation() + encryptFileName , keyFromKMS);
			log.info("Completed encryption task  ");
		} catch (JAXBException | IOException | XMLStreamException e) {
			log.error("Error while creating Trustpolicy", e);
			throw new DirectorException("Error while encrypting image", e);
		} catch (DbException ex) {
			log.error("DB Error while encrypting image task", ex);
			throw new DirectorException("Error while encrypting image", ex);
		} catch (Exception exx) {
			log.error("Error while encrypting image task", exx);
			throw new DirectorException("Error while encrypting image", exx);
		}
	}

	/**
	 * Task name identifier
	 */
	@Override
	public String getTaskName() {
		return Constants.TASK_NAME_ENCRYPT_IMAGE;
	}

	/**
	 * Extracts key from KMS url
	 * 
	 * @param url
	 *            KMS key url
	 * @return id of the key
	 */
}
