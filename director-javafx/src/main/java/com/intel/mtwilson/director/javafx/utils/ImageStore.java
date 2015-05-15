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
public interface ImageStore {
    public String uploadImage(String imagePath, Map<String, String> imageProperties)throws Exception ;
    
    public String uploadTrustPolicy(String trustPolicyPath) throws Exception;
    
    public boolean updateImageProperty(String imageID, String propName, String propValue)throws Exception ;
}
