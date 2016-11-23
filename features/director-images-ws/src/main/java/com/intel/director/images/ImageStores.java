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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.api.ArtifactsForDeploymentType;
import com.intel.director.api.CommonValidations;
import com.intel.director.api.ConnectorProperties;
import com.intel.director.api.ErrorCode;
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
import com.intel.mtwilson.util.exec.EscapeUtil;

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
	 * 
	 * 
	 * {
	 * 	"image_stores": [{
	 * 		"id": "F4510B2C-503B-4984-97CD-3ADCFC712A62",
	 * 		"name": "Mitaka-138",
	 * 		"artifact_types": ["Docker",
	 * 		"Image"],
	 * 		"connector": "Glance",
	 * 		"deleted": false,
	 * 		"image_store_details": [{
	 * 			"id": "F56C12C0-C4E9-4B26-868E-E786BF3B877C",
	 * 			"image_store_id": "F4510B2C-503B-4984-97CD-3ADCFC712A62",
	 * 			"key": "glance.api.endpoint",
	 * 			"value": "http://10.35.35.138:9292",
	 * 			"key_display_value": "API Endpoint",
	 * 			"place_holder_value": "http://<HOST>:<PORT>"
	 * 		},
	 * 		{
	 * 			"id": "2FEA843B-352E-4FCF-A3AF-64BC4DB2D4EA",
	 * 			"image_store_id": "F4510B2C-503B-4984-97CD-3ADCFC712A62",
	 * 			"key": "glance.keystone.public.endpoint",
	 * 			"value": "http://10.35.35.138:5000",
	 * 			"key_display_value": "Authorization Endpoint",
	 * 			"place_holder_value": "http://<HOST>:<PORT>"
	 * 		},
	 * 		{
	 * 			"id": "5A69242C-9AD4-446B-900A-34B8346746D9",
	 * 			"image_store_id": "F4510B2C-503B-4984-97CD-3ADCFC712A62",
	 * 			"key": "glance.tenant.name",
	 * 			"value": "admin",
	 * 			"key_display_value": "Tenant/Project Name",
	 * 			"place_holder_value": "Tenant/Project Name"
	 * 		},
	 * 		{
	 * 			"id": "CB5881DB-3C95-4809-B2FD-3874FBB6E6B1",
	 * 			"image_store_id": "F4510B2C-503B-4984-97CD-3ADCFC712A62",
	 * 			"key": "glance.image.store.username",
	 * 			"value": "admin",
	 * 			"key_display_value": "UserName",
	 * 			"place_holder_value": "UserName"
	 * 		},
	 * 		{
	 * 			"id": "6C7F02C9-B810-4078-996D-10986A795707",
	 * 			"image_store_id": "F4510B2C-503B-4984-97CD-3ADCFC712A62",
	 * 			"key": "glance.image.store.password",
	 * 			"value": "",
	 * 			"key_display_value": "Password",
	 * 			"place_holder_value": "Password"
	 * 		},
	 * 		{
	 * 			"id": "8F06CA92-EFC8-477C-9CEA-153F40735601",
	 * 			"image_store_id": "F4510B2C-503B-4984-97CD-3ADCFC712A62",
	 * 			"key": "glance.domain.name",
	 * 			"value": "default",
	 * 			"key_display_value": "Domain Name",
	 * 			"place_holder_value": "Domain(Mandatory for Mitaka)"
	 * 		},
	 * 		{
	 * 			"id": "FA70D602-2C8A-4C61-B1BF-BBA4439946C8",
	 * 			"image_store_id": "F4510B2C-503B-4984-97CD-3ADCFC712A62",
	 * 			"key": "glance.visibility",
	 * 			"value": "public",
	 * 			"key_display_value": "Visibility",
	 * 			"place_holder_value": "Visibility"
	 * 		}],
	 * 		"connector_composite_items_list": [{
	 * 			"key": "keystone.version",
	 * 			"option_list": [{
	 * 				"id": 3,
	 * 				"value": "v3",
	 * 				"display_name": "Mitaka"
	 * 			},
	 * 			{
	 * 				"id": 2,
	 * 				"value": "v2.0",
	 * 				"display_name": "Liberty"
	 * 			}],
	 * 			"value": "v2.0",
	 * 			"placeholder": "Openstack Version",
	 * 			"id": "028CC443-F4E0-4B20-9183-C28E8DBEA85E"
	 * 		}]
	 * 	},
	 * 	{
	 * 		"id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"name": "Ext",
	 * 		"artifact_types": ["Docker",
	 * 		"Image"],
	 * 		"connector": "Glance",
	 * 		"deleted": false,
	 * 		"image_store_details": [{
	 * 			"id": "88CD3120-A1CF-4B5A-8979-1287D2ACB63D",
	 * 			"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 			"key": "glance.api.endpoint",
	 * 			"value": "http://10.35.35.138:9292",
	 * 			"key_display_value": "API Endpoint",
	 * 			"place_holder_value": "http://<HOST>:<PORT>"
	 * 		},
	 * 		{
	 * 			"id": "EB13377E-AE83-407B-86E0-E0FFEE97A4A3",
	 * 			"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 			"key": "glance.keystone.public.endpoint",
	 * 			"value": "http://10.35.35.138:5000",
	 * 			"key_display_value": "Authorization Endpoint",
	 * 			"place_holder_value": "http://<HOST>:<PORT>"
	 * 		},
	 * 		{
	 * 			"id": "DE72C559-CC5F-4B1E-9619-B2211986DCD5",
	 * 			"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 			"key": "glance.tenant.name",
	 * 			"value": "admin",
	 * 			"key_display_value": "Tenant/Project Name",
	 * 			"place_holder_value": "Tenant/Project Name"
	 * 		},
	 * 		{
	 * 			"id": "5D94ADB7-9E68-4800-BCAC-2C518B876BF0",
	 * 			"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 			"key": "glance.image.store.username",
	 * 			"value": "admin",
	 * 			"key_display_value": "UserName",
	 * 			"place_holder_value": "UserName"
	 * 		},
	 * 		{
	 * 			"id": "CB9F8719-8C64-4F21-A937-87F70EAFD510",
	 * 			"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 			"key": "glance.image.store.password",
	 * 			"value": "kKcwJ2EJEQVOvGSotQft0c4+8B0iTZw6F8YYvL9Ljm5JdWnVG5T57kecF0qZwBo0",
	 * 			"key_display_value": "Password",
	 * 			"place_holder_value": "Password"
	 * 		},
	 * 		{
	 * 			"id": "B5060752-0001-4EC4-85B9-53D0D4F77113",
	 * 			"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 			"key": "glance.domain.name",
	 * 			"value": "admin",
	 * 			"key_display_value": "Domain Name",
	 * 			"place_holder_value": "Domain(Mandatory for Mitaka)"
	 * 		},
	 * 		{
	 * 			"id": "05F5D81F-4B17-42FE-A054-3348658C2E1F",
	 * 			"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 			"key": "glance.visibility",
	 * 			"value": "public",
	 * 			"key_display_value": "Visibility",
	 * 			"place_holder_value": "Visibility"
	 * 		}],
	 * 		"connector_composite_items_list": [{
	 * 			"key": "keystone.version",
	 * 			"option_list": [{
	 * 				"id": 3,
	 * 				"value": "v3",
	 * 				"display_name": "Mitaka"
	 * 			},
	 * 			{
	 * 				"id": 2,
	 * 				"value": "v2.0",
	 * 				"display_name": "Liberty"
	 * 			}],
	 * 			"value": "v2.0",
	 * 			"placeholder": "Openstack Version",
	 * 			"id": "028CC443-F4E0-4B20-9183-C28E8DBEA85E"
	 * 		}]
	 * 	}]
	 * }
	 * </pre>
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
		    imageStoreResponse.errorCode = ErrorCode.VALIDATION_FAILED;
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
	 * https://<TD_HOST>/v1/image-stores/6332B463-E914-4115-84DA-A8EA64DBB441
	 * Output:
	 * 
	 * {
	 * 	"id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 	"name": "Ext",
	 * 	"artifact_types": ["Docker",
	 * 	"Image"],
	 * 	"connector": "Glance",
	 * 	"deleted": false,
	 * 	"image_store_details": [{
	 * 		"id": "88CD3120-A1CF-4B5A-8979-1287D2ACB63D",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.api.endpoint",
	 * 		"value": "http://10.35.35.138:9292",
	 * 		"key_display_value": "API Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>"
	 * 	},
	 * 	{
	 * 		"id": "EB13377E-AE83-407B-86E0-E0FFEE97A4A3",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.keystone.public.endpoint",
	 * 		"value": "http://10.35.35.138:5000",
	 * 		"key_display_value": "Authorization Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>"
	 * 	},
	 * 	{
	 * 		"id": "DE72C559-CC5F-4B1E-9619-B2211986DCD5",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.tenant.name",
	 * 		"value": "admin",
	 * 		"key_display_value": "Tenant/Project Name",
	 * 		"place_holder_value": "Tenant/Project Name"
	 * 	},
	 * 	{
	 * 		"id": "5D94ADB7-9E68-4800-BCAC-2C518B876BF0",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.username",
	 * 		"value": "admin",
	 * 		"key_display_value": "UserName",
	 * 		"place_holder_value": "UserName"
	 * 	},
	 * 	{
	 * 		"id": "CB9F8719-8C64-4F21-A937-87F70EAFD510",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.password",
	 * 		"value": "kKcwJ2EJEQVOvGSotQft0c4+8B0iTZw6F8YYvL9Ljm5JdWnVG5T57kecF0qZwBo0",
	 * 		"key_display_value": "Password",
	 * 		"place_holder_value": "Password"
	 * 	},
	 * 	{
	 * 		"id": "B5060752-0001-4EC4-85B9-53D0D4F77113",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.domain.name",
	 * 		"value": "admin",
	 * 		"key_display_value": "Domain Name",
	 * 		"place_holder_value": "Domain(Mandatory for Mitaka)"
	 * 	},
	 * 	{
	 * 		"id": "05F5D81F-4B17-42FE-A054-3348658C2E1F",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.visibility",
	 * 		"value": "public",
	 * 		"key_display_value": "Visibility",
	 * 		"place_holder_value": "Visibility"
	 * 	}],
	 * 	"connector_composite_items_list": [{
	 * 		"key": "keystone.version",
	 * 		"option_list": [{
	 * 			"id": 3,
	 * 			"value": "v3",
	 * 			"display_name": "Mitaka"
	 * 		},
	 * 		{
	 * 			"id": 2,
	 * 			"value": "v2.0",
	 * 			"display_name": "Liberty"
	 * 		}],
	 * 		"value": "v2.0",
	 * 		"placeholder": "Openstack Version",
	 * 		"id": "A678AC54-2F4B-46E7-BFDC-1E6158C544E6"
	 * 	}]
	 * }
	 * }]
	 * }
	 *     
	 *     If an invalid ID for image store is give, a HTTP 404 is returned
	 * </pre>
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
	    response.setErrorCode(ErrorCode.INAVLID_ID);
	    return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
	}
	ImageStoreTransferObject imageStoreResponse = imageStoreService.getImageStoreById(imageStoreId);
	if (imageStoreResponse == null) {
	    response.setErrorCode(ErrorCode.RECORD_NOT_FOUND);
	    return Response.status(Status.NOT_FOUND).entity(response).build();
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
	 *   {"artifact_types":["Image","Docker"],"name":"Ext","connector":"Glance","deleted":true}
	 *   
	 *   Output:
	 * 	{
	 * 	"id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 	"name": "Ext",
	 * 	"artifact_types": ["Docker",
	 * 	"Image"],
	 * 	"connector": "Glance",
	 * 	"deleted": true,
	 * 	"image_store_details": [{
	 * 		"id": "88CD3120-A1CF-4B5A-8979-1287D2ACB63D",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.api.endpoint",
	 * 		"key_display_value": "API Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>"
	 * 	},
	 * 	{
	 * 		"id": "EB13377E-AE83-407B-86E0-E0FFEE97A4A3",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.keystone.public.endpoint",
	 * 		"key_display_value": "Authorization Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>"
	 * 	},
	 * 	{
	 * 		"id": "DE72C559-CC5F-4B1E-9619-B2211986DCD5",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.tenant.name",
	 * 		"key_display_value": "Tenant/Project Name",
	 * 		"place_holder_value": "Tenant/Project Name"
	 * 	},
	 * 	{
	 * 		"id": "5D94ADB7-9E68-4800-BCAC-2C518B876BF0",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.username",
	 * 		"key_display_value": "UserName",
	 * 		"place_holder_value": "UserName"
	 * 	},
	 * 	{
	 * 		"id": "CB9F8719-8C64-4F21-A937-87F70EAFD510",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.password",
	 * 		"key_display_value": "Password",
	 * 		"place_holder_value": "Password"
	 * 	},
	 * 	{
	 * 		"id": "B5060752-0001-4EC4-85B9-53D0D4F77113",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.domain.name",
	 * 		"key_display_value": "Domain Name",
	 * 		"place_holder_value": "Domain(Mandatory for Mitaka)"
	 * 	},
	 * 	{
	 * 		"id": "05F5D81F-4B17-42FE-A054-3348658C2E1F",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.visibility",
	 * 		"key_display_value": "Visibility",
	 * 		"place_holder_value": "Visibility"
	 * 	}],
	 * 	"connector_composite_items_list": [{
	 * 		"key": "keystone.version",
	 * 		"option_list": [{
	 * 			"id": 3,
	 * 			"value": "v3",
	 * 			"display_name": "Mitaka"
	 * 		},
	 * 		{
	 * 			"id": 2,
	 * 			"value": "v2.0",
	 * 			"display_name": "Liberty"
	 * 		}],
	 * 		"placeholder": "Openstack Version",
	 * 		"id": "A678AC54-2F4B-46E7-BFDC-1E6158C544E6"
	 * 	}]
	 * }
	 * 	
	 *   There are validations for the connector and artifact_types fields. 
	 *   
	 *   If incorrect values are given
	 *   {"name":"Ext", "connector":"Glance", "artifact_types":["Image","aa"]}
	 *   following is the response:	   
	 * 	  
	 *   {"error": "Connector or Artifacts for the connector are not supported ","error_code":{"errorCode":600,"errorDescription":"Validation failed"}}
	 * 
	 * </pre>
	 * 
	 * @param imageStoreTransferObject
	 * @return Response
	 */
    @Path("image-stores")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createImageStore(ImageStoreTransferObject imageStoreTransferObject) {

	boolean validateConnectorArtifacts = imageStoreService.validateConnectorArtifacts(
		imageStoreTransferObject.getArtifact_types(), imageStoreTransferObject.getConnector());
	ImageStoreTransferObject createImageStore = new ImageStoreTransferObject();
	GenericResponse genericResponse = new GenericResponse();
	if (!validateConnectorArtifacts) {
	    genericResponse.setError("Connector or Artifacts for the connector are not supported ");
	    genericResponse.setErrorCode(ErrorCode.VALIDATION_FAILED);
	    return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
	}
	//Regex: <("[^"]*"|'[^']*'|[^'">])*>

	imageStoreTransferObject.name = StringEscapeUtils.escapeHtml(imageStoreTransferObject.name);
	imageStoreTransferObject.name = EscapeUtil.doubleQuoteEscapeShellArgument(imageStoreTransferObject.name);
	
	boolean doesImageStoreNameExist;
	try {
	    doesImageStoreNameExist = imageStoreService.doesImageStoreNameExist(imageStoreTransferObject.getName(),
		    imageStoreTransferObject.id);
	} catch (DirectorException e1) {
	    genericResponse.setError("Error while checking if the image store with the give name exists.");
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    genericResponse.setDetails(e1.getMessage());
	    return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
	}
	if (doesImageStoreNameExist) {
	    genericResponse.setError("Image Store Name Already Exists.");
	    genericResponse.setErrorCode(ErrorCode.DUPLICATE_RECORD);
	    return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
	}

	try {
	    createImageStore = imageStoreService.createImageStore(imageStoreTransferObject);
	} catch (DirectorException e) {
	    log.error(e.getMessage());
	    genericResponse.setError(Constants.ERROR);
	    genericResponse.setDetails(e.getMessage());
	    genericResponse.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
	}
	return Response.ok(createImageStore).build();
    }

	/**
	 * 
	 * This method updates the image store record. This call replaces the
	 * existing record of the image store with the data provided by the user. It
	 * updates the name, artifacts and the connection details for the connector.
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType PUT
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 *  Input:
	 *   	https://{IP/HOST_NAME}/v1/image-stores
	 *   
	 * {
	 * 	"id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 	"name": "Ext",
	 * 	"artifact_types": ["Image",
	 * 	"Docker"],
	 * 	"connector": "Glance",
	 * 	"deleted": false,
	 * 	"image_store_details": [{
	 * 		"id": "88CD3120-A1CF-4B5A-8979-1287D2ACB63D",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.api.endpoint",
	 * 		"value": "http://10.35.35.138:9292",
	 * 		"key_display_value": "API Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>"
	 * 	},
	 * 	{
	 * 		"id": "EB13377E-AE83-407B-86E0-E0FFEE97A4A3",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.keystone.public.endpoint",
	 * 		"value": "http://10.35.35.138:5000",
	 * 		"key_display_value": "Authorization Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>"
	 * 	},
	 * 	{
	 * 		"id": "DE72C559-CC5F-4B1E-9619-B2211986DCD5",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.tenant.name",
	 * 		"value": "admin",
	 * 		"key_display_value": "Tenant/Project Name",
	 * 		"place_holder_value": "Tenant/Project Name"
	 * 	},
	 * 	{
	 * 		"id": "5D94ADB7-9E68-4800-BCAC-2C518B876BF0",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.username",
	 * 		"value": "admin",
	 * 		"key_display_value": "UserName",
	 * 		"place_holder_value": "UserName"
	 * 	},
	 * 	{
	 * 		"id": "CB9F8719-8C64-4F21-A937-87F70EAFD510",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.password",
	 * 		"value": "intelmh",
	 * 		"key_display_value": "Password",
	 * 		"place_holder_value": "Password"
	 * 	},
	 * 	{
	 * 		"id": "B5060752-0001-4EC4-85B9-53D0D4F77113",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.domain.name",
	 * 		"value": "admin",
	 * 		"key_display_value": "Domain Name",
	 * 		"place_holder_value": "Domain(Mandatory for Mitaka)"
	 * 	},
	 * 	{
	 * 		"id": "05F5D81F-4B17-42FE-A054-3348658C2E1F",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.visibility",
	 * 		"value": "public",
	 * 		"key_display_value": "Visibility",
	 * 		"place_holder_value": "Visibility"
	 * 	},
	 * 	{
	 * 		"id": "05F5D81F-4B17-42FE-A054-3348658C2E1F",
	 * 		"value": "v2.0",
	 * 		"key": "keystone.version",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441"
	 * 	}]
	 * }
	 * 
	 *   Output: 
	 *  {
	 * 	"id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 	"name": "Ext",
	 * 	"artifact_types": ["Docker",
	 * 	"Image"],
	 * 	"connector": "Glance",
	 * 	"deleted": false,
	 * 	"image_store_details": [{
	 * 		"id": "05F5D81F-4B17-42FE-A054-3348658C2E1F",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "keystone.version",
	 * 		"value": "v2.0"
	 * 	},
	 * 	{
	 * 		"id": "88CD3120-A1CF-4B5A-8979-1287D2ACB63D",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.api.endpoint",
	 * 		"value": "http://10.35.35.138:9292",
	 * 		"key_display_value": "API Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>"
	 * 	},
	 * 	{
	 * 		"id": "EB13377E-AE83-407B-86E0-E0FFEE97A4A3",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.keystone.public.endpoint",
	 * 		"value": "http://10.35.35.138:5000",
	 * 		"key_display_value": "Authorization Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>"
	 * 	},
	 * 	{
	 * 		"id": "DE72C559-CC5F-4B1E-9619-B2211986DCD5",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.tenant.name",
	 * 		"value": "admin",
	 * 		"key_display_value": "Tenant/Project Name",
	 * 		"place_holder_value": "Tenant/Project Name"
	 * 	},
	 * 	{
	 * 		"id": "5D94ADB7-9E68-4800-BCAC-2C518B876BF0",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.username",
	 * 		"value": "admin",
	 * 		"key_display_value": "UserName",
	 * 		"place_holder_value": "UserName"
	 * 	},
	 * 	{
	 * 		"id": "CB9F8719-8C64-4F21-A937-87F70EAFD510",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.password",
	 * 		"value": "kKcwJ2EJEQVOvGSotQft0c4+8B0iTZw6F8YYvL9Ljm5JdWnVG5T57kecF0qZwBo0",
	 * 		"key_display_value": "Password",
	 * 		"place_holder_value": "Password"
	 * 	},
	 * 	{
	 * 		"id": "B5060752-0001-4EC4-85B9-53D0D4F77113",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.domain.name",
	 * 		"value": "admin",
	 * 		"key_display_value": "Domain Name",
	 * 		"place_holder_value": "Domain(Mandatory for Mitaka)"
	 * 	},
	 * 	{
	 * 		"id": "05F5D81F-4B17-42FE-A054-3348658C2E1F",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.visibility",
	 * 		"value": "public",
	 * 		"key_display_value": "Visibility",
	 * 		"place_holder_value": "Visibility"
	 * 	}],
	 * 	"connector_composite_items_list": [{
	 * 		"key": "keystone.version",
	 * 		"option_list": [{
	 * 			"id": 3,
	 * 			"value": "v3",
	 * 			"display_name": "Mitaka"
	 * 		},
	 * 		{
	 * 			"id": 2,
	 * 			"value": "v2.0",
	 * 			"display_name": "Liberty"
	 * 		}],
	 * 		"value": "v2.0",
	 * 		"placeholder": "Openstack Version",
	 * 		"id": "A678AC54-2F4B-46E7-BFDC-1E6158C544E6"
	 * 	}]
	 * }
	 * 	
	 *   There are validations for the connector and artifact_types fields. 
	 *   If incorrect values are given
	 *   {"name":"ExtStore_1", "connector":"Glance", "artifact_types":["ABC", "Tarball"]} 
	 *   following is the reponse:	  
	 *    
	 *   In case of error:
	 *   {"error": "Connector or Artifacts for the connector are not supported","error_code":{"errorCode":600,"errorDescription":"Validation failed"}}
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

	// Begin validation
	boolean validateConnectorArtifacts = imageStoreService.validateConnectorArtifacts(
		imageStoreTransferObject.getArtifact_types(), imageStoreTransferObject.getConnector());
	GenericResponse genericResponse = new GenericResponse();
	if (!validateConnectorArtifacts) {
	    genericResponse.setError("Connector or Artifacts for the connector are not supported ");
	    genericResponse.setErrorCode(ErrorCode.VALIDATION_FAILED);
	    return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
	}
	
	ImageStoreTransferObject imageStoreById = imageStoreService.getImageStoreById(imageStoreTransferObject.getId());
	if (imageStoreById == null) {
	    genericResponse.errorCode = ErrorCode.RECORD_NOT_FOUND;
	    return Response.status(Status.NOT_FOUND).entity(genericResponse).build();
	}
	imageStoreTransferObject.name = StringEscapeUtils.escapeHtml(imageStoreTransferObject.name);
	imageStoreTransferObject.name = EscapeUtil.doubleQuoteEscapeShellArgument(imageStoreTransferObject.name);
	boolean doesImageStoreNameExist = imageStoreService.doesImageStoreNameExist(imageStoreTransferObject.getName(),
		imageStoreTransferObject.id);
	if (doesImageStoreNameExist) {
	    genericResponse.setError("Image Store Name Already Exists.");
	    genericResponse.errorCode = ErrorCode.DUPLICATE_RECORD;
	    return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
	}

	List<String> errorList = new ArrayList<String>();

	Collection<ImageStoreDetailsTransferObject> image_store_details = imageStoreTransferObject
		.getImage_store_details();

	for (ImageStoreDetailsTransferObject imageStoreDetailsTransferObject : image_store_details) {
		ImageStoreDetailsTransferObject passwordConfiguration = imageStoreTransferObject.fetchPasswordConfiguration();
	    if (passwordConfiguration != null && passwordConfiguration.equals(imageStoreDetailsTransferObject)) {
		continue;
	    }
	    if (StringUtils.isBlank(imageStoreDetailsTransferObject.getValue())) {
		errorList.add(I18Util.format(imageStoreDetailsTransferObject.getKey()));
	    }
	}

	if (!errorList.isEmpty()) {
	    genericResponse.setError(StringUtils.join(errorList, ",") + " can't be blank");
	    genericResponse.errorCode = ErrorCode.VALIDATION_FAILED;	    
	    return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
	}

	// End validation

	// Encrypt password fields if its provided by the user
	ImageStoreDetailsTransferObject passwordConfiguration = imageStoreTransferObject.fetchPasswordConfiguration();
	if (passwordConfiguration != null && StringUtils.isNotBlank(passwordConfiguration.getValue())) {
	    ImageStorePasswordUtil imageStorePasswordUtil = new ImageStorePasswordUtil(passwordConfiguration.id);
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
     */
    @Path("image-stores/{imageStoreId: [0-9a-zA-Z_-]+}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteImageStores(@PathParam("imageStoreId") String imageStoreId) {
	GenericDeleteResponse deleteImageStore;
	/// GenericDeleteResponse deleteResponse = new GenericDeleteResponse();
	GenericResponse genericResponse = new GenericResponse();
	if (!ValidationUtil.isValidWithRegex(imageStoreId, RegexPatterns.UUID)) {
	    genericResponse.error = "Imaged id is empty or not in uuid format";
	    genericResponse.errorCode = ErrorCode.INAVLID_ID;
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	try {
	    deleteImageStore = imageStoreService.deleteImageStore(imageStoreId);
	} catch (DirectorException e) {
	    log.error("Error in Deleting Image Store", e);
	    genericResponse.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
	    genericResponse.error = Constants.ERROR;
	    genericResponse.details = e.getMessage();
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(genericResponse).build();
	}

	if (deleteImageStore == null) {
	    genericResponse.errorCode = ErrorCode.RECORD_NOT_FOUND;
	    genericResponse.error = Constants.ERROR;
	    genericResponse.details = "No image store found";
	    return Response.status(Status.NOT_FOUND).entity(genericResponse).build();
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
    		"Image": "Image",
    		"Tarball": "Image With Policy"			
    	}
     * 
     * In case no deployment type is provided, gives error:
    	{"error": "Please provide depolyment type","error_code":{"errorCode":600,"errorDescription":"Validation failed"}}
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
	    genericResponse.errorCode = ErrorCode.VALIDATION_FAILED;
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	if (!CommonValidations.validateImageDeployments(deploymentType)) {
	    genericResponse.error = "Incorrect deployment_type";
	    genericResponse.errorCode = ErrorCode.VALIDATION_FAILED;
	    return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
	}
	switch (deploymentType) {
	case Constants.DEPLOYMENT_TYPE_VM:
	    return Response.ok(ArtifactsForDeploymentType.VM.getArtifacts()).build();

	case Constants.DEPLOYMENT_TYPE_DOCKER:
	    return Response.ok(ArtifactsForDeploymentType.DOCKER.getArtifacts()).build();
	}
	genericResponse.setDetails("No supported artifacts for given deployment type");
	genericResponse.errorCode = ErrorCode.RECORD_NOT_FOUND;
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
	 *     
	 * Input: Name of connector Docker,Glance else it would give 404 not found
	 * Output:
	 * https://{IP/HOST_NAME}/v1/Docker
	 *     	{
	 *     		"name": "DOCKER",
	 *     		"driver": "com.intel.director.dockerhub.DockerHubManager",
	 *     		"properties": [{
	 *     			"key": "Username"
	 *     		},
	 *     		{
	 *     			"key": "Password"
	 *     		},
	 *     		{
	 *     			"key": "Email"
	 *     		}],
	 *     		"supported_artifacts": {
	 *     			"Docker": "Docker"
	 *     		}
	 *     	}
	 * https://{IP/HOST_NAME}/v1/Glance
	 *     	{
	 * 	"name": "GLANCE",
	 * 	"driver": "com.intel.director.images.GlanceImageStoreManager",
	 * 	"properties": [{
	 * 		"key": "glance.api.endpoint"
	 * 	},
	 * 	{
	 * 		"key": "glance.keystone.public.endpoint"
	 * 	},
	 * 	{
	 * 		"key": "glance.tenant.name"
	 * 	},
	 * 	{
	 * 		"key": "glance.image.store.username"
	 * 	},
	 * 	{
	 * 		"key": "glance.image.store.password"
	 * 	},
	 * 	{
	 * 		"key": "glance.domain.name"
	 * 	},
	 * 	{
	 * 		"key": "glance.visibility"
	 * 	}],
	 * 	"supported_artifacts": {
	 * 		"Image": "Image",
	 * 		"Docker": "Docker",
	 * 		"Tarball": "Tarball"
	 * 	},
	 * 	"connector_composite_item": [{
	 * 		"key": "keystone.version",
	 * 		"option_list": [{
	 * 			"id": 3,
	 * 			"value": "v3",
	 * 			"display_name": "Mitaka"
	 * 		},
	 * 		{
	 * 			"id": 2,
	 * 			"value": "v2.0",
	 * 			"display_name": "Liberty"
	 * 		}],
	 * 		"value": "v3",
	 * 		"placeholder": "Openstack Version",
	 * 		"id": "7E322AD9-8A43-41F4-91CF-1A53EC1D5892"
	 * 
	 * 
	 * 
	 * </pre>
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
	    storeConnector.setConnectorCompositeItem(Arrays.asList(connectorProperties.getConnectorCompositeItem()));
	    return Response.ok(storeConnector).build();
	} else {
	    GenericResponse genericResponse = new GenericResponse();
	    genericResponse.setErrorCode(ErrorCode.RECORD_NOT_FOUND);
	    genericResponse.setError(Constants.ERROR);
	    genericResponse.setDetails("No connector found for name provided");
	    genericResponse.errorCode = ErrorCode.VALIDATION_FAILED;
	    return Response.status(Status.NOT_FOUND).entity(genericResponse).build();
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
	 * https://{IP/HOST_NAME}/v1/rpc/image-stores/validate                    
	 * 
	 * Input: 
	 * {
	 * 	"id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 	"name": "Ext",
	 * 	"artifact_types": ["Image",
	 * 	"Docker"],
	 * 	"connector": "Glance",
	 * 	"deleted": true,
	 * 	"image_store_details": [{
	 * 		"id": "88CD3120-A1CF-4B5A-8979-1287D2ACB63D",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.api.endpoint",
	 * 		"key_display_value": "API Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>",
	 * 		"value": "http://10.35.35.138:9292"
	 * 	},
	 * 	{
	 * 		"id": "EB13377E-AE83-407B-86E0-E0FFEE97A4A3",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.keystone.public.endpoint",
	 * 		"key_display_value": "Authorization Endpoint",
	 * 		"place_holder_value": "http://<HOST>:<PORT>",
	 * 		"value": "http://10.35.35.138:5000"
	 * 	},
	 * 	{
	 * 		"id": "DE72C559-CC5F-4B1E-9619-B2211986DCD5",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.tenant.name",
	 * 		"key_display_value": "Tenant/Project Name",
	 * 		"place_holder_value": "Tenant/Project Name",
	 * 		"value": "admin"
	 * 	},
	 * 	{
	 * 		"id": "5D94ADB7-9E68-4800-BCAC-2C518B876BF0",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.username",
	 * 		"key_display_value": "UserName",
	 * 		"place_holder_value": "UserName",
	 * 		"value": "admin"
	 * 	},
	 * 	{
	 * 		"id": "CB9F8719-8C64-4F21-A937-87F70EAFD510",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.image.store.password",
	 * 		"key_display_value": "Password",
	 * 		"place_holder_value": "Password",
	 * 		"value": "intelmh"
	 * 	},
	 * 	{
	 * 		"id": "B5060752-0001-4EC4-85B9-53D0D4F77113",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.domain.name",
	 * 		"key_display_value": "Domain Name",
	 * 		"place_holder_value": "Domain(Mandatory for Mitaka)",
	 * 		"value": "admin"
	 * 	},
	 * 	{
	 * 		"id": "05F5D81F-4B17-42FE-A054-3348658C2E1F",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441",
	 * 		"key": "glance.visibility",
	 * 		"key_display_value": "Visibility",
	 * 		"place_holder_value": "Visibility",
	 * 		"value": "public"
	 * 	},
	 * 	{
	 * 		"id": "05F5D81F-4B17-42FE-A054-3348658C2E1F",
	 * 		"value": "v2.0",
	 * 		"key": "keystone.version",
	 * 		"image_store_id": "6332B463-E914-4115-84DA-A8EA64DBB441"
	 * 	}]
	 * }
	 * 
	 * 
	 * 
	 * 
	 * Output:
	 * 		{"valid":true}
	 * 		OR
	 * 		If validation is unsuccessful
	 * 		{"valid":false,"error":"Cannot construct glance rest client with given credentials"}
	 * </pre>
	 * 
	 * @param imageStoreId
	 * @return Response containing the status
	 */
    @Path("rpc/image-stores/validate")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validateImageStore(ImageStoreTransferObject imageStoreTransferObject)  {
	
	GenericResponse response = new GenericResponse();
	ValidationResponse validationResponse = new ValidationResponse();
	
	
	
	boolean validateConnectorArtifacts = imageStoreService.validateConnectorArtifacts(
			imageStoreTransferObject.getArtifact_types(), imageStoreTransferObject.getConnector());
		GenericResponse genericResponse = new GenericResponse();
		if (!validateConnectorArtifacts) {
		    genericResponse.setError("Connector or Artifacts for the connector are not supported ");
		    genericResponse.setErrorCode(ErrorCode.VALIDATION_FAILED);
		    return Response.status(Status.BAD_REQUEST).entity(genericResponse).build();
		}
	
	try {
	    imageStoreService.validateImageStore(imageStoreTransferObject);
	} catch (DirectorException e) {
	    response.setError(e.getMessage());
	    response.setDetails(e.getMessage());
	    response.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
	    return Response.ok(response).build();
	}

	validationResponse.setValid(true);
	return Response.ok(validationResponse).build();

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
     *                    </pre>
     * 
     * @param imageStoreId
     * @return Response containing the status
     */
    @Path("rpc/image-stores/{imageStoreId: [0-9a-zA-Z_-]+}/validate")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
	public Response validateImageStore(
			@PathParam("imageStoreId") String imageStoreId) {
		ImageStoreTransferObject imageStoreTransferObject = null;
		GenericResponse response = new GenericResponse();
		ValidationResponse validationResponse = new ValidationResponse();
		if (!ValidationUtil.isValidWithRegex(imageStoreId, RegexPatterns.UUID)) {
			response.errorCode = ErrorCode.INAVLID_ID;
			response.error = "Image store id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(response).build();
		}

		try {
			imageStoreTransferObject = persistService
					.fetchImageStorebyId(imageStoreId);
		} catch (DbException e1) {
			log.error("Error fetching image store by id {}", imageStoreId, e1);
		}
		if (imageStoreTransferObject == null) {
			// / GenericResponse response = new GenericResponse();
			response.error = "Invalid image store id";
			response.errorCode = ErrorCode.RECORD_NOT_FOUND;
			return Response.status(Response.Status.NOT_FOUND).entity(response)
					.build();
		}

		imageStoreTransferObject.setIsValid(true);

		try {
			imageStoreService.updateImageStore(imageStoreTransferObject);
		} catch (DirectorException e) {
			response.error = e.getMessage();
			response.errorCode = ErrorCode.REQUEST_PROCESSING_FAILED;
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(response).build();
		}

		try {
			imageStoreService.validateImageStore(imageStoreId);
		} catch (DirectorException e) {
			response.setError(e.getMessage());
			response.setDetails(e.getMessage());
			response.setErrorCode(ErrorCode.REQUEST_PROCESSING_FAILED);
			return Response.ok(response).build();
		}

		validationResponse.setValid(true);
		return Response.ok(validationResponse).build();

	}
    

}
