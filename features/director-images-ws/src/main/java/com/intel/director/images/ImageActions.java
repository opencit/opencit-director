/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

import java.util.Calendar;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.api.GenericDeleteResponse;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionRequest;
import com.intel.director.api.ImageActionResponse;
import com.intel.director.api.ImageInfoResponse;
import com.intel.director.api.ListImageActionResponse;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ActionServiceBuilder;
import com.intel.director.service.ImageActionService;
import com.intel.director.service.ImageService;
import com.intel.director.service.impl.ImageActionImpl;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.mtwilson.launcher.ws.ext.V2;

/**
 * Image Actions related APIs
 * 
 * @author Siddharth
 * 
 */
@V2
@Path("/")
public class ImageActions {

	ImageService imageService = new ImageServiceImpl();
	ImageActionService actionService = new ImageActionImpl();

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageActions.class);
	
	/**
	 * This method will fetch an image-action based on actionId on which actions
	 * are performed, list of actions to be performed, etc. if actionId do not
	 * exist it give 404 Not Found.
	 * 
	 * action_count and action_completed are provided for convenience. These two
	 * attributes are guaranteed to have the right data corresponding to the
	 * actions collection. action size - is the data uploaded, action_size_max -
	 * total size of the image uploaded
	 * 
	 * The tasks in "action" attribute are processed sequentially. The current
	 * task holds the name of the task in the "action" array that is currently
	 * being processed.
	 * 
	 * Status of the action tasks can be Incomplete,Complete or Error 
	 * depending on result of execution
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall <pre>
	 * 	  https://{IP/HOST_NAME}/v1/image-actions/<action_id>
	 * 	  Input: PathParam : action_id = 808B57E5-1A02-4731-813C-60123F739A85
	 * 	  Output: 
	 * 	  {"id":"808B57E5-1A02-4731-813C-60123F739A85","image_id":"42ECB200-CACB-4D58-AE2D-51FB0413FA67","action_count":3,"action_completed":3,"action_size":0,"action_size_max":0,"actions":[{"status":"Complete","task_name":"Recreate Policy","message":"recreate task completed"},{"status":"Complete","task_name":"Create Tar","message":"Create Tar completed"},{"status":"Complete","task_name":"Upload Tar","message":"Upload Tar complete","store_id":"5506B552-0E88-4C23-8E71-06FD26AF9162"}],"current_task_status":"Complete","current_task_name":"Upload Tar","datetime":1465284021967}
	 * 
	 * Docker image action:
	 * {
		  "id": "C9EF6E99-33A6-43E0-AD8A-A2897FAF4661",
		  "image_id": "BF36833E-15BB-4D65-8FAB-3B8E04684CAA",
		  "action_count": 1,
		  "action_completed": 0,
		  "action_size": 0,
		  "action_size_max": 0,
		  "actions": [
		    {
		      "status": "Incomplete",
		      "task_name": "Upload Image",
		      "store_id": "12A48CDF-604E-4026-9441-9A1EF950DA22"
		    }
		  ],
		  "current_task_status": "Incomplete",
		  "current_task_name": "Upload Image"
		}
	 * </pre>
	 * 
	 * If such action do not exist it will give 404 not found
	 * 
	 * @param actionId
	 * @return ImageActionResponse
	 */

	@Path("image-actions/{actionId: [0-9a-zA-Z_-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response fetchImageAction(@PathParam("actionId") String actionId) {
		try {
			GenericResponse genericResponse= new GenericResponse();
			if(!ValidationUtil.isValidWithRegex(actionId,RegexPatterns.UUID)){
				genericResponse.error = "Action Id is empty or not in uuid format";
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(genericResponse).build();
			}
			if(actionService.fetchImageAction(actionId)!=null){
				return Response.ok(actionService.fetchImageAction(actionId))
						.build();
					
			}else{
				return  Response.status(404)
					.build();
			}
			
		} catch (DirectorException e) {
			return Response.status(500).build();
		}
	}

	/**
	 * This method will create an image-action. Data required by this method is
	 * image_id and artifacts and store id user want to upload. Output contains
	 * actionId(id),image_id, actions etc.
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall <pre>
	 * https://{IP/HOST_NAME}/v1/image-actions
	 * 
	 * Input:
	 * {"artifact_store_list":[{"artifact_name":"Tarball","image_store_id":"5506B552-0E88-4C23-8E71-06FD26AF9162"}],"image_id":"42ECB200-CACB-4D58-AE2D-51FB0413FA67"}
	 *  
	 * Output:
	 * {"id":"808B57E5-1A02-4731-813C-60123F739A85","image_id":"42ECB200-CACB-4D58-AE2D-51FB0413FA67","action_count":3,"action_completed":0,"action_size":0,"action_size_max":0,"actions":[{"status":"Incomplete","task_name":"Recreate Policy"},{"status":"Incomplete","task_name":"Create Tar"},{"status":"Incomplete","task_name":"Upload Tar","store_id":"5506B552-0E88-4C23-8E71-06FD26AF9162"}],"current_task_status":"Incomplete","current_task_name":"Recreate Policy","created_date_time":1465283994090,"deleted":false}
	 * 
	 * 
	 * In case of error:
	 * {
	 *   "error": "Image does not exist for id: 1AFBA2F5-C02E-420E-9842-C455BB35B332"
	 * }
	 * 
	 * </pre>
	 * 
	 * @param imageActionRequest
	 * @return ImageActionResponse containing action_id.
	 */

	@Path("image-actions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response createImageAction(
			ImageActionRequest imageActionRequest) {
		ImageActionResponse imageActionResponse = new ImageActionResponse();
		GenericResponse genericResponse= new GenericResponse();
		ImageActionObject imageActionObject;
		String error=imageActionRequest.validate();
		if(!StringUtils.isBlank(error)){
			genericResponse.error=error;
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}
		
		ImageInfo fetchImageById = null;;
		try {
			fetchImageById = imageService
					.fetchImageById(imageActionRequest.image_id);
		} catch (DirectorException e1) {
			log.error("unable to fetch image");
		}

		if (fetchImageById == null) {
			genericResponse.error = "Image does not exist for id: "
					+ imageActionRequest.image_id;
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}
		
		try {
			
			actionService = ActionServiceBuilder.build(imageActionRequest
					.getImage_id());
			imageActionObject = actionService
					.createImageAction(imageActionRequest);
		} catch (DirectorException e) {
			log.error("Error in createImageAction", e);
			imageActionResponse.setError(e.getMessage());
			return Response.ok(imageActionResponse).build();
		}
		imageActionResponse.setId(imageActionObject.getId());
		imageActionResponse.setImage_id(imageActionObject.getImage_id());
		imageActionResponse.setAction_completed(imageActionObject
				.getAction_completed());
		imageActionResponse
				.setAction_count(imageActionObject.getAction_count());
		imageActionResponse.setActions(imageActionObject.getActions());
		imageActionResponse.setCurrent_task_name(imageActionObject
				.getCurrent_task_name());
		imageActionResponse.setCurrent_task_status(imageActionObject
				.getCurrent_task_status());
		imageActionResponse.setCreatedDateTime(Calendar.getInstance());
		return Response.ok(imageActionResponse).build();
	}

	/**
	 * This method will delete existing image-action. Data required by this
	 * method is action_id. Output will contain status of delete task initiated.
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall <pre>
	 * https://{IP/HOST_NAME}/v1/image-actions
	 * Input: PathParam =  actionId : CF0A8FA3-F73E-41E9-8421-112FB22BB057
	 * Output: {"deleted":true}
	 * 
	 * </pre>
	 * 
	 * @return Status of delete operation
	 */
	@Path("image-actions/{actionId: [0-9a-zA-Z_-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@DELETE
	public Response deleteImageAction(
			@PathParam("actionId") String actionId) {
		GenericDeleteResponse imageDeleteResponse = new GenericDeleteResponse();
		GenericResponse genericResponse= new GenericResponse();
		ImageActionObject imageActionObject;
		imageDeleteResponse.setDeleted(true);
	///	GenericResponse genericResponse= new GenericResponse();
		if(!ValidationUtil.isValidWithRegex(actionId,RegexPatterns.UUID)){
			genericResponse.error = "Action Id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}
		ImageActionObject fetchImageAction = null;
		try {
			fetchImageAction = actionService.fetchImageAction(actionId);
		} catch (DirectorException e1) {
			log.error("unable to fetch image action", e1);
		}

		if(fetchImageAction == null){			
			genericResponse.error = "Image action does not exist for id "+actionId;
			return Response.status(Response.Status.NOT_FOUND)
					.entity(genericResponse).build();
		}
		try {
			actionService.deleteImageAction(actionId);
		} catch (Exception e) {
			log.error("Error in deleteImageAction", e);
			imageDeleteResponse.setError("Error in deleteImageAction");
			imageDeleteResponse.setDeleted(false);
			return Response.ok(imageDeleteResponse).build();
		}
		return Response.ok(imageDeleteResponse).build();
	}
	
	
	/**
	 * This method will fetch image upload history of given imageId.
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * 	  https://{IP/HOST_NAME}/v1/image-actions/history/64E8AFCC-182F-42C9-8A7B-42AD3C93EDCF
	 * 	  Input: PathParam : imageId = 64E8AFCC-182F-42C9-8A7B-42AD3C93EDCF
	 * 	  Output:
	 * 	{
	 * 		"image_action_history_list":[
	 * 			{"store_names":"Glance-36","execution_status":"Complete","id":"30869EF3-9809-48F6-AC36-21994318313F","artifact_name":"Image With Policy As Tarball","datetime":"2016 Mar 22 12:30:46"},
	 * 			{"store_names":"Glance-36","execution_status":"Complete","id":"689AF185-2232-4E61-A1ED-2435FF7DF337","artifact_name":"Image With Policy As Tarball","datetime":"2016 Mar 18 15:34:36"}
	 * 		]
	 * 	}
	 * </pre>
	 * 
	 *                    It will give 404 id image do not exist with the given
	 *                    id: { "error": "Image with the id do not exist" }
	 * 
	 * @param imageId
	 *            imageId as PathParam
	 * @return Response containing list of image-action-history for given
	 *         imageId
	 */
	@Path("image-actions/history/{imageId: [0-9a-zA-Z_-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getImageActionHistory(@PathParam("imageId") String imageId) {
		ListImageActionResponse imageActionResponseList = new ListImageActionResponse();
		GenericResponse genericResponse= new GenericResponse();
		if (!ValidationUtil.isValidWithRegex(imageId, RegexPatterns.UUID)) {
			genericResponse.error = "ImageId is empty or not in uuid format";
			
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}
	
		ImageInfoResponse imageInfoResponse=null;
		try {
			imageInfoResponse = imageService
					.getImageDetails(imageId);
		} catch (DirectorException e1) {
			log.error("getImageDetails failed",e1);
			return Response.status(500).build();	
		}
		if (imageInfoResponse == null) {
			genericResponse.error = "Image with the id do not exist";
			return Response.status(Response.Status.NOT_FOUND)
					.entity(genericResponse).build();
		}
		try {
			imageActionResponseList.setImageActionResponseList(actionService
					.getImageActionHistory(imageId));

		} catch (Exception e) {
			log.error("getImageActionHistory failed",e);
			return Response.status(500).build();
		}
		
		return Response.ok(imageActionResponseList).build();

	}

}
