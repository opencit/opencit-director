package com.intel.director.store.util;

import java.util.Collection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.crypto.key.password.PBKDFCryptoCodec;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.director.api.ConnectorProperties;
import com.intel.director.api.ImageStoreDetailsTransferObject;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.common.Constants;

public class ImageStorePasswordUtil {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageStorePasswordUtil.class);

	private String randomPassword = null;
	private PasswordProtection protection = null;
	private PBKDFCryptoCodec cipher = null;

	public ImageStorePasswordUtil(String id) {
		randomPassword = id;
		log.info("Using directorPassword : {}", randomPassword);
		protection = PasswordProtectionBuilder.factory().aes(128)
				.digestAlgorithm("SHA-256").keyAlgorithm("PBKDF2WithHmacSHA1")
				.iterations(1000).saltBytes(16).mode("CBC")
				.padding("PKCS5Padding").build();
		cipher = new PBKDFCryptoCodec(randomPassword, protection);

	}

	public String encryptPasswordForImageStore(String password) {
		String encryptedPassword = null;
		encryptedPassword = Base64.encodeBase64String(cipher.encrypt(password
				.getBytes()));
		return encryptedPassword;

	}

	public String decryptPasswordForImageStore(String encryptedPassword) {
		String decryptedPassword = null;
		decryptedPassword = new String(cipher.decrypt(Base64
				.decodeBase64(encryptedPassword)));
		return decryptedPassword;
	}

}
