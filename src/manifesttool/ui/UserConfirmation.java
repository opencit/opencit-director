package manifesttool.ui;

import manifesttool.utils.GlanceImageStoreImpl;
import manifesttool.utils.GenerateHash;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import static manifesttool.ui.CreateImage.manifestFlag;
import manifesttool.utils.ConfigProperties;
import manifesttool.utils.FileUtilityOperation;
import manifesttool.utils.IImageStore;
import manifesttool.utils.ImageStoreException;
import manifesttool.utils.ImageStoreUtil;
import manifesttool.utils.LoggerUtility;
import manifesttool.utils.MHUtilityOperation;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author root
 */
public class UserConfirmation {
    
    private static final Logger logger = Logger.getLogger(UserConfirmation.class.getName());
    private static final String opensslPassword = "intelrp";
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }
    
//    public void showLocation(final Stage primaryStage, final String fileLocation, final Map<String, String> confInfo) {
//        
//        primaryStage.setTitle("Manifest Location");
//        String info = "Manifest File Saved at : \n\"" + fileLocation + "\"";
//        
//        // Check for the "Host_Manifest" property in the property file, if present, manifest generation will be for host
//        String tempHostManifest = ConfigProperties.getProperty(Constants.HOST_MANIFEST);
//        if( tempHostManifest != null) {
//            tempHostManifest = tempHostManifest.trim();
//        }
//
//        final String hostManifest = tempHostManifest;
//        if((hostManifest != null) && (hostManifest.equalsIgnoreCase("true"))) {
//            info = "Host Manifest File Location : \"" + fileLocation + "\"" + "\n\n" + "Rootfs Location : \"" + confInfo.get(Constants.IMAGE_LOCATION) + "\"";
//        }
//        
//        if(!((hostManifest != null) && (hostManifest.equalsIgnoreCase("true")))) {
//             glanceUploadConfirmation(primaryStage, fileLocation, confInfo);
//            } else {
//	       generateManifesConfirmation(primaryStage,fileLocation);
//	}
//           
//       
//        
//        
//    }
    
    
    public void glanceUploadConfirmation(final Stage primaryStage, final String manifestLocation, final Map<String, String> confInfo) {
        primaryStage.setTitle("Upload to Glance");
        String info = "Continue Uploading to Glance . . .";
        Label confirm = new Label(info);
        confirm.setFont(new Font("Arial", 14));
        
        Button saveButton=new Button("Upload Later");
        Button uploadButton=new Button("Upload");
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);
        
        
        
