/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.schmizz.sshj.SSHClient;

import org.apache.commons.codec.binary.Hex;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import com.github.dnault.xmlpatch.Patcher;
import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.ImageUploadRequest;
import com.intel.director.api.ImagesReadyToDeployResponse;
import com.intel.director.api.MountHostResponse;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.PolicyToHostResponse;
import com.intel.director.api.PolicyToMountedImageResponse;
import com.intel.director.api.SshKey;
import com.intel.director.api.SshPassword;
import com.intel.director.api.SshSettingInfo;
import com.intel.director.api.SshSettingRequest;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftRequest;
import com.intel.director.api.UnmountHostResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.manifest.xml.DirectoryMeasurementType;
import com.intel.mtwilson.manifest.xml.FileMeasurementType;
import com.intel.mtwilson.manifest.xml.Manifest;
import com.intel.mtwilson.manifest.xml.MeasurementType;
import com.intel.mtwilson.trustpolicy.xml.Checksum;
import com.intel.mtwilson.trustpolicy.xml.DecryptionKey;
import com.intel.mtwilson.trustpolicy.xml.DigestAlgorithm;
import com.intel.mtwilson.trustpolicy.xml.Director;
import com.intel.mtwilson.trustpolicy.xml.DirectoryMeasurement;
import com.intel.mtwilson.trustpolicy.xml.Encryption;
import com.intel.mtwilson.trustpolicy.xml.FileMeasurement;
import com.intel.mtwilson.trustpolicy.xml.Image;
import com.intel.mtwilson.trustpolicy.xml.LaunchControlPolicy;
import com.intel.mtwilson.trustpolicy.xml.Measurement;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy;
import com.intel.mtwilson.trustpolicy.xml.Whitelist;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
/**
 * 
 * @author GS-0681
 */
public class TdaasUtil {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(TdaasUtil.class);

	public static MountImageResponse mapImageAttributesToMountImageResponse(
			ImageAttributes imageAttributes) {
		Mapper mapper = new DozerBeanMapper();
		MountImageResponse mountImageResponse = mapper.map(imageAttributes,
				MountImageResponse.class);
		return mountImageResponse;
	}

	public static UnmountImageResponse mapImageAttributesToUnMountImageResponse(
			ImageAttributes imageAttributes) {
		Mapper mapper = new DozerBeanMapper();
		UnmountImageResponse unmountImageResponse = mapper.map(imageAttributes,
				UnmountImageResponse.class);
		return unmountImageResponse;
	}

	public static TrustDirectorImageUploadResponse mapImageAttributesToTrustDirectorImageUploadResponse(
			ImageAttributes imageAttributes) {
		Mapper mapper = new DozerBeanMapper();
		TrustDirectorImageUploadResponse directorImageUploadResponse = mapper
				.map(imageAttributes, TrustDirectorImageUploadResponse.class);
		return directorImageUploadResponse;
	}

	public static String getMountPath(String imageId) {
		return DirectorUtil.getMountPath(imageId) + File.separator + "mount";
	}

	public static String patch(String src, String patch) {
		try {
			InputStream inputStream = new ByteArrayInputStream(
					src.getBytes(StandardCharsets.UTF_8));

			InputStream patchStream = new ByteArrayInputStream(
					patch.getBytes(StandardCharsets.UTF_8));
			OutputStream outputStream = new ByteArrayOutputStream();

			Patcher.patch(inputStream, patchStream, outputStream);
			String patched = ((ByteArrayOutputStream) outputStream)
					.toString("UTF-8");
			return patched;
		} catch (IOException e) {
			System.err.println("ERROR: Could not access file: "
					+ e.getMessage());
			System.exit(1);
		}
		return "";
	}

