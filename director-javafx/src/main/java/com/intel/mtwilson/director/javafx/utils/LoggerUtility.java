/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.director.javafx.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author root
 */
public class LoggerUtility {
    private static FileHandler fileHandler;
    
    public static void setHandler(Logger logger) {
        initializeHandler();
        logger.setUseParentHandlers(false);
        logger.addHandler(fileHandler);
    }
    
    public static void initializeHandler() {
        try {
            fileHandler = new FileHandler("./log/manifest-tool.log",true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (IOException ex) {
            Logger.getLogger(LoggerUtility.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(LoggerUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
