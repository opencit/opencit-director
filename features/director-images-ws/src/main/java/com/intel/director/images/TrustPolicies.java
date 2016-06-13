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
	 * @mtwSampleRestCall
	 * 
	 * 					<pre>
	 * https://{IP/HOST_NAME}/v1/trust-policy/31DE80A1-875C-4274-BFB2-D26F596A4346
	 * 
	 * Input: pass the UUID of the image as path param
	 * Output: 
	 * VM Policy
	 *  {
			"created_by_user_id": "admin",
			"created_date": "2016-05-16 05:41:46",
			"edited_by_user_id": "admin",
			"edited_date": "2016-05-16 05:41:46",
			"id": "519EABEE-884A-4F1F-B858-22AE35EC39EC",
			"trust_policy": "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns3:TrustPolicy xmlns:ns3=\"mtwilson:trustdirector:policy:1.1\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><ns3:Director><ns3:CustomerId>admin</ns3:CustomerId></ns3:Director><ns3:Image><ns3:ImageId>C97E3F36-D8AC-4491-8D08-23F418E13CB7</ns3:ImageId><ns3:ImageHash>0000000000000000000000000000000000000000000000000000000000000000</ns3:ImageHash></ns3:Image><ns3:LaunchControlPolicy>MeasureOnly</ns3:LaunchControlPolicy><ns3:Whitelist DigestAlg=\"sha256\"/><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>Futuojo3RIy8S++qDBbOJUaM/uA=</DigestValue></Reference></SignedInfo><SignatureValue>GB4GeJ1ddZUH6mN/0VDgHs2YW4x5s3WKro1LZMqUJ6s5RG91jdLMsFVU9GGLW5Voq8RjyyHFywAE\nemlPMdTQ+vrgGjGqtjOrKsNaehS8f6l3k/u1LPGu4Wd8QWFuPOPKp6dpUXQaNX9UIEnZrapZs8hD\nzst/glGeMI3Fdl5TLoexjzSa2zZ9VE3DC+wBf++VdSPQn6Vz90lNFsCXJq+JVi9AznpvT9EFgsuc\ng+uSzDLxxLLifsOCoK6zSR8SFyzVuBOGfGUR1eVfotHCuLjq6lLTniZPSRB61taMF8xA9pHDTKZQ\n5ayWXjNig1B88YZ1Z9M5PjqPmuf/sBA0s5FXHg==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US</X509SubjectName><X509Certificate>MIIDYzCCAkugAwIBAgIEWr8S1zANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwNDI5MTE1MDQxWhcNMjYwNDI3MTE1MDQxWjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCnAGkEJoldD50ENz8lCUMqdSARg2LQOkVTEwUar5/srjMRNbtV8L7MdmW5\nsQYOQhUby6/5bu9x0T1oWJ0tQKJliYYeiJaO2FxzAwsW8aLlfDFdVx3sZOQoIXm7hwCKXc38jHTn\nV5CfNS8eadF+C1XUqjYY1iBN2jkM3pts+gF9RWnF8CX03pa7W6hCXDij1HuqxLDD9kzwADpcPXM6\nz4oDhW64uRUAg3Pc50TjYopbo6B+v5/wdoJt8kXQ5RsgiCbL88U+Fz2Wi5brYR00zTvx5reY1tya\nV03ELeWpfxuQ0cLl//Hknlif1Dvk++C6hqgRtjfckv5jw825C3VyXA9VAgMBAAGjITAfMB0GA1Ud\nDgQWBBS5KKZzika6mcYmOdaJwZzjNgSkvzANBgkqhkiG9w0BAQsFAAOCAQEAI4sZLOU8g6NM9kYn\nnuqTvoFHgW3dEJ0Rv/zW/AigyrTCw8niY++P/GUoS0HoHoqsvrInmP73HjMqj3/4jJdSpwjri4DL\niPm6lG3O2vDsVqmje0x4VqBA5LGJJsExHcf+ElLciiNezVNprWeNXZ916vYkCHCbArMuL/GXnYMw\nUTCix4AJCqsrtYPpQDPaaNrcciugHBFFS4sWgMNluJYrLYutXw1Loe9j5XjkIh/JAWwcO6mgjQds\n6Ok8pe6hPsVrOQlLsPi0uaBbeWt5yTw++iVO5I8hlCpUs8kqctBcxeljhEbzMryx3pt9rp8PaBG0\ncf5Va9rMRJdNR5fUNYMrHQ==</X509Certificate></X509Data></KeyInfo></Signature></ns3:TrustPolicy>",
			"img_attributes": {
				"created_by_user_id": "admin",
				"created_date": "2016-05-16 05:41:35",
				"edited_by_user_id": "admin",
				"edited_date": "2016-05-16 05:41:47",
				"id": "C97E3F36-D8AC-4491-8D08-23F418E13CB7",
				"image_name": "cirros_cc1.img",
				"image_format": "qcow2",
				"image_deployments": "VM",
				"image_size": 13631488,
				"sent": 13631488,
				"deleted": false,
				"upload_variable_md5": "441ad50d1b27e62c017853fc1f56132d",
				"image_upload_status": "Complete",
				"image_Location": "/mnt/images/"
			},
			"display_name": "cirros_cc1.img",
			"archive": false,
			"encrypted": false,
			"image_launch_policy": "MeasureOnly"
		} 
	 * 
	 * 
	 * Docker policy
	 * {
  "created_by_user_id": "admin",
  "created_date": "2016-05-16 06:22:31",
  "edited_by_user_id": "admin",
  "edited_date": "2016-05-16 06:22:31",
  "id": "8E41EA5D-8BE6-4557-8AC4-52F0C5F61819",
  "trust_policy": "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns3:TrustPolicy xmlns:ns3=\"mtwilson:trustdirector:policy:1.1\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><ns3:Director><ns3:CustomerId>admin</ns3:CustomerId></ns3:Director><ns3:Image><ns3:ImageId>BF36833E-15BB-4D65-8FAB-3B8E04684CAA</ns3:ImageId><ns3:ImageHash>cad440e0080e96dab43b95696629e5c55d8c0cde27e566b5d097e21ca44d8d77</ns3:ImageHash></ns3:Image><ns3:LaunchControlPolicy>MeasureOnly</ns3:LaunchControlPolicy><ns3:Whitelist DigestAlg=\"sha256\"><ns3:File Path=\"/bin/acpid\">aa490cd8b89b6fa4c1030d2d6e30b03cd15e69abec9504ad9d07491ba51e9fba</ns3:File></ns3:Whitelist><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>VBNoQJgNbBtirefBsd/bpnt4OqM=</DigestValue></Reference></SignedInfo><SignatureValue>PrrIG+4d46Q/mrD4ouRERYaeACxfprNpap1PaNZ4iisqctLXTi/E2dUVteZ5WEF1/vXnH6WzVrS9\nEBP07OYlg51JpcNc/r0IqVfJf9qjrCuFfr0S8RFqTIkr2j541bP4EHDEeebCSbg+wpgqqxrwuw3h\nB8BMR9YhL9/P+YTtz2Pl++By1jKTpxxy7nXVqU1fSYoCuJcW6XjYCd0oRP7GZyWtZ8q89HHEoyNQ\nnWL3CzIwEwQR6GdrTG8lJmQj5oG6Brt43ggzNX2lhPyGXBeD0IEa9LuYSNACgBdgOV6ZiDCK/fX0\n/ZbTE03xFvojeIjyfkMxiX51fNO6KuFrqkcDpA==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US</X509SubjectName><X509Certificate>MIIDYzCCAkugAwIBAgIEWr8S1zANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwNDI5MTE1MDQxWhcNMjYwNDI3MTE1MDQxWjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCnAGkEJoldD50ENz8lCUMqdSARg2LQOkVTEwUar5/srjMRNbtV8L7MdmW5\nsQYOQhUby6/5bu9x0T1oWJ0tQKJliYYeiJaO2FxzAwsW8aLlfDFdVx3sZOQoIXm7hwCKXc38jHTn\nV5CfNS8eadF+C1XUqjYY1iBN2jkM3pts+gF9RWnF8CX03pa7W6hCXDij1HuqxLDD9kzwADpcPXM6\nz4oDhW64uRUAg3Pc50TjYopbo6B+v5/wdoJt8kXQ5RsgiCbL88U+Fz2Wi5brYR00zTvx5reY1tya\nV03ELeWpfxuQ0cLl//Hknlif1Dvk++C6hqgRtjfckv5jw825C3VyXA9VAgMBAAGjITAfMB0GA1Ud\nDgQWBBS5KKZzika6mcYmOdaJwZzjNgSkvzANBgkqhkiG9w0BAQsFAAOCAQEAI4sZLOU8g6NM9kYn\nnuqTvoFHgW3dEJ0Rv/zW/AigyrTCw8niY++P/GUoS0HoHoqsvrInmP73HjMqj3/4jJdSpwjri4DL\niPm6lG3O2vDsVqmje0x4VqBA5LGJJsExHcf+ElLciiNezVNprWeNXZ916vYkCHCbArMuL/GXnYMw\nUTCix4AJCqsrtYPpQDPaaNrcciugHBFFS4sWgMNluJYrLYutXw1Loe9j5XjkIh/JAWwcO6mgjQds\n6Ok8pe6hPsVrOQlLsPi0uaBbeWt5yTw++iVO5I8hlCpUs8kqctBcxeljhEbzMryx3pt9rp8PaBG0\ncf5Va9rMRJdNR5fUNYMrHQ==</X509Certificate></X509Data></KeyInfo></Signature></ns3:TrustPolicy>",
  "img_attributes": {
    "created_by_user_id": "admin",
    "created_date": "2016-05-16 06:22:15",
    "edited_by_user_id": "admin",
    "edited_date": "2016-05-16 06:22:35",
    "id": "BF36833E-15BB-4D65-8FAB-3B8E04684CAA",
    "image_name": "bl.tar",
    "image_format": "tar",
    "image_deployments": "Docker",
    "image_size": 1322496,
    "sent": 1322496,
    "deleted": false,
    "repository": "busybox",
    "tag": "latest",
    "upload_variable_md5": "e3317460e2ce7a35438dbb1c4904cb69",
    "image_upload_status": "Complete",
    "image_Location": "/mnt/images/"
  },
  "display_name": "busybox:latest",
  "archive": false,
  "encrypted": false,
  "image_launch_policy": "MeasureOnly"
}
	 * 
	 * In case of error:
	 * HTTP 404: Not Found
	 *                    </pre>
	 * 
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
			//trustpolicyresponse = new TrustPolicyResponse();
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
	 * @mtwSampleRestCall
	 * 
	 * 					<pre>
	 * https://{IP/HOST_NAME}/v1/trust-policies/d80ce469-39fd-4940-bb67-c0573551ce4c2
	 * Input: UUID of trust policy in path {"display_name":"policy_renamed"}
	 * Output: 
	 * In case of success : 
	 * {"status":"success"}
	 * 
	 * In case of policy ID in incorrect format or empty:
	 * {
		  "error": "Trust policy ID is not in a UUID format"
		}
	 * 
	 * In case of ID that does not exist
	 * {
		  "error": "Trust Policy does not exist for the given id"
		}
		
		In case the display name is not provided:
		{
		  "error": "Display name is empty or improper format. Name can contain only numbers, alphabets, no spaces and following characters ,;.@_-"
		}
	 *                    </pre>
	 * 
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
	 * 1) When invalid image id is provided
	 * {
		  "error": "No image with id : 7A871544-87E0-42A1-B545-CDC6477DC2F9 exists."
		}
	 *  
	 *  2) When image id provided is not in correct format: 
	 *  {
		  "error": "Image id is empty or not in uuid format"
		}
		
		3) 
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
	 * Output: Content sent as xml document 
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
