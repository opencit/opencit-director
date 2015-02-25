/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.intel.mtwilson.director.javafx.ui.Constants;
import com.intel.mtwilson.director.javafx.ui.Directories;

/**
 *
 * @author admkrushnakant
 */
public class GenerateHash {
    private ConfigProperties configProperties;
    private static final Logger logger = Logger.getLogger(GenerateHash.class.getName());
    private List<String> allFiles = new ArrayList<String>();
    private static final String splitChar = "###";
    private String hashType;
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }
    
    public GenerateHash(){
       configProperties = new ConfigProperties();
       hashType=configProperties.getProperty(Constants.HASH_TYPE);
    }
    
    private String mountPath = Constants.MOUNT_PATH;
    private boolean isBareMetal;
    private String value = null;
    
    
    
    
    public String calculateHash(List<Directories> list, Map<String, String> confInfo) {
//        System.out.println("PSDebug  Calculate HASh");
        isBareMetal=Boolean.valueOf(confInfo.get(Constants.BARE_METAL));
        if(isBareMetal)
        {
            mountPath="/";
        }
        logger.info("Calculating hash of " + list.size() + " directories");
        
        Map<String, LinkedHashMap<String, String>> dirAndFilesMapping = new HashMap<String, LinkedHashMap<String, String>>();

        boolean isWindows = Boolean.valueOf(confInfo.get(Constants.IS_WINDOWS));
        if(isWindows) {
            mountPath = Constants.MOUNT_PATH + "/";
        }

        // Initialize MessageDigest
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(hashType);
         } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, null, md);
        }
        
        // Iterate through each directory
        for (Directories dirObject : list) {
            String dirPath="";
           if(isBareMetal)
        {
             dirPath = dirObject.getCbox().getText();
        }else{
               dirPath = mountPath + dirObject.getCbox().getText(); 
           }
//             dirPath = mountPath + dirObject.getCbox().getText();
//            System.out.println("PSDebug Directory Path:" + dirPath);
            File dir = new File(dirPath);
            String fileFormat = dirObject.getChoice().getValue().toString();
            String filter = dirObject.getTfield().getText();
//            System.out.println("PSDebug Filter is" + filter);
            //Check for hidden files to include or not
            boolean includeHiddenFiles = Boolean.valueOf(confInfo.get(Constants.HIDDEN_FILES));
//            System.out.println("PSDebug includeHiddenFiles" + includeHiddenFiles);
//            System.out.println("The param are" + ":" + dir + ":" + (filter.replace(".", "").split(";")) + ":" + includeHiddenFiles + ":" + isWindows);
            
            // Get the files from a directory recursively, this list will containg the files with its actual file appended with "###"(in case of symbolic links)
            //TODO boski change
            allFiles = getFilesFromDir(dir, fileFormat, filter, includeHiddenFiles, isWindows);
//            System.out.println("PSDebug Dir List All files:" +" "+ allFiles.toString());
            // Sort the list of files alphabetically
            Collections.sort(allFiles);
            
            // Calculate the hash
            logger.info("Number of Files selected for calculating the hash are : " + allFiles.size());
             
            Map<String, String> fileAndHash = new LinkedHashMap<String, String>();
            String fileHash = null;
            
            for(String str : allFiles) {
                fileHash = computeHash(md, new File(str.split(splitChar)[1]));
                fileAndHash.put(str.split(splitChar)[0], fileHash);
            }
            
            allFiles.clear();
            logger.info("Number of Files after calculating the hash are : " + fileAndHash.size());
            
            // Store in the map<DirName::filter, fileAndHash_Map>
            String dirPathAndFilter = dirPath + "::" + filter ;
            dirAndFilesMapping.put(dirPathAndFilter, (LinkedHashMap<String, String>) fileAndHash);
        }     
        
        // Write to the manifest file
        String fileLocation = new GenerateManifest().writeToXMLManifest(dirAndFilesMapping, confInfo);
//        System.out.println("Target file was Created at " + fileLocation);
        logger.info("String fileLocation val in Calculate HASh" + fileLocation);
