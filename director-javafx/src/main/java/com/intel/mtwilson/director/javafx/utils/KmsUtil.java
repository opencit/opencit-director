package com.intel.mtwilson.director.javafx.utils;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.crypto.file.RsaPublicKeyProtectedPemKeyEnvelopeOpener;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.pem.Pem;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Scanner;
import com.intel.mtwilson.director.javafx.ui.Constants;
import com.intel.kms.api.*;
import com.intel.kms.client.jaxrs2.Keys;
import com.intel.kms.client.jaxrs2.Users;
import com.intel.kms.user.User;
import com.intel.kms.ws.v2.api.Key;
import static com.intel.mtwilson.Environment.keys;
import com.intel.mtwilson.Folders;
import static com.intel.mtwilson.configuration.ConfigurationFactory.getConfiguration;
import com.intel.mtwilson.configuration.PasswordVaultFactory;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import java.io.File;
import java.io.FileNotFoundException;
import static java.lang.Math.log;
import java.net.URL;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Properties;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author root
 */
public class KmsUtil {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KmsUtil.class);
    private static final String DIRECTOR_ENVELOPE_ALIAS = "director.envelope.alias";
    private static final String DIRECTOR_KEYSTORE = "director.keystore";
    private static final String DIRECTOR_KEYSTORE_PASSWORD = "director.keystore.password";
    private static final String KMS_ENDPOINT_URL = "kms.endpoint.url";
    private static final String KMS_TLS_POLICY_CERTIFICATE_SHA1 = "kms.tls.policy.certificate.sha1";
    private static final String KMS_LOGIN_BASIC_USERNAME = "kms.login.basic.username";
    private static final String KMS_LOGIN_BASIC_PASSWORD = "kms.login.basic.password";

    private ConfigProperties configProperties;
    private String mhKeyName;
    //private String mhJarLocation = ConfigProperties.getProperty(Constants.MH_JAR_LOCATION);
