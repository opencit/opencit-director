/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.director.javafx.ui;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import static java.awt.Color.red;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import com.intel.mtwilson.director.javafx.utils.ConfigProperties;
import com.intel.mtwilson.director.javafx.utils.FileUtilityOperation;
import com.intel.mtwilson.director.javafx.utils.LoggerUtility;
import com.intel.mtwilson.director.javafx.utils.MHUtilityOperation;
import com.intel.mtwilson.director.javafx.utils.MountVMImage;
import com.intel.mtwilson.director.javafx.utils.GlanceImageStoreImpl;

/**
 *
 * @author preetisr
 */
public class LocalSystem {
    private final Stage localSystemStage;
    private ConfigProperties configProperties;
   private final ToggleGroup togBoxMeasure=new ToggleGroup();    
   String hostManifest;
    
    private static final Logger logger; 
    // Set FileHandler for logger
    static {
        logger = Logger.getLogger(ConfigurationInformation.class.getName());
        LoggerUtility.setHandler(logger);
    }
    
    
    public LocalSystem(Stage localSystemStage) {
        this.localSystemStage = localSystemStage;
        configProperties = new ConfigProperties();
    }
    
    public void launch() {
     
        // Check for the Host Manifest
        hostManifest = configProperties.getProperty(Constants.HOST_MANIFEST);
        if(hostManifest != null) {
            hostManifest = hostManifest.trim();
        }

        localSystemStage.setTitle("Bare Metal Host System");
        
        //PS: New label for Create Image window
//        Label chooseManifest=new Label("Choose Manifest");
        Label launchPolicy=new Label("Launch Control Policy");
                
//        final Button browseManifest = new Button("Browse");
//        browseManifest.setPrefSize(80, 15);
//        Tooltip toolTipManifest = new Tooltip();
//        toolTipManifest.setText("Browse the manifest files");
//        browseManifest.setTooltip(toolTipManifest);

        RadioButton rbMeasure=new RadioButton("Measure Only");
        rbMeasure.setToggleGroup(togBoxMeasure);
        rbMeasure.setUserData("MeasureOnly");
        rbMeasure.setSelected(true);
        RadioButton rbMeasureEnforce=new RadioButton("Measure and Enforce");
        rbMeasureEnforce.setUserData("MeasureEnforce");
        rbMeasureEnforce.setToggleGroup(togBoxMeasure);
        
        Button NextButton=new Button("Next");
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);
      
        VBox vBox = new VBox();
  
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(30, 15,30, 15));
          
        
        final HBox launchPolicyHBox = new HBox();
        launchPolicyHBox.setPadding(new Insets(3, 0, 5, 0));
        launchPolicyHBox.setSpacing(10);
        launchPolicyHBox.getChildren().add(rbMeasure);
        launchPolicyHBox.getChildren().add(rbMeasureEnforce);
        grid.add(launchPolicy,0,1);
        grid.add(launchPolicyHBox, 1,1);
        
//        grid.add(chooseManifest,0,2);
//        grid.add(browseManifest,1,2);
  
        HBox hBox4 = new HBox();
        hBox4.setPadding(new Insets(20, 12, 20, 12));
        hBox4.setSpacing(30);
        hBox4.setStyle("-fx-background-color: #336699;");
        hBox4.getChildren().add(cancelButton);
        hBox4.getChildren().add(NextButton);
        
        
        
        vBox.getChildren().addAll(grid,hBox4);
     
	       
       // Handler for "Browse" button to Choose Manifest, Mount the disk image and browse the directories
        NextButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                boolean includeImageHash = false;
//                 Write configuration values to map
                Map<String, String> customerInfo;
                customerInfo = hostWriteToMap();
                int exitCode = 0;
//                if((hostManifest != null) && (hostManifest.equalsIgnoreCase("true"))) {
//                 Extract the compressed HOST image tgz file
//                String extractCommand = "mount /"; 
//                int extractExitCode = MountVMImage.callExec(extractCommand);
//                if(extractExitCode != 0) {
////               showWarningPopup("Error while extracting .... Exiting .....");
//                 System.exit(1);
//                } else {
                        BrowseDirectories secondWindow = new BrowseDirectories(localSystemStage);
                         secondWindow.launch(customerInfo);                                                    
//                }                         
            }
        });
        
        // Handler for "Cancel" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Will close the window
                localSystemStage.close();
                
            }
        });
  
        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root);
        //scene.setFill(Color.AQUA);
        localSystemStage.setScene(scene);
        localSystemStage.show(); 
      }
    
    // Store configuration values in hash map for host manifest generation
    private Map<String, String> hostWriteToMap() {
        Map<String, String> customerInfo = new HashMap<>();
        boolean isProper = true;
        FileUtilityOperation opt = new FileUtilityOperation();
         
        if(isProper) {
          customerInfo.put(Constants.POLICY_TYPE,togBoxMeasure.getSelectedToggle().getUserData().toString());
          customerInfo.put((Constants.BARE_METAL),"true");

        } else {
            return null;
        }
        return customerInfo;
    }
    
    

    
}
