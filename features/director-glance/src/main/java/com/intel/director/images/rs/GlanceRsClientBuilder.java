/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.rs;

import com.intel.dcsg.cpg.configuration.Configuration;
import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.ClientBuilder;

/**
 *
 * @author GS-0681
 */
public class GlanceRsClientBuilder {

    public static GlanceRsClient build(Configuration configuration) throws GlanceException {
        try {
            if (configuration == null || configuration.get("glance.endpoint.url") == null) {
                throw new GlanceException("No configuration provided or glance.endpoint.url is not configured");
            }
            URL url = new URL(configuration.get("glance.endpoint.url")); // example: "http://localhost:8080/";
            Client client = ClientBuilder.newBuilder().build();
            WebTarget target = client.target(url.toExternalForm());
            return new GlanceRsClient(target, client);
        } catch (MalformedURLException ex) {
            throw new GlanceException("Cannot construct glance rest client", ex);
        }
    }

}
