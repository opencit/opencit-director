/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.director.api.CreateTrustPolicyRequest;
import com.intel.director.api.CreateTrustPolicyResponse;
import com.intel.director.api.ImageStoreRequest;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPoliciesResponse;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.SignTrustPolicyResponse;
import com.intel.director.api.TrustDirectorImageUploadRequest;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.images.exception.ImageMountException;
import com.intel.director.service.ImageService;
import com.intel.director.service.LookupService;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.director.service.impl.LookupServiceImpl;
import com.intel.mtwilson.director.db.exception.DbException;

/**
 * 
 * @author GS-0681
 */
@Component
@Path("/images")
public class Images extends Application {


	ImageService imageService=new ImageServiceImpl();


	LookupService lookupService=new LookupServiceImpl();

	private static final String AUTHORIZATION_HEADER = "Authorization";

	@GET
	@Path("/abc11")
	public Response getMsg(@PathParam("param") String msg) {
		// /// log.debug("##########SSSSUCCCESSSSSSSSSSSSSSSYY");
		String output = "Jersey say : " + " Succes s" + msg;

		Map<String, Object> response = new HashMap<String, Object>();
		// / Image img = new Image();
		// / img.setImageName("testImage");

		// /return img;
		return Response.status(201).entity("Called successfuly").build();

	}

	@Path("/uploadtotd")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadImageMetaDataToTrustDirector(
			TrustDirectorImageUploadRequest directorImageUploadRequest)
			throws DbException, JsonProcessingException, URISyntaxException {
		imageService = new ImageServiceImpl();
		TrustDirectorImageUploadResponse trustDirectorImageUploadResponse = imageService
				.uploadImageMetaDataToTrustDirector(directorImageUploadRequest);
		trustDirectorImageUploadResponse.imageUploadUrl = "/v1/images/"
				+ trustDirectorImageUploadResponse.id + "/content";
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper
				.writeValueAsString(trustDirectorImageUploadResponse);
		return Response.created(
				new URI(trustDirectorImageUploadResponse.imageUploadUrl))
				.build();
	}

	@Path("/uploads/{imageId: [0-9a-zA-Z_-]+}/content")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public TrustDirectorImageUploadResponse uploadImageToTrustDirector(
			@PathParam("imageId") String imageId,
			@Context HttpServletRequest request) throws Exception {
		if (!ServletFileUpload.isMultipartContent(request)) {
			TrustDirectorImageUploadResponse directorImageUploadResponse = new TrustDirectorImageUploadResponse();
			directorImageUploadResponse.status = "Error";
			return directorImageUploadResponse;
		}

		imageService = new ImageServiceImpl();
		long lStartTime = new Date().getTime();
		TrustDirectorImageUploadResponse uploadImageToTrustDirector = imageService
				.uploadImageToTrustDirector(imageId, request);
		long lEndTime = new Date().getTime();

		long difference = lEndTime - lStartTime;

		System.out.println("Elapsed milliseconds: " + difference);

		return uploadImageToTrustDirector;
	}

	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public SearchImagesResponse searchImages(
			@QueryParam("deploymentType") String deploymentType)
			throws DbException {
		SearchImagesRequest searchImagesRequest = new SearchImagesRequest();
		searchImagesRequest.deploymentType = deploymentType;
		imageService = new ImageServiceImpl();
		SearchImagesResponse searchImagesResponse = imageService
				.searchImages(searchImagesRequest);
		return searchImagesResponse;
	}

	@Path("/{imageId: [0-9a-zA-Z_-]+}/mount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public MountImageResponse mountImage(@PathParam("imageId") String imageId,
			@Context HttpServletRequest httpServletRequest,
			@Context HttpServletResponse httpServletResponse)
			throws ImageMountException, DbException {
		String user = getLoginUsername(httpServletRequest);
		MountImageResponse mountImageResponse = imageService.mountImage(
				imageId, user);
		return mountImageResponse;
	}

	@Path("/{imageId: [0-9a-zA-Z_-]+}/unmount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public UnmountImageResponse unMountImage(
			@PathParam("imageId") String imageId,
			@Context HttpServletRequest httpServletRequest,
			@Context HttpServletResponse httpServletResponse)
			throws ImageMountException {
		String user = getLoginUsername(httpServletRequest);
		UnmountImageResponse unmountImageResponse = imageService.unMountImage(
				imageId, user);
		return unmountImageResponse;
	}