	public static void getParentDirectory(String imageId, String filePath,
			String root, Map<String, Boolean> parentsList, boolean recursive) {
		String mountPath = TdaasUtil.getMountPath(imageId);
		File parent = new File(filePath).getParentFile();
		if (parent == null
				|| parent.getAbsolutePath().equals(getMountPath(imageId))) {
			return;
		}

		if (parent.isDirectory()) {
			String parentPath = parent.getAbsolutePath();
			if (parentPath.equals(root)) {
				return;
			}
			parentsList.put(parentPath.replace(mountPath, ""), recursive);
			getParentDirectory(imageId, parentPath, root, parentsList, true);			
		}

	}

	public static String generateInitialPolicyDraft(
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest)
			throws JAXBException {
		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = new com.intel.mtwilson.trustpolicy.xml.TrustPolicy();
		Director director = new Director();
		director.setCustomerId(DirectorUtil.getDirectorId() == null ? "TESTDID"
				: DirectorUtil.getDirectorId());
		Image image = new Image();
		image.setImageId(createTrustPolicyMetaDataRequest.getImageid());
		Whitelist whitelist = new Whitelist();
		policy.setLaunchControlPolicy(LaunchControlPolicy
				.fromValue(createTrustPolicyMetaDataRequest
						.getLaunch_control_policy()));
		policy.setDirector(director);
		policy.setImage(image);
		if (createTrustPolicyMetaDataRequest.deployment_type
				.equals(Constants.DEPLOYMENT_TYPE_BAREMETAL)) {
			whitelist.setDigestAlg(DigestAlgorithm.SHA_1);
		} else {
			whitelist.setDigestAlg(DigestAlgorithm.SHA_256);
		}
		policy.setWhitelist(whitelist);
		policy = setEncryption(createTrustPolicyMetaDataRequest, policy);

		JAXBContext context = JAXBContext.newInstance(TrustPolicy.class);
		Marshaller m = context.createMarshaller();

		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter w = new StringWriter();
		m.marshal(policy, w);
		return w.toString();

	}

	public static com.intel.mtwilson.trustpolicy.xml.TrustPolicy setEncryption(
			CreateTrustPolicyMetaDataRequest req,
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy) {
		boolean encryptFlag = req.encrypted;
		boolean isPolicyEncrypted = false;
		if (policy.getEncryption() != null) {
			isPolicyEncrypted = true;
		}
		if (encryptFlag && !isPolicyEncrypted) {
			Encryption encryption = new Encryption();
			DecryptionKey key = new DecryptionKey();
			key.setURL("uri");
			key.setValue("uri");
			Checksum checksum = new Checksum();
			checksum.setValue("1");
			encryption.setChecksum(checksum);
			encryption.setKey(key);
			policy.setEncryption(encryption);
		} else if (isPolicyEncrypted && !encryptFlag) {
			policy.setEncryption(null);
		}
		return policy;
	}

	public static String updatePolicyDraft(String existingPolicyDraft,
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(TrustPolicy.class);
		Unmarshaller unmarshaller = (Unmarshaller) jaxbContext
				.createUnmarshaller();

		StringReader reader = new StringReader(existingPolicyDraft);
		TrustPolicy policy = (TrustPolicy) unmarshaller.unmarshal(reader);
		LaunchControlPolicy controlPolicy = LaunchControlPolicy
				.fromValue(createTrustPolicyMetaDataRequest
						.getLaunch_control_policy());
		policy.setLaunchControlPolicy(controlPolicy);
		policy = setEncryption(createTrustPolicyMetaDataRequest, policy);
		Marshaller m = jaxbContext.createMarshaller();

		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter w = new StringWriter();
		m.marshal(policy, w);
		return w.toString();

	}

	public static TrustPolicy getPolicy(String policyxml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(TrustPolicy.class);
		Unmarshaller unmarshaller = (Unmarshaller) jaxbContext
				.createUnmarshaller();

		StringReader reader = new StringReader(policyxml);
		TrustPolicy policy = (TrustPolicy) unmarshaller.unmarshal(reader);
		return policy;

	}
	
