package com.intel.mtwilson.trust.director.ui;

import com.intel.mtwilson.trust.director.utils.UploadToGlance;
import com.intel.mtwilson.trust.director.utils.GenerateHash;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
import com.intel.mtwilson.trust.director.utils.ConfigProperties;
import com.intel.mtwilson.trust.director.utils.FileUtilityOperation;
import com.intel.mtwilson.trust.director.utils.LoggerUtility;
import com.intel.mtwilson.trust.director.utils.MHUtilityOperation;

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
    public void showLocation(final Stage primaryStage, final String fileLocation, final Map<String, String> confInfo) {
        
        primaryStage.setTitle("Manifest Location");
        String info = "Manifest File Saved at : \n\"" + fileLocation + "\"";
        
        // Check for the "Host_Manifest" property in the property file, if present, manifest generation will be for host
        String tempHostManifest = ConfigProperties.getProperty(Constants.HOST_MANIFEST);
        if( tempHostManifest != null) {
            tempHostManifest = tempHostManifest.trim();
        }

        final String hostManifest = tempHostManifest;
        if((hostManifest != null) && (hostManifest.equalsIgnoreCase("true"))) {
            info = "Host Manifest File Location : \"" + fileLocation + "\"" + "\n\n" + "Rootfs Location : \"" + confInfo.get(Constants.IMAGE_LOCATION) + "\"";
        }
        
        Label message = new Label(info);
        message.setFont(new Font("Arial", 14));
        Button okButton = new Button("Ok");
        okButton.setPrefSize(100, 20);
        
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(15, 12, 15, 12));
        vbox.setSpacing(30);
        
        vbox.getChildren().add(message);
        vbox.getChildren().add(okButton);
        
        StackPane root = new StackPane();
        root.getChildren().add(vbox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                primaryStage.close();
                if(!((hostManifest != null) && (hostManifest.equalsIgnoreCase("true")))) {
                    glanceUploadConfirmation(primaryStage, fileLocation, confInfo);
                } else {
		    generateManifesConfirmation(primaryStage);
		}
            }
        });
        
        
    }
    
    private void glanceUploadConfirmation(final Stage primaryStage, final String manifestLocation, final Map<String, String> confInfo) {
        primaryStage.setTitle("Upload to Glance");
        String info = "Continue Uploading to Glance . . .";
        Label confirm = new Label(info);
        confirm.setFont(new Font("Arial", 14));
        
        final RadioButton manifestRB = new RadioButton("Upload Manifest Only");
        //manifestRB.setSelected(true);
        final RadioButton encryptImageRB = new RadioButton("Upload Encrypted Image with Manifest");
        final RadioButton plainImageRB = new RadioButton("Upload Plain Image with Manifest");
        final ToggleGroup group = new ToggleGroup();
        manifestRB.setToggleGroup(group);
        encryptImageRB.setToggleGroup(group);
        plainImageRB.setToggleGroup(group);  
        
        Button continueButton = new Button("Continue");
        continueButton.setPrefSize(100, 20);
        
        Button cancelButton = new Button("I will upload");
        cancelButton.setPrefSize(100, 20);        
        
        VBox radioVBox = new VBox();
        radioVBox.setPadding(new Insets(0, 12, 0, 10));
        radioVBox.setSpacing(20);
        //radioVBox.setStyle("-fx-background-color: #336699;");
        
        radioVBox.getChildren().add(manifestRB);
        radioVBox.getChildren().add(encryptImageRB);
        radioVBox.getChildren().add(plainImageRB);
        
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        vbox.setPadding(new Insets(15, 12, 15, 12));
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(35);
        hbox.setStyle("-fx-background-color: #336699;");
        
        hbox.getChildren().addAll(cancelButton, continueButton);
        
        vbox.getChildren().add(confirm);
        vbox.getChildren().add(radioVBox);
        vbox.getChildren().add(hbox);
        
        continueButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                if(manifestRB.isSelected()) {
                    logger.info("Uploading Manifest to glance");
                    manifestUploadConfirmation(primaryStage, manifestLocation);
                } else if(encryptImageRB.isSelected()) {
                    MHUtilityOperation mhOptImage = new MHUtilityOperation();

		    // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                    //String mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME);
                    //String encryptedImageLocation = mhOptImage.startMHProcess(confInfo.get(Constants.IMAGE_LOCATION), mhKeyName);
                    String encryptedImageLocation = mhOptImage.encryptFile(confInfo.get(Constants.IMAGE_LOCATION), opensslPassword);
                    if(encryptedImageLocation == null) {
                        new ConfigurationInformation(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                        System.exit(1);
                    }
                    confInfo.put(Constants.MH_DEK_URL_IMG, mhOptImage.getDekURL());
                    confInfo.put(Constants.Enc_IMAGE_LOCATION, encryptedImageLocation);
                    if(confInfo.containsKey(Constants.KERNEL_PATH) && confInfo.containsKey(Constants.INITRD_PATH)) { 
                        MHUtilityOperation mhOptKernel = new MHUtilityOperation();
	
			// Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                        //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-kernel";
                        //String encryptedKernelPath = mhOptKernel.startMHProcess(confInfo.get(Constants.KERNEL_PATH), mhKeyName);
                        String encryptedKernelPath = mhOptImage.encryptFile(confInfo.get(Constants.KERNEL_PATH), opensslPassword);
                        if(encryptedKernelPath == null) {
                            new ConfigurationInformation(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                            System.exit(1);
                        }
                        
			MHUtilityOperation mhOptInitrd = new MHUtilityOperation();

			// Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                        //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-initrd";
                        //String encryptedInitrdPath = mhOptInitrd.startMHProcess(confInfo.get(Constants.INITRD_PATH), mhKeyName);
                        String encryptedInitrdPath = mhOptImage.encryptFile(confInfo.get(Constants.INITRD_PATH), opensslPassword);
                        if(encryptedInitrdPath == null) {
                            new ConfigurationInformation(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                            System.exit(1);
                        }
                        confInfo.put(Constants.MH_DEK_URL_KERNEL, mhOptKernel.getDekURL());
                        confInfo.put(Constants.MH_DEK_URL_INITRD, mhOptInitrd.getDekURL());
                        confInfo.put(Constants.Enc_KERNEL_PATH, encryptedKernelPath);
                        confInfo.put(Constants.Enc_INITRD_PATH, encryptedInitrdPath);
                    }
                    boolean isEncrypted = true;
                    String message = setImagePropertiesAndUploadToGlance(confInfo, manifestLocation, isEncrypted,primaryStage);
                    showUploadSuccessMessage(primaryStage, message);
                    //encImageUploadConfirmation(primaryStage, confInfo, manifestLocation);
                } else if(plainImageRB.isSelected()) {
                    boolean isEncrypted = false;
                    String message = setImagePropertiesAndUploadToGlance(confInfo, manifestLocation, isEncrypted, primaryStage);
                    showUploadSuccessMessage(primaryStage, message);
                } else {
                    new ConfigurationInformation(primaryStage).showWarningPopup("Plese select an option");
                }
            }
        });
        
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                MHUtilityOperation mhOptImage = new MHUtilityOperation();
                primaryStage.close();

		// Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                //String mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME);
                //String encryptedImageLocation = mhOptImage.startMHProcess(confInfo.get(Constants.IMAGE_LOCATION), mhKeyName);
                String encryptedImageLocation = mhOptImage.encryptFile(confInfo.get(Constants.IMAGE_LOCATION), opensslPassword);
                if(encryptedImageLocation == null) {
                    new ConfigurationInformation(primaryStage).showWarningPopup("Error In Image Encryption ..... Exiting.....");
                    System.exit(1);
                }
                String message = "VM Image Encrypted \n\n Encrypted Image Path : " + encryptedImageLocation + "\n\n"
                        + "mh_dek_url for image : " + mhOptImage.getDekURL() + "\n\n";
                if(confInfo.containsKey(Constants.KERNEL_PATH) && confInfo.containsKey(Constants.INITRD_PATH)) { 
                    MHUtilityOperation mhOptKernel = new MHUtilityOperation();

		    // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                    //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-kernel";
                    //String encryptedKernelPath = mhOptKernel.startMHProcess(confInfo.get(Constants.KERNEL_PATH), mhKeyName);
                    String encryptedKernelPath = mhOptImage.encryptFile(confInfo.get(Constants.KERNEL_PATH), opensslPassword);
                    if(encryptedKernelPath == null) {
                        new ConfigurationInformation(primaryStage).showWarningPopup("Error In Image Encryption ..... Exiting.....");
                        System.exit(1);
                    }
                    
		    MHUtilityOperation mhOptInitrd = new MHUtilityOperation();
		    // Uncomment the following two lines and comment the third line for KMS integration (for regestering the decryption key)
                    //mhKeyName = ConfigProperties.getProperty(Constants.MH_KEY_NAME) + "-initrd";
                    //String encryptedInitrdPath = mhOptInitrd.startMHProcess(confInfo.get(Constants.INITRD_PATH), mhKeyName);
                    String encryptedInitrdPath = mhOptImage.encryptFile(confInfo.get(Constants.INITRD_PATH), opensslPassword);
                    if(encryptedInitrdPath == null) {
                        new ConfigurationInformation(primaryStage).showWarningPopup("Error In Image Encryption ..... Exiting.....");
                        System.exit(1);
                    }
                    message = message + "Encrypted Kernel Path : " + encryptedKernelPath + "\n\n"
                            + "mh_dek_url for Kernel : " + mhOptKernel.getDekURL() + "\n\n" + "Encrypted Initrd Path : "
                                + encryptedInitrdPath + "\n\n" + "mh_dek_url for Initrd : " + mhOptInitrd.getDekURL() + "\n\n";
                }
                showUploadSuccessMessage(primaryStage, message);
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(vbox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // Generate the new manifest file for Image
    private void generateManifesConfirmation(final Stage primaryStage) {
        primaryStage.setTitle("Generate the New Manifest");
        String info = "Generate Manifest of New Image ?";
        Label confirm = new Label(info);
        confirm.setFont(new Font("Arial", 14));        

        Button continueButton = new Button("Continue");
        //continueButton.setPrefSize(80, 20);
        Button cancelButton = new Button("Close");
        cancelButton.setPrefSize(80, 20);        
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 10));
        hbox.setSpacing(75);
        hbox.setStyle("-fx-background-color: #336699;");
        
        hbox.getChildren().add(continueButton);
        hbox.getChildren().add(cancelButton);
        
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        vbox.setPadding(new Insets(15, 12, 15, 12));
        
        vbox.getChildren().add(confirm);
        vbox.getChildren().add(hbox);
        
        // Handler for "Continue" button
        continueButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                primaryStage.close();
                ConfigurationInformation window = new ConfigurationInformation(primaryStage);
                window.launch();
            }
        });
        
        // Handler for "I will Upload" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                primaryStage.close();
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(vbox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show(); 
    }
    
    // Upload the manifest file to glance and update the glance image property
    private void manifestUploadConfirmation(final Stage primaryStage, final String manifestLocation) {
        primaryStage.setTitle("Upload Manifest to Glance");
        String info = "Image ID";
        Label imageIDLabel = new Label(info);
        
        final TextField imageIDTField = new TextField();
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 10));
        hbox.setSpacing(10);        
        
        hbox.getChildren().addAll(imageIDLabel, imageIDTField);
        
        Button continueButton = new Button("Upload Manifest");
        
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(15, 12, 15, 12));
        
        vbox.getChildren().addAll(hbox, continueButton);
        
        continueButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                UploadToGlance glanceObject = new UploadToGlance();
                
                if(!new FileUtilityOperation().validateUUID(imageIDTField.getText())) {
                    new ConfigurationInformation(primaryStage).showWarningPopup("Please provide the valid image id ....");
                } else {
                    // Upload manifest to Glance
                    String manifestGlanceID = glanceObject.uploadManifest(manifestLocation);
                
                    if(manifestGlanceID == null) {
                        String message = "Failed to upload the Manifest to Glance .... Exiting";
                        showUploadSuccessMessage(primaryStage, message);
                        System.exit(1);
                    }
                
                    // Update Image property
                    boolean isSuccess = glanceObject.updateImageProperty(imageIDTField.getText(), "x-image-meta-property-manifest_uuid" , manifestGlanceID);
                
                    if(!isSuccess) {
                        String message = "Failed to update the glance image property.....Exiting";
                        showUploadSuccessMessage(primaryStage, message);
                        System.exit(1);
                    }
                    String message = "Manifest Uploaded to glance " + "\n\n" + "Glance ID is : " + manifestGlanceID;
                    showUploadSuccessMessage(primaryStage, message);
                }
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(vbox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show(); 
        
    }
    
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
                generateManifesConfirmation(primaryStage);
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(vbox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();         
        
    }
    
    private String setImagePropertiesAndUploadToGlance(Map<String, String> confInfo, String manifestLocation, boolean isEncrypted, Stage primaryStage) {
        
        String imageName = confInfo.get(Constants.IMAGE_NAME);
        String diskFormat = null;
        String containerFormat = null;
        boolean isSuccess = true;
        String isPublic = "true";
        String imageId = confInfo.get(Constants.IMAGE_ID);
        UploadToGlance glanceObject = new UploadToGlance();
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
            
            if(isEncrypted) {
                kernelGlanceID = glanceObject.uploadImage(confInfo.get(Constants.Enc_KERNEL_PATH), imageProperties);
                if(kernelGlanceID == null) {
                    String message = "Failed to upload the Image to Glance .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);
                }
                isSuccess = glanceObject.updateImageProperty(kernelGlanceID, "x-image-meta-property-mh_encrypted", "true");
                if(!isSuccess) {
                    String message = "Failed to update the Glance Image property .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);                
                }
                isSuccess = glanceObject.updateImageProperty(kernelGlanceID, "x-image-meta-property-mh_checksum", new GenerateHash().computeHash(md, new File(confInfo.get(Constants.KERNEL_PATH))));
                if(!isSuccess) {
                    String message = "Failed to update the Glance Image property .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);                
                }
                isSuccess = glanceObject.updateImageProperty(kernelGlanceID, "x-image-meta-property-mh_dek_url", confInfo.get(Constants.MH_DEK_URL_KERNEL));
                if(!isSuccess) {
                    String message = "Failed to update the Glance Image property .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);                
                }
            } else {
                kernelGlanceID = glanceObject.uploadImage(confInfo.get(Constants.KERNEL_PATH), imageProperties);
                if(kernelGlanceID == null) {
                    String message = "Failed to upload the Image to Glance .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);
                }
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
                initrdGlanceID = glanceObject.uploadImage(confInfo.get(Constants.Enc_INITRD_PATH), imageProperties);
                if(initrdGlanceID == null) {
                    String message = "Failed to upload the Image to Glance .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);
                }
                isSuccess = glanceObject.updateImageProperty(initrdGlanceID, "x-image-meta-property-mh_encrypted", "true");
                if(!isSuccess) {
                    String message = "Failed to update the Glance Image property .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);                
                }
                isSuccess = glanceObject.updateImageProperty(initrdGlanceID, "x-image-meta-property-mh_checksum", new GenerateHash().computeHash(md, new File(confInfo.get(Constants.INITRD_PATH))));
                if(!isSuccess) {
                    String message = "Failed to update the Glance Image property .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);                
                }
                isSuccess = glanceObject.updateImageProperty(initrdGlanceID, "x-image-meta-property-mh_dek_url", confInfo.get(Constants.MH_DEK_URL_INITRD));
                if(!isSuccess) {
                    String message = "Failed to update the Glance Image property .... Exiting";
                    showUploadSuccessMessage(primaryStage, message);
                    System.exit(1);                
                }
            } else {
                initrdGlanceID = glanceObject.uploadImage(confInfo.get(Constants.INITRD_PATH), imageProperties);
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
            imageGlanceID = glanceObject.uploadImage(confInfo.get("EncImage Location"), imageProperties);
            if(imageGlanceID == null) {
                String message = "Failed to upload the Image to Glance .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);
            }
            
            isSuccess = glanceObject.updateImageProperty(imageGlanceID, "x-image-meta-property-mh_encrypted", "true");            
            if(!isSuccess) {
                String message = "Failed to update the Glance Image property .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);                
            }
            isSuccess = glanceObject.updateImageProperty(imageGlanceID, "x-image-meta-property-mh_checksum", new GenerateHash().computeHash(md, new File(confInfo.get("Image Location"))));
            if(!isSuccess) {
                String message = "Failed to update the Glance Image property .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);                
            }
            isSuccess = glanceObject.updateImageProperty(imageGlanceID, "x-image-meta-property-mh_dek_url", confInfo.get(Constants.MH_DEK_URL_IMG));
            if(!isSuccess) {
                String message = "Failed to update the Glance Image property .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);                
            }
        } else {
            imageGlanceID = glanceObject.uploadImage(confInfo.get("Image Location"), imageProperties);
            if(imageGlanceID == null) {
                String message = "Failed to upload the Image to Glance .... Exiting";
                showUploadSuccessMessage(primaryStage, message);
                System.exit(1);
            }            
        }
        String manifestGlanceID = glanceObject.uploadManifest(manifestLocation);
        if(manifestGlanceID == null) {
            String message = "Failed to upload the Manifest to Glance .... Exiting";
            showUploadSuccessMessage(primaryStage, message);
            System.exit(1);
        }
        isSuccess = glanceObject.updateImageProperty(imageGlanceID, "x-image-meta-property-manifest_uuid", manifestGlanceID);
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
