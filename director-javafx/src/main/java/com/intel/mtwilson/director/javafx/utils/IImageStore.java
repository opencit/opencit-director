/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

import java.util.Map;

/**
 *
 * @author boskisha
 */
public interface IImageStore {
    public String uploadImage(String imagePath, Map<String, String> imageProperties);
    
    public String uploadTrustPolicy(String trustPolicyPath);
    
    public boolean updateImageProperty(String imageID, String propName, String propValue);
}
