/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.persistence.impl;

import com.intel.director.api.ImageAttributes;
import com.intel.director.persistence.ImagePersistenceManager;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author GS-0681
 */
public class ImagePersistenceManagerImpl implements ImagePersistenceManager {

    @Override
    public ImageAttributes saveImageMetadata(ImageAttributes img) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateImage(ImageAttributes img) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroyImage(ImageAttributes img) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ImageAttributes> fetchImagesBySearchCriteria(HashMap<String, String> searchMap, HashMap<String, String> orderByMap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ImageAttributes> fetchImagesPaginatedBySearchCriteria(HashMap<String, String> searchMap, HashMap<String, String> orderByMap, int firstRecord, int maxRecords) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getTotalImagesCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ImageAttributes fetchImageById(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pleaseAutowire() {
        System.out.println("22222222222222: Inside ImagePersistenceManagerImpl.pleaseAutoWire");
    }

}
