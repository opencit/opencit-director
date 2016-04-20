package com.intel.director.service.impl;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.SshSettingInfo;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.TrustPolicyResponse;
import com.intel.director.api.UpdateTrustPolicyRequest;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.TrustPolicyDraftFilter;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.common.MountImage;
import com.intel.director.service.TrustPolicyService;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;
import com.intel.mtwilson.director.trust.policy.CreateTrustPolicy;
import com.intel.mtwilson.services.mtwilson.vm.attestation.client.jaxrs2.TrustPolicySignature;
import com.intel.mtwilson.shiro.ShiroUtil;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.trustpolicy.xml.Measurement;
import com.jcraft.jsch.JSchException;

public class TrustPolicyServiceImpl implements TrustPolicyService {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustPolicyServiceImpl.class);

	IPersistService persistService = new DbServiceImpl();

	private ImageInfo imageInfo;

	private TrustPolicy trustPolicy = null;
	private TrustPolicyDraft trustPolicyDraft = null;
	private String trustPolicyFile;
	private String manifestFile;
	private FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();
	private File dirForPolicyAndManifest;
	
	public TrustPolicyServiceImpl(String imageId) throws DirectorException {
		super();
		try {
			imageInfo = persistService.fetchImageById(imageId);
		} catch (DbException e) {
			log.error("Error fetching image ", e);
			throw new DirectorException("No Image found  ", e);
		}
		try {
			if (StringUtils.isNotBlank(imageInfo.getTrust_policy_id())) {
				trustPolicy = persistService.fetchPolicyById(imageInfo
						.getTrust_policy_id());
			}
		} catch (DbException e) {
			log.error("Error fetching policy for image ", e);
		}
		try {
			if (StringUtils.isNotBlank(imageInfo.getTrust_policy_draft_id())) {
				trustPolicyDraft = persistService
						.fetchPolicyDraftById(imageInfo
								.getTrust_policy_draft_id());
			}
		} catch (DbException e) {
			log.error("Error fetching policy for image ", e);
		}
	}

	@Override
	public void createTrustPolicy(String polictyXml) {
		throw new UnsupportedOperationException("Not yet implemented");

	}

	@SuppressWarnings("deprecation")
	@Override
	public String signTrustPolicy(String policyXml) throws DirectorException {
		String signedPolicyXml;
		Extensions.register(TlsPolicyCreator.class,
				com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);
		log.info("Register TlsPolicyCreator");

		Properties mtwConfig = DirectorUtil.getPropertiesFile(Constants.MTWILSON_PROP_FILE);// My.configuration().getClientProperties();
		log.info("Get MTW prop file");

		TrustPolicySignature client;
		try {
			client = new TrustPolicySignature(mtwConfig);
			log.info("MTW client init");

		} catch (Exception e) {
			log.error("Unable to create client for signing the policy with attestation service", e);
			throw new DirectorException("Unable to create client for signing  policy with attestation service", e);
		}
		try {
			signedPolicyXml = client.signTrustPolicy(policyXml);
		} catch (Exception e) {
			log.error("Unable to sign the policy with attestation service", e);
			throw new DirectorException("Unable to sign the policy with attestation service", e);
		}
		log.info("****** SIGN : " + signedPolicyXml);
		return signedPolicyXml;
	}

	
	//TODO: Refactor
	//Called from normal UI flow
	@Override
	public TrustPolicy archiveAndSaveTrustPolicy(String policyXml,String userName) throws DirectorException {
		TrustPolicy newTrustPolicy = new TrustPolicy();
		newTrustPolicy.setTrust_policy(policyXml);
		ImageAttributes imgAttrs = new ImageAttributes();
		imgAttrs.setId(imageInfo.id);
		newTrustPolicy.setImgAttributes(imgAttrs);
		
		if (StringUtils.isNotBlank(imageInfo.getTrust_policy_draft_id())) {
			newTrustPolicy.setDisplay_name(trustPolicyDraft.getDisplay_name());
		}else if (StringUtils.isNotBlank(imageInfo.getTrust_policy_id())) {
			newTrustPolicy.setDisplay_name(trustPolicy.getDisplay_name());
			newTrustPolicy.setName(trustPolicy.getName());
		}  else {
			throw new DirectorException("No policy or draft for the image");
		}

		newTrustPolicy.setCreated_date(Calendar.getInstance());
		newTrustPolicy.setEdited_date(Calendar.getInstance());
		newTrustPolicy.setEdited_by_user_id(userName);
		newTrustPolicy.setCreated_by_user_id(userName);


		try {
			newTrustPolicy = persistService.savePolicy(newTrustPolicy);
			if (newTrustPolicy.getId() != null && trustPolicy != null) {
				trustPolicy.setArchive(true);
				try {
					persistService.updatePolicy(trustPolicy);
				} catch (DbException e1) {
					log.error("Unable to updatePolicy", e1);
					throw new DirectorException("Unable to updatePolicy", e1);
				}
			}
			
		} catch (DbException e) {
			log.error("Unable to save policy after signing", e);
			throw new DirectorException("Unable to save policy after signing", e);
		}
		log.info("trust policy succesfylly created , createdPolicyId::" + newTrustPolicy.getId());

		if (trustPolicyDraft != null) {
			try {
				persistService.destroyPolicyDraft(trustPolicyDraft);
			} catch (DbException e) {
				log.error("Unable to delete policy draft after creating policy", e);
			}
		}
		return newTrustPolicy;

	}
	
	
	//only called from async task - RecreatePolicy
	
	public TrustPolicy archiveAndSaveTrustPolicy(String policyXml) throws DirectorException {
		return archiveAndSaveTrustPolicy(policyXml, ShiroUtil.subjectUsername());
	}

	@Override
	public void copyTrustPolicyAndManifestToHost(String policyXml) throws DirectorException {
		if (!Constants.DEPLOYMENT_TYPE_BAREMETAL.equals(imageInfo.getImage_deployments())) {
			return;
		}


		// Push the policy and manifest to the remote host
		SshSettingInfo existingSsh;
		try {
			existingSsh = persistService.fetchSshByImageId(imageInfo.id);
		} catch (DbException e) {
			log.error("Unable to fetch SSH details for host", e);
			throw new DirectorException("Unable to fetch SSH details for host", e);
		}
		String user = existingSsh.getUsername();
		String password = existingSsh.getPassword().getKey();
		String ip = existingSsh.getIpAddress();

		log.info("Connecting to remote host : " + ip + " with user " + user);

		if (StringUtils.isBlank(imageInfo.getPartition())) {
			writePolicyAndManifest(policyXml);
			SSHManager sshManager = new SSHManager(user, password, ip);
			try {
				List<String> files = new ArrayList<String>(2);
				files.add(manifestFile);
				files.add(trustPolicyFile);
				sshManager.sendFileToRemoteHost(files, "/boot/trust");
				log.info("Completed transfer of manifest and trust policy");
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				log.error(
						"Unable to send trustPolicy /manifest  file to remote host ",
						e);
				throw new DirectorException(
						"Unable to send trustPolicy /manifest  file to remote host",
						e);
			} finally {
				if (dirForPolicyAndManifest.exists()) {
					fileUtilityOperation
							.deleteFileOrDirectory(dirForPolicyAndManifest);
				}
				log.info("Trust policy and manifest written to tmp cleaned up");
			}
		} else {
			policyXml = convertPolicyInWindowsFormat(policyXml);
			writePolicyAndManifest(policyXml);
			String[] partition = imageInfo.getPartition().split(",");
			String mountpath = TdaasUtil.getMountPath(imageInfo.getId()) + File.separator + "C:";
			MountImage.unmountRemoteSystem(mountpath);
			MountImage.mountWindowsRemoteSystem(ip, user, password, mountpath,
					partition[0], new String("0666"), new String("0666"));
			
			log.info("Pushing Policy To Windows Host");
			//String trustPolicyPath = mountpath + File.separator + "boot"+ File.separator +"trust";
			String trustPolicyPath = mountpath ;
			
			File trustPolicyDir = new File(trustPolicyPath);
			//trustPolicyDir.mkdirs();
			
			try {
				FileUtils.copyFile(new File(trustPolicyFile), new File(
						trustPolicyPath + File.separator + "trustpolicy.xml"));
				FileUtils.copyFile(new File(manifestFile), new File(
						trustPolicyPath + File.separator + "manifest.xml"));
				log.info("Policy Pushed Successfully");

			} catch (IOException e) {
				log.error("Error writing policy and manifest ", e);
			} finally {
				if (dirForPolicyAndManifest.exists()) {
					fileUtilityOperation
							.deleteFileOrDirectory(dirForPolicyAndManifest);
				}
				log.info("Trust policy and manifest written to tmp cleaned up");
			}
		}
		


	}

	@Override
	public void addEncryption(com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy) throws DirectorException {
		if (policy == null) {
			return;
		}
		if (policy.getEncryption() == null) {
			return;
		}
		if (!policy.getEncryption().getChecksum().getValue().equals("1")) {
			return;
		}

		if(!policy.getEncryption().getKey().getURL().equals("uri")){
			//Means a key was previously generated
			if (!policy.getEncryption().getKey().getValue().contains("keys")) {
				throw new DirectorException("Invalid key for encryption");
			}
		}

		String filePath = imageInfo.getLocation() + imageInfo.getImage_name();
		File imgFile = new File(filePath);
		log.info("Calculating MD5 of file : {}", filePath);
		String computeHash;
		try {
			computeHash = DirectorUtil.computeHash(MessageDigest.getInstance("MD5"), imgFile);
		} catch (NoSuchAlgorithmException | IOException e) {
			log.error("Unable to compute hash for image while creating policy", e);
			throw new DirectorException("Unable to compute hash for image while creating policy", e);
		}
		policy.getEncryption().getChecksum().setValue(computeHash);
	}

	@Override
	public void calculateHashes(com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy) throws DirectorException {
		if (trustPolicy != null) {
			policy.setSignature(null);
			List<Measurement> measurements = policy.getWhitelist().getMeasurements();
			for (Measurement measurement : measurements) {
				measurement.setValue(null);
			}
		}
		try {
			new CreateTrustPolicy(imageInfo.id).createTrustPolicy(policy);
		} catch (Exception e1) {
			log.error("Unable to create trust policy- create hashes");
			throw new DirectorException("Unable to create policy - create hashes", e1);
		}
	}

	@Override
	public TrustPolicyDraft editTrustPolicyDraft(TrustPolicyDraftEditRequest trustpolicyDraftEditRequest)
			throws DirectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteTrustPolicy(String trust_policy_id) throws DirectorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTrustPolicyForImage(String imageId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CreateTrustPolicyMetaDataResponse saveTrustPolicyMetaData(
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest) throws DirectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CreateTrustPolicyMetaDataResponse getPolicyMetadata(String draftid) throws DirectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CreateTrustPolicyMetaDataResponse getPolicyMetadataForImage(String image_id) throws DirectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrustPolicyDraft createPolicyDraftFromPolicy(String imageId) throws DirectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrustPolicy getTrustPolicyByTrustId(String trustId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrustPolicy getTrustPolicyByImageId(String imageId) throws DirectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteTrustPolicyDraft(String trust_policy_draft_id) throws DirectorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TrustPolicyResponse getTrustPolicyMetaData(String trust_policy_id) throws DirectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateTrustPolicy(UpdateTrustPolicyRequest updateTrustPolicyRequest, String trust_policy_id)
			throws DirectorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<TrustPolicyDraft> getTrustPolicyDrafts(TrustPolicyDraftFilter trustPolicyDraftFilter)
			throws DirectorException {
		// TODO Auto-generated method stub
		return null;
	}

	public void writePolicyAndManifest(String policyXml) throws DirectorException{
		// Writing inside bare metal modified image

		String localPathForPolicyAndManifest = "/tmp/" + imageInfo.id;
		String trustPolicyName = "trustpolicy.xml";
		trustPolicyFile = localPathForPolicyAndManifest + File.separator + trustPolicyName;

		manifestFile = localPathForPolicyAndManifest + File.separator + "manifest.xml";
		String manifest = null;
		try {
			String unformattedManifest = TdaasUtil.getManifestForPolicy(policyXml);
			manifest = DirectorUtil.prettifyXml(unformattedManifest);
			if(manifest == null){
				throw new DirectorException("Unable to format the manifest xml");
			}
		} catch (JAXBException e) {
			log.error("Unable to convert policy into manifest", e);
			throw new DirectorException("Unable to convert policy into manifest", e);
		}
		dirForPolicyAndManifest = new File(localPathForPolicyAndManifest);
		if (!dirForPolicyAndManifest.exists()) {
			dirForPolicyAndManifest.mkdir();
		}

		
		fileUtilityOperation.createNewFile(manifestFile);
		fileUtilityOperation.createNewFile(trustPolicyFile);

		fileUtilityOperation.writeToFile(trustPolicyFile, policyXml);
		fileUtilityOperation.writeToFile(manifestFile, manifest);
	}

	@Override
	public String convertPolicyInWindowsFormat(String policyXml) {
		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policyObj = null;
		try {
			policyObj = TdaasUtil.getPolicy(policyXml);
		} catch (JAXBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (Measurement measurement : policyObj.getWhitelist()
				.getMeasurements()) {
			measurement.setPath(measurement.getPath().substring(1));
		}

		try {
			policyXml = TdaasUtil.convertTrustPolicyToString(policyObj);
		} catch (JAXBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return policyXml;
	}
}
