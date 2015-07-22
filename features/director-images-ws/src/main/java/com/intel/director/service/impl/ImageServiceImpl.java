package com.intel.director.service.impl;

import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageStoreUploadRequest;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.TrustDirectorImageUploadRequest;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.common.Constants;
import com.intel.director.common.MountVMImage;
import com.intel.director.images.exception.ImageMountException;
import com.intel.director.persistence.ImagePersistenceManager;
import com.intel.director.service.ImageService;
import com.intel.director.util.DirectorUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Siddharth
 */
@Component
public class ImageServiceImpl implements ImageService {

    private ImagePersistenceManager imagePersistenceManager;

    @Override
    public MountImageResponse mountImage(String imageId, String user) throws ImageMountException {
        MountImageResponse mountImageResponse = null;
        try {
            ImageAttributes image = imagePersistenceManager.fetchImageById(imageId);
            //Mark the image mounted by the user
            image.mounted = Boolean.TRUE;
            image.mountedBy = user;
            imagePersistenceManager.updateImage(image);

            //Mount the image
            String mountPath = DirectorUtil.computeVMMountPath(image.id);
            MountVMImage.mountImage(image.location, mountPath);
            mountImageResponse = DirectorUtil.mapImageAttributesToMountImageResponse(image);
        } catch (Exception ex) {
            throw new ImageMountException("Unable to mount image", ex);
        }
        return mountImageResponse;
    }

    @Override
    public UnmountImageResponse unMountImage(String imageId, String user) throws ImageMountException {
        UnmountImageResponse unmountImageResponse = null;
        try {
            ImageAttributes image = imagePersistenceManager.fetchImageById(imageId);

            //Throw an exception if different user than the mounted_by user 
            //tries to unmount
            if (!image.mountedBy.equalsIgnoreCase(user)) {
                throw new ImageMountException("Image cannot be unmounted by a differnt user");
            }

            //Mark the image unmounted 
            image.mounted = Boolean.FALSE;
            image.mountedBy = null;
            imagePersistenceManager.updateImage(image);

            //Unmount the image
            String mountPath = DirectorUtil.computeVMMountPath(image.id);
            MountVMImage.unmountImage(mountPath);
            unmountImageResponse = DirectorUtil.mapImageAttributesToUnMountImageResponse(image);
        } catch (Exception ex) {
            throw new ImageMountException("Unable to unmount image", ex);
        }
        return unmountImageResponse;
    }

    @Override
    public TrustDirectorImageUploadResponse uploadImageMetaDataToTrustDirector(TrustDirectorImageUploadRequest trustDirectorImageUploadRequest) {
        TrustDirectorImageUploadResponse directorImageUploadResponse = null;

        //Check if the file with the same name has been uploaded earlier
        //If so, append "_1" to the file name and then save
        File image = new File(Constants.vmImagesPath + trustDirectorImageUploadRequest.imageAttributes.name);
        if (image.exists()) {
            trustDirectorImageUploadRequest.imageAttributes.name += "_1";
        }

        //Save image meta data to the database
        ImageAttributes imageAttributes = imagePersistenceManager.saveImageMetadata(trustDirectorImageUploadRequest.imageAttributes);
        return DirectorUtil.mapImageAttributesToTrustDirectorImageUploadResponse(imageAttributes);
    }

    /**
     *
     * @param imageId
     * @param imageFileInputStream
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Override
    public TrustDirectorImageUploadResponse uploadImageToTrustDirector(String imageId, InputStream imageFileInputStream)
            throws FileNotFoundException, IOException {

        //Get the image details saved in the earlier step
        ImageAttributes imageAttributes = imagePersistenceManager.fetchImageById(imageId);

        //Write image to file
        DirectorUtil.writeImageToFile(imageFileInputStream, imageAttributes);

        //Save image meta data to the database
        TrustDirectorImageUploadResponse directorImageUploadResponse = (TrustDirectorImageUploadResponse) imagePersistenceManager.saveImageMetadata(imageAttributes);

        return directorImageUploadResponse;
    }

    @Override
    public SearchImagesResponse searchImages(SearchImagesRequest searchImagesRequest) {
        SearchImagesResponse searchImagesResponse = new SearchImagesResponse();
        List<ImageAttributes> imagesBySearchCriteria = imagePersistenceManager.fetchImagesBySearchCriteria(null, null);
        searchImagesResponse.images = imagesBySearchCriteria;
        return searchImagesResponse;
    }

    @Override
    public SearchFilesInImageResponse searchFilesInImage(SearchFilesInImageRequest searchFilesInImageRequest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * **************************************************************************************
     */
    /**
     * Setters and getters
     *
     * @return
     */
    @Autowired
    public ImageServiceImpl(ImagePersistenceManager imagePersistenceManager) {
        this.imagePersistenceManager = imagePersistenceManager;
    }

    public ImagePersistenceManager getImagePersistenceManager() {
        return imagePersistenceManager;
    }

    @Autowired
    public void setImagePersistenceManager(ImagePersistenceManager imagePersistenceManager) {
        this.imagePersistenceManager = imagePersistenceManager;
    }

    @Override
    public ImageStoreUploadResponse uploadImageToImageStore(ImageStoreUploadRequest imageStoreUploadRequest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
