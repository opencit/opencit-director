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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
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
//    private final Stage secondStage;
    String hostManifest;
    
    private static final Logger logger; 
    
    private HBox togBoxTrustPolicyType;
    private HBox togBoxBareMetalType;
    
	// Set FileHandler for logger
    static {
        logger = Logger.getLogger(ConfigurationInformation.class.getName());
        LoggerUtility.setHandler(logger);
    }
    
    public ConfigurationInformation(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
	// Return the Stage
    public Stage getStage() {
    return this.primaryStage;
    }
    public void launch() {
                
        // Check for the Host Manifest
        hostManifest = ConfigProperties.getProperty(Constants.HOST_MANIFEST);
        if(hostManifest != null) {
            hostManifest = hostManifest.trim();
        }
        
        primaryStage.setTitle("Trust Director");
        final FileUtilityOperation op = new FileUtilityOperation();
	
        //PS:Label
	Label imageType=new Label("Image Type");
        final Label trustPolicy=new Label("Trust Policy");
        final Label manifestSource=new Label (" Manifest Source");
        
        
        
        //PS: Toggle Button to choose the Image Type
	final ToggleGroup imageGroup=new ToggleGroup();
	ToggleButton tb_none = new ToggleButton("None");
	tb_none.setToggleGroup(imageGroup);
	tb_none.setSelected(true);
		
	final ToggleButton tb_vm = new ToggleButton("VM");
	tb_vm.setToggleGroup(imageGroup);
        tb_vm.setUserData(trustPolicy);
        tb_vm.setUserData(togBoxTrustPolicyType);
		
	ToggleButton tb_bareMetal = new ToggleButton("Bare Metal");
	tb_bareMetal.setToggleGroup(imageGroup);
        tb_bareMetal.setUserData(manifestSource);
        tb_bareMetal.setUserData(togBoxBareMetalType);
		
        ToggleButton tb_docker = new ToggleButton("Docker");
	tb_docker.setToggleGroup(imageGroup);
        
        
        Button closeButton=new Button("Close");
		        
        //PS: Toggle Button to choose the Trust Policy Type
		final ToggleGroup trustPolicyGroup=new ToggleGroup();
		ToggleButton tb_createImage= new ToggleButton("Create Image");
		tb_createImage.setToggleGroup(trustPolicyGroup);
		ToggleButton tb_uploadExisting = new ToggleButton("Upload Existing");
		tb_uploadExisting.setToggleGroup(trustPolicyGroup);
				
        //PS: Toggle Button to choose the Bare Metal types
		final ToggleGroup bareMetalGroup=new ToggleGroup();
		ToggleButton tb_localSystem= new ToggleButton("Local System");
		tb_localSystem.setToggleGroup(bareMetalGroup);
        ToggleButton tb_remoteSystem = new ToggleButton("Remote System");
		tb_remoteSystem.setToggleGroup(bareMetalGroup);
                
                
		//PS: Add toggle button to HBox
        HBox togBoxImageType=new HBox();
        togBoxImageType.setPadding(new Insets(3, 3, 5, 5));
        togBoxImageType.setSpacing(10);
        togBoxImageType.getChildren().add(tb_none);
        togBoxImageType.getChildren().add(tb_vm);
        togBoxImageType.getChildren().add(tb_bareMetal);
        togBoxImageType.getChildren().add(tb_docker);
        
        //PS: HBox for TrustPolicy Type toggle button
        final HBox togBoxTrustPolicyType=new HBox();
        togBoxTrustPolicyType.setPadding(new Insets(1, 1, 3, 3));
        togBoxTrustPolicyType.setSpacing(10);
        togBoxTrustPolicyType.getChildren().add(trustPolicy);
        togBoxTrustPolicyType.getChildren().add(tb_createImage);
        togBoxTrustPolicyType.getChildren().add(tb_uploadExisting);
        togBoxTrustPolicyType.setVisible(false);
        
        //PS: HBox for TrustPolicy Type toggle button
        final HBox togBoxBareMetalType=new HBox();
        togBoxBareMetalType.setPadding(new Insets(3, 3, 5, 5));
        togBoxBareMetalType.setSpacing(10);
        togBoxBareMetalType.getChildren().add(manifestSource);
        togBoxBareMetalType.getChildren().add(tb_localSystem);
        togBoxBareMetalType.getChildren().add(tb_remoteSystem);
        togBoxBareMetalType.setVisible(false);
        	        
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        
        
        grid.add(imageType, 0,1);
        grid.add(togBoxImageType, 1, 1);
        
        grid.add(togBoxTrustPolicyType, 1, 2);
        grid.add(togBoxBareMetalType, 1, 2);
        
        grid.add(closeButton, 1, 3);
	
        VBox vBox = new VBox();
        vBox.getChildren().addAll(grid);
        
        
        //PS: When NONE button is clicked
		tb_none.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) 
            {
                //Execute some code here for the event.. 
                manifestSource.setVisible(false);
                togBoxBareMetalType.setVisible(false);
                trustPolicy.setVisible(false);
                togBoxTrustPolicyType.setVisible(false);
                
                
               
            } });
                
        //PS: When VM button is chosen        
        tb_vm.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) 
            {
                //Execute some code here for the event.. 
                manifestSource.setVisible(false);
                togBoxBareMetalType.setVisible(false);
                
                trustPolicy.setVisible(true);
                togBoxTrustPolicyType.setVisible(true);
                
                
               
            } });
        
        //PS: When Bare Metal option is chosen
        tb_bareMetal.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) 
            {
                //Execute some code here for the event.. 
                trustPolicy.setVisible(false);
                togBoxTrustPolicyType.setVisible(false);
                
                manifestSource.setVisible(true);
                togBoxBareMetalType.setVisible(true);
            } });
        
        //PS: DOCKER button is selected
        tb_docker.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) 
            {
                //Execute some code here for the event.. 
                manifestSource.setVisible(false);
                togBoxBareMetalType.setVisible(false);
                trustPolicy.setVisible(false);
                togBoxTrustPolicyType.setVisible(false);
                
                
               
            } });
        
        //Open a new window for CREATE IMAGE button
        tb_createImage.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e)
            {
                
                try{
                    Stage createImageStage=new Stage();
                    CreateImage createImageObj=new CreateImage(createImageStage);
                    createImageObj.launch();
//                
                }catch(Exception ex)
                {
                    
                    System.out.println("Exception occurred here");
                    ex.printStackTrace();
                }
//                Stage stage = new Stage();
                //Fill stage with content
//                stage.show();
            }});
        
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e)
            {
                
                try{
                    primaryStage.close();
                }catch(Exception ex)
                {
                    
                    System.out.println("Exception occurred here");
                    ex.printStackTrace();
                }
//                Stage stage = new Stage();
                //Fill stage with content
//                stage.show();
            }});
        
        // Load the stack pane: This is the primary window for TD
        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show(); 
        
    }
    
 }
    
   