//        PS: Save button action: generates Trust Policy and encrypts the image if encrypt option is chosen
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent t) {
            MHUtilityOperation mhOptImage = new MHUtilityOperation();
                primaryStage.close();

		// Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                //String mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME);
                //String encryptedImageLocation = mhOptImage.startMHProcess(confInfo.get(Constants.IMAGE_LOCATION), mhKeyName);
                String encryptedImageLocation = mhOptImage.encryptFile(confInfo.get(Constants.IMAGE_LOCATION), opensslPassword);
                if(encryptedImageLocation == null) {
                    new CreateImage(primaryStage).showWarningPopup("Error In Image Encryption ..... Exiting.....");
                    System.exit(1);
                }
                String message = "VM Image Encrypted \n\n Encrypted Image Path : " + encryptedImageLocation + "\n\n"
                        + "Trust Policy Location : " + manifestLocation + "\n\n";
                if(confInfo.containsKey(Constants.KERNEL_PATH) && confInfo.containsKey(Constants.INITRD_PATH)) { 
                    MHUtilityOperation mhOptKernel = new MHUtilityOperation();

		    // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                    //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-kernel";
                    //String encryptedKernelPath = mhOptKernel.startMHProcess(confInfo.get(Constants.KERNEL_PATH), mhKeyName);
                    String encryptedKernelPath = mhOptImage.encryptFile(confInfo.get(Constants.KERNEL_PATH), opensslPassword);
                    if(encryptedKernelPath == null) {
                        new CreateImage(primaryStage).showWarningPopup("Error In Image Encryption ..... Exiting.....");
                        System.exit(1);
                    }
                    
		    MHUtilityOperation mhOptInitrd = new MHUtilityOperation();
		    // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                    //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-initrd";
                    //String encryptedInitrdPath = mhOptInitrd.startMHProcess(confInfo.get(Constants.INITRD_PATH), mhKeyName);
                    String encryptedInitrdPath = mhOptImage.encryptFile(confInfo.get(Constants.INITRD_PATH), opensslPassword);
                    if(encryptedInitrdPath == null) {
                        new CreateImage(primaryStage).showWarningPopup("Error In Image Encryption ..... Exiting.....");
                        System.exit(1);
                    }
                    message = message + "Encrypted Kernel Path : " + encryptedKernelPath + "\n\n"
                            + "mh_dek_url for Kernel : " + mhOptKernel.getDekURL() + "\n\n" + "Encrypted Initrd Path : "
                                + encryptedInitrdPath + "\n\n" + "mh_dek_url for Initrd : " + mhOptInitrd.getDekURL() + "\n\n";
                }
                showUploadSuccessMessage(primaryStage, message);
            }
        });
        
        //PS: Upload image
        uploadButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                if(confInfo.get(Constants.IS_ENCRYPTED)=="true") {
                    boolean isEncrypted = true;
                    String opensslPassword = ConfigProperties.getProperty(Constants.PASSWORD);
                    MHUtilityOperation mhOptImage = new MHUtilityOperation();

		    // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                    String mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME);
                    String encryptedImageLocation = mhOptImage.startMHProcess(confInfo.get(Constants.IMAGE_LOCATION), mhKeyName);
//                    String encryptedImageLocation = mhOptImage.encryptFile(confInfo.get(Constants.IMAGE_LOCATION), opensslPassword);
                    if(encryptedImageLocation == null) {
                        new CreateImage(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                        System.exit(1);
                    }
                    confInfo.put(Constants.MH_DEK_URL_IMG, mhOptImage.getDekURL());
                    confInfo.put(Constants.Enc_IMAGE_LOCATION, encryptedImageLocation);
                    if(confInfo.containsKey(Constants.KERNEL_PATH) && confInfo.containsKey(Constants.INITRD_PATH)) { 
                        MHUtilityOperation mhOptKernel = new MHUtilityOperation();
	
			// Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                        mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-kernel";
                        String encryptedKernelPath = mhOptKernel.startMHProcess(confInfo.get(Constants.KERNEL_PATH), mhKeyName);
//                        String encryptedKernelPath = mhOptImage.encryptFile(confInfo.get(Constants.KERNEL_PATH), opensslPassword);
                        if(encryptedKernelPath == null) {
                            new CreateImage(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                            System.exit(1);
                        }
                        
			MHUtilityOperation mhOptInitrd = new MHUtilityOperation();

			// Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                        mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-initrd";
                        String encryptedInitrdPath = mhOptInitrd.startMHProcess(confInfo.get(Constants.INITRD_PATH), mhKeyName);
//                        String encryptedInitrdPath = mhOptImage.encryptFile(confInfo.get(Constants.INITRD_PATH), opensslPassword);
                        if(encryptedInitrdPath == null) {
                            new CreateImage(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                            System.exit(1);
                        }
                        confInfo.put(Constants.MH_DEK_URL_KERNEL, mhOptKernel.getDekURL());
                        confInfo.put(Constants.MH_DEK_URL_INITRD, mhOptInitrd.getDekURL());
                        confInfo.put(Constants.Enc_KERNEL_PATH, encryptedKernelPath);
                        confInfo.put(Constants.Enc_INITRD_PATH, encryptedInitrdPath);
                    }
                    String message=null;
                    try {
                        message = setImagePropertiesAndUploadToGlance(confInfo, manifestLocation, isEncrypted,primaryStage);
                    } catch (ImageStoreException ex) {
                        Logger.getLogger(UserConfirmation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    showUploadSuccessMessage(primaryStage, message);
                    //encImageUploadConfirmation(primaryStage, confInfo, manifestLocation);
                } else if(confInfo.get(Constants.IS_ENCRYPTED)=="false") {
                    boolean isEncrypted = false;
                    String message=null;
                    try {
                        message = setImagePropertiesAndUploadToGlance(confInfo, manifestLocation, isEncrypted, primaryStage);
                    } catch (ImageStoreException ex) {
                        Logger.getLogger(UserConfirmation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    showUploadSuccessMessage(primaryStage, message);
                } else {
                    new CreateImage(primaryStage).showWarningPopup("Plese select an option");
                }
            }
        });
        
        // Handler for "I will Upload" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                primaryStage.close();
                 ConfigurationInformation window = new ConfigurationInformation(primaryStage);
                window.launch();
            }
        });
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(35);
        hbox.setStyle("-fx-background-color: #336699;");
        
        hbox.getChildren().addAll(cancelButton, uploadButton,saveButton);


        
        StackPane root = new StackPane();
        root.getChildren().add(hbox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // Generate the new manifest file for Image
    public void generateManifesConfirmation(final Stage primaryStage, final String manifestFileLocation) {
        primaryStage.setTitle("Save Trust Policy");
      
        Button saveButton=new Button("Save");
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);
        
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(35);
        hbox.setStyle("-fx-background-color: #336699;");
        
        hbox.getChildren().addAll(cancelButton,saveButton);
        

        saveButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
               String message = "Trust Policy Location : " + manifestFileLocation + "\n\n";
               showUploadSuccessMessage(primaryStage, message); 
                
            }
        });
        
        
        // Handler for "I will Upload" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                primaryStage.close();
                ConfigurationInformation window = new ConfigurationInformation(primaryStage);
                window.launch();
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(hbox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show(); 
    }
    
