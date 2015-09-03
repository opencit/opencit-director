/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.util;

import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.common.Constants;
import com.intel.director.images.GlanceImageStoreManager;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.api.ImageStoreManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import com.github.dnault.xmlpatch.Patcher;

/**
 *
 * @author GS-0681
 */
public class DirectorUtil {

    public static String computeVMMountPath(String imageName, String imagePath) throws NoSuchAlgorithmException {
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

    public static MountImageResponse mapImageAttributesToMountImageResponse(ImageAttributes imageAttributes) {
        Mapper mapper = new DozerBeanMapper();
        MountImageResponse mountImageResponse = mapper.map(imageAttributes, MountImageResponse.class);
        return mountImageResponse;
    }

    public static UnmountImageResponse mapImageAttributesToUnMountImageResponse(ImageAttributes imageAttributes) {
        Mapper mapper = new DozerBeanMapper();
        UnmountImageResponse unmountImageResponse = mapper.map(imageAttributes, UnmountImageResponse.class);
        return unmountImageResponse;
    }

    public static TrustDirectorImageUploadResponse mapImageAttributesToTrustDirectorImageUploadResponse(ImageAttributes imageAttributes) {
        Mapper mapper = new DozerBeanMapper();
        TrustDirectorImageUploadResponse directorImageUploadResponse = mapper.map(imageAttributes, TrustDirectorImageUploadResponse.class);
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
        imageAttributes.image_size = new Long(new File(imageAttributes.location).length()).intValue();
    }

    //TODO: get the class name from the store name
    public static ImageStoreManager getImageStoreManager(String storeName) throws DirectorException {
        ImageStoreManager imageStoreManager = null;
        try {
            switch (storeName) {
                case "glance":
                    imageStoreManager = new GlanceImageStoreManager();
                    break;
            }
        } catch (Exception e) {
            throw new DirectorException("Unable to fetch an image store manager", e);
        }
        return imageStoreManager;
    }
    
    
	public static String patch(String src, String patch) throws IOException {
		String patched = "";
		try {
			InputStream inputStream = new ByteArrayInputStream(src.getBytes(StandardCharsets.UTF_8));

			InputStream patchStream =new ByteArrayInputStream(patch.getBytes(StandardCharsets.UTF_8)); 
			OutputStream outputStream = new ByteArrayOutputStream(); 

			Patcher.patch(inputStream, patchStream, outputStream);
			patched = ((ByteArrayOutputStream)outputStream).toString("UTF-8");

		} catch (FileNotFoundException e) {
			System.err.println("ERROR: Could not access file: "
					+ e.getMessage());
			System.exit(1);
		}
		return patched;
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

}
