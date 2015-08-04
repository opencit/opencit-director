/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;

/**
 *
 * @author GS-0681
 */
public interface ImageStoreManager {

    public ImageStoreResponse uploadImage(ImageStoreRequest imageStoreUploadRequest);

    public ImageStoreResponse searchImages(ImageStoreRequest imageStoreSearchRequest);

    public ImageStoreResponse deleteImage(ImageStoreRequest imageStoreDeleteRequest);

    public ImageStoreResponse fetchImageDetails(ImageStoreRequest imageStoreDeleteRequest);
}
