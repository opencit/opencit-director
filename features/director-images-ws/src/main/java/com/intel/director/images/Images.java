/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import java.io.File;
import java.io.InputStream;
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

import org.apache.commons.lang.StringUtils;

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
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.shiro.ShiroUtil;

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

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(Images.class);

	/**
	 * API for uploading image metadata like image format,
	 * deployment type(VM, BareMetal, Docker), image file name,
	 * image size, etc.
	 * @param image_format
	 * @param image_deployments
	 * @param fileName
	 * @param fileSize
	 * @return TrustDirectorImageUploadResponse - contains newly created
	 * 				image metadata along with image_id
	 * @throws Exception
	 */
	@Path("/uploads/content/uploadMetadata")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public TrustDirectorImageUploadResponse createUploadImageMetadata(
			@QueryParam("image_format") String image_format,
			@QueryParam("image_deployments") String image_deployments,
			@QueryParam("fileName") String fileName,
			@QueryParam("fileSize") String fileSize)
			throws Exception {

		imageService = new ImageServiceImpl();
		TrustDirectorImageUploadResponse uploadImageToTrustDirector = imageService
				.createUploadImageMetadataImpl(image_deployments,
						image_format, fileName, fileSize);
		log.info("Successfully uploaded image to location: "
				+ uploadImageToTrustDirector.getLocation());
	
		return uploadImageToTrustDirector;
	}
	
	
	/**
	 * API for uploading image data for the given image id.
	 * Before Uploading image it is divided in chunks
	 * and sent one by one. Once the chunk is received
	 * it is then saved to disk.
	 * @param image_id - id received as response of 
	 * 						../uploadMetadata request
	 * @param filInputStream - image data sent as chunk
	 * @return TrustDirectorImageUploadResponse - 
	 * 			updated image upload metadata in response
	 * @throws Exception
	 */
	@Path("/uploads/content/upload/{image_id: [0-9a-zA-Z_-]+}")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public TrustDirectorImageUploadResponse uploadImageToTrustDirector(
			@PathParam("image_id") String image_id,
			InputStream filInputStream)
			throws Exception {
		log.info("Uploading image to TDaaS");

		imageService = new ImageServiceImpl();
		long lStartTime = new Date().getTime();
		TrustDirectorImageUploadResponse uploadImageToTrustDirector = imageService
				.uploadImageToTrustDirectorSingle(image_id, filInputStream);
		log.info("Successfully uploaded image to location: "
				+ uploadImageToTrustDirector.getLocation());

		long lEndTime = new Date().getTime();

		long difference = lEndTime - lStartTime;

		log.info("Time taken to upload image to TD: " + difference);

		return uploadImageToTrustDirector;
	}

	/**
	 * Returns list of images in TD depending on the image deployment type
	 * supplied. This call is made so that grids on the UI for VM and Hosts can
	 * be populated. Each image has an image deployment type : BareMetal/VM.
	 * 
	 * This method not just gets the list of images based on the deployment
	 * type, it also generates the actions and the images that are shown in the
	 * grid. For example in the VM grid, we have the download policy, upload to
	 * store icons. Those are build inside the ImageService.getImages
	 * implementation
	 * 
	 * @param image_deployment
	 *            : expected values BareMetal, VM
	 * @return ImageListResponse containing list of images.
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
		SearchImagesResponse searchImagesResponse = imageService
				.searchImages(searchImagesRequest);
		return imageService.getImages(searchImagesResponse.images,
				deployment_type);

	}

	/**
	 * Method to mount the image. This call is invoked in all types of
	 * deployment types as we treat SSH hosts as images too. When a host is
	 * added and entry is made in the MW_IMAGE table, but with image_format as
	 * null. For images, the image format is qcow2
	 * 
	 * Mount path for VM and BM image is /mnt/director/<db_image_uuid> . There
	 * would be just one mountpoint per image. If someone tries to mount same
	 * image again, it will throw exception. Once image is mounted,
	 * mw_image->mounted_by_user_id database field will be updated.
	 * 
	 * This method returns a response in all scenarios, even in case of errors
	 * while mounting the image. In the case when the user attempts to mount an
	 * image which is already in use by another user, and error message: Unable
	 * to mount image. Image is already in use by user: <user_name> is thrown
	 * and sent back in the details attribute of the response object and status
	 * as ERROR.
	 * 
	 * In case the case of the user who has mounted the image, because of
	 * inactivity, the session timed out; and the user logs back in. The image
	 * will not be re-mounted.
	 * 
	 * In successful mount scenario, status is sent back as SUCCESS
	 * 
	 * 
	 * 
	 * Sample response: { “id”: “1eebe380-1a36-11e5-9472-0002a5d5c51b”, “name”:
	 * “cirros-x86.img” “image_deployments”: “VM,Bare_Metal” “image_format”:
	 * “qcow2” “mounted”: “true”, "status":"SUCCESS", details:"" }
	 * 
	 * @param imageId
	 *            UUID: Image id of the image in MW_IMAGE to be mounted
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @return MountImageResponse containing the details of the mount process.
	 */
	@Path("/{imageId: [0-9a-zA-Z_-]+}/mount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public MountImageResponse mountImage(@PathParam("imageId") String imageId,
			@Context HttpServletRequest httpServletRequest,
			@Context HttpServletResponse httpServletResponse) {
		log.info("inside mounting image in web service");
		String user = getLoginUsername();
		log.info("User mounting image : " + user);

		MountImageResponse mountImageResponse = new MountImageResponse();
		try {
			mountImageResponse = imageService.mountImage(imageId, user);
		} catch (DirectorException e) {
			log.error("Error while Mounting the Image");
			mountImageResponse.status = Constants.ERROR;
			mountImageResponse.details = e.getMessage();
			return mountImageResponse;
		}
		mountImageResponse.status = "SUCCESS";
		return mountImageResponse;
	}

	/**
	 * Method to unmount the mounted image.
	 * 
	 * API will first check whether image is mounted by same user or not using
	 * mw_image -> mounted_by_user_id field. If not then, it will throw
	 * exception. Otherwise API should figure out mount point based on image Id
	 * and unmount the image. The default mount path is /mnt/director/UUID
	 * 
	 * As part of the unmount process, the MW_IMAGE.mounted_by_user_id field is
	 * set to NULL again. the unmount process in the service, throws an
	 * exception wrapped in DirectorException in case of any error.
	 * 
	 * 
	 * Sample response: { “id”: “1eebe380-1a36-11e5-9472-0002a5d5c51b”, “name”:
	 * “cirros-x86.img” “image_deployments”: “VM,Bare_Metal” “image_format”:
	 * “qcow2” “mounted”: “false” }
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
		String user = getLoginUsername();
		UnmountImageResponse unmountImageResponse = imageService.unMountImage(
				imageId, user);
		return unmountImageResponse;
	}

	/**
	 * Update the policy draft by applying the patch. Whenever an user selects
	 * directories on the UI, a patch of the changes made to the selections is
	 * sent to the server every 10 mins. this method does the job of merging the
	 * patch/delta into the trust policy draft that is stored in the database.
	 * 
	 * A sample patch looks like this: <patch> <add sel="<node selector>"><File
	 * path="<file path>"></add> <remove sel="<node selector>"></remove>
	 * </patch>
	 * 
	 * We use https://github.com/dnault/xml-patch library to apply patches.
	 * 
	 * the method returns the patched trust policy as a response to successful
	 * patch application In case of error, a DirectorException is thrown. It is
	 * caught in the failure section of the ajax call and shown as a pop up
	 * message.
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
	 * This method is invoked as an ajax call from the tree component of the
	 * policy wizard. All the directory selections, viewing of child nodes,
	 * application of regex is handled by this method.
	 * 
	 * the SearchFilesInImageRequest object contains all the flags to support
	 * above functionalities. For example, in case of regex application, the
	 * dir, include, exclude, include_recursive attributes are utilized.
	 * 
	 * In case of regex reset, dir, reset_regex is utilized.
	 * 
	 * This method expects a policy draft to be existing when the user comes to
	 * this screen. We create a default empty policy draft when a user starts
	 * creating one. We just keep on modifying the Whitelist tag of the xml.
	 * 
	 * The UI library for the tree also sends an "init" parameter when its
	 * loaded the first time. This indicates that the tree needs to pick up he
	 * current selections that might be made by the users and pre-check the tree
	 * items. The init method reads the existing policy draft and creates a list
	 * of files already selected earlier.
	 * 
	 * Depending on the user actions and the corresponding attributed in the
	 * SearchFilesInImageRequest object, the DirectoryAndFileUtil class methods
	 * are invoked to find the files and dirs inside the directory of interest.
	 * 
	 * In cases of regex and "select all" operation where user clicks on the
	 * checkbox next to a directory in order to select all the contents, this
	 * method created a patch as a list of strings and sends it in the
	 * "patchXml" attribute. Once the UI receives it, it adds it to the current
	 * selections on the UI and then sends back the consolidated patch to the
	 * server
	 * 
	 * 
	 * @param imageId
	 *            Id of the image which is mounted and whose files are being
	 *            browsed
	 * @param searchFilesInImageRequest
	 *            Request containing the options selected by the user on the
	 *            tree. It contains flags like include/exclude flags for regex
	 *            filter, select all flag, init flag for first time load
	 * @return returns HTML representation of the tree and the patch in some
	 *         cases like regex and select all.
	 * @throws DirectorException
	 */

	@Path("/browse/{imageId: [0-9a-zA-Z_-]+}/search")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public SearchFilesInImageResponse searchFilesInImage(
			@PathParam("imageId") String imageId,
			SearchFilesInImageRequest searchFilesInImageRequest) {
		imageService = new ImageServiceImpl();
		searchFilesInImageRequest.id = imageId;
		SearchFilesInImageResponse filesInImageResponse = new SearchFilesInImageResponse();
		try {
			filesInImageResponse = imageService
					.searchFilesInImage(searchFilesInImageRequest);
		} catch (DirectorException e) {
			// TODO Handle Error
			log.error("Error while searching for files in image : " + imageId,
					e);
			try {
				imageService.unMountImage(imageId, null);
			} catch (DirectorException e1) {
				// TODO Handle Error
				log.error("Error while unmounting image  : " + imageId, e);
			}
		}
		String join = StringUtils.join(filesInImageResponse.files, "");
		filesInImageResponse.treeContent = join;

		// return join;
		return filesInImageResponse;
	}

	/**
	 * Retrieves list of deployment types - VM and BareMetal are the types
	 * returned as JSON
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
	 * Lookup method to fetch the image formats. Currently we return vhd and
	 * qcow2 as JSON
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
	 * lookup method to fetch the launch policies. The current launch policies
	 * that are returned are MeasureOnly and MeasureAndEnforce
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
	 * Returns the data required for the first step of the wizard. If the user
	 * has previously created a policy, the text field on the screen would show
	 * the name picked by the user. If not, it will pick the name of the image
	 * for which the policy is being created.
	 * 
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
	 * 
	 * 
	 * 
	 * @param image_id
	 * @return
	 * @throws DirectorException
	 */
	@Path("/{image_id: [0-9a-zA-Z_-]+}/getpolicymetadataforimage")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public CreateTrustPolicyMetaDataRequest getPolicyMetadataForImage(
			@PathParam("image_id") String image_id) throws DirectorException {
		return imageService.getPolicyMetadataForImage(image_id);
	}

	/**
	 * When the user has finished selecting files and dirs and clicks on the
	 * Next button for creating a policy, we call this method to : 1) Sign with
	 * MTW 2) Generate Hashes
	 * 
	 * 
	 * @param image_id
	 *            id of the image whose policy is being created
	 * @return
	 */
	@Path("/{image_id: [0-9a-zA-Z_-]+}/createpolicy")
	@Consumes(MediaType.APPLICATION_JSON)
	@POST
	public String createTrustPolicy(@PathParam("image_id") String image_id) {
		try {
			return imageService.createTrustPolicy(image_id);
		} catch (DirectorException | JAXBException de) {
			log.error("Error creating policy from draft for image : "
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
	protected String getLoginUsername() {
		return ShiroUtil.subjectUsername();
	}

	/**
	 * 
	 * Creates an initial draft of policy. This method is invoked when the user,
	 * navigates from the grid, where there is a "plus" icon for the trust
	 * policy icon, indicating that there is no draft currently associated. When
	 * the user navigates from the first screen of wizard to second, we create a
	 * default trust policy, with no files in whitelist.
	 * 
	 * 
	 * 
	 * 
	 * @param createTrustPolicyMetaDataRequest
	 * @return
	 */
	@Path("/trustpoliciesmetadata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public CreateTrustPolicyMetaDataResponse createTrustPolicyMetaData(
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest) {

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

	/**
	 * 
	 * This call is made during policy create flow for Live host. We have
	 * templates defined in the database. Depending on the type of the live host
	 * (with vrtm installed or not), a certain template is picked and applied
	 * during creating a new blank policy draft.
	 * 
	 * 
	 * @param image_id
	 *            the image for which the template needs to be applied
	 * @return Response that sends back the status of the function.
	 */
	@Path("/{image_id: [0-9a-zA-Z_-]+}/importpolicytemplate")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public CreateTrustPolicyMetaDataResponse importPolicyTemplate(
			@PathParam("image_id") String image_id) {
		CreateTrustPolicyMetaDataResponse createTrustPolicyMetadataResponse = new CreateTrustPolicyMetaDataResponse();
		createTrustPolicyMetadataResponse.setStatus("SUCCESS");
		try {
			createTrustPolicyMetadataResponse = imageService
					.importPolicyTemplate(image_id);

		} catch (DirectorException e) {
			createTrustPolicyMetadataResponse.setStatus(Constants.ERROR);
			createTrustPolicyMetadataResponse.setDetails(e.getMessage());
			return createTrustPolicyMetadataResponse;
		}

		return createTrustPolicyMetadataResponse;
	}

	/**
	 * After the user has finalized the list of files and dirs and created a
	 * policy, if he chooses to revisit the files/dirs selection we need to
	 * recreate the policy draft. this method does the same.
	 * 
	 * 
	 * @param imageId
	 * @param image_action_id
	 * @return
	 * @throws DirectorException
	 */
	@Path("/{imageId: [0-9a-zA-Z_-]+}/recreatedraft")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TrustPolicyDraft createPolicyDraftFromPolicy(
			@PathParam("imageId") String imageId,
			@QueryParam("action_id") String image_action_id)
			throws DirectorException {
		try {
			return imageService.createPolicyDraftFromPolicy(imageId,
					image_action_id);
		} catch (DirectorException e) {
			log.error("Unable to download Policy");
			throw new DirectorException(
					"Error in creating draft again from policy", e);
		}

	}

	/**
	 * 
	 * Method lets the user download the policy from the grids page. The user
	 * can visit the grid any time and download the policy. This method looks
	 * into the MW_TRUST_POLICY table and gets the policy string and sends it as
	 * an xml content to the user
	 * 
	 * 
	 * @param imageId
	 *            the image for which the policy is downloaded
	 * @return XML content of the policy
	 * @throws DirectorException
	 */
	@Path("/{imageId: [0-9a-zA-Z_-]+}/downloadPolicy")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response downloadPolicyForImageId(
			@PathParam("imageId") String imageId) throws DirectorException {
		try {
			TrustPolicy policy = imageService.getTrustPolicyByImageId(imageId);
			ResponseBuilder response = Response.ok(policy.getTrust_policy());
			response.header("Content-Disposition",
					"attachment; filename=policy_"
							+ policy.getImgAttributes().getName() + ".xml");

			return response.build();
		} catch (DbException e) {
			log.error("Unable to download Policy");
			throw new DirectorException("Unable to download Policy", e);
		}
	}

	/**
	 * Method that downloads the BM image which has been modified to push the
	 * trust policy in the /boot/trust folder. The user, on the third step of
	 * the wizard, gets a link which downlods the modified image
	 * 
	 * @param imageId
	 *            Id of the image which needs to be downloaded
	 * @param isModified
	 *            Flag to check if we need to download the image itself or the
	 *            modified image, which is with the embedded policy
	 * @return Sends back the image file
	 * @throws DirectorException
	 */
	@Path("/{imageId: [0-9a-zA-Z_-]+}/downloadImage")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response downloadImage(@PathParam("imageId") String imageId,
			@QueryParam("modified") boolean isModified)
			throws DirectorException {
		try {
			String pathname;

			pathname = imageService.getFilepathForImage(imageId, isModified);
			File imagefile = new File(pathname);
			ResponseBuilder response = Response.ok(imagefile);
			response.header(
					"Content-Disposition",
					"attachment; filename="
							+ pathname.substring(pathname
									.lastIndexOf(File.separator) + 1));
			return response.build();
		} catch (DbException e) {
			log.error("Unable to download Image");
			throw new DirectorException("Unable to download Image", e);
		}

	}


	/**
	 * 
	 * Method lets the user download the policy and manifest as a tarball from the grids page. The user
	 * can visit the grid any time and download the policy and manifest as it was created in the wizrd. 
	 * This method looks
	 * into the MW_TRUST_POLICY table and gets the policy string, creates a manifest and sends it as
	 * an tarball content to the user
	 * 
	 * 
	 * @param imageId
	 *            the image for which the policy and manifest is downloaded
	 * @return TAR ball  content of the policy
	 * @throws DirectorException
	 */
	@Path("/{imageId: [0-9a-zA-Z_-]+}/downloadPolicyAndManifest")
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadPolicyAndManifestForImageId(
			@PathParam("imageId") String imageId) throws DirectorException {
		File tarBall = imageService.createTarballOfPolicyAndManifest(imageId);
		
		ResponseBuilder response = Response.ok(tarBall);

		response.header("Content-Disposition", "attachment; filename="
				+ tarBall.getName());

		return response.build();
	}
	
	@Path("/{imageId: [0-9a-zA-Z_-]+}/deletePolicy")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String deletePolicy(@PathParam("imageId") String imageId)
	{
		String response = "Success";
		try {
			imageService.deleteTrustPolicy(imageId);
		} catch (DirectorException e) {
			response = Constants.ERROR;
		}
		return response;

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

}
