package com.intel.director.images;

import java.util.ArrayList;
import java.util.List;

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

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.CreateTrustPolicyResponse;
import com.intel.director.api.GenericDeleteResponse;
import com.intel.director.api.GenericRequest;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.ListTrustPolicyDrafts;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.TrustPolicyDraftResponse;
import com.intel.director.common.Constants;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ImageService;
import com.intel.director.service.TrustPolicyService;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.director.service.impl.TrustPolicyServiceImpl;
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
	 * @mtwSampleRestCall
	 * 
	 * 					<pre>
	 * https://{IP/HOST_NAME}/v1/trust-policy-drafts
	 * Input: NA
	 * 
	 * Output:
	 * {
		"trust_policy_drafts": [{
			"created_by_user_id": "admin",
			"created_date": "2016-05-08 18:30:00",
			"edited_by_user_id": "admin",
			"edited_date": "2016-05-08 18:30:00",
			"id": "b9d7fb1b-82f9-4ca4-a39b-605cc44dfda9",
			"trust_policy_draft": "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"></TrustPolicy>\n",
			"display_name": "aa1-v1",
			"image_attributes": {
				"created_date": "2016-05-08 18:30:00",
				"edited_by_user_id": "admin",
				"edited_date": "2016-05-08 18:30:00",
				"id": "B462AA9C-84BD-4E72-9E52-38E18C09A720",
				"image_name": "10.35.35.131",
				"image_deployments": "BareMetal",
				"deleted": false,
				"image_upload_status": "Complete"
			}
		},
		{
			"created_by_user_id": "admin",
			"created_date": "2016-05-08 18:30:00",
			"edited_by_user_id": "admin",
			"edited_date": "2016-05-13 12:42:38",
			"id": "dc22495d-380a-434c-8f30-051d77eac631",
			"trust_policy_draft": "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"></TrustPolicy>\n",
			"display_name": "v1-v1",
			"image_attributes": {
				"created_date": "2016-05-08 18:30:00",
				"edited_by_user_id": "admin",
				"edited_date": "2016-05-13 12:42:45",
				"id": "92986CA0-20F3-46C6-B226-E9744FEC8734",
				"image_name": "10.35.35.182",
				"image_deployments": "BareMetal",
				"deleted": false,
				"image_upload_status": "Complete"
			}
		},
		{
			"created_by_user_id": "admin",
			"created_date": "2016-05-08 18:30:00",
			"edited_by_user_id": "admin",
			"edited_date": "2016-05-08 18:30:00",
			"id": "48419d0b-9c5e-46dc-bc70-9b5b410ce01e",
			"trust_policy_draft": "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"></TrustPolicy>\n",
			"display_name": "222",
			"image_attributes": {
				"created_by_user_id": "admin",
				"created_date": "2016-05-08 18:30:00",
				"edited_by_user_id": "admin",
				"edited_date": "2016-05-13 13:19:31",
				"id": "F6B0C7D1-A12C-4A0F-84C2-9F0EBA888334",
				"image_name": "111",
				"image_format": "qcow2",
				"image_deployments": "VM",
				"image_size": 13631488,
				"sent": 13631488,
				"deleted": false,
				"image_upload_status": "Complete",
				"image_Location": "/mnt/images/"
			}
		},
		{
			"created_by_user_id": "admin",
			"created_date": "2016-05-08 18:30:00",
			"edited_by_user_id": "admin",
			"edited_date": "2016-05-08 18:30:00",
			"id": "f4370aab-21ad-4097-904d-95c74159899f",
			"trust_policy_draft": "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"></TrustPolicy>",
			"display_name": "333-v4",
			"image_attributes": {
				"created_by_user_id": "admin",
				"created_date": "2016-05-08 18:30:00",
				"edited_by_user_id": "admin",
				"edited_date": "2016-05-08 18:30:00",
				"id": "0F61E33B-3BFF-4E49-9B5E-B6931135CC8F",
				"image_name": "333",
				"image_format": "qcow2",
				"image_deployments": "VM",
				"image_size": 13631488,
				"sent": 13631488,
				"deleted": true,
				"image_upload_status": "Complete",
				"image_Location": "/mnt/images/"
			}
		}]
		}
	 * 
	 * 
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

		CreateTrustPolicyMetaDataResponse createTrustPolicyMetaDataResponse = new CreateTrustPolicyMetaDataResponse();
		if(!ValidationUtil.isValidWithRegex(trustPolicyDraftId,RegexPatterns.UUID)){
			createTrustPolicyMetaDataResponse.error = "Trust Policy Draft Id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(createTrustPolicyMetaDataResponse).build();
		}
		

		
		TrustPolicyDraft fetchTrustpolicydraftById = imageService
				.fetchTrustpolicydraftById(trustPolicyDraftId);
		if (fetchTrustpolicydraftById == null) {
			GenericResponse genericResponse = new GenericResponse();
			genericResponse.error = "Trust policy draft with id "+ trustPolicyDraftId+" does not exist";
			return Response.status(Response.Status.NOT_FOUND)
					.entity(genericResponse).build();
		}

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
	 * Input: UUID of the trust policy draft in path
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
	public Response editPolicyDraft(
			@PathParam("trustPolicyDraftId") String trustPolicyDraftId,
			TrustPolicyDraftEditRequest trustPolicyDraftEditRequest) {
		trustPolicyDraftEditRequest.trust_policy_draft_id = trustPolicyDraftId;
		TrustPolicyDraftResponse policyDraft = new TrustPolicyDraftResponse();
		/////String trustPolicyDraftXML = null;
		
		String error = trustPolicyDraftEditRequest.validate();

		if (!StringUtils.isBlank(error)) {
			GenericResponse genericResponse = new GenericResponse();
			genericResponse.error = error;
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}
		
		List<String> errors = new ArrayList<>();
		TrustPolicyDraft fetchTrustpolicydraftById = imageService
				.fetchTrustpolicydraftById(trustPolicyDraftId);
		if (fetchTrustpolicydraftById == null) {
			errors.add("Invalid trust policy draft id provided");
		}

		if (errors.size() > 0) {
			GenericResponse genericResponse = new GenericResponse();
			genericResponse.error = StringUtils.join(errors, ", ");
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}
		

		
		try {
			policyDraft = imageService
					.editTrustPolicyDraft(trustPolicyDraftEditRequest);
			policyDraft.setImgAttributes(null);
			log.debug("Updated policy draft trustPolicyXML : ");
		} catch (Exception e) {
			// /response.setStatus(Constants.ERROR);
			policyDraft.setError(e.getMessage());
			log.error("Error while updating policy draft : "
					+ trustPolicyDraftId);
		}

		return 
				Response.ok(policyDraft).build();
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
	 * {"trust_policy_draft_id":"<UUID of trust policy draft>", "image_id" : "<UUID of the image>"}
	 * 
	 * {"trust_policy_draft_id":"a173302e-39f1-4083-8f6e-a7f0b9d45b20","image_id":"9C64F16D-782C-4F24-8880-9A9F1EE6794D"}
	 * In case of a success, the response would be :
	 * {"id":"1526B5F9-1414-4DA2-91A9-4858C51352ED"}
	 * id returned in case of success response is id of trust policy created 
	 * by this call by signing draft and generating hashes 
	 * 
	 * In case of error where signing with MTW fails: 
	 * {"error":"Unable to sign the policy with attestation service"}
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
	public Response createTrustPolicy(
			CreateTrustPolicyMetaDataRequest createPolicyRequest) {
		CreateTrustPolicyResponse createTrustPolicyDraftResponse = new CreateTrustPolicyResponse();
		try {
		String imageId=imageService.fetchImageIdByDraftOrPolicy(createPolicyRequest.getTrust_policy_draft_id());
		/*	ImageInfo imageInfo = imageService.fetchImageById(createPolicyRequest.image_id);
			String imageId = imageInfo.id;*/
			String trustPolicyId = imageService
					.createTrustPolicy(createPolicyRequest.trust_policy_draft_id);
			createTrustPolicyDraftResponse.setId(trustPolicyId);
			imageService.deletePasswordForHost(imageId);
			// / response.setStatus(Constants.SUCCESS);
		} catch (DirectorException de) {
			// //response.setStatus(Constants.ERROR);
			createTrustPolicyDraftResponse.setError(de.getMessage());
			log.error("Error creating policy from draft for image : "
					+ createPolicyRequest.image_id, de);
		}
		return Response.ok(createTrustPolicyDraftResponse).build();
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
	 * Input: {"image_id":"9C64F16D-782C-4F24-8880-9A9F1EE6794D","image_name":"c2","display_name":"c2","launch_control_policy":"MeasureOnly","encrypted":false}
	 * 
	 * Output: {"encrypted":false,"id":"a173302e-39f1-4083-8f6e-a7f0b9d45b20","trust_policy":"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<TrustPolicy xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"mtwilson:trustdirector:policy:1.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n    <Director>\n        <CustomerId>admin</CustomerId>\n    </Director>\n    <Image>\n        <ImageId>9C64F16D-782C-4F24-8880-9A9F1EE6794D</ImageId>\n    </Image>\n    <LaunchControlPolicy>MeasureOnly</LaunchControlPolicy>\n    <Whitelist DigestAlg=\"sha256\"/>\n</TrustPolicy>\n","status":"success"}
	 * </pre>
	 * 
	 * In case of wrong input it will show 400 Bad request
	 * with output:-
	 * 
	 * {"error": "Invalid image id provided"}
	 * 
	 * @param createTrustPolicyMetaDataRequest
	 * @return CreateTrustPolicyMetaDataResponse
	 */
	@Path("trust-policy-drafts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response createTrustPolicyDraft(CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest) {

		CreateTrustPolicyMetaDataResponse createTrustPolicyMetadataResponse = new CreateTrustPolicyMetaDataResponse();
		GenericResponse genericResponse= new GenericResponse();
		ImageInfo fetchImageById;
		try {

			fetchImageById = imageService.fetchImageById(createTrustPolicyMetaDataRequest.image_id);
			if (fetchImageById == null) {
				genericResponse.setError("Invalid image id provided");
				///createTrustPolicyMetadataResponse.setId(createTrustPolicyMetaDataRequest.image_id);
				return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
			}

		} catch (DirectorException e1) {
			log.error("Invalid image id", e1);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		createTrustPolicyMetaDataRequest.deployment_type = fetchImageById.getImage_deployments();

		String error = createTrustPolicyMetaDataRequest.validate("draft");
		if (!StringUtils.isBlank(error)) {
			genericResponse.setError(error);
			return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
		}

		if (fetchImageById.getImage_deployments().equals(Constants.DEPLOYMENT_TYPE_DOCKER)) {
			String display_name = createTrustPolicyMetaDataRequest.display_name;
			if (!createTrustPolicyMetaDataRequest.display_name.startsWith(fetchImageById.getRepository() + ":")) {
				genericResponse.setError("Invalid Repo Name");
				return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
			}
			String[] split = display_name.split(fetchImageById.getRepository() + ":");
			if (split.length == 0 || StringUtils.isBlank(split[1])) {
				genericResponse.setError("Tag cannot be empty");
				return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
			}
		}

		try {
			createTrustPolicyMetadataResponse = imageService.saveTrustPolicyMetaData(createTrustPolicyMetaDataRequest);
		} catch (DirectorException e) {
			log.error("createTrustPolicyMetaData failed", e);
			genericResponse.setError(e.getMessage());
			return Response.ok(genericResponse).build();
		}

		return Response.ok(createTrustPolicyMetadataResponse).build();
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
	 * @mtwSampleRestCall
	 * 
	 * <pre>
	 * https://{IP/HOST_NAME}/v1/rpc/create-draft-from-policy
	 * Input: {"image_id":"3BCDCF57-0065-4859-93FB-12F99C89B99C"}
	 * 
	 * Output: {"created_by_user_id":"admin","created_date":"2016-05-04 09:18:10","edited_by_user_id":"admin","edited_date":"2016-05-04 09:18:58",
	 * "id":"6ab4bc2d-c7cd-4e5e-9b68-e5b237f0fe1e","trust_policy_draft":"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>
	 * <TrustPolicy ....>...</TrustPolicy>","display_name":"t2-PME-TAR-v2","image_attributes":{"created_by_user_id":"admin","created_date":"2016-05-03 10:05:19",
	 * "edited_by_user_id":"admin","edited_date":"2016-05-04 09:18:10","id":"3BCDCF57-0065-4859-93FB-12F99C89B99C","image_name":"t2",
	 * "image_format":"qcow2","image_deployments":"VM","sent":13287936,"deleted":false,"upload_variable_md5":"9ad8c4f85a8f0cb8ecd65352fc204a73",
	 * "image_upload_status":"Complete","image_Location":"/mnt/images/"}}
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
	public Response createPolicyDraftFromPolicy(GenericRequest req) {
		TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
		if (!ValidationUtil.isValidWithRegex(req.getImage_id(), RegexPatterns.UUID)) {
			trustPolicyDraft.setError("Image id is empty or not in uuid format");
			return Response.status(Response.Status.BAD_REQUEST).entity(trustPolicyDraft).build();
		}

		ImageInfo imageInfo = null;
		try {
			imageInfo = imageService.fetchImageById(req.getImage_id());
		} catch (DirectorException e1) {
			log.error("Unable to fetch image", e1);
		}
		if (imageInfo == null) {
			trustPolicyDraft.setError("No image with id : " + req.getImage_id() + " exists.");
			return Response.status(Response.Status.BAD_REQUEST).entity(trustPolicyDraft).build();
		}

		String trust_policy_id = imageInfo.getTrust_policy_id();
		TrustPolicy trustPolicyByTrustId = imageService.getTrustPolicyByTrustId(trust_policy_id);

		if (trustPolicyByTrustId == null) {
			trustPolicyDraft.setError("No trust policy exists for image with id : " + req.getImage_id() + " exists.");
			return Response.status(Response.Status.BAD_REQUEST).entity(trustPolicyDraft).build();
		}

		try {

			trustPolicyDraft = imageService.createPolicyDraftFromPolicy(req.getImage_id());
		} catch (DirectorException e) {
			log.error("createPolicyDraftFromPolicy failed ", e);
			trustPolicyDraft.setError(e.getMessage());

		}
		return Response.ok(trustPolicyDraft).build();
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
	 * In case of wrong input it shows 400 Bad request:
	 * Output: { "error": "Trust Policy Draft Id does not exist", "deleted": false}
	 * </pre>
	 * @param trustPolicyDraftId
	 * @return GenericResponse
	 */
	@Path("trust-policy-drafts/{trustPolicyDraftId: [0-9a-zA-Z_-]+}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deletePolicyDraft(
			@PathParam("trustPolicyDraftId") String trustPolicyDraftId) {
		GenericDeleteResponse genericResponse = new GenericDeleteResponse();
		if(!ValidationUtil.isValidWithRegex(trustPolicyDraftId,RegexPatterns.UUID)){
			genericResponse.error = "Trust Policy Draft Id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}
		
		TrustPolicyDraft fetchTrustpolicydraftById = imageService.fetchTrustpolicydraftById(trustPolicyDraftId);
		if(fetchTrustpolicydraftById == null){
			genericResponse.error = "Trust Policy Draft Id does not exist";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();

		}
		
		try {
			imageService.deleteTrustPolicyDraft(trustPolicyDraftId);
			genericResponse.setDeleted(true);
		} catch (DirectorException e) {
			log.error("Error in deletePolicyDraft", e);
			genericResponse.setDeleted(false);
			genericResponse.setError("Error in deletePolicyDraft");
		}
		return Response.ok(genericResponse).build();
	}
	
	/**
	 * 
	 * This method looks into the MW_TRUST_POLICY_DRAFTS table and gets the policy draft
	 * string and sends it as an xml content to the user.
	 * 
	 * In case the policy draft is not found for the trust policy draft id, HTTP 404 is returned
	 * 
	 * @mtwContentTypeReturned XML
	 * @mtwMethodType GET
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 *  https://{IP/HOST_NAME}/v1/trust-policy-drafts/08EB37D7-2678-495D-B485-59233EB51996/download
	 * Input: Trust policy draft id as path param
	 * Output: Content sent as stream
	 * 
	 * In case of improper input it will show 400 bad request with 
	 * <error>Trust Policy id is empty or not in uuid format</error>
	 *                    </pre>
	 * 
	 *                    *
	 * @param trustPolicyDraftId
	 *            the trust policy draft for which the draft is downloaded
	 * @return XML content of the policy
	 * @throws DirectorException
	 */
	@Path("trust-policy-drafts/{trustPolicyDraftId: [0-9a-zA-Z_-]+}/download")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response downloadPolicyForTrustPolicyDraftId(
			@PathParam("trustPolicyDraftId") String trustPolicyDraftId) {
		GenericResponse genericResponse= new GenericResponse();
		if(!ValidationUtil.isValidWithRegex(trustPolicyDraftId,RegexPatterns.UUID)){
			genericResponse.error = "Trust Policy Draft id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(genericResponse).build();
		}
		
		TrustPolicyDraft trustPolicyDraft = imageService.fetchTrustpolicydraftById(trustPolicyDraftId);
		if(trustPolicyDraft == null){
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		ResponseBuilder response = Response.ok(trustPolicyDraft.getTrust_policy_draft());
		response.header("Content-Disposition", "attachment; filename=policy_"
				+ trustPolicyDraft.getImgAttributes().getImage_name() + ".xml");
		return response.build();
	}
	
	
	/**
	 * 
	 * This method is called to check the name of the trust policy name. If the
	 * docker image has been uploaded to the store before the policy is
	 * uploaded, the versioned name is returned.
	 * 
	 * 
	 * @mtwContentTypeReturned JSON
	 * @mtwMethodType POST
	 * @mtwSampleRestCall
	 * 
	 *                    <pre>
	 *                    https://{IP/HOST_NAME}/v1/rpc/fetch-versioned-display-
	 *                    name Input:
	 *                    {"image_id":"CC60C7E3-7442-4D1E-BDA7-5A5A59186EB8"}
	 *                    Output: { "details": "busybox:latest-v1" }
	 * 
	 * @return "details" returns the result of the call
	 * @throws DirectorException
	 */
	@Path("rpc/fetch-versioned-display-name")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVersionedDisplayNameForDockerImage(GenericRequest req) {
		GenericResponse genericResponse = new GenericResponse();

		if (!ValidationUtil.isValidWithRegex(req.getImage_id(), RegexPatterns.UUID)) {
			genericResponse.error = "Image id is empty or not in uuid format";
			return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
		}

		ImageInfo imageInfo = null;

		try {
			imageInfo = imageService.fetchImageById(req.getImage_id());
		} catch (DirectorException e1) {
			log.error("Unable to fetch image", e1);
		}
		if (imageInfo == null) {
			genericResponse.error = "No image with id : " + req.getImage_id() + " exists.";
			return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();
		}
		if (!Constants.DEPLOYMENT_TYPE_DOCKER.equals(imageInfo.getImage_deployments())) {
			genericResponse.error = "Only supported for Docker images";
			return Response.status(Response.Status.NO_CONTENT).entity(genericResponse).build();
		}
		String versionedDisplayNameForDockerImage;
		TrustPolicyService trustPolicyService;
		try {
			trustPolicyService = new TrustPolicyServiceImpl(req.getImage_id());
			versionedDisplayNameForDockerImage = trustPolicyService
					.getVersionedDisplayNameForDockerImage(imageInfo);
		} catch (DirectorException e) {
			log.error("Unable to get versioned name for policy", e);
			genericResponse.error = "Unable to get versioned name for docker image policy ";
			return Response.status(Response.Status.BAD_REQUEST).entity(genericResponse).build();

		}
		genericResponse.setDetails(versionedDisplayNameForDockerImage);

		return Response.ok(genericResponse).build();
	}
}
