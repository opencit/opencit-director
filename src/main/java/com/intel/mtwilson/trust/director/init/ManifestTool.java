/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manifesttool.init;

import manifesttool.ui.ConfigurationInformation;
import javafx.application.Application;
import javafx.stage.Stage;
import manifesttool.utils.ConfigProperties;
import manifesttool.utils.LoggerUtility;

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
        initLogger();
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

    private static void initLogger() {
        LoggerUtility.initializeHandler();
    }
}
