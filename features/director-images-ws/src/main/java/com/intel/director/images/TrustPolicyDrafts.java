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

import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.CreateTrustPolicyResponse;
import com.intel.director.api.GenericRequest;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.ListTrustPolicyDrafts;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageService;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.mtwilson.launcher.ws.ext.V2;

/**
 * Trust Policy Drafts related APIs
 * 
 * @author Siddharth
 * 
 */
@V2
@Path("/")
public class TrustPolicyDrafts {

	ImageService imageService = new ImageServiceImpl();

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(TrustPolicyDrafts.class);

	/**
	 * Retrieves all trust_policy_drafts
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall <pre>
	 * https://{IP/HOST_NAME}/v1/trust-policy-drafts
	 * Input: NA
	 * 
	 * Output:
	 * {
	 *   "trust_policy_drafts": [
	 *     {
	 *       "created_by_user_id": "admin",
	 *       "created_date": "2015-12-29",
	 *       "edited_by_user_id": "admin",
	 *       "edited_date": "2015-12-29",
	 *       "id": "ac3044b0-e842-4fc1-a4b6-8b41a8be9b66",
	 *       "trust_policy_draft": "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><Director><CustomerId>testId</CustomerId></Director><Image><ImageId>4439C209-6CD8-40DA-801B-E91DA0D3E639</ImageId><ImageHash>427b8344669ccd10697225768b2bbc9d9cad0517440f358112f8b6e8118c7ba4</ImageHash></Image><LaunchControlPolicy>MeasureAndEnforce</LaunchControlPolicy><Whitelist DigestAlg=\"sha256\"><File Path=\"/boot/grub/menu.lst\"></File></Whitelist></TrustPolicy>",
	 *       "display_name": "cirros-pme",
	 *       "image_attributes": {
	 *         "created_by_user_id": "admin",
	 *         "created_date": "2015-12-17",
	 *         "edited_by_user_id": "admin",
	 *         "edited_date": "2015-12-29",
	 *         "id": "4439C209-6CD8-40DA-801B-E91DA0D3E639",
	 *         "image_name": "cirros-gauri",
	 *         "image_format": "qcow2",
	 *         "image_deployments": "VM",
	 *         "image_size": 12976,
	 *         "sent": 12976,
	 *         "deleted": false,
	 *         "image_upload_status": "Complete",
	 *         "image_Location": "/mnt/images/"
	 *       }
	 *     },
	 *     {
	 *       "created_by_user_id": "admin",
	 *       "created_date": "2015-12-17",
	 *       "edited_by_user_id": "admin",
	 *       "edited_date": "2015-12-29",
	 *       "id": "5b287b2d-c8c2-4550-9d8e-6154b4e41b1f",
	 *       "trust_policy_draft": "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><Director><CustomerId>testId</CustomerId></Director><Image><ImageId>BD14A8F7-4423-413E-B12E-13E9EBC37925</ImageId><ImageHash>6a749cfc57df53bd36b6ce1cb67c54632be4bc904d084ddd8dbeb79a30f44ccc</ImageHash>    </Image>    <LaunchControlPolicy>MeasureOnly</LaunchControlPolicy><Whitelist DigestAlg=\"sha256\"><File Path=\"/boot/grub/stage2\"></File></Whitelist></TrustPolicy>",
	 *       "display_name": "cirrus.img",
	 *       "image_attributes": {
	 *         "created_by_user_id": "admin",
	 *         "created_date": "2015-12-16",
	 *         "edited_by_user_id": "admin",
	 *         "edited_date": "2015-12-16",
	 *         "id": "BD14A8F7-4423-413E-B12E-13E9EBC37925",
	 *         "image_name": "cirrus.img",
	 *         "image_format": "qcow2",
	 *         "image_deployments": "VM",
	 *         "image_size": 13312,
	 *         "sent": 13312,
	 *         "deleted": true,
	 *         "image_upload_status": "Complete",
	 *         "image_Location": "/mnt/images/"
	 *       }
	 *     }
	 *   ]
	 * }
	 * </pre>
	 * 
	 * @return List of trust policy drafts
	 * @throws DirectorException
	 */
	@Path("trust-policy-drafts")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ListTrustPolicyDrafts getPolicyDraftList() throws DirectorException {
		ListTrustPolicyDrafts listTrustPolicyDrafts = new ListTrustPolicyDrafts();
		listTrustPolicyDrafts.setTrust_policy_drafts(imageService
				.getTrustPolicyDrafts(null));
		return listTrustPolicyDrafts;
	}