//          new GenerateManifest().RetrieveFileHash(dirAndFilesMapping, confInfo);
          return fileLocation; //fileLocation;
    }
    
    // Set the file filter value - * = All_Files, binary = Executables, extensions = Custom Extensions
    private String initializeFilter(Directories dirObject) {
        String fileFormat = dirObject.getChoice().getValue().toString();
        String filter = null;
        switch (fileFormat) {
            case "All Files":
                filter = "*";
                break;
            case "Binaries":
                filter = "binary";
                break;
            case "Custom Formats":
                filter = dirObject.getTfield().getText();
                break;
        }
        return filter;
    }

    // Traverse through directory iteratively and returns list of 'file###actualfile'
    private List<String> getFilesFromDir(File dir, String fileFormat, String filter, boolean includeHiddenFiles, boolean isWindows) {
//        System.out.println("PSDebug getFilesfromDir Function");
//        System.out.println("PSDebug dir path:" + dir.getAbsolutePath());
//        System.out.println("PSDebug includeHiddenFiles:" + includeHiddenFiles);
        iterateRecursively(dir.getAbsolutePath(), dir.getAbsolutePath(), includeHiddenFiles);
        
        // Select the files depending on the filter value
        switch (fileFormat) {
            case "*":
                break;
            case "binary":
                List<String> deleteMe = new ArrayList<>();
                for(String str : allFiles) {
//                    System.out.println("PSDebug Str all Files:" + str);
                    File file = new File(str.split(splitChar)[1]);
                    if(isWindows) {
                        if(!isExecutable(file)) {
                            deleteMe.add(str);
                        }
                    } else if(!file.canExecute()){
                        deleteMe.add(str);
                    }
                }
		for(String myfile : deleteMe) {
		    allFiles.remove(myfile);
		}
                break;
            default:
                //BS COMM allFiles = getFilesWithSpecifiedExtension(allFiles, filter);
                break;
        }
//        System.out.println("PSDebug all Files val is:" + allFiles.toString());
        return allFiles;
    }
    
    // Check for executable file -- specific to Windows
    private boolean isExecutable(File file){
        boolean isExecutable = false;
        byte[] firstBytes = new byte[4];
        try
        {
            FileInputStream input = new FileInputStream(file);
            input.read(firstBytes);

            // Check for Windows executable
            if (firstBytes[0] == 0x4d && firstBytes[1] == 0x5a)
            {
                isExecutable = true;
            }
            input.close();
            return isExecutable;
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    // Get the files with specified file extension
    private List<String> getFilesWithSpecifiedExtension(List<String> allFiles, String [] extensions) {
        List<String> returnList = new ArrayList<>();
        for(int i = 0; i < extensions.length; i++) {
            for(String str : allFiles) {
                File file = new File(str.split(splitChar)[0]);
                if(file.getAbsolutePath().endsWith("." + extensions[i])) {
                    returnList.add(str);
                }
            }            
        }
        return returnList;
    }

    // Calculate hash and return hash value
    public String computeHash(MessageDigest md, File file) {
        Map<String, String> fileAndHash = new LinkedHashMap<>();
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
            logger.log(Level.SEVERE, null, ex);
        }
        
        return sb.toString();
    }    
    
    // This function will iterate recursively through a directory and will add the file###actualFile to list. This is needed when we have symolic link
    public void iterateRecursively(String origPath, String newPath, boolean includeHiddenFiles) {
       
        File [] files = new File(newPath).listFiles();
        for(File file : files) {
            if(!file.isDirectory()) {
                try {
                    String path = file.getAbsolutePath();
//                    System.out.println("PSDebug path is:" + path);
                    if(!path.startsWith(mountPath)) {
//                        System.out.println("PSDebug !!!!!!!!!!!!!!!!!");
                        path = mountPath + path;
                    }
                    if(Files.isSymbolicLink(Paths.get(path))) {
//                        System.out.println("PSDebug @@@@@@@@@@");
//                        System.out.println("PSDebug getSymPath" + path);
                        getSymlinkValue(path);
                        value = new File(value).getCanonicalPath();
                    } else {
//                        System.out.println("PSDebug ##############");
                        value = new File(path).getCanonicalPath();                        
                    }
                    if(!value.startsWith(mountPath)) {
//                        System.out.println("PSDebug $$$$$$$$$$$$");
                        value = mountPath + value;
                    }
                    File tempFile;
                    if(value != null) {
//                        System.out.println("PSDebug %%%%%%%%%%%%%%%%%");
                        tempFile = new File(value);
                        if(tempFile.exists() && tempFile.isFile()) {
//                            System.out.println("PSDebug ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                            if(includeHiddenFiles) {
//                                System.out.println("PSDebug &&&&&&&&&&&&&&");
                                path = path.replace(newPath, origPath);  
                                allFiles.add(path + splitChar + tempFile.getAbsolutePath());  
                            } else if(!tempFile.isHidden()) {
                                path = path.replace(newPath, origPath);
                                allFiles.add(path + splitChar + tempFile.getAbsolutePath());  
                            }
                        }
                    }
                } catch(IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                
            } else {
                String dirPath = file.getAbsolutePath();
                logger.info("Entry is a directory : " + dirPath);
                if(Files.isSymbolicLink(Paths.get(dirPath))) {
                    try {
                        logger.info("SYMLINK : " + dirPath);
                        String symPath = Files.readSymbolicLink(Paths.get(dirPath)).toString();
                        logger.info("SYMPATH : " + symPath);
                        logger.info("ORIGPATH : " + origPath);
                        //String origSymPath = Files.readSymbolicLink(Paths.get(origPath)).toString();
                        if(!symPath.equals(dirPath)) {
                            logger.info("SYMPATH != DIRPATH" + dirPath);
                            if(symPath.startsWith("/") && ((isBareMetal) || !symPath.startsWith(mountPath))) {
                                logger.info("PSDebug Came in here 333" + dirPath);
                                symPath = mountPath + symPath;
                            } else {
                                    symPath = file.getParent() + "/" + symPath;
                            }
                            dirPath = dirPath.replace(newPath, origPath);
                            iterateRecursively(dirPath, symPath, includeHiddenFiles);                                                    
                        }

                    } catch(IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                } else {
                    dirPath = dirPath.replace(newPath, origPath);
                    iterateRecursively(dirPath, file.getAbsolutePath(), includeHiddenFiles);
                }
            }
        }
    }
    
    // Finds the final target of the symbolic link
    private void getSymlinkValue(String filePath) {
        Path path = Paths.get(filePath);
        if(Files.isSymbolicLink(path)) {
            try {
                filePath = (Files.readSymbolicLink(path)).toString();
                if(filePath.startsWith("/") && (isBareMetal || !filePath.startsWith(mountPath))) {
                    filePath = mountPath + filePath;
                    getSymlinkValue(filePath);
                } else if(filePath.startsWith(".") || filePath.startsWith("..") || !filePath.startsWith("/")){
                    filePath = path.toFile().getParent() + "/" + filePath;
                    getSymlinkValue(filePath);
                } else {
                    value = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                logger.log(Level.SEVERE, null, ex);
                value = null;
            }
            
        } else {            
            value = filePath;
        } 
    }
}
