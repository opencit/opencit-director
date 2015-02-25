/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.director.javafx.ui.Constants;
import com.intel.mtwilson.director.javafx.ui.Directories;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy.*;
import java.util.List;
import java.util.Map;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy.Image.ImageHash;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy.Whitelist.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

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
    public String createTrustPolicy(List<Directories> directories, Map<String, String> configInfo){
        //Initialize schema
        TrustPolicy trustpolicy = new TrustPolicy();
        //Set customerId
        String customerId = configProperties.getProperty(Constants.CONF_CUSTOMER_ID);
        if(customerId == null || customerId.equals("")){
            UUID uuid = new UUID();
            customerId = uuid.toString();
            configProperties.setProperty(customerId, Constants.CONF_CUSTOMER_ID);
        }
        System.out.println("Customer ID is:"+ customerId);
        Director director = new Director();
        director.setCustomerId(customerId);
        trustpolicy.setDirector(director);
        
        //Set encryption
        if(configInfo.get(Constants.IS_ENCRYPTED).equals("true")){
            Encryption encryption = new Encryption();
            encryption.setDekKey(configInfo.get(Constants.MH_DEK_URL_IMG));
            
            try {
                encryption.setChecksum(computeHash(MessageDigest.getInstance("MD5"), new java.io.File(configInfo.get("Image Location"))));
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
            }
            trustpolicy.setEncryption(encryption);
        }
        System.out.println("Encryption::"+configInfo.get(Constants.IS_ENCRYPTED)+" \n DEK URL::"+configInfo.get(Constants.MH_DEK_URL_IMG));
        //Set Launch control policy
        trustpolicy.setLaunchControlPolicy(configInfo.get(Constants.POLICY_TYPE));
        System.out.println("Launch Control Policy is: "+configInfo.get(Constants.POLICY_TYPE));
        
        //Set Image
        Image image = new Image();
        image.setImageId(configInfo.get(Constants.IMAGE_ID));

        //Set whitelist
        Whitelist whitelist = createWhitelist(directories, configInfo, image);
        trustpolicy.setWhitelist(whitelist);
 
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
        List<Dir> whitelistDir = whitelist.getDir();
        List<File> whiltelistFile = whitelist.getFile();
//        String mountPath = Constants.MOUNT_PATH;
        if(Boolean.valueOf(configInfo.get(Constants.BARE_METAL)))
        mountPath="";
        System.out.println("Hash type is ::::::::::"+configInfo.get(Constants.HASH_TYPE));
        ImageHash imageHash = new ImageHash();
        try {
            //set digest algorithm
            switch (configInfo.get(Constants.HASH_TYPE)) {
                case "SHA-256":
                    digestSha256 = new Sha256Digest(new byte[] {0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0});
                    System.out.println("sha256");
                    md = MessageDigest.getInstance("SHA-256");
                    whitelist.setDigestAlg("sha256");
                    imageHash.setDigestAlg("sha256");
                    break;
                case "SHA-1":
                    digestSha1 = Sha1Digest.ZERO;
                    System.out.println("sha1");
                    md = MessageDigest.getInstance("SHA-1");
                    whitelist.setDigestAlg("sha1");
                    imageHash.setDigestAlg("sha1");
                    break;
                default:
                    digestSha1 = Sha1Digest.ZERO;
                    System.out.println("sha1");
                    md = MessageDigest.getInstance("SHA-1");
                    whitelist.setDigestAlg("sha1");
                    imageHash.setDigestAlg("sha1");
                    break;
            }
        }catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GenerateTrustPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        String fileList = "";
        //iterate throught each directory
        for (Directories directory : directories){
            Dir dir = new Dir();
            //set include exclude attribute
            dir.setPath(directory.getCbox().getText());
            
            
            String excludeFileList = Constants.EXCLUDE_FILE_NAME;
            String cmd = "cat "+excludeFileList+" | grep -E '^("+dir.getPath()+")'";
            String exclude = executeShellCommand(cmd);
            if(exclude != null && !exclude.isEmpty()){
                exclude = exclude.replaceAll("\\n", "|");
                exclude = "("+exclude+")";
                dir.setExclude(exclude);
            }
            System.out.println("Exclude tag is::::: "+exclude);


            String filter = directory.getChoice().getValue().toString();
            String findCmd = "find "+mountPath+dir.getPath()+" ! -type d";
            switch (filter){
                case "All Files":
                    whitelist.setDigestAlg("sha256");
                    if(dir.getExclude() != null){
                        findCmd += " | grep -vE '"+dir.getExclude()+"'";
                    }
                    break;
                case "Binaries":
                    whitelist.setDigestAlg("sha1");
                    break;
                default:
                    String include = directory.getTfield().getText();
                    if(include != null && !include.equals("")){
                        dir.setInclude(include);
                    }
                    findCmd += "| grep -E '"+include+"'";
                    if(dir.getExclude() != null){
                        findCmd += " | grep -vE '"+dir.getExclude()+"'";
                    }
                    break;
            }
            System.out.println("Find Command is::: "+findCmd);
            String fileListForDir = executeShellCommand(findCmd);
            //System.out.println("file list is::: "+fileListForDir);

            if(fileListForDir == null){
                dir.setValue(computeHash(md, ""));
                whitelistDir.add(dir);
                continue;
            }
            //add the directory to whitelist, create include and exclude attribute
            dir.setValue(computeHash(md, fileListForDir));
            whitelistDir.add(dir);
            fileList = fileList + fileListForDir + "\n";
            
            //Extend image hash
            if(digestSha1 != null)
                digestSha1.extend(Sha1Digest.valueOfHex(dir.getValue()));
            else if(digestSha256 != null)
                digestSha256.extend(Sha256Digest.valueOfHex(dir.getValue()).toByteArray());
            else{}
        }
        fileList = fileList.replaceAll("\\n$", "");
        //Replace file path with symbolic link if any and add each file to whitelist
        String files[] = fileList.split("\\n");
        for (String file : files) {
//                String symLinkValue = getSymlinkValue(file,mountPath);
//                if(symLinkValue != null){
//                    if(!symLinkValue.startsWith(mountPath))
//                        symLinkValue = mountPath+symLinkValue;
//                    fileListForDir = fileListForDir.replace(file, symLinkValue);
//                    file = symLinkValue;
//                }
            String symLink = getSymlinkValue(file);
            if (!(new java.io.File(symLink).exists())) {
                continue;
            }
            File newFile = new File();
            newFile.setPath(file.replace(mountPath, ""));
            newFile.setValue(computeHash(md, new java.io.File(symLink)));
            whiltelistFile.add(newFile);
            //Extend image hash
            if(digestSha1 != null)
                digestSha1.extend(Sha1Digest.valueOfHex(newFile.getValue()));
            else if(digestSha256 != null)
                digestSha256.extend(Sha256Digest.valueOfHex(newFile.getValue()).toByteArray());
            else{}
        }
        if(digestSha1 != null)
            imageHash.setValue(digestSha1.toHexString());
        else if(digestSha256 != null)
            imageHash.setValue(digestSha256.toHexString());
        else{}
        return whitelist;
    }
    
    //Executes command and return results
    //Review: you may  want to use java matcher instead of pipe grep
    private String executeShellCommand(String command){
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
}
