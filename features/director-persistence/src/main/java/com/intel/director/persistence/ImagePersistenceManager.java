/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.persistence;

import com.intel.director.api.ImageAttributes;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author GS-0681
 */
public interface ImagePersistenceManager {

    public ImageAttributes saveImageMetadata(ImageAttributes img);

    public void updateImage(ImageAttributes img);

    public void destroyImage(ImageAttributes img);

    public List<ImageAttributes> fetchImagesBySearchCriteria(HashMap<String, String> searchMap,
            HashMap<String, String> orderByMap);

    public List<ImageAttributes> fetchImagesPaginatedBySearchCriteria(HashMap<String, String> searchMap,
            HashMap<String, String> orderByMap, int firstRecord, int maxRecords);

    public int getTotalImagesCount();

    public ImageAttributes fetchImageById(String id);

    public void pleaseAutowire();
}
