/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images;

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
import com.intel.director.api.GenericResponse;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionRequest;
import com.intel.director.api.ImageActionResponse;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.images.exception.DirectorException;
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

	ImageActionService actionService = new ImageActionImpl();
	ImageService imageService = new ImageServiceImpl();
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageActions.class);

	/**
	  This method will fetch an image-action based on actionId on which
	  actions are performed, list of actions to be performed, etc.
	  if actionId do not exist it give 404 Not Found.
	  
	  action_count and action_completed are provided for convenience. These two
	  attributes are guaranteed to have the right data corresponding to the
	  actions collection. action size - is the data uploaded, action_size_max 
	  - total size of the image uploaded
	  
	  The tasks in "action" attribute are processed sequentially. The current
	  task holds the name of the task in the "action" array that is currently
	  being processed.
	  
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall <pre>
	  https://{IP/HOST_NAME}/v1/image-actions/<action_id>
	  Input: PathParam : action_id = CF0A8FA3-F73E-41E9-8421-112FB22BB057
	  Output: {
	  "id": "CF0A8FA3-F73E-41E9-8421-112FB22BB057",
	  "image_id": "08EB37D7-2678-495D-B485-59233EB51996",
	  "action_count": 2,
	  "action_completed": 2,
	  "action_size": 66570,
	  "action_size_max": 66570,
	  "action": [ { "status": "Complete","task_name": "Create Tar"},
	   "status": "Complete", "storename": "Glance", "task_name": "Upload Tar" }],
	  "current_task_status": "Complete",
	  "current_task_name": "Upload Tar" }
	  
	   {
	  "id": "CF0A8FA3-F73E-41E9-8421-112FB22BB057",
	  "image_id": "08EB37D7-2678-495D-B485-59233EB51996",
	  "action_count": 2,
	  "action_completed": 1,
	  "action_size": 66570,
	  "action_size_max": 66570,
	  "action": [ { "status": "Complete","task_name": "Create Tar"},
	   "status": "In Progress", "storename": "Glance", "task_name": "Upload Tar" }],
	  "current_task_status": "Complete",
	  "current_task_name": "Create Tar" }
	  
	  
	  In case of error creating tar : 
	  {
	  "id": "CF0A8FA3-F73E-41E9-8421-112FB22BB057",
	  "image_id": "08EB37D7-2678-495D-B485-59233EB51996",
	  "action_count": 2,
	  "action_completed": 0,
	  "action_size": 66570,
	  "action_size_max": 66570,
	  "action": [ { "status": "Error","task_name": "Create Tar","error":"Error Creating tar"},
	  "status": "Incomplete", "storename": "Glance", "task_name": "Upload Tar" }],
	  "current_task_status": "Error : Error Creating tar ",
	  "current_task_name": "Create Tar" }
	  
	  In case of error uploading tar : 
	  {
	  "id": "CF0A8FA3-F73E-41E9-8421-112FB22BB057",
	  "image_id": "08EB37D7-2678-495D-B485-59233EB51996",
	  "action_count": 2,
	  "action_completed": 1,
	  "action_size": 66570,
	  "action_size_max": 66570,
	  "action": [ { "status": "Complete","task_name": "Create Tar"},
	  "status": "Error", "storename": "Glance", "task_name": "Upload Tar","error":"Error Uploading tar"}],
	  "current_task_status": "Error : Error Uploading tar ",
	  "current_task_name": "Upload Tar" }
	  
	  </pre>
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
					.entity("No image action found for id: " + actionId)
					.build();
			}
			
		} catch (DirectorException e) {
			return Response.status(500).build();
		}
	}

	/**
	 * This method will create an image-action. Data required by this method is
	 * image_id and list of task and other parameter associated with it(Ex.
	 * store_name in case of Upload Tar task). Output contains actionId(id),image_id, actions etc.
	 * 
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall <pre>
	 * https://{IP/HOST_NAME}/v1/image-actions
	 * Input: { "image_id":"08EB37D7-2678-495D-B485-59233EB51996",
	 * "actions":[ {"task_name":"Create Tar"},
	 * {"task_name":"Upload Tar","storename":"Glance"}]
	 * }
	 * Output:
	 * {
	 * 	"id": "CF0A8FA3-F73E-41E9-8421-112FB22BB057",
	 * 	"image_id": "08EB37D7-2678-495D-B485-59233EB51996",
	 * 	"action_count": 2,
	 * 	"action_completed": 2,
	 * 	"action_size": 66570,
	 * 	"action_size_max": 66570,
	 * 	"actions": [ 
	 * 		{
	 * 			"status": "Incomplete",
	 * 			"task_name": "Create Tar"
	 * 		}, {
	 * 			"status": "Incomplete", 
	 * 			"storename": "Glance", 
	 * 			"task_name": "Upload Tar" 
	 * 		}],
	 * 	"current_task_status": "Incomplete",
	 * 	"current_task_name": "Create Tar" 
	 * }
	 * 
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
		ImageActionObject imageActionObject;
		String error=imageActionRequest.vaidate();
		if(!StringUtils.isBlank(error)){
			imageActionResponse.error=error;
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(imageActionResponse).build();
		}
		
		ImageInfo fetchImageById = null;
		try {
			fetchImageById = imageService
					.fetchImageById(imageActionRequest.image_id);
		} catch (DirectorException e1) {
			log.error("unable to fetch image");
		}

		if (fetchImageById == null) {
			imageActionResponse.error = "Image does not exist for id: "
					+ imageActionRequest.image_id;
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(imageActionResponse).build();
		}
		
		try {
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
	 * Output: {"deleted":true,"action_count":0,"action_completed":0,"action_size":0,"action_size_max":0}
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
		ImageActionResponse imageActionResponse = new ImageActionResponse();
		imageActionResponse.setDeleted(true);
	///	GenericResponse genericResponse= new GenericResponse();
		if(!ValidationUtil.isValidWithRegex(actionId,RegexPatterns.UUID)){
			imageActionResponse.error = "Action Id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(imageActionResponse).build();
		}
		ImageActionObject fetchImageAction = null;
		try {
			fetchImageAction = actionService.fetchImageAction(actionId);
		} catch (DirectorException e1) {
			log.error("unable to fetch image action", e1);
		}

		if(fetchImageAction == null){			
			imageActionResponse.error = "Image action does not exist for id "+actionId;
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(imageActionResponse).build();
		}
		try {
			actionService.deleteImageAction(actionId);
		} catch (Exception e) {
			log.error("Error in deleteImageAction", e);
			imageActionResponse.setError("Error in deleteImageAction");
			imageActionResponse.setDeleted(false);
			return Response.ok(imageActionResponse).build();
		}
		return Response.ok(imageActionResponse).build();
	}

}
