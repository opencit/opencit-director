/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

import com.intel.mtwilson.director.javafx.ui.Constants;

/**
 *
 * @author boskisha
 */
public class ImageStoreUtil {

    public static ImageStore getImageStore(){
        ConfigProperties configProperties=new ConfigProperties();
        String imageStore = configProperties.getProperty(Constants.IMAGE_STORE_TYPE);
        switch(imageStore){
            case(Constants.GLANCE_IMAGE_STORE):
                return new GlanceImageStoreImpl();    
            default:
                return null;
        }
        
    }
}
