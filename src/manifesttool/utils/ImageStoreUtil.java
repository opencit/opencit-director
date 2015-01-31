/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manifesttool.utils;

import manifesttool.ui.Constants;

/**
 *
 * @author boskisha
 */
public class ImageStoreUtil {
    public static IImageStore getImageStore(){
        String imageStore = ConfigProperties.getProperty(Constants.IMAGE_STORE_TYPE);
        switch(imageStore){
            case(Constants.GLANCE_IMAGE_STORE):
                return new GlanceImageStoreImpl();    
            default:
                return null;
        }
        
    }
}