//    private String mhJarLocation;
//    private String keystorePasswd;
//    private String keystoreLocation;
//    private String tlsSSLPasswd;
//    private String KMSServerIP;

    public KmsUtil()throws Exception {
        configProperties = new ConfigProperties();
//        mhJarLocation = "/opt/trustdirector/java/client-0.1-SNAPSHOT-with-dependencies.jar";
//        keystorePasswd = configProperties.getProperty(Constants.MH_KEYSTORE_PASSWD);
//        keystoreLocation = configProperties.getProperty(Constants.MH_KEYSTORE_LOCATION);
//        tlsSSLPasswd = configProperties.getProperty(Constants.MH_TLS_SSL_PASSWD);
//        KMSServerIP = configProperties.getProperty(Constants.KMS_SERVER);
    }

    public void encryptImage(Map<String, String> confInfo) throws Exception {
        KmsUtil kms = new KmsUtil();
        KeyContainer keyContainer = kms.getEncryptionKeyFromKms();
        String encryptedImageLocation = encryptFile(confInfo.get(Constants.IMAGE_LOCATION), Base64.encodeBase64String(keyContainer.secretKey.getEncoded()));
        confInfo.put(Constants.MH_DEK_URL_IMG, keyContainer.url.toString());
        confInfo.put(Constants.Enc_IMAGE_LOCATION, encryptedImageLocation);
        /*if (confInfo.containsKey(Constants.KERNEL_PATH) && confInfo.containsKey(Constants.INITRD_PATH)) {
         MHUtilityOperation mhOptKernel = new MHUtilityOperation();

         // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
         mhKeyName = configProperties.getProperty(Constants.MH_KEY_NAME) + "-kernel";
         String encryptedKernelPath = mhOptKernel.startMHProcess(confInfo.get(Constants.KERNEL_PATH), mhKeyName);
         //String encryptedKernelPath = mhOptImage.encryptFile(confInfo.get(Constants.KERNEL_PATH), opensslPassword);
         if (encryptedKernelPath == null) {
         message = "Error while Uploading the key to KMS..... Exiting.....";
         }

         MHUtilityOperation mhOptInitrd = new MHUtilityOperation();

         // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
         mhKeyName = configProperties.getProperty(Constants.MH_KEY_NAME) + "-initrd";
         String encryptedInitrdPath = mhOptInitrd.startMHProcess(confInfo.get(Constants.INITRD_PATH), mhKeyName);
         //String encryptedInitrdPath = mhOptImage.encryptFile(confInfo.get(Constants.INITRD_PATH), opensslPassword);
         if (encryptedInitrdPath == null) {
         message = "Error while Uploading the key to KMS..... Exiting.....";
         }
         confInfo.put(Constants.MH_DEK_URL_KERNEL, mhOptKernel.getDekURL());
         confInfo.put(Constants.MH_DEK_URL_INITRD, mhOptInitrd.getDekURL());
         confInfo.put(Constants.Enc_KERNEL_PATH, encryptedKernelPath);
         confInfo.put(Constants.Enc_INITRD_PATH, encryptedInitrdPath);
         }*/
    }

    public KeyContainer getEncryptionKeyFromKms() throws Exception {
        Password keystorePassword = null;
        PublicKey directorEnvelopePublicKey;
        String kmsEndpointUrl;
        String kmsTlsPolicyCertificateSha1;
        String kmsLoginBasicUsername;
        String kmsLoginBasicPassword ;
        Keys keys ;

        //Get director envelope key
        String directorEnvelopeAlias = getConfiguration().get(DIRECTOR_ENVELOPE_ALIAS, "director-envelope");
        if( directorEnvelopeAlias == null || directorEnvelopeAlias.isEmpty() ) {
            throw new ConfigurationNotFoundException("Trust Director Envelope alias not configured");
        }
        
        String keystorePath = getConfiguration().get(DIRECTOR_KEYSTORE, Folders.configuration() + File.separator + "keystore.jks");
        File keystoreFile = new File(keystorePath);
        if( !keystoreFile.exists() ) {
            throw new ConfigurationNotFoundException("Director Keystore file does not exist");
        }
        
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            if( passwordVault.contains(DIRECTOR_KEYSTORE_PASSWORD)) {
                keystorePassword = passwordVault.get(DIRECTOR_KEYSTORE_PASSWORD);
            }
        }
        if( keystorePassword == null || keystorePassword.toCharArray().length == 0) {
            throw new ConfigurationNotFoundException("Director Keystore password is not configured");
        }
        
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        RsaCredentialX509 wrappingKeyCertificate;
        try {
            wrappingKeyCertificate = keystore.getRsaCredentialX509(directorEnvelopeAlias, keystorePassword);
            log.debug("Found key {}", wrappingKeyCertificate.getCertificate().getSubjectX500Principal().getName());
            
            directorEnvelopePublicKey = wrappingKeyCertificate.getPublicKey();
            if ( directorEnvelopePublicKey == null ) {
                log.error("Trust Director envelope public key is not configured");
            }
        } catch (FileNotFoundException e) {
            log.error("Keystore does not contain the specified key %s", directorEnvelopeAlias);
            throw new FileNotFoundException(e);
        }
        catch(java.security.UnrecoverableKeyException e) {
            log.debug("Incorrect password for existing key: {}", e.getMessage());
            log.error("Key must be recreated");
            throw new java.security.UnrecoverableKeyException(e);
        }
        
        
        //Collect KMS configurations
        kmsEndpointUrl = getConfiguration().get(KMS_ENDPOINT_URL, null);
        if( kmsEndpointUrl == null || kmsEndpointUrl.isEmpty() ) {
            throw new ConfigurationNotFoundException("KMS endpoint URL not configured");
        }
        
        kmsTlsPolicyCertificateSha1 = getConfiguration().get(KMS_TLS_POLICY_CERTIFICATE_SHA1, null);
        if( kmsTlsPolicyCertificateSha1 == null || kmsTlsPolicyCertificateSha1.isEmpty() ) {
            throw new ConfigurationNotFoundException("KMS TLS policy certificate digest not configured");
        }
        
        kmsLoginBasicUsername = getConfiguration().get(KMS_LOGIN_BASIC_USERNAME, null);
        if( kmsLoginBasicUsername == null || kmsLoginBasicUsername.isEmpty() ) {
            throw new ConfigurationNotFoundException("KMS API username not configured");
        }
        
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            if( passwordVault.contains(KMS_LOGIN_BASIC_PASSWORD)) {
                kmsLoginBasicPassword = new String(passwordVault.get(KMS_LOGIN_BASIC_PASSWORD).toCharArray());
            }
            else 
                kmsLoginBasicPassword = null;
        }
