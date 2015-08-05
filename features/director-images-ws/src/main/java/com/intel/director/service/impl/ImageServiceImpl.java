package com.intel.director.service.impl;

import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ImageStoreRequest;
import com.intel.director.api.ImageStoreResponse;
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
import com.intel.director.exception.ImageStoreException;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.images.exception.ImageMountException;
import com.intel.director.service.ImageService;
import com.intel.director.api.ImageStoreManager;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.util.DirectorUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.IPersistService;
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

    @Autowired
    private IPersistService imagePersistenceManager;

    @Autowired
    private ImageStoreManager imageStoreManager;

    public ImageServiceImpl() {
    }

    @Override
    public MountImageResponse mountImage(String imageId, String user) throws ImageMountException {
        MountImageResponse mountImageResponse = null;
        ImageAttributes image;
        try {
            image = imagePersistenceManager.fetchImageById(imageId);
        } catch (DbException ex) {
            throw new ImageMountException("No image found with id: "+imageId, ex);
        }
        //Check if the image is already mounted. If so, return error
        if (image.mounted_by_user_id != null) {
            throw new ImageMountException("Unable to mount image. Image is already in use by user: " + image.mounted_by_user_id);
        }
        String mountPath = DirectorUtil.computeVMMountPath(image.id);

        try {
            //Mount the image
            MountVMImage.mountImage(image.location, mountPath);
            mountImageResponse = DirectorUtil.mapImageAttributesToMountImageResponse(image);
        } catch (Exception ex) {
            throw new ImageMountException("Unable to mount image", ex);
        }

        //Mark the image mounted by the user
        try {
            image.mounted_by_user_id = user;
            imagePersistenceManager.updateImage(image);
            mountImageResponse.mounted_by_user_id = user;
        } catch (DbException ex) {
            try {
                //unmount the image
                MountVMImage.unmountImage(mountPath);
            } catch (Exception ex1) {
                throw new ImageMountException("Failed to unmoubt image. The attempt was made after the DB update for mounted_by_user failed. ", ex1);
            }

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
            if (!image.mounted_by_user_id.equalsIgnoreCase(user)) {
                throw new ImageMountException("Image cannot be unmounted by a differnt user");
            }

            //Mark the image unmounted 
            image.mounted_by_user_id = null;
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
    public TrustDirectorImageUploadResponse uploadImageMetaDataToTrustDirector(TrustDirectorImageUploadRequest trustDirectorImageUploadRequest) throws DbException {
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
    public TrustDirectorImageUploadResponse uploadImageToTrustDirector(String imageId, InputStream imageFileInputStream) throws DbException, IOException {

        //Get the image details saved in the earlier step
        ImageAttributes imageAttributes = imagePersistenceManager.fetchImageById(imageId);

        //Write image to file
        DirectorUtil.writeImageToFile(imageFileInputStream, imageAttributes);

        //Save image meta data to the database
        ImageAttributes attributes = imagePersistenceManager.saveImageMetadata(imageAttributes);

        return DirectorUtil.mapImageAttributesToTrustDirectorImageUploadResponse(imageAttributes);
    }

    @Override
    public SearchImagesResponse searchImages(SearchImagesRequest searchImagesRequest) throws DbException {
        SearchImagesResponse searchImagesResponse = new SearchImagesResponse();
        List<ImageInfo> fetchImages = imagePersistenceManager.fetchImages(null);
        searchImagesResponse.images = fetchImages;
        return searchImagesResponse;
    }

    @Override
    public SearchFilesInImageResponse searchFilesInImage(SearchFilesInImageRequest searchFilesInImageRequest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param imageStoreUploadRequest
     * @return
     * @throws DirectorException
     * @throws ImageStoreException
     */
    @Override
    public ImageStoreResponse uploadImageToImageStore(ImageStoreRequest imageStoreUploadRequest) throws DirectorException, ImageStoreException {
        ImageStoreResponse uploadImage = imageStoreManager.uploadImage(imageStoreUploadRequest);
        //save image upload details to mw_image_upload
        return uploadImage;
    }

    /**
     * **************************************************************************************
     */
    /**
     * Setters and getters
     *
     * @param imagePersistenceManager
     * @param imageStoreManager
     * @return
     */
    @Autowired
    public ImageServiceImpl(IPersistService imagePersistenceManager, ImageStoreManager imageStoreManager) {
        this.imagePersistenceManager = imagePersistenceManager;
        this.imageStoreManager = imageStoreManager;
    }

}
