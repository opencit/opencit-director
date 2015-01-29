/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manifesttool.ui;

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
import static manifesttool.ui.AMIImageInformation.logger;
import manifesttool.utils.ConfigProperties;
import manifesttool.utils.FileUtilityOperation;
import manifesttool.utils.GenerateManifest;
import manifesttool.utils.LoggerUtility;
import manifesttool.utils.MHUtilityOperation;
import manifesttool.utils.MountVMImage;
import manifesttool.utils.UploadToGlance;

/**
 *
 * @author preetisr
 */
public class CreateImage {    

    private final Stage createImageStage;
    
    private TextField imagePathTField;
    private TextField imageNameTField;
    private TextField imageIDTField;
    private CheckBox encryptImage;
    private ChoiceBox imageFormatChoiceBox;
    private ChoiceBox hashTypeChoiceBox;
    private final ToggleGroup togBoxMeasure=new ToggleGroup();
    public static boolean manifestFlag=false;
    String hostManifest;
    String delimiter="/";
    
    private static final Logger logger; 
    // Set FileHandler for logger
    static {
        logger = Logger.getLogger(ConfigurationInformation.class.getName());
        LoggerUtility.setHandler(logger);
    }
    
    public CreateImage(Stage createImageStage) {
        this.createImageStage = createImageStage;
    }

//    public CreateImage() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    
    // Return the Stage
    public Stage getStage() {
        return this.createImageStage;
    }
  
    public void launch() {
                
        // Check for the Host Manifest
        hostManifest = ConfigProperties.getProperty(Constants.HOST_MANIFEST);
        if(hostManifest != null) {
            hostManifest = hostManifest.trim();
        }
        
        createImageStage.setTitle("Create Image");
        
        final FileUtilityOperation op = new FileUtilityOperation();
        
//        ObservableList<String> hashTypeList = FXCollections.observableArrayList(
//            "SHA-256", "SHA1"
//        );
        
        ObservableList<String> imageFormatList = FXCollections.observableArrayList(
            "qcow2", "raw", "vhd", "ami"
        );
       
        //PS: New label for Create Image window
        Label imageFormat=new Label("Image Format");
//        Label hashType=new Label ("Hash Type");
        Label imagePath=new Label("Image Path");
        Label chooseManifest=new Label("Choose Manifest");
        Label launchPolicy=new Label("Launch Control Policy");
        Label imageName = new Label("Image Name");
        Label imageID = new Label("Image ID");
        
        imagePathTField = new TextField();
        imageNameTField = new TextField();
        imageIDTField = new TextField();
        imageFormatChoiceBox = new ChoiceBox(imageFormatList);
        imageFormatChoiceBox.setValue("qcow2");
//        hashTypeChoiceBox=new ChoiceBox(hashTypeList);
        hashTypeChoiceBox.setValue("SHA-256");

        Button browseImage = new Button("Browse");
        browseImage.setPrefSize(80, 15);
        Tooltip toolTip = new Tooltip();
        toolTip.setText("For ami and vhd image format, provide the tar bundled image");
        browseImage.setTooltip(toolTip);
        
        final Button browseManifest = new Button("Next");
        browseManifest.setPrefSize(80, 15);
        Tooltip toolTipManifest = new Tooltip();
        toolTipManifest.setText("Browse the manifest files");
        browseImage.setTooltip(toolTipManifest);
       
        RadioButton rbMeasure=new RadioButton("Measure Only");
        rbMeasure.setToggleGroup(togBoxMeasure);
        rbMeasure.setUserData("MeasureOnly");
        rbMeasure.setSelected(true);
        RadioButton rbMeasureEnforce=new RadioButton("Measure and Enforce");
        rbMeasureEnforce.setUserData("MeasureEnforce");
        rbMeasureEnforce.setToggleGroup(togBoxMeasure);
        
        encryptImage=new CheckBox("Encrypt VM Image");
        encryptImage.setSelected(true);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);
        
