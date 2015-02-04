package com.intel.mtwilson.director.javafx.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.director.javafx.ui.Constants;
import com.intel.mtwilson.director.javafx.ui.UserConfirmation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.json.XML;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author root
 */
public class SignWithMtWilson {
    private static final Logger logger = Logger.getLogger(SignWithMtWilson.class.getName());
//    private String mtWilsonIP;
//    private String mtWilsonPort;
    
    ConfigProperties configProperties;
    private String mtWilsonIP;
    private String mtWilsonPort;
    private String mtWilsonUserName;
    private String mtWilsonPassword;
    
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }
    
    public SignWithMtWilson() {
        ConfigProperties configProperties = new ConfigProperties();
        mtWilsonIP = configProperties.getProperty(Constants.Mt_WILSON_IP);
        mtWilsonPort = configProperties.getProperty(Constants.Mt_WILSON_PORT);
        mtWilsonUserName = configProperties.getProperty(Constants.Mt_WILSON_USER_NAME);
        mtWilsonPassword = configProperties.getProperty(Constants.Mt_WILSON_PASSWORD);
    }
    
    public String signManifest(String imageID, String trustPolicy) {
//        this.mtWilsonIP = ip;
//        this.mtWilsonPort = port;
        String response = getMtWilsonResponse(trustPolicy);
        logger.info("Signed the manifest with Mt. Wilson\n" + "Mt. Wilson response is :\n" + response);
        
        return response;
        
    }

    private String getMtWilsonResponse(String trustPolicy) {
        String mtWisontResponse = null;
        try {
            String url = "https://" + mtWilsonIP + ":" + mtWilsonPort + "/mtwilson/v2/manifest-signature";
            logger.info("MTwilson URL is" + url);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(url);
            HttpEntity entity = new ByteArrayEntity(trustPolicy.getBytes("UTF-8"));
            
            postRequest.setEntity(entity);
            postRequest.setHeader("Content-Type", "application/xml");
            postRequest.setHeader("Accept", "application/xml");
            if (mtWilsonUserName == null || mtWilsonPassword == null) {
                logger.warning("Mt Wilson credentials are not present in property file");
                return null;
            }
            String encryptedUserNameAndPassword = new FileUtilityOperation().base64Encode(mtWilsonUserName + ":" + mtWilsonPassword);
            
            postRequest.setHeader("Authorization", "Basic " + encryptedUserNameAndPassword);
            HttpResponse response = httpClient.execute(postRequest);
//            System.out.println("HTTP Response is" + response);
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.log(Level.SEVERE, null, new RuntimeException(response.getStatusLine().toString()));
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            String output = null;
            StringBuffer sb = new StringBuffer();
//            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
                

            mtWisontResponse = sb.toString();
                
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, null, e);
            return null;
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
            return null;
        }
            
/*        String response = "<manifest_signature>" +
                    "<vm_image_id>123456</vm_image_id>" +
                    "<manifest_hash>" + fileHash + "</manifest_hash>" +
                    "<customer_id>982734</customer_id>" +
                    "<signature>abcdef01234567890abcdef01234567890</signature>" +
                    "<document><vm_manifest><customer_id>1234</customer_id><image_id>1235289</image_id><manifest_hash>aaaaaa</manifest_hash></vm_manifest></document>" +
                    "</manifest_signature>";
*/
        return mtWisontResponse;
    }  
}