	@Path("/trustpolicies")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public CreateTrustPolicyResponse createTrustPolicy(
			CreateTrustPolicyRequest createTrustPolicyRequest) {
		return null;
	}

	@Path("/policydraft/{imageId: [0-9a-zA-Z_-]+}/edit")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String editPolicyDraft(@PathParam("imageId") String imageId, TrustPolicyDraftEditRequest trustPolicyDraftEditRequest )
			throws DirectorException {
		imageService = new ImageServiceImpl();
		trustPolicyDraftEditRequest.imageId = imageId;
		
		imageService.editTrustPolicyDraft(trustPolicyDraftEditRequest);
		String trustPolicyForImage = imageService
				.getTrustPolicyForImage(imageId);
		if (trustPolicyForImage == null) {
			throw new DirectorException(
					"Error with fetching policy for image : " + imageId);
		}	
		
		
		return trustPolicyForImage;
	}

	@Path("/browse/{imageId: [0-9a-zA-Z_-]+}/search")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String searchFilesInImage(@PathParam("imageId") String imageId,
			SearchFilesInImageRequest searchFilesInImageRequest) {
		imageService = new ImageServiceImpl();
		SearchFilesInImageResponse filesInImageResponse = imageService
				.searchFilesInImage(searchFilesInImageRequest);
		String join = StringUtils.join(filesInImageResponse.files, "");
		return join;
	}

	@Path("/trustpolicies/{trustpolicy_id: [0-9a-zA-Z_-]+}/sign")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public SignTrustPolicyResponse signTrustPolicy(
			@PathParam("trustpolicy_id") String trustpolicyId) {
		return null;
	}

	@Path("/uploads")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public ImageStoreResponse uploadImageToImageStore(
			ImageStoreRequest imageStoreRequest) {
		return null;
	}

	/**
	 * Lookup methods
	 * 
	 * @return
	 */
	@Path("/image-deployments")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ListImageDeploymentsResponse getImageDeployments() {
		return lookupService.getImageDeployments();
	}

	@Path("/image-formats")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ListImageFormatsResponse getImageFormats() {
		return lookupService.getImageFormats();
	}

	@Path("/image-launch-policies")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ListImageLaunchPoliciesResponse getImageLaunchPolicies() {
		return lookupService.getImageLaunchPolicies();
	}

	/**
	 * Utility methods
	 * 
	 * @param httpServletRequest
	 * @return
	 */
	protected String getLoginUsername(HttpServletRequest httpServletRequest) {
		String header = httpServletRequest.getHeader(AUTHORIZATION_HEADER); // Authorization:
																			// Basic
																			// am9uYXRoYW46am9uYXRoYW4=
		if (header == null || header.isEmpty()) {
			return null;
		}
		String[] schemeCredential = header.split(" "); // Basic
														// am9uYXRoYW46am9uYXRoYW4=
		if (schemeCredential == null || schemeCredential.length < 2) {
			return null;
		}
		if (!schemeCredential[0]
				.equalsIgnoreCase(HttpServletRequest.BASIC_AUTH)) {
			return null;
		}
		String basicUsernamePassword = new String(
				Base64.decodeBase64(schemeCredential[1]));
		String[] usernamePassword = basicUsernamePassword.split(":");
		if (usernamePassword == null || usernamePassword.length < 2) {
			return null;
		}

		String username = usernamePassword[0];
		String password = usernamePassword[1];
		String host = httpServletRequest.getRemoteHost();
		UsernamePasswordToken authenticationToken = new UsernamePasswordToken(
				username, password, false, host);
		Subject subject = SecurityUtils.getSubject();
		subject.login(authenticationToken);
		if (subject.isAuthenticated()) {
			return username;
		}
		return null;
	}
	

	  @Path("/trustpoliciesmetadata")
	    @Consumes(MediaType.APPLICATION_JSON)
	    @Produces(MediaType.APPLICATION_JSON)
	    @POST
	    public CreateTrustPolicyMetaDataResponse createTrustPolicyMetaData

	(CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest) {
	    	CreateTrustPolicyMetaDataResponse createPolicyMetadataResponse=new 

	CreateTrustPolicyMetaDataResponse();
	    	createPolicyMetadataResponse.setId("123");
	    	createPolicyMetadataResponse.setTrustPolicy("<trustpolicy></trustpolicy>");
	        return createPolicyMetadataResponse;
	    }

}
