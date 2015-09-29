/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.util;

import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.mtwilson.trustpolicy.xml.Checksum;
import com.intel.mtwilson.trustpolicy.xml.DecryptionKey;
import com.intel.mtwilson.trustpolicy.xml.Director;
import com.intel.mtwilson.trustpolicy.xml.Encryption;
import com.intel.mtwilson.trustpolicy.xml.Image;
import com.intel.mtwilson.trustpolicy.xml.LaunchControlPolicy;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy;
import com.intel.mtwilson.trustpolicy.xml.Whitelist;
import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.common.Constants;
import com.intel.director.images.GlanceImageStoreManager;
import com.intel.director.images.exception.DirectorException;

import java.text.SimpleDateFormat;

import com.intel.director.api.ImageStoreManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import com.github.dnault.xmlpatch.Patcher;

/**
 * 
 * @author GS-0681
 */
public class DirectorUtil {

	public static String computeVMMountPath(String imageName, String imagePath)
			throws NoSuchAlgorithmException {
		Md5Digest md5Digest = Md5Digest.digestOf(imagePath.getBytes());
		String hexString = md5Digest.toHexString();
		String prefix = hexString.substring(hexString.length() - 4);

		StringBuilder sb = new StringBuilder(Constants.mountPath);
		sb.append(prefix);
		sb.append(imageName);
		return sb.toString();
	}

	public static String computeVMMountPath(String imageId) {
		StringBuilder sb = new StringBuilder(Constants.mountPath);
		sb.append(imageId);
		return sb.toString();
	}

	public static MountImageResponse mapImageAttributesToMountImageResponse(
			ImageAttributes imageAttributes) {
		Mapper mapper = new DozerBeanMapper();
		MountImageResponse mountImageResponse = mapper.map(imageAttributes,
				MountImageResponse.class);
		return mountImageResponse;
	}

	public static UnmountImageResponse mapImageAttributesToUnMountImageResponse(
			ImageAttributes imageAttributes) {
		Mapper mapper = new DozerBeanMapper();
		UnmountImageResponse unmountImageResponse = mapper.map(imageAttributes,
				UnmountImageResponse.class);
		return unmountImageResponse;
	}

	public static TrustDirectorImageUploadResponse mapImageAttributesToTrustDirectorImageUploadResponse(
			ImageAttributes imageAttributes) {
		Mapper mapper = new DozerBeanMapper();
		TrustDirectorImageUploadResponse directorImageUploadResponse = mapper
				.map(imageAttributes, TrustDirectorImageUploadResponse.class);
		return directorImageUploadResponse;
	}

	// save file to new location
	public static void writeImageToFile(InputStream uploadedInputStream,
			ImageAttributes imageAttributes) throws IOException {
		OutputStream out = null;
		try {
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(imageAttributes.location));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
		imageAttributes.image_size = new Long(
				new File(imageAttributes.location).length()).intValue();
	}

	// TODO: get the class name from the store name
	public static ImageStoreManager getImageStoreManager(String storeName)
			throws DirectorException {
		ImageStoreManager imageStoreManager = null;
		try {
			switch (storeName) {
			case "glance":
				imageStoreManager = new GlanceImageStoreManager();
				break;
			}
		} catch (Exception e) {
			throw new DirectorException(
					"Unable to fetch an image store manager", e);
		}
		return imageStoreManager;
	}

