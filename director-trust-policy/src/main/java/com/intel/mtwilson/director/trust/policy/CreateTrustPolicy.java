/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.trust.policy;

import com.intel.mtwilson.trustpolicy2.xml.SymlinkMeasurement;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.director.common.exception.DirectorException;
import com.intel.mtwilson.director.features.director.kms.KeyContainer;
import com.intel.mtwilson.director.features.director.kms.KmsUtil;
import com.intel.mtwilson.trustpolicy2.xml.Checksum;
import com.intel.mtwilson.trustpolicy2.xml.DecryptionKey;
import com.intel.mtwilson.trustpolicy2.xml.DigestAlgorithm;
import com.intel.mtwilson.trustpolicy2.xml.DirectoryMeasurement;
import com.intel.mtwilson.trustpolicy2.xml.Encryption;
import com.intel.mtwilson.trustpolicy2.xml.FileMeasurement;
import com.intel.mtwilson.trustpolicy2.xml.ImageHash;
import com.intel.mtwilson.trustpolicy2.xml.Measurement;
import com.intel.mtwilson.trustpolicy2.xml.TrustPolicy;
import com.intel.mtwilson.trustpolicy2.xml.Whitelist;
import java.util.Iterator;

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

    public CreateTrustPolicy(String imageId) {
        this.imageId = imageId;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(CreateTrustPolicy.class);

    /**
     * creates trust policy 1) Adds whitelist value for files, directory and
     * cumulative hash 2) Calls KMS to gets encryption key and adds DEK url in
     * trust policy
     *
     * @param trustPolicy contains information about what is selected by user
     * from UI such as launch control and encryption policy, files and
     * directories.
     * @throws CryptographyException
     * @throws IOException
     */
    public void createTrustPolicy(TrustPolicy trustPolicy)
            throws DirectorException {
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
     * @param trustPolicy
     * @return
     * @throws XMLStreamException
     * @throws JAXBException
     * @throws IOException
     * @throws CryptographyException
     */
    private void addEncryption(TrustPolicy trustPolicy)
            throws DirectorException {
        // get DEK
        KmsUtil kmsUtil;
        try {
            kmsUtil = new KmsUtil();
        } catch (Exception e1) {
            log.error("Error in initialization of KMS Util {}", e1);
            throw new DirectorException("Error initializing KMSUtil", e1);
        }

        //Check if a key already exists
        //If so, check if the key is still valid
        //If so, return
        if (StringUtils.isNotBlank(trustPolicy.getEncryption().getKey().getValue()) && trustPolicy.getEncryption().getKey().getValue().contains("keys")) {
            String url = trustPolicy.getEncryption().getKey().getValue();
            String keyIdFromUrl = getKeyIdFromUrl(url);
            String keyFromKMS = kmsUtil.getKeyFromKMS(keyIdFromUrl);
            if (StringUtils.isNotBlank(keyFromKMS)) {
                log.info("Existing key is still valid. Not creating a new one.");
                return;
            }
        }

        KeyContainer key;
        try {
            key = kmsUtil.createKey();
        } catch (CryptographyException e) {
            throw new DirectorException("Error creating key in KMS", e);
        }
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
    }

    /**
     * 1) Adds whitelist value for file and directory. File hash is a hash of
     * the content of file. Directory hash is the hash of list of files which
     * satisfies include and exclude criteria of a directory. 2) Adds cumulative
     * hash which is extended hash of files and directories hashes
     *
     * @param trustPolicy
     * @return
     * @throws DirectorException
     */
    private void addWhitelistValue(TrustPolicy trustPolicy) throws DirectorException {
        Whitelist whitelist = trustPolicy.getWhitelist();
        List<Measurement> measurements = whitelist.getMeasurements();
        DirectoryAndFileUtil dirFileUtil = new DirectoryAndFileUtil(imageId);
        //List<String> invalidFiles = new ArrayList<>();
        // Initialize cumulative hash
        Digest cumulativeDigest = Digest.algorithm(
                whitelist.getDigestAlg().value()).zero();

        // Get file and directory hash and extend value to cumulative hash
        Digest hash = null;
        for (Iterator<Measurement> it = measurements.iterator(); it.hasNext();) {
            Measurement measurement = it.next();
            hash = null;
            if (measurement instanceof DirectoryMeasurement) {
                try {
                    hash = dirFileUtil.getDirectoryHash(imageId,
                            (DirectoryMeasurement) measurement, whitelist
                            .getDigestAlg().value());
                } catch (IOException e) {
                    log.warn("No directory found for measurement " + measurement.getPath());
//                    throw new DirectorException("No directory found for measurement " + measurement.getPath(), e);
                }
            } else if (measurement instanceof FileMeasurement) {
                try {
                    hash = dirFileUtil.getFileHash(imageId,
                            (FileMeasurement) measurement, whitelist.getDigestAlg()
                            .value());
                } catch (IOException e) {
                    log.warn("No file found for measurement " + measurement.getPath());
//                    throw new DirectorException("No file found for measurement " + measurement.getPath(), e);
                }
            } else if (measurement instanceof SymlinkMeasurement) {
                try {
                    hash = dirFileUtil.getSymlinkHash(imageId,
                            (SymlinkMeasurement) measurement, whitelist
                            .getDigestAlg().value());
                } catch (IOException e) {
                    log.warn("No Symlink found for measurement " + measurement.getPath());
//                    throw new DirectorException("No Symlink found for measurement " + measurement.getPath(), e);
                }
            } else {
                log.error("Unsupported measurement type");
                return;
            }
            if (hash == null) {
                it.remove();
                continue;
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

    private String getKeyIdFromUrl(String url) {
        log.debug("URL :: " + url);
        String[] split = url.split("/");
        int index = 0;
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("keys")) {
                log.debug("Keys index :: " + i);
                index = ++i;
                break;
            }
        }

        log.debug("Index :: " + index);

        if (index != 0) {
            return split[index];
        } else {
            return null;
        }

    }
}
