/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.ui;

import com.intel.mtwilson.director.javafx.utils.ConfigProperties;
import com.intel.mtwilson.director.javafx.utils.FileUtilityOperation;
import com.intel.mtwilson.director.javafx.utils.LoggerUtility;
import com.intel.mtwilson.director.javafx.utils.LoggerUtility;
import com.intel.mtwilson.director.javafx.utils.IImageStore;
import com.intel.mtwilson.director.javafx.utils.ImageStoreException;
import com.intel.mtwilson.director.javafx.utils.ImageStoreUtil;
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

/**
 *
 * @author preetisr
 */
public class UploadExisting {    

    private final Stage uploadExistingStage;
    
    private TextField imagePathTField;
    private TextField imageNameTField;
    private TextField manifestPathTField;
    private final ToggleGroup togBoxMeasure=new ToggleGroup();
    private ConfigProperties configProperties;
    
    String hostManifest;
    
    private static final Logger logger; 
    // Set FileHandler for logger
    static {
        logger = Logger.getLogger(ConfigurationInformation.class.getName());
        LoggerUtility.setHandler(logger);
    }
    
    public UploadExisting(Stage uploadExistingStage) {
        this.uploadExistingStage = uploadExistingStage;
        configProperties = new ConfigProperties();;
    }

    
    // Return the Stage
    public Stage getStage() {
        return this.uploadExistingStage;
    }
    
    
    public void launch() {
//                
        // Check for the Host Manifest
        try {
            hostManifest = configProperties.getProperty(Constants.HOST_MANIFEST);
            if (hostManifest != null) {
                hostManifest = hostManifest.trim();
            }
        } catch (NullPointerException npe) {
            logger.log(Level.WARNING, "Host manifest value not set in configuration file.", npe);
        }

        uploadExistingStage.setTitle("Upload Existing image");
//        
//        final FileUtilityOperation op = new FileUtilityOperation();
//        
//        
        //PS: New label for Create Image window
        Label imagePath=new Label("Image Path");
        Label imageName = new Label("Image Name");
        Label manifestPath = new Label("Trust Policy path");
        
        imagePathTField = new TextField();
        imageNameTField = new TextField();
        manifestPathTField=new TextField();

        Button browseImage = new Button("Browse");
        browseImage.setPrefSize(80, 15);
        Tooltip toolTip = new Tooltip();
        toolTip.setText("For ami and vhd image format, provide the tar bundled image");
        browseImage.setTooltip(toolTip);
        
        final Button browseManifest = new Button("Browse");
        browseManifest.setPrefSize(80, 15);
        Tooltip toolTipManifest = new Tooltip();
        toolTipManifest.setText("Browse the manifest files");
        browseManifest.setTooltip(toolTipManifest);
//        
        Button uploadButton=new Button("Upload");
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);
      
