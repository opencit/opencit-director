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
    
    String hostManifest;
    
    
    private static final Logger logger; 
    // Set FileHandler for logger
    static {
        logger = Logger.getLogger(ConfigurationInformation.class.getName());
        LoggerUtility.setHandler(logger);
    }
    
    public CreateImage(Stage createImageStage) {
        this.createImageStage = createImageStage;
    }

    public CreateImage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
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
        
        ObservableList<String> hashTypeList = FXCollections.observableArrayList(
            "SHA-256", "SHA1"
        );
        
        ObservableList<String> imageFormatList = FXCollections.observableArrayList(
            "qcow2", "raw", "vhd", "ami"
        );
        
        
        
        //PS: New label for Create Image window
        Label imageFormat=new Label("Image Format");
        Label hashType=new Label ("Hash Type");
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
        hashTypeChoiceBox=new ChoiceBox(hashTypeList);
        hashTypeChoiceBox.setValue("SHA-256");

        Button browseImage = new Button("Browse");
        browseImage.setPrefSize(80, 15);
        Tooltip toolTip = new Tooltip();
        toolTip.setText("For ami and vhd image format, provide the tar bundled image");
        browseImage.setTooltip(toolTip);
        
        final Button browseManifest = new Button("Browse");
        browseManifest.setPrefSize(80, 15);
        Tooltip toolTipManifest = new Tooltip();
        toolTipManifest.setText("Browse the manifest files");
        browseImage.setTooltip(toolTipManifest);
        
        Button saveButton=new Button("Upload Later");
        Button uploadButton=new Button("Upload");
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);
        
//        final ToggleGroup togBoxMeasure=new ToggleGroup();
        
        RadioButton rbMeasure=new RadioButton("Measure Only");
        rbMeasure.setToggleGroup(togBoxMeasure);
        rbMeasure.setUserData("MeasureOnly");
        rbMeasure.setSelected(true);
        RadioButton rbMeasureEnforce=new RadioButton("Measure and Enforce");
        rbMeasureEnforce.setUserData("MeasureEnforce");
        rbMeasureEnforce.setToggleGroup(togBoxMeasure);
        
        encryptImage=new CheckBox("Encrypt VM Image");
        encryptImage.setSelected(true);
        
        VBox vBox = new VBox();
        
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(30, 15,30, 15));
        
        grid.add(imageFormat, 0, 1);
        grid.add(imageFormatChoiceBox, 1, 1);   
              
        grid.add(hashType, 0, 2);
        grid.add(hashTypeChoiceBox, 1, 2);
        
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
//        grid.add(imagePathTField,1,4);
//        grid.add(browseImage,2,4);
        
        final HBox launchPolicyHBox = new HBox();
        launchPolicyHBox.setPadding(new Insets(3, 0, 5, 0));
        launchPolicyHBox.setSpacing(10);
        launchPolicyHBox.getChildren().add(rbMeasure);
        launchPolicyHBox.getChildren().add(rbMeasureEnforce);
        grid.add(launchPolicy,0,6);
        grid.add(launchPolicyHBox, 1, 6);

        grid.add(encryptImage,0,7);
        
        grid.add(chooseManifest,0,8);
        grid.add(browseManifest,1,8);
        
        
        // Set the Image ID
        String uuid = new UserConfirmation().getUUID();
        imageIDTField.setText(uuid);
        
        
        
        HBox hBox4 = new HBox();
        hBox4.setPadding(new Insets(10, 12, 10, 12));
        hBox4.setSpacing(30);
        hBox4.setStyle("-fx-background-color: #336699;");
        hBox4.getChildren().add(saveButton);
        hBox4.getChildren().add(uploadButton);
        hBox4.getChildren().add(cancelButton);
        
        
        vBox.getChildren().addAll(grid,hBox4);
        
	// Disable Unnecessary controls in case of Host Manifest generation
        if((hostManifest != null) && (hostManifest.equalsIgnoreCase("true"))) {
	    createImageStage.setTitle("Host Manifest Generation");
//            customerIDTField.setDisable(true);
            imageNameTField.setDisable(true);
            imageIDTField.clear();
            imageIDTField.setDisable(true);
            imageFormatChoiceBox.setDisable(true);            
        }
        
        // Handler for "Browse" button to Choose Manifest, Mount the disk image and browse the directories
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
                    
                        int exitCode = 0;
                            // Mount the VM disk image
                        System.out.println("mount image:" + imagePathTField.getText());
                            exitCode = MountVMImage.mountImage(imagePathTField.getText());
                            System.out.println("exit code in mount image:" + exitCode) ;
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
//                        }
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
                
            }
        });
        

        
        //PS: Save button action: generates Trust Policy and encrypts the image if encrypt option is chosen
        saveButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
