/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;

/**
 *
 * @author Siddharth
 */
@Component
@Path("/dashboard")
public class Dashboard {

    /**
     * Method to get the trust polices edited by the logged in user
     */
    @GET
    @Path("/trustpolicies/currentuser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void getRecentEditedTrustPolicies() {
    }

    /**
     * Method to get the trust policies edited by all users
     */
    @GET
    @Path("/trustpolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void getAllRecentEditedTrustPolicies() {
    }

    /**
     * Method to retrieve the images uploaded to TD which do not have any
     * policies
     */
    @GET
    @Path("/newImages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void getRecentTrustDirectorImages() {
    }

    /**
     * Images who have polices created but not yet uploaded to any store
     */
    @GET
    @Path("/imagesToUpload")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void getImagesReadyToUpload() {
    }

    /**
     * Method to retrieve the images that were recently uploaded to image store
     */
    @GET
    @Path("/uploadedImages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void getRecentUploadedImages() {
    }

    /**
     * Method to retrieve the update on the progress of the images that are
     * uploaded to the image store
     */
    @GET
    @Path("/uploadProgress")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void getUploadProgress() {
    }
}