        VBox vBox = new VBox();
  
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(30, 15,30, 15));
        
        grid.add(imageName, 0,1);
        grid.add(imageNameTField, 1,1);
        
        final HBox imagePathHBox = new HBox();
        imagePathHBox.setPadding(new Insets(3, 0, 5, 0));
        imagePathHBox.setSpacing(10);
        imagePathHBox.getChildren().addAll(imagePathTField, browseImage);
        grid.add(imagePath,0,2);
        grid.add(imagePathHBox, 1,2);
        
        final HBox manifestPathHBox = new HBox();
        manifestPathHBox.setPadding(new Insets(3, 0, 5, 0));
        manifestPathHBox.setSpacing(10);
        manifestPathHBox.getChildren().addAll(manifestPathTField, browseManifest);
        grid.add(manifestPath, 0, 3);
        grid.add(manifestPathHBox, 1,3);
  
        HBox hBox4 = new HBox();
        hBox4.setPadding(new Insets(10, 12, 10, 12));
        hBox4.setSpacing(30);
        hBox4.setStyle("-fx-background-color: #336699;");
        hBox4.getChildren().add(uploadButton);
        hBox4.getChildren().add(cancelButton);
        
        
        vBox.getChildren().addAll(grid,hBox4);
     
	// Handler for 'Browse' for Trust Policy Path button, browse the vm image
        browseImage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                FileChooser imageFile = new FileChooser();
                //imageFile.setInitialDirectory(new File(imagesOnFS));
                try {
                    File file = imageFile.showOpenDialog(uploadExistingStage);
                    imagePathTField.setText(file.getAbsolutePath());
                } catch(Exception e) {
                    logger.info("Not selected anything");
                }
            }
        });
       
        // Handler for 'Browse' for Image Path button, browse the vm image
        browseManifest.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                FileChooser manifestFile = new FileChooser();
                //imageFile.setInitialDirectory(new File(imagesOnFS));
                try {
                    File file = manifestFile.showOpenDialog(uploadExistingStage);
                    manifestPathTField.setText(file.getAbsolutePath());
                } catch(Exception e) {
                    logger.info("Not selected anything");
                }
            }
        });
        
         // Handler for "Cancel" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Will close the window
                uploadExistingStage.close();
                
            }
        });     
        
        //Upload button
        uploadButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                try {
                //Generate the Manifest, encrypt the image
                String manifestFileLocation = manifestPathTField.getText();
                String imagePathLocation=imagePathTField.getText();
                
                //Upload to the Glance
                    String message;
                    message = UploadNow(manifestFileLocation);
                showUploadSuccessMessage(uploadExistingStage, message);
                } catch (ImageStoreException ex) {
                    Logger.getLogger(UploadExisting.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        
        // Handler for "Cancel" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Will close the window
                uploadExistingStage.close();
                
            }
        });
  
        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root);
        //scene.setFill(Color.AQUA);
        uploadExistingStage.setScene(scene);
        uploadExistingStage.show(); 
      }
    
    
    private String UploadNow(String manifestFileLocation) throws ImageStoreException{
        Map<String, String> customerInfo = writeToMap();
                String message="";
            try{
                UserConfirmation userObj=new UserConfirmation();
            IImageStore imageStoreObj = ImageStoreUtil.getImageStore();
                      boolean isEncrypted = (Boolean)null;
                     System.out.println("PSDebug Encrypted and saved the manifest and the image to upload NOW");
                     System.out.println("PSDebug Image Loc is" + imagePathTField.getText());
            message=imageStoreObj.uploadImage(imagePathTField.getText(), null);
                    System.out.println("PSDebug Upload done");
                    showUploadSuccessMessage(uploadExistingStage, message);
            
        }catch(NullPointerException e){
            throw new ImageStoreException(e);
        }
            return message;
    }
    
    //Show the Target location and Manifest location
    private void showUploadSuccessMessage(final Stage primaryStage, String messageInfo) {
        //primaryStage.setTitle("Upload Success Message");
        //String info = "Manifest Uploaded on glance " + "\n" + "Manifest Glance ID is : " + manifestGlanceID;;
        Label message = new Label(messageInfo);
        message.setFont(new Font("Arial", 14));
        
        Button okButton = new Button("Ok");
        okButton.setPrefSize(80, 20);
        
        VBox vbox = new VBox();
        vbox.setSpacing(30);
        vbox.setPadding(new Insets(15, 12, 15, 12));
        
        vbox.getChildren().addAll(message, okButton);
        
        okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                primaryStage.close();
//                createImageStage.close();
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(vbox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();         
        
    }
    
    
    // Show the warning messages
    public void showWarningPopup(String warnMessage) {
        
        Text message = new Text(warnMessage);
        message.setFont(new Font("Arial", 14));
        Button okButton = new Button("Ok");
        okButton.setPrefSize(80, 15);
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(12, 10, 12, 10));
        vbox.setSpacing(20);
        vbox.setStyle("-fx-background-color: #336699;");
        vbox.getChildren().addAll(message, okButton);
        final Popup popup = new Popup();
        popup.setX(400);
        popup.setY(250);
        Stage stage = new Stage();
        popup.getContent().addAll(vbox);
        
        popup.show(uploadExistingStage);
        
        okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                popup.hide();
            }
        }); 
    }
    
    // Store configuration values in hash map
    private Map<String, String> writeToMap() {
        Map<String, String> customerInfo = new HashMap<>();
        boolean isProper = true;
        FileUtilityOperation opt = new FileUtilityOperation();
        if(("".equals(manifestPathTField.getText())) || ("".equals(imagePathTField.getText())) || ("".equals(imageNameTField.getText()))) {
//            showWarningPopup("Some fields are Empty .. Please fill the Values");
            isProper = false;
        } else if(!opt.validateUUID(manifestPathTField.getText())){
//            showWarningPopup("Please provide the valid image ID");
            isProper = false;
        }           
        if(isProper) {
            customerInfo.put(Constants.IMAGE_NAME, imageNameTField.getText());
            customerInfo.put(Constants.TRUST_POLICY_LOCATION , manifestPathTField.getText());
            customerInfo.put(Constants.IMAGE_LOCATION, imagePathTField.getText());
        } else {
            return null;
        }
        return customerInfo;
    }
}
    