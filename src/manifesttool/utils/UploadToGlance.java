/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package manifesttool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import manifesttool.ui.Constants;
import manifesttool.ui.UserConfirmation;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author root
 */
public class UploadToGlance {
    private static final Logger logger = Logger.getLogger(UserConfirmation.class.getName());
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }
    
    private String glanceIP = ConfigProperties.getProperty(Constants.GLANCE_IP);
    private String userName = ConfigProperties.getProperty(Constants.USER_NAME);
    private String password = ConfigProperties.getProperty(Constants.PASSWORD);
    private String tenantName = ConfigProperties.getProperty(Constants.TENANT_NAME);
    
    public String uploadManifest(String manifestLocation) {
        
        String glanceID = null;
        Map<String, String> manifestProperties = new HashMap<>();
        manifestProperties.put(Constants.NAME, manifestLocation.substring((manifestLocation.lastIndexOf("/") + 1)));
        manifestProperties.put(Constants.DISK_FORMAT, "ari");
        manifestProperties.put(Constants.CONTAINER_FORMAT, "ari");
        manifestProperties.put(Constants.IS_PUBLIC, "true");
        
        glanceID = uploadFileToGlance(manifestLocation,manifestLocation,manifestProperties);
        return glanceID;
    }
    
    public String uploadImage(String imageLocation, String manifestLocation, Map<String, String> imageProperties) {
        System.out.println("PSDebug Image location is:" + imageLocation);
        String glanceID;
        glanceID = uploadFileToGlance(imageLocation,manifestLocation,imageProperties);
        return glanceID;
    }
    
    
    
    private HttpPost setHeaders(HttpPost postRequest, Map<String, String> fileProperties) {
        
        postRequest.setHeader("x-image-meta-container_format", fileProperties.get(Constants.CONTAINER_FORMAT));
        postRequest.setHeader("x-image-meta-is_public", fileProperties.get(Constants.IS_PUBLIC));
        postRequest.setHeader("x-image-meta-disk_format", fileProperties.get(Constants.DISK_FORMAT));
        postRequest.setHeader("x-image-meta-name", fileProperties.get(Constants.NAME));
        postRequest.setHeader("x-image-meta-id", fileProperties.get(Constants.IMAGE_ID));
        if(fileProperties.containsKey(Constants.KERNEL_ID) && fileProperties.containsKey(Constants.INITRD_ID)) {
            postRequest.setHeader("x-image-meta-property-kernel_id", fileProperties.get(Constants.KERNEL_ID));
            postRequest.setHeader("x-image-meta-property-ramdisk_id",fileProperties.get(Constants.INITRD_ID));            
        }
        postRequest.setHeader("Content-Type", "application/octet-stream");
        
        return postRequest;
    }
    
    private String uploadFileToGlance(String fileLocation, String manifestLocation, Map<String, String> fileProperties) {
        String id = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost("http://" + glanceIP + ":9292/v1/images");
            postRequest = setHeaders(postRequest, fileProperties);
            String authToken = getAuthToken();
            postRequest.setHeader("X-Auth-Token", authToken);
            InputStream is = new FileInputStream(new File(fileLocation));
                        
            HttpEntity input = new InputStreamEntity(is);
            postRequest.setEntity(input);
            System.out.println("PostRequest is:" + postRequest.toString().trim());
//            MultipartEntity reqEntity;            
//            reqEntity = new MultipartEntity();
            
            
            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() != 201) {
                logger.log(Level.SEVERE, null, new RuntimeException("Failed : HTTP error code : "
				+ response.getStatusLine().getStatusCode()));
            }
            BufferedReader br = new BufferedReader(
                        new InputStreamReader((response.getEntity().getContent())));
 
            String output;
            
            StringBuffer sb = new StringBuffer();
            logger.info("Output from Server ....");
            while ((output = br.readLine()) != null) {
                sb.append(output);
                logger.info(output);
            }
            System.out.println("PSDebug Output from Glance is:" + output);
            
            JSONObject obj = new JSONObject(sb.toString());
            JSONObject property = obj.getJSONObject("image");
            id = property.getString("id");
            logger.info("ID is : " + id);
            httpClient.getConnectionManager().shutdown();            
            br.close();
            //br1.close();
            is.close();
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, null, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        } catch (JSONException ex) {
            logger.log(Level.SEVERE, null, ex);
        }    
        return id;
    }   

    public boolean updateImageProperty(String imageID, String propName, String propValue) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            String url = "http://" + glanceIP + ":9292/v1/images/" + imageID;
            HttpPut httpPut = new HttpPut(url);
            String authToken = getAuthToken();
            httpPut.setHeader("X-Auth-Token", authToken);
            httpPut.setHeader("x-glance-registry-purge-props", "false");

            httpPut.addHeader(propName, propValue);
            HttpResponse response = httpClient.execute(httpPut);
            
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.log(Level.SEVERE, null, new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode()));
                return false;
            }
 
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            logger.info("Put request .....");
 
            String output;
            logger.info("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                logger.info(output);
            }
 
            httpClient.getConnectionManager().shutdown();
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        return true;    
    }
    
    private String getAuthToken() {
        String authToken = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost("http://" + glanceIP + ":5000/v2.0/tokens");
            
            String body = "{\"auth\": {\"tenantName\": \"" + tenantName + "\", \"passwordCredentials\": {\"username\": \"" + userName + "\", \"password\": \"" + password + "\"}}}";
            HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
            
            postRequest.setEntity(entity);
            postRequest.setHeader("Content-Type", "application/json");
            postRequest.setHeader("Accept", "application/json");
            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.log(Level.SEVERE, null, new RuntimeException("Failed : HTTP error code : "
				+ response.getStatusLine().getStatusCode()));
            }
            BufferedReader br = new BufferedReader(
                        new InputStreamReader((response.getEntity().getContent())));
 
            String output;
            StringBuffer sb = new StringBuffer();
            logger.info("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                sb.append(output);
                //System.out.println(output);
            }
            
            JSONObject obj = new JSONObject(sb.toString());
            JSONObject property = obj.getJSONObject("access").getJSONObject("token");
            authToken = property.getString("id");
            httpClient.getConnectionManager().shutdown();            
            br.close();
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, null, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        } catch (JSONException ex) {
            logger.log(Level.SEVERE, null, ex);
        }       
        return authToken;
    }
    
}