	public static Manifest convertStringToManifest(String manifestStr) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Manifest.class);
		Unmarshaller unmarshaller = (Unmarshaller) jaxbContext
				.createUnmarshaller();

		StringReader reader = new StringReader(manifestStr);
		Manifest manifest = (Manifest) unmarshaller.unmarshal(reader);
		return manifest;

	}


	public static String convertTrustPolicyToString(TrustPolicy policy)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(TrustPolicy.class);
		Marshaller marshaller = (Marshaller) jaxbContext.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(policy, writer);
		String xml = writer.toString();
		return xml;
	}

	public static void main(String[] args) {
		String imageId = "123";
		ImageAttributes imageAttributes = new ImageAttributes();
		imageAttributes.id = imageId;
		imageAttributes.image_deployments = "VM";
		imageAttributes.image_format = "qcow2";
		imageAttributes.image_size = 1000;
		imageAttributes.location = "/opt/director/vm/" + imageId;
		imageAttributes.mounted_by_user_id = null;
		imageAttributes.name = "IMG_" + imageId;
		imageAttributes.status = null;

		TrustDirectorImageUploadResponse directorImageUploadResponse = mapImageAttributesToTrustDirectorImageUploadResponse(imageAttributes);

		System.out.println(directorImageUploadResponse.id);

	}

	public static String createImageTrustPolicyTar(String trustPolicyLocation,
			String imageLocation) {
		String imagePathDelimiter = "/";

		String imageTPDir = trustPolicyLocation.substring(0,
				trustPolicyLocation.lastIndexOf(imagePathDelimiter));
		// // TO DO:- Use common code method
		executeShellCommand("cd " + imageTPDir);
		executeShellCommand("pwd ");
		String imageName = imageLocation.substring(imageLocation
				.lastIndexOf(imagePathDelimiter) + 1);
		String trustpolicyName = trustPolicyLocation
				.substring(trustPolicyLocation.lastIndexOf(imagePathDelimiter) + 1);

		String tarName = imageName + "-tar" + ".tar";
		executeShellCommand("tar -cf " + imageTPDir + imagePathDelimiter
				+ tarName + " -C " + imageTPDir + " " + imageName + " "
				+ trustpolicyName);
		return imageTPDir + imagePathDelimiter + tarName;
	}

	public static String executeShellCommand(String command) {
		// log.debug("Command to execute is:"+command);
		String[] cmd = { "/bin/sh", "-c", command };
		Process p;
		String excludeList = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				result.append(line + "\n");
			}
			if (result.toString().length() != 0) {				excludeList = result.toString();
				excludeList = excludeList.replaceAll("\\n$", "");
			}
			// log.debug("Result of execute command: "+result);
			reader.close();
		} catch (InterruptedException ex) {
			// log.error(null, ex);
			ex.printStackTrace();
		} catch (IOException ex) {
			// log.error(null, ex);
			ex.printStackTrace();
		}
		return excludeList;
	}

	public static boolean isImageEncryptStatus(String existingPolicyDraft)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(TrustPolicy.class);
		Unmarshaller unmarshaller = (Unmarshaller) jaxbContext
				.createUnmarshaller();

		StringReader reader = new StringReader(existingPolicyDraft);
		TrustPolicy policy = (TrustPolicy) unmarshaller.unmarshal(reader);

		if (policy.getEncryption() == null)
			return false;
		else
			return true;

	}

	public String toKey(SshKey sshKey) {
		String key = sshKey.getSshKey();
		return key;

	}

	public String toPassword(SshPassword sshPassword) {
		String password = sshPassword.getKey();
		return password;

	}

	public SshKey fromKey(String sshKey) {
		SshKey key = new SshKey();
		key.setSshKey(sshKey);
		return key;

	}

	public SshPassword fromPassword(String sshPassword) {
		SshPassword password = new SshPassword();
		password.setKey(sshPassword);
		return password;

	}

	public SshSettingRequest toSshSettingRequest(SshSettingInfo sshSettingInfo) {
		SshSettingRequest sshSettingRequest = new SshSettingRequest();

		sshSettingRequest.setId(sshSettingInfo.getId());
		sshSettingRequest.setIpAddress(sshSettingInfo.getIpAddress());
		sshSettingRequest.setKey(toKey(sshSettingInfo.getSshKeyId()));
		sshSettingRequest.setName(sshSettingInfo.getName());
		sshSettingRequest.setPassword(toPassword(sshSettingInfo.getPassword()));
		sshSettingRequest.setUsername(sshSettingInfo.getUsername());
		sshSettingRequest.setCreated_by_user_id(sshSettingInfo
				.getCreated_by_user_id());
		sshSettingRequest.setCreated_date(sshSettingInfo.getCreated_date());
		sshSettingRequest.setEdited_by_user_id(sshSettingInfo
				.getEdited_by_user_id());
		sshSettingRequest.setEdited_date(sshSettingInfo.getEdited_date());
		sshSettingRequest.setImage_id(sshSettingInfo.getImage_id().getId());
		return sshSettingRequest;

	}

	public static PolicyToMountedImageResponse mapImageAttributesToPolicyToMountedImageResponse(
			ImageAttributes image) {
		Mapper mapper = new DozerBeanMapper();
		PolicyToMountedImageResponse mountImageResponse = mapper.map(image,
				PolicyToMountedImageResponse.class);
		return mountImageResponse;
	}

	public static PolicyToHostResponse mapImageAttributesToPolicyToHostResponse(
			SshSettingInfo ssh) {
		Mapper mapper = new DozerBeanMapper();
		PolicyToHostResponse policyToHostResponse = mapper.map(ssh,
				PolicyToHostResponse.class);
		return policyToHostResponse;
	}

	public SshSettingInfo fromSshSettingRequest(
			SshSettingRequest sshSettingRequest) {
		SshSettingInfo sshSettingInfo = new SshSettingInfo();
		sshSettingInfo.setId(sshSettingRequest.getId());
		sshSettingInfo.setIpAddress(sshSettingRequest.getIpAddress());
		sshSettingInfo.setSshKeyId(fromKey(sshSettingRequest.getKey()));
		sshSettingInfo.setName(sshSettingRequest.getName());
		sshSettingInfo
				.setPassword(fromPassword(sshSettingRequest.getPassword()));
		sshSettingInfo.setUsername(sshSettingRequest.getUsername());
		sshSettingInfo.setImage_id(toImage(sshSettingRequest.getImage_id(),
				sshSettingRequest.getIpAddress(),
				sshSettingRequest.getUsername()));
		return sshSettingInfo;

	}

	public ImageAttributes toImage(String id, String ip, String username) {
		/*
		 * Calendar c = Calendar.getInstance(); c.setTime(new Date());
		 * c.add(Calendar.DATE, -3); Date currentDate = new Date();
		 */
		ImageAttributes img = new ImageAttributes();
		img.setCreated_date(new Date());
		img.setEdited_date(new Date());
		img.setDeleted(false);
		img.setId(id);
		img.setImage_deployments("BareMetal");
		img.setImage_format(null);
		img.setImage_size(null);
		img.setLocation(null);
		img.setName(ip);
		img.setSent(null);
		img.setStatus(null);
		return img;
	}

	public TrustPolicyDraftRequest toTrustPolicyDraft(TrustPolicyDraft obj) {
		TrustPolicyDraftRequest trustPolicyDraftRequest = new TrustPolicyDraftRequest();
		trustPolicyDraftRequest.setId(obj.getId());
		trustPolicyDraftRequest.setImage_format(obj.getImgAttributes()
				.getImage_format());
		trustPolicyDraftRequest.setImage_name(obj.getName());
		trustPolicyDraftRequest.setName(obj.getName());
		trustPolicyDraftRequest.setCreated_by_user_id(obj
				.getCreated_by_user_id());
		trustPolicyDraftRequest.setCreated_date(obj.getCreated_date());
		trustPolicyDraftRequest
				.setEdited_by_user_id(obj.getEdited_by_user_id());
		trustPolicyDraftRequest.setEdited_date(obj.getEdited_date());
		return trustPolicyDraftRequest;
	}

	public ImageUploadRequest toImageUpload(ImageStoreUploadTransferObject obj) {
		ImageUploadRequest imageUploadRequest = new ImageUploadRequest();
		DateFormat df = new SimpleDateFormat();
		imageUploadRequest.setDate(df.format(obj.getDate()));
		imageUploadRequest.setImage_format(obj.getImg().getImage_format());
		imageUploadRequest.setName(obj.getImg().getName());

		return imageUploadRequest;
	}

	public static String computeVMMountPath(String imageName, String imagePath)
			throws NoSuchAlgorithmException {
		Md5Digest md5Digest = Md5Digest.digestOf(imagePath.getBytes());
		String hexString = md5Digest.toHexString();
		String prefix = hexString.substring(hexString.length() - 4);

		StringBuilder sb = new StringBuilder(Constants.mountPath);
		sb.append(prefix);
		sb.append(imageName);
		return sb.toString();
	}

	public static String computeVMMountPath(String imageId) {
		StringBuilder sb = new StringBuilder(Constants.mountPath);
		sb.append(imageId);
		return sb.toString();
	}

	public static MountHostResponse mapHostAttributesToMountHostResponse(
			SshSettingInfo sshSettingInfo) {
		Mapper mapper = new DozerBeanMapper();
		MountHostResponse mountHostResponse = mapper.map(sshSettingInfo,
				MountHostResponse.class);
		return mountHostResponse;
	}

	public static UnmountHostResponse mapHostAttributesToUnMountHostResponse(
			SshSettingInfo sshSettingInfo) {
		Mapper mapper = new DozerBeanMapper();
		UnmountHostResponse unmountHostResponse = mapper.map(sshSettingInfo,
				UnmountHostResponse.class);
		return unmountHostResponse;
	}

	// save file to new location
	public static void writeImageToFile(InputStream uploadedInputStream,
			ImageAttributes imageAttributes) throws IOException {
		OutputStream out = null;
		try {
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(imageAttributes.location));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
		imageAttributes.image_size = new Long(
				new File(imageAttributes.location).length()).intValue();
	}

	public static void getParentDirectory(String filePath, String root,
			Map<String, Boolean> parentsList, boolean recursive) {
		File parent = new File(filePath).getParentFile();
		if (parent == null) {
			return;
		}

		if (parent.isDirectory()) {
			try {
				String parentPath = parent.getCanonicalPath()
						.replace("\\", "/");
				if (parentPath.equals(root)) {
					return;
				}
				parentsList.put(parentPath, recursive);
				getParentDirectory(parentPath, root, parentsList, true);
			} catch (IOException e) {
				// TODO Handle Error
				log.error("Error occured at getParentDirectory" + e);
			}
		}

	}

	public ImagesReadyToDeployResponse toImageReadyToDeploy(ImageInfo img)
			throws DirectorException, DbException {
		ImageServiceImpl imageServiceImpl = new ImageServiceImpl();

		ImagesReadyToDeployResponse imagesReadyToDeploy = new ImagesReadyToDeployResponse();
		imagesReadyToDeploy.setCreated_by_user_id(img.getCreated_by_user_id());
		imagesReadyToDeploy.setCreated_date(img.getCreated_date());
		imagesReadyToDeploy.setDisplay_name(imageServiceImpl
				.getDisplayNameForImage(img.getId()));
		imagesReadyToDeploy.setEdited_by_user_id(img.getEdited_by_user_id());
		imagesReadyToDeploy.setEdited_date(img.getEdited_date());
		imagesReadyToDeploy.setImage_format(img.getImage_format());
		imagesReadyToDeploy.setImage_name(img.getName());
		imagesReadyToDeploy.setUser(img.getCreated_by_user_id());
		return imagesReadyToDeploy;
	}

	public static String getKeyIdFromUrl(String url) {
		log.debug("URL :: " + url);
		String[] split = url.split("/");
		int index = 0;
		for (int i = 0; i < split.length; i++) {
			if (split[i].equals("keys")) {
				log.debug("Keys index :: " + i);
				index = ++i;
				break;
			}
		}

		log.debug("Index :: " + index);

		if (index != 0) {
			return split[index];
		} else {
			return null;
		}

	}

	public static boolean addSshKey(String ip, String username, String password)
			throws DirectorException {
		boolean flag = false;
		try{
		if (checkSshConnection(ip, username, password)) {
			log.debug("User home is {}", System.getProperty("user.home"));
			if (!Files.exists(Paths.get(System.getProperty("user.home")
					+ "/.ssh"))) {
				ExecUtil.executeQuoted("/bin/bash", "-c", "mkdir ~/.ssh");
			}
			String addHostKey = "ssh-keyscan -Ht rsa " + ip
					+ " >> ~/.ssh/known_hosts";
			Result executeQuoted = ExecUtil.executeQuoted("/bin/bash", "-c",
					addHostKey);
			log.debug("addHostKey exit code is {}", executeQuoted.getExitCode());
			flag = (executeQuoted.getExitCode() == 0);

		}
		}catch(Exception e){
			log.error("Unable to add SSh key to remot host, addSshKey method",e);
			throw new DirectorException("Unable to add SSh key to remot host",e);
		}
		return flag;
	}

	private static boolean checkSshConnection(String ipaddress,
			String username, String password) throws IOException {
		SSHClient ssh = new SSHClient();

		log.debug("Trying to connect IP :: " + ipaddress);
		ssh.addHostKeyVerifier(new net.schmizz.sshj.transport.verification.PromiscuousVerifier());
		ssh.connect(ipaddress);
		ssh.authPassword(username, password);
		log.debug("Connected To Host :: " + ipaddress);
		boolean authentication = ssh.isAuthenticated();
		log.debug("Host Authentication is {}", authentication);
		ssh.disconnect();
		return authentication;
	}

	public static String getManifestForPolicy(String policyXml)
			throws JAXBException {

		Manifest manifest = new Manifest();
		TrustPolicy trustpolicy = getPolicy(policyXml);
		List<Measurement> measurements = trustpolicy.getWhitelist()
				.getMeasurements();
		manifest.setDigestAlg(trustpolicy.getWhitelist().getDigestAlg().value());
		List<MeasurementType> manifestList = manifest.getManifest();

		MeasurementType measurementType = null;
		for (Measurement measurement : measurements) {
			if (measurement instanceof DirectoryMeasurement) {
				measurementType = new DirectoryMeasurementType();
			} else if (measurement instanceof FileMeasurement) {
				measurementType = new FileMeasurementType();
			}
			if (measurementType != null) {
				measurementType.setPath(measurement.getPath());
				manifestList.add(measurementType);
			}
		}
		JAXB jaxb = new JAXB();
		String result = jaxb.write(manifest);
		log.debug("Manifest is: " + result);
		return result;
	}

	public static String computeHash(MessageDigest md, File file)
			throws IOException {
		if (!file.exists()) {
			return null;
		}
	
		md.reset();
		byte[] bytes = new byte[2048];
		int numBytes;
		FileInputStream is = new FileInputStream(file);

		while ((numBytes = is.read(bytes)) != -1) {
			md.update(bytes, 0, numBytes);
		}
		byte[] digest = md.digest();
		String result = new String(Hex.encodeHex(digest));
		is.close();
		return result;
		
	}

	public static SshSettingRequest convertSshInfoToRequest(SshSettingInfo info) {
		SshSettingRequest setting = new SshSettingRequest();
		setting.setImage_id(info.getImage_id().getId());
		return setting;
	}
	

}
