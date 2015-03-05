/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.ui;

import static com.intel.mtwilson.director.javafx.ui.AMIImageInformation.logger;
import com.intel.mtwilson.director.javafx.utils.LoggerUtility;
import com.intel.mtwilson.director.javafx.utils.GenerateTrustPolicy;
import com.intel.mtwilson.director.javafx.utils.MHUtilityOperation;
import com.intel.mtwilson.director.javafx.utils.MountVMImage;
import com.intel.mtwilson.director.javafx.utils.SignWithMtWilson;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import javafx.scene.control.ChoiceBox;
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

/**
 *
 * @author admkrushnakant
 */
public class BrowseDirectories {
    
    private Stage primaryStage;
    private Scene firstWindowScene;

    private String mountPath = Constants.MOUNT_PATH;
    private boolean isBareMetalLocal;
    private boolean isBareMetalRemote;
    ObservableList<Directories> list = null;
    
    private static final Logger logger = Logger.getLogger(BrowseDirectories.class.getName());
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }
/*    
    final ObservableList<Directories> list = FXCollections.observableArrayList(
            new Directories(new CheckBox("/etc"), new ChoiceBox(choices), new TextField()),
            new Directories(new CheckBox("/sbin"), new ChoiceBox(choices), new TextField()),
            new Directories(new CheckBox("/bin"), new ChoiceBox(choices), new TextField()),
            new Directories(new CheckBox("/boot"), new ChoiceBox(choices), new TextField())
            );
*/    
    public BrowseDirectories(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.firstWindowScene = primaryStage.getScene();
    }

