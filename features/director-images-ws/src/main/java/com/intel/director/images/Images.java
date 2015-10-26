/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import com.intel.director.api.ImageListResponse;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.api.ImageStoreUploadRequest;
import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPoliciesResponse;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.PolicyToMountedImageRequest;
import com.intel.director.api.PolicyToMountedImageResponse;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageService;
import com.intel.director.service.LookupService;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.director.service.impl.LookupServiceImpl;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.features.director.kms.KeyContainer;
import com.intel.mtwilson.director.features.director.kms.KmsUtil;
import com.intel.mtwilson.launcher.ws.ext.V2;

/**
 * Images related APIs
 * 
 * @author Siddharth
 * 
 */
@V2
@Path("/images")
public class Images {

	ImageService imageService = new ImageServiceImpl();

	LookupService lookupService = new LookupServiceImpl();

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(Images.class);

	@Path("/test/enc")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		String ret = "ERROR";
		try {
			KmsUtil kmsUtil = new KmsUtil();
			KeyContainer createKey = kmsUtil.createKey();
			log.info("******* URL :: " + createKey.url.toString());
			
			
			
			//fetch
			String keyFromKMS = kmsUtil.getKeyFromKMS(TdaasUtil.getKeyIdFromUrl(createKey.url.toString()));
			log.info("******** Key from KMS : "+ keyFromKMS);

		} catch (Exception e) {
			log.error("******************************* Error ::: ", e);
			ret = e.getMessage();
		}
		return ret;
	}

	/**
	 * Method invoked while uploading image from the console to the TD
	 * 
	 * @param request
	 * @param image_format
	 *            Format of the image being deployed
	 * @param image_deployments
	 *            Deployment type of the image
	 * @return TrustDirectorImageUploadResponse in response
	 * @throws Exception
	 */
	@Path("/uploads/content/upload")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public TrustDirectorImageUploadResponse uploadImageToTrustDirector(

	@Context HttpServletRequest request,
			@QueryParam("image_format") String image_format,
			@QueryParam("image_deployments") String image_deployments)
			throws Exception {
		log.info("Inside upload image to TDAAS web service");
		if (!ServletFileUpload.isMultipartContent(request)) {
			TrustDirectorImageUploadResponse directorImageUploadResponse = new TrustDirectorImageUploadResponse();
			directorImageUploadResponse.status = "Error";
			return directorImageUploadResponse;
		}

		imageService = new ImageServiceImpl();
		long lStartTime = new Date().getTime();
		TrustDirectorImageUploadResponse uploadImageToTrustDirector = imageService
				.uploadImageToTrustDirectorSingle(image_deployments,
						image_format, request);
		log.info("Inside upload image to TDAAS web service. completed upload : "
				+ uploadImageToTrustDirector.getLocation());

		long lEndTime = new Date().getTime();

		long difference = lEndTime - lStartTime;

		log.info("Time taken to upload image to TD: " + difference);

		return uploadImageToTrustDirector;
	}

	/**
	 * Returns list of images in TD depending on the image deployment type
	 * supplied
	 * 
	 * @param deployment_type
	 *            deployment type for filtering the images returned
	 * @return ImageListResponse containing list of images
	 * @throws DirectorException
	 * @throws DbException
	 */
	@Path("/imagesList/{image_deployment: [a-zA-Z_-]+}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ImageListResponse getImages(
			@PathParam("image_deployment") String deployment_type)
			throws DirectorException, DbException {
		SearchImagesRequest searchImagesRequest = new SearchImagesRequest();
		searchImagesRequest.deploymentType = deployment_type;
		imageService = new ImageServiceImpl();
		SearchImagesResponse searchImagesResponse = imageService
				.searchImages(searchImagesRequest);
		return imageService.getImages(searchImagesResponse.images,
				deployment_type);

	}

	/**
	 * Method to mount the image
	 * 
	 * @param imageId
	 *            Image id of the image to be mounted
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @return MountImageResponse containing the details of the mount
	 * @throws DirectorException
	 */
	@Path("/{imageId: [0-9a-zA-Z_-]+}/mount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public MountImageResponse mountImage(@PathParam("imageId") String imageId,
			@Context HttpServletRequest httpServletRequest,
			@Context HttpServletResponse httpServletResponse) {
		log.info("inside mounting image in web service");
		String user = getLoginUsername(httpServletRequest);
		log.info("User mounting image : " + user);

		MountImageResponse mountImageResponse = new MountImageResponse();
		try {
			mountImageResponse = imageService.mountImage(
					imageId, user);
		} catch (DirectorException e) {
			// TODO Auto-generated catch block
			log.error("Error while Mounting the Image");
			mountImageResponse.status = Constants.ERROR;
			mountImageResponse.details = e.getMessage();
			return mountImageResponse;
		}
		mountImageResponse.status = "SUCCESS";
		return mountImageResponse;
	}

	/**
	 * Method to unmount the mounted image
	 * 
	 * @param imageId
	 *            Id of the image to be un-mounted
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @return UnmountImageResponse containing the details of the unmount
	 * @throws DirectorException
	 */
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

	/**
	 * Update the policy draft by applying the patch
	 * 
	 * @param imageId
	 *            Id of image whose draft is to be edited
	 * @param trustPolicyDraftEditRequest
	 * @return Updated policy
	 * @throws DirectorException
	 */

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
		log.debug("Updated policy draft : " + trustPolicyForImage);
		if (trustPolicyForImage == null) {
			throw new DirectorException(
					"Error with fetching policy for image : " + imageId);
		}

		return trustPolicyForImage;
	}

	/**
	 * Method called by the tree on Wizard 2/2 screen to find the files in the
	 * mounted image
	 * 
	 * @param imageId
	 *            Id of the image which is mounted and whose files are being
	 *            browsed
	 * @param searchFilesInImageRequest
	 * @return returns HTML representation of the tree and the patch in some
	 *         cases
	 * @throws DirectorException
	 */
	@Path("/browse/{imageId: [0-9a-zA-Z_-]+}/search")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public SearchFilesInImageResponse searchFilesInImage(
			@PathParam("imageId") String imageId,
			SearchFilesInImageRequest searchFilesInImageRequest)
			 {
		imageService = new ImageServiceImpl();
		searchFilesInImageRequest.id = imageId;
		SearchFilesInImageResponse filesInImageResponse = new SearchFilesInImageResponse();
		try {
			filesInImageResponse = imageService
					.searchFilesInImage(searchFilesInImageRequest);
		} catch (DirectorException e) {
			// TODO Auto-generated catch block
			log.error("Error while searching for files in image : "+imageId, e);
			try {
				imageService.unMountImage(imageId, null);
			} catch (DirectorException e1) {
				// TODO Auto-generated catch block
				log.error("Error while unmounting image  : "+imageId, e);
			}
		}
		String join = StringUtils.join(filesInImageResponse.files, "");
		filesInImageResponse.treeContent = join;

		// return join;
		return filesInImageResponse;
	}

	@Path("/uploads")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public ImageStoreResponse uploadImageToImageStore(
			ImageStoreUploadRequest imageStoreUploadRequest)
			throws DirectorException {

		return imageService.uploadImageToImageStore(imageStoreUploadRequest);
	}

	/**
	 * Adds the policy to the mounted image. Used in case of BM only
	 * 
	 * @param policyToMountedImageRequest
	 * @return PolicyToMountedImageResponse
	 * @throws DirectorException
	 */
	@Path("/pushpolicy")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public PolicyToMountedImageResponse pushToMountedImage(
			PolicyToMountedImageRequest policyToMountedImageRequest)
			throws DirectorException {
		// System.out.println(policyToMountedImageRequest.getHost_Id());
		return imageService
				.pushPolicyToMountedImage(policyToMountedImageRequest);
	}

	/**
	 * Lookup method that return the deployment types
	 * 
	 * @return list of deployment types
	 */
	@Path("/image-deployments")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ListImageDeploymentsResponse getImageDeployments() {
		return lookupService.getImageDeployments();
	}

	/**
	 * Lookup method to fetch the image formats
	 * 
	 * @return list of image formats
	 */
	@Path("/image-formats")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ListImageFormatsResponse getImageFormats() {
		return lookupService.getImageFormats();
	}

	/**
	 * lookup method to fetch the launch policies
	 * 
	 * @return launch policy list
	 */
	@Path("/image-launch-policies")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ListImageLaunchPoliciesResponse getImageLaunchPolicies() {

		return lookupService.getImageLaunchPolicies();
	}

	/**
	 * Creates an initial draft of policy
	 * 
	 * @param image_id
	 *            Image for which the draft is created
	 * @return ListImageLaunchPoliciesResponse
	 * @throws DirectorException
	 */
	@Path("/{image_id: [0-9a-zA-Z_-]+}/trustpolicymetadata")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ListImageLaunchPoliciesResponse getImageLaunchPolicies(
			@PathParam("image_id") String image_id) throws DirectorException {
		ListImageLaunchPoliciesResponse trustpolicymetadata = lookupService
				.getImageLaunchPolicies();
		trustpolicymetadata.display_name = imageService
				.getDisplayNameForImage(image_id);
		return trustpolicymetadata;
	}

	/**
	 * fetch the draft details for an image
	 * 
	 * @param draftid
	 *            the id of the draft
	 * @return CreateTrustPolicyMetaDataRequest
	 * @throws DirectorException
	 */
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

	/**
	 * Call to convert the draft into a policy
	 * 
	 * @param image_id
	 *            id of the image whose policy is being created
	 * @return
	 */
	@Path("/{image_id: [0-9a-zA-Z_-]+}/createpolicy")
	@Consumes(MediaType.APPLICATION_JSON)
	@POST
	public String createTrustPolicy(@PathParam("image_id") String image_id)   {
		try {
			return imageService.createTrustPolicy(image_id);
		} catch (DirectorException | JAXBException de) {
			log.error("Error creating policy from dtaft for image : "
					+ image_id, de);
			return "ERROR";
		}
	}

	/**
	 * List configured image stores
	 * 
	 * @return
	 * @throws DirectorException
	 */
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
	public CreateTrustPolicyMetaDataResponse createTrustPolicyMetaData(CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest)
	{

		CreateTrustPolicyMetaDataResponse createTrustPolicyMetadataResponse = new CreateTrustPolicyMetaDataResponse();
		try {
			createTrustPolicyMetadataResponse = imageService
					.saveTrustPolicyMetaData(createTrustPolicyMetaDataRequest);
		} catch (DirectorException e) {
			createTrustPolicyMetadataResponse.setStatus(Constants.ERROR);
			createTrustPolicyMetadataResponse.setDetails(e.getMessage());
			return createTrustPolicyMetadataResponse;
		}

		return createTrustPolicyMetadataResponse;
	}

	@Path("/{trustPolicyId: [0-9a-zA-Z_-]+}/download")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response downloadPolicy(
			@PathParam("trustPolicyId") String trustPolicyId) throws Exception {

		TrustPolicy policy = imageService
				.getTrustPolicyByTrustId(trustPolicyId);

		ResponseBuilder response = Response.ok(policy.getTrust_policy());
		response.header("Content-Disposition", "attachment; filename=policy_"
				+ policy.getImgAttributes().getName() + ".xml");

		return response.build();
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
	
	@Path("/{imageId: [0-9a-zA-Z_-]+}/downloadPolicy")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response downloadPolicyForImageId(
			@PathParam("imageId") String imageId) throws Exception {

		TrustPolicy policy = imageService.getTrustPolicyByImageId(imageId);
		ResponseBuilder response = Response.ok(policy.getTrust_policy());
		response.header("Content-Disposition", "attachment; filename=policy_"
				+ policy.getImgAttributes().getName() + ".xml");

		return response.build();
	}
	
	@Path("/{imageId: [0-9a-zA-Z_-]+}/downloadImage")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response downloadImage(
			@PathParam("imageId") String imageId ,
			@QueryParam("modified") boolean isModified) throws Exception {
		
		String pathname = imageService.getFilepathForImage(imageId,isModified);
		File imagefile = new File(pathname);
		ResponseBuilder response = Response.ok(imagefile);
		response.header("Content-Disposition", "attachment; filename="
				+ pathname.substring(pathname.lastIndexOf(File.separator) + 1));
		return response.build();
	}

}
