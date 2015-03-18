/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template filePath, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

//import com.intel.dcsg.cpg.crypto.Sha1Digest;
//import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.director.javafx.ui.Constants;
import com.intel.mtwilson.director.javafx.ui.Directories;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy.*;
import java.util.List;
import java.util.Map;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.manifest.xml.Manifest;
import com.intel.mtwilson.trustpolicy.xml.DirectoryMeasurementType;
import com.intel.mtwilson.trustpolicy.xml.FileMeasurementType;
import com.intel.mtwilson.trustpolicy.xml.MeasurementType;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy.Encryption.Key;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy.Image.ImageHash;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author boskisha
 */
public class GenerateTrustPolicy {
    private ConfigProperties configProperties;
    private String mountPath;
    //private String imageHash;
    public GenerateTrustPolicy(){
        configProperties = new ConfigProperties();
        mountPath = Constants.MOUNT_PATH;
    }
    
    public String createManifest(List<Directories> directories, Map<String, String> configInfo){
        if(Boolean.valueOf(configInfo.get(Constants.BARE_METAL_LOCAL)))
            mountPath="";
        Manifest manifest = new Manifest();
        //For bare Metal just supporting sha-1
        manifest.setDigestAlg("sha1");
        List<com.intel.mtwilson.manifest.xml.MeasurementType> manifestList = manifest.getManifest();
        
        for (Directories directory : directories){
            com.intel.mtwilson.manifest.xml.DirectoryMeasurementType dir = new com.intel.mtwilson.manifest.xml.DirectoryMeasurementType();
            //set include exclude attribute
            dir.setPath(directory.getCbox().getText());            
            
            String cmd = "cat "+Constants.EXCLUDE_FILE_NAME+" | grep -E '^("+dir.getPath()+")'";
            String exclude = executeShellCommand(cmd);
            if(exclude != null && !exclude.isEmpty()){
                exclude = exclude.replaceAll("\\n", "|");
                exclude = "("+exclude+")";
                dir.setExclude(exclude);
            }
            System.out.println("Exclude tag is::::: "+exclude);

            String findCmd = "find " + mountPath + dir.getPath() + " ! -type d";
            String include = directory.getTfield().getText();
            if (include != null && !include.equals("")) {
                dir.setInclude(include);
                findCmd += " | grep -E '" + include + "'";            
            }
            if (dir.getExclude() != null) {
                findCmd += " | grep -vE '" + dir.getExclude() + "'";
            }
            System.out.println("Find Command is::: " + findCmd);
            String fileListForDir = executeShellCommand(findCmd);
            //System.out.println("filePath list is::: "+fileListForDir);
            // add directory to manifest
            if (fileListForDir == null) {
                manifestList.add((com.intel.mtwilson.manifest.xml.MeasurementType) dir);
                continue;
            }
            
            manifestList.add((com.intel.mtwilson.manifest.xml.MeasurementType) dir);

            fileListForDir = fileListForDir.replaceAll("\\n$", "");
            //Replace filePath path with symbolic link if any and add each filePath to whitelist
            String files[] = fileListForDir.split("\\n");
            for (String file : files) {
                String symLink = getSymlinkValue(file);
                if (!(new java.io.File(symLink).exists())) {
                    continue;
                }
                com.intel.mtwilson.manifest.xml.FileMeasurementType newFile = new com.intel.mtwilson.manifest.xml.FileMeasurementType();
                newFile.setPath(file.replace(mountPath, ""));
                manifestList.add(newFile);
            }

        }
        JAXB jaxb = new JAXB();
        String result = null;
        try {
            result = jaxb.write(manifest);
        } catch (JAXBException ex) {
            //Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("TrustPolicy is::: "+result);
        return result;
    }
    
    
    public String createTrustPolicy(List<Directories> directories, Map<String, String> configInfo){
        //Initialize schema
        TrustPolicy trustpolicy = new TrustPolicy();
        //Set customerId
        String customerId = configProperties.getProperty(Constants.CONF_CUSTOMER_ID);
        if(customerId == null || customerId.equals("")){
            UUID uuid = new UUID();
            customerId = uuid.toString();
            //configProperties.setProperty(Constants.CONF_CUSTOMER_ID, customerId);            
        }
        System.out.println("Customer ID is:"+ customerId);
        Director director = new Director();
        director.setCustomerId(customerId);
        trustpolicy.setDirector(director);
        
        //Set Launch control policy
        trustpolicy.setLaunchControlPolicy(configInfo.get(Constants.POLICY_TYPE));
        System.out.println("Launch Control Policy is: "+configInfo.get(Constants.POLICY_TYPE));
        
        //Set Image
        Image image = new Image();
        image.setImageId(configInfo.get(Constants.IMAGE_ID));
               
        //Set whitelist and calculate cumulative hash
        Whitelist whitelist = createWhitelist(directories, configInfo, image);
        trustpolicy.setWhitelist(whitelist);
        trustpolicy.setImage(image);
 
        //create xml
        JAXB jaxb = new JAXB();
        String result = null;
        try {
            result = jaxb.write(trustpolicy);
        } catch (JAXBException ex) {
            Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("TrustPolicy is: "+result);
        return result;
    }
    
    //Creates whitelist and sets imageHash and digestAlg
    public Whitelist createWhitelist(List<Directories> directories, Map<String, String> configInfo, Image image){
        MessageDigest md = null;
        Sha1Digest digestSha1 = null;
        Sha256Digest digestSha256 = null;
        Whitelist whitelist = new Whitelist();
        if(Boolean.valueOf(configInfo.get(Constants.BARE_METAL_LOCAL)))
            mountPath="";
        System.out.println("Hash type is ::::::::::"+configProperties.getProperty(Constants.HASH_TYPE));
        ImageHash imageHash = new ImageHash();
        String opensslCmd ="";
        List<MeasurementType> whitelistValue = whitelist.getMeasurements();
        try {
            //set digest algorithm
            switch (configProperties.getProperty(Constants.HASH_TYPE)) {
                case "SHA-256":
                    digestSha256 = new Sha256Digest(new byte[] {0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0});
                    md = MessageDigest.getInstance("SHA-256");
                    whitelist.setDigestAlg("sha256");
                    imageHash.setDigestAlg("sha256");
                    opensslCmd = "openssl dgst -sha256";
                    break;
                case "SHA-1":                    
                default:
                    //digestSha1 = Sha1Digest.ZERO;
                    System.out.println("sha1");
                    md = MessageDigest.getInstance("SHA-1");
                    whitelist.setDigestAlg("sha1");
                    imageHash.setDigestAlg("sha1");
                    opensslCmd = "openssl dgst -sha1";
                    break;
            }
        }catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        //sort directories before adding it to trust policy so that list of files and directories added in trust policy is sorted
        Collections.sort(directories);
        //iterate throught each directory
        for (Directories directory : directories){
            DirectoryMeasurementType directoryWhitelist = new DirectoryMeasurementType();
            directoryWhitelist.setPath(directory.getCbox().getText());            
            
            //set exclude attribute
            String exclude = executeShellCommand("cat "+Constants.EXCLUDE_FILE_NAME+" | grep -E '^("+directoryWhitelist.getPath()+")'");
            if(exclude != null && !exclude.isEmpty()){
                exclude = exclude.replaceAll("\\n", "|");
                exclude = "("+exclude+")";
                directoryWhitelist.setExclude(exclude);
            }
            System.out.println("Exclude tag is::::: "+exclude);
            //create command to get list of files from directory that matches specified filter criteria
            String getFilesCmd = "find " + mountPath + directoryWhitelist.getPath() + " ! -type d";            
            //set include attribute
            String include = directory.getTfield().getText();
            if (include != null && !include.equals("")) {
                directoryWhitelist.setInclude(include);
                getFilesCmd += "| grep -E '" + include + "'";            
            }
            if (directoryWhitelist.getExclude() != null) {
                getFilesCmd += " | grep -vE '" + directoryWhitelist.getExclude() + "'";
            }
            System.out.println("Find Command is::: " + getFilesCmd);
            String fileListForDir = executeShellCommand(getFilesCmd);
            
            //add the directory to whitelist
            directoryWhitelist.setValue(executeShellCommand(getFilesCmd+" | "+opensslCmd+"|awk '{print $2}'"));
            System.out.println("Directory hash command is&&&&&&&&&&&&&&&&&& " + getFilesCmd + " | " + opensslCmd + "|awk '{print $2}'" + "result is" + directoryWhitelist.getValue());
            whitelistValue.add((MeasurementType) directoryWhitelist);

            //Extend image hash to include directory
            switch (configProperties.getProperty(Constants.HASH_TYPE)) {
                case "SHA-256":
                    if (digestSha256 != null) {
                        System.out.println("Before extending hash is: " + digestSha256.toHexString());
                        digestSha256 = digestSha256.extend(directoryWhitelist.getValue().getBytes());
                        System.out.println("After extending " + directoryWhitelist.getValue() + " Extended hash is::" + digestSha256.toHexString());
                    } else {
                        digestSha256 = Sha256Digest.digestOf(directoryWhitelist.getValue().getBytes());
                    }
                    break;
                case "SHA-1":
                default:
                    if (digestSha1 != null) {
                        System.out.println("Before extending hash is: " + digestSha1.toHexString());
                        digestSha1 = digestSha1.extend(directoryWhitelist.getValue().getBytes());
                        System.out.println("After extending " + directoryWhitelist.getValue() + " Extended hash is::" + digestSha1.toHexString());
                    } else {
                        digestSha1 = Sha1Digest.digestOf(directoryWhitelist.getValue().getBytes());
                    }
            }
            if (fileListForDir == null) {
                continue;
            }
            fileListForDir = fileListForDir.replaceAll("\\n$", "");
            String files[] = fileListForDir.split("\\n");
            //Iterate through list of files and add each to whitelist and extend cumulative hash
            for (String filePath : files) {
            //Replace filePath path with symbolic link if any and add each filePath to whitelist
            String symLink = getSymlinkValue(filePath);
            if (!(new java.io.File(symLink).exists())) {
                continue;
            }
            //Create filePath tag
            FileMeasurementType newFile = new FileMeasurementType();
            newFile.setPath(filePath.replace(mountPath, ""));
            newFile.setValue(computeHash(md, new java.io.File(symLink)));
            whitelistValue.add((MeasurementType)newFile);
            //Extend file hash to cumulative image hash 
            
            switch (configProperties.getProperty(Constants.HASH_TYPE)) {
                case "SHA-256":
                    if (digestSha256 != null) {
                        System.out.println("Before extending hash is: " + digestSha256.toHexString());
                        digestSha256 = digestSha256.extend(newFile.getValue().getBytes());
                        System.out.println("After extending " + newFile.getValue() + " Extended hash is::" + digestSha256.toHexString());
                    } else {
                        digestSha256 = Sha256Digest.digestOf(newFile.getValue().getBytes());
                    }
                    break;
                case "SHA-1":
                default:
                    if (digestSha1 != null) {
                        System.out.println("Before extending hash is: " + digestSha1.toHexString());
                        digestSha1 = digestSha1.extend(newFile.getValue().getBytes());
                        System.out.println("After extending " + newFile.getValue() + " Extended hash is::" + digestSha1.toHexString());
                    } else {
                        digestSha1 = Sha1Digest.digestOf(newFile.getValue().getBytes());
                    }
            }            
        }
        if(digestSha1 != null)
            imageHash.setValue(digestSha1.toHexString());
        else if(digestSha256 != null)
            imageHash.setValue(digestSha256.toHexString());
        else{}
        }
        image.setImageHash(imageHash);
        return whitelist;    
    }
    
    //Executes command and return results
    //Review: you may  want to use java matcher instead of pipe grep
    public String executeShellCommand(String command){
        System.out.println("Command to execute is:"+command);
        String[] cmd = {
        "/bin/sh",
        "-c",
        command
        };
        Process p;
        String excludeList = null;
        try{
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null){
                result.append(line+"\n");
            }
            if(!result.toString().equals("")){
                excludeList = result.toString();
                excludeList = excludeList.replaceAll("\\n$", "");
            }
            //System.out.println("Result of execute command: "+result);            
        } catch (InterruptedException ex) {
            Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return excludeList;
    }
    
    public String setEncryption(String trustpolicyXml, Map<String, String> configInfo){
        //Set encryption
        if (configInfo.get(Constants.IS_ENCRYPTED).equals("true")) {
            try {
                JAXB jaxb = new JAXB();
                TrustPolicy trustpolicyObj = jaxb.read(trustpolicyXml, TrustPolicy.class);
                Encryption encryption = trustpolicyObj.getEncryption();
                if(encryption == null)
                    encryption = new Encryption();
                Key key = new Key();
                key.setURL("uri");
                key.setValue(configInfo.get(Constants.MH_DEK_URL_IMG));
                encryption.setKey(key);

                Encryption.Checksum checksum = new Encryption.Checksum();
                checksum.setDigestAlg("md5");
                checksum.setValue(computeHash(MessageDigest.getInstance("MD5"), new java.io.File(configInfo.get("Image Location"))));
                encryption.setChecksum(checksum);
                trustpolicyObj.setEncryption(encryption);
                trustpolicyXml = jaxb.write(trustpolicyObj);
            } catch (IOException ex) {
                Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JAXBException ex) {
                Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XMLStreamException ex) {
                Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Encryption::"+configInfo.get(Constants.IS_ENCRYPTED)+" \n DEK URL::"+configInfo.get(Constants.MH_DEK_URL_IMG));
        return trustpolicyXml;
    }
    // Finds the final target of the symbolic link returns null if oath is not a symbolic link
    private String getSymlinkValue(String filePath) {
        Path path = Paths.get(filePath);
        boolean isSymbolicLink = Files.isSymbolicLink(path);
        String symPath = null;
        if(isSymbolicLink){
            try {
                Path symLink = Files.readSymbolicLink(path);
                symPath = symLink.toString();
            } catch (IOException ex) {
                Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(symPath.startsWith(".") || symPath.startsWith("..") || !symPath.startsWith("/")){
                symPath = path.toFile().getParent() + "/" + symPath;
            }
            if(symPath.startsWith("/") && (!symPath.startsWith(mountPath))){
                symPath = mountPath+symPath;
            }  
            try {
                symPath = new java.io.File(symPath).getCanonicalPath();
                //System.out.println("Symbilic link value for '"+filePath+"' is: '"+symPath);
            } catch (IOException ex) {
                Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else
            symPath = filePath;
        return symPath;
    }
    
    // Calculate hash and return hash value
    public String computeHash(MessageDigest md, java.io.File file) {
        if(!file.exists())
            return null;
        StringBuffer sb = null;
        try {
            byte[] dataBytes = new byte[1024];
            int nread = 0;
            FileInputStream fis = new FileInputStream(file);
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            };
            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            fis.close();           
        } catch (IOException ex) {
            Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return sb.toString();
    }    
    
    public String computeHash(MessageDigest md, String text) {
        StringBuffer sb = null;
        try {
            md.update(text.getBytes("UTF-8")); 
            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return sb.toString();
    }    
    public String createSha256(java.io.File file) throws Exception  {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    InputStream fis = new FileInputStream(file);
    int n = 0;
    byte[] mdbytes = new byte[8192];
    while (n != -1) {
        n = fis.read(mdbytes);
        if (n > 0) {
            digest.update(mdbytes, 0, n);
        }
    }
    StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.digest().length; i++) {
                sb.append(Integer.toString((digest.digest()[i] & 0xff) + 0x100, 16).substring(1));
            }
        
        
        return sb.toString();
}
}