	/**
	 * Retrieves trust policy draft based on uuid provided
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType GET
	 * @mtwSampleRestCall <pre>
	 *  https://{IP/HOST_NAME}/v1/trust-policy-drafts/ACD7747D-79BE-43E3-BAA5-7DBEC13D272
	 *  
	 * Input: PathParam String: trustPolicyDraftId=ACD7747D-79BE-43E3-BAA5-7DBEC13D272
	 * 
	 * Output: {
	 * "launch_control_policy":"MeasureAndEnforce",
	 * "encrypted":true,
	 * "image_name":"cirrus_1811.img",
	 * "display_name":"111"
	 * "trust_policy": "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TrustPolicy xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><Director><CustomerId>testId</CustomerId></Director><Image><ImageId>05EECBC5-C8BA-4523-A891-7AF455FAFAAB</ImageId></Image><LaunchControlPolicy>MeasureAndEnforce</LaunchControlPolicy><Whitelist DigestAlg=\"sha256\"><File Path=\"/lib/modules/3.2.0-37-virtual/modules.isapnpmap\" />    </Whitelist></TrustPolicy>"
	 * }
	 * 
	 * If no draft exist for corresponding uuid HTTP 404 NOT Found is returned
	 * 
	 * </pre>
	 * 
	 * @param trustPolicyDraftId
	 *            ID of the trust policy draft
	 * @return trust policy draft details for the requested ID
	 */
	@Path("trust-policy-drafts/{trustPolicyDraftId:[0-9a-zA-Z_-]+ }")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPolicyDraft(
			@PathParam("trustPolicyDraftId") String trustPolicyDraftId) {

		CreateTrustPolicyMetaDataResponse createTrustPolicyMetaDataResponse = null;

		try {
			createTrustPolicyMetaDataResponse = imageService
					.getPolicyMetadata(trustPolicyDraftId);
			if (createTrustPolicyMetaDataResponse == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (DirectorException e) {
			createTrustPolicyMetaDataResponse = new CreateTrustPolicyMetaDataResponse();
			createTrustPolicyMetaDataResponse.setStatus(Constants.ERROR);
			createTrustPolicyMetaDataResponse.setDetails(e.getMessage());
			log.error("Error in trust-policy-drafts/" + trustPolicyDraftId);
			return Response.ok(createTrustPolicyMetaDataResponse).build();
		}
		return Response.ok(createTrustPolicyMetaDataResponse).build();

	}

	/**
	 * Update the policy draft by applying the patch. Whenever an user selects
	 * directories on the UI, a patch of the changes made to the selections is
	 * sent to the server every 10 mins. this method does the job of merging the
	 * patch/delta into the trust policy draft that is stored in the database.
	 * 
	 * A sample patch looks like this: <patch> <add sel="<node selector>" ><File
	 * path="<file path>"></add> <remove sel="<node selector>" ></remove>
	 * </patch>
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
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 * https://{IP/HOST_NAME}/v1/trust-policy-drafts/97c4b9d2-d0e6-42b5-a4e2-1642b01db21f
	 * Input: UUID of the image in path
	 * {"patch":
	 * "<patch></patch>"
	 * }
	 * 
	 * Patch examples:-
	 * 
	 * i)Selecting initrd.img file
	 * {"patch":"<patch><add pos=\"prepend\" sel='//*[local-name()=\"Whitelist\"]'><File Path=\"/initrd.img\"/></add></patch>"}
	 * 
	 * ii)Applying regex on boot folder :-
	 * Include filter:- *.gz
	 * 
	 * {"patch":"<patch><add sel='//*[local-name()=\"Whitelist\"]'><Dir Path=\"/boot\" Include=\"*.gz\" Exclude=\"\" Recursive=\"false\"/>
	 * </add><add pos=\"after\" sel='//*[local-name()=\"Whitelist\"]/*[local-name()=\"Dir\"][@Path=\"/boot\"]'><File Path=\"/boot/tboot.gz\"/>
	 * </add></patch>"}
	 * 
	 * Output: 
	 * {
	 *   "created_by_user_id": "admin",
	 *   "created_date": "2015-12-21",
	 *   "edited_by_user_id": "admin",
	 *   "edited_date": 1450686737378,
	 *   "id": "97c4b9d2-d0e6-42b5-a4e2-1642b01db21f",
	 *   "trust_policy_draft": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TrustPolicy xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n    <Director>\n        <CustomerId>testId</CustomerId>\n    </Director>\n    <Image>\n        <ImageId>5A56C717-EF21-4A6E-8701-08EE0FF4C620</ImageId>\n        <ImageHash>6a749cfc57df53bd36b6ce1cb67c54632be4bc904d084ddd8dbeb79a30f44ccc</ImageHash>\n    </Image>\n    <LaunchControlPolicy>MeasureOnly</LaunchControlPolicy>\n    <Encryption>\n        <Key URL=\"uri\">http://10.35.35.53/v1/keys/013890ed-9af3-4d87-bfe7-26a7f6605ee3/transfer</Key>\n        <Checksum DigestAlg=\"md5\">cd5b437a55ae166aecd8c2fe2cf4ce29</Checksum>\n    </Encryption>\n    <Whitelist DigestAlg=\"sha256\">\n        <File Path=\"/boot/grub/menu.lst\" />\n        <File Path=\"/boot/vmlinuz-3.2.0-37-virtual\" />\n        <File Path=\"/boot/config-3.2.0-37-virtual\" />\n        <File Path=\"/boot/initrd.img-3.2.0-37-virtual\" />\n        <File Path=\"/boot/grub/stage1\" />\n        <File Path=\"/boot/grub/e2fs_stage1_5\" />\n        <File Path=\"/boot/grub/stage2\" />\n    </Whitelist>\n</TrustPolicy>\n",
	 *   "display_name": "CIRROS_ETE_1.img"
	 * }
	 * 
	 * In case of error:
	 *  Output: {"status":"Error", details:"<Cause of error>"}
	 * 
	 * </pre>
	 * 
	 * @param imageId
	 *            Id of image whose draft is to be edited
	 * @param trustPolicyDraftEditRequest
	 *            Object that maps teh patch, image id and the trust policy
	 *            draft id attributes
	 * @return Updated policy
	 * @throws DirectorException
	 */
	@Path("trust-policy-drafts/{trustPolicyDraftId: [0-9a-zA-Z_-]+}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TrustPolicyDraft editPolicyDraft(
			@PathParam("trustPolicyDraftId") String trustPolicyDraftId,
			TrustPolicyDraftEditRequest trustPolicyDraftEditRequest) {
		trustPolicyDraftEditRequest.trust_policy_draft_id = trustPolicyDraftId;
		TrustPolicyDraft policyDraft = null;
		String trustPolicyDraftXML = null;
		try {
			policyDraft = imageService
					.editTrustPolicyDraft(trustPolicyDraftEditRequest);
			policyDraft.setImgAttributes(null);
			/*
			 * trustPolicyDraftXML= policyDraft.getTrust_policy_draft();
			 * response.setStatus(Constants.SUCCESS);
			 * response.setDetails(trustPolicyDraftXML);
			 */
			log.debug("Updated policy draft trustPolicyXML : "
					+ trustPolicyDraftXML);
		} catch (Exception e) {
			// /response.setStatus(Constants.ERROR);
			policyDraft = new TrustPolicyDraft();
			policyDraft.setError(e.getMessage());
			log.error("Error while updating policy draft : "
					+ trustPolicyDraftId);
		}

		return policyDraft;
	}

	/**
	 * When the user has finished selecting files and dirs and clicks on the
	 * Next button for creating a policy, we call this method to : 1) Sign with
	 * MTW 2) Generate Hashes
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall <pre>
	 *  https://{IP/HOST_NAME}/v1/rpc/finalize-trust-policy-draft
	 *  
	 * Input
	 * {"trust_policy_draft_id":"<UUID of trust policy draft>"}
	 * In case of a success, the response would be :
	 * {"id":"14767a34-b5a4-4f84-be7a-7604670fe8b5"}
	 * id returned in case of success response is id of trust policy created 
	 * by this call by signing draft and generating hashes 
	 * 
	 * In case of error where signing with MTW fails: 
	 * {"error":"Unable to sign the policy with MTW"}.
	 * </pre>
	 * 
	 * 
	 * @param CreateTrustPolicyMetaDataRequest
	 *            CreateTrustPolicyMetaDataRequest containing trust_policy_draft_id 
	 * @return id of created trust policy
	 */
	@Path("rpc/finalize-trust-policy-draft")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public CreateTrustPolicyResponse createTrustPolicy(
			CreateTrustPolicyMetaDataRequest createPolicyRequest) {
		CreateTrustPolicyResponse response = new CreateTrustPolicyResponse();
		try {
			String imageId = imageService
					.getImageByTrustPolicyDraftId(createPolicyRequest.trust_policy_draft_id);
			String trustPolicyId = imageService
					.createTrustPolicy(createPolicyRequest.trust_policy_draft_id);
			response.setId(trustPolicyId);
			imageService.deletePasswordForHost(imageId);
			// / response.setStatus(Constants.SUCCESS);
		} catch (DirectorException de) {
			// //response.setStatus(Constants.ERROR);
			response.setError(de.getMessage());
			log.error("Error creating policy from draft for image : "
					+ createPolicyRequest.image_id, de);
		}
		return response;
	}

	/**
	 * 
	 * Creates an initial trust policy draft for image. 
	 * 
	 * image_id,display_name and launch_control_policy are mandatory fields
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall <pre>
	 * https://{IP/HOST_NAME}/v1/trust-policy-drafts
	 * Input: {"image_id":"08EB37D7-2678-495D-B485-59233EB51996","image_name":"cirrus_1811.img","display_name":"cirrus_1811.img","launch_control_policy":"MeasureOnly","encrypted":false}
	 * 
	 * Output: {"id":"50022e9c-577a-4bbd-9445-197a3e1a349f","trust_policy":"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n <Director>\n <CustomerId>testId</CustomerId>\n </Director>\n <Image>\n <ImageId>08EB37D7-2678-495D-B485-59233EB51996</ImageId>\n <ImageHash>6413fccb72e36d2cd4b20efb5b5fe1be916ab60f0fe1d7e2aab1a2170be1ff40</ImageHash>\n </Image>\n <LaunchControlPolicy>MeasureOnly</LaunchControlPolicy>\n <Whitelist DigestAlg=\"sha256\">\n <File Path=\"/boot/grub/stage1\"></File>\n <File Path=\"/boot/grub/menu.lst\"></File>\n <File Path=\"/initrd.img\"></File>\n <File Path=\"/boot/vmlinuz-3.2.0-37-virtual\"></File>\n <File Path=\"/boot/config-3.2.0-37-virtual\"></File>\n <File Path=\"/boot/initrd.img-3.2.0-37-virtual\"></File>\n <File Path=\"/boot/grub/e2fs_stage1_5\"></File>\n <File Path=\"/boot/grub/stage2\"></File>\n </Whitelist>\n</TrustPolicy>\n"}
	 * </pre>
	 * 
	 * 
	 * @param createTrustPolicyMetaDataRequest
	 * @return CreateTrustPolicyMetaDataResponse
	 */
	@Path("trust-policy-drafts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public CreateTrustPolicyMetaDataResponse createTrustPolicyDraft(
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest) {

		CreateTrustPolicyMetaDataResponse createTrustPolicyMetadataResponse = new CreateTrustPolicyMetaDataResponse();
		if (StringUtils.isBlank(createTrustPolicyMetaDataRequest
				.getDisplay_name())) {
			createTrustPolicyMetadataResponse.setError("Display Name is mandatory");
		} else if (StringUtils.isBlank(createTrustPolicyMetaDataRequest.getLaunch_control_policy())) {
			createTrustPolicyMetadataResponse.setError("Launch_control_policy is mandatory");
		} else if (StringUtils.isBlank(createTrustPolicyMetaDataRequest.getImage_id())) {
			createTrustPolicyMetadataResponse.setError("image_id is mandatory");
		} else {
			try {
				createTrustPolicyMetadataResponse = imageService
						.saveTrustPolicyMetaData(createTrustPolicyMetaDataRequest);
			} catch (DirectorException e) {
				log.error("createTrustPolicyMetaData failed", e);
				// createTrustPolicyMetadataResponse.setStatus(Constants.ERROR);
				// createTrustPolicyMetadataResponse.setDetails(e.getMessage());
				createTrustPolicyMetadataResponse.setError("Unable to create policy draft");
				return createTrustPolicyMetadataResponse;
			}
		}

		return createTrustPolicyMetadataResponse;
	}

	/**
	 * After the user has finalized the list of files and dirs and created a
	 * policy, if he chooses to revisit the files/dirs selection we need to
	 * recreate the policy draft. This method creates draft from existing policy
	 * . Unlike POST trust-policy-drafts method it does not accepts launch
	 * control policy , encrytion details.
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall <pre>
	 * https://{IP/HOST_NAME}/v1/rpc/create-draft-from-policy
	 * Input: {"image_id":"08EB37D7-2678-495D-B485-59233EB51996"}
	 * 
	 * Output: {"id":"<UUID of Policy draft>", "trust_policy_draft":"<XML representation of policy>", "display_name":"<name provided by user for the policy>", "image_attributes":"{"id":"<UUID of image>", "image_format":"qcow2", ..... }"}
	 * 
	 * </pre>
	 * 
	 * @param imageId
	 *            id of the image for which policy draft is being created
	 * @return TrustPolicyDraft - the created policy draft
	 * @throws DirectorException
	 */
	@Path("rpc/create-draft-from-policy")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public TrustPolicyDraft createPolicyDraftFromPolicy(GenericRequest req)
			 {
		TrustPolicyDraft trustPolicyDraft= new TrustPolicyDraft();
		try {
			
			trustPolicyDraft= imageService.createPolicyDraftFromPolicy(req.getImage_id());
		} catch (DirectorException e) {
			log.error("createPolicyDraftFromPolicy failed ",e);
			trustPolicyDraft.setError(e.getMessage());
			
		}
		return trustPolicyDraft;
	}

	/**
	 * Delete the trust policy draft by the provided ID
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType DELETE
	 * @mtwSampleRestCall <pre>
	 * https://{IP/HOST_NAME}/v1/trust-policy-drafts/08EB37D7-2678-495D-B485-59233EB51996
	 * Input: UUID of the policy draft to be deleted
	 * Output: In case of successful deletion:
	 * {"deleted":true}
	 * 
	 * In case of error:
	 * {"deleted":false; "error":"Error in deleting policy draft"}
	 * </pre>
	 * @param trustPolicyDraftId
	 * @return GenericResponse
	 */
	@Path("trust-policy-drafts/{trustPolicyDraftId: [0-9a-zA-Z_-]+}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public GenericResponse deletePolicyDraft(
			@PathParam("trustPolicyDraftId") String trustPolicyDraftId) {
		GenericResponse response = new GenericResponse();
		try {
			imageService.deleteTrustPolicyDraft(trustPolicyDraftId);
			response.setDeleted(true);
		} catch (DirectorException e) {
			log.error("Error in deletePolicyDraft", e);
			response.setDeleted(false);
			response.setError("Error in deletePolicyDraft");
		}
		return response;

	}
}
