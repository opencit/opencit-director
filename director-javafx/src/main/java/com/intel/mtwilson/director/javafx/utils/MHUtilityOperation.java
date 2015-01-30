package com.intel.mtwilson.director.javafx.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
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
    private static final Logger logger = Logger.getLogger(MHUtilityOperation.class.getName());
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }
    
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
        mhJarLocation = "./resources/client-0.1-SNAPSHOT-with-dependencies.jar";
        keystorePasswd = configProperties.getProperty(Constants.MH_KEYSTORE_PASSWD);
        keystoreLocation = configProperties.getProperty(Constants.MH_KEYSTORE_LOCATION);
        tlsSSLPasswd = configProperties.getProperty(Constants.MH_TLS_SSL_PASSWD);
        KMSServerIP = configProperties.getProperty(Constants.KMS_SERVER_IP);
    }

    public String startMHProcess(String fileLocation, String mhKeyName) {
        mhKeyName += new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	this.mhKeyName = mhKeyName;
	System.out.println("MH Keystore Location : " + keystoreLocation);
        String expScriptName = "./resources/login";
        MountVMImage obj = new MountVMImage();
        FileUtilityOperation fileOpt = new FileUtilityOperation();
        int exitCode;
        String randomPassword = getRandomPassword();
        String encLocation = encryptFile(fileLocation, randomPassword);
        if(encLocation == null) {
            return null;
        }
        File passFileLocation = new File("./vmpass.txt");
        fileOpt.writeToFile(passFileLocation, randomPassword, false);
        
        //String mhJarLocation = "/root/mhagent/client-0.1-SNAPSHOT-with-dependencies.jar";
        //String keyName = "mydemokey123";
        
        // Encrypt the VM password and send it to the key management service with a label�?
        String command = "java -jar " + mhJarLocation + " import-data-encryption-key " + mhKeyName + " " + passFileLocation;
        File tempCommandFile = new File("./runme");
        fileOpt.writeToFile(tempCommandFile, command, false);
        
        String expScriptCommand = expScriptName + " " + tempCommandFile + " " + randomPassword;
        logger.info(expScriptCommand);
        System.out.println(command);
        System.out.println(expScriptCommand);
        //String command = "cd /root/mhagent/.mhclient;java -jar" + jarLocation + " import-data-encryption-key " + keyName + passFileLocation;
        //callExec(expCommand);
        exitCode = obj.callExec(expScriptCommand);
        System.out.println("Exit code is : " + exitCode);
        //if(exitCode != 0) {
            //System.out.println("Exit code is : " + exitCode);
            //return null;
        //}
        
        // Prepare the key for sending to the key management server
//        String keystore = "/root/mhagent/.mhclient/dek-recipients.jks";
//	String keystore = "./dek-recipients.jks";
        command = "keytool -keystore " + keystoreLocation + " -storepass " + keystorePasswd + " -list";
        System.out.println("---- " + command + "----");
        exitCode = obj.callExec(command);
        System.out.println("Exit code is : " + exitCode);
        //if(exitCode != 0) {
            //return null;
        //}
        //File keytoolOutput = new File("./keytool_output");
        //fileOpt.writeToFile(keytoolOutput, String.valueOf(output));
        //String id = parseFile(keytoolOutput.getAbsolutePath());
        String id = parseFile(Constants.EXEC_OUTPUT_FILE);

        System.out.println(id);
        //id = "d18b96c4b3e01728aa79621f20ceba67";
        
        command = "java -jar " + mhJarLocation + " wrap-data-encryption-key " + mhKeyName + " " + id;
        fileOpt.writeToFile(tempCommandFile, command, false);
        expScriptCommand = expScriptName + " " + tempCommandFile + " " + randomPassword + " " + tlsSSLPasswd;
        System.out.println(command);
        System.out.println(expScriptCommand);
        exitCode = obj.callExec(expScriptCommand);
        System.out.println("Exit code is : " + exitCode);
        //if(exitCode != 0) {
            //return null;
        //}

        command = "java -jar " + mhJarLocation + " post-data-encryption-key " + mhKeyName + " " + id + " https://" + KMSServerIP + ":8443";
        fileOpt.writeToFile(tempCommandFile, command, false);
        expScriptCommand = expScriptName + " "+ tempCommandFile + " " + keystorePasswd;
        System.out.println(command);
        System.out.println(expScriptCommand);
        exitCode = obj.callExec(expScriptCommand);
        System.out.println("Exit code is : " + exitCode);
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
            //System.out.println("Error while encrypting the file .....");
            logger.log(Level.SEVERE, "Error while encrypting the file .....");
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
                    System.out.println();
                    System.out.println(line.split(",")[0]);
                    System.out.println();
                    id = line.split(",")[0];
		    break;
                }
                //System.out.println("Lines: "+scanner.next());
            }
        } catch (IOException ex) {
            //Logger.getLogger(MHUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
    
    
}
