/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.ui;

import com.intel.dcsg.cpg.net.InternetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
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
import com.intel.mtwilson.director.javafx.utils.ConfigProperties;
import com.intel.mtwilson.director.javafx.utils.GenerateTrustPolicy;
import com.intel.mtwilson.director.javafx.utils.IncorrectCredentialsException;
import com.intel.mtwilson.director.javafx.utils.MountVMImage;
import com.intel.mtwilson.director.javafx.utils.UnsuccessfulRemoteMountException;
import java.io.IOException;
import java.security.PublicKey;
import java.util.logging.Level;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javax.naming.AuthenticationException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import org.apache.commons.codec.binary.Base64;
/**
 *
 * @author preetisr
 */
public class RemoteSystem {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RemoteSystem.class);
    private final Stage remoteSystemStage;
    private ConfigProperties configProperties;
    private TextField ipAddressTField;
    private TextField userNameTField;
    private PasswordField passwordTField;
    private final RemoteHostKey remoteHostKey = new RemoteHostKey(); 
    public boolean confirmationResult=false;
    private final ToggleGroup togBoxMeasure = new ToggleGroup();
    String hostManifest;

    public RemoteSystem(Stage remoteSystemStage) {
        this.remoteSystemStage = remoteSystemStage;
        configProperties = new ConfigProperties();
    }

    public void launch() {

        // Check for the Host Manifest
        hostManifest = configProperties.getProperty(Constants.HOST_MANIFEST);
        if (hostManifest != null) {
            hostManifest = hostManifest.trim();
        }

        remoteSystemStage.setTitle("Bare Metal Remote System");

        Label ipAddress = new Label("IP Address");
        Label userName = new Label("User Name");
        Label password = new Label("Password");
        //Label launchPolicy=new Label("Launch Control Policy");       
        ipAddressTField = new TextField();
        userNameTField = new TextField();
        passwordTField = new PasswordField();

        //RadioButton rbMeasure=new RadioButton("Measure Only");
//        rbMeasure.setToggleGroup(togBoxMeasure);
//        rbMeasure.setUserData("MeasureOnly");
//        rbMeasure.setSelected(true);
//        RadioButton rbMeasureEnforce=new RadioButton("Measure and Enforce");
//        rbMeasureEnforce.setUserData("MeasureEnforce");
//        rbMeasureEnforce.setToggleGroup(togBoxMeasure);
        Button NextButton = new Button("Next");
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 15);

        VBox vBox = new VBox();

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(30, 15, 30, 15));

        grid.add(ipAddress, 0, 1);
        grid.add(ipAddressTField, 1, 1);
        grid.add(userName, 0, 2);
        grid.add(userNameTField, 1, 2);
        grid.add(password, 0, 3);
        grid.add(passwordTField, 1, 3);
        final HBox launchPolicyHBox = new HBox();
        launchPolicyHBox.setPadding(new Insets(3, 0, 5, 0));
        launchPolicyHBox.setSpacing(10);
