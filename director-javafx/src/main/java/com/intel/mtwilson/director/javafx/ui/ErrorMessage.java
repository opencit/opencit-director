/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author boskisha
 */
public class ErrorMessage {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorMessage.class);
    public static void showErrorMessage(final Stage primaryStage, Exception e) {
        Label message = new Label(e.getClass().getName());
        log.error("{}",e);
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
    
    public static void ConfirmationMessage( Exception e) {
        
    }
}
