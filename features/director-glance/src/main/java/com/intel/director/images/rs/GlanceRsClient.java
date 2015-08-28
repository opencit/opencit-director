/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.rs;

import com.intel.director.api.ImageStoreRequest;
import com.intel.director.images.glance.api.GlanceResponse;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Entity;

/**
 *
 * @author GS-0681
 */
public class GlanceRsClient {

    public WebTarget webTarget;
    public Client client;

    public GlanceRsClient(WebTarget webTarget, Client client) {
        this.webTarget = webTarget;
        this.client = client;
    }

    public void uploadImage(ImageStoreRequest imageStoreRequest) {
        Response response = webTarget.path("/v1/images").request().put(Entity.json(imageStoreRequest));
        response.readEntity(GlanceResponse.class);
    }

    public GlanceResponse uploadImageMetaData(ImageStoreRequest imageStoreRequest) {
        Response response = webTarget.path("/v1/images").request().post(Entity.json(imageStoreRequest));
        GlanceResponse glanceResponse = response.readEntity(GlanceResponse.class);
        return glanceResponse;
    }

    public void getImageMetaData() {
    }

    public void deleteImage() {
    }
}
