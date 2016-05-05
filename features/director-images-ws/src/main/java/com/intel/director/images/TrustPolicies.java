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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.api.GenericDeleteResponse;
import com.intel.director.api.GenericRequest;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.ImportPolicyTemplateResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyResponse;
import com.intel.director.api.UpdateTrustPolicyRequest;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.exception.DirectorException;
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
	 * https://{IP/HOST_NAME}/v1/trust-policy/31DE80A1-875C-4274-BFB2-D26F596A4346
	 * 
	 * Input: pass the UUID of the image as path param
	 * Output: {
	 *   		"created_by_user_id": "admin",
	 *   "created_date": "2016-05-04 10:42:00",
	 *   "edited_by_user_id": "admin",
	 *   "edited_date": "2016-05-04 10:42:00",
	 *   "id": "31DE80A1-875C-4274-BFB2-D26F596A4346",
	 *   "trust_policy": "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TrustPolicy xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:ns3=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><Director><CustomerId>admin</CustomerId></Director><Image><ImageId>1AFBA2F5-C02E-420E-9842-C455BB35B334</ImageId><ImageHash>9a6f4091e78a41a812a659adbb26bbebfe1ac278638e02dc3924313c88acf94b</ImageHash></Image><LaunchControlPolicy>MeasureOnly</LaunchControlPolicy><Whitelist DigestAlg=\"sha256\"><File Path=\"/init\">c36d3e03001121541e75e591b4cfa7e113af6ac159d6ed5d9d821cf714d3f283</File></Whitelist><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>DrBczDtH8fwU1OZ4YeKDzgM7hmE=</DigestValue></Reference></SignedInfo><SignatureValue>fio/1vOcOZXGVZEDs1qrEJMr+YXNxOXt1f9VOHcFSP8llqcryQppMQxD+LtpCHvDtZrvZoc7Sipm\nqD7BDnQm84Y+RG9PEIUIeK8OnT8AZnY4ZmYiQE2K8xCSJBunVsRh1tXtYCU1Wpb5pZ3zQpXL9XBx\nP6uae6nScNpbIzNTYyqL21gmBYCf39g3u4aOWEjy1pkUZDlfxOEaJDnx/LawssZM0eTk/5dxvGaQ\niz/gGUMqj5rReWrIPoIGzs0rYfEPn5ztKAhJcDwQ/O5AKn5Ig3V4lEFiXppW1Zpvay/oVMCt+QVI\nlxqYyAkEhbwsWpXHLpyMeoZ95dwxWEXlvro3fw==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US</X509SubjectName><X509Certificate>MIIDYzCCAkugAwIBAgIEWr8S1zANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwNDI5MTE1MDQxWhcNMjYwNDI3MTE1MDQxWjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCnAGkEJoldD50ENz8lCUMqdSARg2LQOkVTEwUar5/srjMRNbtV8L7MdmW5\nsQYOQhUby6/5bu9x0T1oWJ0tQKJliYYeiJaO2FxzAwsW8aLlfDFdVx3sZOQoIXm7hwCKXc38jHTn\nV5CfNS8eadF+C1XUqjYY1iBN2jkM3pts+gF9RWnF8CX03pa7W6hCXDij1HuqxLDD9kzwADpcPXM6\nz4oDhW64uRUAg3Pc50TjYopbo6B+v5/wdoJt8kXQ5RsgiCbL88U+Fz2Wi5brYR00zTvx5reY1tya\nV03ELeWpfxuQ0cLl//Hknlif1Dvk++C6hqgRtjfckv5jw825C3VyXA9VAgMBAAGjITAfMB0GA1Ud\nDgQWBBS5KKZzika6mcYmOdaJwZzjNgSkvzANBgkqhkiG9w0BAQsFAAOCAQEAI4sZLOU8g6NM9kYn\nnuqTvoFHgW3dEJ0Rv/zW/AigyrTCw8niY++P/GUoS0HoHoqsvrInmP73HjMqj3/4jJdSpwjri4DL\niPm6lG3O2vDsVqmje0x4VqBA5LGJJsExHcf+ElLciiNezVNprWeNXZ916vYkCHCbArMuL/GXnYMw\nUTCix4AJCqsrtYPpQDPaaNrcciugHBFFS4sWgMNluJYrLYutXw1Loe9j5XjkIh/JAWwcO6mgjQds\n6Ok8pe6hPsVrOQlLsPi0uaBbeWt5yTw++iVO5I8hlCpUs8kqctBcxeljhEbzMryx3pt9rp8PaBG0\ncf5Va9rMRJdNR5fUNYMrHQ==</X509Certificate></X509Data></KeyInfo></Signature></TrustPolicy>",
	 *   "img_attributes": {
	 *     "created_by_user_id": "admin",
	 *     "created_date": "2016-05-02 05:45:44",
	 *     "edited_by_user_id": "admin",
	 *     "edited_date": "2016-05-04 10:42:01",
	 *     "id": "1AFBA2F5-C02E-420E-9842-C455BB35B334",
	 *     "image_name": "cirros.img",
	 *     "image_format": "qcow2",
	 *     "image_deployments": "VM",
	 *     "image_size": 13287936,
	 *     "sent": 13287936,
	 *     "deleted": false,
	 *     "upload_variable_md5": "110db09830b79d60c9faf056d7bdb2f5",
	 *     "image_upload_status": "Complete",
	 *     "image_Location": "/mnt/images/"
	 *   },
	 *   "display_name": "cirros.img-v5",
	 *   "archive": false,
	 *   "encrypted": false,
	 *   "image_launch_policy": "MeasureOnly"
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
		TrustPolicyResponse trustpolicyresponse = new TrustPolicyResponse();
		if(!ValidationUtil.isValidWithRegex(trustPolicyId,RegexPatterns.UUID)){
			GenericResponse genericResponse=new GenericResponse();
			genericResponse.error = "Trust Policy Id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}

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
	 * This method is used to update the trust policy display name.
	 * 
	 * On the step 3/3 of the wizard for VM, when the user clicks on the "Upload
	 * now" button, we accept the last moment changes in the name of the policy
	 * and update it. 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall <pre>
	 * https://{IP/HOST_NAME}/v1/trust-policies/d80ce469-39fd-4940-bb67-c0573551ce4c2
	 * Input: UUID of trust policy in path {"display_name":"policy_renamed"}
	 * Output: 
	 * In case of success : 
	 * {"status":"success"}
	 * 
	 * In case of wrong input"
	 * {
  "error": "Trust policy ID is not in a UUID format"
}
	 * 
	 * </pre>
	 * @param updateTrustPolicyRequest
	 * @return GenericResponse
	 */
	@Path("trust-policies/{trustPolicyId: [0-9a-zA-Z_-]+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response updateTrustPolicy(
			@PathParam("trustPolicyId") String trustPolicyId,
			UpdateTrustPolicyRequest updateTrustPolicyRequest) {
		GenericResponse monitorStatus = new GenericResponse();
		String errors = updateTrustPolicyRequest.validate(trustPolicyId);
		if (StringUtils.isNotBlank(errors)) {
			monitorStatus.error = errors;
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(monitorStatus).build();
		}
		

		TrustPolicy trustPolicyByTrustId = imageService.getTrustPolicyByTrustId(trustPolicyId);
		if(trustPolicyByTrustId == null){
			monitorStatus.error = "Trust Policy does not exist for the given id";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(monitorStatus).build();
		}
		
		try {
			imageService.updateTrustPolicy(updateTrustPolicyRequest,
					trustPolicyId);
			monitorStatus.status = Constants.SUCCESS;
		} catch (DirectorException de) {
			log.error("Error updating policy name for : " + trustPolicyId, de);
			//monitorStatus.status = Constants.ERROR;
			//monitorStatus.details = de.getMessage();
			monitorStatus.setError(de.getMessage());
		}
		return Response.ok(monitorStatus).build();
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
	 * https://{IP/HOST_NAME}/v1/rpc/apply-trust-policy-template
	 * Input: {"image_id":"08EB37D7-2678-495D-B485-59233EB51996"}
	 *  
	 * Output: In case of success :: {"trust_policy":"<policy xml>"}
	 *  
	 * In case of error::
	 * {
  "error": "No image with id : 7A871544-87E0-42A1-B545-CDC6477DC2F9 exists."
}
	 *  
	 *  details attribute in output describes the error. If the image id provided does not exist, an error 
	 *  "No image found during import of policy" would be returned in the details.
	 * 
	 * </pre>
	 * 
	 * @return Response that sends back the status of the function.
	 */
	@Path("rpc/apply-trust-policy-template/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response importPolicyTemplate(GenericRequest req) {
		ImportPolicyTemplateResponse importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
		if(!ValidationUtil.isValidWithRegex(req.getImage_id(),RegexPatterns.UUID)){
			importPolicyTemplateResponse.error = "Image id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(importPolicyTemplateResponse).build();
		}
		
		ImageInfo imageInfo=null;
		try {
			imageInfo = imageService.fetchImageById(req.getImage_id());
		} catch (DirectorException e1) {
			log.error("Unable to fetch image", e1);
		}
		if(imageInfo == null){
			importPolicyTemplateResponse.error = "No image with id : "+req.getImage_id()+" exists.";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(importPolicyTemplateResponse).build();
		}else if (!imageInfo.getImage_deployments().equals(Constants.DEPLOYMENT_TYPE_BAREMETAL)){
			importPolicyTemplateResponse.error = "Templates can only be applied to non virtualized servers.";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(importPolicyTemplateResponse).build();
		}
		try {
			importPolicyTemplateResponse = imageService
					.importPolicyTemplate(req.getImage_id());

		} catch (DirectorException e) {
			log.error("Error in importPolicyTemplate ", e);
			// /importPolicyTemplateResponse.setStatus(Constants.ERROR);
			importPolicyTemplateResponse.setError(e.getMessage());
			return Response.ok(importPolicyTemplateResponse).build();
		}

		return Response.ok(importPolicyTemplateResponse).build();
	}

	/**
	 * Deletes the signed trust policy by the provided id
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * https://{IP/HOST_NAME}/v1/trust-policy/08EB37D7-2678-495D-B485-59233EB51996
	 * Input: UUID of the policy to be deleted
	 * Output: In case of successful deletion:
	 * {"deleted":true}
	 * 
	 * In case of error:
	 * {"deleted":false; "error":"Error in deleting trust policy: <UUID>"}
	 *                    </pre>
	 * 
	 * @param trustPolicyId
	 * @return GenericResponse
	 * 
	 */
	@Path("trust-policy/{trustPolicyId: [0-9a-zA-Z_-]+}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deletePolicy(
			@PathParam("trustPolicyId") String trustPolicyId) {
		GenericDeleteResponse response = new GenericDeleteResponse();
		if(!ValidationUtil.isValidWithRegex(trustPolicyId,RegexPatterns.UUID)){
			response.error = "Trust Policy Id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(response).build();
		}
		
		TrustPolicy trustPolicyByTrustId = imageService.getTrustPolicyByTrustId(trustPolicyId);
		if(trustPolicyByTrustId == null){
			response.error = "Trust Policy does not exist for id: "+trustPolicyId;
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(response).build();
		}
		
		try {
			imageService.deleteTrustPolicy(trustPolicyId);
			response.setDeleted(true);
		} catch (DirectorException e) {
			log.error("Error in deletePolicy ", e);
			response.setDeleted(false);
			response.setError("Error in deleting trust policy : "
					+ trustPolicyId);
		}
		return Response.ok(response).build();
	}
	
	/**
	 * 
	 * This method looks into the MW_TRUST_POLICY table and gets the policy
	 * string and sends it as an xml content to the user.
	 * 
	 * In case the policy is not found for the trust policy id, HTTP 404 is
	 * returned
	 * 
	 * @mtwContentTypeReturned XML
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 *  https://{IP/HOST_NAME}/v1/trust-policies/08EB37D7-2678-495D-B485-59233EB51996/download
	 * Input: Trust policy id as path param
	 * Output: Content sent as stream
	 * 
	 *                    </pre>              
	 * @param trustPolicyId
	 *            the trust policy for which the policy is downloaded
	 * @return XML content of the policy
	 * @throws DirectorException
	 */
	@Path("trust-policies/{trustPolicyId: [0-9a-zA-Z_-]+}/download")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response downloadPolicyForTrustPolicyId(
			@PathParam("trustPolicyId") String trustPolicyId) {
		GenericResponse genericResponse= new GenericResponse();
		if(!ValidationUtil.isValidWithRegex(trustPolicyId,RegexPatterns.UUID)){
			genericResponse.error = "Trust Policy id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}
		
		TrustPolicy trustPolicy = imageService.getTrustPolicyByTrustId(trustPolicyId);
		
		if(trustPolicy == null){
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		ResponseBuilder response = Response.ok(trustPolicy.getTrust_policy());
		response.header("Content-Disposition", "attachment; filename=policy_"
				+ trustPolicy.getImgAttributes().getImage_name() + ".xml");
		return response.build();
	}

}
