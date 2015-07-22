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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

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
            File file = new File(imageAttributes.location);
            out = new FileOutputStream(file);
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
        imageAttributes.image_size = new File(imageAttributes.location).length();
    }

    public static void main(String[] args) {
        try {
            String s = DirectorUtil.computeVMMountPath("cirus_x56.img", "/opt/vm/");
            System.out.println(s);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DirectorUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