        VBox vBox = new VBox();
        
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(30, 15,30, 15));
        
        grid.add(imageFormat, 0, 1);
        grid.add(imageFormatChoiceBox, 1, 1);   
              
//        grid.add(hashType, 0, 2);
//        grid.add(hashTypeChoiceBox, 1, 2);
        
        grid.add(imageID, 0, 3);
        grid.add(imageIDTField, 1,3);
        
        grid.add(imageName, 0, 4);
        grid.add(imageNameTField, 1, 4);
       
        final HBox imagePathHBox = new HBox();
        imagePathHBox.setPadding(new Insets(3, 0, 5, 0));
        imagePathHBox.setSpacing(10);
        imagePathHBox.getChildren().addAll(imagePathTField, browseImage);
        
        grid.add(imagePath,0,5);
        grid.add(imagePathHBox, 1, 5);

        final HBox launchPolicyHBox = new HBox();
        launchPolicyHBox.setPadding(new Insets(3, 0, 5, 0));
        launchPolicyHBox.setSpacing(10);
        launchPolicyHBox.getChildren().add(rbMeasure);
        launchPolicyHBox.getChildren().add(rbMeasureEnforce);
        grid.add(launchPolicy,0,6);
        grid.add(launchPolicyHBox, 1, 6);

        grid.add(encryptImage,0,7);
       
        // Set the Image ID
        String uuid = new UserConfirmation().getUUID();
        imageIDTField.setText(uuid);
        
        
        
        HBox hBox4 = new HBox();
        hBox4.setPadding(new Insets(10, 12, 10, 12));
        hBox4.setSpacing(30);
        hBox4.setStyle("-fx-background-color: #336699;");
        hBox4.getChildren().add(cancelButton);
        hBox4.getChildren().add(browseManifest);
        
        
        vBox.getChildren().addAll(grid,hBox4);
        
	// Disable Unnecessary controls in case of Host Manifest generation
        if((hostManifest != null) && (hostManifest.equalsIgnoreCase("true"))) {
	    createImageStage.setTitle("Trust Policy Generation");
            imageNameTField.setDisable(true);
            imageIDTField.clear();
            imageIDTField.setDisable(true);
            imageFormatChoiceBox.setDisable(true);            
        }
        
        // Handler for 'Browse' for Image Path button, browse the vm image
        browseImage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                FileChooser imageFile = new FileChooser();
                try {
                    File file = imageFile.showOpenDialog(createImageStage);
                    imagePathTField.setText(file.getAbsolutePath());
                    
                    if(imagePathTField.getText()!=null){
                        String autoComplete=imagePathTField.getText();
                        int index=autoComplete.lastIndexOf(delimiter);
                        autoComplete=autoComplete.substring(index+1,autoComplete.length()-4);
                        
                        imageNameTField.setText(autoComplete);
                    }
                } catch(Exception e) {
                    logger.info("Not selected anything");
                }
            }
        });
        
        
        
        // Handler for "NEXT" Button
        browseManifest.setOnAction(new EventHandler<ActionEvent>() {

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
                    
//                    //Check for the "Host_Manifest" property in the property file, if present, manifest generation will be for host
//                    if((hostManifest != null) && (hostManifest.equalsIgnoreCase("true"))) {
//                        // Extract the compressed HOST image tgz file
//                        String extractCommand = "tar zxf " + customerInfo.get(Constants.IMAGE_LOCATION) + " -C " + Constants.MOUNT_PATH;
//                        int extractExitCode = MountVMImage.callExec(extractCommand);
//                        if(extractExitCode != 0) {
//                            showWarningPopup("Error while extracting .... Exiting .....");
//                            System.exit(1);
//                        } else {
//                            BrowseDirectories secondWindow = new BrowseDirectories(createImageStage);
//                            secondWindow.launch(customerInfo);                                                    
//                        }                        
//                    } else {
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
                                createImageStage.close();
                                // Get the AMI Image Information
                                new AMIImageInformation().getAMIImageInfo(createImageStage, customerInfo, extractedLocation, includeImageHash);   
                            }
                        } else {
                            // Mount the VM disk image
                            exitCode = MountVMImage.mountImage(imagePathTField.getText());
                            // Mount the VM disk image
                            exitCode = MountVMImage.mountImage(imagePathTField.getText());

                            if( exitCode == 0) {
                                if(new File(Constants.MOUNT_PATH + "/Windows/System32/ntoskrnl.exe").exists()) {
                                    customerInfo.put(Constants.IS_WINDOWS, "true");
                                } else {
                                    customerInfo.put(Constants.IS_WINDOWS, "false");
                                }
                                BrowseDirectories secondWindow = new BrowseDirectories(createImageStage);
                                secondWindow.launch(customerInfo);                        
                            } else {
                                logger.log(Level.SEVERE, "Error while mounting the image .. Exiting ....");
                                String warningMessage = "Error while mounting the image .. Exiting ....";
//                                showWarningPopup(warningMessage);
                                System.exit(exitCode);
                            }
                        }
