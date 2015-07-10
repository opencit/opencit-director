/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.ui;

import com.intel.mtwilson.director.javafx.utils.ConfigProperties;
import com.intel.mtwilson.director.javafx.utils.FileUtilityOperation;
import com.intel.mtwilson.director.javafx.utils.GenerateTrustPolicy;
import com.intel.mtwilson.director.javafx.utils.ImageInUseException;
import com.intel.mtwilson.director.javafx.utils.MountVMImage;
import com.intel.mtwilson.director.javafx.utils.UnsuccessfulImageMountException;
import java.io.File;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.apache.derby.diag.ErrorMessages;
import com.intel.mtwilson.util.exec.ExecUtil;
import java.io.IOException;
/**
 *
 * @author preetisr
 */
public class CreateImage {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateImage.class);
    private final Stage createImageStage;
    private TextField imagePathTField;
    private TextField imageNameTField;
    private TextField imageIDTField;
    private CheckBox encryptImage;
    private ChoiceBox imageFormatChoiceBox;
    private ChoiceBox hashTypeChoiceBox;
    private final ToggleGroup togBoxMeasure = new ToggleGroup();
    private ConfigProperties configProperties;

    String delimiter = "/";

    public CreateImage(Stage createImageStage) throws Exception {
        this.createImageStage = createImageStage;
        configProperties = new ConfigProperties();
    }

    // Return the Stage
    public Stage getStage() {
        return this.createImageStage;
    }

    public void launch(final String imageDeploymentType) {

        createImageStage.setTitle("Create Policy");

        final FileUtilityOperation op = new FileUtilityOperation();

        ObservableList<String> hashTypeList = FXCollections.observableArrayList(
                "SHA256", "SHA1"
        );

        ObservableList<String> imageFormatList = FXCollections.observableArrayList(
                "qcow2", "raw", "vhd"
        );

        //PS: New label for Create Image window
        Label imageFormat = new Label("Image Format");
        Label hashType = new Label("Hash Type");
        Label imagePath = new Label("Image Path");
        Label chooseManifest = new Label("Choose Manifest");
        Label launchPolicy = new Label("Launch Control Policy");
        Label imageName = new Label("Image Name");
        Label imageID = new Label("Image ID");

        imagePathTField = new TextField();
        imagePathTField.setEditable(false);
        imageNameTField = new TextField();
        imageIDTField = new TextField();
        imageFormatChoiceBox = new ChoiceBox(imageFormatList);
        imageFormatChoiceBox.setValue("qcow2");
        hashTypeChoiceBox = new ChoiceBox(hashTypeList);
        hashTypeChoiceBox.setValue("SHA-256");

        Button browseImage = new Button("Browse");
        browseImage.setPrefSize(80, 15);
        Tooltip toolTip = new Tooltip();
        toolTip.setText("For ami and vhd image format, provide the tar bundled image");
        browseImage.setTooltip(toolTip);

        final Button browseManifest = new Button("Next");
        browseManifest.setPrefSize(80, 15);
        Tooltip toolTipManifest = new Tooltip();
        toolTipManifest.setText("Select Image");
        browseImage.setTooltip(toolTipManifest);

        Button saveButton = new Button("Upload Later");
        Button uploadButton = new Button("Upload");
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);