//        launchPolicyHBox.getChildren().add(rbMeasure);
//        launchPolicyHBox.getChildren().add(rbMeasureEnforce);
//        grid.add(launchPolicy,0,4);
        grid.add(launchPolicyHBox, 1, 4);

        HBox hBox4 = new HBox();
        hBox4.setPadding(new Insets(20, 12, 20, 12));
        hBox4.setSpacing(30);
        hBox4.setStyle("-fx-background-color: #336699;");
        hBox4.getChildren().add(cancelButton);
        hBox4.getChildren().add(NextButton);

        vBox.getChildren().addAll(grid, hBox4);

        // Handler for "Browse" button to Choose Manifest, Mount the disk image and browse the directories
        NextButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                try {
                    boolean includeImageHash = false;
//                 Write configuration values to map
                    Map<String, String> customerInfo;
                    customerInfo = hostWriteToMap();
                    String mountpath = "/mnt/host/" + ipAddressTField.getText();
                    customerInfo.put(Constants.MOUNT_PATH2, mountpath);
                    // Testing ssh connection. Main reason is to add sshkey while first connection, need to find better solution 
                    boolean authentication = CheckSshConnection(ipAddressTField.getText(), userNameTField.getText(), passwordTField.getText());
                    if (!authentication && !addSshKey(ipAddressTField.getText())) {
                        log.debug("Remote host ssh key is not added to the known host");
                        ErrorMessage.showErrorMessage(remoteSystemStage, new AuthenticationException());
                        return;
                    } else {
                        log.debug("Remote Host authentication successful");
                    }

                    //calling mount script
                    int exitCode = MountVMImage.mountRemoteSystem(ipAddressTField.getText(), userNameTField.getText(), passwordTField.getText(), mountpath);
                    if (exitCode != 0) {
                        log.error("Error while mounting remote file system. Exit code {}", exitCode);
                        ErrorMessage.showErrorMessage(remoteSystemStage, new UnsuccessfulRemoteMountException(Integer.toString(exitCode)));
                        return;
                    }
                    log.debug("Remote system mounted");
                    BrowseDirectories secondWindow = new BrowseDirectories(remoteSystemStage);
                    secondWindow.launch(customerInfo);
                } catch (Exception ex) {
                    ErrorMessage.showErrorMessage(remoteSystemStage, ex);
                }
            }
        });
        
        // Handler for "Cancel" button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Will close the window
                remoteSystemStage.close();

            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(vBox);
        Scene scene = new Scene(root);
        //scene.setFill(Color.AQUA);
        remoteSystemStage.setScene(scene);
        remoteSystemStage.show();
    }
    
    public void confirmation(String confMsg) throws InterruptedException{
        Text message = new Text(confMsg);
        message.setFont(new Font("Arial", 14));
        Button yesBtn = new Button("Yes");
        Button noBtn = new Button("No");
        yesBtn.setPrefSize(80, 15);
        noBtn.setPrefSize(80, 15);
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(35);
        hbox.setStyle("-fx-background-color: #336699;");        
        hbox.getChildren().addAll(yesBtn,noBtn);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(12, 10, 12, 10));
        vbox.setSpacing(20);
        vbox.setStyle("-fx-background-color: #336699;");
        vbox.getChildren().addAll(message, hbox);
        final Popup popup = new Popup();
        popup.setX(400);
        popup.setY(250);
        Stage stage = new Stage();
        popup.getContent().addAll(vbox);
        
        yesBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                remoteSystemStage.close();
                confirmationResult = true;
                log.debug("Confirmation result is set to true");
                popup.hide();                
            }
        }); 
        noBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                confirmationResult = false;
                log.debug("Confirmation result is set to false");
                popup.hide();
            }
        }); 
        popup.show(remoteSystemStage);
        
    }
    public boolean addSshKey(String ipAddress) throws Exception {
//        String sshKeyCmd = "ssh-keyscan -t rsa 10.1.68.58";
//        log.debug(sshKeyCmd);
//        String sshKey = new GenerateTrustPolicy().executeShellCommand(sshKeyCmd);
//        log.debug(sshKey);
//        confirmation("Host key is:\n"+sshKeyCmd+"\n\nAre you sure you want to continue connecting?");
//        if (confirmationResult) {
        String addHostKey = "ssh-keyscan -Ht rsa " + ipAddress + " >> ~/.ssh/known_hosts";
        int exitCode = new UserConfirmation().executeShellCommand(addHostKey);
        log.debug("addHostKey exit code is {}",exitCode);
        return (exitCode==0);
    }
    private boolean CheckSshConnection(String ipaddress, String username, String password) throws IOException{
        InternetAddress remoteHost = new InternetAddress(ipaddress);
        SSHClient ssh = new SSHClient();        
        //RemoteHostKeyDeferredVerifier hostKeyVerifier = new RemoteHostKeyDeferredVerifier(remoteHostKey);
        //ssh.addHostKeyVerifier(hostKeyVerifier); // this accepts all remote public keys, then you have to verify the remote host key before continuing!!!
        boolean authentication = ssh.isAuthenticated();
        log.debug("Host Authentication is {}",authentication);
//        ssh.
//        ssh.connect(remoteHost.toString());
//        ssh.authPassword(username, password);
//        ssh.disconnect(); 
//        log.debug("host ssh public key is:::::"+Base64.encodeBase64String(remoteHostKey.publicKey.getEncoded()));
        
        return authentication;
    }
    
    public static class RemoteHostKey {
        public String server;
        public int port;
        public PublicKey publicKey;
    }
    
    
     public static class RemoteHostKeyDeferredVerifier implements HostKeyVerifier {
        private RemoteHostKey remoteHostKey;
        public RemoteHostKeyDeferredVerifier() {
            this.remoteHostKey = new RemoteHostKey();
        }
        public RemoteHostKeyDeferredVerifier(RemoteHostKey remoteHostKey) {
            this.remoteHostKey = remoteHostKey;
        }
        @Override
        public boolean verify(String string, int i, PublicKey pk) {
            remoteHostKey.server = string;
            remoteHostKey.port = i;
            remoteHostKey.publicKey = pk;
            return true;
        }
        public RemoteHostKey getRemoteHostKey() { return remoteHostKey; }
    }

    // Store configuration values in hash map for host manifest generation
    private Map<String, String> hostWriteToMap() {
        Map<String, String> customerInfo = new HashMap<>();
        boolean isProper = true;

        if (isProper) {
            customerInfo.put(Constants.remoteSystemIpAddress, ipAddressTField.getText().toString());
            customerInfo.put(Constants.remoteSystemuserName, userNameTField.getText().toString());
            customerInfo.put(Constants.remoteSystemPassword, passwordTField.getText().toString());
            customerInfo.put((Constants.BARE_METAL_REMOTE), "true");
            customerInfo.put((Constants.BARE_METAL_LOCAL), "false");

        } else {
            return null;
        }
        return customerInfo;
    }

}