//    BrowseDirectories() {
//        System.out.println("Default Constructor");
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        
//    }
    
    public void launch(final Map<String, String> confInfo) {
        
         
        // Depending upon mounted image(Windows or Linux)
        
        initializeDefaultDirectoryList(Boolean.valueOf(confInfo.get(Constants.IS_WINDOWS)));
        isBareMetalLocal=(Boolean.valueOf(confInfo.get(Constants.BARE_METAL_LOCAL)));
        isBareMetalRemote=(Boolean.valueOf(confInfo.get(Constants.BARE_METAL_REMOTE)));
        
        if(isBareMetalLocal){
            mountPath="/";
        }
       
//        System.out.println("############# : " + "On the Browse directory  window");
        
       //By default disable the text field from table
        for(Directories listComp : list) {
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
             okButton.setOnAction(new EventHandler<ActionEvent>(){
 
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
                if(new_val) {
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
        Button browse = new Button("Add more");
        browse.setPrefSize(100, 20);
        Button next = new Button("Next");
        //hash.setPrefSize(100, 20);
        hBox2.getChildren().add(browse);
        hBox2.getChildren().add(next);
        
        //Add handler to "Add more" button
        browse.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                DirectoryChooser directory = new DirectoryChooser();
                directory.setInitialDirectory(new File(mountPath));
                CheckBox checkBox = null;
                try {
                    File file = directory.showDialog(primaryStage);
                    if(file.getAbsolutePath().contains(mountPath)) {
                        if(!file.getAbsolutePath().equals(mountPath)) {
                            if(!isDirectoryAlreadySelected(file)) {
                                if(isDirectoryAlreadyPresent(file)) {
                                } else {
                                    if(mountPath!="/"){
                                    checkBox = new CheckBox(file.getAbsolutePath().replace(mountPath, ""));
                                    } else {
                                        checkBox = new CheckBox(file.getAbsolutePath());
                                    }

                                    checkBox.setSelected(true);
                                    Directories dir = new Directories(checkBox, new TextField());
                                    initializeTableComponents(dir);
                                    list.add(dir);                                
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
                } catch(Exception ex) {
                    //System.out.println("Not selected anything");
                    //logger.info("Not selected anything");
                }
            }
        });
        
        // Add handler to "Calculate Hash" button
        next.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
//                CreateImage.manifestFlag=true;
                System.out.println("..................................................................");
                boolean isProper = true;
                boolean isDirExist = true;
                List<Directories> dirList = new ArrayList<>();
                for (Directories dir : list) {
                    if(dir.getCbox().isSelected() && (mountPath!="/")) {
                        if(!new File(mountPath + dir.getCbox().getText()).exists()) {
                            isDirExist = false;
                            break;
                        } 
                        dirList.add(dir);
                       
                    } else if(dir.getCbox().isSelected() && (mountPath=="/")) {
                        if(!new File(dir.getCbox().getText()).exists()) {
                            isDirExist = false;
                            break;
                        } 
                        dirList.add(dir);                        
                    }
                }
                if(!isDirExist) {
                    new CreateImage(primaryStage).showWarningPopup("Directory does not exist !!");
                //} else if(dirList.isEmpty()) {
                    //new FirstWindow(primaryStage).showWarningPopup("Please select atleast one directory !!");
                } else if(!isProper) {
                    new CreateImage(primaryStage).showWarningPopup("Please enter the custom file formats !!");
                } else if (confInfo!=null){
                    
                    String trustPolicy ;
                    if (!isBareMetalLocal && !isBareMetalRemote) {
                        //Encrypt image 
                        if (confInfo.containsKey(Constants.IS_ENCRYPTED) && confInfo.get(Constants.IS_ENCRYPTED).equals("true")) {
                            MHUtilityOperation mhUtil = new MHUtilityOperation();
                            String message = mhUtil.encryptImage(confInfo);
                            if (message != null) {
                                new CreateImage(primaryStage).showWarningPopup("Error while Uploading the key to KMS..... Exiting.....");
                                System.exit(1);
                            }
                        }
                        // Generate TrustPolicy and encrypt image if necessary
                        System.err.println("Calling generateTP......................................");
                        trustPolicy = new GenerateTrustPolicy().createTrustPolicy(dirList, confInfo);
                        System.err.println("After Calling generateTP......................................");
                        //sign trustpolicy with MTW and save it to a file
                        trustPolicy = new SignWithMtWilson().signManifest(confInfo.get(Constants.IMAGE_ID), trustPolicy);
                        if (trustPolicy == null | trustPolicy.equals("") | trustPolicy.equals("null")) {
                            //TODO handle exception
                        }
                    }
                    else{
                        trustPolicy = new GenerateTrustPolicy().createManifest(dirList, confInfo);                        
                    }
                    // Unmount the VM Image
                    if (!isBareMetalLocal) {
                        logger.info("Unmounting the VM Image");
                        int exitCode = MountVMImage.unmountImage(mountPath);
                    }
                    String trustPolicyLocation = saveTrustPolicy(trustPolicy, confInfo);
                    if (trustPolicyLocation != null && (!isBareMetalLocal) && (!isBareMetalRemote)) {
                        // Show the manifest file location
                        new UserConfirmation().glanceUploadConfirmation(primaryStage, trustPolicyLocation, confInfo);
                    } else if(isBareMetalLocal || isBareMetalRemote) {
                        new UserConfirmation().generateManifesConfirmation(primaryStage, trustPolicyLocation);
                        
                    }else {
                        logger.log(Level.SEVERE, "Error in creating the manifest file");
//			new ConfigurationInformation(primaryStage).showWarningPopup("Error in creating the manifest file, \n \nPlease refer the manifest-tool.log for more information");
                    }
                    
//                  primaryStage.setScene(firstWindowScene);
                    
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
    private String getHelpMessage(){
        String message = "\n"
                + "Here are some examples on how to use regular expression:\n\n" + 
"Select all files - Leave blank or put *\n" +
"Filter file based on file format - (*.bin$|*.jar$)\n" +
"Filter file based on file name - (^/root/ssl.crt$|^/root/director-javafx-0.1-SNAPSHOT.jar$)\n" +
"Filter file based on some pattern - *log*\n";
        return message;
    }
    //save trustPolicy
    private String saveTrustPolicy(String signedTrustPolicy, Map<String, String> confInfo) {
        PrintWriter out = null;
        String imagePathDelimiter = "/";
        String trustPolicyName = "/TrustPolicy-" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xml";
        String trustPolicyDirLocation;
        if (Boolean.valueOf(confInfo.get(Constants.BARE_METAL_LOCAL)) | Boolean.valueOf(confInfo.get(Constants.BARE_METAL_REMOTE))) {
            trustPolicyDirLocation = "/etc/trustdirector/trustpolicy";
        }
        else{
            String imageLocation = confInfo.get(Constants.IMAGE_LOCATION);
            int endIndex = imageLocation.lastIndexOf(imagePathDelimiter);
            trustPolicyDirLocation = imageLocation.substring(0, endIndex);
        }
        String filePath = trustPolicyDirLocation + trustPolicyName;
        try {
            out = new PrintWriter(filePath);
            out.println(signedTrustPolicy);
            out.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BrowseDirectories.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
        return filePath;
    }
    
    // Initialize the table components i.e disable the textfield etc
    private void initializeTableComponents(final Directories dir) {
        dir.getTfield().setEditable(true);
        dir.getCbox().setSelected(true);
    } 
    
    // Check the directory is already selected or not
    private boolean isDirectoryAlreadySelected(File file) {
        boolean isSelected = false;
        for(Directories testDir : list) {
            if(file.getAbsolutePath().replace(mountPath, "").equals(testDir.getCbox().getText())) {
                if(testDir.getCbox().isSelected()) {
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
        for(Directories testDir : list) {
            if(file.getAbsolutePath().replace(mountPath, "").equals(testDir.getCbox().getText())) {
                isPresent = true;
                if(!testDir.getCbox().isSelected()) {
                    testDir.getCbox().setSelected(true);
                    break;   
                }
            }
        }
        return isPresent;
    }
    

    private void initializeDefaultDirectoryList(boolean isWindows) {
        if(isWindows) {
            
            list = FXCollections.observableArrayList(
                    new Directories(new CheckBox("Windows/System32"), new TextField()),
                    new Directories(new CheckBox("Windows/Boot"), new TextField())
                    );
            mountPath = Constants.MOUNT_PATH + "/";
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
