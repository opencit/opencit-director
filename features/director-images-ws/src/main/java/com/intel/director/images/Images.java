/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;






import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.GetImageStoresResponse;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageListResponse;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.api.ImageStoreUploadRequest;
import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPoliciesResponse;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.SignTrustPolicyResponse;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageService;
import com.intel.director.service.LookupService;
import com.intel.director.service.impl.ImageActionImpl;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.director.service.impl.LookupServiceImpl;
import com.intel.mtwilson.director.db.exception.DbException;

@Path("/images")
public class Images {

	ImageService imageService = new ImageServiceImpl();

	LookupService lookupService = new LookupServiceImpl();

	private static final String AUTHORIZATION_HEADER = "Authorization";

	@Path("/uploads/content/upload")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public TrustDirectorImageUploadResponse uploadImageToTrustDirector(

			@Context HttpServletRequest request,
			@QueryParam("image_format") String image_format,
			@QueryParam("image_deployments") String image_deployments)
			throws Exception {
		if (!ServletFileUpload.isMultipartContent(request)) {
			TrustDirectorImageUploadResponse directorImageUploadResponse = new TrustDirectorImageUploadResponse();
			directorImageUploadResponse.status = "Error";
			return directorImageUploadResponse;
		}

		imageService = new ImageServiceImpl();
		long lStartTime = new Date().getTime();
		TrustDirectorImageUploadResponse uploadImageToTrustDirector = imageService
				.uploadImageToTrustDirectorSingle(image_deployments, image_format,
						request);
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
			throws DirectorException {
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
			throws DirectorException {
		String user = getLoginUsername(httpServletRequest);
		UnmountImageResponse unmountImageResponse = imageService.unMountImage(
				imageId, user);
		return unmountImageResponse;
	}

	@Path("/policydraft/{imageId: [0-9a-zA-Z_-]+}/edit")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String editPolicyDraft(@PathParam("imageId") String imageId,
			TrustPolicyDraftEditRequest trustPolicyDraftEditRequest)
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
	// @Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public SearchFilesInImageResponse searchFilesInImage(
			@PathParam("imageId") String imageId,
			SearchFilesInImageRequest searchFilesInImageRequest)
			throws DirectorException {
		imageService = new ImageServiceImpl();
		searchFilesInImageRequest.id = imageId;
		SearchFilesInImageResponse filesInImageResponse = imageService
				.searchFilesInImage(searchFilesInImageRequest);
		String join = StringUtils.join(filesInImageResponse.files, "");
		filesInImageResponse.treeContent = join;

		// return join;
		return filesInImageResponse;
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
			ImageStoreUploadRequest imageStoreUploadRequest)
			throws DirectorException {
		System.out.println(imageStoreUploadRequest.getImage_action_id());
		return imageService.uploadImageToImageStore(imageStoreUploadRequest);
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

	@Path("/trustpolicies/{trustpolicyid}/getmetadata")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public CreateTrustPolicyMetaDataRequest getPolicyMetadata(
			@PathParam("trustpolicyid") String draftid)
			throws DirectorException {
		return imageService.getPolicyMetadata(draftid);
	}

	@Path("/{image_id: [0-9a-zA-Z_-]+}/getpolicymetadataforimage")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public CreateTrustPolicyMetaDataRequest getPolicyMetadataForImage(
			@PathParam("image_id") String image_id) throws DirectorException {
		return imageService.getPolicyMetadataForImage(image_id);
	}

	@Path("/{image_id: [0-9a-zA-Z_-]+}/createpolicy")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public ImageActionObject createTrustPolicy(
			@PathParam("image_id") String image_id) throws DirectorException,
			JAXBException {

		return imageService.createTrustPolicy(image_id);
	}

	@Path("/imagesList/{image_deployment: [a-zA-Z_-]+}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ImageListResponse getImages(
			@PathParam("image_deployment") String deployment_type)
			throws DirectorException {
		return imageService.getImages(deployment_type);
	}

	@Path("/imagestores")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public GetImageStoresResponse getImageStores() throws DirectorException {
		GetImageStoresResponse imageStores = new GetImageStoresResponse();
		List<String> imageStoreNames = new ArrayList<String>();
		imageStoreNames.add("Glance");
		imageStoreNames.add("Swift");
		imageStores.setImageStoreNames(imageStoreNames);
		return imageStores;
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

	(CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest)
			throws DirectorException {

		CreateTrustPolicyMetaDataResponse createTrustPolicyMetadataResponse = imageService
				.saveTrustPolicyMetaData(createTrustPolicyMetaDataRequest);

		return createTrustPolicyMetadataResponse;
	}

	@Path("/{trustPolicyId: [0-9a-zA-Z_-]+}/download")
	@GET
	@Produces(MediaType.APPLICATION_XML)  
	public Response downloadPolicy(@PathParam("trustPolicyId") String trustPolicyId) throws Exception {
		TrustPolicy policy= imageService.getTrustPolicyByTrustId(trustPolicyId);
		ResponseBuilder response = Response.ok(policy.getTrust_policy());
		response.header( "Content-Disposition","attachment; filename=policy_"+policy.getImgAttributes().getName()+".xml");

		return response.build();
	}

	@GET
	@Path("/action/getdata")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImageActionObject> get() throws DbException {
		ImageActionImpl imageActionImpl = new ImageActionImpl();

		return imageActionImpl.getdata();

	}

	@POST
	@Path("/action/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String createImageAction(ImageActionObject imageActionObject)
			throws DbException {
		ImageActionImpl imageActionImpl = new ImageActionImpl();
		imageActionImpl.createImageAction(imageActionObject);
		return "Create Success";
	}

	@PUT
	@Path("/action/{id: [0-9a-zA-Z_-]+}/update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String updateSsh(@PathParam("id") String id,
			ImageActionObject imageActionObject) throws DbException {
		ImageActionImpl imageActionImpl = new ImageActionImpl();
		imageActionImpl.updateImageAction(id, imageActionObject);
		return "Update Success";

	}

	@Path("/{imageId: [0-9a-zA-Z_-]+}/recreatedraft")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TrustPolicyDraft createPolicyDraftFromPolicy(
			@PathParam("imageId") String imageId,
			@QueryParam("action_id") String image_action_id) throws Exception {
		return imageService.createPolicyDraftFromPolicy(imageId,
				image_action_id);

	}
}