//        kmsLoginBasicPassword = getConfiguration().get(KMS_LOGIN_BASIC_PASSWORD, kmsLoginBasicPassword);
        if( kmsLoginBasicPassword == null || kmsLoginBasicPassword.isEmpty() ) {
            throw new ConfigurationNotFoundException("KMS API password not configured");
        }
        // create KMS Keys API client
        Properties properties = new Properties();
        properties.setProperty("endpoint.url", kmsEndpointUrl);
        properties.setProperty("tls.policy.certificate.sha1", kmsTlsPolicyCertificateSha1);
        properties.setProperty("login.basic.username", kmsLoginBasicUsername);
        properties.setProperty("login.basic.password", kmsLoginBasicPassword);
        keys = new Keys(properties);

        // Request server to create a new key        
        CreateKeyRequest createKeyRequest = new CreateKeyRequest();
        createKeyRequest.setAlgorithm("AES");
        createKeyRequest.setKeyLength(128);
        createKeyRequest.setMode("OFB");
        Key createKeyResponse = keys.createKey(createKeyRequest);
        // Request server to transfer the new key to us (encrypted)
        String transferKeyPemResponse = keys.transferKey(createKeyResponse.getId().toString());
        // decrypt the requested key
        RsaPublicKeyProtectedPemKeyEnvelopeOpener opener = new RsaPublicKeyProtectedPemKeyEnvelopeOpener(wrappingKeyCertificate.getPrivateKey(), kmsLoginBasicUsername);
        SecretKey secretKey = (SecretKey) opener.unseal(Pem.valueOf(transferKeyPemResponse));
        // package all these into a single container
        KeyContainer keyContainer = new KeyContainer();
        keyContainer.secretKey = secretKey;
        keyContainer.url = createKeyResponse.getTransferLink();
        keyContainer.attributes = createKeyResponse;
        return keyContainer;
    }

    public static class KeyContainer {

        public SecretKey secretKey;
        public URL url;
        public Key attributes;
    }

    /*   public String startMHProcessOld(String fileLocation, String mhKeyName) {
     mhKeyName += new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
     this.mhKeyName = mhKeyName;
     log.debug("MH Keystore Location : " + keystoreLocation);
     String expScriptName = "/opt/trustdirector/bin/login";
     MountVMImage obj = new MountVMImage();
     FileUtilityOperation fileOpt = new FileUtilityOperation();
     int exitCode;
     String randomPassword = getRandomPassword();
     String encLocation = encryptFile(fileLocation, randomPassword);
     if (encLocation == null) {
     return null;
     }
     //TODO look at vmpass
     File passFileLocation = new File("/opt/trustdirector/vmpass.txt");
     fileOpt.writeToFile(passFileLocation, randomPassword, false);

     //String mhJarLocation = "/root/mhagent/client-0.1-SNAPSHOT-with-dependencies.jar";
     //String keyName = "mydemokey123";
     // Encrypt the VM password and send it to the key management service with a label�?
     String command = "java -jar " + mhJarLocation + " import-data-encryption-key " + mhKeyName + " " + passFileLocation;
     File tempCommandFile = new File("/opt/trustdirector/runme");
     fileOpt.writeToFile(tempCommandFile, command, false);

     String expScriptCommand = expScriptName + " " + tempCommandFile + " " + randomPassword;        
     log.debug(expScriptCommand);
     //String command = "cd /root/mhagent/.mhclient;java -jar" + jarLocation + " import-data-encryption-key " + keyName + passFileLocation;
     //callExec(expCommand);
     exitCode = obj.callExec(expScriptCommand);
     if (exitCode != 0) {
     throw new SetupException("KMS setup is not done properly. Error while executing this command: " + "java -jar " + mhJarLocation + " import-data-encryption-key " + mhKeyName + " " + randomPassword + " Exit code: " + exitCode);
     }
     log.debug("Exit code is : " + exitCode);
     //if(exitCode != 0) {
     //log.debug("Exit code is : " + exitCode);
     //return null;
     //}
        
     // Prepare the key for sending to the key management server
     //        String keystore = "/root/mhagent/.mhclient/dek-recipients.jks";
     //	String keystore = "./dek-recipients.jks";
     command = "keytool -keystore " + keystoreLocation + " -storepass " + keystorePasswd + " -list";
     log.debug("---- " + command + "----");
     exitCode = obj.callExec(command);
     if(exitCode != 0){
     throw new SetupException("KMS setup is not done properly. Error while executing this command: "+command + " Exit code: "+exitCode);
     }
     log.debug("Exit code is : " + exitCode);
     //if(exitCode != 0) {
     //return null;
     //}
     //File keytoolOutput = new File("./keytool_output");
     //fileOpt.writeToFile(keytoolOutput, String.valueOf(output));
     //String id = parseFile(keytoolOutput.getAbsolutePath());
     String id = parseFile(Constants.EXEC_OUTPUT_FILE);

     //        log.debug(id);
     //id = "d18b96c4b3e01728aa79621f20ceba67";
        
     command = "java -jar " + mhJarLocation + " wrap-data-encryption-key " + mhKeyName + " " + id;
     fileOpt.writeToFile(tempCommandFile, command, false);
     expScriptCommand = expScriptName + " " + tempCommandFile + " " + randomPassword + " " + tlsSSLPasswd;
     log.debug(command);
     log.debug(expScriptCommand);
     exitCode = obj.callExec(expScriptCommand);
     if(exitCode != 0){
     throw new SetupException("KMS setup is not done properly. Error while executing this command: "+command + " Exit code: "+exitCode);
     }
     log.debug("Exit code is : " + exitCode);
     //if(exitCode != 0) {
     //return null;
     //}

     command = "java -jar " + mhJarLocation + " post-data-encryption-key " + mhKeyName + " " + id + " https://" + KMSServerIP + ":8443";
     fileOpt.writeToFile(tempCommandFile, command, false);
     expScriptCommand = expScriptName + " "+ tempCommandFile + " " + keystorePasswd;
     log.debug(command);
     log.debug(expScriptCommand);
     exitCode = obj.callExec(expScriptCommand);
     if(exitCode != 0){
     throw new SetupException("KMS setup is not done properly. Error while executing this command: "+command + " Exit code: "+exitCode);
     }
     log.debug("Exit code is : " + exitCode);
     //if(exitCode != 0) {
     //return null;
     //}
        
     //fileOpt.deleteDir(passFileLocation);
     //fileOpt.deleteDir(tempCommandFile);
        
     return encLocation;
     }*/
    // Encrypt the file using openssl aes
    public String encryptFile(String location, String password) throws Exception {
        String command = "openssl enc -aes-128-ofb -in " + location + " -out " + location + "-enc" + " -pass pass:" + password;
        int exitCode = new MountVMImage().callExec(command);
        if (exitCode != 0) {
            log.error("Error while encrypting the file .....");
            throw new Exception("Can not encrypt image");
        }
        return location + "-enc";
    }

    // Generates the random password which to be used for image encryption
    private String getRandomPassword() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public String parseFile(String location) throws Exception{
        Path path = Paths.get(location);
        String id = null;
        Scanner scanner = new Scanner(path);
        scanner.useDelimiter(System.getProperty("line.separator"));
        while (scanner.hasNext()) {
            String line = scanner.next();
            if (line.contains("trustedCertEntry")) {
                id = line.split(",")[0];
                break;
            }
        }
        return id;
    }

}
