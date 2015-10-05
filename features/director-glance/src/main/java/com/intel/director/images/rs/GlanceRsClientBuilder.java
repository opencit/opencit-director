/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.rs;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.images.glance.constants.Constants;

/**
 *
 * @author GS-0681
 */
public class GlanceRsClientBuilder {

    public static GlanceRsClient build(Configuration configuration) throws GlanceException {
        try {
            if (configuration == null || configuration.get(Constants.GLANCE_IP) == null) {
                throw new GlanceException("No configuration provided ");
            }
            String glanceIP = configuration.get(Constants.GLANCE_IP, ""); 
            String glancePort = configuration.get(Constants.GLANCE_PORT, ""); 
            String userName = configuration.get(Constants.GLANCE_IMAGE_STORE_USERNAME);
            String password = configuration.get(Constants.GLANCE_IMAGE_STORE_PASSWORD);
            String tenantName = configuration.get(Constants.GLANCE_TENANT_NAME);
            
         
            URL url = new URL("http://" + glanceIP + ":"+glancePort); // example: "http://localhost:8080/";
            
            Client client = ClientBuilder.newBuilder().build();
            WebTarget target = client.target(url.toExternalForm());
            return new GlanceRsClient(target, client,glanceIP,tenantName,userName,password);
        } catch (MalformedURLException ex) {
            throw new GlanceException("Cannot construct glance rest client", ex);
        }
    }

}
