/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.trust.policy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.mtwilson.director.features.director.kms.KeyContainer;
import com.intel.mtwilson.director.features.director.kms.KmsUtil;
import com.intel.mtwilson.trustpolicy.xml.Checksum;
import com.intel.mtwilson.trustpolicy.xml.DecryptionKey;
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
	 */
	public static void createTrustPolicy(TrustPolicy trustPolicy)
			throws IOException, JAXBException, XMLStreamException, Exception {
		// calculate files, directory and cumulative hash
		addWhitelistValue(trustPolicy);

		// if encryption policy is set
		if (trustPolicy.getEncryption() != null) {
			addEncryption(trustPolicy);
		}
	}


	/**
	 * Adds encryption information such as dek URL and checksum in trust policy
	 * 
	 * @param inputPolicy
	 * @return
	 */
	private static void addEncryption(TrustPolicy trustPolicy) throws Exception {
		// get DEK
		KeyContainer key = new KmsUtil().createKey();		
		DecryptionKey decryptionKey = new DecryptionKey();
		decryptionKey.setURL("uri");
		decryptionKey.setValue(key.url.toString());
		Encryption encryption = trustPolicy.getEncryption();
		if(encryption == null){
			encryption = new Encryption();
		}		
		encryption.setKey(decryptionKey);
        Checksum checksum = new Checksum();
        checksum.setDigestAlg(trustPolicy.getWhitelist().getDigestAlg());
        checksum.setValue("1");
        encryption.setChecksum(checksum);
		trustPolicy.setEncryption(encryption);
		// Get checksum
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
	private static void addWhitelistValue(TrustPolicy trustPolicy)
			throws IOException {
		Whitelist whitelist = trustPolicy.getWhitelist();
		List<Measurement> measurements = whitelist.getMeasurements();
		String imageId = trustPolicy.getImage().getImageId();
		DirectoryAndFileUtil dirFileUtil = new DirectoryAndFileUtil();
		List<String> invalidFiles = new ArrayList<>();
		// Initialize cumulative hash
		Digest cumulativeDigest = Digest.algorithm(
				whitelist.getDigestAlg().value()).zero();

		// Get file and directory hash and extend value to cumulative hash
		for (Measurement measurement : measurements) {
			Digest hash;
			if (measurement instanceof DirectoryMeasurement) {
				hash = dirFileUtil.getDirectoryHash(imageId,
						(DirectoryMeasurement) measurement, whitelist
								.getDigestAlg().value());
			} else if (measurement instanceof FileMeasurement) {
				hash = dirFileUtil.getFileHash(imageId,
						(FileMeasurement) measurement, whitelist.getDigestAlg()
								.value());
				if (hash == null) {
					invalidFiles.add(measurement.getPath());
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
