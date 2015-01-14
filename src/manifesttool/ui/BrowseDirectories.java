/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manifesttool.ui;

import manifesttool.utils.GenerateHash;
import manifesttool.utils.MountVMImage;
import manifesttool.ui.ConfigurationInformation;
import manifesttool.ui.Directories;
import java.io.File;
import java.util.ArrayList;
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
import javafx.stage.Stage;
import static manifesttool.ui.AMIImageInformation.logger;
import manifesttool.utils.GenerateManifest;
import manifesttool.utils.LoggerUtility;

/**
 *
 * @author admkrushnakant
 */
public class BrowseDirectories {
    
    private Stage primaryStage;
    private Scene firstWindowScene;

    private String mountPath = Constants.MOUNT_PATH;
    
    private ObservableList<String> choices = FXCollections.observableArrayList(
            "Binaries", "All Files", "Custom Formats");
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

    BrowseDirectories() {
        System.out.println("Default Constructor");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
    }
    
    public void launch(final Map<String, String> confInfo) {
        
         
        // Depending upon mounted image(Windows or Linux)
        initializeDefaultDirectoryList(Boolean.valueOf(confInfo.get(Constants.IS_WINDOWS)));
        
        System.out.println("############# : " + "On the Browse directory  window");
        
       //By default disable the text field from table
        for(Directories listComp : list) {
            initializeTableComponents(listComp);
        }
        
        primaryStage.setTitle("Generate Manifest!");
        
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
        final CheckBox includeHiddenFiles = new CheckBox("Include Hidden Files");
        includeHiddenFiles.setSelected(true);
        
        selectAllHBox.getChildren().addAll(selectAllCBox, includeHiddenFiles);    
        
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

        TableColumn secondColumn = new TableColumn("File Filter");
        //secondColumn.setMinWidth(100);
        secondColumn.setCellValueFactory(new PropertyValueFactory("choice"));
        
        TableColumn thirdColumn = new TableColumn("File Formats");
        //thirdColumn.setMinWidth(100);
        thirdColumn.setCellValueFactory(new PropertyValueFactory("tfield"));
        
        directoryTable.setItems(list);
        directoryTable.getColumns().addAll(firstColumn, secondColumn, thirdColumn);
        
        flowPane.getChildren().add(directoryTable);
        
        HBox hBox2 = new HBox();
        hBox2.setPadding(new Insets(15, 12, 15, 12));
        hBox2.setSpacing(155);
        hBox2.setStyle("-fx-background-color: #336699;");
        Button browse = new Button("Add more");
        browse.setPrefSize(100, 20);
        Button hash = new Button("Calculate Hash");
        //hash.setPrefSize(100, 20);
        hBox2.getChildren().add(browse);
        hBox2.getChildren().add(hash);
        
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
                                    System.out.println("Selected the directory checkbox");
                                } else {
                                    checkBox = new CheckBox(file.getAbsolutePath().replace(mountPath, ""));
                                    
/*                                    // Add listener to checkbox: deselect the 'Select All' checkbox if any directory is unselected  
                                    checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                        @Override
                                        public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                                            System.out.println(old_val + " -- " + new_val);
                                            if(!new_val) {
                                                selectAllCBox.setSelected(false);
                                            }
                                        }
                                    });
*/
                                    checkBox.setSelected(true);
                                    Directories dir = new Directories(checkBox, new ChoiceBox(choices), new TextField());
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
        hash.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                boolean isProper = true;
                boolean isDirExist = true;
                List<Directories> dirList = new ArrayList<>();
                for (Directories dir : list) {
                    if(dir.getCbox().isSelected()) {
                        if(!new File(mountPath + dir.getCbox().getText()).exists()) {
                            isDirExist = false;
                            break;
                        }
                        dirList.add(dir);
                        if(dir.getChoice().getValue().toString().equals("Custom Formats") && dir.getTfield().getText().equals("")) {
                            isProper = false;
                            break;
                        }
                    }
                }
                if(!isDirExist) {
                    new CreateImage(primaryStage).showWarningPopup("Directory does not exist !!");
                //} else if(dirList.isEmpty()) {
                    //new FirstWindow(primaryStage).showWarningPopup("Please select atleast one directory !!");
                } else if(!isProper) {
                    new CreateImage(primaryStage).showWarningPopup("Please enter the custom file formats !!");
                } else {
                    // Add entry in confInfo for hidden file check
                    confInfo.put(Constants.HIDDEN_FILES, String.valueOf(includeHiddenFiles.isSelected()));
                    
                    // Calculate Hash and generate manifest 
                    String manifestFileLocation = new GenerateHash().calculateHash(dirList, confInfo);

                    // Unmount the VM Image
                    //MountVMImage.unmountImage(mountPath);
                    logger.info("Unmounting the VM Image");
                    int exitCode = MountVMImage.unmountImage(mountPath);
                    //System.out.println("----------------------------- \n" + "umount exit code is : " + exitCode + "\n ----------------------");

//                    if (manifestFileLocation != null) {
//                        // Show the manifest file location
//                        new UserConfirmation().showLocation(primaryStage, manifestFileLocation, confInfo);
//                    } else {
//                        //System.out.println("Error in creating the manifest file");
//                        logger.log(Level.SEVERE, "Error in creating the manifest file");
//			new CreateImage(primaryStage).showWarningPopup("Error in creating the manifest file, \n \nPlease refer the manifest-tool.log for more information");
//                    }
                    primaryStage.setScene(firstWindowScene);
                    
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
    
    // Initialize the table components i.e disable the textfield etc
    private void initializeTableComponents(final Directories dir) {
        
        dir.getTfield().setEditable(false);
        dir.getCbox().setSelected(true);
        dir.getChoice().setValue("All Files");
            
        // Add listener for choice box
        dir.getChoice().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number value, Number new_value){
                if(dir.getChoice().getSelectionModel().getSelectedIndex() == 2) {
                    dir.getTfield().setEditable(true);
                    dir.getTfield().requestFocus();
                } else {
                    dir.getTfield().setText("");
                    dir.getTfield().setEditable(false);
                }
            }
        });    
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
                    new Directories(new CheckBox("Windows/System32"), new ChoiceBox(choices), new TextField()),
                    new Directories(new CheckBox("Windows/Boot"), new ChoiceBox(choices), new TextField())
                    );
            mountPath = Constants.MOUNT_PATH + "/";
        } else {
            list = FXCollections.observableArrayList(
                    new Directories(new CheckBox("/etc"), new ChoiceBox(choices), new TextField()),
                    new Directories(new CheckBox("/sbin"), new ChoiceBox(choices), new TextField()),
                    new Directories(new CheckBox("/bin"), new ChoiceBox(choices), new TextField()),
                    new Directories(new CheckBox("/boot"), new ChoiceBox(choices), new TextField())
                    );
        }
        
    }
}
