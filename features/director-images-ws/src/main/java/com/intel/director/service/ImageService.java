package com.intel.director.service;

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
import com.intel.director.exception.ImageStoreException;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.images.exception.ImageMountException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * this interface serves the purpose of wrapping all the methods that the web
 * service would need. This service implementation would internally call the
 * DAO, the XML generation class etc.
 *
 * @author Siddharth
 */
public interface ImageService {

    public MountImageResponse mountImage(String imageId, String user) throws ImageMountException;

    public UnmountImageResponse unMountImage(String imageId, String user) throws ImageMountException;

    public TrustDirectorImageUploadResponse uploadImageMetaDataToTrustDirector(TrustDirectorImageUploadRequest trustDirectorImageUploadRequest);

    public TrustDirectorImageUploadResponse uploadImageToTrustDirector(String imageId, InputStream imageFileInputStream) throws FileNotFoundException, IOException;

    public SearchImagesResponse searchImages(SearchImagesRequest searchImagesRequest);

    public SearchFilesInImageResponse searchFilesInImage(SearchFilesInImageRequest searchFilesInImageRequest);

    public ImageStoreResponse uploadImageToImageStore(ImageStoreRequest imageStoreUploadRequest) throws DirectorException, ImageStoreException;

    public void pleaseAutoWire();

}
