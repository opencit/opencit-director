/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.trust.director.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public class ConfigProperties {
    
    public static Properties prop;
    static Logger logger = Logger.getLogger(ConfigProperties.class.getName());
    
    public static void loadProperty() {
        prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("/opt/trustdirector/configuration/config.properties");
            prop.load(input);  
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            if(input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
/*    
    public static void loadPropertyForWrite(String propertyFile) {
        prop = new Properties();
        OutputStream output = null;
        try {
            output = new FileOutputStream(new File(propertyFile));
            prop.setProperty("Glance_IP", "10.35.35.10");
            prop.setProperty("User_Name", "admin");
            prop.setProperty("Password", "intelrp");
            prop.setProperty("Tenant_Name", "admin");
            
            prop.store(output, null);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(WriteToPropertyFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
        	}
            }
        }
    }
*/    
    public static String getProperty(String name) {
        return prop.getProperty(name);
    }
    
    // Implementation is pending
    public static void setProperty(String name, String value) {
        OutputStream output = null;
    }
}