	public static String patch(String src, String patch) throws IOException {
		String patched = "";
		try {
			InputStream inputStream = new ByteArrayInputStream(
					src.getBytes(StandardCharsets.UTF_8));

			InputStream patchStream = new ByteArrayInputStream(
					patch.getBytes(StandardCharsets.UTF_8));
			OutputStream outputStream = new ByteArrayOutputStream();

			Patcher.patch(inputStream, patchStream, outputStream);
			patched = ((ByteArrayOutputStream) outputStream).toString("UTF-8");

		} catch (FileNotFoundException e) {
			System.err.println("ERROR: Could not access file: "
					+ e.getMessage());
			System.exit(1);
		}
		return patched;
	}


    
    public static void getParentDirectory(String filePath, String root, Map<String, Boolean> parentsList, boolean recursive){
    	File parent = new File(filePath).getParentFile();
    	if(parent == null){
    		return;
    	}
    	
    	if(parent.isDirectory()){
    		String parentPath = null;
    		try {
    			parentPath = parent.getCanonicalPath().replace("\\", "/");
				if(parentPath.equals(root)){
					return;
				}
				parentsList.put(parentPath, recursive);
				getParentDirectory(parentPath, root, parentsList, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	}

    }


	public static String generateInitialPolicyDraft(
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest)
			throws JAXBException {
		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = new com.intel.mtwilson.trustpolicy.xml.TrustPolicy();
		Director director = new Director();
		director.setCustomerId("");
		Image image = new Image();
		image.setImageId(createTrustPolicyMetaDataRequest.getImageid());
		Whitelist whitelist = new Whitelist();
		policy.setLaunchControlPolicy(LaunchControlPolicy.fromValue(createTrustPolicyMetaDataRequest
				.getLaunch_control_policy()));
		policy.setDirector(director);
		policy.setImage(image);
		policy.setWhitelist(whitelist);
		policy=setEncryption(createTrustPolicyMetaDataRequest,policy);
		
	
		JAXBContext context = JAXBContext.newInstance(TrustPolicy.class);
		Marshaller m = context.createMarshaller();

		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter w = new StringWriter();
		m.marshal(policy, w);
		return w.toString();

	}
	
	
	
public static com.intel.mtwilson.trustpolicy.xml.TrustPolicy  setEncryption(CreateTrustPolicyMetaDataRequest req,com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy){
	boolean encryptFlag=req.getIsEncrypted();
	if(encryptFlag){
		 Encryption encryption = new Encryption();
		 DecryptionKey key = new DecryptionKey();
          key.setURL("uri");
          key.setValue("213123123123");
         Checksum checksum = new Checksum();
         checksum.setValue("sdfsdfsdfsdf");
          encryption.setChecksum(checksum);
          encryption.setKey(key);
		policy.setEncryption(encryption);
	}
	return policy;
}

	public static String updatePolicyDraft(String existingPolicyDraft,
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(TrustPolicy.class);
		Unmarshaller unmarshaller = (Unmarshaller) jaxbContext
				.createUnmarshaller();

		StringReader reader = new StringReader(existingPolicyDraft);
		TrustPolicy policy = (TrustPolicy) unmarshaller
				.unmarshal(reader);
		LaunchControlPolicy controlPolicy = LaunchControlPolicy.fromValue(createTrustPolicyMetaDataRequest
				.getLaunch_control_policy());
		policy.setLaunchControlPolicy(controlPolicy);
		policy=setEncryption(createTrustPolicyMetaDataRequest,policy);
		Marshaller m = jaxbContext.createMarshaller();

		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter w = new StringWriter();
		m.marshal(policy, w);
		return w.toString();

	}

	public static void main(String[] args) {
		String imageId = "123";
		ImageAttributes imageAttributes = new ImageAttributes();
		imageAttributes.id = imageId;
		imageAttributes.image_deployments = "VM";
		imageAttributes.image_format = "qcow2";
		imageAttributes.image_size = 1000;
		imageAttributes.location = "/opt/director/vm/" + imageId;
		imageAttributes.mounted_by_user_id = null;
		imageAttributes.name = "IMG_" + imageId;
		imageAttributes.status = null;

		TrustDirectorImageUploadResponse directorImageUploadResponse = mapImageAttributesToTrustDirectorImageUploadResponse(imageAttributes);

		System.out.println(directorImageUploadResponse.id);

	}
	
	
	public static String createImageTrustPolicyTar(String trustPolicyLocation, String imageLocation) {
        String imagePathDelimiter = "/";

        String imageTPDir = trustPolicyLocation.substring(0, trustPolicyLocation.lastIndexOf(imagePathDelimiter));
        //// TO DO:-   Use common code method
        executeShellCommand("cd " + imageTPDir);
        executeShellCommand("pwd ");
        String imageName = imageLocation.substring(imageLocation.lastIndexOf(imagePathDelimiter) + 1);
        String trustpolicyName = trustPolicyLocation.substring(trustPolicyLocation.lastIndexOf(imagePathDelimiter) + 1);
      
        String tarName = imageName + "-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".tar";
        executeShellCommand("tar -cf " + imageTPDir + imagePathDelimiter + tarName + " -C " + imageTPDir + " " + imageName + " " + trustpolicyName);
        return imageTPDir + imagePathDelimiter + tarName;
    }
	
	
	 public static String executeShellCommand(String command){
	     //   log.debug("Command to execute is:"+command);
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
	            //log.debug("Result of execute command: "+result);            
	        } catch (InterruptedException ex) {
	          //  log.error(null, ex);
	        } catch (IOException ex) {
	          //  log.error(null, ex);
	        }
	        return excludeList;
	    }

		
	

}
