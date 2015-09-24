/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;

import java.io.File;
import java.util.Map;

/**
 *
 * @author GS-0681
 */
public interface ImageStoreManager {

    public void upload(File file, Map<String, String> imageProperties);

  /*  public ImageStoreResponse searchImages(ImageStoreRequest imageStoreSearchRequest);

    public ImageStoreResponse deleteImage(ImageStoreRequest imageStoreDeleteRequest);*/

   /// public ImageStoreResponse fetchImageUploadDetails(ImageStoreRequest imageStoreDeleteRequest);
}
