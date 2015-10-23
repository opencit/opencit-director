/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common;

/**
 * 
 * @author boskisha
 */
public class EncryptImage {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(EncryptImage.class);

	// Encrypt the file using openssl aes
	public static String encryptFile(String location, String password)
			throws Exception {
		String command = "openssl enc -aes-128-ofb -in " + location + " -out "
				+ location + "-enc" + " -pass pass:" + password;
		int exitCode = DirectorUtil.executeCommandInExecUtil(Constants.encryptImageScript, location, location+"-enc", password);
		if (exitCode != 0) {
			log.error("Error while encrypting the file .....");
			throw new Exception("Can not encrypt image");
		}
		return location + "-enc";
	}
}
