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
        initConfigProperty();
        launch(args);
    }
    
    @Override
    public void start(final Stage primaryStage) {
        //new WriteToPropertyFile().write();
        ConfigurationInformation firstWindow = new ConfigurationInformation(primaryStage);
        firstWindow.launch();
    }
    
    private static void initConfigProperty() {
        ConfigProperties.loadProperty();
    }
}
