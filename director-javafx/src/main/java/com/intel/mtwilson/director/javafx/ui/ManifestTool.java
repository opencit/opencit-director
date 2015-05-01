/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.ui;

import com.intel.mtwilson.director.javafx.utils.ConfigProperties;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author admkrushnakant
 */
public class ManifestTool extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            initConfigProperty();
            launch(args);
        } catch (Exception exception) {
            ErrorMessage.showErrorMessage(null, exception);
        }
    }
    
    @Override
    public void start(final Stage primaryStage) {
        try {
            ConfigurationInformation firstWindow = new ConfigurationInformation(primaryStage);
            firstWindow.launch();
        } catch (Exception exception) {
            ErrorMessage.showErrorMessage(primaryStage, exception);
        }
    }
    
    private static void initConfigProperty()throws Exception {
        ConfigProperties.loadProperty();
    }
}
