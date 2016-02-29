package com.intel.director.images;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import com.intel.director.api.ConnectorProperties;
import com.intel.director.api.GenericDeleteResponse;
import com.intel.director.api.ImageStoreDetailsTransferObject;
import com.intel.director.api.ImageStoreFilter;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.api.ui.ImageStoreConnector;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageStoresService;
import com.intel.director.service.LookupService;
import com.intel.director.service.impl.ImageStoresServiceImpl;
import com.intel.director.service.impl.LookupServiceImpl;
import com.intel.director.store.util.ImageStorePasswordUtil;
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

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageStores.class);

	/**
	 * List of configured image stores
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * 
	 * @return GetImageStoresResponse
	 * @throws DirectorException
	 * @TDMethodType GET
	 * @TDSampleRestCall <pre>
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
	 * </pre>
	 */
	@Path("image-stores")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ImageStoreResponse getImageStores(
			@QueryParam("artifacts") String artifacts) throws DirectorException {
		ImageStoreResponse imageStoreResponse = new ImageStoreResponse();
		List<ImageStoreTransferObject> imageStores = null;
		if (StringUtils.isBlank(artifacts)) {
			imageStores = imageStoreService.getImageStores(null);
		} else {
			ImageStoreFilter imageStoreFilter = new ImageStoreFilter();
			String[] artifactsArray = artifacts.split(",");
			imageStoreFilter.setArtifact_types(artifactsArray);
			imageStores = imageStoreService.getImageStores(imageStoreFilter);
		}
		imageStoreResponse.image_stores = new ArrayList<ImageStoreTransferObject>(
				imageStores);
		return imageStoreResponse;
	}

	/**
	 * Get image store for the provided imageStoreId if not exists respond with
	 * HTTP 404 Not Found
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall <pre>
	 * Input:
	 * 
	 * Output:
	 * 
	 * 
	 * </pre>
	 * @param imageStoreId
	 * @return
	 * @throws DirectorException
	 */
	@Path("image-stores/{imageStoreId: [0-9a-zA-Z_-]+}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getImageStore(@PathParam("imageStoreId") String imageStoreId)
			throws DirectorException {
		GenericDeleteResponse response = new GenericDeleteResponse();
		if(!ValidationUtil.isValidWithRegex(imageStoreId,RegexPatterns.UUID)){
			response.error = "Imaged id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(response).build();
		}
		ImageStoreTransferObject imageStoreResponse = imageStoreService
				.getImageStoreById(imageStoreId);
		if (imageStoreResponse == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(imageStoreResponse).build();
	}

	@Path("image-stores")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createImageStore(
			ImageStoreTransferObject imageStoreTransferObject)
			throws DirectorException {

		boolean validateConnectorArtifacts = imageStoreService
				.validateConnectorArtifacts(
						imageStoreTransferObject.getArtifact_types(),
						imageStoreTransferObject.getConnector());
		ImageStoreTransferObject createImageStore = new ImageStoreTransferObject();
		if (!validateConnectorArtifacts) {
			return Response.ok("Connector Doesn\'t Support Given Artifact(s)")
					.status(Status.BAD_REQUEST).build();
		}

		boolean doesImageStoreNameExist = imageStoreService
				.doesImageStoreNameExist(imageStoreTransferObject.getName(),
						imageStoreTransferObject.id);
		if (doesImageStoreNameExist) {
			createImageStore.setError("Image Store Name Already Exists.");
			return Response.ok(createImageStore).build();
		}

		try {
			createImageStore = imageStoreService
					.createImageStore(imageStoreTransferObject);
		} catch (DirectorException e) {
			log.error(e.getMessage());
			throw new DirectorException(e.getMessage());
		}
		return Response.ok(createImageStore).build();
	}

	@Path("image-stores")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public ImageStoreTransferObject updateImageStores(
			ImageStoreTransferObject imageStoreTransferObject)
			throws DirectorException {
		ImageStoreTransferObject updateImageStore = new ImageStoreTransferObject();
		boolean doesImageStoreNameExist = imageStoreService
				.doesImageStoreNameExist(imageStoreTransferObject.getName(),
						imageStoreTransferObject.id);
		if (doesImageStoreNameExist) {
			updateImageStore.setError("Image Store Name Already Exists.");
			return updateImageStore;
		}
		
		//Encrypt password fields
		ImageStorePasswordUtil imageStorePasswordUtil = new ImageStorePasswordUtil();
		ImageStoreDetailsTransferObject passwordConfiguration = imageStorePasswordUtil.getPasswordConfiguration(imageStoreTransferObject);
		String encryptedPassword = imageStorePasswordUtil.encryptPasswordForImageStore(passwordConfiguration.getValue());
		passwordConfiguration.setValue(encryptedPassword);
		return imageStoreService.updateImageStore(imageStoreTransferObject);
	}

	/**
	 * Turns deleted flag of image store to false for the provided imageStoreId
	 * if not exists respond with HTTP 404 Not Found
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall <pre>
	 * Input:
	 *  	https://{IP/HOST_NAME}/v1/image-stores/9EC519A5-F57C-4A07-ABEA-D7C5C58B5CD8
	 * 		PathParam: 9EC519A5-F57C-4A07-ABEA-D7C5C58B5CD8 
	 * 
	 * Output:
	 * In case of successful deletion:
	 * {"deleted" : true}
	 * 
	 * In case ImageStore doesn't exist, gives HTTP 404 Not Found
	 * 
	 * </pre>
	 * @param imageStoreId
	 * @return
	 * @throws DirectorException
	 */
	@Path("image-stores/{imageStoreId: [0-9a-zA-Z_-]+}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteImageStores(
			@PathParam("imageStoreId") String imageStoreId)
			throws DirectorException {
		GenericDeleteResponse deleteImageStore = null;
		GenericDeleteResponse response = new GenericDeleteResponse();
		if(!ValidationUtil.isValidWithRegex(imageStoreId,RegexPatterns.UUID)){
			response.error = "Imaged id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(response).build();
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
	 * List the configured supported artifacts for the deployment type
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * 
	 * @return List of supported artifacts	  
	 * @TDMethodType GET
	 * @TDSampleRestCall 
	 * <pre>
	 * 	https://{IP/HOST_NAME}/v1/deployment-artifacts?depolymentType=VM

	 * Input: Required: Name of deploymentType: VM or Docker
	 * 
	 * 
	 * Output:
	 * 
	 * If invalid deployment type is provided, emppty list is returned
	 * </pre>
	 */
	@Path("deployment-artifacts")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> getArtifactsForDeployment(
			@QueryParam("depolymentType") String depolymentType) {
		switch (depolymentType) {
		case Constants.DEPLOYMENT_TYPE_VM:
			return ArtifactsForDeploymentType.VM.getArtifacts();

		case Constants.DEPLOYMENT_TYPE_DOCKER:
			return ArtifactsForDeploymentType.DOCKER.getArtifacts();
		}
		return null;
	}


	/**
	 * List the configured connectors or the connector by the name provided
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * 
	 * @return External store connector details	  
	 * @TDMethodType GET
	 * @TDSampleRestCall 
	 * <pre>
	 * 	https://{IP/HOST_NAME}/v1/image-store-connectors

	 * Input: Optional : Name of connector
	 * https://{IP/HOST_NAME}/v1/Docker
	 * https://{IP/HOST_NAME}/v1/Glance
	 * https://{IP/HOST_NAME}/v1/Swift
	 * 
	 * Output:
	 * 
	 * </pre>
	 */
	@Path("/image-store-connectors/{connector: [0-9a-zA-Z_-]+|}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConnector(
			@PathParam("connector") String connector) {
		 
		if(StringUtils.isBlank(connector)){
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
		
		if(connectorProperties != null){
			ImageStoreConnector storeConnector = new ImageStoreConnector();
			storeConnector.setName(connectorProperties.getName());
			storeConnector.setDriver(connectorProperties.getDriver());
			storeConnector.setProperties(Arrays.asList(connectorProperties.getProperties()));
			storeConnector.setSupported_artifacts(connectorProperties.getSupported_artifacts());
			return Response.ok(storeConnector).build();
		}else{
			return Response.status(Status.NOT_FOUND).build();
		}
		
	}

}
