/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.rs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.intel.director.images.glance.constants.Constants;

/**
 *
 * @author GS-0681
 */
public class GlanceRsClientBuilder {

    public static GlanceRsClient build(Map<String, String> configuration) throws GlanceException {
        try {
            if (configuration == null || configuration.get(Constants.GLANCE_API_ENDPOINT) == null) {
                throw new GlanceException("No configuration provided ");
            }
            String glanceApiEndpoint = configuration.get(Constants.GLANCE_API_ENDPOINT); 
            String glanceKeystonePublicEndpoint = configuration.get(Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT); 
            String userName = configuration.get(Constants.GLANCE_IMAGE_STORE_USERNAME);
            String password = configuration.get(Constants.GLANCE_IMAGE_STORE_PASSWORD);
            String tenantName = configuration.get(Constants.GLANCE_TENANT_NAME);
            
         
            URL url = new URL(glanceApiEndpoint); // example: "http://localhost:8080/";
            
            Client client = ClientBuilder.newBuilder().build();
            WebTarget target = client.target(url.toExternalForm());
            return new GlanceRsClient(target, client, glanceKeystonePublicEndpoint,tenantName,userName,password);
        } catch (MalformedURLException ex) {
            throw new GlanceException("Cannot construct glance rest client with given credentials", ex);
        }
    }

}
