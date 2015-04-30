/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.ui;

import com.intel.mtwilson.director.javafx.utils.ConfigProperties;
import com.intel.mtwilson.director.javafx.utils.GenerateTrustPolicy;
import com.intel.mtwilson.director.javafx.utils.KmsUtil;
import com.intel.mtwilson.director.javafx.utils.MountVMImage;
import com.intel.mtwilson.director.javafx.utils.MtwConnectionException;
import com.intel.mtwilson.director.javafx.utils.SignWithMtWilson;
import com.intel.mtwilson.director.javafx.utils.UnmountException;
import com.intel.mtwilson.director.javafx.utils.UnsuccessfulImageMountException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.derby.diag.ErrorMessages;

/**
 *
 * @author admkrushnakant
 */
public class BrowseDirectories {

    private Stage primaryStage;
    private Scene firstWindowScene;

    private String mountPath;
    private boolean isBareMetalLocal;
    private boolean isBareMetalRemote;
    ObservableList<Directories> list = null;
    private ConfigProperties configProperties;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BrowseDirectories.class);

    public BrowseDirectories(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.firstWindowScene = primaryStage.getScene();
        configProperties = new ConfigProperties();
    }

    public void launch(final Map<String, String> confInfo) {

        // Depending upon mounted image(Windows or Linux)
        initializeDefaultDirectoryList(Boolean.valueOf(confInfo.get(Constants.IS_WINDOWS)));
        isBareMetalLocal = (Boolean.valueOf(confInfo.get(Constants.BARE_METAL_LOCAL)));
        isBareMetalRemote = (Boolean.valueOf(confInfo.get(Constants.BARE_METAL_REMOTE)));
        mountPath = confInfo.get(Constants.MOUNT_PATH2);

        if (isBareMetalLocal) {
            mountPath = "/";
        }

        log.trace("On the Browse directory  window");

        for (Directories listComp : list) {
            initializeTableComponents(listComp);
        }
        primaryStage.setTitle("Generate Trust Policy!");

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(0));
        vBox.setSpacing(0);

        HBox titleHBox = new HBox();
        titleHBox.setPadding(new Insets(15, 12, 15, 12));
        titleHBox.setSpacing(150);
        titleHBox.setStyle("-fx-background-color: #336699;");
        Text title = new Text("Selected Directories");
        title.setFont(Font.font("Arial", 14));
        titleHBox.getChildren().add(title);

        Button backButton = new Button("Back");
        backButton.setPrefSize(80, 20);
        titleHBox.getChildren().add(backButton);

        //Add 'select all' check box
        HBox selectAllHBox = new HBox();
        selectAllHBox.setPadding(new Insets(15, 12, 15, 5));
        selectAllHBox.setSpacing(130);
        final CheckBox selectAllCBox = new CheckBox("Select All");
        selectAllCBox.setSelected(true);
        Button helpBtn = new Button("Help");

        selectAllHBox.getChildren().addAll(selectAllCBox, helpBtn);

        helpBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                final Stage myDialog = new Stage();
                myDialog.initModality(Modality.WINDOW_MODAL);

                Button okButton = new Button("CLOSE");
                okButton.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent arg0) {
                        myDialog.close();
                    }

                });

                Scene myDialogScene = new Scene(VBoxBuilder.create()
                        .children(new Text(getHelpMessage()), okButton)
                        .alignment(Pos.CENTER)
                        .padding(new Insets(10))
                        .build());

                myDialog.setScene(myDialogScene);
                myDialog.show();
            }
        });

        selectAllCBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                if (new_val) {
                    for (Directories d : list) {
                        d.getCbox().setSelected(true);
                    }
                } else {
                    for (Directories d : list) {
                        d.getCbox().setSelected(false);
                    }
                }
            }
        });

        final AnchorPane flowPane = new AnchorPane();
        flowPane.setStyle("-fx-background-color: DAE6F3;");
        flowPane.autosize();

        //Add table to flowpanel
        TableView directoryTable = new TableView();
        directoryTable.setEditable(true);

        TableColumn firstColumn = new TableColumn("Directory Path");
        //firstColumn.setMinWidth(140);
        firstColumn.setCellValueFactory(new PropertyValueFactory("cbox"));

        TableColumn thirdColumn = new TableColumn("Regular Expression");
        //thirdColumn.setMinWidth(100);
        thirdColumn.setCellValueFactory(new PropertyValueFactory("tfield"));

        directoryTable.setItems(list);
        directoryTable.getColumns().addAll(firstColumn, thirdColumn);
        directoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        flowPane.getChildren().add(directoryTable);

        HBox hBox2 = new HBox();
        hBox2.setPadding(new Insets(15, 12, 15, 12));
        hBox2.setSpacing(155);
        hBox2.setStyle("-fx-background-color: #336699;");
        Button addMore = new Button("Add more");
        addMore.setPrefSize(100, 20);
        Button create = new Button("Create");
        //hash.setPrefSize(100, 20);
        hBox2.getChildren().add(addMore);
        hBox2.getChildren().add(create);

        //Add handler to "Add more" button
        addMore.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                DirectoryChooser directory = new DirectoryChooser();
                directory.setInitialDirectory(new File(mountPath));
                CheckBox checkBox = null;
                try {
                    File file = directory.showDialog(primaryStage);
                    if(file == null)
                        return;
                    if (file.getAbsolutePath().contains(mountPath)) {
                        if (!file.getAbsolutePath().equals(mountPath)) {
                            if (!isDirectoryAlreadySelected(file)) {
                                if (isDirectoryAlreadyPresent(file)) {
                                } else {
                                    if (mountPath != "/") {
                                        checkBox = new CheckBox(file.getAbsolutePath().replace(mountPath, ""));
                                    } else {
                                        checkBox = new CheckBox(file.getAbsolutePath());
                                    }
                                    Directories dir = new Directories(checkBox, new TextField());
                                    initializeTableComponents(dir);
                                    ObservableList<Directories> tmp = deepCopy(list);
//                                    tmp.add(dir);
                                    list.clear();
                                    list.add(dir);
                                    list.addAll(tmp);
                                }
                            } else {
                                new CreateImage(primaryStage).showWarningPopup("Directory Already Selected !!");
                            }
                        } else {
                            new CreateImage(primaryStage).showWarningPopup("Please select a valid directory");
                        }
                    } else {
                        new CreateImage(primaryStage).showWarningPopup("Directory does not belong to VM image");
                    }
                } catch (Exception ex) {
                    ErrorMessage.showErrorMessage(primaryStage, ex);
                }
            }

            private ObservableList<Directories> deepCopy(ObservableList<Directories> list) {
                ObservableList<Directories> copy = FXCollections.observableArrayList();
                for (Directories dir : list) {
                    copy.add(dir);
                }
                return copy;
            }
        });

        // Add handler to "Calculate Hash" button
        create.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                try {
                    log.debug("calculating hash.");
                    List<Directories> dirList = new ArrayList<>();
                    for (Directories dir : list) {
                        log.debug("Mount path {} ,dir path {}", mountPath, dir.getCbox().getText());
                        if (dir.getCbox().isSelected() && (mountPath != "/")) {
                            if (!new File(mountPath + dir.getCbox().getText()).exists()) {
                                ErrorMessage.showErrorMessage(primaryStage, new FileNotFoundException());
                                return;
                            }
                            dirList.add(dir);

                        } else if (dir.getCbox().isSelected() && (mountPath == "/")) {
                            if (!new File(dir.getCbox().getText()).exists()) {
                                ErrorMessage.showErrorMessage(primaryStage, new FileNotFoundException());
                                return;
                            }
                            dirList.add(dir);
                        }
                    }
                    
                    String trustPolicyLocation = null;
                    String manifestLocation = null;
                    
                    //create trust policy
                    String trustPolicy = new GenerateTrustPolicy().createTrustPolicy(dirList, confInfo);
                    //Encrypt image if required and update policy
                    if (confInfo.containsKey(Constants.IS_ENCRYPTED) && confInfo.get(Constants.IS_ENCRYPTED).equals("true")) {
                        KmsUtil mhUtil = new KmsUtil();
                        mhUtil.encryptImage(confInfo);
                        trustPolicy = new GenerateTrustPolicy().setEncryption(trustPolicy, confInfo);
                    }
                    //sign trustpolicy with MTW 
                    trustPolicy = new SignWithMtWilson().signManifest(confInfo.get(Constants.IMAGE_ID), trustPolicy);
                    if (trustPolicy == null | trustPolicy.equals("") | trustPolicy.equals("null")) {
                        throw new MtwConnectionException();
                    }
                    //save trust policy to a file
                    trustPolicyLocation = saveTrustPolicy(trustPolicy, confInfo);

                    if (isBareMetalLocal || isBareMetalRemote) {
                        // Generate manifest                      
                        String manifest = new GenerateTrustPolicy().createManifest(dirList, confInfo);
                        manifestLocation = saveManifest(manifest, confInfo);
                    }
                    
                    // Unmount the VM Image
                    if (!isBareMetalLocal) {
                        log.debug("Unmounting the VM Image from mount path {}", mountPath);
                        int exitCode = MountVMImage.unmountImage(mountPath);
                        if (exitCode != 0) {
                            throw new UnsuccessfulImageMountException();
                        }
                    }
                    //promp policy location or upload image
                    if (isBareMetalLocal) {
                        String message = "\nManifest:  " + manifestLocation + "\n"
                                + "Trust Policy:  " + trustPolicyLocation;
                        new UserConfirmation().showUploadSuccessMessage(primaryStage, message);
                    } else if (isBareMetalRemote) {
                        trustPolicyLocation = trustPolicyLocation.replace(mountPath, "");
                        String message = "Remote Host: \n"
                                + "Manifest:  " + confInfo.get(Constants.BM_MANIFEST_REMOTE) + "\n"
                                + "Trust Policy: " + confInfo.get(Constants.BM_TRUSTPOLICY_REMOTE) + "\n\n"
                                + "Trust Director:\n"
                                + "Manifest:  " + manifestLocation + "\n"
                                + "Trust Policy:  " + trustPolicyLocation + "\n";
                        new UserConfirmation().showUploadSuccessMessage(primaryStage, message);
                    } else {
                        new UserConfirmation().glanceUploadConfirmation(primaryStage, trustPolicyLocation, confInfo);
                    }
                } catch (Exception ex) {
                    ErrorMessage.showErrorMessage(primaryStage, ex);
                }
            }
        });

        // Handler for "Back" button
        backButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                primaryStage.setScene(firstWindowScene);
            }
        });

        vBox.getChildren().addAll(titleHBox, selectAllHBox, directoryTable, hBox2);
        BorderPane border = new BorderPane();
        border.setCenter(vBox);

        StackPane root = new StackPane();
        root.getChildren().add(border);
        primaryStage.setScene(new Scene(root));

        primaryStage.show();

    }

    private String getHelpMessage() {
        String message = "\n"
                + "Here are some examples on how to use regular expression:\n\n"
                + "Select all files - Leave blank or put *\n"
                + "Filter file based on file format - (*.bin$|*.jar$)\n"
                + "Filter file based on file name - (^/root/ssl.crt$|^/root/director-javafx-0.1-SNAPSHOT.jar$)\n"
                + "Filter file based on some pattern - openjdk-.*-jre-headless - filters out all files where path includes any versions of openjdk-jre-headless\n";
        return message;
    }

    //save trustPolicy and return file path
    private String saveTrustPolicy(String trustPolicy, Map<String, String> confInfo) throws Exception {
        String trustPolicyName = "TrustPolicy-" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xml";
        String trustPolicyDirLocation;
        if(isBareMetalLocal || isBareMetalRemote){
            //TODO get DIRECTOR_HOME
            trustPolicyDirLocation =  "/opt/director/repository/policy";
        }
        else{
            String imageLocation = confInfo.get(Constants.IMAGE_LOCATION);
            int endIndex = imageLocation.lastIndexOf("/");
            trustPolicyDirLocation = imageLocation.substring(0, endIndex);
        }
        if(!Files.exists(Paths.get(trustPolicyDirLocation)));
            MountVMImage.callExec("mkdir -p " + trustPolicyDirLocation);
        String trustpolicyPath = trustPolicyDirLocation+"/"+trustPolicyName;
        Files.write(Paths.get(trustpolicyPath), trustPolicy.getBytes());
        if(isBareMetalRemote){
            String remoteDirPath = mountPath+"/boot/trust";
            if(!Files.exists(Paths.get(remoteDirPath)));
                MountVMImage.callExec("mkdir -p " + remoteDirPath);
            String policyPath = remoteDirPath+"/"+"TrustPolicy.xml";
            Files.write(Paths.get(policyPath), trustPolicy.getBytes());
            confInfo.put(Constants.BM_TRUSTPOLICY_REMOTE,policyPath.replace(mountPath, ""));
        }
        return trustpolicyPath;
    }

    //Save baremetal manifest and return path
    private String saveManifest(String trustPolicy, Map<String, String> confInfo) throws Exception {
        String fileName = "Manifest" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xml";
        //TODO get DIRECTOR_HOME
        String manifestDir =  "/opt/director/repository/policy";
        String manifestLocalPath = manifestDir+"/"+fileName;
        if(!Files.exists(Paths.get(manifestDir)));
            MountVMImage.callExec("mkdir -p " + manifestDir);
        Files.write(Paths.get(manifestLocalPath), trustPolicy.getBytes());
        if(isBareMetalRemote){
            String remoteDirPath = mountPath+"/boot/trust";
            if(!Files.exists(Paths.get(remoteDirPath)));
                MountVMImage.callExec("mkdir -p " + remoteDirPath);
            String manifestPath = remoteDirPath+"/"+"Manifest.xml";
            Files.write(Paths.get(manifestPath), trustPolicy.getBytes());
            confInfo.put(Constants.BM_MANIFEST_REMOTE,manifestPath.replace(mountPath, ""));
        }
        return manifestLocalPath;
    }

    // Initialize the table components i.e disable the textfield etc
    private void initializeTableComponents(final Directories dir) {
        dir.getTfield().setEditable(true);
        dir.getCbox().setSelected(true);
    }

    // Check the directory is already selected or not
    private boolean isDirectoryAlreadySelected(File file) {
        boolean isSelected = false;
        for (Directories testDir : list) {
            if (file.getAbsolutePath().replace(mountPath, "").equals(testDir.getCbox().getText())) {
                if (testDir.getCbox().isSelected()) {
                    isSelected = true;
                    break;
                }
            }
        }
        return isSelected;
    }

    // Check the directory is already present or not, if present then select the directory
    private boolean isDirectoryAlreadyPresent(File file) {
        boolean isPresent = false;
        for (Directories testDir : list) {
            if (file.getAbsolutePath().replace(mountPath, "").equals(testDir.getCbox().getText())) {
                isPresent = true;
                if (!testDir.getCbox().isSelected()) {
                    testDir.getCbox().setSelected(true);
                    break;
                }
            }
        }
        return isPresent;
    }

    private void initializeDefaultDirectoryList(boolean isWindows) {
        if (isWindows) {

//            list = FXCollections.observableArrayList(
//                    new Directories(new CheckBox("Windows/System32"), new TextField()),
//                    new Directories(new CheckBox("Windows/Boot"), new TextField())
//                    );
//            mountPath = Constants.MOUNT_PATH + "/";
        } else {
            list = FXCollections.observableArrayList(
                    new Directories(new CheckBox("/etc"), new TextField()),
                    new Directories(new CheckBox("/sbin"), new TextField()),
                    new Directories(new CheckBox("/bin"), new TextField()),
                    new Directories(new CheckBox("/boot"), new TextField())
            );
        }

    }
}
