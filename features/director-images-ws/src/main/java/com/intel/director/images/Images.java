/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;

import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.CreateTrustPolicyResponse;
import com.intel.director.api.GenericRequest;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.GetImageStoresResponse;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionRequest;
import com.intel.director.api.ImageActionResponse;
import com.intel.director.api.ImageStoreObject;
import com.intel.director.api.ImportPolicyTemplateResponse;
import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.ListImageLaunchPolicyResponse;
import com.intel.director.api.MountImageRequest;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.SshSettingRequest;
import com.intel.director.api.TrustDirectorImageUploadRequest;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.TrustPolicyResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.api.UpdateTrustPolicyRequest;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageActionService;
import com.intel.director.service.ImageService;
import com.intel.director.service.LookupService;
import com.intel.director.service.impl.ImageActionImpl;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.director.service.impl.LookupServiceImpl;
import com.intel.director.util.TdaasUtil;
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
@Path("/")
public class Images {

	ImageService imageService = new ImageServiceImpl();
	ImageActionService actionService = new ImageActionImpl();
	LookupService lookupService = new LookupServiceImpl();

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(Images.class);

	/**
	 * API for uploading image metadata like image format, deployment type(VM,
	 * BareMetal, Docker), image file name, image size, etc. Creates image
	 * upload metadata with specified parameters and returns metadata along with
	 * image id.
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * <pre>
	 * https://server.com:8443/v1/images/uploads/content/uploadMetadata
	 * Input: {"name":"test.img","image_deployments":"VM","image_format": "qcow2", "image_size":202354}
	 * Output: {"created_by_user_id":"admin","created_date":1446801301639,"edited_by_user_id":"admin",
	 * 			"edited_date":1446801301639,"id":"B79EDFE9-4690-42B7-B4F0-71C53E36368C","name":"test.img",
	 * 			"image_format":"qcow2","image_deployments":"VM","status":"Incomplete","image_size":407552,
	 * 			"sent":0,"deleted":false,"location":"/mnt/images/"}
	 * </pre>
	 *
	 * @param TrustDirectorImageUploadRequest
	 *            object which includes metadata information
	 * @return TrustDirectorImageUploadResponse object contains newly created
	 *         image metadata along with image_id
	 * @throws DirectorException
	 * @throws Exception
	 */
	@Path("images")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TrustDirectorImageUploadResponse createUploadImageMetadata(
			TrustDirectorImageUploadRequest uploadRequest)
			throws DirectorException {

		imageService = new ImageServiceImpl();

		TrustDirectorImageUploadResponse uploadImageToTrustDirector;
		try {
			if (imageService.doesImageNameExist(uploadRequest.image_name)) {
				uploadImageToTrustDirector = new TrustDirectorImageUploadResponse();
				uploadImageToTrustDirector.state = Constants.ERROR;
				uploadImageToTrustDirector.details = "Image with Same Name already exists. <br>Please Enter Image Name ";
				return uploadImageToTrustDirector;
			}
			uploadImageToTrustDirector = imageService
					.createUploadImageMetadataImpl(
							uploadRequest.image_deployments,
							uploadRequest.image_format, uploadRequest.image_name,
							uploadRequest.image_size);
			uploadImageToTrustDirector.state = Constants.SUCCESS;
			log.info("Successfully uploaded image to location: "
					+ uploadImageToTrustDirector.getLocation());

			return uploadImageToTrustDirector;
		}
		catch (DirectorException e) {
			log.error("Error in Saving Image metadata", e);
			throw new DirectorException("Error in Saving Image metadata", e);
		}

	}

	/**
	 * API for uploading image data for the given image id. Before Uploading
	 * image it is divided in chunks and sent to server one by one. Once the
	 * chunk is received location to save image is retrieved from DB using given
	 * image id and chunk is saved to that location.
	 *
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 *  <pre>
	 * https://server.com:8443/v1/images/uploads/content/upload/B79EDFE9-4690-42B7-B4F0-71C53E36368C
	 * Input: chunk for image upload
	 * Output: {"created_by_user_id":"admin","created_date":1446801301639,"edited_by_user_id":"admin",
	 * 			"edited_date":1446801301639,"id":"B79EDFE9-4690-42B7-B4F0-71C53E36368C","name":"test.img",
	 * 			"image_format":"qcow2","image_deployments":"VM","status":"Complete","image_size":407552,
	 * 			"sent":407552,"deleted":false,"location":"/mnt/images/"}
	 * </pre>
	 * @param imageId
	 *            - id received as response of
	 *            https://server.com:8443/v1/images/
	 *            uploads/content/uploadMetadata request
	 * @param filInputStream
	 *            - image data sent as chunk
	 * @return TrustDirectorImageUploadResponse object with updated image upload
	 *         metadata
	 * @throws Exception	 
	 */
	@Path("rpc/images/content/{image_id: [0-9a-zA-Z_-]+}")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public TrustDirectorImageUploadResponse uploadImageToTrustDirector (
			@PathParam("image_id") String imageId, InputStream filInputStream)  throws DirectorException{
		log.info("Uploading image to TDaaS");
		imageService = new ImageServiceImpl();
		TrustDirectorImageUploadResponse uploadImageToTrustDirector;
		try {
			long lStartTime = new Date().getTime();

			uploadImageToTrustDirector = imageService
					.uploadImageToTrustDirectorSingle(imageId, filInputStream);
			log.info("Successfully uploaded image to location: "
					+ uploadImageToTrustDirector.getLocation());
			long lEndTime = new Date().getTime();
			
			long difference = lEndTime - lStartTime;
			log.info("Time taken to upload image to TD: " + difference);
			filInputStream.close();
			Session session=SecurityUtils.getSubject().getSession();
			session.touch();
			return uploadImageToTrustDirector;

		}
		catch (DirectorException | IOException e) {
			log.error("Error while uploading image to Trust Director", e);
			throw new DirectorException("Error in uploading image", e);
		}
	}


