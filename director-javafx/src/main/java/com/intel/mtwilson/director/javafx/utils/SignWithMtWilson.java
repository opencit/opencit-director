package com.intel.mtwilson.director.javafx.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import static com.intel.mtwilson.configuration.ConfigurationFactory.getConfiguration;
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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignWithMtWilson.class);
    
    private String mtWilsonIP;
    private String mtWilsonPort;
    private String mtWilsonUserName;
    private String mtWilsonPassword;
    
    public SignWithMtWilson() throws IOException {
        mtWilsonIP = getConfiguration().get(Constants.MTWILSON_SERVER);
        mtWilsonPort = getConfiguration().get(Constants.MTWILSON_PORT);
        mtWilsonUserName = getConfiguration().get(Constants.MTWILSON_USER_NAME);
        mtWilsonPassword = getConfiguration().get(Constants.MTWILSON_PASSWORD);
    }
    
    public String signManifest(String imageID, String trustPolicy) throws Exception{
//        this.mtWilsonIP = ip;
//        this.mtWilsonPort = port;
        String response = getMtWilsonResponse(trustPolicy);
        log.info("Signed the manifest with Mt. Wilson\n" + "Mt. Wilson response is :\n" + response);
        
        return response;
        
    }

    private String getMtWilsonResponse(String trustPolicy) throws Exception{
        String mtWisontResponse = null;
        try {
            String url = "https://" + mtWilsonIP + ":" + mtWilsonPort + "/mtwilson/v2/trustpolicy-signature";
            log.debug("MTwilson URL is" + url);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(url);
            HttpEntity entity = new ByteArrayEntity(trustPolicy.getBytes("UTF-8"));
            
            postRequest.setEntity(entity);
            postRequest.setHeader("Content-Type", "application/xml");
            postRequest.setHeader("Accept", "application/xml");
            if (mtWilsonUserName == null || mtWilsonPassword == null) {
                log.warn("Mt Wilson credentials are not present in property file");
                return null;
            }
            String encryptedUserNameAndPassword = new FileUtilityOperation().base64Encode(mtWilsonUserName + ":" + mtWilsonPassword);
            
            postRequest.setHeader("Authorization", "Basic " + encryptedUserNameAndPassword);
            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                log.error(null, new RuntimeException(response.getStatusLine().toString()));
                throw new MtwConnectionException();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            String output = null;
            StringBuilder sb = new StringBuilder();
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
                

            mtWisontResponse = sb.toString();
                
        } catch (MalformedURLException ex) {
            log.error(null, ex);
            return null;
        } catch (IOException ex) {
            log.error(null, ex);
            return null;
        }
     return mtWisontResponse;
    }  
}
