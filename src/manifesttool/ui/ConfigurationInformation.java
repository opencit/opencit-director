/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manifesttool.ui;

//import java.util.logging.Level;
//import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import static manifesttool.ui.AMIImageInformation.logger;
import manifesttool.utils.ConfigProperties;
import manifesttool.utils.FileUtilityOperation;
import manifesttool.utils.GenerateManifest;
import manifesttool.utils.LoggerUtility;
import manifesttool.utils.MountVMImage;

/**
 *
 * @author admkrushnakant
 */
public class ConfigurationInformation {    

    private final Stage primaryStage;
    
    private TextField customerIDTField;
    private TextField imageNameTField;
    private TextField imageIDTField;
    private TextField mtWilsonIPTField;
    private TextField mtWilsonPortTField;
    private TextField imageLocationTField;

    private ChoiceBox hashTypeChoiceBox;
    private ChoiceBox policyTypeChoiceBox;
    private ChoiceBox imageTypeChoiceBox;
    
    String hostManifest;
    
    private static final Logger logger; 
    // Set FileHandler for logger
    static {
        logger = Logger.getLogger(ConfigurationInformation.class.getName());
        LoggerUtility.setHandler(logger);
    }
    
    public ConfigurationInformation(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    public void launch() {
                
        // Check for the Host Manifest
        hostManifest = ConfigProperties.getProperty(Constants.HOST_MANIFEST);
        if(hostManifest != null) {
            hostManifest = hostManifest.trim();
        }
        
        primaryStage.setTitle("Configuration Information");
        
        final FileUtilityOperation op = new FileUtilityOperation();
        
        ObservableList<String> hashTypeList = FXCollections.observableArrayList(
            "SHA-256", "SHA1"
        );
        ObservableList<String> policyTypeList = FXCollections.observableArrayList(
            "Audit", "Enforce"
        );
        ObservableList<String> imageTypeList = FXCollections.observableArrayList(
            "qcow2", "raw", "vhd", "ami"
        );
        Label customerID = new Label("Customer ID");
        Label policyType = new Label("Policy Type");
        Label imageID = new Label("Image ID");
        Label imageName = new Label("Image Name");
        Label hashType = new Label("Hash Type");
        Label mtWilsonIP = new Label("Mt. Wilson IP");
        Label mtWilsonPort = new Label("Mt. Wilson Port");
        Label imageType = new Label("Image Type");
        Label imageLocation = new Label("Disk Image Path");
        
        customerIDTField = new TextField();
        imageNameTField = new TextField();
        imageIDTField = new TextField();
        mtWilsonIPTField = new TextField();
        mtWilsonPortTField = new TextField();
        imageLocationTField = new TextField();
        
        hashTypeChoiceBox = new ChoiceBox(hashTypeList);
        hashTypeChoiceBox.setValue("SHA-256");
        
        policyTypeChoiceBox = new ChoiceBox(policyTypeList);
        policyTypeChoiceBox.setValue("Enforce");
        
        imageTypeChoiceBox = new ChoiceBox(imageTypeList);
        imageTypeChoiceBox.setValue("qcow2");

        Button browseImage = new Button("Browse");
        browseImage.setPrefSize(80, 15);
        
        Tooltip toolTip = new Tooltip();
        toolTip.setText("For ami and vhd image format, provide the tar bundled image");
        browseImage.setTooltip(toolTip);
        Button nextButton = new Button("Browse Directories");
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);
        Button calculateImageHash = new Button("Only Image Hash");
        
        VBox vBox = new VBox();
        
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 10, 0, 10));
        
        grid.add(customerID, 0, 1);
        grid.add(customerIDTField, 1, 1);
        grid.add(hashType, 0, 2);
        grid.add(hashTypeChoiceBox, 1, 2);
        grid.add(imageName, 0, 3);
        grid.add(imageNameTField, 1, 3);
        grid.add(imageID, 0, 4);
        grid.add(imageIDTField, 1, 4);
        grid.add(policyType, 0, 5);
        grid.add(policyTypeChoiceBox, 1, 5);
        grid.add(mtWilsonIP, 0, 6);
        grid.add(mtWilsonIPTField, 1, 6);
        grid.add(mtWilsonPort, 0, 7);
        grid.add(mtWilsonPortTField, 1, 7);
        grid.add(imageType, 0, 8);
        grid.add(imageTypeChoiceBox, 1, 8);     
        
        // Set the Image ID
        String uuid = new UserConfirmation().getUUID();
        imageIDTField.setText(uuid);
        
        final HBox imageLocationHBox = new HBox();
        imageLocationHBox.setPadding(new Insets(3, 0, 5, 0));
        imageLocationHBox.setSpacing(10);

        HBox hBox4 = new HBox();
        hBox4.setPadding(new Insets(12, 10, 12, 10));
        hBox4.setSpacing(30);
        hBox4.setStyle("-fx-background-color: #336699;");

        imageLocationHBox.getChildren().addAll(imageLocationTField, browseImage);
        
        grid.add(imageLocation, 0, 9);
        grid.add(imageLocationHBox, 1, 9);      
        
        hBox4.getChildren().add(calculateImageHash);
        hBox4.getChildren().add(nextButton);
        hBox4.getChildren().add(cancelButton);
        
        
        vBox.getChildren().addAll(grid, hBox4);
        
	// Disable Unnecessary controls in case of Host Manifest generation
        if((hostManifest != null) && (hostManifest.equalsIgnoreCase("true"))) {
	    primaryStage.setTitle("Host Manifest Generation");
            customerIDTField.setDisable(true);
            imageNameTField.setDisable(true);
            imageIDTField.clear();
            imageIDTField.setDisable(true);
            imageTypeChoiceBox.setDisable(true);            
        }
        
        // Handler for "Next" button, Mount the disk image and browse the directories
        nextButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                boolean includeImageHash = false;
                
                // Write configuration values to map
                Map<String, String> customerInfo;
                if((hostManifest != null) && (hostManifest.equalsIgnoreCase("true"))) {
                    customerInfo = hostWriteToMap();
                } else {
                    customerInfo = writeToMap();
                }
                if (customerInfo != null) {
                    
                    Iterator it = customerInfo.entrySet().iterator();
                    logger.info("Configuration Values Are ");
                    while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry) it.next();
                        logger.info(pairs.getKey().toString() + " : " + pairs.getValue().toString());
                    }
                    
                    // Check for the "Host_Manifest" property in the property file, if present, manifest generation will be for host
                    if((hostManifest != null) && (hostManifest.equalsIgnoreCase("true"))) {
                        // Extract the compressed HOST image tgz file
                        String extractCommand = "tar zxf " + customerInfo.get(Constants.IMAGE_LOCATION) + " -C " + Constants.MOUNT_PATH;
                        int extractExitCode = MountVMImage.callExec(extractCommand);
                        if(extractExitCode != 0) {
                            showWarningPopup("Error while extracting .... Exiting .....");
                            System.exit(1);
                        } else {
                            BrowseDirectories secondWindow = new BrowseDirectories(primaryStage);
                            secondWindow.launch(customerInfo);                                                    
                        }                        
                    } else {
                        int exitCode = 21
                                ;
                        // Check for ami Image
                        if(customerInfo.get(Constants.IMAGE_TYPE).equals("ami")) {
                            
                            // Extract the compressed VM AMI Image
                            String extractedLocation = new File(customerInfo.get(Constants.IMAGE_LOCATION)).getParent() + "/extracted-ami";
                            boolean isExtracted = op.extractCompressedImage(customerInfo.get(Constants.IMAGE_LOCATION), extractedLocation);
                            if(!isExtracted) {
                                showWarningPopup("Error while extracting .... Exiting .....");
                                System.exit(1);
                            } else {
                                primaryStage.close();
                                // Get the AMI Image Information
                                new AMIImageInformation().getAMIImageInfo(primaryStage, customerInfo, extractedLocation, includeImageHash);   
                            }
                        } else {
                            // Mount the VM disk image
                            exitCode = MountVMImage.mountImage(imageLocationTField.getText());
                            if( exitCode == 0) {
                                if(new File(Constants.MOUNT_PATH + "/Windows/System32/ntoskrnl.exe").exists()) {
                                    customerInfo.put(Constants.IS_WINDOWS, "true");
                                } else {
                                    customerInfo.put(Constants.IS_WINDOWS, "false");
                                }
                                BrowseDirectories secondWindow = new BrowseDirectories(primaryStage);
                                secondWindow.launch(customerInfo);                        
                            } else {
                                logger.log(Level.SEVERE, "Error while mounting the image .. Exiting ....");
                                String warningMessage = "Error while mounting the image .. Exiting ....";
                                showWarningPopup(warningMessage);
                                System.exit(exitCode);
                            }
                        }
                    }
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
        
        // Calculates the hash of image, do not try to mount
        calculateImageHash.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                boolean includeImageHash = true;
                Map<String, String> customerInfo = writeToMap();
                if (customerInfo != null) {
                    Iterator it = customerInfo.entrySet().iterator();
                    logger.info("Configuration Values Are ");
                    while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry) it.next();
                        logger.info(pairs.getKey().toString() + " : " + pairs.getValue().toString());
                    }
                    
                    int exitCode = 21;
                    // Check for ami Image
                    if(customerInfo.get(Constants.IMAGE_TYPE).equals("ami")) {
                        // Extract the compressed VM AMI Image
                        String extractedLocation = new File(customerInfo.get(Constants.IMAGE_LOCATION)).getParent() + "/extracted-ami";
                        boolean isExtracted = op.extractCompressedImage(customerInfo.get(Constants.IMAGE_LOCATION), extractedLocation);
                        
                        if(!isExtracted) {
                            showWarningPopup("Error while extracting .... Exiting .....");
                            System.exit(1);
                        } else {
                            primaryStage.close();
                            // Get the AMI Image Information
                            new AMIImageInformation().getAMIImageInfo(primaryStage, customerInfo, extractedLocation, includeImageHash);                            
                        }
                    } else {
                        customerInfo.put(Constants.HIDDEN_FILES, "true");
                        String manifestFileLocation = new GenerateManifest().writeToXMLManifest(customerInfo);
                        if (manifestFileLocation != null) {
                            // Show the manifest file location
                            new UserConfirmation().showLocation(primaryStage, manifestFileLocation, customerInfo);
                        } else {
                            logger.log(Level.SEVERE, "Error in creating the manifest file...");
                            showWarningPopup("Error in creating the manifest file...");
                            System.exit(1);
                        }
                    }
                }   
            }
        });
        
        // Handler for 'Browse' button, browse the vm image
        browseImage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                FileChooser imageFile = new FileChooser();
                //imageFile.setInitialDirectory(new File(imagesOnFS));
                try {
                    File file = imageFile.showOpenDialog(primaryStage);
                    imageLocationTField.setText(file.getAbsolutePath());
                } catch(Exception e) {
                    logger.info("Not selected anything");
                }
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root);
        //scene.setFill(Color.AQUA);
        primaryStage.setScene(scene);
        primaryStage.show(); 
        
    }
    
    // Return the Stage
    public Stage getStage() {
        return this.primaryStage;
    }
    
    // Shwo the warning messages
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
        
        popup.show(primaryStage);
        
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
        if(("".equals(customerIDTField.getText())) || ("".equals(imageIDTField.getText())) || ("".equals(mtWilsonIPTField.getText())) || ("".equals(mtWilsonPortTField.getText())) || ("".equals(imageLocationTField.getText())) || ("".equals(imageNameTField.getText()))) {
            showWarningPopup("Some fields are Empty .. Please fill the Values");
            isProper = false;
        } else if(!opt.validateUUID(imageIDTField.getText())){
            showWarningPopup("Please provide the valid image ID");
            isProper = false;
        } else if(!opt.validateIPAddress(mtWilsonIPTField.getText())) {
            showWarningPopup("Please provide the valid Mt. Wilson IP address");
            isProper = false;
        }  else if(!opt.validatePort(mtWilsonPortTField.getText())) {
            showWarningPopup("Please provide the valid Mt. Wilson Port");
            isProper = false;
        } else if((imageTypeChoiceBox.getValue().toString().equals("ami") || imageTypeChoiceBox.getValue().toString().equals("vhd")) && !imageLocationTField.getText().endsWith(".tgz") && !imageLocationTField.getText().endsWith("tar.gz") && !imageLocationTField.getText().endsWith(".gz")) {
            showWarningPopup("Please provide the tar bundled image ");
            isProper = false;
        }
        if(isProper) {
            customerInfo.put(Constants.CUSTOMER_ID, customerIDTField.getText());
            customerInfo.put(Constants.IMAGE_NAME, imageNameTField.getText());
            customerInfo.put(Constants.IMAGE_ID, imageIDTField.getText());
            customerInfo.put(Constants.HASH_TYPE, hashTypeChoiceBox.getValue().toString());
            customerInfo.put(Constants.Mt_WILSON_IP, mtWilsonIPTField.getText());
            customerInfo.put(Constants.Mt_WILSON_PORT, mtWilsonPortTField.getText());
            customerInfo.put(Constants.IMAGE_LOCATION, imageLocationTField.getText());
            customerInfo.put(Constants.POLICY_TYPE, policyTypeChoiceBox.getValue().toString());
            customerInfo.put(Constants.IMAGE_TYPE, imageTypeChoiceBox.getValue().toString());                    
        } else {
            return null;
        }
        return customerInfo;
    }
    
    // Store configuration values in hash map for host manifest generation
    private Map<String, String> hostWriteToMap() {
        Map<String, String> customerInfo = new HashMap<>();
        boolean isProper = true;
        FileUtilityOperation opt = new FileUtilityOperation();
        if(("".equals(mtWilsonIPTField.getText())) || ("".equals(mtWilsonPortTField.getText())) || ("".equals(imageLocationTField.getText()))) {
            showWarningPopup("Some fields are Empty .. Please fill the Values");
            isProper = false;
        } else if(!opt.validateIPAddress(mtWilsonIPTField.getText())) {
            showWarningPopup("Please provide the valid Mt. Wilson IP address");
            isProper = false;
        } else if(!opt.validatePort(mtWilsonPortTField.getText())) {
            showWarningPopup("Please provide the valid Mt. Wilson Port");
            isProper = false;
        } 
        if(isProper) {
            customerInfo.put(Constants.CUSTOMER_ID, customerIDTField.getText());
            customerInfo.put(Constants.IMAGE_NAME, imageNameTField.getText());
            customerInfo.put(Constants.IMAGE_ID, imageIDTField.getText());
            customerInfo.put(Constants.HASH_TYPE, hashTypeChoiceBox.getValue().toString());
            customerInfo.put(Constants.Mt_WILSON_IP, mtWilsonIPTField.getText());
            customerInfo.put(Constants.Mt_WILSON_PORT, mtWilsonPortTField.getText());
            customerInfo.put(Constants.IMAGE_LOCATION, imageLocationTField.getText());
            customerInfo.put(Constants.POLICY_TYPE, policyTypeChoiceBox.getValue().toString());
            customerInfo.put(Constants.IMAGE_TYPE, imageTypeChoiceBox.getValue().toString());                    
        } else {
            return null;
        }
        return customerInfo;
    }
}