	/**
	 * Returns list of images in TD depending on the image deployment type
	 * supplied. This call is made so that grids on the UI for VM and Hosts can
	 * be populated. Each image has an image deployment type : BareMetal or VM.
	 *
	 * This method gets the list of images based on the deployment type provided as a query param.
	 * Providing the deployment type is optional. If provided the value should be 
	 * VM or BareMetal. 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * <pre>
	 * https://server.com:8443/v1/images
	 * Input: deploymentType : VM
	 * Output: {
	 * "images": [
	 * {
	 * "created_by_user_id": "admin",
	 * "created_date": "2015-11-10",
	 * "edited_by_user_id": "admin",
	 * "edited_date": "2015-11-12",
	 * "id": "F9E2C446-2AA3-4620-8A76-60F43721FE10",
	 * "name": "CIRROS_S_2.img",
	 * "image_format": "qcow2",
	 * "image_deployments": "VM",
	 * "status": "Complete",
	 * "sent": 13312,
	 * "deleted": false,
	 * "location": "/mnt/images/",
	 * "trust_policy_id": "247d041e-f2ae-4746-9b9d-68c75a8834c3",
	 * "uploads_count": 1
	 * }
	 * }]}
	 * 
	 * Input: deploymentType : BareMetal
	 * Output: {
	 * "images": [
	 * {
	 * "created_by_user_id": "admin",
	 * "created_date": "2015-11-10",
	 * "edited_by_user_id": "admin",
	 * "edited_date": "2015-11-12",
	 * "id": "F9E2C446-2AA3-4620-8A76-60F43721FE10",
	 * "name": "DBHost",
	 * "image_format": "",
	 * "image_deployments": "BareMetal",
	 * "status": "Complete",
	 * "sent": "",
	 * "deleted": false,
	 * "location": "",
	 * "trust_policy_id": "247d041e-f2ae-4746-9b9d-68c75a8834c3",
	 * "uploads_count": 
	 * }
	 * }]}
	 * </pre>
	 * 
	 * @param deployment_type
	 *            - VM/BareMetal
	 * @return List of image details
	 * @throws DirectorException
	 * @throws DbException
	 */
	@Path("images")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public SearchImagesResponse getImagesByDeploymentType(
			@QueryParam("deploymentType") String deployment_type)
			throws DirectorException {
		SearchImagesRequest searchImagesRequest = new SearchImagesRequest();
		searchImagesRequest.deploymentType = deployment_type;
		SearchImagesResponse searchImagesResponse = imageService
				.searchImages(searchImagesRequest);
		return searchImagesResponse;

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
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall 
	 * <pre>
	 *
	 * Input: {id : "ACD7747D-79BE-43E3-BAA5-07DBEC13D272"} 
	 * Output: { created_by_user_id: "admin", created_date: 1448217000000, deleted: false, 	edited_by_user_id: "admin", edited_date: 1448303400000, id: "ACD7747D-79BE-43E3-	BAA5-07DBEC13D272", image_deployments: "VM", image_format: "qcow2", image_size: 	13312, location: "/mnt/images/",	name: "cirrus_1811.img", sent: 13312, status: 	"SUCCESS"	}
	 *
	 *
	 * If the user tries to mount an image which, for some reason, has been removed from the uploaded location, the response will look like :
	 * {"status":"Error", "details":" No image found with id: <UUID>"}
	 * 
	 * </pre>
	 * 
	 * @param imageId
	 *            UUID: Image id of the image in MW_IMAGE to be mounted
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @return MountImageResponse containing the details of the mount process.
	 */
	@Path("rpc/mount-image")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public MountImageResponse mountImage(MountImageRequest mountImage,
			@Context HttpServletRequest httpServletRequest,
			@Context HttpServletResponse httpServletResponse) {
		log.info("inside mounting image in web service");
		String user = getLoginUsername();
		log.info("User mounting image : " + user);
		MountImageResponse mountImageResponse = new MountImageResponse();
		try {
			mountImageResponse = imageService.mountImage(mountImage.id, user);
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
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall 
	 * <pre>
	 * Input: {id : "ACD7747D-79BE-43E3-BAA5-07DBEC13D272"} 
	 * Output: { created_by_user_id: "admin", created_date: 1448217000000, deleted: false, 	edited_by_user_id: "admin", edited_date: 1448303400000, id: "ACD7747D-79BE-43E3-	BAA5-07DBEC13D272", image_deployments: "VM", image_format: "qcow2", image_size: 	13312, location: "/mnt/images/",	name: "cirrus_1811.img", sent: 13312, status: 	"Success"}
	 * 
	 * In case of error unmounting, the unmount script returned a non zero exit code:
	 * { "id": "1eebe380-1a36-11e5-9472-0002a5d5c51b", "name":	"cirros-x86.img" "image_deployments": "VM,Bare_Metal" "image_format":
	 * "qcow2" "mounted": "false", status:"Error", details: "Unmount script executed with errors" }
	 * 
	 * </pre>
	 * 
	 * @param imageId
	 *            Id of the image to be un-mounted
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @return UnmountImageResponse containing the details of the unmount
	 * @throws DirectorException
	 */
	@Path("rpc/unmount-image")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public UnmountImageResponse unMountImage(MountImageRequest unmountimage,
			@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
		String user = getLoginUsername();
		UnmountImageResponse unmountImageResponse;
		try {
			unmountImageResponse = imageService.unMountImage(unmountimage.id, user);
		} catch (Exception e) {
			unmountImageResponse = new UnmountImageResponse();
			log.error("Error while unmounting image ", e);
			unmountImageResponse.setStatus(Constants.ERROR);
			unmountImageResponse.setDetails(e.getMessage());
			unmountImageResponse.setId(unmountimage.id);
		}
		return unmountImageResponse;
	}

	/**
	 * Update the policy draft by applying the patch. Whenever an user selects
	 * directories on the UI, a patch of the changes made to the selections is
	 * sent to the server every 10 mins. this method does the job of merging the
	 * patch/delta into the trust policy draft that is stored in the database.
	 *
	 * A sample patch looks like this: <patch> <add sel="<node selector>"
	 * ><File path="<file path>"></add> <remove sel="<node selector>"
	 * ></remove> </patch>
	 *
	 * We use https://github.com/dnault/xml-patch library to apply patches.
	 *
	 * the method returns the patched trust policy as a response to successful
	 * patch application In case of error, a DirectorException is thrown. It is
	 * caught in the failure section of the ajax call and shown as a pop up
	 * message.
	 *
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType PUT
	 * @mtwSampleRestCall
	 * <pre>
	 * 
	 * Input: UUID of the image in path
	 * {"patch":
	 * "<patch></patch>"
	 * }
	 * Output: {"status":"Success", details:"<policy>"}
	 *
	 * In case of error:
	 *  Output: {"status":"Error", details:"<Cause of error>"}
	 * 
	 * </pre>
	 *
	 * @param imageId
	 *            Id of image whose draft is to be edited
	 * @param trustPolicyDraftEditRequest
	 * @return Updated policy
	 * @throws DirectorException
	 */
	@Path("trust-policy-drafts/{trustPolicyDraftId: [0-9a-zA-Z_-]+}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public GenericResponse editPolicyDraft(
			@PathParam("trustPolicyDraftId") String trustPolicyDraftId,
			TrustPolicyDraftEditRequest trustPolicyDraftEditRequest) {
		GenericResponse response = new GenericResponse();
		trustPolicyDraftEditRequest.trust_policy_draft_id = trustPolicyDraftId;
		TrustPolicyDraft policyDraft = null;
		String trustPolicyDraftXML = null;
		try {
			policyDraft = imageService
					.editTrustPolicyDraft(trustPolicyDraftEditRequest);
			trustPolicyDraftXML= policyDraft.getTrust_policy_draft();
			response.setStatus(Constants.SUCCESS);
			response.setDetails(trustPolicyDraftXML);
			log.debug("Updated policy draft trustPolicyXML : "
					+ trustPolicyDraftXML);
		} catch (DirectorException e) {
			response.setStatus(Constants.ERROR);
			response.setDetails(e.getMessage());
			log.error("Error while updating policy draft : "+ trustPolicyDraftId);
		}
		
		return response;
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
	 *         
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall 
	 * <pre>
	 * https://server.com:8443/v1/images/08EB37D7-2678-495D-B485-59233EB51996/search
	 * Input: QueryPAram : dir=/boot/&recursive=false&files_for_policy=false&init=false&include_recursive=false&reset_regex=false
	 * output: {"tree_content":"<Html containing the nested ul and li tags>", "patch_xml":"<pacth><list of add remove tags as per the operation></pacth>"}
	 * 
	 * The output tag has the patch_xml set only in case in the following cases of the query parameters:
	 * 1) recursive=true and files_for_policy=true
	 * 2) recursive=true and files_for_policy=false
	 * 3) reset_regex = true
	 * 4) include="<regex expression>" & exclude="<regex expression>" with optional include_regex=true
	 * 
	 * </pre>
	 * @throws DirectorException
	 */