//        final ToggleGroup togBoxMeasure=new ToggleGroup();
        RadioButton rbMeasure = new RadioButton("Measure Only");
        rbMeasure.setToggleGroup(togBoxMeasure);
        rbMeasure.setUserData("MeasureOnly");
        rbMeasure.setSelected(true);
        RadioButton rbMeasureEnforce = new RadioButton("Measure and Enforce");
        rbMeasureEnforce.setUserData("MeasureAndEnforce");
        rbMeasureEnforce.setToggleGroup(togBoxMeasure);

        encryptImage = new CheckBox("Encrypt VM Image");
        encryptImage.setSelected(true);

        VBox vBox = new VBox();

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(30, 15, 30, 15));

        grid.add(imageFormat, 0, 1);
        grid.add(imageFormatChoiceBox, 1, 1);

        grid.add(imageID, 0, 2);
        grid.add(imageIDTField, 1, 2);

        final HBox imagePathHBox = new HBox();
        imagePathHBox.setPadding(new Insets(3, 0, 5, 0));
        imagePathHBox.setSpacing(10);
        imagePathHBox.getChildren().addAll(imagePathTField, browseImage);

        grid.add(imagePath, 0, 3);
        grid.add(imagePathHBox, 1, 3);

        grid.add(imageName, 0, 4);
        grid.add(imageNameTField, 1, 4);

        final HBox launchPolicyHBox = new HBox();
        launchPolicyHBox.setPadding(new Insets(3, 0, 5, 0));
        launchPolicyHBox.setSpacing(10);
        launchPolicyHBox.getChildren().add(rbMeasure);
        launchPolicyHBox.getChildren().add(rbMeasureEnforce);
        grid.add(launchPolicy, 0, 6);
        grid.add(launchPolicyHBox, 1, 6);
        if(imageDeploymentType.equals("VM")){
            grid.add(encryptImage, 0, 7);
            encryptImage.setSelected(false);
        }
 
        // Set the Image ID
        String uuid = null;
        try {
            uuid = new UserConfirmation().getUUID();
        } catch (Exception exception) {
            ErrorMessage.showErrorMessage(createImageStage, exception);
            return;
        }
        imageIDTField.setText(uuid);

        HBox hBox4 = new HBox();
        hBox4.setPadding(new Insets(10, 12, 10, 12));
        hBox4.setSpacing(30);
        hBox4.setStyle("-fx-background-color: #336699;");
        hBox4.getChildren().add(cancelButton);
        hBox4.getChildren().add(browseManifest);

        vBox.getChildren().addAll(grid, hBox4);

        // Handler for 'Browse' for Image Path button, browse the vm image
        browseImage.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                FileChooser imageFile = new FileChooser();
                try {
                    File file = imageFile.showOpenDialog(createImageStage);
                    if(file != null && file.exists()){
                        imagePathTField.setText(file.getAbsolutePath());
                        imageNameTField.setText(getImageNameFromPath(imagePathTField.getText()));
                    }
                } catch (Exception e) {
                    ErrorMessage.showErrorMessage(createImageStage, e);
                }
            }
        });

        // Handler for "NEXT" Button
        browseManifest.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                try {
                    boolean includeImageHash = false;
                    // Write configuration values to map
                    Map<String, String> configInfo;
                    configInfo = writeToMap();
                    configInfo.put(Constants.IMAGE_DEPLOYMENT_TYPE, imageDeploymentType);
                    if (configInfo != null) {
                        Iterator it = configInfo.entrySet().iterator();
                        log.debug("Configuration Values Are ");
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            log.debug(pairs.getKey().toString() + " : " + pairs.getValue().toString());
                        }

                        int exitCode = 21;
                        // Check for ami Image

                        if (configInfo.get(Constants.IMAGE_TYPE).equals("ami")) {
                            /*
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
                             */
                        } else {
                            String imagePath=imagePathTField.getText();
                            String mountType;
                            if(imageDeploymentType.equals("BM")){
                                imagePath = createImageCopy(imagePath);
                                configInfo.put(Constants.IMAGE_NAME, imagePath);    
                                mountType="writable";
                            }
                            else{
                                mountType = "read-only";
                            }
                            //get image path md5 and use last four digits of md5 as a prefix of directory name
                            String pathHash = new GenerateTrustPolicy().computeHash(MessageDigest.getInstance("MD5"),imagePath);
                            //Get image name from path and remove all spaces
                            String dirname = pathHash.substring(28,32)+getImageNameFromPath(imagePath).replaceAll(" ", "");
                            String mountpath = "/mnt/director/images/" + dirname;
                            log.debug("Mount path is {}",mountpath);
                            //check if mount path already exist. If it does then image is already in use and we need to throw an exception
                            if(new File(mountpath).exists()){
                                ErrorMessage.showErrorMessage(createImageStage, new ImageInUseException(mountpath));
                                return;
                            }
                            configInfo.put(Constants.MOUNT_PATH, mountpath);
                            // Mount the VM disk image
                            exitCode = MountVMImage.mountImage(imagePath, mountpath, mountType);
                            log.info("Exit Code" + exitCode);
                            if (exitCode == 0) {
                                if (new File(configInfo.get(Constants.MOUNT_PATH) + "/Windows/System32/ntoskrnl.exe").exists()) {
                                    configInfo.put(Constants.IS_WINDOWS, "true");
                                } else {
                                    configInfo.put(Constants.IS_WINDOWS, "false");
                                }
                                BrowseDirectories secondWindow = new BrowseDirectories(createImageStage);
                                secondWindow.launch(configInfo);
                            } else {
                                log.error("Error while mounting the image. Exit code {}", exitCode);
                                ErrorMessage.showErrorMessage(createImageStage, new UnsuccessfulImageMountException(Integer.toString(exitCode)));
                            }
                        }
//                    }
                    }
                } catch (Exception e) {
                    new ErrorMessage().showErrorMessage(createImageStage, e);
                }

            }

            

        });

        // Handler for "Cancel" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Will close the window
                try {
                    createImageStage.close();
                    ConfigurationInformation window = new ConfigurationInformation(createImageStage);
                    window.launch();
                } catch (Exception e) {
                    ErrorMessage.showErrorMessage(createImageStage, e);
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
    private String createImageCopy(String imagePath) throws IOException, InterruptedException {
                String imageName = getImageNameFromPath(imagePath);
                String newPath = imagePath.replace(imageName, imageName+"-edited");
                new MountVMImage().callExec("cp "+imagePath+"  "+newPath);
                return newPath;
    }
    
    private String getImageNameFromPath(String path) {
        int index = path.lastIndexOf(File.separator);
        String name = path.substring(index + 1);
        if(name.contains("."))
            return name.substring(0,name.lastIndexOf("."));
        return name;
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
        Map<String, String> configInfo = new HashMap<>();
        boolean isProper = true;
        FileUtilityOperation opt = new FileUtilityOperation();
        if (("".equals(imageIDTField.getText())) || ("".equals(imagePathTField.getText())) || ("".equals(imageNameTField.getText()))) {
            showWarningPopup("Some fields are Empty .. Please fill the Values");
            isProper = false;
        } else if (!opt.validateUUID(imageIDTField.getText())) {
//            showWarningPopup("Please provide the valid image ID");
            isProper = false;
        }
//        }  else if((imageFormatChoiceBox.getValue().toString().equals("ami") || imageFormatChoiceBox.getValue().toString().equals("vhd")) && !imagePathTField.getText().endsWith(".tgz") && !imagePathTField.getText().endsWith("tar.gz") && !imagePathTField.getText().endsWith(".gz")) {
////            showWarningPopup("Please provide the tar bundled image ");
//            isProper = false;
//        }         
        if (isProper) {
            configInfo.put(Constants.IMAGE_NAME, imageNameTField.getText());
            configInfo.put(Constants.IMAGE_ID, imageIDTField.getText());
            configInfo.put(Constants.IMAGE_LOCATION, imagePathTField.getText());
            configInfo.put(Constants.IMAGE_TYPE, imageFormatChoiceBox.getValue().toString());
            configInfo.put(Constants.VM_WHITELIST_HASH_TYPE, hashTypeChoiceBox.getValue().toString());
            configInfo.put(Constants.POLICY_TYPE, togBoxMeasure.getSelectedToggle().getUserData().toString());
            if (encryptImage.isSelected()) {
                configInfo.put(Constants.IS_ENCRYPTED, "true");
            } else {
                configInfo.put(Constants.IS_ENCRYPTED, "false");
            }
        } else {
            return null;
        } 
        return configInfo;
    }
}
