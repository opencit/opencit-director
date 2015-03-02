/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.director.javafx.ui;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.intel.mtwilson.director.javafx.utils.LoggerUtility;
import com.intel.mtwilson.director.javafx.utils.MountVMImage;

/**
 *
 * @author root
 */
public class AMIImageInformation {
    public static Logger logger = Logger.getLogger(AMIImageInformation.class.getName());
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }
    public void getAMIImageInfo(final Stage primaryStage, final Map<String, String> confInfo, final String extractedLocation, final boolean includeImageHash) {
        
        primaryStage.setTitle("AMI Image Information");
        
        Label extractedInfo = new Label("VM Image extracted to \"" + extractedLocation + "\"");
        final Label imageLocation = new Label("Disk Image Path");
        final Label kernelPath = new Label("Kernel Path");
        final Label initrdPath = new Label("Initrd Path");
        final Label diskImagePath = new Label("Disk Image Path");

        final TextField imageLocationTField = new TextField();
        final TextField kernelTField = new TextField();
        final TextField initrdTField = new TextField();
        //TextField diskImageTField = new TextField();
        
        Button browseKernel = new Button("Browse");
        browseKernel.setPrefSize(80, 15);
        Button browseInitrd = new Button("Browse");
        browseInitrd.setPrefSize(80, 15);
        Button browseImage = new Button("Browse");
        browseImage.setPrefSize(80, 15);
        Button nextButton = new Button("Next");
        nextButton.setPrefSize(80, 15);
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);
        
        VBox vBox = new VBox();
        
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 10, 0, 10));
        
        HBox extractLocationHBox = new HBox();
        extractLocationHBox.setPadding(new Insets(12, 10, 12, 10));
        //extractLocationHBox.setSpacing(170);
        extractLocationHBox.setStyle("-fx-background-color: #336699;");
        
        final HBox kernelHBox = new HBox();
        kernelHBox.setPadding(new Insets(3, 0, 3, 0));
        kernelHBox.setSpacing(10);
        
        final HBox initrdHBox = new HBox();
        initrdHBox.setPadding(new Insets(3, 0, 3, 0));
        initrdHBox.setSpacing(10);
        
        final HBox imageLocationHBox = new HBox();
        imageLocationHBox.setPadding(new Insets(3, 0, 12, 0));
        imageLocationHBox.setSpacing(10);

        HBox hBox4 = new HBox();
        hBox4.setPadding(new Insets(12, 10, 12, 10));
        hBox4.setSpacing(214);
        hBox4.setStyle("-fx-background-color: #336699;");
        
        extractLocationHBox.getChildren().add(extractedInfo);
        kernelHBox.getChildren().addAll(kernelTField, browseKernel);
        initrdHBox.getChildren().addAll(initrdTField, browseInitrd);
        imageLocationHBox.getChildren().addAll(imageLocationTField, browseImage);
        
        grid.add(kernelPath, 0, 1);
        grid.add(kernelHBox, 1, 1);
        grid.add(initrdPath, 0, 2);
        grid.add(initrdHBox, 1, 2);
        grid.add(imageLocation, 0, 3);
        grid.add(imageLocationHBox, 1, 3);
        
        hBox4.getChildren().add(cancelButton);
        hBox4.getChildren().add(nextButton);
        
        
        vBox.getChildren().addAll(extractLocationHBox, grid, hBox4);
        
        // Handler for Browse Kernel
        browseKernel.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                FileChooser imageFile = new FileChooser();
                imageFile.setInitialDirectory(new File(extractedLocation));
                try {
                    File file = imageFile.showOpenDialog(primaryStage);
                    kernelTField.setText(file.getAbsolutePath());
                } catch(Exception e) {
                    logger.info("Not selected anything");
                    //System.out.println("Not selected anything");
                }
            }
        });
        
        // Handler for Browse Initrd
        browseInitrd.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                FileChooser imageFile = new FileChooser();
                imageFile.setInitialDirectory(new File(extractedLocation));
                try {
                    File file = imageFile.showOpenDialog(primaryStage);
                    initrdTField.setText(file.getAbsolutePath());
                } catch(Exception e) {
                    //System.out.println("Not selected anything");
                    logger.info("Not selected anything");
                }
            }
        });
        
        // Handler for Browse Disk Image
        browseImage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                FileChooser imageFile = new FileChooser();
                imageFile.setInitialDirectory(new File(extractedLocation));
                try {
                    File file = imageFile.showOpenDialog(primaryStage);
                    imageLocationTField.setText(file.getAbsolutePath());
                } catch(Exception e) {
                    //System.out.println("Not selected anything");
                    logger.info("Not selected anything");
                }
            }
        });
        
        // Handler for Next Button
        nextButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                boolean isEmpty = false;
                if(!imageLocationTField.getText().equals("")) {
                    if(!kernelTField.getText().equals("") && !initrdTField.getText().equals("")) {
                        confInfo.put(Constants.KERNEL_PATH, kernelTField.getText());
                        confInfo.put(Constants.INITRD_PATH, initrdTField.getText());
                    }
                    confInfo.put(Constants.IMAGE_LOCATION, imageLocationTField.getText());
                    
                    if(new File(Constants.MOUNT_PATH + "/Windows/System32/ntoskrnl.exe").exists()) {
                        confInfo.put(Constants.IS_WINDOWS, "true");
                    } else {
                        confInfo.put(Constants.IS_WINDOWS, "false");
                    }
                    
                    if(includeImageHash) {
                        String manifestFileLocation = null; // BS temp comment please look into it replace it with next line. new GenerateManifest().writeToXMLManifest(confInfo);
                        if (manifestFileLocation != null) {
                            // Show the manifest file location
//                            new UserConfirmation().showLocation(primaryStage, manifestFileLocation, confInfo);
                        } else {
                            //System.out.println("Error in creating the manifest file");
                            logger.log(Level.SEVERE, "Error in creating the manifest file");
                        }
                    } else {
                        int exitCode = MountVMImage.mountImage(imageLocationTField.getText());
                        if( exitCode == 0) {
//                            BrowseDirectories secondWindow = new BrowseDirectories(primaryStage);
//                            secondWindow.launch(confInfo);                        
                        } else {
                            //System.out.println("Exiting ....");
                            //System.exit(exitCode);
                            logger.log(Level.SEVERE, "Error while mounting the image .. Exiting ....");
                            String warningMessage = "Error while mounting the image .. Exiting ....";
//                            new ConfigurationInformation(primaryStage).showWarningPopup(warningMessage);
                            System.exit(exitCode);
                        }  
                    }                 
                } else {
//                    new ConfigurationInformation(primaryStage).showWarningPopup("Please provide the VM Disk Image Location !!!");
                }
            }
        });  

        // Handler for "Cancel" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Will close the window
                primaryStage.close();
            }
        });        

        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show(); 
    }    
}