	@Path("images/{imageId: [0-9a-zA-Z_-]+}/search")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public SearchFilesInImageResponse searchFilesInImage(
			@PathParam("imageId") String imageId, @Context UriInfo uriInfo) {
		imageService = new ImageServiceImpl();
		SearchFilesInImageRequest searchFilesInImageRequest = new TdaasUtil().mapUriParamsToSearchFilesInImageRequest(uriInfo);
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
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * <pre>
	 * https://server.com:8443/v1/image-deployments
	 * Input: None
	 * Output: {
	 *   "image_deployments": [
	 *     {
	 *       "name": "VM",
	 *       "display_name": "Virtualized Server"
	 *     },
	 *     {
	 *       "name": "BareMetal",
	 *       "display_name": "Non-Virtualized Server"
	 *     }
	 *   ]
	 * }
	 * </pre>
	 */
	@Path("image-deployments")
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
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall 
	 * <pre>
	 * https://server.com:8443/v1/image-formats
	 * Input: None
	 * Output: {"image_formats": [{"name": "qcow2","display_name": "qcow2"}]}
	 * </pre>

	 */
	@Path("image-formats")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ListImageFormatsResponse getImageFormats() {
		return lookupService.getImageFormats();
	}

	/**
	 * lookup method to fetch the launch policies. The current launch policies
	 * that are returned are MeasureOnly and MeasureAndEnforce
	 * @param deployment_type
	 * @return launch policy list
	 * @TDMethodType GET
	 * @TDSampleRestCall <pre>
	 * https://server.com:8443/v1/image-launch-policies
	 * Input: QueryParam String deploymentType=VM
	 * Output: { "image_launch_policies": [
	 * {"name":"MeasureOnly","value":"Hash Only","image_deployments":["VM","BareMetal"]},
	 * {"name":"MeasureAndEnforce","value":"Hash and enforce","image_deployments":["VM"]},
	 * {"name":"encrypted","value":"Encryption","image_deployments":["VM"]}]}
	 * </pre>
	 */
	@Path("image-launch-policies")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ListImageLaunchPolicyResponse getImageLaunchPoliciesList(@QueryParam("deploymentType") String deploymentType){
		return lookupService.getImageLaunchPolicies(deploymentType);
	}

	

	/**
	 * Get the current metadata of a policy, which includes the options chosen
	 * by the user while creating a trust policy. The data includes the policy
	 * xml, whether its encrypted, the launch policy
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall 
	 * <pre>
	 * Input: QueryParam String: imageId=ACD7747D-79BE-43E3-BAA5-7DBEC13D272&imageArchive=false
	 * 
	 * Output: {"launch_control_policy":"MeasureAndEnforce","encrypted":true,"image_name":"cirrus_1811.img","display_name":"111"}
	 * </pre>
	 * 
	 * @param image_id
	 * @return
	 * @throws DirectorException
	 */
	@Path("trust-policy-drafts/{trustPolicyDraftId:  | [0-9a-zA-Z_-]+ }")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public CreateTrustPolicyMetaDataResponse getPolicyMetadataForImage(
			@PathParam("trustPolicyDraftId") String trustPolicyDraftId,
			@QueryParam("imageId") String imageId,
			@QueryParam("deploymentType") String deploymentType,
			@QueryParam("imageArchive") boolean imageArchive) {
		
		CreateTrustPolicyMetaDataResponse createTrustPolicyMetaDataResponse = null;
		if(StringUtils.isNotBlank(deploymentType) && deploymentType.equals(Constants.DEPLOYMENT_TYPE_BAREMETAL)){
			try {
				SshSettingRequest bareMetalMetaData = imageService.getBareMetalMetaData(imageId);
				createTrustPolicyMetaDataResponse  = new CreateTrustPolicyMetaDataResponse();
				createTrustPolicyMetaDataResponse.setIp_address(bareMetalMetaData.getIpAddress());
				createTrustPolicyMetaDataResponse.setUsername(bareMetalMetaData.getUsername());
				createTrustPolicyMetaDataResponse.setDisplay_name(bareMetalMetaData.getPolicy_name());
				createTrustPolicyMetaDataResponse.setStatus(Constants.SUCCESS);
				return createTrustPolicyMetaDataResponse;
			} catch (DirectorException e) {
				createTrustPolicyMetaDataResponse = new CreateTrustPolicyMetaDataResponse();
				createTrustPolicyMetaDataResponse.setStatus(Constants.ERROR);
				createTrustPolicyMetaDataResponse.setDetails(e.getMessage());
				log.error("Error in trust-policy-drafts/"+trustPolicyDraftId);
				return createTrustPolicyMetaDataResponse;
			}
		}
		if(StringUtils.isNotBlank(trustPolicyDraftId)){
			try {
				return imageService.getPolicyMetadata(trustPolicyDraftId);
			} catch (DirectorException e) {
				createTrustPolicyMetaDataResponse = new CreateTrustPolicyMetaDataResponse();
				createTrustPolicyMetaDataResponse.setStatus(Constants.ERROR);
				createTrustPolicyMetaDataResponse.setDetails(e.getMessage());
				log.error("Error in trust-policy-drafts/"+trustPolicyDraftId);
				return createTrustPolicyMetaDataResponse;
			}
		}
		try {
			return imageService.getPolicyMetadataForImage(imageId);
		} catch (DirectorException e) {
			createTrustPolicyMetaDataResponse = new CreateTrustPolicyMetaDataResponse();
			createTrustPolicyMetaDataResponse.setStatus(Constants.ERROR);
			createTrustPolicyMetaDataResponse.setDetails(e.getMessage());
			log.error("Error in trust-policy-drafts/"+trustPolicyDraftId);
			return createTrustPolicyMetaDataResponse;
		}
	}

	/**
	 * When the user has finished selecting files and dirs and clicks on the
	 * Next button for creating a policy, we call this method to : 1) Sign with
	 * MTW 2) Generate Hashes
	 *
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * <pre>
	 * Input
	 * {"trust_policy_draft_id":"<UUID of trust policy draft>"}
	 * In case of a success, the response would be :
	 * {"status":"Success", details:""} 
	 * 		In case of error: {"status":"Error", "details":"Unable to sign the policy with MTW"} in the case where signing with MTW fails.
	 * </pre>
	 * 
	 * 
	 * @param image_id
	 *            id of the image whose policy is being created
	 * @return
	 */
	@Path("rpc/trust-policies")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public CreateTrustPolicyResponse createTrustPolicy(CreateTrustPolicyMetaDataRequest createPolicyRequest) {
		CreateTrustPolicyResponse response = new CreateTrustPolicyResponse();
		try {
			String imageId = imageService.getImageByTrustPolicyDraftId(createPolicyRequest.trust_policy_draft_id);
			String trustPolicyId = imageService.createTrustPolicy(createPolicyRequest.trust_policy_draft_id);
			response.setId(trustPolicyId);
			imageService.deletePasswordForHost(imageId);	
			response.setStatus(Constants.SUCCESS);	
		} catch (DirectorException de) {
			response.setStatus(Constants.ERROR);
			response.setDetails(de.getMessage());
			log.error("Error creating policy from draft for image : "
					+ createPolicyRequest.image_id, de);
		}
		return response;
	}

	/**
	 * On the step 3/3 of the wizard for VM, when the user clicks on the "Upload
	 * now" button, we accept the last moment changes in the name of the policy
	 * and update it. This method just validates that the name given by the user
	 * is unique
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType PUT
	 * @mtwSampleRestCall
	 * <pre>
	 * https://<host>/v1/trust-policies/7897-232321-432423-4322
	 * Input: UUID of trust policy in path {"display_name":"Name of policy"}
	 * Output: {"status":"<success/Error>", "details":"Error details"}
	 * </pre>
	 * @param createPolicyRequest
	 * @return
	 */
	@Path("trust-policies/{trust_policy_id: [0-9a-zA-Z_-]+|}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PUT
	public GenericResponse updateTrustPolicy(@PathParam("trust_policy_id") String trustPolicyId, UpdateTrustPolicyRequest updateTrustPolicyRequest) {
		GenericResponse monitorStatus = new GenericResponse();
		monitorStatus.status = Constants.SUCCESS;
		if(StringUtils.isBlank(trustPolicyId)){
			return monitorStatus;
		}
		try {
			imageService.updateTrustPolicy(updateTrustPolicyRequest, trustPolicyId);
		} catch (DirectorException de) {
			log.error("Error updating policy name for : "
					+ trustPolicyId, de);
			monitorStatus.status = Constants.ERROR;
			monitorStatus.details = de.getMessage();
		}
		return monitorStatus;
	}


	/**
	 * List configured image stores
	 *
	 *
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 *  
	 * @return
	 * @throws DirectorException
	 */
    @Deprecated
	@Path("images/image-stores")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public GetImageStoresResponse getImageStores() throws DirectorException {
		GetImageStoresResponse imageStores = new GetImageStoresResponse();
		imageStores.image_stores = new ArrayList<ImageStoreObject>();
		ImageStoreObject imageStore = new ImageStoreObject();
		imageStore.setName("Glance");
		imageStores.image_stores.add(imageStore);
		imageStore = new ImageStoreObject();
		imageStore.setName("Swift");
		imageStores.image_stores.add(imageStore);
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
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * <pre>
	 *	Input: {"image_id":"08EB37D7-2678-495D-B485-59233EB51996","image_name":"cirrus_1811.img","display_name":"cirrus_1811.img","launch_control_policy":"MeasureOnly","encrypted":false}
	 *
	 * Output: {"id":"50022e9c-577a-4bbd-9445-197a3e1a349f","trust_policy":"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n <Director>\n <CustomerId>testId</CustomerId>\n </Director>\n <Image>\n <ImageId>08EB37D7-2678-495D-B485-59233EB51996</ImageId>\n <ImageHash>6413fccb72e36d2cd4b20efb5b5fe1be916ab60f0fe1d7e2aab1a2170be1ff40</ImageHash>\n </Image>\n <LaunchControlPolicy>MeasureOnly</LaunchControlPolicy>\n <Whitelist DigestAlg=\"sha256\">\n <File Path=\"/boot/grub/stage1\"></File>\n <File Path=\"/boot/grub/menu.lst\"></File>\n <File Path=\"/initrd.img\"></File>\n <File Path=\"/boot/vmlinuz-3.2.0-37-virtual\"></File>\n <File Path=\"/boot/config-3.2.0-37-virtual\"></File>\n <File Path=\"/boot/initrd.img-3.2.0-37-virtual\"></File>\n <File Path=\"/boot/grub/e2fs_stage1_5\"></File>\n <File Path=\"/boot/grub/stage2\"></File>\n </Whitelist>\n</TrustPolicy>\n"}
	 * </pre>
	 *
	 *
	 * @param createTrustPolicyMetaDataRequest
	 * @return
	 */
	@Path("trust-policy-drafts")
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
			log.error("createTrustPolicyMetaData failed",e);
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
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * <pre>
	 * 
	 * Input: {"image_id":"08EB37D7-2678-495D-B485-59233EB51996"}
	 *
	 *	Output: {"trust_policy":"<policy xml>", "status":"SUCCESS","details":"<In case of error>"}
     *
	 * </pre>
	 *
	 * @param image_id
	 *            the image for which the template needs to be applied
	 * @return Response that sends back the status of the function.
	 */
	@Path("rpc/apply-trust-policy-template/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public ImportPolicyTemplateResponse importPolicyTemplate(
			GenericRequest req) {
		ImportPolicyTemplateResponse importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
		try {
			importPolicyTemplateResponse = imageService
					.importPolicyTemplate(req.getImage_id());

		} catch (DirectorException e) {
			log.error("Error in importPolicyTemplate ", e);	
			importPolicyTemplateResponse.setStatus(Constants.ERROR);
			importPolicyTemplateResponse.setDetails(e.getMessage());
			return importPolicyTemplateResponse;
		}

		return importPolicyTemplateResponse;
	}

	/**
	 * After the user has finalized the list of files and dirs and created a
	 * policy, if he chooses to revisit the files/dirs selection we need to
	 * recreate the policy draft. this method does the same.
	 *
	 *
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * <pre>
	 * Input: {"image_id":"08EB37D7-2678-495D-B485-59233EB51996"}
     *	
	 *	Output: {"id":"<UUID of Policy draft>", "trust_policy_draft":"<XML representation of policy>", "display_name":"<name provided by user for the policy>", "imgAttributes":"{"id":"<UUID of image>", "image_format":"qcow2", ..... }"}
	 *
	 * </pre>
	 *
	 * @param imageId
	 * @param image_action_id
	 * @return
	 * @throws DirectorException
	 */
	@Path("rpc/createDraftFromPolicy")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public TrustPolicyDraft createPolicyDraftFromPolicy(
			GenericRequest req)
			throws DirectorException {
		try {
			return imageService.createPolicyDraftFromPolicy(req.getImage_id());
		} catch (DirectorException e) {
			log.error("createPolicyDraftFromPolicy failed ");
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
	 * @mtwContentTypeReturned XML
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * <pre>
	 * Input: Image id as path param
	 *	Output: Content sent as stream
     *
	 * </pre>
	 * @param imageId
	 *            the image for which the policy is downloaded
	 * @return XML content of the policy
	 * @throws DirectorException
	 */
	@Path("images/{imageId: [0-9a-zA-Z_-]+}/downloads/policy")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response downloadPolicyForImageId(
			@PathParam("imageId") String imageId) {
		try {
			TrustPolicy policy = imageService.getTrustPolicyByImageId(imageId);
			ResponseBuilder response = Response.ok(policy.getTrust_policy());
			response.header("Content-Disposition",
					"attachment; filename=policy_"
							+ policy.getImgAttributes().getImage_name() + ".xml");

			return response.build();
		} catch (DbException e) {
			log.error("dowload policy and manifest failed",e);
			return Response.noContent().build();

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
	 *//*
	@Path("images/{imageId: [0-9a-zA-Z_-]+}/downloadImage")
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

	}*/

	/**
	 *
	 * Method lets the user download the policy and manifest as a tarball from
	 * the grids page. The user can visit the grid any time and download the
	 * policy and manifest as it was created in the wizrd. This method looks
	 * into the MW_TRUST_POLICY table and gets the policy string, creates a
	 * manifest and sends it as an tarball content to the user
	 * 
	 * 
	 * @mtwContentTypeReturned File
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * <pre>
	 * 
	 * Input: Image UUID
	 * Output: Content of tarball as stream
	 * </pre>
	 *
	 * @param imageId
	 *            the image for which the policy and manifest is downloaded
	 * @return TAR ball content of the policy
	 * @throws DirectorException
	 */
	@Path("images/{imageId: [0-9a-zA-Z_-]+}/downloads/policyAndManifest")
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadPolicyAndManifestForImageId(
			@PathParam("imageId") String imageId) {

		File tarBall;
		try {
			tarBall = imageService.createTarballOfPolicyAndManifest(imageId);
		} catch (DirectorException e) {
			// TODO Auto-generated catch block
			log.error("dowload policy and manifest failed", e);
			return Response.noContent().build();
		}
		ResponseBuilder response = Response.ok(tarBall);

		response.header("Content-Disposition", "attachment; filename="
				+ tarBall.getName());

		Response downloadResponse = response.build();
		return downloadResponse;
	}

	
	/**
	 * Delete the trust policy draft by the provided ID
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall
	 * <pre>
	 * Input: UUID of the policy draft to be deleted
	 * Output: Status of operation in json(Success/Error)
	 * {"status":"success", details:""}
	 * </pre> 
	 */
	@Path("trust-policy-drafts/{trustPolicyDraftId: [0-9a-zA-Z_-]+}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public GenericResponse deletePolicyDraft(@PathParam("trustPolicyDraftId") String trustPolicyDraftId) {
		GenericResponse response= new GenericResponse();
		try {
			imageService.deleteTrustPolicyDraft(trustPolicyDraftId);
			response.setStatus(Constants.SUCCESS);	
		} catch (DirectorException e) {
			log.error("Error in deletePolicyDraft", e);
			response.setStatus(Constants.ERROR);	
		}
		return response;

	}

	/**
	 * Deletes the signed trust policy by the provided id
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall 
	 * <pre> 
	 * Input: UUID of the policy to be deleted
	 * Output: Status(success/error) of operation in json
	 * "status":"success"}
	 * </pre>
	 */
	@Path("trust-policy/{trust_policy_id: [0-9a-zA-Z_-]+}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public GenericResponse deletePolicy(@PathParam("trust_policy_id") String trust_policy_id) {
		GenericResponse response= new GenericResponse();
		try {
			imageService.deleteTrustPolicy(trust_policy_id);
			response.setStatus(Constants.SUCCESS);	
		} catch (DirectorException e) {
			log.error("Error in deletePolicy ", e);	
			response.setStatus(Constants.ERROR);	
		}
		return response;

	}


	/**
	 *
	 * Mark image as deleted. We turn the disabled flag=true in the MW_IMAGE
	 * table
	 *
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall
	 * <pre>
	 * Input: pass the UUID of the image as path param
	 * Output: {"status": "success"}
	 * </pre>
	 * 
	 * @param imageId
	 *            Id of the image to be deleted
	 * @return Response stating status of the operation - Success/Error
	 * @throws DirectorException
	 */

	@Path("images/{imageId: [0-9a-zA-Z_-]+}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public GenericResponse deleteImage(@PathParam("imageId") String imageId) {
		GenericResponse response= new GenericResponse();
		try {
			imageService.deleteImage(imageId);
			response.setStatus(Constants.SUCCESS);	
		} catch (DirectorException e) {
			log.error("Error in deleteImage ", e);	
			response.setStatus(Constants.ERROR);	
		}
		return response;
		
	}

	/**
	 * Get trust policy for the provided ID
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @param trustPolicyId
	 * @return 
	 */

	@Path("trust-policy/{trustPolicyId: [0-9a-zA-Z_-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public TrustPolicyResponse getTrustPoliciesData(
			@PathParam("trustPolicyId") String trustPolicyId)  {
		TrustPolicyResponse trustpolicyresponse = null;
		try {
			trustpolicyresponse = imageService.getTrustPolicyMetaData(trustPolicyId);
		} catch (DirectorException e) {
			log.error("Error in getTrustPoliciesData ", e);
			trustpolicyresponse = new TrustPolicyResponse();
			trustpolicyresponse.setId(null);
		}
		return trustpolicyresponse;
	}
	
	/**
	 * This method will fetch an image-action which has image_id on which actions are performed, 
	 * list of actions to be performed, etc.
	 * 
	 * @param action_id
	 * @return ImageActionObject.
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * <pre>
	 * https://server.com:8443/v1/image-actions
	 * Input: action_id = CF0A8FA3-F73E-41E9-8421-112FB22BB057
	 * Output: {
	 * "id": "CF0A8FA3-F73E-41E9-8421-112FB22BB057",
	 * "image_id": "08EB37D7-2678-495D-B485-59233EB51996",
	 * "action_count": 2,
	 * "action_completed": 2,
	 * "action_size": 66570,
	 * "action_size_max": 66570,
	 * "action": [ { "status": "Complete","task_name": "Create Tar",  "execution_details": "Complete" },
	 * { "status": "Complete", "storename": "Glance", "task_name": "Upload Tar", "execution_details": "Complete" }],
	 * "current_task_status": "Complete",
	 * "current_task_name": "Upload Tar" }
	 * </pre>
	 */
	
	@Path("image-actions/{action_id: [0-9a-zA-Z_-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public ImageActionObject fetchImageAction(@PathParam("action_id") String actionId ) throws DirectorException {
		return actionService.fetchImageAction(actionId);
	}
	
	/**
	 * This method will create an image-action.
	 * Data required by this method is action_id and list of task and other parameter associated with it(Ex. store_name in case of Upload Tar task). 
	 * Output will contain action_id.
	 * 
	 * @param ImageActionRequest
	 * @return Output will contain action_id.
 	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall  
	 * <pre>
	 * https://server.com:8443/v1/image-actions
	 * Input: { "image_id":"08EB37D7-2678-495D-B485-59233EB51996",
	 * "actions":[ {"task_name":"Create Tar","status":"Incomplete"},
	 * {"task_name":"Upload Tar","status":"Incomplete","storename":"Glance"}]
	 * }
	 * Output:{
	 * "id": "CF0A8FA3-F73E-41E9-8421-112FB22BB057",
	 * "image_id": "08EB37D7-2678-495D-B485-59233EB51996",
	 * "action_count": 2,
	 * "action_completed": 2,
	 * "action_size": 66570,
	 * "action_size_max": 66570,
	 * "actions": [ { "status": "Incomplete","task_name": "Create Tar" },
	 * { "status": "Incomplete", "storename": "Glance", "task_name": "Upload Tar" }],
	 * "current_task_status": "Incomplete",
	 * "current_task_name": "Create Tar" }
	 * </pre>
	 */
	
	@Path("image-actions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public ImageActionResponse createImageAction(ImageActionRequest imageActionRequest) {	
		ImageActionResponse imageActionResponse = new ImageActionResponse();
		imageActionResponse.setStatus(Constants.SUCCESS);
		ImageActionObject imageActionObject;
		try {
			imageActionObject = actionService.createImageAction(imageActionRequest);
		} catch (DirectorException e) {
			log.error("Error in createImageAction",e);
			imageActionResponse.setStatus(Constants.ERROR);
			imageActionResponse.setDetails(e.getMessage());
			return imageActionResponse;
		}
		imageActionResponse.setId(imageActionObject.getId());
		imageActionResponse.setImage_id(imageActionObject.getImage_id());
		imageActionResponse.setAction_completed(imageActionObject.getAction_completed());
		imageActionResponse.setAction_count(imageActionObject.getAction_count());
		imageActionResponse.setActions(imageActionObject.getActions());
		imageActionResponse.setCurrent_task_name(imageActionObject.getCurrent_task_name());
		imageActionResponse.setCurrent_task_status(imageActionObject.getCurrent_task_status());
		return imageActionResponse;
	}
	/**
	 * This method will update existing image-action.
	 * Data required by this method is action_id and list of task and other parameter associated with it(Ex. store_name in case of Upload Tar task). 
	 * Output will contain updated action_id.
	 * 
	 * @param ImageActionRequest
	 * @return Status of update operation
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType PUT
	 * @mtwSampleRestCall 
	 * <pre>
	 * https://server.com:8443/v1/image-actions
	 * Input: { "action_id": "CF0A8FA3-F73E-41E9-8421-112FB22BB057"
	 * "image_id":"08EB37D7-2678-495D-B485-59233EB51996",
	 * "actions":[ {"task_name":"Create Tar","status":"Incomplete"},
	 * {"task_name":"Upload Tar","status":"Incomplete","storename":"Glance"}]
	 * }
	 * Output: { "id": "CF0A8FA3-F73E-41E9-8421-112FB22BB057", "image_id": "08EB37D7-2678-495D-B485-59233EB51996", "status": "success"}
	 * 
	 * In case of error : { status : "Error" , details : "Error in fetching image with image id :: 08EB37D7-2678-495D-B485-59233EB51996"}
	 * </pre>
	 */
	
	@Path("image-actions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PUT
	public ImageActionResponse updateImageAction(ImageActionRequest imageActionRequest) {
		ImageActionResponse imageActionResponse = new ImageActionResponse();
		imageActionResponse.setStatus(Constants.SUCCESS);
		ImageActionObject imageActionObject;
		try {
			imageActionObject = actionService.updateImageAction(imageActionRequest);
		} catch (DirectorException e) {
			log.error("Error in updateImageAction",e);
			imageActionResponse.setStatus(Constants.ERROR);
			imageActionResponse.setDetails(e.getMessage());
			return imageActionResponse;
		}
		imageActionResponse.setId(imageActionObject.getId());
		imageActionResponse.setImage_id(imageActionObject.getImage_id());
		return imageActionResponse;
	}
	/**
	 * This method will delete existing image-action. Data required by this method is action_id. 
	 * Output will contain status of delete task initiated.
	 * 
	 * @param imageActionRequest
	 * @return Status of delete operation
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall  
	 * <pre>
	 * https://server.com:8443/v1/image-actions
	 * Input: PathParam =  actionId : CF0A8FA3-F73E-41E9-8421-112FB22BB057
	 * Output: { "status": "success"}
	 * </pre>

	 */
	@Path("image-actions/{actionId: [0-9a-zA-Z_-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@DELETE
	public ImageActionResponse deleteImageAction(@PathParam("actionId") String actionId) {	
		ImageActionResponse imageActionResponse = new ImageActionResponse();
		imageActionResponse.setStatus(Constants.SUCCESS);
		try {
			actionService.deleteImageAction(actionId);
		} catch (DirectorException e) {
			log.error("Error in deleteImageAction",e);
			imageActionResponse.setStatus(Constants.ERROR);
			imageActionResponse.setDetails(e.getMessage());
			return imageActionResponse;
		}
		return imageActionResponse;
	}

}
