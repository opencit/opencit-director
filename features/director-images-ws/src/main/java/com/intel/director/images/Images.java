/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import com.intel.director.api.ImageStoreUploadRequest;
import com.intel.director.api.ImageStoreUploadResponse;
import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPoliciesResponse;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.TrustDirectorImageUploadRequest;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.images.exception.ImageMountException;
import com.intel.director.service.ImageService;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author GS-0681
 */
@Component
@Path("/images")
public class Images {

    ImageService imageService;
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Path("/{imageId: [0-9a-zA-Z_-]+}/mount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public MountImageResponse mountImage(@PathParam("imageId") String imageId, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) throws ImageMountException {
        MountImageResponse mountImageResponse = null;
        String user = getLoginUsername(httpServletRequest);
        mountImageResponse = imageService.mountImage(imageId, user);
        return mountImageResponse;
    }

    @Path("/{imageId: [0-9a-zA-Z_-]+}/unmount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public UnmountImageResponse unMountImage(@PathParam("imageId") String imageId, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) throws ImageMountException {
        UnmountImageResponse unmountImageResponse = null;
        String user = getLoginUsername(httpServletRequest);
        unmountImageResponse = imageService.unMountImage(imageId, user);
        return unmountImageResponse;
    }

    @Path("/uploads")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TrustDirectorImageUploadResponse uploadImageMetaDataToTrustDirector(@FormParam("") TrustDirectorImageUploadRequest directorImageUploadRequest) {
        TrustDirectorImageUploadResponse trustDirectorImageUploadResponse = null;
        imageService.uploadImageMetaDataToTrustDirector(directorImageUploadRequest);
        return trustDirectorImageUploadResponse;
    }

    @Path("/uploads/{imageId: [0-9a-zA-Z_-]+}/content")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public TrustDirectorImageUploadResponse uploadImageToTrustDirector(@PathParam("imageId") String imageId, @FormParam("image") InputStream uploadedInputStream) throws IOException {
        TrustDirectorImageUploadResponse trustDirectorImageUploadResponse = null;
        imageService.uploadImageToTrustDirector(imageId, uploadedInputStream);
        return trustDirectorImageUploadResponse;
    }

    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public SearchImagesResponse searchImages(@FormParam("") SearchImagesRequest searchImagesRequest) {
        SearchImagesResponse searchImagesResponse = null;
        searchImagesResponse = imageService.searchImages(searchImagesRequest);
        return searchImagesResponse;
    }

    @Path("/imageId: [0-9a-zA-Z_-]+}/search")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    public SearchFilesInImageResponse searchFilesInImage(@PathParam("imageId") String imageId) {
        return null;
    }

    @Path("/uploads")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ImageStoreUploadResponse searchFilesInImage(@FormParam("") ImageStoreUploadRequest imageStoreUploadRequest) {
        return null;
    }

    /**
     * Lookup methods
     */
    @Path("/image-deployments")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ListImageDeploymentsResponse getImageDeployments() {
        return null;
    }

    @Path("/image-formats")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ListImageFormatsResponse getImageFormats() {
        return null;
    }

    @Path("image-launch-policies")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ListImageLaunchPoliciesResponse getImageLaunchPolicies() {
        return null;
    }

    /**
     * Utility methods
     */
    protected String getLoginUsername(HttpServletRequest httpServletRequest) {
        String header = httpServletRequest.getHeader(AUTHORIZATION_HEADER); // Authorization: Basic am9uYXRoYW46am9uYXRoYW4=
        if (header == null || header.isEmpty()) {
            return null;
        }
        String[] schemeCredential = header.split(" "); // Basic am9uYXRoYW46am9uYXRoYW4=
        if (schemeCredential == null || schemeCredential.length < 2) {
            return null;
        }
        if (!schemeCredential[0].equalsIgnoreCase(HttpServletRequest.BASIC_AUTH)) {
            return null;
        }
        String basicUsernamePassword = new String(Base64.decodeBase64(schemeCredential[1]));
        String[] usernamePassword = basicUsernamePassword.split(":");
        if (usernamePassword == null || usernamePassword.length < 2) {
            return null;
        }

        String username = usernamePassword[0];
        String password = usernamePassword[1];
        String host = httpServletRequest.getRemoteHost();
        UsernamePasswordToken authenticationToken = new UsernamePasswordToken(username, password, false, host);
        Subject subject = SecurityUtils.getSubject();
        subject.login(authenticationToken);
        if (subject.isAuthenticated()) {
            return username;
        }
        return null;
    }

    @Autowired
    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

}
