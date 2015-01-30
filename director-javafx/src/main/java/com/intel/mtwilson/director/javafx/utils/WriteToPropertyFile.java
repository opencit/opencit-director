package com.intel.mtwilson.director.javafx.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.intel.mtwilson.director.javafx.ui.Constants;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author root
 */
public class WriteToPropertyFile {
    private String propertyFile = "config.properties";
    public void write() {
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            output = new FileOutputStream(new File(propertyFile));
            prop.setProperty(Constants.GLANCE_IP, "10.35.35.10");
            prop.setProperty(Constants.USER_NAME, "admin");
            prop.setProperty(Constants.PASSWORD, "intelrp");
            prop.setProperty(Constants.TENANT_NAME, "admin");
            
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
}
