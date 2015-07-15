/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.ui;

import com.intel.mtwilson.director.javafx.utils.ConfigProperties;
import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author admkrushnakant
 */
public class ConfigurationInformation {    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationInformation.class);
    private final Stage primaryStage;
    private HBox togBoxTrustPolicyType;
    private HBox togBoxBareMetalType;
    private ConfigProperties configProperties;
    
    public ConfigurationInformation(Stage primaryStage)throws Exception {
        this.primaryStage = primaryStage;
        configProperties = new ConfigProperties();
    }
    
    // Return the Stage
    public Stage getStage() {
    return this.primaryStage;
    }
    public void launch() {
        primaryStage.setTitle("Trust Director");
	
        //PS:Label
	Label imageType=new Label("Deployment Type");
        final Label trustPolicy=new Label("Trust Policy");
        final Label manifestSource=new Label ("Source");
        
        //PS: Toggle Button to choose the Image Type
	final ToggleGroup imageGroup=new ToggleGroup();
		
	final ToggleButton tb_vm = new ToggleButton("VM");
	tb_vm.setToggleGroup(imageGroup);
        tb_vm.setUserData(trustPolicy);
        tb_vm.setUserData(togBoxTrustPolicyType);
		
	ToggleButton tb_bareMetal = new ToggleButton("Bare Metal");
	tb_bareMetal.setToggleGroup(imageGroup);
        tb_bareMetal.setUserData(manifestSource);
        tb_bareMetal.setUserData(togBoxBareMetalType);
		
        //PS: Toggle Button to choose the Trust Policy Type
		final ToggleGroup trustPolicyGroup=new ToggleGroup();
		ToggleButton tb_createImage= new ToggleButton("Create New Policy");
		tb_createImage.setToggleGroup(trustPolicyGroup);
		ToggleButton tb_uploadExisting = new ToggleButton("Upload Pending Policy");
		tb_uploadExisting.setToggleGroup(trustPolicyGroup);
				
        //PS: Toggle Button to choose the Bare Metal types
		final ToggleGroup bareMetalGroup=new ToggleGroup();
		ToggleButton tb_localSystem= new ToggleButton("Local System");
		tb_localSystem.setToggleGroup(bareMetalGroup);
        ToggleButton tb_remoteSystem = new ToggleButton("Remote System");
		tb_remoteSystem.setToggleGroup(bareMetalGroup);
                ToggleButton tb_image= new ToggleButton("Image");
		tb_image.setToggleGroup(bareMetalGroup);
                
                
		//PS: Add toggle button to HBox
        HBox togBoxImageType=new HBox();
        togBoxImageType.setPadding(new Insets(3, 3, 5, 5));
        togBoxImageType.setSpacing(10);
        togBoxImageType.getChildren().add(tb_vm);
        togBoxImageType.getChildren().add(tb_bareMetal);
        
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
        togBoxBareMetalType.getChildren().add(tb_image);
        togBoxBareMetalType.setVisible(false);
        	        
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        
        
        grid.add(imageType, 0,1);
        grid.add(togBoxImageType, 1, 1);
        
        grid.add(togBoxTrustPolicyType, 0, 2, 2, 1);
        grid.add(togBoxBareMetalType, 0, 2, 2, 1);
        
        VBox vBox = new VBox();
        vBox.getChildren().addAll(grid);
        
        
//        //PS: When NONE button is clicked
//	tb_none.setOnAction(new EventHandler<ActionEvent>() {
//            @Override public void handle(ActionEvent e) 
//            {
//                //Execute some code here for the event.. 
//                manifestSource.setVisible(false);
//                togBoxBareMetalType.setVisible(false);
//                trustPolicy.setVisible(false);
//                togBoxTrustPolicyType.setVisible(false);
//                
//                
//               
//            } });
                
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
//        tb_docker.setOnAction(new EventHandler<ActionEvent>() {
//            @Override public void handle(ActionEvent e) 
//            {
//                //Execute some code here for the event.. 
//                manifestSource.setVisible(false);
//                togBoxBareMetalType.setVisible(false);
//                trustPolicy.setVisible(false);
//                togBoxTrustPolicyType.setVisible(false);
//                
//                
//               
//            } });
        
        //Open a new window for CREATE IMAGE button
        tb_createImage.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e)
            {
                
                try{
                    primaryStage.close();
                    Stage createImageStage=new Stage();
                    CreateImage createImageObj=new CreateImage(createImageStage);
                    createImageObj.launch("VM");
//                
                }catch(Exception ex)
                {                    
                    ErrorMessage.showErrorMessage(primaryStage, ex);
                }
            }});
        
        //Open a new window to upload existing image
        tb_uploadExisting.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e)
            {
                
                try{
                    primaryStage.close();
                    Stage uploadExistingStage=new Stage();
                    UploadExisting uploadExistingObj=new UploadExisting(uploadExistingStage);
                    uploadExistingObj.launch();
                 }catch(Exception ex){
                    ErrorMessage.showErrorMessage(primaryStage, ex);
                }
                
//                Stage stage = new Stage();
                //Fill stage with content
//                stage.show();
            }});
        
        //        Open a new window to upload image from local system
        tb_localSystem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e)
            {                
                try{
                    primaryStage.close();
                    Map<String, String> customerInfo = new HashMap<>();
                    customerInfo.put((Constants.BARE_METAL_LOCAL), "true");
                    Stage broeseDirectoryStage=new Stage();
                    BrowseDirectories secondWindow = new BrowseDirectories(broeseDirectoryStage);
                    secondWindow.launch(customerInfo);
                } catch (Exception ex) {                    
                    ErrorMessage.showErrorMessage(primaryStage, ex);
                }
             }});
        
        tb_remoteSystem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e)
            {
                
                try{
                    primaryStage.close();
                    Stage remoteSystemStage=new Stage();
                    RemoteSystem remoteSystemObj=new RemoteSystem(primaryStage);
                    remoteSystemObj.launch();
//                
                }catch(Exception ex)
                {
                    ErrorMessage.showErrorMessage(primaryStage, ex);
                }
             }});
        tb_image.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e)
            {                
                try{
                    primaryStage.close();
                    Stage createImageStage=new Stage();
                    CreateImage createImageObj=new CreateImage(createImageStage);
                    createImageObj.launch("BM");
//                
                }catch(Exception ex)
                {                    
                    ErrorMessage.showErrorMessage(primaryStage, ex);
                }
            }});
        
        // Load the stack pane: This is the primary window for TD
        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show(); 
        
    }
    
    // Store configuration values in hash map for host manifest generation
    private Map<String, String> hostWriteToMap() {
        Map<String, String> customerInfo = new HashMap<>();
        boolean isProper = true;
         
        if(isProper) {
          customerInfo.put((Constants.BARE_METAL_LOCAL),"true");

        } else {
            return null;
        }
        return customerInfo;
    }
    
 }
    
   