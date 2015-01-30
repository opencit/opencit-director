/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import com.intel.mtwilson.director.javafx.ui.Constants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author admkrushnakant
 */
public class GenerateManifest {
    private static final Logger logger = Logger.getLogger(GenerateManifest.class.getName());
    private String excludeFile = Constants.EXCLUDE_FILE_NAME;
    private String mountPath = Constants.MOUNT_PATH;
    private int count = 0;
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }    
    public static Map<String, LinkedHashMap<String, String>> dirFilesHashMapping = new HashMap<>();
    public static Map<String, String> configInfo=new HashMap<>();
    public static boolean manifestFlag=false;

// Write the hash value to xml file
    public String writeToXMLManifest() { 
    String manifestTargetLocation="/manifest-" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xml";
    String imagePathDelimiter="/";
    int beginIndex=0;
    int endIndex;
    String trustPolicy=null;
    if(Boolean.valueOf(configInfo.get(Constants.IS_WINDOWS))) {
            mountPath = mountPath + "/";
        }
        
        if(dirFilesHashMapping != null) {
            logger.info("Calculated hash of : " + dirFilesHashMapping.size() + " directories");
        }
        
        String manifestStorage=configInfo.get(Constants.IMAGE_LOCATION);
        endIndex=manifestStorage.lastIndexOf(imagePathDelimiter);
        manifestStorage=manifestStorage.substring(beginIndex, endIndex);
                
        // Target location of the manifest file
        String targetLocation= manifestStorage + manifestTargetLocation;
//        System.out.println("Target location is:" + targetLocation);
       
        
        
        // Create the "/root/manifest_files" directory if not present
        File manifestDir = new File("/root/manifest_files");
        if(!manifestDir.exists()) {
            manifestDir.mkdir();
        }
        
        
        // This map is used to calculate the Image Hash
        Map<String, String> dirAndAggregateHash = new LinkedHashMap<>();
        String imageHash = "";
        
        // Initialize MessageDigest
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(configInfo.get(Constants.HASH_TYPE));
          } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, null, md);
        }
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            
            // Root Element
            Element rootElement = doc.createElement("TrustPolicy");
            doc.appendChild(rootElement);
            
            Element headers = doc.createElement("Headers");
            rootElement.appendChild(headers);
            
            Attr manifestVersion = doc.createAttribute("version");
            manifestVersion.setValue("1.1");
            rootElement.setAttributeNode(manifestVersion);
            
            
            Element imageEncryption = doc.createElement("Image_Encryption");
            imageEncryption.appendChild(doc.createTextNode(configInfo.get(Constants.IS_ENCRYPTED)));
            headers.appendChild(imageEncryption);
            
            Element imageId = doc.createElement("Image_ID");
            imageId.appendChild(doc.createTextNode(configInfo.get(Constants.IMAGE_ID)));
            headers.appendChild(imageId);
            
            Element launchPolicy = doc.createElement("Launch_Policy");
            //TODO Remove temporary hack
            String policy = configInfo.get(Constants.POLICY_TYPE);
            if(policy.equalsIgnoreCase("MeasureOnly")){
                policy = "Audit";
            }
            else{
                policy = "Enforce";
            }
            launchPolicy.appendChild(doc.createTextNode(policy));
            headers.appendChild(launchPolicy);
            
            Element hashType = doc.createElement("Hash_Type");
            hashType.appendChild(doc.createTextNode(configInfo.get(Constants.HASH_TYPE)));
            headers.appendChild(hashType);
                      
            Element hiddenFiles = doc.createElement("Hidden_Files");
            hiddenFiles.appendChild(doc.createTextNode(configInfo.get(Constants.HIDDEN_FILES)));
            headers.appendChild(hiddenFiles);
            
            Element fileHashes = doc.createElement("File_Hashes");

            // Hash of kernel and initrd (Only for ami images)
            String kernelHashValue = null;
            String initrdHashValue = null;
            if(configInfo.containsKey(Constants.KERNEL_PATH) && configInfo.containsKey(Constants.INITRD_PATH)) {
                Element kernelHash = doc.createElement("Kernel_Hash");
                kernelHashValue = getFileHash(new File(configInfo.get(Constants.KERNEL_PATH)), md);
                kernelHash.appendChild(doc.createTextNode(kernelHashValue));
                fileHashes.appendChild(kernelHash);

                Element initrdHash = doc.createElement("Initrd_Hash");
                initrdHashValue = getFileHash(new File(configInfo.get(Constants.INITRD_PATH)), md);

                initrdHash.appendChild(doc.createTextNode(initrdHashValue));
                fileHashes.appendChild(initrdHash);

                // write to map
                //dirAndAggregateHash.put(confInfo.get(Constants.KERNEL_PATH), kernelHashValue);
                //dirAndAggregateHash.put(confInfo.get(Constants.INITRD_PATH), initrdHashValue);
            }
            
            if(dirFilesHashMapping != null) {
                // Add the "Measurement_Exclude_Files" tag in the manifest
                Element excludeFiles = doc.createElement("Measurement_Exclude_Files");
                try {
                    File file = new File(excludeFile);
                    if(file.exists()) {
                        FileReader fr = new FileReader(file);
                        BufferedReader br = new BufferedReader(fr);
                        String line;
                        while((line = br.readLine()) != null) {
                            Element filePath = doc.createElement("FilePath");
                            filePath.appendChild(doc.createTextNode(line));
                            excludeFiles.appendChild(filePath);
                        }
                        br.close();                
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                fileHashes.appendChild(excludeFiles);
                
                // Iterate through all directories and add the "Dir" tag in the manifest
                for (Map.Entry pairs : dirFilesHashMapping.entrySet()) {
                    logger.info("Dir Name : " + pairs.getKey().toString().split("::")[0].replace(mountPath, ""));
                    logger.info("Size before excluding files : " + dirFilesHashMapping.get(pairs.getKey()).size());
//                    System.out.println("Dir Name : " + pairs.getKey().toString().split("::")[0].replace(mountPath, ""));
//                    System.out.println("Size before excluding files : " + dirFilesHashMapping.get(pairs.getKey()).size());
                    
                    // Exclude the files from measurement
                    LinkedHashMap<String, String> modifiedFileAndHashMap = (LinkedHashMap)excludeFilesFromMeasurement(dirFilesHashMapping.get(pairs.getKey()));
                    logger.info("Size after excluding files : " + modifiedFileAndHashMap.size());
                    System.out.println("Size after excluding files : " + modifiedFileAndHashMap.size());
                    
                    Element dir = doc.createElement("Dir");
                
                    Attr dirName = doc.createAttribute("name");
                    String name = pairs.getKey().toString().split("::")[0].replace(mountPath, "");
                    dirName.setValue(name);
                    dir.setAttributeNode(dirName);

                    Attr fileCount = doc.createAttribute("file_count");
                    fileCount.setValue(String.valueOf(modifiedFileAndHashMap.size()));
                    dir.setAttributeNode(fileCount);
                
                    Attr filterValue = doc.createAttribute("filter");
                    filterValue.setValue(pairs.getKey().toString().split("::")[1]);
                    dir.setAttributeNode(filterValue);
                
                    Attr dirHash = doc.createAttribute("dir_hash");
                    String cumulativeHash = getCumulativeHash(modifiedFileAndHashMap, md);
                    logger.info("Cumulative hash for directory " + pairs.getKey().toString().replace(mountPath, "") + " is : " + cumulativeHash);
                    System.out.println("Cumulative hash for directory " + pairs.getKey().toString().replace(mountPath, "") + " is : " + cumulativeHash);
                    dirHash.setValue(cumulativeHash);
                    dirAndAggregateHash.put(name, cumulativeHash);
                    dir.setAttributeNode(dirHash);
                
                    for (Map.Entry mapPairs : modifiedFileAndHashMap.entrySet()) {
                        Element fileHash = doc.createElement("File_Hash");
                
                        Attr filePath = doc.createAttribute("file_path");
                        filePath.setValue(mapPairs.getKey().toString().replace(mountPath, ""));
                        fileHash.setAttributeNode(filePath);
                
                        fileHash.appendChild(doc.createTextNode(mapPairs.getValue().toString()));
                    
                        dir.appendChild(fileHash);                                        
                    }
                    fileHashes.appendChild(dir);
                }
		// Add kernel and initrd hash to map for final image hash
                if((initrdHashValue != null) && (kernelHashValue != null)) {
                    dirAndAggregateHash.put(configInfo.get(Constants.KERNEL_PATH), kernelHashValue);
                    dirAndAggregateHash.put(configInfo.get(Constants.INITRD_PATH), initrdHashValue);
                }
                
                imageHash = getCumulativeHash((LinkedHashMap<String, String>) dirAndAggregateHash, md);
                
            } else {
                
                imageHash = getFileHash(new File(configInfo.get(Constants.IMAGE_LOCATION)), md);
            }

            Element imageHashTag = doc.createElement("Image_Hash");
            imageHashTag.appendChild(doc.createTextNode(imageHash));
            headers.appendChild(imageHashTag);
            
            rootElement.appendChild(fileHashes);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            
            StreamResult result = new StreamResult(new File(targetLocation));
            
            transformer.transform(source, result);
            StringWriter writter = new StringWriter();
            transformer.transform(source, new StreamResult(writter));
            trustPolicy = writter.toString();
            System.out.println("Trust Policy is" + trustPolicy.toString());
            System.out.println("File saved at : " + targetLocation);
            logger.info("Manifest file saved at " + targetLocation);

            md = MessageDigest.getInstance("SHA-256");
            // Sign manifest with Mt. Wilson
            // This part is commented because as of now IMVM doesn't verify the Mt. Wilson signature

            String fileHash = getFileHash(new File(targetLocation), md);
            String base64Hash = new FileUtilityOperation().base64Encode(fileHash);
            System.out.println("Signing Mt wilson image ID" + configInfo.get(Constants.IMAGE_ID));
            System.out.println("Trust Policy is"+ trustPolicy);
            String signedTrustPolicy = new SignWithMtWilson().signManifest(configInfo.get(Constants.IMAGE_ID), trustPolicy);
            if(signedTrustPolicy == null) {
                logger.log(Level.SEVERE, "Failed in signing the trustPolicy with Mt Wilson");
                System.out.println("Deleting the trustPolicy file " + targetLocation);
                new File(targetLocation).delete();
                return null;
            }
            
            //writting signed trustpolicy to a file
            BufferedWriter out = new BufferedWriter(new FileWriter(targetLocation));
            out.write(signedTrustPolicy);
            out.close();
            
        } catch (ParserConfigurationException pce) {
		logger.log(Level.SEVERE, null, pce);
	} catch (TransformerException tfe) {
		logger.log(Level.SEVERE, null, tfe);
	} catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GenerateManifest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GenerateManifest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
//        // Sign manifest with Mt. Wilson
//        // This part is commented because as of now IMVM doesn't verify the Mt. Wilson signature
//
//        String fileHash = getFileHash(new File(targetLocation), md);
//        
//        String base64Hash = new FileUtilityOperation().base64Encode(fileHash);
//        String signature = new SignWithMtWilson().signManifest(configInfo.get(Constants.IMAGE_ID), base64Hash);
//        if(signature == null) {
//            logger.log(Level.SEVERE, "Failed in signing the manifest with Mt Wilson");
////	    System.out.println("Deleting the manifest file " + targetLocation);
//	    new File(targetLocation).delete();
//            return null;
//        }
//        new FileUtilityOperation().writeToFile(new File(targetLocation), signature, true);
        
//        configInfo.clear();
//        dirFilesHashMapping.clear();
        
        return targetLocation;
    }
    
    //PS:Helper Method
    public void RetrieveFileHash(Map<String, LinkedHashMap<String, String>> dirAndFilesMapping, Map<String, String> confInfo){
        
        
        if(dirAndFilesMapping!=null)
        {
            dirFilesHashMapping.putAll(dirAndFilesMapping);
//            Iterator it = dirFilesHashMapping.entrySet().iterator();
//                    System.out.println("PSDebug Retrieve function: DirFile Hash Values Are ");
//                    while (it.hasNext()) {
//                        Map.Entry pairs = (Map.Entry) it.next();
//                        System.out.println(pairs.getKey().toString() + " : " + pairs.getValue().toString());
//                    }
        }
        if(confInfo!=null)
        {
            configInfo.putAll(confInfo);
//            Iterator it = configInfo.entrySet().iterator();
//                    System.out.println("PSDebug Retrieve func configInfo Values Are ");
//                    while (it.hasNext()) {
//                        Map.Entry pairs = (Map.Entry) it.next();
//                        System.out.println(pairs.getKey().toString() + " : " + pairs.getValue().toString());
//                    }
        }
        manifestFlag=true;
    }

    // Calculates the aggregate hash
    private String getCumulativeHash(LinkedHashMap<String, String> fileAndHash, MessageDigest md) {
        File hashFile = null;
        if(count == 0) {
            hashFile = new File("/tmp/fileHashes.txt");
            count++;
        } else {
            hashFile = new File("/tmp/dirHashes.txt");
        }
        
        try {
            
            if(hashFile.exists()) {
                hashFile.delete();
            } else {
                hashFile.createNewFile();
            }
            
            FileWriter writer = new FileWriter(hashFile, true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            
            for(Map.Entry pairs : fileAndHash.entrySet()) {
                bufferWriter.write(pairs.getValue().toString());
                bufferWriter.newLine();
            }
            bufferWriter.close();
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        return getFileHash(hashFile, md);
    }
    
    // Calculates the hash value for a file
    public String getFileHash(File fileLocation, MessageDigest md) {
        GenerateHash genHash = new GenerateHash();
        
        return genHash.computeHash(md, fileLocation);
    }
    
    // Exclude the files from measurement
    private Map<String, String> excludeFilesFromMeasurement(Map<String, String> fileAndHashMap) {
        try {
            File file = new File(excludeFile);
            if(file.exists()) {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                while((line = br.readLine()) != null) {
                    fileAndHashMap.remove(mountPath + line);
                }
                br.close();                
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return fileAndHashMap;
    }
}