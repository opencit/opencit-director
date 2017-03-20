/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import com.intel.director.api.upload.Chunk;
import com.intel.director.service.impl.UploadService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import com.intel.director.util.FileFormatExecutor;
import com.intel.director.util.UpdateImageFormatTask;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.api.CommonValidations;
import com.intel.director.api.ErrorCode;
import com.intel.director.api.GenericDeleteResponse;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.HashTypeObject;
import com.intel.director.api.ImageInfoResponse;
import com.intel.director.api.ListImageDeploymentsResponse;
import com.intel.director.api.ListImageFormatsResponse;
import com.intel.director.api.MountImageRequest;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.SshSettingRequest;
import com.intel.director.api.SshSettingResponse;
import com.intel.director.api.TrustDirectorImageUploadRequest;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.api.UpgradeTrustPolicyResponse;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DockerUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.exception.ConnectionFailException;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ArtifactUploadService;
import com.intel.director.service.DockerActionService;
import com.intel.director.service.ImageService;
import com.intel.director.service.LookupService;
import com.intel.director.service.impl.ArtifactUploadServiceImpl;
import com.intel.director.service.impl.DockerActionImpl;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.director.service.impl.LookupServiceImpl;
import com.intel.director.service.impl.SettingImpl;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.jaxrs2.server.PATCH;
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
    LookupService lookupService = new LookupServiceImpl();
    SettingImpl settingimpl = new SettingImpl();
    ArtifactUploadService artifactUploadService = new ArtifactUploadServiceImpl();
    DockerActionService dockerActionService = new DockerActionImpl();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Images.class);

	/**
	 * API for uploading image metadata like image format, deployment type(VM,
	 * BareMetal, Docker), image file name, image size ( in bytes ), etc.
	 * Creates image upload metadata with specified parameters and returns
	 * metadata along with image id.
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * https://{IP/HOST_NAME}/v1/images
	 * Input: {"image_name":"test.img","image_deployments":"VM","image_format": "qcow2", "image_size":13631488 }
	 * Output: {
	 *   "created_by_user_id": "admin",
	 *   "created_date": "2016-05-16 07:19:39",
	 *   "edited_by_user_id": "admin",
	 *   "edited_date": "2016-05-16 07:19:39",
	 *   "id": "D848C017-36BC-43BB-BC69-698A517878A0",
	 *   "image_name": "test.img",
	 *   "image_format": "qcow2",
	 *   "image_deployments": "VM",
	 *   "image_size": 13631488,
	 *   "sent": 0,
	 *   "deleted": false,
	 *   "image_upload_status": "success",
	 *   "image_Location": "/mnt/images/vm/"
	 * }
	 * 
	 * 
	 * In case of Docker:
	 * Input:    {
	 * 	    "image_deployments": "Docker",
	 * 	    "image_format" : "tar",
	 * 	    "repository" : "debian",
	 * 	    "tag":"latest",
	 * 	    "image_size" : 1322496
	 * 	 }
	 * 	 Output:
	 * 		{
	 *   "created_by_user_id": "admin",
	 *   "created_date": "2016-05-16 07:20:40",
	 *   "edited_by_user_id": "admin",
	 *   "edited_date": "2016-05-16 07:20:40",
	 *   "id": "D3341CA4-1F3A-4CD7-B9BA-710F363A3CD8",
	 *   "image_name": "debian:latest",
	 *   "image_format": "tar",
	 *   "image_deployments": "Docker",
	 *   "image_size": 1322496,
	 *   "sent": 0,
	 *   "deleted": false,
	 *   "repository": "debian",
	 *   "tag": "latest",
	 *   "image_upload_status": "success",
	 *   "image_Location": "/mnt/images/docker/"
	 * }
	 * In Case of error such as image name already exists on the server :
	 * {
	 *   "status": "Error",
	 *   "details": "Image with Same Name already exists. <br>Please Enter Image Name",
	 *   "error_code": {
	 *     "errorCode": 601,
	 *     "errorDescription": "Request processing failed"
	 *   }
	 * }
	 *      
	 * 
	 * in case of insufficient or invalid data following response is returned:
	 * 1) Invalid deployment type : XYZ
	 * 	{
	 *   "status": "Error",
	 *   "details": "Invalid deployment type for image",
	 *   "error_code": {
	 *     "errorCode": 600,
	 *     "errorDescription": "Validation failed"
	 *   }
	 * }
	 * 2) Invalid format 
	 * {
	 *   "status": "Error",
	 *   "details": "Invalid format for image. Valid formats are: qcow2, vhd(vpc), vmdk, raw, vdi",
	 *   "error_code": {
	 *     "errorCode": 600,
	 *     "errorDescription": "Validation failed"
	 *   }
	 * }
	 * </pre>
	 * 
	 * @param uploadRequest
	 *            object which includes metadata information
	 * @return Response object contains newly created image metadata along with
	 *         image_id
	 */
    @Path("images")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUploadImageMetadata(TrustDirectorImageUploadRequest uploadRequest) {

	TrustDirectorImageUploadResponse uploadImageToTrustDirector;

	String errors = uploadRequest.validate();
	if (StringUtils.isNotBlank(errors)) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.status = Constants.ERROR;
	    genericResponse.details = errors;
	    genericResponse.errorCode = ErrorCode.VALIDATION_FAILED;
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	
	// If the image is docker image we check that image with same repo and tag should not exist already 
	if (Constants.DEPLOYMENT_TYPE_DOCKER.equals(uploadRequest.image_deployments) && uploadRequest.image_size == 0) {
	    boolean doesRepoTagExistInDockerHub = false;
	    try {
		doesRepoTagExistInDockerHub = DockerUtil.doesRepoTagExistInDockerHub(uploadRequest.getRepository(),
			uploadRequest.getTag());
	    } catch (DirectorException e) {
		GenericResponse genericResponse = new GenericResponse();
		genericResponse.status = Constants.ERROR;
		genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
		genericResponse.details = e.getMessage();
		if (e instanceof ConnectionFailException) {
		    genericResponse.details = "Unable to connect to docker hub";
		    return Response.ok(genericResponse).status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.ok(genericResponse).status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }

	    if (!doesRepoTagExistInDockerHub) {
		uploadImageToTrustDirector = new TrustDirectorImageUploadResponse();
		uploadImageToTrustDirector.status = Constants.ERROR;
		uploadImageToTrustDirector.details = "Image with repo tag not available in docker hub";
		return Response.ok(uploadImageToTrustDirector).status(Response.Status.NOT_FOUND).build();
	    }
	}

	imageService = new ImageServiceImpl();
	String imageName = uploadRequest.image_name;
	// Imagename in case of docker if not sent in request we take repo:tag
	if (Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(uploadRequest.image_deployments)
		&& StringUtils.isBlank(imageName)) {
	    String repositoryInName = uploadRequest.repository;
	    imageName = repositoryInName.replace("/", "-") + ":" + uploadRequest.tag;
	}
	try {
	    if (Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(uploadRequest.image_deployments)
		    && dockerActionService.doesRepoTagExist(uploadRequest.repository, uploadRequest.tag)) {
		GenericResponse genericResponse = new GenericResponse();
		genericResponse.status = Constants.ERROR;
		genericResponse.details = "Image with Repo And Tag already exists";
		genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
		return Response.ok(genericResponse).build();
	    }

	    if (imageService.doesImageNameExist(imageName,uploadRequest.image_deployments)) {
		GenericResponse genericResponse = new GenericResponse();
		genericResponse.status = Constants.ERROR;
		genericResponse.details = "Image with Same Name already exists. <br>Please Enter Image Name";
		genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
		return Response.ok().entity(genericResponse).build();
	    }

	    uploadImageToTrustDirector = imageService.createUploadImageMetadataImpl(uploadRequest.image_deployments,
		    uploadRequest.image_format, imageName, uploadRequest.image_size, uploadRequest.repository,
		    uploadRequest.tag);
	    uploadImageToTrustDirector.status = Constants.SUCCESS;
	    log.info("Successfully uploaded image to location: " + uploadImageToTrustDirector.getLocation());
	    return Response.ok().entity(uploadImageToTrustDirector).build();
	} catch (DirectorException e) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.status = Constants.ERROR;
	    genericResponse.details = "Error in saving image metadata";
	    genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
	    return Response.ok(genericResponse).status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}

    }

    /**
     * API for uploading image data for the given image id. Before Uploading
     * image it is divided in chunks and sent to server one by one. Once the
     * chunk is received location to save image is retrieved from DB using given
     * image id and chunk is saved to that location.
     * 
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/rpc/images/content/C4C9E453-A864-4B14-8B72-9F9DF9406198
     * Input: chunk for image upload
     * Output: {
     *   "created_by_user_id": "admin",
     *   "created_date": "2016-05-16 08:53:08",
     *   "edited_by_user_id": "admin",
     *   "edited_date": "2016-05-16 08:56:15",
     *   "id": "C4C9E453-A864-4B14-8B72-9F9DF9406198",
     *   "image_name": "test11.img",
     *   "image_format": "qcow2",
     *   "image_deployments": "VM",
     *   "image_size": 13631488,
     *   "sent": 13631488,
     *   "deleted": false,
     *   "image_upload_status": "Complete",
     *   "image_Location": "/mnt/images/vm/"
     * }
     * 
     * While the image upload is in progress:
     * {
     *   "created_by_user_id": "admin",
     *   "created_date": "2016-05-16 08:53:08",
     *   "edited_by_user_id": "admin",
     *   "edited_date": "2016-05-16 08:53:50",
     *   "id": "C4C9E453-A864-4B14-8B72-9F9DF9406198",
     *   "image_name": "test11.img",
     *   "image_format": "qcow2",
     *   "image_deployments": "VM",
     *   "image_size": 13631488,
     *   "sent": 0,
     *   "deleted": false,
     *   "image_upload_status": "In Progress",
     *   "image_Location": "/mnt/images/docker/"
     * }
     * 
     * In case of Docker: 
     * 
     * 
     * {
     *   "created_by_user_id": "admin",
     *   "created_date": "2016-05-16 07:20:40",
     *   "edited_by_user_id": "admin",
     *   "edited_date": "2016-05-16 08:50:53",
     *   "id": "D3341CA4-1F3A-4CD7-B9BA-710F363A3CD8",
     *   "image_name": "debian:latest",
     *   "image_format": "tar",
     *   "image_deployments": "Docker",
     *   "image_size": 1322496,
     *   "sent": 0,
     *   "deleted": false,
     *   "repository": "debian",
     *   "tag": "latest",
     *   "image_upload_status": "In Progress",
     *   "image_Location": "/mnt/images/docker/"
     * }
     * In case of docker, after the call to upload image content another call needs to be done:  https://HOST:PORT/v1/rpc/docker-setup/UUID_OF_IMAGE
     *                    </pre>
     * 
     * @param imageId
     *            - id received as response of https://{IP/HOST_NAME}/v1/images/
     *            request
     * @param filInputStream
     *            - image data sent as chunk
     * @return TrustDirectorImageUploadResponse object with updated image upload
     *         metadata
     * @throws DirectorException
     */
    @Path("rpc/images/content/{imageId: [0-9a-zA-Z_-]+}")
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadImageToTrustDirector(@PathParam("imageId") String imageId, InputStream filInputStream) {
	log.info("Uploading image to TDaaS");
	imageService = new ImageServiceImpl();
	TrustDirectorImageUploadResponse uploadImageToTrustDirector = new TrustDirectorImageUploadResponse();
	if (!ValidationUtil.isValidWithRegex(imageId, RegexPatterns.UUID)) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.details = "Imaged id is empty or not in uuid format";
	    genericResponse.status = Constants.ERROR;
	    genericResponse.setErrorCode(ErrorCode.INAVLID_ID);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	try {
	    long lStartTime = new Date().getTime();

	    uploadImageToTrustDirector = imageService.uploadImageToTrustDirector(imageId, filInputStream);
	    log.info("Successfully uploaded image to location: " + uploadImageToTrustDirector.getLocation());
	    long lEndTime = new Date().getTime();

	    long difference = lEndTime - lStartTime;
	    log.info("Time taken to upload image to TD: " + difference);
	    filInputStream.close();
	    Session session = SecurityUtils.getSubject().getSession();
	    session.touch();
	    return Response.ok(uploadImageToTrustDirector).build();
	} catch (DirectorException | IOException e) {
	    log.error("Error while uploading image to Trust Director", e);
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.details = e.getMessage();
	    genericResponse.status = Constants.ERROR;
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(genericResponse).build();
	}
    }

    /**
     * Returns list of images in TD depending on the image deployment type
     * supplied. This call is made so that grids on the UI for VM and Hosts can
     * be populated. Each image has an image deployment type : BareMetal or VM.
     * 
     * This method gets the list of images based on the deployment type provided
     * as a query param. Providing the deployment type is optional. If provided
     * the value should be VM or BareMetal. edited_date field is updated in case
     * of mounting and delete.image_upload_status can have values Incomplete and
     * Complete. Since the upload happens in multiple steps, the status is
     * Incomplete till the sent field is equal to image_size. image_location is
     * for internal server use and it can't accessed it directly.
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/images
     * Input: deploymentType=VM (Example : https://{IP/HOST_NAME}/v1/images?deploymentType=VM)
     * 
     * Output:
     * 	
     * 			{
     * 			"images": [
     * 			{
     * 			  "created_by_user_id": "admin",
     * 			  "created_date": "2016-05-03 20:40:17",
     * 			  "edited_by_user_id": "admin",
     * 			  "edited_date": "2016-05-03 20:40:17",
     * 			  "id": "D67DE0A7-9FED-48B1-894E-4E345AD3475F",
     * 			  "image_name": "debian:testing",
     * 			  "image_format": "tar",
     * 			  "image_deployments": "Docker",
     * 			  "image_size": 0,
     * 			  "sent": 0,
     * 			  "deleted": false,
     * 			  "repository": "debian",
     * 			  "tag": "testing",
     * 			  "image_uploads_count": 0,
     * 			  "policy_uploads_count": 0,
     * 			  "policy_name": "-",
     * 			  "action_entry_created": false,
     * 			  "image_upload_status": "In Progress",
     * 			  "image_Location": "/mnt/images/docker/"
     * 			},
     * 			{
     * 			  "created_by_user_id": "admin",
     * 			  "created_date": "2016-05-02 05:45:44",
     * 			  "edited_by_user_id": "admin",
     * 			  "edited_date": "2016-05-01 22:26:17",
     * 			  "id": "1AFBA2F5-C02E-420E-9842-C455BB35B334",
     * 			  "image_name": "cirros.img",
     * 			  "image_format": "qcow2",
     * 			  "image_deployments": "VM",
     * 			  "image_size": 13287936,
     * 			  "sent": 13287936,
     * 			  "deleted": false,
     * 			  "upload_variable_md5": "110db09830b79d60c9faf056d7bdb2f5",
     * 			  "trust_policy_id": "ACE7EA06-34EE-412B-8505-83FD58AD79AD",
     * 			  "image_uploads_count": 0,
     * 			  "policy_uploads_count": 0,
     * 			  "policy_name": "cirros.img",
     * 			  "action_entry_created": true,
     * 			  "image_upload_status": "Complete",
     * 			  "image_Location": "/mnt/images/vm/"
     * 			},
     * 			{
     * 			  "created_by_user_id": "admin",
     * 			  "created_date": "2016-05-02 05:48:28",
     * 			  "edited_by_user_id": "admin",
     * 			  "edited_date": "2016-05-03 01:15:25",
     * 			  "id": "9C64F16D-782C-4F24-8880-9A9F1EE6794D",
     * 			  "image_name": "c2",
     * 			  "image_format": "qcow2",
     * 			  "image_deployments": "VM",
     * 			  "image_size": 13287936,
     * 			  "sent": 13287936,
     * 			  "deleted": false,
     * 			  "image_uploads_count": 0,
     * 			  "policy_uploads_count": 0,
     * 			  "policy_name": "-",
     * 			  "action_entry_created": false,
     * 			  "image_upload_status": "Complete",
     * 			  "image_Location": "/mnt/images/vm/"
     * 			},
     * 			{
     * 			  "created_date": "2016-05-03 19:30:10",
     * 			  "edited_by_user_id": "admin",
     * 			  "edited_date": "2016-05-03 19:36:44",
     * 			  "id": "EC45AC23-0A64-41E7-8083-A339E29F4A56",
     * 			  "image_name": "10.35.35.131",
     * 			  "image_deployments": "BareMetal",
     * 			  "image_size": 0,
     * 			  "deleted": false,
     * 			  "trust_policy_draft_id": "9268e9cc-cfc2-43ce-9847-133e96ff0c54",
     * 			  "image_uploads_count": 0,
     * 			  "policy_uploads_count": 0,
     * 			  "policy_name": "P1",
     * 			  "action_entry_created": false,
     * 			  "image_upload_status": "Complete"
     * 			},
     * 			{
     * 			  "created_date": "2016-05-03 19:37:02",
     * 			  "edited_by_user_id": "admin",
     * 			  "edited_date": "2016-05-03 20:37:54",
     * 			  "id": "EB7E45CD-F84C-419E-9841-A685E8E28050",
     * 			  "image_name": "10.35.35.131",
     * 			  "image_deployments": "BareMetal",
     * 			  "image_size": 0,
     * 			  "deleted": false,
     * 			  "trust_policy_draft_id": "f08b2b01-7fe7-4ea1-ba8c-4883b366253a",
     * 			  "image_uploads_count": 0,
     * 			  "policy_uploads_count": 0,
     * 			  "policy_name": "P2",
     * 			  "action_entry_created": false,
     * 			  "image_upload_status": "Complete"
     * 			},
     * 			{
     * 			  "created_by_user_id": "admin",
     * 			  "created_date": "2016-05-03 20:38:44",
     * 			  "edited_by_user_id": "admin",
     * 			  "edited_date": "2016-05-03 20:39:39",
     * 			  "id": "960ECC0E-8DDF-4FDB-B788-A2855F9E799B",
     * 			  "image_name": "busybox:latest",
     * 			  "image_format": "tar",
     * 			  "image_deployments": "Docker",
     * 			  "image_size": 1322496,
     * 			  "sent": 1322496,
     * 			  "deleted": false,
     * 			  "repository": "busybox",
     * 			  "tag": "latest",
     * 			  "image_uploads_count": 0,
     * 			  "policy_uploads_count": 0,
     * 			  "policy_name": "-",
     * 			  "action_entry_created": false,
     * 			  "image_upload_status": "Complete",
     * 			  "image_Location": "/mnt/images/docker/"
     * 			}
     * 			]
     * 			}
     * 
     *                    </pre>
     * 
     * @param deployment_type
     *            - VM/BareMetal
     * @return List of image details
     * @throws DirectorException
     */
    @Path("images")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImagesByDeploymentType(@QueryParam("deploymentType") String deployment_type)
	    throws DirectorException {
	SearchImagesRequest searchImagesRequest = new SearchImagesRequest();
	if (!CommonValidations.validateImageDeployments(deployment_type)) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.error = "Incorrect deployment_type. Valid types are BareMetal or VM";
	    genericResponse.setErrorCode(ErrorCode.VALIDATION_FAILED);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	SearchImagesResponse searchImagesResponse = new SearchImagesResponse();

	searchImagesRequest.deploymentType = deployment_type;
	searchImagesResponse = imageService.searchImages(searchImagesRequest);
	return Response.ok(searchImagesResponse).build();

    }

    /**
     * This method provides image details if image corresponding to the imageid
     * exists. In case of baremetallive it also returns host and user name along
     * with other image details. if image corresponding to imageId doesn't exist
     * it responds with 404 No found
     * 
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/images/465A8B27-7CC8-4A3C-BBBC-26161E3853CD
     * Input: imageId : 465A8B27-7CC8-4A3C-BBBC-26161E3853CD
     * Output:
     * {
     *   "created_by_user_id": "admin",
     *   "created_date": "2016-05-02 05:45:44",
     *   "edited_by_user_id": "admin",
     *   "edited_date": "2016-05-02 10:26:17",
     *   "id": "1AFBA2F5-C02E-420E-9842-C455BB35B334",
     *   "image_name": "cirros.img",
     *   "image_format": "qcow2",
     *   "image_deployments": "VM",
     *   "image_size": 13287936,
     *   "sent": 13287936,
     *   "deleted": false,
     *   "upload_variable_md5": "110db09830b79d60c9faf056d7bdb2f5",
     *   "trust_policy_id": "ACE7EA06-34EE-412B-8505-83FD58AD79AD",
     *   "image_upload_status": "Complete",
     *   "image_Location": "/mnt/images/vm/"
     * }
     * 
     * 	Docker image response: 
     * 	{
     *   "created_by_user_id": "admin",
     *   "created_date": "2016-05-04 08:40:17",
     *   "edited_by_user_id": "admin",
     *   "edited_date": "2016-05-04 08:40:17",
     *   "id": "D67DE0A7-9FED-48B1-894E-4E345AD3475F",
     *   "image_name": "debian:testing",
     *   "image_format": "tar",
     *   "image_deployments": "Docker",
     *   "image_size": 0,
     *   "sent": 0,
     *   "deleted": false,
     *   "repository": "debian",
     *   "tag": "testing",
     *   "image_upload_status": "In Progress",
     *   "image_Location": "/mnt/images/docker/"
     * }
     * 
     * Bare metal response; 
     * {
     *   "created_date": "2016-05-04 07:30:10",
     *   "edited_by_user_id": "admin",
     *   "edited_date": "2016-05-04 07:36:44",
     *   "id": "EC45AC23-0A64-41E7-8083-A339E29F4A56",
     *   "image_name": "10.35.35.131",
     *   "image_deployments": "BareMetal",
     *   "image_size": 0,
     *   "deleted": false,
     *   "trust_policy_draft_id": "9268e9cc-cfc2-43ce-9847-133e96ff0c54",
     *   "ip_address": "10.35.35.131",
     *   "username": "root",
     *   "image_upload_status": "Complete"
     * }
     * 
     * 
     * 
     * When image corresponding to imageid do not exist it gives 404 Not Found
     *                    </pre>
     * 
     * @param imageId
     *            The id of the image for which the details are requested
     * @return image details in JSON format
     * @throws DirectorException
     */
    @Path("images/{imageId: [0-9a-zA-Z_-]+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImageDetails(@PathParam("imageId") String imageId) throws DirectorException {
	if (!ValidationUtil.isValidWithRegex(imageId, RegexPatterns.UUID)) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.error = "Imaged id is empty or not in uuid format";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	ImageInfoResponse imageInfoResponse = new ImageInfoResponse();

	imageInfoResponse = imageService.getImageDetails(imageId);
	if (imageInfoResponse == null) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.error = "Unable to find image buy id: " + imageId;
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.NOT_FOUND).entity(genericResponse).build();
	}
	return Response.ok(imageInfoResponse).build();

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
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/rpc/mount-image
     * Input: {"id" : "465A8B27-7CC8-4A3C-BBBC-26161E3853CD"} 
     * Output: 
     * 
     * Docker image mount respose:
     * {
     * 	"created_by_user_id": "admin",
     * 	"created_date": "2016-05-04 08:38:44",
     * 	"edited_by_user_id": "admin",
     * 	"edited_date": "2016-05-04 08:45:31",
     * 	"id": "960ECC0E-8DDF-4FDB-B788-A2855F9E799B",
     * 	"image_name": "busybox:latest",
     * 	"image_format": "tar",
     * 	"image_deployments": "Docker",
     * 	"image_size": 1322496,
     * 	"sent": 1322496,
     * 	"mounted_by_user_id": "admin",
     * 	"deleted": false,
     * 	"repository": "busybox",
     * 	"tag": "latest",
     * 	"image_upload_status": "Complete",
     * 	"image_Location": "/mnt/images/docker/"
     * 	}
     * 	
     * 	VM mount response
     * 	{
     * 	"created_by_user_id": "admin",
     * 	"created_date": "2016-05-02 05:48:28",
     * 	"edited_by_user_id": "admin",
     * 	"edited_date": "2016-05-04 08:47:17",
     * 	"id": "9C64F16D-782C-4F24-8880-9A9F1EE6794D",
     * 	"image_name": "c2",
     * 	"image_format": "qcow2",
     * 	"image_deployments": "VM",
     * 	"image_size": 13287936,
     * 	"sent": 13287936,
     * 	"mounted_by_user_id": "admin",
     * 	"deleted": false,
     * 	"image_upload_status": "Complete",
     * 	"image_Location": "/mnt/images/vm/"
     * 	}
     * 
     * 
     * 
     * 
     * If the user tries to mount an image which, for some reason, has been removed from the uploaded location, the response will look like :
     * {"error": "No image found with id: BAA5747D-B2ED-4E7D-A4D5-0256DEE7FBB1","error_code":{"errorCode":602,"errorDescription":"Invalid ID"}}
     * 
     * If user tries to mount deleted image, the response will look like:
     * {"error":"Cannot launch an image marked as deleted or incomplete image BCD6A45B-185B-4744-9E6F-BB0DF79AC6AB","error_code":{"errorCode":601,"errorDescription":"Request processing failed"}}
     * 
     *                    </pre>
     * 
     * @param httpServletRequest
     * @param httpServletResponse
     * @return MountImageResponse containing the details of the mount process.
     */
    @Path("rpc/mount-image")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response mountImage(MountImageRequest mountImage, @Context HttpServletRequest httpServletRequest,
	    @Context HttpServletResponse httpServletResponse) {
	MountImageResponse mountImageResponse = new MountImageResponse();
	String error = mountImage.validate();
	if (StringUtils.isNotBlank(error)) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setError(error);
	    genericResponse.setErrorCode(ErrorCode.VALIDATION_FAILED);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	ImageInfo fetchImageById = null;
	try {
	    fetchImageById = imageService.fetchImageById(mountImage.id);
	} catch (DirectorException e1) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setError(e1.getMessage());
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	if (fetchImageById == null) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setError("Invalid image id provided");
	    genericResponse.setErrorCode(ErrorCode.INAVLID_ID);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();

	}
	log.info("inside mounting image in web service");
	String user = ShiroUtil.subjectUsername();
	log.info("User mounting image : " + user);
	try {
	    mountImageResponse = imageService.mountImage(mountImage.id, user);
	} catch (DirectorException e) {
	    log.error("Error while Mounting the Image",e);
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setError(e.getMessage());
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.ok(genericResponse).build();
	}
	return Response.ok(mountImageResponse).build();
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
     * set to NULL again. the unmount process in the service.
     * 
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * 
     *                    <pre>
     *  https://{IP/HOST_NAME}/v1/rpc/unmount-image
     *  Input: {id : "465A8B27-7CC8-4A3C-BBBC-26161E3853CD"} 
     *  Output: 
     *  In case of VM:
     *  {
     * 		  "created_by_user_id": "admin",
     * 		  "created_date": "2016-05-02 05:48:28",
     * 		  "edited_by_user_id": "admin",
     * 		  "edited_date": "2016-05-04 08:50:32",
     * 		  "id": "9C64F16D-782C-4F24-8880-9A9F1EE6794D",
     * 		  "image_name": "c2",
     * 		  "image_format": "qcow2",
     * 		  "image_deployments": "VM",
     * 		  "image_size": 13287936,
     * 		  "sent": 13287936,
     * 		  "deleted": false,
     * 		  "upload_variable_md5":"39b61f1d3234565bfa6fc585ee8177f2",
     * 		  "image_upload_status": "Complete",
     * 		  "image_Location": "/mnt/images/vm/"
     * 		}
     * 		
     * 		Docker: 
     * 		{
     * 		  "created_by_user_id": "admin",
     * 		  "created_date": "2016-05-04 08:40:17",
     * 		  "edited_by_user_id": "admin",
     * 		  "edited_date": "2016-05-04 08:51:34",
     * 		  "id": "D67DE0A7-9FED-48B1-894E-4E345AD3475F",
     * 		  "image_name": "debian:testing",
     * 		  "image_format": "tar",
     * 		  "image_deployments": "Docker",
     * 		  "image_size": 0,
     * 		  "sent": 0,
     * 		  "deleted": false,
     * 		   "upload_variable_md5":"48b6171d3234565bfa6fc585ee8177f2",
     * 		  "repository": "debian",
     * 		  "tag": "testing",
     * 		  "image_upload_status": "Complete",
     * 		  "image_Location": "/mnt/images/docker/"
     * 		}
     *  
     *  In case of error:
     * {"id":"2C0EFEF6-A3C4-4FC6-9CEC-A679FF1F74","deleted":false,"error":"Image Id is empty or is not in uuid format","error_code":{"errorCode":600,"errorDescription":"Validation failed"}}
     * 		OR in case of invalid image
     * 		{"id":"2C0EFEF6-A3C4-4FC6-9CEC-A679FF1F743a","deleted":false,"error":"Invalid image id provided","error_code":{"errorCode":602,"errorDescription":"Invalid ID"}}
     * 
     * 
     *                    </pre>
     * 
     * @param httpServletRequest
     * @param httpServletResponse
     * @return UnmountImageResponse containing the details of the unmount
     * @throws DirectorException
     */
    @Path("rpc/unmount-image")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response unMountImage(MountImageRequest unmountimage, @Context HttpServletRequest httpServletRequest,
	    @Context HttpServletResponse httpServletResponse) {
	String user = ShiroUtil.subjectUsername();
	String error = unmountimage.validate();
	if (StringUtils.isNotBlank(error)) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setError(error);
	    genericResponse.setErrorCode(ErrorCode.VALIDATION_FAILED);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	ImageInfo fetchImageById = null;
	try {
	    fetchImageById = imageService.fetchImageById(unmountimage.id);
	} catch (DirectorException e1) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setError(e1.getMessage());
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	if (fetchImageById == null) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setError("Invalid image id provided");
	    genericResponse.setErrorCode(ErrorCode.INAVLID_ID);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	UnmountImageResponse unmountImageResponse = new UnmountImageResponse();

	try {
	    unmountImageResponse = imageService.unMountImage(unmountimage.id, user);
	} catch (Exception e) {
	    log.error("Error while unmounting image ", e);
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setError(e.getMessage());
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(genericResponse).build();
	}
	return Response.ok(unmountImageResponse).build();
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
     * "patch_xml" attribute. Once the UI receives it, it adds it to the current
     * selections on the UI and then sends back the consolidated patch to the
     * server
     * 
     * 
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/images/08EB37D7-2678-495D-B485-59233EB51996/search
     * Input: QueryPAram : dir=/boot/&recursive=false&files_for_policy=false&init=false&include_recursive=false&reset_regex=false
     * 
     * https://10.35.35.133/v1/images/E4770A39-024D-4A2A-989E-7EE1123E8204/search?dir=/&recursive=false&files_for_policy=false&init=true&include_recursive=false&reset_regex=false
     * 
     * output: {"tree_content":"<Html containing the nested ul and li tags>", "patch_xml":"<patch><list of add remove tags as per the operation></patch>"}
     * 
     * The output tag has the patch_xml set only in case in the following cases of the query parameters:
     * 1) recursive=true and files_for_policy=true
     * 2) recursive=true and files_for_policy=false
     * 3) reset_regex = true
     * 4) include="<regex expression>" & exclude="<regex expression>" with optional include_regex=true
     * 
     *                    </pre>
     * 
     * @param imageId
     *            Id of the image which is mounted and whose files are being
     *            browsed
     * @param uriInfo
     *            Request containing the options selected by the user on the
     *            tree. It contains flags like include/exclude flags for regex
     *            filter, select all flag, init flag for first time load
     * @return returns HTML representation of the tree and the patch in some
     *         cases like regex and select all.
     * 
     * 
     * @throws DirectorException
     */

    @Path("images/{imageId: [0-9a-zA-Z_-]+}/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchFilesInImage(@PathParam("imageId") String imageId, @Context UriInfo uriInfo) {
	imageService = new ImageServiceImpl();
	SearchFilesInImageRequest searchFilesInImageRequest = new TdaasUtil()
		.mapUriParamsToSearchFilesInImageRequest(uriInfo);
	searchFilesInImageRequest.id = imageId;
	if (!ValidationUtil.isValidWithRegex(imageId, RegexPatterns.UUID)) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.error = "Imaged id is empty or not in uuid format";
	    genericResponse.setErrorCode(ErrorCode.INAVLID_ID);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}

	ImageInfo fetchImageById = null;
	try {
	    fetchImageById = imageService.fetchImageById(imageId);
	} catch (DirectorException e2) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.error = "Error fetching image by id : " + imageId;
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	if (fetchImageById == null) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.error = "Invalid image id provided";
	    genericResponse.setErrorCode(ErrorCode.INAVLID_ID);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}

	String mountPath = TdaasUtil.getMountPath(imageId);
	File f = new File(mountPath);
	if (!f.exists()) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    genericResponse.error = "Image not mounted";
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	SearchFilesInImageResponse filesInImageResponse = new SearchFilesInImageResponse();

	try {
	    filesInImageResponse = imageService.searchFilesInImage(searchFilesInImageRequest);
	} catch (DirectorException e) {
	    log.error("Error while searching for files in image : " + imageId, e);
	    try {
		imageService.unMountImage(imageId, null);
	    } catch (DirectorException e1) {
		log.error("Error while unmounting image  : " + imageId, e);
	    }
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    genericResponse.error = "Error doing search operation";

	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(genericResponse).build();

	}
	String join = StringUtils.join(filesInImageResponse.files, "");
	filesInImageResponse.treeContent = join;

	// return join;
	return Response.ok(filesInImageResponse).build();
    }
    
    
    
    

    /**
	 * API for for upgrading policy from older schema 1.1 to newer schema 1.2. This newer schema supports symlinks and have recursive flag 
	 * removed from Dir. The api returns draft which can be further used to create policy. 
	
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType PATCH
	 * @mtwSampleRestCall
	 * 
	 *<pre>
	 * https://{IP/HOST_NAME}/v1/images/35D7EF4E-D7EA-41D5-9F05-E0AF182D7F3B/upgradePolicy
	 * 
	 * Output:
	 * {"status":"success","message":"Policy Draft Succesfully upgraded to 1.2","policy_draft":"<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns2:TrustPolicy xmlns:ns2=\"mtwilson:trustdirector:policy:1.2\" .........</ns2:TrustPolicy>"}
	 * 
	 * In case the policy is already of 1.2 version:
	 * {"status":"success","message":"Policy Draft already of version 1.2, no action done","policy_draft":"<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns2:TrustPolicy xmlns:ns2=\"mtwilson:trustdirector:policy:1.2\" .........</ns2:TrustPolicy>"}
	 * </pre>
	 * 
	 * @param imageId
     *            - imageid in url
	 * @return Response object contains newly upgrade policy draft
	 */ 
    @Path("images/{imageId: [0-9a-zA-Z_-]+}/upgradePolicy")
    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
	public Response upgradePolicyForImage(@PathParam("imageId") String imageId) {
		UpgradeTrustPolicyResponse upgardeResponse = new UpgradeTrustPolicyResponse();
		try {
			upgardeResponse = imageService.upgradePolicyForImage(imageId);
		} catch (DirectorException e) {
			upgardeResponse.status = "error";
			upgardeResponse.message = e.getMessage();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(upgardeResponse).build();
		}
		return Response.ok(upgardeResponse).build();

	}
    
    
    
    
    
    
    

    /**
     * Retrieves list of deployment types - VM,BareMetal and Docker are the
     * types returned as JSON
     * 
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/image-deployments
     * Input: None
     * Output: 
     * {
     * 		  "image_deployments": [
     * 		    {
     * 		      "name": "VM",
     * 		      "display_name": "Virtualized Server"
     * 		    },
     * 		    {
     * 		      "name": "BareMetal",
     * 		      "display_name": "Non-Virtualized Server"
     * 		    },
     * 		    {
     * 		      "name": "Docker",
     * 		      "display_name": "Docker"
     * 		    }
     * 		  ]
     * 		}
     *                    </pre>
     * 
     * @return list of deployment types
     */
    @Path("image-deployments")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ListImageDeploymentsResponse getImageDeployments() {
	return lookupService.getImageDeployments();
    }

    /**
     * Lookup method to fetch the image formats. Currently we return qcow2, vhd,
     * vmdk, raw, vdi as JSON
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/image-formats
     * Input: None
     * Output: 
     * {
     * 		  "image_formats": [
     * 		    {
     * 		      "name": "qcow2",
     * 		      "display_name": "qcow2"
     * 		    },
     * 		    {
     * 		      "name": "vhd",
     * 		      "display_name": "vhd"
     * 		    },
     * 		    {
     * 		      "name": "vmdk",
     * 		      "display_name": "vmdk"
     * 		    },
     * 		    {
     * 		      "name": "raw",
     * 		      "display_name": "raw"
     * 		    },
     * 		    {
     * 		      "name": "vdi",
     * 		      "display_name": "vdi"
     * 		    }
     * 		  ]
     * 		}
     *                    </pre>
     * 
     * @return list of image formats
     */
    @Path("image-formats")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ListImageFormatsResponse getImageFormats() {
	return lookupService.getImageFormats();
    }

    /**
     * lookup method to fetch the launch policies. The current launch policies
     * that are returned are MeasureOnly and MeasureAndEnforce If not
     * deploymentType is provided, all image launch policies are returned. I the
     * user wants to fetch launch specific to a deployment type, the possible
     * values for deployment type are: 1) VM 2) BareMetal
     * 
     * 
     * @return launch policy list
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/image-launch-policies
     * Input: QueryParam String deploymentType=VM
     * deploymentType can be VM , BareMetal or Docker
     * Output:
     * {
     * 		  "image_launch_policies": [
     * 		    {
     * 		      "name": "MeasureOnly",
     * 		      "display_name": "Hash Only",
     * 		      "image_deployments": [
     * 		        "VM",
     * 		        "BareMetal",
     * 		        "Docker"
     * 		      ]
     * 		    },
     * 		    {
     * 		      "name": "MeasureAndEnforce",
     * 		      "display_name": "Hash and enforce",
     * 		      "image_deployments": [
     * 		        "VM",
     * 		        "Docker"
     * 		      ]
     * 		    }
     * 		  ]
     * 		}
     * 		
     * 		If VM is given as a deploymentType query parameter, response is :
     * 		{
     * 		  "image_launch_policies": [
     * 		    {
     * 		      "name": "MeasureOnly",
     * 		      "display_name": "Hash Only",
     * 		      "image_deployments": [
     * 		        "VM",
     * 		        "BareMetal",
     * 		        "Docker"
     * 		      ]
     * 		    },
     * 		    {
     * 		      "name": "MeasureAndEnforce",
     * 		      "display_name": "Hash and enforce",
     * 		      "image_deployments": [
     * 		        "VM",
     * 		        "Docker"
     * 		      ]
     * 		    }
     * 		  ]
     * 		}
     * 		
     * 		valid deplopymentType values are Docker, VM and BareMetal
     *                    </pre>
     */
    @Path("image-launch-policies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImageLaunchPoliciesList(@QueryParam("deploymentType") String deploymentType) {
	if (!CommonValidations.validateImageDeployments(deploymentType)) {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.error = "Incorrect deployment_type";
	    genericResponse.errorCode = ErrorCode.VALIDATION_FAILED;
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	return Response.ok(lookupService.getImageLaunchPolicies(deploymentType)).build();
    }

    /**
     * 
     * Method lets the user download the policy from the grids page. The user
     * can visit the grid any time and download the policy. This method looks
     * into the MW_TRUST_POLICY table and gets the policy string and sends it as
     * an xml content to the user
     * 
     * In case the policy is not found for the image id, HTTP 404 is returned
     * 
     * https://{IP/HOST_NAME}/v1/images/
     * 08EB37D7-2678-495D-B485-59233EB51996/downloads/policy
     * 
     * </pre>
     * 
     * @mtwContentTypeReturned XML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * Input: Image id as path param
     * Output: xml
     * 
     *                    </pre>
     * 
     * @param imageId
     *            the image for which the policy is downloaded
     * @return XML content of the policy
     * @throws DirectorException
     */
    @Path("images/{imageId: [0-9a-zA-Z_-]+}/downloads/policy")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response downloadPolicyForImageId(@PathParam("imageId") String imageId) {
	TrustPolicy policy = null;
	GenericResponse genericResponse = new GenericResponse();
	if (!ValidationUtil.isValidWithRegex(imageId, RegexPatterns.UUID)) {
	    genericResponse.error = "Imaged id is empty or not in uuid format";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}

	ImageInfo imageInfo = null;
	try {
	    imageInfo = imageService.fetchImageById(imageId);
	} catch (DirectorException e1) {
	    log.error("Unable to fetch image", e1);
	}
	if (imageInfo == null) {
	    genericResponse.error = "No image with id : " + imageId + " exists.";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.NOT_FOUND).entity(genericResponse).build();
	}

	String trust_policy_id = imageInfo.getTrust_policy_id();
	TrustPolicy trustPolicyByTrustId = imageService.getTrustPolicyByTrustId(trust_policy_id);

	if (trustPolicyByTrustId == null) {
	    genericResponse.error = "No trust policy exists for image with id : " + imageId + " exists.";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.NOT_FOUND).entity(genericResponse).build();
	}

	try {
	    policy = imageService.getTrustPolicyByImageId(imageId);
	} catch (DirectorException e) {
	    String msg = "Unable to fetch trust policy for image id : " + imageId;
	    log.error(msg, e);
	    log.info("Returning HTTP 404");
	}
	if (policy == null) {
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Status.NOT_FOUND).entity(genericResponse).build();
	}

	ResponseBuilder response = Response.ok(policy.getTrust_policy());
	response.header("Content-Disposition",
		"attachment; filename=policy_" + policy.getImgAttributes().getImage_name() + ".xml");
	return response.build();
    }

    /**
     * 
     * Method lets the user download the manifest from the grids page. The user
     * can visit the grid any time and download the policy. This method looks
     * into the MW_TRUST_POLICY table and gets the policy string and sends it as
     * an xml content to the user
     * 
     * In case the policy is not found for the image id, HTTP 404 is returned
     * 
     * https://{IP/HOST_NAME}/v1/images/
     * 08EB37D7-2678-495D-B485-59233EB51996/downloads/manifest Input: Image id
     * as path param Output: XML content sent as stream
     * 
     * @mtwContentTypeReturned XML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * Input: Image id as path param
     * Output: xml
     * 
     *                    </pre>
     * 
     * @param imageId
     *            the image for which the policy is downloaded
     * @return XML content of the policy
     * @throws DirectorException
     */
    @Path("images/{imageId: [0-9a-zA-Z_-]+}/downloads/manifest")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response downloadManifestForImageId(@PathParam("imageId") String imageId) {
	TrustPolicy policy = null;
	GenericResponse genericResponse = new GenericResponse();
	if (!ValidationUtil.isValidWithRegex(imageId, RegexPatterns.UUID)) {
	    genericResponse.error = "Imaged id is empty or not in uuid format";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}

	ImageInfo imageInfo = null;
	try {
	    imageInfo = imageService.fetchImageById(imageId);
	} catch (DirectorException e1) {
	    log.error("Unable to fetch image", e1);
	}
	if (imageInfo == null) {
	    genericResponse.error = "No image with id : " + imageId + " exists.";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.NOT_FOUND).entity(genericResponse).build();
	}

	String trust_policy_id = imageInfo.getTrust_policy_id();
	TrustPolicy trustPolicyByTrustId = imageService.getTrustPolicyByTrustId(trust_policy_id);

	if (trustPolicyByTrustId == null) {
	    genericResponse.error = "No trust policy exists for image with id : " + imageId + " exists.";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.NOT_FOUND).entity(genericResponse).build();
	}

	try {
	    policy = imageService.getTrustPolicyByImageId(imageId);
	} catch (DirectorException e) {
	    String msg = "Unable to fetch trust policy for image id : " + imageId;
	    log.error(msg, e);
	    log.info("Returning HTTP 404");
	}
	if (policy == null) {
	    genericResponse.error = "No trust policy exists for image with id : " + imageId;
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Status.NOT_FOUND).entity(genericResponse).build();
	}
	String manifestForPolicy;
	try {
	    manifestForPolicy = TdaasUtil.getManifestForPolicy(policy.getTrust_policy());
	} catch (JAXBException e) {
	    genericResponse.error = "Error converting trust policy into manifest.xml" + imageId;
	    genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
	    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(genericResponse).build();
	}

	ResponseBuilder response = Response.ok(manifestForPolicy);
	response.header("Content-Disposition",
		"attachment; filename=manifest_" + policy.getImgAttributes().getImage_name() + ".xml");
	return response.build();
    }

    /**
     * 
     * Method lets the user download the policy and manifest as a tarball from
     * the grids page. The user can visit the grid any time and download the
     * policy and manifest as it was created in the wizrd. This method looks
     * into the MW_TRUST_POLICY table and gets the policy string, creates a
     * manifest and sends it as an tarball content to the user
     * 
     * In case the policy is not found for the image id, HTTP 404 is returned
     * 
     * @mtwContentTypeReturned File
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/images/08EB37D7-2678-495D-B485-59233EB51996/downloads/policyAndManifest
     * Input: Image UUID
     * Output: Content of tarball as stream
     *                    </pre>
     * 
     * @param imageId
     *            the image for which the policy and manifest is downloaded
     * @return TAR ball content of the policy
     * @throws DirectorException
     */
    @Path("images/{imageId: [0-9a-zA-Z_-]+}/downloads/policyAndManifest")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPolicyAndManifestForImageId(@PathParam("imageId") final String imageId) {

	File tarBall;
	GenericResponse genericResponse = new GenericResponse();
	if (!ValidationUtil.isValidWithRegex(imageId, RegexPatterns.UUID)) {
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    genericResponse.error = "Imaged id is empty or not in uuid format";
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}

	ImageInfo imageInfo = null;
	try {
	    imageInfo = imageService.fetchImageById(imageId);
	} catch (DirectorException e1) {
	    log.error("Unable to fetch image", e1);
	}
	if (imageInfo == null) {
	    genericResponse.error = "No image with id : " + imageId + " exists.";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.NOT_FOUND).entity(genericResponse).build();
	}

	String trust_policy_id = imageInfo.getTrust_policy_id();
	TrustPolicy trustPolicyByTrustId = imageService.getTrustPolicyByTrustId(trust_policy_id);

	if (trustPolicyByTrustId == null) {
	    genericResponse.error = "No trust policy exists for image with id : " + imageId + " exists.";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}

	try {
	    tarBall = imageService.createTarballOfPolicyAndManifest(imageId);
	} catch (DirectorException e) {
	    log.error("dowload policy and manifest failed", e);
	    tarBall = null;
	}
	if (tarBall == null) {
	    genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
	    return Response.status(Status.NOT_FOUND).entity(genericResponse).build();
	}
	FileInputStream tarInputStream = null;
	try {
	    tarInputStream = new FileInputStream(tarBall) {
		@Override
		public void close() throws IOException {
		    log.info("Deleting temporary files " + Constants.TARBALL_PATH + imageId);
		    super.close();
		    FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
		    final File dir = new File(Constants.TARBALL_PATH + imageId);
		    fileUtilityOperation.deleteFileOrDirectory(dir);
		}
	    };
	} catch (FileNotFoundException e) {
	    log.error("Error while dowloading policy and manifest", e);
	    genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
	    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(genericResponse).build();
	}
	ResponseBuilder response = Response.ok(tarInputStream);

	response.header("Content-Disposition", "attachment; filename=" + tarBall.getName());

	Response downloadResponse = response.build();
        try{
            tarInputStream.close();
        }catch(IOException ex){
            log.debug("error during closing tarInputStream",ex);
        }
	return downloadResponse;
    }

    /**
     * 
     * Method lets the user download the image from the grids page. The user can
     * visit the grid any time and download image.
     * 
     * In case the image is not found for the image id, HTTP 404 is returned
     * 
     * @mtwContentTypeReturned File
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/images/08EB37D7-2678-495D-B485-59233EB51996/downloads/image
     * Input: Image UUID
     * Output: Content of image as stream
     *                    </pre>
     * 
     * @param imageId
     *            the image for which the policy and manifest is downloaded
     * @return TAR ball content of the policy
     * @throws DirectorException
     */
    @Path("images/{imageId: [0-9a-zA-Z_-]+}/downloads/image")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadImage(@PathParam("imageId") final String imageId) {

	GenericResponse genericResponse = new GenericResponse();
	if (!ValidationUtil.isValidWithRegex(imageId, RegexPatterns.UUID)) {
	    genericResponse.error = "Imaged id is empty or not in uuid format";
	    genericResponse.setErrorCode(ErrorCode.INAVLID_ID);
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}

	ImageInfo imageInfo;
	try {
	    imageInfo = imageService.fetchImageById(imageId);
	} catch (DirectorException e1) {
	    log.error("Unable to fetch image", e1);
	    genericResponse.details = "Error fetching image";
	    genericResponse.error = Constants.ERROR;
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(genericResponse).build();
	}
	if (imageInfo == null) {
	    genericResponse.error = "No image with id : " + imageId + " exists.";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.NOT_FOUND).entity(genericResponse).build();
	}

	File tarBall = new File(imageInfo.getLocation() + File.separator + imageInfo.getImage_name());

	FileInputStream tarInputStream = null;
	try {
	    tarInputStream = new FileInputStream(tarBall) {
		@Override
		public void close() throws IOException {
		    super.close();
		}
	    };
	} catch (FileNotFoundException e) {
	    log.error("Error while dowloading Image", e);
	    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
	ResponseBuilder response = Response.ok(tarInputStream);

	if (Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(imageInfo.getImage_deployments())
		&& !tarBall.getName().endsWith(".tar")) {
	    response.header("Content-Disposition", "attachment; filename=" + tarBall.getName() + ".tar");
	} else {
	    response.header("Content-Disposition", "attachment; filename=" + tarBall.getName());
	}

        try{
            tarInputStream.close();
        }catch(IOException ex){
            log.debug("error during closing tarInputStream",ex);
        }
	return response.build();
    }

    /**
     * 
     * Mark image as deleted. We turn the deleted flag=true in the MW_IMAGE
     * table
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/images/08EB37D7-2678-495D-B485-59233EB51996
     * Input: pass the UUID of the image as path param
     * Output: {"deleted": true}
     * In case of error:
     * {"deleted": false , "error":"Error in deleteImage", "error_code": {"errorCode": 601,"errorDescription": "Request processing failed"}}
     * 
     * 
     *                    </pre>
     * 
     * 
     * @param imageId
     *            Id of the image to be deleted
     * @return Response
     */

    @Path("images/{imageId: [0-9a-zA-Z_-]+}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteImage(@PathParam("imageId") String imageId) {
	GenericDeleteResponse response = new GenericDeleteResponse();
	// / GenericResponse genericResponse= new GenericResponse();
	if (!ValidationUtil.isValidWithRegex(imageId, RegexPatterns.UUID)) {
	    response.errorCode = ErrorCode.INAVLID_ID;
	    response.error = "Imaged id is empty or not in uuid format";
	    return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
	}
	try {
	    if (imageService.fetchImageById(imageId) != null) {
		imageService.deleteImage(imageId);
		response.setDeleted(true);
	    } else {
		return Response.status(Response.Status.NOT_FOUND).build();
	    }
	} catch (DirectorException e) {
	    log.error("Error in deleteImage ", e);
	    response.setDeleted(false);
	    // /response.setDetails(e.getMessage());
	    response.setError("Error in deleteImage");
	    response.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;

	}
	return Response.ok(response).build();

    }

    /**
     * This method adds the host related details provided by the user. The
     * connection details are used to verify whether connection can be
     * established with the remote host.
     * 
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/images/host
     * Input: {"policy_name":"P2","ip_address":"10.35.35.131","username":"root","password":"intelmh","name":"10.35.35.131"}
     * 
     * Output: {"ip_address":"10.35.35.131","username":"root","image_name":"10.35.35.131","image_id":"EB7E45CD-F84C-419E-9841-A685E8E28050"}
     * 
     * In case of error:
     * Input: {"policy_name":"P2","ip_address":"","username":"root","password":"intelmh","name":"10.35.35.131"}
     * Lets say the user does not provide the IP:
     * 
     * { "error": "No host provided or host is in incorrect format,"error_code":{"errorCode":600,"errorDescription":"Validation failed"}}
     * 
     * In case of any back end error, the error would contain the error occurred at the backed.
     * 
     *                    </pre>
     * 
     * @param sshSettingRequest
     *            JSON representation of the connection details
     * @return Response containing the ID of the image created for the host
     * @throws DirectorException
     */

    @POST
    @Path("images/host")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addHost(SshSettingRequest sshSettingRequest) throws DirectorException {
	SshSettingResponse sshResponse = sshSettingRequest.validate("add");

	if (StringUtils.isNotBlank(sshResponse.getError())) {
	    sshResponse.setErrorCode(ErrorCode.VALIDATION_FAILED);
	    return Response.status(Response.Status.BAD_REQUEST).entity(sshResponse).build();
	}

	try {
	    sshResponse = imageService.addHost(sshSettingRequest);
	} catch (DirectorException e) {
	    log.error("Error while adding shh settings");
	    sshResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    sshResponse.setError(e.getMessage());
	}
	return Response.ok(sshResponse).build();
    }

    /**
     * This method updates the host related details provided by the user. The
     * connection details are used to verify whether connection can be
     * established with the remote host. This call acts similar to the POST
     * call, only difference being it expects an image id for update. If that is
     * not provided the method returns an error.
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/images/host
     * Input: {"policy_name":"P2","ip_address":"10.35.35.131","username":"root","password":"intelmh","image_id":"EB7E45CD-F84C-419E-9841-A685E8E28050"}
     * 
     * Output: 
     * {"ip_address":"10.35.35.131","username":"root","image_name":"10.35.35.131","image_id":"EB7E45CD-F84C-419E-9841-A685E8E28050"}
     * 
     * In case of error:
     * Input: {"policy_name":"P2","ip_address":"","username":"root","password":"intelmh","image_id":"EB7E45CD-F84C-419E-9841-A685E8E28050"}
     * Lets say the user does not provide the correct details to connect to the remote host :
     * 
     * { "error": "No host provided or host is in incorrect format,"error_code":{"errorCode":600,"errorDescription":"Validation failed"}}
     * 
     * In case of any back end error, the error would contain the error occurred at the backed.
     * 
     *                    </pre>
     * 
     * @param sshSettingRequest
     *            JSON representation of the connection details
     * @return Response containing the ID of the image created for the host
     * @throws DirectorException
     */

    @PUT
    @Path("images/host")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateHost(SshSettingRequest sshSettingRequest) {
	SshSettingResponse sshResponse = sshSettingRequest.validate("update");
	if (StringUtils.isNotBlank(sshResponse.getError())) {
	    sshResponse.setErrorCode(ErrorCode.VALIDATION_FAILED);
	    return Response.status(Response.Status.BAD_REQUEST).entity(sshResponse).build();
	}

	try {
	    sshResponse = imageService.updateSshData(sshSettingRequest);
	} catch (DirectorException e) {
	    log.error("Error in updateHost");
	    sshResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    sshResponse.setError(e.getMessage());
	}
	return Response.ok(sshResponse).build();
    }

    /**
     * This method initiates docker pull task for given image_id provided that
     * deployment_type of image must be 'Docker' and repo tag should be given
     * before.
     * 
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/rpc/docker-pull/3DED763F-99BA-4F99-B53B-5A6F6736E1E9
     * 
     * Input: Pathparam: 3DED763F-99BA-4F99-B53B-5A6F6736E1E9
     * 
     * Output: 
     * 		{
     * 			"details": "Docker Image successfully queued for download",
     * 			"status": "Success"
     * 		}
     * 
     * In case of error:
     * Input: Pathparam: FAA5AA92-5872-44CD-BBF4-AD3EFB61D7C9
     * Lets say the user provide the image id which does not have image_deployment as 'Docker':
     * {"error": "Cannot Perform Docker Pull Operation in this Image", "error_code": {"errorCode": 601,"errorDescription": "Request processing failed"}}
     * 
     *                    </pre>
     * 
     * @param image_id
     *            : image_id
     * @return Response containing details of docker-pull
     */
    @Path("rpc/docker-pull/{image_id: [0-9a-zA-Z_-]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response dockerPull(@PathParam("image_id") String image_id) {
	log.info("Performing Director Docker Pull for imageId::" + image_id);
	GenericResponse response = new GenericDeleteResponse();
	if (!ValidationUtil.isValidWithRegex(image_id, RegexPatterns.UUID)) {
	    response.error = "Imaged id is empty or not in uuid format";
	    response.setErrorCode(ErrorCode.INAVLID_ID);
	    return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
	}
	GenericResponse monitorStatus = new GenericResponse();
	try {
	    imageService.pullDockerImageFromRepository(image_id);
	} catch (DirectorException e) {
	    log.error("Error while performing docker pull");
	    monitorStatus.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    monitorStatus.error = e.getMessage();
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(monitorStatus).build();
	}
	monitorStatus.setStatus(Constants.SUCCESS);
	monitorStatus.setDetails("Docker Image succesfully queued for download");
	return Response.ok(monitorStatus).build();
    }

    /**
     * This method sets up docker image uploaded manually for given image_id for
     * further operation provided that deployment_type of image must be 'Docker'
     * and repo tag should be given before. This is mandatory step before
     * performing any further operation on docker image.
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/rpc/docker-process-uploaded-image/3DED763F-99BA-4F99-B53B-5A6F6736E1E9
     * 
     * Input: Pathparam: 3DED763F-99BA-4F99-B53B-5A6F6736E1E9
     * 
     * Output: 
     * 		{
     * 			"details": "Docker Image successfully uploaded",
     * 			"status": "Success"
     * 		}
     * 
     * In case of error:
     * Input: Pathparam: FAA5AA92-5872-44CD-BBF4-AD3EFB61D7C9
     * Lets say the user provide the image id which does not have image_deployment as 'Docker':
     * {"error": "Image must be of docker deployment type", "error_code": {"errorCode": 601,"errorDescription": "Request processing failed"}}
     * 
     *                    </pre>
     * 
     * @param image_id
     *            : image_id
     * @return Response containing details of docker-setup
     */
    @Path("rpc/docker-process-uploaded-image/{image_id: [0-9a-zA-Z_-]+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response dockerProcessUploadedImage(@PathParam("image_id") String image_id) {
	log.info("Performing Director Docker setup for imageid::" + image_id);
	GenericResponse response = new GenericDeleteResponse();
	if (!ValidationUtil.isValidWithRegex(image_id, RegexPatterns.UUID)) {
	    response.error = "Imaged id is empty or not in uuid format";
	    response.setErrorCode(ErrorCode.INAVLID_ID);
	    return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
	}
	GenericResponse monitorStatus = new GenericResponse();
	try {
	    imageService.processUploadedDockerImage(image_id);
	} catch (DirectorException e) {
	    log.error("Error while performing docker setup");
	    monitorStatus = new GenericResponse();
	    monitorStatus.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    monitorStatus.error = e.getMessage();
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(monitorStatus).build();
	}
	monitorStatus.setStatus(Constants.SUCCESS);
	monitorStatus.setDetails("Docker Image succesfully uploaded");
	return Response.ok(monitorStatus).build();
    }

    /**
     * This method removes policies from configured external storages whose
     * associated image is deleted from one or more configured external
     * storages.
     * 
     * @mtwContentTypeReturned JSON
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/rpc/remove-orphan-policies
     * 
     * Input: NA
     * 
     * Output: 
     * 		[{
     * 			"id": "EC69B190-0058-46F7-8AC1-DD5100C745DD",
     * 			"policy_uri": "http://10.35.35.35:8080/v1/AUTH_a4bde8b572054869848712dc9bd262ea/VJ_1/f3d9b14e-8841-4b9e-8e13-a8c6f34ebc19",
     * 			"date": 1460697888698,
     * 			"status": "Complete",
     * 			"trust_policy": {
     * 				"created_by_user_id": "admin",
     * 				"created_date": "2016-04-15 05:24:44",
     * 				"edited_by_user_id": "admin",
     * 				"edited_date": "2016-04-15 05:24:44",
     * 				"id": "F44C455D-8DD9-4AC2-B998-B1CF53872834",
     * 				"trust_policy": "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns3:TrustPolicy xmlns:ns3=\"mtwilson:trustdirector:policy:1.1\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><ns3:Director><ns3:CustomerId>admin</ns3:CustomerId></ns3:Director><ns3:Image><ns3:ImageId>f3d9b14e-8841-4b9e-8e13-a8c6f34ebc19</ns3:ImageId><ns3:ImageHash>bc52e3a4e4cf119d8fabc528eca6783d0ddc21b623a4f0f6bdf836fea1175a83</ns3:ImageHash></ns3:Image><ns3:LaunchControlPolicy>MeasureOnly</ns3:LaunchControlPolicy><ns3:Whitelist DigestAlg=\"sha256\"><ns3:File Path=\"/boot/grub/menu.lst\">bd4b7aa84740262bca78bd8e737693cdf5f90773f7aa2ece36572abb73fa2182</ns3:File><ns3:File Path=\"/boot/vmlinuz-3.2.0-80-virtual\">c9542e8517a25e2370916ca0dc118b4c0502add62f073536b336da7d3e09d298</ns3:File><ns3:File Path=\"/boot/config-3.2.0-80-virtual\">31bd9004a72d95e5c0ed6144fb9d6ea1784b86ea966c346448c08d655113b550</ns3:File><ns3:File Path=\"/boot/initrd.img-3.2.0-80-virtual\">4aa2ed8eee9cfb23f4f9d588d08a8b0da778254a1f64614b86a8976012e73607</ns3:File><ns3:File Path=\"/boot/grub/stage1\">77c1024a494c2170d0236dabdb795131d8a0f1809792735b3dd7f563ef5d951e</ns3:File><ns3:File Path=\"/boot/grub/e2fs_stage1_5\">1d317c1e94328cdbe00dc05d50b02f0cb9ec673159145b7f4448cec28a33dc14</ns3:File><ns3:File Path=\"/boot/grub/stage2\">5aa718ea1ecc59140eef959fc343f8810e485a44acc35805a0f6e9a7ffb10973</ns3:File></ns3:Whitelist><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>AEh2nGTVgr9Eawf3g8H/rz2YLvo=</DigestValue></Reference></SignedInfo><SignatureValue>QMxDQxjohuu+CVlHq57upUYpcYtZ2iGv1g/zsBLe36wH5NzBF1S6DPmS4FpaWiEhWheQdr10ZtBZ\ntk+j4ObA2YzMe7y/zZQ5XqYcgpyB5w43VFxfa1op39TZea29p+F5HG3pDVRqgC8NC3jHcK4g3bSx\nDzagmo1IoP4gzu3r2Zbu8ZNEtP76GtwT7gKMJ+Qq/rIDiUYi3U66HNEPUtpVjDzyq98qi2XxH5lN\nceY+D/BJtCr/EbVCzJN0KriEr86uCtuJysWBy173GV6NnALVYp/XapIOq2uuCDL17bv4T4UbvmoS\nF4JfYaT9WiLdFsSX2Q1/qYQ+aVWWk9l69aj5wg==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US</X509SubjectName><X509Certificate>MIIDYzCCAkugAwIBAgIEZxg6xTANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMzI4MTI0NjM2WhcNMjYwMzI2MTI0NjM2WjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCVGWggOHOQrjZRkxpgeikQDErUSKtI0kzFzdL5mlHO3rzidzJEAkbfPUvw\niCyVoFHn0oHfu7A62wqMcv90sEj4PZR5CrHzDpMwfRA/uEVeCrkl386JG2yXKbOYPsK6DvIXMZBD\nYMFWiRdsdRimpSfc68v8hAEPsDsqrnHld3xJvWhpc0wL3vL6HDfgcyTaMdpwPzif8nL1c00028vU\nK0yeOzPKc+5myYlj9SIelfaYocPBZDbK+VHhe3aEGn+sviI6/JvaskdP+WChKxpX82/dA//lK1TM\nAv81y1DWdSkRVlHHtTWU1pF/IGf8ylFUFh/ByWcjs71SZX9SpOL7XSHrAgMBAAGjITAfMB0GA1Ud\nDgQWBBRCrgwy6dluh/075+1V0pR+jZhMcjANBgkqhkiG9w0BAQsFAAOCAQEAd3st8f1b7+xFiNrz\nDP+CcBiC9mxrivPpLO57U4uhKrQnA0vCL+rL4uL4XfLCMLU2PRL+2YlXlrih+qZUvZCykduhN4RP\n1htv/9hHT6LxeGo9AFE5t7Ef8mNjp3tVsn0q8zOXzHYpILVtFsze2m2Fmyfv2NYMvsxn4IvQMIRA\n1BdJWRxgKvP8pR3VzuWXkuAd8WYz6tL9b3H/vzF/NKDJ+7JHdZ1glYt2BCMwVSsEjsP8aSIQkR9O\nSPjIzUC9cKv8KvU7SI7vjYuM4EpTUvAvtAFyMognyfIjHRkCxEww/lEKl/lhxdjP4cbY3G55txd+\niXHkF6o6nShG0/qn5UZ6ww==</X509Certificate></X509Data></KeyInfo></Signature></ns3:TrustPolicy>",
     * 				"img_attributes": {
     * 					"created_by_user_id": "admin",
     * 					"created_date": "2016-04-15 05:17:10",
     * 					"edited_by_user_id": "admin",
     * 					"edited_date": "2016-04-15 05:18:26",
     * 					"id": "9176FDF8-D3A2-4272-9E12-878B1B09427E",
     * 					"image_name": "cirros-0.3.4-x86_64-raw.img",
     * 					"image_format": "qcow2",
     * 					"image_deployments": "VM",
     * 					"image_size": 41126400,
     * 					"sent": 41126400,
     * 					"deleted": false,
     * 					"upload_variable_md5": "23dcdcde197e05a1fdeeb5ac93282b67",
     * 					"image_upload_status": "Complete",
     * 					"image_Location": "/mnt/images/vm/"
     * 				},
     * 				"display_name": "cirros-0.3.4-x86_64-raw.img",
     * 				"archive": false
     * 			},
     * 			"store_id": "BE03BD95-6407-46E6-A48B-7E1D0866A96E",
     * 			"store_name": "swift_35",
     * 			"upload_variable_md5": "23dcdcde197e05a1fdeeb5ac93282b67",
     * 			"store_artifact_id": "f3d9b14e-8841-4b9e-8e13-a8c6f34ebc19",
     * 			"deleted": true
     * 		}]
     * 
     * In case of error:
     * 
     * Output:
     * {
     *   		"error": "No policy uploads"
     * 		}
     * 
     *                    </pre>
     * 
     * @param
     * @return Response containing list of details of policies removed
     *//*
       * @Path("rpc/remove-orphan-policies")
       * 
       * @Produces(MediaType.APPLICATION_JSON)
       * 
       * @POST public Response removeOrphanPolicies() throws DirectorException {
       * log.info("Removing Orphan Policies"); try {
       * List<PolicyUploadTransferObject> removedOrphanPolicies =
       * artifactUploadService .removeOrphanPolicies(); return
       * Response.ok(removedOrphanPolicies).build(); } catch (DirectorException
       * e) { GenericResponse genericResponse = new GenericResponse();
       * genericResponse.setError(e.getMessage()); return
       * Response.status(Status.INTERNAL_SERVER_ERROR)
       * .entity(genericResponse).build(); } }
       */

    /**
     * This method returns list of deployment types which allow image encryption
     * 
     * @return list of deployment types which allow image encryption
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/encryption-supported
     * Input: NA
     * Output:
     * ["VM"]
     *                    </pre>
     */
    @Path("encryption-supported")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listEncryptionSupported() {
	List<String> imgEncryptSupported = new ArrayList<String>();
	imgEncryptSupported.add(Constants.DEPLOYMENT_TYPE_VM);
	return Response.ok(imgEncryptSupported).build();
    }

    /**
     * This method returns list of stalled images. Stalled images are those
     * images for which upload is pending for long time.
     * 
     * @return list of stalled images
     * @mtwMethodType GET
     * @mtwContentTypeReturned JSON
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/images-stalled
     * Input: NA
     * Output:
     * 		[
     * 			{
     * 			"created_by_user_id": "admin",
     * 			"created_date": "2016-04-14 18:49:11",
     * 			"edited_by_user_id": "admin",
     * 			"edited_date": "2016-04-14 18:49:11",
     * 			"id": "AC4750E4-4018-449D-B471-9517122FE29B",
     * 			"image_name": "123.img",
     * 			"image_format": "qcow2",
     * 			"image_deployments": "VM",
     * 			"image_size": 13631488,
     * 			"sent": 0,
     * 			"deleted": false,
     * 			"image_uploads_count": 0,
     * 			"policy_uploads_count": 0,
     * 			"image_upload_status": "In Progress",
     * 			"image_Location": "/mnt/images/vm/"
     * 			}
     * 		]
     *                    </pre>
     */
    @Path("images-stalled")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStalledImages() {
	List<ImageInfo> stalledImages;
	GenericResponse response = new GenericResponse();
	try {
	    stalledImages = imageService.getStalledImages();
	} catch (DirectorException e) {
	    response.setError(e.getMessage());
	    response.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
	}
	return Response.ok(stalledImages).build();
    }

    /**
     * This method returns hash algorithm used for hash calculation according to
     * valid deployment type. If deployment type is not provided returns hash
     * algorithm used by all supported deployment type.
     * 
     * @param deploymentType
     *            as PathParam
     * @return Response
     * @mtwMethodType GET
     * @mtwContentTypeReturned JSON
     * @mtwSampleRestCall
     * 
     *                    <pre>
     * https://{IP/HOST_NAME}/v1/image-hash-type
     * Input: deploymentType : VM
     * 
     * Output:
     * {
     * 		"deployment_type": "VM",
     * 		"hash_type": "sha256"
     * }
     * 
     * Input: If deployment type is not provided 
     * 
     * Output:
     * [
     * 	{"deployment_type":"BareMetal","hash_type":"sha1"},
     * 	{"deployment_type":"VM","hash_type":"sha256"},
     * 	{"deployment_type":"Docker","hash_type":"sha256"}
     * ]
     * 
     * Input: If invalid deployment is provided,
     * 
     * Output:
     * {"error": "Invalid Deployment Type","error_code":{"errorCode":600,"errorDescription":"Validation failed"}}
     *                    </pre>
     */
    @Path("image-hash-type")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImageHashType(@QueryParam("deploymentType") String deploymentType) {
	GenericResponse response = new GenericResponse();
	List<String> deplomentTypeList = new ArrayList<String>();
	deplomentTypeList.add(Constants.DEPLOYMENT_TYPE_VM);
	deplomentTypeList.add(Constants.DEPLOYMENT_TYPE_DOCKER);
	deplomentTypeList.add(Constants.DEPLOYMENT_TYPE_BAREMETAL);

	if (deploymentType != null && !deplomentTypeList.contains(deploymentType)) {
	    response.setError("Invalid Deployment Type");
	    response.errorCode = ErrorCode.VALIDATION_FAILED;
	    return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
	}

	List<HashTypeObject> imageHashType;
	try {
	    imageHashType = imageService.getImageHashType(deploymentType);
	} catch (DirectorException e) {
			log.error("Error",e);
	    response.setError(e.getMessage());
	    response.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
	    return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
	}
	if (imageHashType.size() == 1) {
	    return Response.ok(imageHashType.get(0)).build();
	}

	return Response.ok(imageHashType).build();
    }

	/**
	 * API to check if chunk of a file/image is available at server or not.
	 * Invoked internally by the Resumable.js library from the UI.
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	
		Sample GET call: 
		File Name: Test.img
		File Size: 10G
		Chunk Size: 5MB
		Chunk Number: 1
		Identifier: D3341CA4-1F3A-4CD7-B9BA-710F363A3CD8 (This is returned by Image metadata create service)

		https://trust-director/v1/images/upload/content/D3341CA4-1F3A-4CD7-B9BA-710F363A3CD8?
		resumableChunkSize=5242880&resumableTotalSize=10737418240&resumableChunkNumber=1&
		resumableIdentifier=D3341CA4-1F3A-4CD7-B9BA-710F363A3CD8&resumableCurrentChunkSize=5242880&
		resumableFilename=Test.img&resumableRelativePath=Test.img 
	 * Output Http responses: 
	 * 200 OK - If chunk is already available at server. No content is returned
	 * 401 Unauthorized - If request is made without auth token 
	 * 404 Not Found- If chunk is unavailable at server. 
	 * 503 Service Unavailable - If server fails to process the request.
	 *                    </pre>
	 */
	@Path("images/upload/content/{imageId: [0-9a-zA-Z_-]+}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response check(@PathParam("imageId") String imageId, @BeanParam Chunk chunk) {
		try {
			UploadService uploadService = new UploadService(Constants.defaultUploadPath);
			if(uploadService.isChunkUploaded(chunk)) {
				return Response.ok("Uploaded").build();
			}
			return Response.status(Status.NOT_FOUND).build();
		}catch (Exception e){
			log.error("Error while uploading image to Trust Director", e);
			GenericResponse genericResponse = new GenericResponse();
			genericResponse.details = e.getMessage();
			genericResponse.status = Constants.ERROR;
			genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(genericResponse).build();
		}
	}

	/**
	 * API to upload File chunk (Binary data) to server
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 *  https://{IP/HOST_NAME}/v1/rpc/images/content/C4C9E453-A864-4B14-8B72-9F9DF9406198
	 *  This API is called by the Resumable.js UI library. 
	 *  
	 *  Input :
	 *   
	 * Chunk Data (Binary)				
		resumableChunkSize 	Chunk size	query	number (integer) 	
		resumableTotalSize 	Total File size.	query	number (integer) 	
		resumableChunkNumber 	Chunk Number.	query	number (integer) 	
		resumableIdentifier 	Unique Identifier to identify the file. This UUID returned by image metadata create service	query	string 	
		resumableCurrentChunkSize 	Current chunk size. Current chunk size can be smaller than the chunk size. This is true for last chunk.	query	number (integer) 	
		resumableFilename 	File Name.	query	string 	
		resumableRelativePath 	File Relative path. Not available in chrome.	query	string 	
	
	chunk for image upload
	
	Output: 
	Completed image upload
	{
	"created_by_user_id": "admin",
	"created_date": "2016-05-16 08:53:08",
	"edited_by_user_id": "admin",
	"edited_date": "2016-05-16 08:56:15",
	"id": "C4C9E453-A864-4B14-8B72-9F9DF9406198",
	"image_name": "test11.img",
	"image_format": "qcow2",
	"image_deployments": "VM",
	"image_size": 13631488,
	"sent": 13631488,
	"deleted": false,
	"image_upload_status": "Complete",
	"image_Location": "/mnt/images/"
	}
	
	While the image upload is in progress:
	
	{
	"created_by_user_id": "admin",
	"created_date": "2016-05-16 08:53:08",
	"edited_by_user_id": "admin",
	"edited_date": "2016-05-16 08:53:50",
	"id": "C4C9E453-A864-4B14-8B72-9F9DF9406198",
	"image_name": "test11.img",
	"image_format": "qcow2",
	"image_deployments": "VM",
	"image_size": 13631488,
	"sent": 0,
	"deleted": false,
	"image_upload_status": "In Progress",
	"image_Location": "/mnt/images/"
	}
	
	In case of Docker: 
	
	
	{
	"created_by_user_id": "admin",
	"created_date": "2016-05-16 07:20:40",
	"edited_by_user_id": "admin",
	"edited_date": "2016-05-16 08:50:53",
	"id": "D3341CA4-1F3A-4CD7-B9BA-710F363A3CD8",
	"image_name": "debian:latest",
	"image_format": "tar",
	"image_deployments": "Docker",
	"image_size": 1322496,
	"sent": 0,
	"deleted": false,
	"repository": "debian",
	"tag": "latest",
	"image_upload_status": "In Progress",
	"image_Location": "/mnt/images/"
	}
	In case of docker, after the call to upload image content another call needs to be done:  https://HOST:PORT/v1/rpc/docker-setup/UUID_OF_IMAGE
	 * 
	 *                    </pre>
	 */
	@Path("rpc/images/upload/content/{imageId: [0-9a-zA-Z_-]+}")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@Context HttpServletRequest request, @PathParam("imageId") String imageId, @BeanParam Chunk chunk){
		try {
			UploadService uploadService = new UploadService(Constants.defaultUploadPath);
			byte[] chunkData = IOUtils.toByteArray(request.getInputStream(), request.getContentLength());
            boolean uploadStatus = uploadService.uploadChunk(imageId, chunk, chunkData);
            if(uploadStatus){
                //upload is completed trigger the task to update the image format
                UpdateImageFormatTask updateImageFormatTask = new UpdateImageFormatTask(imageId);
                FileFormatExecutor.submitTask(updateImageFormatTask);
            }
            return Response.ok(uploadStatus).build();
		}catch (Exception e){
			log.error("Error while uploading image to Trust Director", e);
			GenericResponse genericResponse = new GenericResponse();
			genericResponse.details = e.getMessage();
			genericResponse.status = Constants.ERROR;
			genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(genericResponse).build();
		}
	}

	/**
	 * API provides registration of pre-uploaded image with Trust Director for
	 * Policy creation. This API assumes that Image is available on server. This
	 * image can be uploaded through SSH/FTP. API accepts absolute path on
	 * server. At the end of this operation, file is moved to Trust Director's
	 * image repository<br/>
	 *
	 * Request and Response format example.
	 *
	 * <br>
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * https://{IP/HOST_NAME}/v1/images/upload/remote
	 Input
	 {
		 "image_deployments" : "VM",
		 "image_format" : "raw",
		 "image_name" : "c3",
		 "image_size" : 0,
		 "image_file" : "/tmp/c.img"
	 }
	
	 Output
	 {
		 "created_by_user_id": "admin",
		 "created_date": "2016-12-20 12:23:04",
		 "edited_by_user_id": "admin",
		 "edited_date": "2016-12-20 12:23:04",
		 "id": "EDB35ABB-2787-4A09-8937-53161B8A84F6",
		 "image_name": "c3",
		 "image_format": "raw",
		 "image_deployments": "VM",
		 "image_size": 0,
		 "sent": 0,
		 "deleted": false,
		 "image_upload_status": "success",
		 "image_Location": "/mnt/images/vm/"
	 }
	 *                    </pre>
	 * 
	 */
	@Path("images/upload/remote")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadLocalImage(TrustDirectorImageUploadRequest uploadRequest) {
		TrustDirectorImageUploadResponse uploadImageToTrustDirector;
		String errors = uploadRequest.validate();
		if (StringUtils.isNotBlank(errors)) {
			GenericResponse genericResponse = new GenericResponse();
			genericResponse.status = Constants.ERROR;
			genericResponse.details = errors;
			genericResponse.errorCode = ErrorCode.VALIDATION_FAILED;
			return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
		}


		imageService = new ImageServiceImpl();
		String imageName = uploadRequest.image_name;
		// Imagename in case of docker if not sent in request we take repo:tag
		if (Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(uploadRequest.image_deployments)
				&& StringUtils.isBlank(imageName)) {
			String repositoryInName = uploadRequest.repository;
			imageName = repositoryInName.replace("/", "-") + ":" + uploadRequest.tag;
		}
		try {
			if (Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(uploadRequest.image_deployments)
					&& dockerActionService.doesRepoTagExist(uploadRequest.repository, uploadRequest.tag)) {
				GenericResponse genericResponse = new GenericResponse();
				genericResponse.status = Constants.ERROR;
				genericResponse.details = "Image with Repo And Tag already exists";
				genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
				return Response.ok(genericResponse).build();
			}

			if (imageService.doesImageNameExist(imageName,uploadRequest.image_deployments)) {
				GenericResponse genericResponse = new GenericResponse();
				genericResponse.status = Constants.ERROR;
				genericResponse.details = "Image with same name already exists. <br>Please enter different name";
				genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
				return Response.ok().entity(genericResponse).build();
			}
			File uploadedFile = new File(uploadRequest.image_file.trim());
			if (!uploadedFile.exists() || !uploadedFile.isFile()) {
				GenericResponse genericResponse = new GenericResponse();
				genericResponse.status = Constants.ERROR;
				genericResponse.details = "Image location does not exists. Please check that image is available at given location";
				genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
				return Response.ok().entity(genericResponse).build();
			}
			uploadImageToTrustDirector = imageService.createUploadImageMetadataImpl(uploadRequest.image_deployments,
					uploadRequest.image_format, imageName, uploadRequest.image_size, uploadRequest.repository,
					uploadRequest.tag);
			ImageService imageService = new ImageServiceImpl();
			ImageInfo imageInfo = imageService.fetchImageById(uploadImageToTrustDirector.id);
			uploadedFile.renameTo(Paths.get(imageInfo.getLocation(),imageInfo.getImage_name()).toFile());
			imageInfo.setStatus(Constants.COMPLETE);
			imageInfo.setDeleted(false);
			imageService.updateImageMetadata(imageInfo);
			uploadImageToTrustDirector.status = Constants.SUCCESS;
			log.info("Successfully uploaded image to location: {}" , uploadImageToTrustDirector.getLocation());
            if (Constants.DEPLOYMENT_TYPE_VM.equalsIgnoreCase(uploadRequest.image_deployments)){
                log.info("Updating image format for file {} locaated in  {} ", imageInfo.getImage_name(), uploadImageToTrustDirector.getLocation());
                UpdateImageFormatTask updateImageFormatTask = new UpdateImageFormatTask(imageInfo);
                FileFormatExecutor.submitTask(updateImageFormatTask);
            }

			return Response.ok().entity(uploadImageToTrustDirector).build();
		} catch (DirectorException e) {
			GenericResponse genericResponse = new GenericResponse();
			genericResponse.status = Constants.ERROR;
			genericResponse.details = "Error in saving image metadata";
			genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
			return Response.ok(genericResponse).status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
