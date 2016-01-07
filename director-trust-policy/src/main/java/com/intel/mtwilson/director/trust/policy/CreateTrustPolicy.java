/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.trust.policy;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.director.common.DirectorUtil;
import com.intel.mtwilson.director.features.director.kms.KeyContainer;
import com.intel.mtwilson.director.features.director.kms.KmsUtil;
import com.intel.mtwilson.trustpolicy.xml.Checksum;
import com.intel.mtwilson.trustpolicy.xml.DecryptionKey;
import com.intel.mtwilson.trustpolicy.xml.DigestAlgorithm;
import com.intel.mtwilson.trustpolicy.xml.DirectoryMeasurement;
import com.intel.mtwilson.trustpolicy.xml.Encryption;
import com.intel.mtwilson.trustpolicy.xml.FileMeasurement;
import com.intel.mtwilson.trustpolicy.xml.ImageHash;
import com.intel.mtwilson.trustpolicy.xml.Measurement;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy;
import com.intel.mtwilson.trustpolicy.xml.Whitelist;

/**
 * 
 * @author boskisha
 */

public class CreateTrustPolicy {

	private String imageId;
	
	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public CreateTrustPolicy(String imageId){
		this.imageId = imageId;
	}
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(CreateTrustPolicy.class);

	/**
	 * creates trust policy 1) Adds whitelist value for files, directory and
	 * cumulative hash 2) Calls KMS to gets encryption key and adds DEK url in
	 * trust policy
	 * 
	 * @param trustPolicy
	 *            contains information about what is selected by user from UI
	 *            such as launch control and encryption policy, files and
	 *            directories.
	 * @throws CryptographyException
	 * @throws IOException
	 */
	public void createTrustPolicy(TrustPolicy trustPolicy)
			throws CryptographyException, IOException {
		// calculate files, directory and cumulative hash
		addWhitelistValue(trustPolicy);

		// if encryption policy is set
		if (trustPolicy.getEncryption() != null) {
			try {
				addEncryption(trustPolicy);
			} catch (Exception e) {
				// TODO Handle Error
				log.error(
						"Error in createTrustPolicy() in CreateTrustPolicy.java",
						e);
			}
		}
	}

	/**
	 * Adds encryption information such as dek URL and checksum in trust policy
	 * 
	 * @param inputPolicy
	 * @return
	 * @throws Exception
	 * @throws XMLStreamException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws CryptographyException
	 */
	private void addEncryption(TrustPolicy trustPolicy)
			throws CryptographyException, IOException, JAXBException,
			XMLStreamException {
		// get DEK
		KmsUtil kmsUtil = null;
		try {
			kmsUtil = new KmsUtil();
		} catch (Exception e1) {
			log.error("Error in initialization of KMS Util");
			return;
		}
		
		//Check if a key already exists
		//If so, check if the key is still valid
		//If so, return
		if(StringUtils.isNotBlank(trustPolicy.getEncryption().getKey().getValue()) && trustPolicy.getEncryption().getKey().getValue().contains("keys")){
			String url = trustPolicy.getEncryption().getKey().getValue();
			String keyIdFromUrl = DirectorUtil.getKeyIdFromUrl(url);
			String keyFromKMS = kmsUtil.getKeyFromKMS(keyIdFromUrl);
			if(StringUtils.isNotBlank(keyFromKMS)){
				log.info("Existing key is still valid. Not creating a new one.");
				return;
			}
		}

		KeyContainer key;
		try {
			key = kmsUtil.createKey();
			DecryptionKey decryptionKey = new DecryptionKey();
			decryptionKey.setURL("uri");
			decryptionKey.setValue(key.url.toString());
			Encryption encryption = trustPolicy.getEncryption();
			if (encryption == null) {
				encryption = new Encryption();
			}
			encryption.setKey(decryptionKey);
			Checksum checksum = new Checksum();
			checksum.setDigestAlg(DigestAlgorithm.MD_5);
			checksum.setValue("1");
			encryption.setChecksum(checksum);
			trustPolicy.setEncryption(encryption);
		} catch (Exception e) {
			// TODO Handle Error
			log.error("Error in KmsUtil().createKey() in addEncryption() in CreateTrustPolicy.java", e);
		}
	}

	/**
	 * 1) Adds whitelist value for file and directory. File hash is a hash of
	 * the content of file. Directory hash is the hash of list of files which
	 * satisfies include and exclude criteria of a directory. 2) Adds cumulative
	 * hash which is extended hash of files and directories hashes
	 * 
	 * @param trustPolicy
	 * @return
	 */
	private void addWhitelistValue(TrustPolicy trustPolicy)
			throws IOException {
		Whitelist whitelist = trustPolicy.getWhitelist();
		List<Measurement> measurements = whitelist.getMeasurements();
		String imageId = trustPolicy.getImage().getImageId();
		DirectoryAndFileUtil dirFileUtil = new DirectoryAndFileUtil(imageId);
		//List<String> invalidFiles = new ArrayList<>();
		// Initialize cumulative hash
		Digest cumulativeDigest = Digest.algorithm(
				whitelist.getDigestAlg().value()).zero();

		// Get file and directory hash and extend value to cumulative hash
		Digest hash=null;
		for (Measurement measurement : measurements) {
			hash=null;
			if (measurement instanceof DirectoryMeasurement) {
				hash = dirFileUtil.getDirectoryHash(imageId,
						(DirectoryMeasurement) measurement, whitelist
								.getDigestAlg().value());
			} else if (measurement instanceof FileMeasurement) {
				hash = dirFileUtil.getFileHash(imageId,
						(FileMeasurement) measurement, whitelist.getDigestAlg()
								.value());
				if (hash == null) {
					//invalidFiles.add(measurement.getPath());
					continue;
				}

			} else {
				log.error("Unsupported mesurement type");
				return;
			}
			measurement.setValue(hash.toHex());
			log.debug("before extending hash {}", cumulativeDigest.toHex());
			cumulativeDigest = cumulativeDigest.extend(hash.getBytes());
			log.debug("after extending hash {}", cumulativeDigest.toHex());
		}

		// set cmulative hash
		ImageHash imageHash = new ImageHash();
		imageHash.setValue(cumulativeDigest.toHex());
		trustPolicy.getImage().setImageHash(imageHash);
	}

}