//                boolean includeImageHash = true;
                
                String manifestFileLocation = new GenerateManifest().writeToXMLManifest();
                String message=EncryptImage(manifestFileLocation);
                showUploadSuccessMessage(createImageStage, message);
            }
        });
        
        //PS: Upload image
        uploadButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                //Generate the Manifest, encrypt the image
                String manifestFileLocation = new GenerateManifest().writeToXMLManifest();
//                String message=EncryptImage(manifestFileLocation);
//                showUploadSuccessMessage(createImageStage, message);
                
                //Upload to the Glance
                String message =UploadNow(manifestFileLocation);
                showUploadSuccessMessage(createImageStage, message);
                
            }
        });
        
        // Handler for 'Browse' for Image Path button, browse the vm image
        browseImage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                FileChooser imageFile = new FileChooser();
                //imageFile.setInitialDirectory(new File(imagesOnFS));
                try {
                    File file = imageFile.showOpenDialog(createImageStage);
                    imagePathTField.setText(file.getAbsolutePath());
                } catch(Exception e) {
                    logger.info("Not selected anything");
                }
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root);
        //scene.setFill(Color.AQUA);
        createImageStage.setScene(scene);
        createImageStage.show(); 
        
    }
    
    private String EncryptImage(String manifestFileLocation){
        Map<String, String> customerInfo = writeToMap();
                String message="";
                
                if (customerInfo != null) { 
                    if(customerInfo.get(Constants.IS_ENCRYPTED)=="true"){
                String opensslPassword = ConfigProperties.getProperty(Constants.PASSWORD);
                MHUtilityOperation mhOptImage = new MHUtilityOperation();
                
		// Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                //String mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME);
                //String encryptedImageLocation = mhOptImage.startMHProcess(confInfo.get(Constants.IMAGE_LOCATION), mhKeyName);
                String encryptedImageLocation = mhOptImage.encryptFile(customerInfo.get(Constants.IMAGE_LOCATION), opensslPassword);
                if(encryptedImageLocation == null) {
//                    new ConfigurationInformation(primaryStage).showWarningPopup("Error In Image Encryption ..... Exiting.....");
                    System.exit(1);
                }
                message = "VM Image Encrypted \n\n Encrypted Image Path : " + encryptedImageLocation + "\n\n"
                        + "Trusted Policy Location : " + manifestFileLocation + "\n\n";
                if(customerInfo.containsKey(Constants.KERNEL_PATH) && customerInfo.containsKey(Constants.INITRD_PATH)) { 
                    MHUtilityOperation mhOptKernel = new MHUtilityOperation();

		    // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                    //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-kernel";
                    //String encryptedKernelPath = mhOptKernel.startMHProcess(confInfo.get(Constants.KERNEL_PATH), mhKeyName);
                    String encryptedKernelPath = mhOptImage.encryptFile(customerInfo.get(Constants.KERNEL_PATH), opensslPassword);
                    if(encryptedKernelPath == null) {
//                        new ConfigurationInformation(primaryStage).showWarningPopup("Error In Image Encryption ..... Exiting.....");
                        System.exit(1);
                    }
                    
		    MHUtilityOperation mhOptInitrd = new MHUtilityOperation();
		    // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                    //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-initrd";
                    //String encryptedInitrdPath = mhOptInitrd.startMHProcess(confInfo.get(Constants.INITRD_PATH), mhKeyName);
                    String encryptedInitrdPath = mhOptImage.encryptFile(customerInfo.get(Constants.INITRD_PATH), opensslPassword);
                    if(encryptedInitrdPath == null) {
//                        new ConfigurationInformation(primaryStage).showWarningPopup("Error In Image Encryption ..... Exiting.....");
                        System.exit(1);
                    }
                    message = message + "Encrypted Kernel Path : " + encryptedKernelPath + "\n\n"
                            + "mh_dek_url for Kernel : " + mhOptKernel.getDekURL() + "\n\n" + "Encrypted Initrd Path : "
                                + encryptedInitrdPath + "\n\n" + "mh_dek_url for Initrd : " + mhOptInitrd.getDekURL() + "\n\n";
                       }
                    }else{
                       logger.info("Encryption of the image is not selected");
                       message=message = "Image encryption was not chosen" + "\n\n"
                        + "Trusted Policy Location : " + manifestFileLocation + "\n\n";
                    }    
                 
                }
                return message;
    }
    
    
    private String UploadNow(String manifestFileLocation){
        Map<String, String> customerInfo = writeToMap();
                String message="";
                UserConfirmation userObj=new UserConfirmation();
                UploadToGlance uploadGlanceObj=new UploadToGlance();
                if (customerInfo != null) { 
                    if(customerInfo.get(Constants.IS_ENCRYPTED)=="true"){
                        boolean isEncrypted = true;
                        String opensslPassword = ConfigProperties.getProperty(Constants.PASSWORD);
                        MHUtilityOperation mhOptImage = new MHUtilityOperation();

		    // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                    //String mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME);
                    //String encryptedImageLocation = mhOptImage.startMHProcess(confInfo.get(Constants.IMAGE_LOCATION), mhKeyName);
                    String encryptedImageLocation = mhOptImage.encryptFile(customerInfo.get(Constants.IMAGE_LOCATION), opensslPassword);
                    if(encryptedImageLocation == null) {
//                        new ConfigurationInformation(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                        System.exit(1);
                    }
                    customerInfo.put(Constants.MH_DEK_URL_IMG, mhOptImage.getDekURL());
                    customerInfo.put(Constants.Enc_IMAGE_LOCATION, encryptedImageLocation);
                    if(customerInfo.containsKey(Constants.KERNEL_PATH) && customerInfo.containsKey(Constants.INITRD_PATH)) { 
                        MHUtilityOperation mhOptKernel = new MHUtilityOperation();
	
			// Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                        //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-kernel";
                        //String encryptedKernelPath = mhOptKernel.startMHProcess(confInfo.get(Constants.KERNEL_PATH), mhKeyName);
                        String encryptedKernelPath = mhOptImage.encryptFile(customerInfo.get(Constants.KERNEL_PATH), opensslPassword);
                        if(encryptedKernelPath == null) {
//                            new ConfigurationInformation(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                            System.exit(1);
                        }
                        
			MHUtilityOperation mhOptInitrd = new MHUtilityOperation();
                        

			// Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                        //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-initrd";
                        //String encryptedInitrdPath = mhOptInitrd.startMHProcess(confInfo.get(Constants.INITRD_PATH), mhKeyName);
                        String encryptedInitrdPath = mhOptImage.encryptFile(customerInfo.get(Constants.INITRD_PATH), opensslPassword);
                        if(encryptedInitrdPath == null) {
//                            new ConfigurationInformation(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                            System.exit(1);
                        }
                        customerInfo.put(Constants.MH_DEK_URL_KERNEL, mhOptKernel.getDekURL());
                        customerInfo.put(Constants.MH_DEK_URL_INITRD, mhOptInitrd.getDekURL());
                        customerInfo.put(Constants.Enc_KERNEL_PATH, encryptedKernelPath);
                        customerInfo.put(Constants.Enc_INITRD_PATH, encryptedInitrdPath);
                    }
                    System.out.println("PSDebug Encrypted and saved the manifest and the image to upload NOW");
//                     message=uploadGlanceObj.uploadManifest(manifestFileLocation);
//                     System.out.println("PSDebug message from Upload Glance Manifest:" + message);
                     message=userObj.setImagePropertiesAndUploadToGlance(customerInfo, manifestFileLocation, isEncrypted,createImageStage);
                     System.out.println("PSDebug message from Upload Glance:" + message);
                     
                     System.out.println("PSDebug Upload done");
                    showUploadSuccessMessage(createImageStage, message);
                    //encImageUploadConfirmation(primaryStage, confInfo, manifestLocation);
                    }else if(customerInfo.get(Constants.IS_ENCRYPTED)=="false") {
                    boolean isEncrypted = false;
                    message = userObj.setImagePropertiesAndUploadToGlance(customerInfo, manifestFileLocation, isEncrypted, createImageStage);
                    showUploadSuccessMessage(createImageStage, message);
                }
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