//    // Upload the manifest file to glance and update the glance image property
//    private void manifestUploadConfirmation(final Stage primaryStage, final String manifestLocation) {
//        primaryStage.setTitle("Upload Manifest to Glance");
//        String info = "Image ID";
//        Label imageIDLabel = new Label(info);
//        
//        final TextField imageIDTField = new TextField();
//        
//        HBox hbox = new HBox();
//        hbox.setPadding(new Insets(15, 12, 15, 10));
//        hbox.setSpacing(10);        
//        
//        hbox.getChildren().addAll(imageIDLabel, imageIDTField);
//        
//        Button continueButton = new Button("Upload Manifest");
//        
//        VBox vbox = new VBox();
//        vbox.setSpacing(10);
//        vbox.setPadding(new Insets(15, 12, 15, 12));
//        
//        vbox.getChildren().addAll(hbox, continueButton);
//        
//        continueButton.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent t) {
//                UploadToGlance glanceObject = new UploadToGlance();
//                
//                if(!new FileUtilityOperation().validateUUID(imageIDTField.getText())) {
////                    new ConfigurationInformation(primaryStage).showWarningPopup("Please provide the valid image id ....");
//                } else {
//                    // Upload manifest to Glance
//                    String manifestGlanceID = glanceObject.uploadManifest(manifestLocation);
//                
//                    if(manifestGlanceID == null) {
//                        String message = "Failed to upload the Manifest to Glance .... Exiting";
//                        showUploadSuccessMessage(primaryStage, message);
//                        System.exit(1);
//                    }
//                
//                    // Update Image property
//                    boolean isSuccess = glanceObject.updateImageProperty(imageIDTField.getText(), "x-image-meta-property-manifest_uuid" , manifestGlanceID);
//                
//                    if(!isSuccess) {
//                        String message = "Failed to update the glance image property.....Exiting";
//                        showUploadSuccessMessage(primaryStage, message);
//                        System.exit(1);
//                    }
//                    String message = "Manifest Uploaded to glance " + "\n\n" + "Glance ID is : " + manifestGlanceID;
//                    showUploadSuccessMessage(primaryStage, message);
//                }
//            }
//        });
//        
//        StackPane root = new StackPane();
//        root.getChildren().add(vbox);
//        Scene scene = new Scene(root);
//        primaryStage.setScene(scene);
//        primaryStage.show(); 
//        
//    }
    
    // Show the vm image and manifest glance ID
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
                ConfigurationInformation window = new ConfigurationInformation(primaryStage);
                window.launch();
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(vbox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();         
        
    }
    
    public String setImagePropertiesAndUploadToGlance(Map<String, String> confInfo, String manifestLocation, boolean isEncrypted, Stage primaryStage) throws ImageStoreException {
//        System.out.println("PSDebug Came to set image prop");
        String imageName = confInfo.get(Constants.IMAGE_NAME);
        String diskFormat = null;
        String containerFormat = null;
        boolean isSuccess = true;
        String isPublic = "true";
        String imageId = confInfo.get(Constants.IMAGE_ID);
        IImageStore imageStoreObj = ImageStoreUtil.getImageStore();
        switch(confInfo.get(Constants.IMAGE_TYPE)) {
            case "ami":
                diskFormat = "ami";
                containerFormat = "ami";
                break;
            case "qcow2":
                diskFormat = "qcow2";
                containerFormat = "bare";
                break;
            case "vhd":
                diskFormat = "vhd";
                containerFormat = "bare";
                break;
            case "raw":
                diskFormat = "raw";
                containerFormat = "bare";
                break;
        }
        
        // Set the image properties
        Map<String, String> imageProperties = new HashMap<>();
        imageProperties.put(Constants.NAME, imageName);
        imageProperties.put(Constants.DISK_FORMAT, diskFormat);
        imageProperties.put(Constants.CONTAINER_FORMAT, containerFormat);
        imageProperties.put(Constants.IS_PUBLIC, isPublic);
        imageProperties.put(Constants.IMAGE_ID, imageId); 
        
        // Initialize message digest
        MessageDigest md = null;
        if(isEncrypted) {
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            }
        }
        
        if(confInfo.containsKey(Constants.KERNEL_PATH) && confInfo.containsKey(Constants.INITRD_PATH)) {
            
            // Upload Kernel
            imageProperties.put(Constants.NAME, imageName + "-kernel");
            imageProperties.put(Constants.DISK_FORMAT, "aki");
            imageProperties.put(Constants.CONTAINER_FORMAT, "aki");
            imageProperties.put(Constants.IS_PUBLIC, isPublic);
            imageProperties.put(Constants.IMAGE_ID, getUUID());
            String kernelGlanceID = null;
            try{
                if(isEncrypted) {
                    System.out.println("PSDebug Came to set image prop 22222");
                    kernelGlanceID = imageStoreObj.uploadImage(confInfo.get(Constants.Enc_KERNEL_PATH),imageProperties);
                    if(kernelGlanceID == null) {
                        String message = "Failed to upload the Image to Glance .... Exiting";
                        showUploadSuccessMessage(primaryStage, message);
                        System.exit(1);
                    }
                    isSuccess = imageStoreObj.updateImageProperty(kernelGlanceID, "x-image-meta-property-mh_encrypted", "true");
                    if(!isSuccess) {
                        String message = "Failed to update the Glance Image property .... Exiting";
                        showUploadSuccessMessage(primaryStage, message);
                        System.exit(1);                
                    }
                    isSuccess = imageStoreObj.updateImageProperty(kernelGlanceID, "x-image-meta-property-mh_checksum", new GenerateHash().computeHash(md, new File(confInfo.get(Constants.KERNEL_PATH))));
                    if(!isSuccess) {
                        String message = "Failed to update the Glance Image property .... Exiting";
                        showUploadSuccessMessage(primaryStage, message);
                        System.exit(1);                
                    }
                    isSuccess = imageStoreObj.updateImageProperty(kernelGlanceID, "x-image-meta-property-mh_dek_url", confInfo.get(Constants.MH_DEK_URL_KERNEL));
                    if(!isSuccess) {
                        String message = "Failed to update the Glance Image property .... Exiting";
                        showUploadSuccessMessage(primaryStage, message);
                        System.exit(1);                
                    }
                } else {
                    kernelGlanceID = imageStoreObj.uploadImage(confInfo.get(Constants.KERNEL_PATH),imageProperties);
                    if(kernelGlanceID == null) {
                        String message = "Failed to upload the Image to Glance .... Exiting";
                        showUploadSuccessMessage(primaryStage, message);
                        System.exit(1);
                    }
                }
            }catch(NullPointerException e){
                throw new ImageStoreException(e);
            }
            
            
            // Upload Initrd
            imageProperties.clear();
            imageProperties.put(Constants.NAME, imageName + "-initrd");
            imageProperties.put(Constants.DISK_FORMAT, "ari");
            imageProperties.put(Constants.CONTAINER_FORMAT, "ari");
            imageProperties.put(Constants.IS_PUBLIC, isPublic);
            imageProperties.put(Constants.IMAGE_ID, getUUID());
            String initrdGlanceID = null;
            
            if(isEncrypted) {
                initrdGlanceID = imageStoreObj.uploadImage(confInfo.get(Constants.Enc_INITRD_PATH), imageProperties);
                if(initrdGlanceID == null) {
                    String message = "Failed to upload the Image to Glance .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);
                }
                isSuccess = imageStoreObj.updateImageProperty(initrdGlanceID, "x-image-meta-property-mh_encrypted", "true");
                if(!isSuccess) {
                    String message = "Failed to update the Glance Image property .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);                
                }
                isSuccess = imageStoreObj.updateImageProperty(initrdGlanceID, "x-image-meta-property-mh_checksum", new GenerateHash().computeHash(md, new File(confInfo.get(Constants.INITRD_PATH))));
                if(!isSuccess) {
                    String message = "Failed to update the Glance Image property .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);                
                }
                isSuccess = imageStoreObj.updateImageProperty(initrdGlanceID, "x-image-meta-property-mh_dek_url", confInfo.get(Constants.MH_DEK_URL_INITRD));
                if(!isSuccess) {
                    String message = "Failed to update the Glance Image property .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);                
                }
            } else {
                initrdGlanceID = imageStoreObj.uploadImage(confInfo.get(Constants.INITRD_PATH), imageProperties);
                if(initrdGlanceID == null) {
                    String message = "Failed to upload the Image to Glance .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);
                }
            }          
            
            // Set Image Property
            imageProperties.clear();
            imageProperties.put(Constants.NAME, imageName);
            imageProperties.put(Constants.DISK_FORMAT, "ami");
            imageProperties.put(Constants.CONTAINER_FORMAT, "ami");
            imageProperties.put(Constants.IS_PUBLIC, isPublic);
            imageProperties.put("Image ID", imageId); 
            imageProperties.put("Kernel ID", kernelGlanceID);
            imageProperties.put("Initrd ID", initrdGlanceID);
        }
        
        //Upload image to glance
        String imageGlanceID = null;
        if(isEncrypted) {
//            System.out.println("PSDebug Came to set image prop 4444444444");
            imageGlanceID = imageStoreObj.uploadImage(confInfo.get("EncImage Location"), imageProperties);
            if(imageGlanceID == null) {
                String message = "Failed to upload the Image to Glance .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);
            }
            
            isSuccess = imageStoreObj.updateImageProperty(imageGlanceID, "x-image-meta-property-mh_encrypted", "true");            
            if(!isSuccess) {
                String message = "Failed to update the Glance Image property .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);                
            }
            isSuccess = imageStoreObj.updateImageProperty(imageGlanceID, "x-image-meta-property-mh_checksum", new GenerateHash().computeHash(md, new File(confInfo.get("Image Location"))));
            if(!isSuccess) {
                String message = "Failed to update the Glance Image property .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);                
            }
            isSuccess = imageStoreObj.updateImageProperty(imageGlanceID, "x-image-meta-property-mh_dek_url", confInfo.get(Constants.MH_DEK_URL_IMG));
            if(!isSuccess) {
                String message = "Failed to update the Glance Image property .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);                
            }
        } else {
            imageGlanceID = imageStoreObj.uploadImage(confInfo.get("Image Location"), imageProperties);
//            System.out.println("PSDebug glance ID" + imageGlanceID);
            if(imageGlanceID == null) {
                String message = "Failed to upload the Image to Glance .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);
            }            
        }
