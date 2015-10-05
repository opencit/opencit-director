/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.async.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.intel.director.api.TrustPolicy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.ImageUtil;
import com.intel.director.util.DirectorUtil;

/**ó
 * 
 * @author GS-0681
 */
public class CreateTarTask extends ImageActionTask {

	@Override
	public void run() {

		// Call to update the task status

	
			if (Constants.INCOMPLETE.equalsIgnoreCase(taskAction.getStatus())) {
				updateImageActionState(Constants.IN_PROGRESS, "Started");
				runCreateTarTask();
			}
		

	}

	public void runCreateTarTask() {
		String imageLocation = null;
		String trustPolicyLocation = null;
		String trustPolicyName = null;
		File trustPolicyFile = null;
		try {

			ImageInfo imageinfo = persistService
					.fetchImageById(imageActionObject.getImage_id());
			imageLocation = imageinfo.getLocation();
			// Fetch the policy and write to a location. Move to common

			TrustPolicy trustPolicy = persistService
					.fetchPolicyForImage(imageActionObject.getImage_id());
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = DirectorUtil
					.getPolicy(trustPolicy.getTrust_policy());
			String imageName;
			if (policy != null && policy.getEncryption() != null) {
				imageName = imageinfo.getName() + "-enc";

			} else {
				imageName = imageinfo.getName();
			}

			if (trustPolicy != null) {

				trustPolicyName = "policy_" + trustPolicy.getId() + ".xml";
				trustPolicyLocation = imageLocation;
				trustPolicyFile = new File(trustPolicyLocation + "/"
						+ trustPolicyName);

				if (!trustPolicyFile.exists()) {
					trustPolicyFile.createNewFile();
				}

				FileWriter fw = new FileWriter(
						trustPolicyFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(trustPolicy.getTrust_policy());
				bw.close();
			}

			String tarLocation = imageLocation;
			String tarName = imageName + "_" + trustPolicy.getId() + ".tar";
			ImageUtil.createTar(imageName, imageLocation, trustPolicyName,
					tarLocation, tarName);

			Thread.sleep(60000);

			updateImageActionState(Constants.COMPLETE, Constants.COMPLETE);
		} catch (Exception e) {
			updateImageActionState(Constants.ERROR, e.getMessage());
		} finally {
			if (trustPolicyFile != null && trustPolicyFile.exists()) {
				trustPolicyFile.delete();
			}
		}

	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return Constants.TASK_NAME_CREATE_TAR;
	}

}
