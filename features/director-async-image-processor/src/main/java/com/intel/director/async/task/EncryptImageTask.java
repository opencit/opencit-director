/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import com.intel.director.common.Constants;

/**
 * 
 * @author GS-0681
 */
public class EncryptImageTask extends ImageActionTask {
	@Override
	public void run() {
		// Call to update the task status
		if (taskAction.getStatus().equals(Constants.INCOMPLETE)) {
			updateImageActionState(Constants.IN_PROGRESS, "Started");
			encryptImage();
		}

	}

	private void encryptImage() {/*
		KeyContainer keyContainer;
		try {
			
			String kmsEndpointUrl;
			String kmsTlsPolicyCertificateSha1;
			String kmsLoginBasicUsername;
			String kmsLoginBasicPassword = null;
			
			Properties properties = new Properties();		
			properties.setProperty("endpoint.url", kmsEndpointUrl);
			properties.setProperty("tls.policy.certificate.sha1",
					kmsTlsPolicyCertificateSha1);
			properties.setProperty("login.basic.username", kmsLoginBasicUsername);
			properties.setProperty("login.basic.password", kmsLoginBasicPassword);

			keyContainer = KmsUtil.getEncryptionKeyFromKms();
		} catch (Exception e2) {
			updateImageActionState(Constants.ERROR, e2.getMessage());
			return;
		}
		String location;
		try {
			location = persistService.fetchImageById(
					imageActionObject.getImage_id()).getLocation();
		} catch (DbException e1) {
			updateImageActionState(Constants.ERROR, e1.getMessage());
			return;
		}
		if (location == null) {

		}
		try {
			String encryptedImageLocation = ImageUtil.encryptFile(location,
					Base64.encodeBase64String(keyContainer.secretKey
							.getEncoded()));
			taskAction.setLocation(encryptedImageLocation);
			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
		} catch (Exception e1) {
			updateImageActionState(Constants.ERROR, e1.getMessage());
			return;
		}

	*/
	try {
		Thread.sleep(10000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}finally{
		updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
	}
	
	}

	@Override
	public String getTaskName() {
		
		return Constants.TASK_NAME_ENCRYPT_IMAGE;
	}

}