//        System.out.println("PSDebug manifestLoca is" + manifestLocation);
        String manifestGlanceID = imageStoreObj.uploadTrustPolicy(manifestLocation);
        if(manifestGlanceID == null) {
            String message = "Failed to upload the Manifest to Glance .... Exiting";
            showUploadSuccessMessage(primaryStage, message);
            System.exit(1);
        }
        isSuccess = imageStoreObj.updateImageProperty(imageGlanceID, "x-image-meta-property-manifest_uuid", manifestGlanceID);
        if(!isSuccess) {
            String message = "Failed to update the Glance Image property .... Exiting";
            showUploadSuccessMessage(primaryStage, message);
            System.exit(1);                
        }
        String message = "VM Image Uploaded to glance with Manifest file" 
                + "\n\n" + "Image Glance ID is : " + imageGlanceID + "\n\n" + "Manifest Glance ID is : " 
                + manifestGlanceID;  
        return message;
    }
    
    // Generates random UUID
    public String getUUID() {
        String uuid = UUID.randomUUID().toString();
        boolean isAvailable = checkUUIDAvailability(uuid);
        if(!isAvailable) {
            getUUID();
        }
        return uuid;
    }
    
    // Check availability of UUID
    private boolean checkUUIDAvailability(String uuid) {
        boolean isAvailable = true;
        //System.out.println("Checking availability of UUID");
        logger.info("Checking availability of UUID");
        return isAvailable;
    }
}