//                    }
                }    
            }
        });
        
        // Handler for "Cancel" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Will close the window
                createImageStage.close();
                ConfigurationInformation window = new ConfigurationInformation(createImageStage);
                window.launch();
                
            }
        });
        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root);
        //scene.setFill(Color.AQUA);
        createImageStage.setScene(scene);
        createImageStage.show(); 
        
    }
    
    
    
    //Show the Target location and Manifest location
    private void showUploadSuccessMessage(final Stage primaryStage, String messageInfo) {
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
        
        popup.show(createImageStage);
        
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
        if(("".equals(imageIDTField.getText())) || ("".equals(imagePathTField.getText())) || ("".equals(imageNameTField.getText()))) {
//            showWarningPopup("Some fields are Empty .. Please fill the Values");
            isProper = false;
        } else if(!opt.validateUUID(imageIDTField.getText())){
//            showWarningPopup("Please provide the valid image ID");
            isProper = false;
        }  else if((imageFormatChoiceBox.getValue().toString().equals("ami") || imageFormatChoiceBox.getValue().toString().equals("vhd")) && !imagePathTField.getText().endsWith(".tgz") && !imagePathTField.getText().endsWith("tar.gz") && !imagePathTField.getText().endsWith(".gz")) {
//            showWarningPopup("Please provide the tar bundled image ");
            isProper = false;
        }         
        if(isProper) {
            customerInfo.put(Constants.IMAGE_NAME, imageNameTField.getText());
            customerInfo.put(Constants.IMAGE_ID, imageIDTField.getText());
            customerInfo.put(Constants.IMAGE_LOCATION, imagePathTField.getText());
            customerInfo.put(Constants.IMAGE_TYPE, imageFormatChoiceBox.getValue().toString()); 
            customerInfo.put(Constants.HASH_TYPE, hashTypeChoiceBox.getValue().toString());
            customerInfo.put(Constants.POLICY_TYPE,togBoxMeasure.getSelectedToggle().getUserData().toString());
            if(encryptImage.isSelected()){
            customerInfo.put(Constants.IS_ENCRYPTED,"true");
            }else{
             customerInfo.put(Constants.IS_ENCRYPTED,"false");
            }
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
        if(("".equals(imagePathTField.getText()))) {
//            showWarningPopup("Some fields are Empty .. Please fill the Values");
            isProper = false;
        } 
        if(isProper) {
            customerInfo.put(Constants.IMAGE_NAME, imageNameTField.getText());
            customerInfo.put(Constants.IMAGE_ID, imageIDTField.getText());
            customerInfo.put(Constants.IMAGE_LOCATION, imagePathTField.getText());
            customerInfo.put(Constants.IMAGE_TYPE, imageFormatChoiceBox.getValue().toString());
        } else {
            return null;
        }
        return customerInfo;
    }
}
