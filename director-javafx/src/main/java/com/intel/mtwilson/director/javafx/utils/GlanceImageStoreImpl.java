/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

import static com.intel.mtwilson.configuration.ConfigurationFactory.getConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import com.intel.mtwilson.director.javafx.ui.Constants;
import com.intel.mtwilson.director.javafx.ui.ErrorMessage;
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
public class GlanceImageStoreImpl implements ImageStore {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlanceImageStoreImpl.class);
    private String glanceIP;
    private String userName;
    private String password;
    private String tenantName;

    public GlanceImageStoreImpl() throws IOException {
        glanceIP = getConfiguration().get(Constants.IMAGE_STORE_SERVER, null);
        userName = getConfiguration().get(Constants.IMAGE_STORE_USERNAME);
        password = getConfiguration().get(Constants.IMAGE_STORE_PASSWORD);
        tenantName = getConfiguration().get(Constants.TENANT_NAME);

    }

    @Override
    public String uploadTrustPolicy(String manifestLocation) throws Exception {

        String glanceID = null;
        Map<String, String> manifestProperties = new HashMap<>();
        manifestProperties.put(Constants.NAME, manifestLocation.substring((manifestLocation.lastIndexOf("/") + 1)));
        manifestProperties.put(Constants.DISK_FORMAT, "ari");
        manifestProperties.put(Constants.CONTAINER_FORMAT, "ari");
        manifestProperties.put(Constants.IS_PUBLIC, "true");

        glanceID = uploadImage(manifestLocation, manifestProperties);
        return glanceID;
    }

    @Override
    public String uploadImage(String imageLocation, Map<String, String> imageProperties) throws Exception {
        String id = null;
        log.debug("Uploading image from location: " + imageLocation);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost("http://" + glanceIP + ":9292/v1/images");
        postRequest = setHeaders(postRequest, imageProperties);
        String authToken = getAuthToken();
        postRequest.setHeader("X-Auth-Token", authToken);
        InputStream is = new FileInputStream(new File(imageLocation));

        HttpEntity input = new InputStreamEntity(is);
        postRequest.setEntity(input);

        HttpResponse response = httpClient.execute(postRequest);
        if (response.getStatusLine().getStatusCode() != 201) {
            log.error(null, new RuntimeException("Failed to upload image : HTTP error code : "
                    + response.getStatusLine().getStatusCode()));
            throw new ImageStoreException();
        }
        BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));

        String output;

        StringBuffer sb = new StringBuffer();
        while ((output = br.readLine()) != null) {
            sb.append(output);
            log.info(output);
        }

        JSONObject obj = new JSONObject(sb.toString());
        JSONObject property = obj.getJSONObject("image");
        id = property.getString("id");
        httpClient.getConnectionManager().shutdown();
        br.close();
        is.close();
        return id;
    }

    private HttpPost setHeaders(HttpPost postRequest, Map<String, String> fileProperties) {

        postRequest.setHeader("x-image-meta-container_format", fileProperties.get(Constants.CONTAINER_FORMAT));
        postRequest.setHeader("x-image-meta-is_public", fileProperties.get(Constants.IS_PUBLIC));
        postRequest.setHeader("x-image-meta-disk_format", fileProperties.get(Constants.DISK_FORMAT));
        postRequest.setHeader("x-image-meta-name", fileProperties.get(Constants.NAME));
        postRequest.setHeader("x-image-meta-id", fileProperties.get(Constants.IMAGE_ID));
        if (fileProperties.containsKey(Constants.KERNEL_ID) && fileProperties.containsKey(Constants.INITRD_ID)) {
            postRequest.setHeader("x-image-meta-property-kernel_id", fileProperties.get(Constants.KERNEL_ID));
            postRequest.setHeader("x-image-meta-property-ramdisk_id", fileProperties.get(Constants.INITRD_ID));
        }
        postRequest.setHeader("Content-Type", "application/octet-stream");

        return postRequest;
    }

    @Override
    public boolean updateImageProperty(String imageID, String propName, String propValue) throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "http://" + glanceIP + ":9292/v1/images/" + imageID;
        HttpPut httpPut = new HttpPut(url);
        String authToken = getAuthToken();
        httpPut.setHeader("X-Auth-Token", authToken);
        httpPut.setHeader("x-glance-registry-purge-props", "false");

        httpPut.addHeader(propName, propValue);
        HttpResponse response = httpClient.execute(httpPut);

        if (response.getStatusLine().getStatusCode() != 200) {
            log.error(null, new RuntimeException("Failed to upload image: HTTP error code : "
                    + response.getStatusLine().getStatusCode()));
            return false;
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));

        String output;
        while ((output = br.readLine()) != null) {
            log.trace(output);
        }

        httpClient.getConnectionManager().shutdown();

        return true;
    }

    private String getAuthToken() throws Exception {
        String authToken = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost("http://" + glanceIP + ":5000/v2.0/tokens");

        String body = "{\"auth\": {\"tenantName\": \"" + tenantName + "\", \"passwordCredentials\": {\"username\": \"" + userName + "\", \"password\": \"" + password + "\"}}}";
        HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));

        postRequest.setEntity(entity);
        postRequest.setHeader("Content-Type", "application/json");
        postRequest.setHeader("Accept", "application/json");
        HttpResponse response = httpClient.execute(postRequest);
        if (response.getStatusLine().getStatusCode() != 200) {
            log.error(null, new RuntimeException("Failed : HTTP error code : "
                    + response.getStatusLine().getStatusCode()));
        }
        BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));

        String output;
        StringBuffer sb = new StringBuffer();
        log.info("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            sb.append(output);
            //log.debug(output);
        }

        JSONObject obj = new JSONObject(sb.toString());
        JSONObject property = obj.getJSONObject("access").getJSONObject("token");
        authToken = property.getString("id");
        httpClient.getConnectionManager().shutdown();
        br.close();

        return authToken;
    }

}
