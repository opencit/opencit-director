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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.intel.director.api.GenericRequest;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.ImportPolicyTemplateResponse;
import com.intel.director.api.TrustPolicyResponse;
import com.intel.director.api.UpdateTrustPolicyRequest;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageService;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.mtwilson.launcher.ws.ext.V2;

/**
 * Trust Policies related APIs
 * 
 * @author Siddharth
 * 
 */
@V2
@Path("/")
public class TrustPolicies {

	ImageService imageService = new ImageServiceImpl();

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(TrustPolicies.class);

	/**
	 * Get trust policy for the provided trustPolicyId
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall <pre>
	 * https://server.com:8443/v1/trust-policy/08EB37D7-2678-495D-B485-59233EB51996
	 * 
	 * Input: pass the UUID of the image as path param
	 * Output: {
	 *   "created_by_user_id": "admin",
	 *   "created_date": "2015-12-29",
	 *   "edited_by_user_id": "admin",
	 *   "edited_date": "2015-12-29",
	 *   "id": "01e9fe36-5584-45d9-a644-60962976e54c",
	 *   "trust_policy": "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><Director><CustomerId>testId</CustomerId></Director><Image><ImageId>3645ACBB-1529-421D-A6DD-824BCD0002DE</ImageId><ImageHash>90bdb581df9f6869d2b4761d8b9f5708bd1b5a98715abc6e9afafca6866951ef</ImageHash></Image><LaunchControlPolicy>MeasureAndEnforce</LaunchControlPolicy><Whitelist DigestAlg=\"sha256\"><File Path=\"/sbin/blkid\">1a33e58fd757e195a7919ca40d1e1eda19e9e2439d06b6efbe2006ae5e6ef865</File></Whitelist></TrustPolicy>",
	 *   "img_attributes": {
	 *     "created_by_user_id": "admin",
	 *     "created_date": "2015-12-07",
	 *     "edited_by_user_id": "admin",
	 *     "edited_date": "2015-12-29",
	 *     "id": "3645ACBB-1529-421D-A6DD-824BCD0002DE",
	 *     "image_name": "cirros-0.3.1-x86_64-disk_1.img",
	 *     "image_format": "qcow2",
	 *     "image_deployments": "VM",
	 *     "image_size": 13312,
	 *     "sent": 13312,
	 *     "deleted": false,
	 *     "image_upload_status": "Complete",
	 *     "image_Location": "/mnt/images/"
	 *   },
	 *   "display_name": "11_test_test1",
	 *   "encrypted": false,
	 *   "image_launch_policy": "MeasureAndEnforce"
	 * }
	 * 
	 * 
	 * In case of error:
	 * HTTP 404: Not Found
	 * </pre>
	 * @param trustPolicyId
	 * @return trustPolicy
	 */
	@Path("trust-policy/{trustPolicyId: [0-9a-zA-Z_-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getTrustPoliciesData(
			@PathParam("trustPolicyId") String trustPolicyId) {
		TrustPolicyResponse trustpolicyresponse = null;
		try {
			trustpolicyresponse = imageService
					.getTrustPolicyMetaData(trustPolicyId);
			if (trustpolicyresponse == null) {
				return Response.status(Status.NOT_FOUND).build();
			}
		} catch (DirectorException e) {
			log.error("Error in getTrustPoliciesData ", e);
			trustpolicyresponse = new TrustPolicyResponse();
			trustpolicyresponse.setId(null);
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(trustpolicyresponse).build();
	}

	/**
	 * On the step 3/3 of the wizard for VM, when the user clicks on the "Upload
	 * now" button, we accept the last moment changes in the name of the policy
	 * and update it. This method just validates that the name given by the user
	 * is unique
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall <pre>
	 * 
	 * https://server.com:8443/v1/trust-policies/7897-232321-432423-4322
	 * Input: UUID of trust policy in path {"display_name":"Name of policy"}
	 * Output: 
	 * In case of success : 
	 * { "status" : "success"}
	 * 
	 * In case of error such as policy name already exists:
	 * { "error" : "Policy Name Already Exists" }
	 * 
	 * </pre>
	 * @param createPolicyRequest
	 * @return GenericResponse
	 */
	@Path("trust-policies/{trustPolicyId: [0-9a-zA-Z_-]+|}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public GenericResponse updateTrustPolicy(
			@PathParam("trustPolicyId") String trustPolicyId,
			UpdateTrustPolicyRequest updateTrustPolicyRequest) {
		GenericResponse monitorStatus = new GenericResponse();
		monitorStatus.status = Constants.SUCCESS;
		if (StringUtils.isBlank(trustPolicyId)) {
			return monitorStatus;
		}
		try {
			imageService.updateTrustPolicy(updateTrustPolicyRequest,
					trustPolicyId);
		} catch (DirectorException de) {
			log.error("Error updating policy name for : " + trustPolicyId, de);
			monitorStatus.status = Constants.ERROR;
			monitorStatus.details = de.getMessage();
			monitorStatus.setError(de.getMessage());
		}
		return monitorStatus;
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
	 * @mtwSampleRestCall <pre>
	 * https://server.com:8443/v1/rpc/apply-trust-policy-template
	 * Input: {"image_id":"08EB37D7-2678-495D-B485-59233EB51996"}
	 *  
	 * Output: In case of success :: {"trust_policy":"<policy xml>"}
	 *  
	 * In case of error::
	 * {"error":"No image found during import of policy"}
	 *  
	 *  details attribute in output describes the error. If the image id provided does not exist, an error 
	 *  "No image found during import of policy" would be returned in the details.
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
	public ImportPolicyTemplateResponse importPolicyTemplate(GenericRequest req) {
		ImportPolicyTemplateResponse importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
		try {
			importPolicyTemplateResponse = imageService
					.importPolicyTemplate(req.getImage_id());

		} catch (DirectorException e) {
			log.error("Error in importPolicyTemplate ", e);
			// /importPolicyTemplateResponse.setStatus(Constants.ERROR);
			importPolicyTemplateResponse.setError(e.getMessage());
			return importPolicyTemplateResponse;
		}

		return importPolicyTemplateResponse;
	}

	/**
	 * Deletes the signed trust policy by the provided id
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall <pre>
	 * https://server.com:8443/v1/trust-policy/08EB37D7-2678-495D-B485-59233EB51996
	 * Input: UUID of the policy to be deleted
	 * Output: In case of successful deletion:
	 * {"deleted":true}
	 * 
	 * In case of error:
	 * {"deleted":false; "error":"Error in deleting trust policy: <UUID>"}
	 * </pre>
	 * 
	 * @param trustPolicyId
	 * @return GenericResponse
	 * 
	 */
	@Path("trust-policy/{trustPolicyId: [0-9a-zA-Z_-]+}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public GenericResponse deletePolicy(
			@PathParam("trustPolicyId") String trustPolicyId) {
		GenericResponse response = new GenericResponse();
		try {
			imageService.deleteTrustPolicy(trustPolicyId);
			response.setDeleted(true);
		} catch (DirectorException e) {
			log.error("Error in deletePolicy ", e);
			response.setDeleted(false);
			response.setError("Error in deleting trust policy : "
					+ trustPolicyId);
		}
		return response;

	}

}
