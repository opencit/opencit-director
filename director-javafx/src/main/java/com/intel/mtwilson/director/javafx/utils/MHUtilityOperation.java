package com.intel.mtwilson.director.javafx.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.intel.mtwilson.director.javafx.ui.Constants;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author root
 */
public class MHUtilityOperation {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MHUtilityOperation.class);
       
    private ConfigProperties configProperties;
    private String mhKeyName;
    //private String mhJarLocation = ConfigProperties.getProperty(Constants.MH_JAR_LOCATION);
    private String mhJarLocation;
    private String keystorePasswd;
    private String keystoreLocation;
    private String tlsSSLPasswd;
    private String KMSServerIP;

    public MHUtilityOperation() {
        configProperties = new ConfigProperties();
        mhJarLocation = "/opt/trustdirector/java/client-0.1-SNAPSHOT-with-dependencies.jar";
        keystorePasswd = configProperties.getProperty(Constants.MH_KEYSTORE_PASSWD);
        keystoreLocation = configProperties.getProperty(Constants.MH_KEYSTORE_LOCATION);
        tlsSSLPasswd = configProperties.getProperty(Constants.MH_TLS_SSL_PASSWD);
        KMSServerIP = configProperties.getProperty(Constants.KMS_SERVER);
    }

    public String encryptImage(Map<String, String> confInfo) {
        MHUtilityOperation mhOptImage = new MHUtilityOperation();
        String message = null;
        // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
        String mhKeyName = configProperties.getProperty(Constants.MH_KEY_NAME);
        String encryptedImageLocation = mhOptImage.startMHProcess(confInfo.get(Constants.IMAGE_LOCATION), mhKeyName);
        //String encryptedImageLocation = mhOptImage.encryptFile(confInfo.get(Constants.IMAGE_LOCATION), opensslPassword);
        if (encryptedImageLocation == null) {
            message = "Error while Uploading the key to KMS..... Exiting.....";
        }
        confInfo.put(Constants.MH_DEK_URL_IMG, mhOptImage.getDekURL());
        confInfo.put(Constants.Enc_IMAGE_LOCATION, encryptedImageLocation);
        if (confInfo.containsKey(Constants.KERNEL_PATH) && confInfo.containsKey(Constants.INITRD_PATH)) {
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
        }
        return message;
    }

    public String startMHProcess(String fileLocation, String mhKeyName) {
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
    }
    
    // Encrypt the file using openssl aes
    public String encryptFile(String location, String password) {
        String command = "openssl enc -aes-128-ofb -in " + location + " -out " + location + "-enc" + " -pass pass:" + password;
        int exitCode = new MountVMImage().callExec(command);
        if(exitCode != 0) {
            log.debug("Error while encrypting the file .....");
            return null;
        }
        return location+"-enc";
    }
    
    // Returns the Decryption URL
    public String getDekURL() {
    	return "https://" + KMSServerIP + ":8443/v1/data-encryption-key/request/" + this.mhKeyName;
    }
    
    // Generates the random password which to be used for image encryption
    private String getRandomPassword() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
    
    public String parseFile(String location) {
        Path path = Paths.get(location);
        String id = null;
        try {
            Scanner scanner = new Scanner(path);
            scanner.useDelimiter(System.getProperty("line.separator"));
            while(scanner.hasNext()){
                String line = scanner.next();
                if(line.contains("trustedCertEntry")) {
                    id = line.split(",")[0];
		    break;
                }
            }
        } catch (IOException ex) {
            log.error(null, ex);
        }
        return id;
    }
    
    
}
