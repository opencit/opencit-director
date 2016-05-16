package com.intel.director.images;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.api.CommonValidations;
import com.intel.director.api.ConnectorProperties;
import com.intel.director.api.GenericDeleteResponse;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.ImageStoreDetailsTransferObject;
import com.intel.director.api.ImageStoreFilter;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.api.ValidationResponse;
import com.intel.director.api.ui.ImageStoreConnector;
import com.intel.director.common.Constants;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ImageStoresService;
import com.intel.director.service.LookupService;
import com.intel.director.service.impl.ImageStoresServiceImpl;
import com.intel.director.service.impl.LookupServiceImpl;
import com.intel.director.store.util.ImageStorePasswordUtil;
import com.intel.director.util.I18Util;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;
import com.intel.mtwilson.launcher.ws.ext.V2;

/**
 * ImageStores related APIs
 * 
 * @author Siddharth
 * 
 */
@V2
@Path("/")
public class ImageStores {
	ImageStoresService imageStoreService = new ImageStoresServiceImpl();
	LookupService lookupService = new LookupServiceImpl();
	IPersistService persistService = new DbServiceImpl();

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImageStores.class);

	/**
	 * This method will return list of active image stores based on artifacts
	 * supported by that image stores. Multiple artifacts can be given with
	 * delimited by ','.
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @param artifacts
	 *            comma separated as QueryParam
	 * 
	 * @return Response
	 * @throws DirectorException
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * https://{IP/HOST_NAME}/v1/image-stores
	 * Input: Query parameter is optional If provided: - "artifacts"
	 * 
	 * should have possible values : one or many value in the below list, delimited by ',' in multiple:
	 * Image, Policy, Tarball, Docker
	 * Example: https://{IP/HOST_NAME}/v1/image-stores?artifacts=Docker,Image
	 * 
	 * Output:
	 * {
	 *   "deleted": false,
	 *   "image_stores": [
	 *     {
	 *       "deleted": false,
	 *       "id": "9EC519A5-F57C-4A07-ABEA-D7C5C58B5CD8",
	 *       "name": "Glance1",
	 *       "artifact_types": [
	 *         "Docker"
	 *       ],
	 *       "connector": "Glance",
	 *       "image_store_details": [
	 *         {
	 *           "id": "932FE2CE-AF28-4817-A484-D8EBB30512AB",
	 *           "image_store_id": "9EC519A5-F57C-4A07-ABEA-D7C5C58B5CD8",
	 *           "key": "gkey1",
	 *           "value": "asd1"
	 *         },
	 *         {
	 *           "id": "ADE6E1E1-9BAF-48C5-9A3E-938A6D28FB94",
	 *           "image_store_id": "9EC519A5-F57C-4A07-ABEA-D7C5C58B5CD8",
	 *           "key": "gkey2",
	 *           "value": "asd1212"
	 *         }
	 *       ]
	 *     }
	 *   ]
	 * }
	 *                    </pre>
	 */
	@Path("image-stores")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getImageStores(@QueryParam("artifacts") String artifacts) throws DirectorException {
		ImageStoreResponse imageStoreResponse = new ImageStoreResponse();
		List<ImageStoreTransferObject> imageStores;

		List<ImageStoreTransferObject> activeImageStores = new ArrayList<>();
		if (StringUtils.isBlank(artifacts)) {
			imageStores = imageStoreService.getImageStores(null);
		} else {
			ImageStoreFilter imageStoreFilter = new ImageStoreFilter();

			String[] artifactsArray = artifacts.split(",");
			for (String str : artifactsArray) {
				if (!ValidationUtil.isValidWithRegex(str, Constants.ARTIFACT_IMAGE + "|" + Constants.ARTIFACT_DOCKER
						+ "|" + Constants.ARTIFACT_POLICY + "|" + Constants.ARTIFACT_TAR)) {
					imageStoreResponse.setError("Invalid artifact provided. It should be " + Constants.ARTIFACT_IMAGE
							+ "|" + Constants.ARTIFACT_DOCKER + "|" + Constants.ARTIFACT_POLICY + "|"
							+ Constants.ARTIFACT_TAR);
					return Response.status(Response.Status.BAD_REQUEST).entity(imageStoreResponse).build();
				}
			}
			imageStoreFilter.setArtifact_types(artifactsArray);
			imageStores = imageStoreService.getImageStores(imageStoreFilter);
		}

		for (ImageStoreTransferObject imageStoreTransferObject : imageStores) {
			if (imageStoreTransferObject.deleted) {
				continue;
			}
			activeImageStores.add(imageStoreTransferObject);
		}

		imageStoreResponse.image_stores = new ArrayList<ImageStoreTransferObject>(activeImageStores);
		return Response.ok(imageStoreResponse).build();
	}

	/**
	 * Get image store for the provided imageStoreId if not exists respond with
	 * HTTP 404 Not Found
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * Input:
	 * 
	 * https://<TD_HOST>/v1/image-stores/3DA92563-A2A6-4D52-9337-3201D11105E1
	 * Output:
	 * 
	 * {
		"id": "3DA92563-A2A6-4D52-9337-3201D11105E1",
		"name": "ExtStore_1",
		"artifact_types": [
			"Image",
			"Tarball"
		],
		"connector": "Glance",
		"deleted": false,
		"image_store_details": [
			{
	  			"id": "FFF71D82-29E2-46FA-9789-ED67880B2266",
			  	"image_store_id": "3DA92563-A2A6-4D52-9337-3201D11105E1",
			  	"key": "glance.image.store.username"
			},
			{
				"id": "331BD472-5F66-4EFB-811C-461A0CEF6517",
				"image_store_id": "3DA92563-A2A6-4D52-9337-3201D11105E1",
				"key": "glance.api.endpoint"
			},
			{
				"id": "F76EC7A8-703C-4645-8FE0-2666C377D033",
			  	"image_store_id": "3DA92563-A2A6-4D52-9337-3201D11105E1",
			  	"key": "glance.tenant.name"
			},
			{
			  	"id": "AA24C0E1-9F33-442C-B872-49364D933F35",
			  	"image_store_id": "3DA92563-A2A6-4D52-9337-3201D11105E1",
			  	"key": "glance.keystone.public.endpoint"
			},
			{
			  	"id": "02073D14-648F-41C8-97CF-E3BD831D4F03",
			  	"image_store_id": "3DA92563-A2A6-4D52-9337-3201D11105E1",
			  	"key": "glance.image.store.password"
			}
		]
	}
	
	If an invalid ID for image store is give, a HTTP 404 is returned
	 *                    </pre>
	 * 
	 * @param imageStoreId
	 *            as PathParam
	 * @return Response
	 * @throws DirectorException
	 */
	@Path("image-stores/{imageStoreId: [0-9a-zA-Z_-]+}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getImageStore(@PathParam("imageStoreId") String imageStoreId) throws DirectorException {
		GenericDeleteResponse response = new GenericDeleteResponse();
		if (!ValidationUtil.isValidWithRegex(imageStoreId, RegexPatterns.UUID)) {
			response.error = "Imaged id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
		}
		ImageStoreTransferObject imageStoreResponse = imageStoreService.getImageStoreById(imageStoreId);
		if (imageStoreResponse == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(imageStoreResponse).build();
	}

	/**
	 * 
	 * Creates the external store. The user can either pass the whole object, if
	 * the details are known or just the name, artifacts and connector to
	 * reserve a external store. The user can then call the PUT call to update
	 * the values of the connection properties.
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 *  Input:
	 *   	https://{IP/HOST_NAME}/v1/image-stores
	 *   {"name":"ExtStore_6", "connector":"Glance", "artifact_types":["Image"]}
	 * 
	 *   Output:
	 * 		{
	 *   "id": "3C21E66E-E223-4A85-A7B6-85B0EA7329A9",
	 *   "name": "ExtStore_6",
	 *   "artifact_types": [
	 *     "Image"
	 *   ],
	 *   "connector": "Glance",
	 *   "deleted": false,
	 *   "image_store_details": [
	 *     {
	 *       "id": "1C083C10-D4BA-4191-86FE-3F9E14C16769",
	 *       "image_store_id": "3C21E66E-E223-4A85-A7B6-85B0EA7329A9",
	 *       "key": "glance.api.endpoint",
	 *       "key_display_value": "API Endpoint",
	 *       "place_holder_value": "http://<HOST>:<PORT>"
	 *     },
	 *     {
	 *       "id": "73716DD7-88AC-49E7-A9AE-701183C40A81",
	 *       "image_store_id": "3C21E66E-E223-4A85-A7B6-85B0EA7329A9",
	 *       "key": "glance.keystone.public.endpoint",
	 *       "key_display_value": "Authorization Endpoint",
	 *       "place_holder_value": "http://<HOST>:<PORT>"
	 *     },
	 *     {
	 *       "id": "F1A918F8-ED03-4A91-92A5-C6D1CF6BE558",
	 *       "image_store_id": "3C21E66E-E223-4A85-A7B6-85B0EA7329A9",
	 *       "key": "glance.tenant.name",
	 *       "key_display_value": "Tenant Name",
	 *       "place_holder_value": "Tenant Name"
	 *     },
	 *     {
	 *       "id": "57E12EF9-325A-411D-8D11-29D08E19DD46",
	 *       "image_store_id": "3C21E66E-E223-4A85-A7B6-85B0EA7329A9",
	 *       "key": "glance.image.store.username",
	 *       "key_display_value": "UserName",
	 *       "place_holder_value": "UserName"
	 *     },
	 *     {
	 *       "id": "E08E2410-D9D5-4600-BC5E-89A883FC97D6",
	 *       "image_store_id": "3C21E66E-E223-4A85-A7B6-85B0EA7329A9",
	 *       "key": "glance.image.store.password",
	 *       "key_display_value": "Password",
	 *       "place_holder_value": "Password"
	 *     },
	 *     {
	 *       "id": "516ECBC2-A5E3-44D7-9F36-F4EA90F48130",
	 *       "image_store_id": "3C21E66E-E223-4A85-A7B6-85B0EA7329A9",
	 *       "key": "glance.visibility",
	 *       "key_display_value": "Visibility",
	 *       "place_holder_value": "Visibility"
	 *     }
	 *   ]
	 * }
	 * 	
	 *   There are validations for the connector and artifact_types fields. 
	 *   
	 *   If incorrect values are given
	 *   {"name":"ExtStore_4", "connector":"Glance", "artifact_types":["Image","aa"]}
	 *   following is the response:	   
	 * 	  
	 *   {
	 *   "error": "Connector or Artifacts for the connector are not supported "
	 * }
	 * 
	 * </pre>
	 * 
	 * @param imageStoreTransferObject
	 * @return Response
	 * @throws DirectorException
	 */
	@Path("image-stores")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createImageStore(ImageStoreTransferObject imageStoreTransferObject) throws DirectorException {

		boolean validateConnectorArtifacts = imageStoreService.validateConnectorArtifacts(
				imageStoreTransferObject.getArtifact_types(), imageStoreTransferObject.getConnector());
		ImageStoreTransferObject createImageStore = new ImageStoreTransferObject();
		GenericResponse genericResponse= new GenericResponse();
		if (!validateConnectorArtifacts) {
			genericResponse.setError("Connector or Artifacts for the connector are not supported ");
			return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
		}

		boolean doesImageStoreNameExist = imageStoreService.doesImageStoreNameExist(imageStoreTransferObject.getName(),
				imageStoreTransferObject.id);
		if (doesImageStoreNameExist) {
			genericResponse.setError("Image Store Name Already Exists.");
			return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
		}

		try {
			createImageStore = imageStoreService.createImageStore(imageStoreTransferObject);
		} catch (DirectorException e) {
			log.error(e.getMessage());
			throw new DirectorException(e.getMessage());
		}
		return Response.ok(createImageStore).build();
	}

	/**
	 * 
	 * This method updates the image store record. This call replaces the existing record of the image store with the data provided by the user. It updates the name, artifacts and the 
	 * connection details for the connector. 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType PUT
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 *  Input:
	 *   	https://{IP/HOST_NAME}/v1/image-stores
	 * {"id":"87C85769-AE5E-4B65-9526-B10856F5D888","name":"Glance-all","artifact_types":["Docker","Image","Tarball"],"connector":"Glance","deleted":false,"image_store_details":[{"id":"187DB008-C6DF-4DA5-AC02-A1F758D675DB","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.api.endpoint","value":"http://10.35.35.35:9292","key_display_value":"API Endpoint","place_holder_value":"http://<HOST>:<PORT>"},{"id":"1E22F5CC-84AA-4FBE-8F55-D3714CA81673","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.keystone.public.endpoint","value":"http://10.35.35.35:5000","key_display_value":"Authorization Endpoint","place_holder_value":"http://<HOST>:<PORT>"},{"id":"39613EAC-09C4-4B37-A736-A6E9C934D117","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.tenant.name","value":"admin","key_display_value":"Tenant Name","place_holder_value":"Tenant Name"},{"id":"ED744528-879B-43AD-8A59-C29F03B65194","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.image.store.username","value":"admin","key_display_value":"UserName","place_holder_value":"UserName"},{"id":"76C0AC54-4E2F-4FC1-972C-A8F474C19D69","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.image.store.password","value":"intelmh","key_display_value":"Password","place_holder_value":"Password"}]}
	 * 
	 *   Output: 
	 *   {"id":"87C85769-AE5E-4B65-9526-B10856F5D888","name":"Glance-all","artifact_types":["Docker","Image","Tarball"],"connector":"Glance","deleted":false,"image_store_details":[{"id":"187DB008-C6DF-4DA5-AC02-A1F758D675DB","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.api.endpoint","value":"http://10.35.35.35:9292","key_display_value":"API Endpoint","place_holder_value":"http://<HOST>:<PORT>"},{"id":"1E22F5CC-84AA-4FBE-8F55-D3714CA81673","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.keystone.public.endpoint","value":"http://10.35.35.35:5000","key_display_value":"Authorization Endpoint","place_holder_value":"http://<HOST>:<PORT>"},{"id":"39613EAC-09C4-4B37-A736-A6E9C934D117","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.tenant.name","value":"admin","key_display_value":"Tenant Name","place_holder_value":"Tenant Name"},{"id":"ED744528-879B-43AD-8A59-C29F03B65194","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.image.store.username","value":"admin","key_display_value":"UserName","place_holder_value":"UserName"},{"id":"76C0AC54-4E2F-4FC1-972C-A8F474C19D69","image_store_id":"87C85769-AE5E-4B65-9526-B10856F5D888","key":"glance.image.store.password","value":"Mo6Yct4CMm7lLQkw3ZBhNPbA0RZ+bxad6nPcovgluOgNuYc/MhCF/rEVLXrETfvV","key_display_value":"Password","place_holder_value":"Password"}]}
	 * 	
	 *   There are validations for the connector and artifact_types fields. 
	 *   If incorrect values are given
	 *   {"name":"ExtStore_1", "connector":"Glance", "artifact_types":["ABC", "Tarball"]} 
	 *   following is the reponse:	  
	 *    
	 *   In case of error:
	 *   {
	 *   "error": "Connector or Artifacts for the connector are not supported "
	 * }
	 * 
	 * </pre>
	 * 
	 * @param imageStoreTransferObject
	 * @return Response
	 * @throws DirectorException
	 */
	@Path("image-stores")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateImageStores(ImageStoreTransferObject imageStoreTransferObject) throws DirectorException {
	///	ImageStoreTransferObject updateImageStore = new ImageStoreTransferObject();
		boolean validateConnectorArtifacts = imageStoreService.validateConnectorArtifacts(
				imageStoreTransferObject.getArtifact_types(), imageStoreTransferObject.getConnector());
		GenericResponse genericResponse= new GenericResponse();
		if (!validateConnectorArtifacts) {
			genericResponse.setError("Connector or Artifacts for the connector are not supported ");
			return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
		}
		ImageStoreTransferObject imageStoreById = imageStoreService.getImageStoreById(imageStoreTransferObject.getId());
		if (imageStoreById == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		boolean doesImageStoreNameExist = imageStoreService.doesImageStoreNameExist(imageStoreTransferObject.getName(),
				imageStoreTransferObject.id);
		if (doesImageStoreNameExist) {
			genericResponse.setError("Image Store Name Already Exists.");
			return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
		}

		List<String> errorList = new ArrayList<String>();

		Collection<ImageStoreDetailsTransferObject> image_store_details = imageStoreTransferObject
				.getImage_store_details();
		for (ImageStoreDetailsTransferObject imageStoreDetailsTransferObject : image_store_details) {
			if (StringUtils.isBlank(imageStoreDetailsTransferObject.getValue())) {
				errorList.add(I18Util.format(imageStoreDetailsTransferObject.getKey()));
			}
		}

		if (!errorList.isEmpty()) {
			genericResponse.setError(StringUtils.join(errorList, ",") + " can't be blank");
			return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
		}

		// Encrypt password fields
		ImageStoreDetailsTransferObject passwordConfiguration = imageStoreTransferObject.fetchPasswordConfiguration();
		ImageStorePasswordUtil imageStorePasswordUtil = new ImageStorePasswordUtil(passwordConfiguration.id);

		if (StringUtils.isNotBlank(passwordConfiguration.getValue())) {
			String encryptedPassword = imageStorePasswordUtil
					.encryptPasswordForImageStore(passwordConfiguration.getValue());
			passwordConfiguration.setValue(encryptedPassword);
		}
		ImageStoreTransferObject updatedIS = imageStoreService.updateImageStore(imageStoreTransferObject);
		return Response.ok(updatedIS).build();
	}

	/**
	 * Turns deleted flag of image store to false for the provided imageStoreId
	 * if not exists respond with HTTP 404 Not Found
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * Input:
	 *  	https://{IP/HOST_NAME}/v1/image-stores/9EC519A5-F57C-4A07-ABEA-D7C5C58B5CD8
	 * 		PathParam: 9EC519A5-F57C-4A07-ABEA-D7C5C58B5CD8 
	 * 
	 * Output:
	 * In case of successful deletion:
	 * {"deleted" : true}
	 * 
	 * In case ImageStore with given id does not exist, returns HTTP 404 Not Found
	 * 
	 *                    </pre>
	 * 
	 * @param imageStoreId
	 * @return Response
	 * @throws DirectorException
	 */
	@Path("image-stores/{imageStoreId: [0-9a-zA-Z_-]+}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteImageStores(@PathParam("imageStoreId") String imageStoreId) throws DirectorException {
		GenericDeleteResponse deleteImageStore;
		///GenericDeleteResponse deleteResponse = new GenericDeleteResponse();
		GenericResponse genericResponse= new GenericResponse();
		if (!ValidationUtil.isValidWithRegex(imageStoreId, RegexPatterns.UUID)) {
			genericResponse.error = "Imaged id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
		}
		try {
			deleteImageStore = imageStoreService.deleteImageStore(imageStoreId);
		} catch (DirectorException e) {
			log.error("Error in Deleting Image Store", e);
			throw new DirectorException("Error in Deleting Image Store", e);
		}

		if (deleteImageStore == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		deleteImageStore.setDeleted(true);
		return Response.ok(deleteImageStore).build();
	}

	/**
	 * This method returns list of configured supported artifacts for the
	 * deployment type
	 * 
	 * @param deployment
	 *            type as QueryPAram
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * 
	 * @return Response List of supported artifacts
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * 	https://{IP/HOST_NAME}/v1/deployment-artifacts?deploymentType=VM
	
	 * Input: Required: Name of deploymentType: VM or Docker
	 * 
		{
			"Policy": "Policy",
			"Image": "Image",
			"Tarball": "Image With Policy",
			"ImageWithPolicy": "Image with Policy Separated"
		}
	 * 
	 * In case no deployment type is provided, gives error:
		{
			"error": "Please provide depolyment type"
		}
	 * 
	 *                    </pre>
	 */
	@Path("deployment-artifacts")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArtifactsForDeployment(@QueryParam("deploymentType") String deploymentType) {
		GenericResponse genericResponse = new GenericResponse();
		if (StringUtils.isBlank(deploymentType)) {
			genericResponse.error = "Please provide deployment type";
			return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
		}
		if (!CommonValidations.validateImageDeployments(deploymentType)) {
			genericResponse.error = "Incorrect deployment_type";
			return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
		}
		switch (deploymentType) {
		case Constants.DEPLOYMENT_TYPE_VM:
			return Response.ok(ArtifactsForDeploymentType.VM.getArtifacts()).build();

		case Constants.DEPLOYMENT_TYPE_DOCKER:
			return Response.ok(ArtifactsForDeploymentType.DOCKER.getArtifacts()).build();
		}
		genericResponse.setDetails("No supported artifacts for given deployment type");
		return Response.ok(genericResponse).build();
	}

	/**
	 * List the connector properties by the connector name provided
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @param connector
	 *            type as path param
	 * @return External store connector properties details
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * 	https://{IP/HOST_NAME}/v1/image-store-connectors
	
	 * Input: Name of connector Docker,Glance,Swift else it would give 404 not found
	 * Output:
	 * https://{IP/HOST_NAME}/v1/Docker
		{
			"name": "DOCKER",
			"driver": "com.intel.director.dockerhub.DockerHubManager",
			"properties": [{
				"key": "Username"
			},
			{
				"key": "Password"
			},
			{
				"key": "Email"
			}],
			"supported_artifacts": {
				"Docker": "Docker"
			}
		}
	 * https://{IP/HOST_NAME}/v1/Glance
		{
			"name": "GLANCE",
			"driver": "com.intel.director.images.GlanceImageStoreManager",
			"properties": [{
				"key": "glance.api.endpoint"
			},
			{
				"key": "glance.keystone.public.endpoint"
			},
			{
				"key": "glance.tenant.name"
			},
			{
				"key": "glance.image.store.username"
			},
			{
				"key": "glance.image.store.password"
			},
			{
				"key": "glance.visibility"
			}],
			"supported_artifacts": {
				"Image": "Image",
				"Docker": "Docker",
				"Tarball": "Tarball"
			}
		}
	 * https://{IP/HOST_NAME}/v1/Swift
		{
			"name": "GLANCE",
			"driver": "com.intel.director.images.GlanceImageStoreManager",
			"properties": [{
				"key": "glance.api.endpoint"
			},
			{
				"key": "glance.keystone.public.endpoint"
			},
			{
				"key": "glance.tenant.name"
			},
			{
				"key": "glance.image.store.username"
			},
			{
				"key": "glance.image.store.password"
			},
			{
				"key": "glance.visibility"
			}],
			"supported_artifacts": {
				"Image": "Image",
				"Docker": "Docker",
				"Tarball": "Tarball"
			}
		}
	 * 
	 *                    </pre>
	 */
	@Path("/image-store-connectors/{connector: [0-9a-zA-Z_-]+|}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConnector(@PathParam("connector") String connector) {

		if (StringUtils.isBlank(connector)) {
			List<ImageStoreConnector> connectorsList = new ArrayList<>();
			for (ConnectorProperties connectorProperties : ConnectorProperties.values()) {
				ImageStoreConnector storeConnector = new ImageStoreConnector();
				storeConnector.setName(connectorProperties.getName());
				storeConnector.setDriver(connectorProperties.getDriver());
				storeConnector.setProperties(Arrays.asList(connectorProperties.getProperties()));
				storeConnector.setSupported_artifacts(connectorProperties.getSupported_artifacts());
				connectorsList.add(storeConnector);
			}
			return Response.ok(connectorsList).build();
		}

		ConnectorProperties connectorProperties = ConnectorProperties.getConnectorByName(connector);

		if (connectorProperties != null) {
			ImageStoreConnector storeConnector = new ImageStoreConnector();
			storeConnector.setName(connectorProperties.getName());
			storeConnector.setDriver(connectorProperties.getDriver());
			storeConnector.setProperties(Arrays.asList(connectorProperties.getProperties()));
			storeConnector.setSupported_artifacts(connectorProperties.getSupported_artifacts());
			return Response.ok(storeConnector).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}

	}

	/**
	 * This methods validates given image store associated with id. If image
	 * store id does not exist return HTTP 404.
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * https://{IP/HOST_NAME}/v1/rpc/image-stores/[IMAGE_STORE_UUID]/validate
	 * Input: the UUID of the image store would be sent as part of the request
	 * Output:
	 * 		{"valid":true}
	 * 		OR
	 * 		If validation is unsuccessful
	 * 		{"valid":false,"error":"Cannot construct glance rest client with given credentials"}
	 * </pre>
	 * 
	 * @param imageStoreId
	 * @return Response containing the status
	 * @throws DirectorException
	 */
	@Path("rpc/image-stores/{imageStoreId: [0-9a-zA-Z_-]+}/validate")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateImageStore(@PathParam("imageStoreId") String imageStoreId) throws DirectorException {
		ImageStoreTransferObject imageStoreTransferObject = null;
		GenericResponse response = new GenericResponse();
		ValidationResponse validationResponse = new ValidationResponse();
		if (!ValidationUtil.isValidWithRegex(imageStoreId, RegexPatterns.UUID)) {
			
			response.error = "Image store id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
		}

		try {
			imageStoreTransferObject = persistService.fetchImageStorebyId(imageStoreId);
		} catch (DbException e1) {
			log.error("Error fetching image store by id {}", imageStoreId, e1);
		}
		if (imageStoreTransferObject == null) {
	///		GenericResponse response = new GenericResponse();
			response.error = "Invalid image store id";
			return Response.status(Response.Status.NOT_FOUND).entity(response).build();
		}

		imageStoreTransferObject.setIsValid(true);

		try {
			imageStoreService.validateImageStore(imageStoreId);
		} catch (DirectorException e) {
			validationResponse.setError(e.getMessage());
			validationResponse.setValid(false);
			return Response.ok(validationResponse).build();
		}

		try {
			persistService.updateImageStore(imageStoreTransferObject);
		} catch (DbException e) {
			throw new DirectorException("Error Updating image store");
		}
		validationResponse.setValid(true);
		return Response.ok(validationResponse).build();

	}

}
