package com.intel.director.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageListResponse;
import com.intel.director.api.ImageListResponseInfo;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.api.ImageStoreUploadRequest;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.PolicyTemplateInfo;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.SshSettingInfo;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageInfoFilter;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.MountImage;
import com.intel.director.exception.ImageStoreException;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.imagestore.ImageStoreManager;
import com.intel.director.service.ImageService;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;
import com.intel.mtwilson.director.trust.policy.CreateTrustPolicy;
import com.intel.mtwilson.director.trust.policy.DirectoryAndFileUtil;
import com.intel.mtwilson.manifest.xml.DirectoryMeasurementType;
import com.intel.mtwilson.manifest.xml.FileMeasurementType;
import com.intel.mtwilson.manifest.xml.Manifest;
import com.intel.mtwilson.manifest.xml.MeasurementType;
import com.intel.mtwilson.services.mtwilson.vm.attestation.client.jaxrs2.TrustPolicySignature;
import com.intel.mtwilson.shiro.ShiroUtil;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.trustpolicy.xml.DirectoryMeasurement;
import com.intel.mtwilson.trustpolicy.xml.FileMeasurement;
import com.intel.mtwilson.trustpolicy.xml.Measurement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * 
 * @author Siddharth
 */
@Component
public class ImageServiceImpl implements ImageService {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageServiceImpl.class);

	private List<File> files = null;

	private IPersistService imagePersistenceManager;

	private ImageStoreManager imageStoreManager;

	public ImageServiceImpl() {
		imagePersistenceManager = new DbServiceImpl();
	}

	/*
	 * @Override public String testCreatePolicy(String imageId) throws Exception
	 * { TrustPolicyDraft policyDraft = imagePersistenceManager
	 * .fetchPolicyDraftForImage(imageId);
	 * com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = null; try {
	 * policy = TdaasUtil .getPolicy(policyDraft.getTrust_policy_draft());
	 * CreateTrustPolicy.createTrustPolicy(policy); } catch (JAXBException |
	 * IOException | XMLStreamException e) {
	 * log.error("Error while creating Trustpolicy", e); } catch (Exception ex)
	 * { log.error("Error while creating Trustpolicy", ex); } return "SUCCESS";
	 * }
	 */

	@Override
	public MountImageResponse mountImage(String imageId, String user)
			throws DirectorException {

		user = ShiroUtil.subjectUsername();
		log.info("inside mounting image in service");
		log.info("***** Logged in user : " + ShiroUtil.subjectUsername());

		MountImageResponse mountImageResponse = new MountImageResponse();
		ImageAttributes image;
		try {
			image = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException ex) {
			log.error("Error in mounting image  ", ex);
			throw new DirectorException("No image found with id: " + imageId,
					ex);
		}

		// Check if the image is already mounted. If so, return error

		if (image.mounted_by_user_id != null
				&& !image.mounted_by_user_id
						.equals(ShiroUtil.subjectUsername())) {
			log.info("Image already mounted by user : "
					+ image.mounted_by_user_id);
			throw new DirectorException(
					"Unable to mount image. Image is already in use by user: "
							+ image.mounted_by_user_id);

		}
		
			
		if(image.mounted_by_user_id != null && image.mounted_by_user_id.equals(ShiroUtil.subjectUsername())){
			File f = new File(TdaasUtil.getMountPath(imageId));
			if(f.exists()){
				log.info("Not mounting image again");
				return TdaasUtil.mapImageAttributesToMountImageResponse(image);
			}
		}

		log.info("Mounting image from location: " + image.location);

		String mountPath = null;
		if (image.getImage_format() == null) {
			// BML flow
			mountPath = TdaasUtil.getMountPath(image.id);
		} else {
			mountPath = DirectorUtil.getMountPath(image.id);
		}
		log.info("mount path is " + mountPath);
		String mounteImageName = image.getName();
		log.info("mount image : " + mounteImageName);

		try {
			// Mount the image
			if (image.getImage_format() != null) {
				log.info("VM/BM image Mount ");
				if (Constants.DEPLOYMENT_TYPE_BAREMETAL.equals(image
						.getImage_deployments())) {
					log.info("BM IMage mount");
					// Case of Bare metal Image
					String modifiedImagePath = image.getLocation()
							+ image.getName();
					log.info("Imagepath : " + modifiedImagePath);
					DirectorUtil
							.createCopy(
									image.getLocation() + image.getName(),
									image.getLocation() + "Modified_"
											+ image.getName());
					MountImage.unmountImage(mountPath);
					log.info("Unmounting");
					MountImage.mountImage(image.getLocation() + "Modified_"
							+ image.getName(), mountPath);
					log.info("Mount BM image complete");
				} else {
					log.info("VM IMage mount");
					MountImage.mountImage(
							image.getLocation() + mounteImageName, mountPath);
					log.info("VM IMage mount complete");
				}
			} else {
				log.info("BM Live mount flow");
				SshSettingInfo info = imagePersistenceManager
						.fetchSshByImageId(imageId);
				log.info("BM LIve host : " + info.toString());
				int exitCode = MountImage.mountRemoteSystem(
						info.getIpAddress(), info.getUsername(), info
								.getSshPassword().getKey(), mountPath);
				if (exitCode == 1) {
					log.error("Error mounting remote host : " + info.toString());
					throw new DirectorException(
							"Error mounting remote host with the credentials provided : "
									+ info.toString());
				}
				log.info("BM Live  mount complete");
				// Mount Code For bare Metal Live
			}
		} catch (Exception ex) {
			log.error("Unable to mount image", ex);
			throw new DirectorException("Unable to mount image", ex);
		}

		// Mark the image mounted by the user
		try {
			// /image.mounted_by_user_id = user;
			image.setMounted_by_user_id(user);
			Date currentDate = new Date();
			image.setEdited_date(currentDate);
			image.setEdited_by_user_id(ShiroUtil.subjectUsername());
			imagePersistenceManager.updateImage(image);

			log.info("Update mounted_by_user for image in DB for image at location : "
					+ image.getLocation());
			mountImageResponse = TdaasUtil
					.mapImageAttributesToMountImageResponse(image);
			// /// MountImage.mountImage(image.getLocation(), mountPath);
			log.info("*** Completed mounting of image");
		} catch (DbException ex) {
			log.error("Error while saving mount data to database: "
					+ ex.getMessage());
			throw new DirectorException(
					"Unable to reset mounted_by_user_id field", ex);
		} catch (Exception e) {
			log.error("Error while mounting image : " + e.getMessage());
			try {
				log.info("As rollback unmounting image");
				if (image.getImage_format() != null) {
					MountImage.unmountImage(mountPath);
				} else {

					MountImage.unmountRemoteSystem(mountPath);
					// UnMount Code For bare Metal Live
				}

				log.info("Completed unmounting of image");
			} catch (Exception unmountEx) {
				log.error("*Error while unmount of image : "
						+ unmountEx.getMessage());
				throw new DirectorException(
						"Failed to unmount image. The attempt was made after the DB update for mounted_by_user failed. ",
						unmountEx);
			}
		}
		log.debug("Image mounted succesfully imageId:" + imageId);
		return mountImageResponse;
	}

	@Override
	public UnmountImageResponse unMountImage(String imageId, String user)
			throws DirectorException {
		log.info("inside unmounting image in service");
		user = ShiroUtil.subjectUsername();
		UnmountImageResponse unmountImageResponse = null;
		try {
			ImageAttributes image = imagePersistenceManager
					.fetchImageById(imageId);
			log.info("Unmounting image : " + image.id + " with name : "
					+ image.name);
			String mountPath = null;
			if (image.getImage_format() == null) {
				// BML flow
				mountPath = TdaasUtil.getMountPath(image.id);
			} else {
				mountPath = DirectorUtil.getMountPath(image.id);
			}

			if (image.getMounted_by_user_id() == null) {
				unmountImageResponse = TdaasUtil
						.mapImageAttributesToUnMountImageResponse(image);
				return unmountImageResponse;
			}

			image.setMounted_by_user_id(null);
			Date currentDate = new Date();
			image.setEdited_date(currentDate);
			image.setEdited_by_user_id(ShiroUtil.subjectUsername());
			imagePersistenceManager.updateImage(image);

			if (image.getImage_format() != null) {
				// Throw an exception if different user than the mounted_by user
				// tries to unmount

				log.info("Updated DB with unmount data");
				// Unmount the image

				log.info("Unmounting from location : " + mountPath);
				MountImage.unmountImage(mountPath);
				log.info("Unmount script execution complete : " + mountPath);
				log.info("*** unmount BM/VM complete");
			} else {
				MountImage.unmountRemoteSystem(mountPath);
				log.info("*** unmount of BM LIVE complete");
			}
			unmountImageResponse = TdaasUtil
					.mapImageAttributesToUnMountImageResponse(image);
		} catch (DbException dbe) {
			log.error("Error while updating DB with unmount data: "
					+ dbe.getMessage());
			throw new DirectorException(
					"Error while updating DB with unmount data", dbe);
		} catch (Exception e) {
			log.error("Error while unmount : " + e.getMessage());
			throw new DirectorException("Error while unmount ", e);
		}
		return unmountImageResponse;
	}

	/**
	 * 
	 * @param imageId
	 * @param imageFileInputStream
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	@Override
	public SearchImagesResponse searchImages(
			SearchImagesRequest searchImagesRequest) throws DbException {
		SearchImagesResponse searchImagesResponse = new SearchImagesResponse();
		List<ImageInfo> fetchImages = null;
		// Fetch all images
		if (searchImagesRequest.deploymentType == null) {
			fetchImages = imagePersistenceManager.fetchImages(null);
		} else {

			// Fetch images for the deployment type
			ImageInfoFilter filter = new ImageInfoFilter();
			filter.image_deployments = searchImagesRequest.deploymentType;
			if (searchImagesRequest.deploymentType.equals("BareMetalLive")) {
				filter.image_deployments = Constants.DEPLOYMENT_TYPE_BAREMETAL;
			}
			fetchImages = imagePersistenceManager.fetchImages(filter, null);
		}
		searchImagesResponse.images = fetchImages;
		return searchImagesResponse;
	}

	/**
	 * Method for creating new image metadata for the image being
	 * uploaded and store it in the DB.
	 * @param image_deployments
	 * @param image_format
	 * @param fileName
	 * @param fileSize
	 * @return TrustDirectorImageUploadResponse - newly created 
	 * 					image metadata in response
	 * @throws DirectorException
	 */
	@Override
	public TrustDirectorImageUploadResponse createUploadImageMetadataImpl(
			String image_deployments, String image_format, String fileName,
			String fileSize) throws DirectorException {

		String loggedinUser = ShiroUtil.subjectUsername();

		ImageAttributes imageAttributes = new ImageAttributes();
		imageAttributes.name = fileName;
		imageAttributes.image_deployments = image_deployments;
		imageAttributes.setCreated_by_user_id(loggedinUser);
		imageAttributes.setEdited_by_user_id(loggedinUser);
		imageAttributes.setStatus(Constants.INCOMPLETE);
		imageAttributes.setDeleted(false);
		int sizen_kb = Integer.parseInt(fileSize);
		imageAttributes.setSent(0);
		imageAttributes.setImage_size(sizen_kb);
		imageAttributes.location = Constants.defaultUploadPath;
		imageAttributes.image_format = image_format;
		Date currentDate = new Date();
		imageAttributes.setCreated_date(currentDate);
		imageAttributes.setEdited_date(currentDate);
		imageAttributes.setEdited_by_user_id(ShiroUtil.subjectUsername());
		imageAttributes.setCreated_by_user_id(ShiroUtil.subjectUsername());

		ImageAttributes createdImageMetadata = new ImageAttributes();
		try {
			log.debug("Saving metadata of uploaded file");
			createdImageMetadata = imagePersistenceManager
					.saveImageMetadata(imageAttributes);
		} catch (DbException e) {
			log.error("Error while saving metadata for uploaded file : "
					+ e.getMessage());
			throw new DirectorException("Cannot save image meta data", e);
		}
		log.info("Saved Image upload metadata.");

		return TdaasUtil
				.mapImageAttributesToTrustDirectorImageUploadResponse(createdImageMetadata);
	}

	/**
	 * Method for uploading image data sent in 
	 * chunks for the given image_id
	 * @param image_id - id received as part of response for
	 * 					../uploadMetadata API
	 * @param fileInputStream - image data send in chunks
	 * @return TrustDirectorImageUploadResponse - updated image
	 * 					upload metadata
	 * @throws DirectorException
	 */
	@Override
	public TrustDirectorImageUploadResponse uploadImageToTrustDirectorSingle(
			String image_id, InputStream fileInputStream)
			throws DirectorException {

		ImageInfo imageInfo = new ImageInfo();
		try {
			imageInfo = imagePersistenceManager.fetchImageById(image_id);
			log.info("Obtained image upload metadata for the id " + image_id
					+ " and imageName " + imageInfo.getName());
			if (imageInfo.getStatus() == Constants.COMPLETE) {
				TrustDirectorImageUploadResponse trustDirectorImageUploadResponse = new TrustDirectorImageUploadResponse();
				trustDirectorImageUploadResponse
						.setStatus("Error: given image has already been uploaded");
				return trustDirectorImageUploadResponse;
			}
		} catch (DbException e) {
			log.error("Error while getting metadata for the image with id "
					+ image_id + ": " + e.getMessage());
			throw new DirectorException("Failed to load image metdata", e);
		}
		int bytesread = 0;
		try {
			int read = 0;
			byte[] bytes = new byte[1024];

			OutputStream out = new FileOutputStream(new File(
					imageInfo.getLocation() + imageInfo.getName()), true);
			while ((read = fileInputStream.read(bytes)) != -1) {
				bytesread += read;
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			log.error("Error while writing uploaded image: " + e.getMessage());
			throw new DirectorException("Cannot write the uploaded image", e);
		}

		imageInfo.setSent(imageInfo.getSent() + (int) (bytesread / 1024));
		log.info("Sent in KB Neww: " + imageInfo.getSent());
		log.info("image size: " + imageInfo.getImage_size());
		if (imageInfo.getSent().intValue() == imageInfo.getImage_size()
				.intValue()) {
			imageInfo.setStatus(Constants.COMPLETE);
			log.info("Image upload COMPLETE..");
		}
		try {
			imagePersistenceManager.updateImage(imageInfo);
		} catch (DbException e) {
			log.error("Error while updating metadata for uploaded image : "
					+ e.getMessage());
			throw new DirectorException("Cannot update image meta data", e);
		}
		log.info("Updated Image upload metadata.");

		return TdaasUtil
				.mapImageAttributesToTrustDirectorImageUploadResponse(imageInfo);
	}

	@Override
	public SearchFilesInImageResponse searchFilesInImage(
			SearchFilesInImageRequest searchFilesInImageRequest)
			throws DirectorException {
		log.info("Browsing files for dir: "
				+ searchFilesInImageRequest.getDir());
		List<String> trustPolicyElementsList = new ArrayList<String>();
		Set<String> fileNames = new HashSet<String>();
		Set<String> patchFileAddSet = new HashSet<String>();
		Set<String> patchFileRemoveSet = new HashSet<String>();
		Set<String> patchDirAddSet = new HashSet<String>();
		Collection<File> treeFiles = new ArrayList<>();

		String mountPath = DirectorUtil
				.getMountPath(searchFilesInImageRequest.id);
		log.info("Browsing files for on image mounted at : " + mountPath);

		mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);
		log.info("NOW Browsing files for on image mounted at : " + mountPath);

		SearchFilesInImageResponse filesInImageResponse = new SearchFilesInImageResponse();
		Collection<File> listFilesAndDirs = getFirstLevelFiles(searchFilesInImageRequest);

		List<Measurement> measurements = getMeasurements(searchFilesInImageRequest.id);
		// this map contains the directory paths as key. the value - boolean
		// indicates whether all
		// the sub files of these directory need to be fetched.
		// false - only first level files
		Map<String, Boolean> directoryListContainingPolicyFiles = new HashMap<>();
		Set<String> directoryListContainingRegex = new HashSet<String>();
		init(trustPolicyElementsList, directoryListContainingPolicyFiles,
				directoryListContainingRegex, searchFilesInImageRequest);

		// Fetch the files
		if (searchFilesInImageRequest.recursive) {
			treeFiles = getFilesAndDirectories(
					searchFilesInImageRequest.getDir(), null,
					searchFilesInImageRequest.id);
		} else {
			treeFiles = listFilesAndDirs;
		}

		// In case of edit mode we want to show the exploded view
		// so that the selected file is visible in the initial screen.
		// For that we use the directoryListContainingPolicyFiles to find the
		// directories
		// that contain that file/s

		// populate filenames with the leaf nodes of the root folder

		createListOfFileNamesForTree(searchFilesInImageRequest, treeFiles,
				fileNames, directoryListContainingPolicyFiles.keySet());

		// Now add the exploded file view in case of edit
		Set<String> dirsForEdit = new HashSet<String>();

		for (String dirPath : directoryListContainingPolicyFiles.keySet()) {
			if (directoryListContainingPolicyFiles.get(dirPath)) {
				List<File> filesAndDirectories = getFilesAndDirectories(
						dirPath, dirsForEdit, searchFilesInImageRequest.id);
				createListOfFileNamesForTree(searchFilesInImageRequest,
						filesAndDirectories, fileNames,
						directoryListContainingPolicyFiles.keySet());
			} else {

				Collection<File> firstLevelFiles = getFirstLevelFiles(dirPath,
						searchFilesInImageRequest.id);
				createListOfFileNamesForTree(searchFilesInImageRequest,
						firstLevelFiles, fileNames);
			}
		}

		String parent = searchFilesInImageRequest.getDir();
		TreeNode root = new TreeNode(parent, parent);

		Tree tree = new Tree(root, searchFilesInImageRequest.recursive,
				searchFilesInImageRequest.files_for_policy);
		root.parent = tree;
		tree.mountPath = mountPath;
		tree.directoryListContainingRegex = directoryListContainingRegex;

		// In case of regex, find the list of files and add it here and then set
		// it in
		if (searchFilesInImageRequest.reset_regex) {
			// populate list of measurements to be deleted
			createlistOfFilesForRegexReset(searchFilesInImageRequest,
					patchFileRemoveSet);
		} else if (searchFilesInImageRequest.include != null
				|| searchFilesInImageRequest.exclude != null) {
			Collection<File> regexFiles = getFilesAndDirectoriesWithFilter(searchFilesInImageRequest);
			trustPolicyElementsList = new ArrayList<String>();
			for (File file : regexFiles) {
				if (file.isFile()) {
					patchFileAddSet.add(file.getAbsolutePath().replace(
							mountPath, ""));
				}

				if (!trustPolicyElementsList.contains(file.getAbsolutePath()
						.replace(mountPath, ""))) {
					trustPolicyElementsList.add(file.getAbsolutePath().replace(
							mountPath, ""));
				}
			}

			patchDirAddSet.add(searchFilesInImageRequest.getDir());
			root.rootDirWithRegex = parent;
		}

		// Select all on directory
		if (searchFilesInImageRequest.recursive) {
			filesInImageResponse.patchXml = new ArrayList<>();
			String parentDir = searchFilesInImageRequest.getDir();
			if (searchFilesInImageRequest.files_for_policy) {
				// This means that the user has checked the parent directory
				// checkbox
				for (String fileName : fileNames) {
					String path = mountPath + parentDir + fileName;
					if (new File(path).isFile()) {
						patchFileAddSet.add(parentDir + fileName);
					}
					trustPolicyElementsList.add(parentDir + fileName);
				}
			} else {
				for (String fileName : fileNames) {
					if (new File(mountPath + parentDir + fileName).isFile()) {
						patchFileRemoveSet.add(parentDir + fileName);
					}
				}
			}
		}

		tree.setTrustPolicyElementsList(trustPolicyElementsList);
		tree.setDirPathsForEdit(dirsForEdit);

		for (String data : fileNames) {
			tree.addElement(data);
		}

		tree.printTree();

		// Create patch to be sent in case of directory selection or regex
		buildPatch(patchDirAddSet, patchFileAddSet, patchFileRemoveSet,
				measurements, searchFilesInImageRequest, filesInImageResponse);

		filesInImageResponse.files = tree.treeElementsHtml;
		return filesInImageResponse;

	}

	/**
	 * 
	 * @param imageStoreUploadRequest
	 * @return
	 * @throws DirectorException
	 * @throws ImageStoreException
	 */

	@Override
	public String getTrustPolicyForImage(String imageId) {
		String policyDraftXml = null;
		try {
			TrustPolicyDraft fetchPolicyDraftForImage = imagePersistenceManager
					.fetchPolicyDraftForImage(imageId);
			policyDraftXml = fetchPolicyDraftForImage.getTrust_policy_draft();
		} catch (DbException e) {
			// TODO Handle Error
			log.error("Error in fetchPolicyDraftForImage");
		}

		return policyDraftXml;
	}

	@Override
	public TrustPolicy getTrustPolicyByTrustId(String trustId) {
		TrustPolicy fetchPolicy = null;
		try {
			fetchPolicy = imagePersistenceManager.fetchPolicyById(trustId);

		} catch (DbException e) {
			// TODO Handle Error
			log.error("Error in fetching Trust Policy By Id");
		}

		return fetchPolicy;
	}

	@Override
	public void editTrustPolicyDraft(
			TrustPolicyDraftEditRequest trustpolicyDraftEditRequest) {
		TrustPolicyDraft trustPolicyDraft = null;
		try {
			trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(trustpolicyDraftEditRequest.imageId);
		} catch (DbException e) {
			// TODO Handle Error
			log.error("Error in editTrustPolicyDraft()");
		}

		String draft = trustPolicyDraft.getTrust_policy_draft();

		draft = TdaasUtil.patch(draft, trustpolicyDraftEditRequest.patch);
		Date currentDate = new Date();
		trustPolicyDraft.setEdited_date(currentDate);
		trustPolicyDraft.setTrust_policy_draft(draft);
		trustPolicyDraft.setEdited_by_user_id(ShiroUtil.subjectUsername());

		try {
			imagePersistenceManager.updatePolicyDraft(trustPolicyDraft);
		} catch (DbException e) {
			// TODO Handle Error
			log.error("Error in editTrustPolicyDraft()");
		}

	}

	public CreateTrustPolicyMetaDataRequest getPolicyMetadata(String draftid)
			throws DirectorException {
		CreateTrustPolicyMetaDataRequest metadata = new CreateTrustPolicyMetaDataRequest();
		try {
			TrustPolicyDraft trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftById(draftid);
			String trustPolicyDraftxml = trustPolicyDraft
					.getTrust_policy_draft();
			JAXBContext jaxbContext = JAXBContext
					.newInstance(com.intel.mtwilson.trustpolicy.xml.TrustPolicy.class);
			Unmarshaller unmarshaller = (Unmarshaller) jaxbContext
					.createUnmarshaller();

			StringReader reader = new StringReader(trustPolicyDraftxml);
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = (com.intel.mtwilson.trustpolicy.xml.TrustPolicy) unmarshaller
					.unmarshal(reader);

			metadata.setImage_name(trustPolicyDraft.getImgAttributes()
					.getName());
			// /metadata.setIsEncrypted(isEncrypted);
			metadata.setLaunch_control_policy(policy.getLaunchControlPolicy()
					.value());
			// /metadata.setSelected_image_format(selected_image_format);
		} catch (DbException e) {
			log.error("Unable to get policy metadata draftid::" + draftid, e);
			throw new DirectorException(e);
		} catch (JAXBException e) {
			log.error("Unable to get policy metadata draftid::" + draftid, e);
			throw new DirectorException(e);
		}
		return metadata;
	}

	public CreateTrustPolicyMetaDataRequest getPolicyMetadataForImage(
			String image_id) throws DirectorException {

		CreateTrustPolicyMetaDataRequest metadata = new CreateTrustPolicyMetaDataRequest();
		try {

			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy;
			String policyXml;
			TrustPolicyDraft trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(image_id);
			if (trustPolicyDraft == null) {
				TrustPolicy trustPolicy = imagePersistenceManager
						.fetchPolicyForImage(image_id);
				if (trustPolicy == null) {
					throw new DirectorException(
							"Neither Policy draft nor Polci exists do not exist");
				}
				policyXml = trustPolicy.getTrust_policy();
				metadata.setImage_name(trustPolicy.getImgAttributes().getName());
				metadata.setDisplay_name(trustPolicy.getDisplay_name());

			} else {
				policyXml = trustPolicyDraft.getTrust_policy_draft();
				metadata.setImage_name(trustPolicyDraft.getImgAttributes()
						.getName());
				metadata.setDisplay_name(trustPolicyDraft.getDisplay_name());
			}

			JAXBContext jaxbContext = JAXBContext
					.newInstance(com.intel.mtwilson.trustpolicy.xml.TrustPolicy.class);
			Unmarshaller unmarshaller = (Unmarshaller) jaxbContext
					.createUnmarshaller();

			StringReader reader = new StringReader(policyXml);
			policy = (com.intel.mtwilson.trustpolicy.xml.TrustPolicy) unmarshaller
					.unmarshal(reader);

			// /metadata.setIsEncrypted(isEncrypted);
			metadata.setLaunch_control_policy(policy.getLaunchControlPolicy()
					.value());
			if (policy.getEncryption() != null) {
				metadata.setEncrypted(true);
			}

		} catch (DbException e) {
			// TODO Handle Error
			throw new DirectorException(e);
		} catch (JAXBException e) {
			throw new DirectorException(e);
		}
		return metadata;

	}

	public String createTrustPolicy(String image_id) throws DirectorException {

		ImageAttributes image = null;
		try {

			TrustPolicyDraft existingDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(image_id);
			if (existingDraft == null) {

				throw new DirectorException("Policy draft do not exist");
			}

			Date currentDate = new Date();

			TrustPolicy trustPolicy = new TrustPolicy();
			// trustPolicy.setCreated_date(currentDate);

			// trustPolicy.setEdited_date(currentDate);

			String policyXml = existingDraft.getTrust_policy_draft();
			String display_name = existingDraft.getDisplay_name();

			// / trustPolicy.setTrust_policy(policyXml);
			trustPolicy.setDisplay_name(display_name);
			ImageAttributes imgAttrs = new ImageAttributes();
			imgAttrs.setId(image_id);
			trustPolicy.setImgAttributes(imgAttrs);
			log.debug("Going to save trust policy for image_id::" + image_id);
			try {
				image = imagePersistenceManager.fetchImageById(image_id);
			} catch (DbException ex) {
				log.error("Error in mounting image  ", ex);
				throw new DirectorException("No image found with id: "
						+ image_id, ex);
			}
			// Get the hash
			try {
				com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil
						.getPolicy(policyXml);
				CreateTrustPolicy.createTrustPolicy(policy);

				// Calculate image hash and add to encryption tag
				if (policy.getEncryption() != null) {
					File imgFile = new File(image.getLocation()
							+ image.getName());
					log.info("Calculating MD5 of file : " + image.getLocation()
							+ image.getName());
					try {
						String computeHash = TdaasUtil.computeHash(
								MessageDigest.getInstance("MD5"), imgFile);
						policy.getEncryption().getChecksum()
								.setValue(computeHash);
					} catch (IOException e) {
						log.error("Error while calculating hash of image", e);
					}

				}

				policyXml = TdaasUtil.convertTrustPolicyToString(policy);
				log.info("****** HASH : " + policyXml);
				// Sign the policy with MtWilson
				Extensions
						.register(
								TlsPolicyCreator.class,
								com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);
				Properties p = DirectorUtil
						.getPropertiesFile(Constants.MTWILSON_PROP_FILE);// My.configuration().getClientProperties();
				TrustPolicySignature client = new TrustPolicySignature(p);
				String signedPolicyXml = client.signTrustPolicy(policyXml);
				log.info("****** SIGN : " + signedPolicyXml);

				// /SignWithMtWilson mtw = new SignWithMtWilson();
				// ///mtw.signManifest(image_id, policyXml);
				String trustPolicyName = null;
				File trustPolicyFile = null;
				String mountPath = TdaasUtil.getMountPath(image_id);
				if (trustPolicy != null) {
					if ((Constants.DEPLOYMENT_TYPE_BAREMETAL.equals(image
							.getImage_deployments()) && image.getImage_format() != null)
							|| (image.getImage_format() == null)) {

						// Writing inside bare metal modified image

						String remoteDirPath = mountPath + "/boot/trust";
						if (!Files.exists(Paths.get(remoteDirPath)))
							;
						DirectorUtil.callExec("mkdir -p " + remoteDirPath);
						// // String policyPath =
						// remoteDirPath+"/"+"trustpolicy.xml";
						trustPolicyName = "trustpolicy.xml";
						/*
						 * if (image.getImage_format() == null) {
						 * trustPolicyName = "policy.xml"; } else {
						 * trustPolicyName = "policy_" +
						 * trustPolicy.getDisplay_name() + ".xml"; }
						 */
						trustPolicyFile = new File(remoteDirPath
								+ File.separator + trustPolicyName);

						File manifestFile = new File(remoteDirPath
								+ File.separator + "manifest.xml");
						String manifest = TdaasUtil
								.getManifestForPolicy(policyXml);
						if (!manifestFile.exists()) {
							manifestFile.createNewFile();
						}

						FileWriter fw = new FileWriter(
								manifestFile.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(manifest);
						bw.close();
						fw.close();

					} else {

						trustPolicyName = "trustpolicy-"
								+ trustPolicy.getDisplay_name() + ".xml";

						trustPolicyFile = new File(Constants.defaultUploadPath
								+ File.separator + trustPolicyName);

					}

					if (!trustPolicyFile.exists()) {
						trustPolicyFile.createNewFile();
					}

					FileWriter fw = new FileWriter(
							trustPolicyFile.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(signedPolicyXml);
					bw.close();
					fw.close();

				}

				trustPolicy.setTrust_policy(signedPolicyXml);
				trustPolicy.setCreated_date(currentDate);
				trustPolicy.setEdited_date(currentDate);
				trustPolicy.setEdited_by_user_id(ShiroUtil.subjectUsername());
				trustPolicy.setCreated_by_user_id(ShiroUtil.subjectUsername());

				TrustPolicy createdPolicy = imagePersistenceManager
						.savePolicy(trustPolicy);
				log.debug("deleting draft after policy created existingDraftId::"
						+ existingDraft.getId());
				imagePersistenceManager.destroyPolicyDraft(existingDraft);
				log.debug("trust policy succesfylly created , createdPolicyId::"
						+ createdPolicy.getId());

				// Creating an ImageAction
				if (TdaasUtil.isImageEncryptStatus(policyXml)) {
					return createImageActionById(image_id, trustPolicy, true);
				}

			} catch (Exception e) {
				log.error("Error while creating trust policy", e);
				throw new DirectorException(
						"Exception while creating trust policy from draft", e);
			}
		} catch (DbException e) {
			log.error("Db exception thrown in create trust policy", e);
			throw new DirectorException(e);
		} catch (Exception e) {
			log.error("Error getting MtWIlson signature for image id : "
					+ image_id, e);
			throw new DirectorException(e);
		}
		return "";
	}

	private String createImageActionById(String image_id,
			TrustPolicy existingPolicy, boolean isFlowUpload)
			throws DirectorException {
		try {
			List<ImageActionActions> actions = new ArrayList<ImageActionActions>();
			ImageActionObject imageActionObject = new ImageActionObject();
			ImageActionObject createdImageActionObject = new ImageActionObject();
			ImageActionActions imageActionActions = new ImageActionActions();
			imageActionObject.setImage_id(image_id);
			if (existingPolicy != null
					&& isFlowUpload
					&& TdaasUtil.isImageEncryptStatus(existingPolicy
							.getTrust_policy())) {
				imageActionActions
						.setTask_name(Constants.TASK_NAME_ENCRYPT_IMAGE);
				imageActionActions.setStatus(Constants.INCOMPLETE);
				imageActionObject.setAction_completed(0);
				imageActionObject.setAction_count(1);
				imageActionObject.setAction_size(0);
				imageActionObject.setAction_size_max(1);
				imageActionObject
						.setCurrent_task_name(Constants.TASK_NAME_ENCRYPT_IMAGE);
				imageActionObject.setCurrent_task_status(Constants.INCOMPLETE);
				actions.add(imageActionActions);
				imageActionObject.setAction(actions);
				createdImageActionObject = imagePersistenceManager
						.createImageAction(imageActionObject);
				log.debug("createImageActionById success actiond::"
						+ createdImageActionObject.getId());

			} else {
				createdImageActionObject = imagePersistenceManager
						.createImageAction(imageActionObject);
			}
			if (createdImageActionObject.getId() == null) {
				return "";
			} else {
				return createdImageActionObject.getId();
			}
		} catch (Exception e) {

			throw new DirectorException(e);
		}
	}

	public CreateTrustPolicyMetaDataResponse saveTrustPolicyMetaData(
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest)
			throws DirectorException {
		CreateTrustPolicyMetaDataResponse createPolicyMetadataResponse = new CreateTrustPolicyMetaDataResponse();
		try {
			Date currentDate = new Date();
			String imageid = createTrustPolicyMetaDataRequest.getImageid();
			TrustPolicyDraft existingDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(imageid);

			if (existingDraft == null) {
				TrustPolicy existingPolicy = imagePersistenceManager
						.fetchPolicyForImage(imageid);
				ImageInfo imageInfo = imagePersistenceManager
						.fetchImageById(imageid);

				if (existingPolicy == null) {
					TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
					trustPolicyDraft.setCreated_date(currentDate);

					trustPolicyDraft.setEdited_date(currentDate);
					createTrustPolicyMetaDataRequest
							.setDeploymentType(imageInfo.image_deployments);
					String trust_policy_draft = TdaasUtil
							.generateInitialPolicyDraft(createTrustPolicyMetaDataRequest);
					trustPolicyDraft.setTrust_policy_draft(trust_policy_draft);
					trustPolicyDraft
							.setDisplay_name(createTrustPolicyMetaDataRequest
									.getDisplay_name());

					TrustPolicyDraft policyDraftCreated;
					ImageAttributes imgAttrs = new ImageAttributes();
					imgAttrs.setId(imageid);
					trustPolicyDraft.setImgAttributes(imgAttrs);
					trustPolicyDraft.setCreated_date(new Date());
					trustPolicyDraft.setEdited_date(new Date());
					trustPolicyDraft.setEdited_by_user_id(ShiroUtil
							.subjectUsername());
					trustPolicyDraft.setCreated_by_user_id(ShiroUtil
							.subjectUsername());

					policyDraftCreated = imagePersistenceManager
							.savePolicyDraft(trustPolicyDraft);

					createPolicyMetadataResponse.setId(policyDraftCreated
							.getId());
					createPolicyMetadataResponse
							.setTrustPolicy(policyDraftCreated
									.getTrust_policy_draft());
					createPolicyMetadataResponse.status = "NEW";
				} else {
					//Signed policy exists. We need to copy it over sans the
					//signature and hashes

					log.debug("policy draft already exists");
					TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
					trustPolicyDraft.setCreated_date(currentDate);

					trustPolicyDraft.setEdited_date(currentDate);
					String policyXml = existingPolicy.getTrust_policy();
					trustPolicyDraft
							.setDisplay_name(createTrustPolicyMetaDataRequest
									.getDisplay_name());
					
					com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil.getPolicy(policyXml);
					policy.setSignature(null);
					List<Measurement> measurements = policy.getWhitelist().getMeasurements();
					for(Measurement measurement:measurements){
						measurement.setValue(null);
					}
					
					policyXml = TdaasUtil.convertTrustPolicyToString(policy);
					
					String unsigned_trust_policy = policyXml;// / Need to be
					// removed

					trustPolicyDraft
							.setTrust_policy_draft(unsigned_trust_policy);

					ImageAttributes imgAttrs = new ImageAttributes();
					imgAttrs.setId(imageid);
					trustPolicyDraft.setImgAttributes(imgAttrs);
					TrustPolicyDraft policyDraftCreated;
					trustPolicyDraft.setCreated_date(new Date());
					trustPolicyDraft.setEdited_date(new Date());
					trustPolicyDraft.setEdited_by_user_id(ShiroUtil
							.subjectUsername());
					trustPolicyDraft.setCreated_by_user_id(ShiroUtil
							.subjectUsername());
					policyDraftCreated = imagePersistenceManager
							.savePolicyDraft(trustPolicyDraft);
					log.debug("policy draft created from existing policy, policyDraftCreatedId::"
							+ policyDraftCreated.getId());
				}

			} else {

				existingDraft.setEdited_date(currentDate);
				String existingTrustPolicyDraftxml = existingDraft
						.getTrust_policy_draft();
				String trust_policy_draft = TdaasUtil.updatePolicyDraft(
						existingTrustPolicyDraftxml,
						createTrustPolicyMetaDataRequest);
				existingDraft.setTrust_policy_draft(trust_policy_draft);
				existingDraft.setDisplay_name(createTrustPolicyMetaDataRequest
						.getDisplay_name());

				ImageAttributes imgAttrs = new ImageAttributes();
				imgAttrs.setId(imageid);
				existingDraft.setImgAttributes(imgAttrs);
				existingDraft.setEdited_date(new Date());
				existingDraft.setEdited_by_user_id(ShiroUtil.subjectUsername());
				imagePersistenceManager.updatePolicyDraft(existingDraft);

				createPolicyMetadataResponse.setId(existingDraft.getId());
				createPolicyMetadataResponse.setTrustPolicy(existingDraft
						.getTrust_policy_draft());

			}
		} catch (DbException e) {
			log.error("saving metatdata failed", e);
			throw new DirectorException(e);
		} catch (JAXBException e) {
			log.error("saving metatdata failed", e);
			throw new DirectorException(e);
		}
		log.debug("policy metadata succesfully saved");
		return createPolicyMetadataResponse;
	}

	/**
	 * *************************************************************************
	 * *************
	 */
	/**
	 * Setters and getters
	 * 
	 * @param imagePersistenceManager
	 * @param imageStoreManager
	 * @return
	 */
	@Autowired
	public ImageServiceImpl(IPersistService imagePersistenceManager) {
		this.imagePersistenceManager = imagePersistenceManager;
		this.imageStoreManager = imageStoreManager;
	}

	private void createListOfFileNamesForTree(
			SearchFilesInImageRequest searchFilesInImageRequest,
			Collection<File> treeFiles, Set<String> fileNames) {
		String mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);
		for (File file : treeFiles) {
			String _file = file.getAbsolutePath().replace("\\", "/")
					.replace(mountPath, "");
			_file = _file.replaceFirst(searchFilesInImageRequest.getDir(), "");
			fileNames.add(_file);
		}
	}

	private void createListOfFileNamesForTree(
			SearchFilesInImageRequest searchFilesInImageRequest,
			Collection<File> treeFiles, Set<String> fileNames,
			Set<String> directoryListContainingPolicyFiles) {
		String mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);
		for (File file : treeFiles) {
			String _file = file.getAbsolutePath().replace(mountPath, "");
			_file = _file.replaceFirst(searchFilesInImageRequest.getDir(), "");
			if (!directoryListContainingPolicyFiles.contains(mountPath
					+ File.separator + _file)) {
				fileNames.add(_file);
			}
		}
	}

	private void init(List<String> trustPolicyElementsList,
			Map<String, Boolean> directoryListContainingPolicyFiles,
			Set<String> directoryListContainingRegex,
			SearchFilesInImageRequest searchFilesInImageRequest) {
		/*
		 * if (!searchFilesInImageRequest.init) { trustPolicyElementsList =
		 * null; return; }
		 */
		// Fetch the files from the draft
		TrustPolicyDraft trustPolicyDraft = null;
		String mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);
		try {
			trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(searchFilesInImageRequest.id);
			JAXBContext jaxbContext;

			try {
				jaxbContext = JAXBContext
						.newInstance(com.intel.mtwilson.trustpolicy.xml.TrustPolicy.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				StringReader reader = new StringReader(
						trustPolicyDraft.getTrust_policy_draft());
				com.intel.mtwilson.trustpolicy.xml.TrustPolicy trustPolicyDraftObj = (com.intel.mtwilson.trustpolicy.xml.TrustPolicy) unmarshaller
						.unmarshal(reader);
				if (trustPolicyDraftObj.getWhitelist().getMeasurements().size() > 0) {
					for (Measurement measurement : trustPolicyDraftObj
							.getWhitelist().getMeasurements()) {
						if (searchFilesInImageRequest.init) {
							trustPolicyElementsList.add(measurement.getPath());

							if (measurement instanceof FileMeasurement) {
								TdaasUtil.getParentDirectory(
										searchFilesInImageRequest.id,
										mountPath + measurement.getPath(),
										mountPath
												+ searchFilesInImageRequest
														.getDir(),
										directoryListContainingPolicyFiles,
										true);
							}
							if (measurement instanceof DirectoryMeasurement) {
								directoryListContainingRegex.add(measurement
										.getPath());
							}

						}
					}
				}
			} catch (JAXBException e) {
				log.error(
						"Error while reading JAXB creating file list from policy draft  ",
						e);
			}

		} catch (DbException ex) {
			log.error("Error while fetching current policy draft ", ex);
		}

	}

	private List<Measurement> getMeasurements(String imageID) {
		try {
			TrustPolicyDraft trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(imageID);
			JAXBContext jaxbContext;

			jaxbContext = JAXBContext
					.newInstance(com.intel.mtwilson.trustpolicy.xml.TrustPolicy.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			StringReader reader = new StringReader(
					trustPolicyDraft.getTrust_policy_draft());
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy trustPolicyDraftObj = (com.intel.mtwilson.trustpolicy.xml.TrustPolicy) unmarshaller
					.unmarshal(reader);
			if (trustPolicyDraftObj.getWhitelist().getMeasurements().size() > 0) {
				return trustPolicyDraftObj.getWhitelist().getMeasurements();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void buildPatch(Set<String> patchDirAddSet,
			Set<String> patchFileAddSet, Set<String> patchFileRemoveSet,
			List<Measurement> measurements,
			SearchFilesInImageRequest searchFilesInImageRequest,
			SearchFilesInImageResponse filesInImageResponse) {
		// Build the PATCH
		// Add Dir patch
		if (filesInImageResponse.patchXml == null) {
			filesInImageResponse.patchXml = new ArrayList<>();
		}
		boolean includeDir = false;
		if (searchFilesInImageRequest.include != null) {
			includeDir = true;
		}
		// Remove patch
		if (measurements != null) {
			for (Measurement measurement : measurements) {
				if (measurement instanceof DirectoryMeasurement) {
					continue;
				}

				if (patchFileRemoveSet.contains(measurement.getPath())) {
					filesInImageResponse.patchXml
							.add("<remove sel='//*[local-name()=\"Whitelist\"]/*[local-name()=\"File\"][@Path=\""
									+ measurement.getPath() + "\"]'></remove>");
				}
			}
		}
		if (searchFilesInImageRequest.reset_regex) {
			filesInImageResponse.patchXml
					.add("<remove sel='//*[local-name()=\"Whitelist\"]/*[local-name()=\"Dir\"][@Path=\""
							+ searchFilesInImageRequest.getDir()
							+ "\"]'></remove>");
		}

		if (includeDir) {
			String dirPath = null;
			for (String patchFile : patchDirAddSet) {
				boolean found = false;
				if (measurements != null) {
					for (Measurement measurement : measurements) {
						if (measurement instanceof DirectoryMeasurement) {
							if (measurement.getPath().equals(patchFile)) {
								found = true;
								dirPath = measurement.getPath();
								break;
							}
						}
					}
				}

				String recursiveAttr = " Recursive=\"false\"";
				if (searchFilesInImageRequest.include_recursive) {
					recursiveAttr = " Recursive=\"true\"";
				}
				if (found) {
					filesInImageResponse.patchXml
							.add("<remove sel='//*[local-name()=\"Whitelist\"]/*[local-name()=\"Dir\"][@Path=\""
									+ dirPath + "\"]'></remove>");
				}
				filesInImageResponse.patchXml
						.add("<add sel='//*[local-name()=\"Whitelist\"]'><Dir Path=\""
								+ patchFile
								+ "\" Include=\""
								+ searchFilesInImageRequest.include
								+ "\" Exclude=\""
								+ searchFilesInImageRequest.exclude
								+ "\""
								+ recursiveAttr + "/></add>");

			}
		}

		for (String patchFile : patchFileAddSet) {
			boolean found = false;
			if (measurements != null) {
				for (Measurement measurement : measurements) {
					if (measurement instanceof FileMeasurement) {
						if (measurement.getPath().equals(patchFile)) {
							found = true;
							break;
						}
					}
				}
			}

			if (!found || includeDir) {
				String dirPath = "";
				String pos = "";
				if (includeDir) {
					dirPath = "/*[local-name()=\"Dir\"][@Path=\""
							+ searchFilesInImageRequest.getDir() + "\"]";
					pos = "pos=\"after\"";
				} else {
					pos = "pos=\"prepend\"";
				}

				filesInImageResponse.patchXml.add("<add " + pos
						+ " sel='//*[local-name()=\"Whitelist\"]" + dirPath
						+ "'><File Path=\"" + patchFile + "\"/></add>");
			}
		}
	}

	public ImageStoreResponse uploadImageToImageStore(
			ImageStoreUploadRequest imageStoreUploadRequest)
			throws DirectorException {
		try {

			log.debug("Inside  imageStoreUploadRequest::"
					+ imageStoreUploadRequest);
			// Updating Or Creating ImageAction
			updateOrCreateImageAction(imageStoreUploadRequest);

			if (imageStoreUploadRequest.display_name != null) {
				// Updating Name in case name is changed

				ImageInfo image_info = imagePersistenceManager
						.fetchImageById(imageStoreUploadRequest.getImage_id());
				if (image_info.getTrust_policy_draft_id() != null) {
					TrustPolicyDraft trustPolicyDraft = imagePersistenceManager
							.fetchPolicyDraftById(image_info
									.getTrust_policy_draft_id());
					trustPolicyDraft.setDisplay_name(imageStoreUploadRequest
							.getDisplay_name());
					trustPolicyDraft.setEdited_date(new Date());
					trustPolicyDraft.setEdited_by_user_id(ShiroUtil
							.subjectUsername());
					imagePersistenceManager.updatePolicyDraft(trustPolicyDraft);
				} else if (image_info.getTrust_policy_id() != null) {
					TrustPolicy trustPolicy = imagePersistenceManager
							.fetchPolicyById(image_info.getTrust_policy_id());
					trustPolicy.setDisplay_name(imageStoreUploadRequest
							.getDisplay_name());
					trustPolicy.setEdited_date(new Date());
					trustPolicy.setEdited_by_user_id(ShiroUtil
							.subjectUsername());
					imagePersistenceManager.updatePolicy(trustPolicy);
				}
			}

		} catch (Exception e) {
			log.error("", e);
			throw new DirectorException(e);
		}
		return new ImageStoreResponse();
	}

	public ImageStoreManager getImageStoreImpl(String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		ImageStoreManager imgManager;
		Class c;

		c = Class.forName(className);

		imgManager = (ImageStoreManager) c.newInstance();

		return imgManager;

	}

	@Override
	public ImageListResponse getBareMetalLive(List<ImageInfo> images)
			throws DirectorException {

		List<ImageInfo> imageList = images;
		ImageListResponse imageListresponse = new ImageListResponse();

		imageListresponse.images = new ArrayList<ImageListResponseInfo>();
		for (ImageInfo imageInfo : imageList) {
			if (imageInfo.getImage_format() != null) {
				continue;
			}

			ImageListResponseInfo imgResponse = new ImageListResponseInfo();
			imgResponse.setImage_name(imageInfo.getName());

			String trust_policy = "<div id=\"trust_policy_column" + imageInfo.id + "\">";
			if (imageInfo.getTrust_policy_draft_id() == null
					&& imageInfo.getTrust_policy_id() == null) {

				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createPolicy2('"
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "')\"></span></a>";
			}

			if (imageInfo.getTrust_policy_draft_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicyForBMLive('"
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "')\"></span></a>";

			} else if (imageInfo.getTrust_policy_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicyForBMLive('"
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "')\"></span></a>";
			}

			if (imageInfo.getTrust_policy_id() != null) {
				trust_policy = trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicyAndManifest('"
						+ imageInfo.getId() + "','"
						+ imageInfo.getTrust_policy_id() + "')\"></span></a>";
			}

			if (imageInfo.getTrust_policy_id() != null
					|| imageInfo.getTrust_policy_draft_id() != null) {
				trust_policy = trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicy('"
						+ imageInfo.getId() + "','"
						+ imageInfo.getTrust_policy_id() + "','"
						+ imageInfo.getName() + "')\"></span></a>";
			}

			trust_policy = trust_policy + "</div>";
			imgResponse.setTrust_policy(trust_policy);

			String image_upload = "<div id='policy_name'" + imageInfo.id + ">";
			String display_name = getDisplayNameForImage(imageInfo.id);
			if ( display_name.equals(imageInfo.getName()) && imageInfo.getTrust_policy_draft_id() == null && imageInfo.getTrust_policy_id() == null ) {
				image_upload = image_upload + "NA";
			} else {
				image_upload = image_upload
						+ getDisplayNameForImage(imageInfo.id);
			}
			image_upload = image_upload + "</div>";

			imgResponse.setImage_upload(image_upload);

			imgResponse.setCreated_date(imageInfo.getCreated_date());

			imageListresponse.images.add(imgResponse);

		}

		return imageListresponse;
	}

	@Override
	public ImageListResponse getImagesForBareMetal(List<ImageInfo> images)
			throws DirectorException {

		List<ImageInfo> imageList = images;
		ImageListResponse imageListresponse = new ImageListResponse();

		imageListresponse.images = new ArrayList<ImageListResponseInfo>();
		for (ImageInfo imageInfo : imageList) {

			if (imageInfo.getImage_format() == null) {
				continue;
			}
			ImageListResponseInfo imgResponse = new ImageListResponseInfo();
			imgResponse.setImage_name(imageInfo.getName());
			imgResponse.setImage_format(imageInfo.getImage_format());

			String trust_policy = "<div id=\"trust_policy_column" + imageInfo.id +"\">";
			if (imageInfo.getTrust_policy_draft_id() == null
					&& imageInfo.getTrust_policy_id() == null) {

				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createPolicyForBMImage('"
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "')\"></span></a>";

			}

			if (imageInfo.getTrust_policy_draft_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicyForBMImage('"
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "')\"></span></a>";

			} else if (imageInfo.getTrust_policy_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicyForBMImage('"
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "')\"></span></a>";
			}

			if (imageInfo.getTrust_policy_id() != null) {
				trust_policy = trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicy('"
						+ imageInfo.getId() + "','"
						+ imageInfo.getTrust_policy_id() + "')\"></span></a>";
			}

			if (imageInfo.getTrust_policy_id() != null
					|| imageInfo.getTrust_policy_draft_id() != null) {
				trust_policy = trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicy('"
						+ imageInfo.getId() + "','"
						+ imageInfo.getTrust_policy_id() + "','"
						+ imageInfo.getName() + "')\"></span></a>";
			}

			trust_policy = trust_policy + "</div>";
			imgResponse.setTrust_policy(trust_policy);

			String image_upload;
			if (imageInfo.getTrust_policy_id() != null) {
				image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-ok\" title=\"Policy exists\"></span></a>";
				image_upload += "&nbsp;"
						+ "<a href=\"#\" title=\"Download Modified Image\" ><span class=\"glyphicon glyphicon-save\" title=\"Download Modified Image\" onclick=\"downloadImage('"
						+ imageInfo.getId() + "')\" ></span></a>";
			} else {
				image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-minus\" title=\"Never Uploaded\"></span></a>";
			}

			imgResponse.setImage_upload(image_upload);

			imgResponse.setCreated_date(imageInfo.getCreated_date());

			imageListresponse.images.add(imgResponse);

		}

		return imageListresponse;
	}

	@Override
	public ImageListResponse getImagesForVM(List<ImageInfo> images)
			throws DirectorException {

		List<ImageInfo> imageList = images;
		ImageListResponse imageListresponse = new ImageListResponse();

		imageListresponse.images = new ArrayList<ImageListResponseInfo>();
		for (ImageInfo imageInfo : imageList) {
			ImageListResponseInfo imgResponse = new ImageListResponseInfo();
			imgResponse.setImage_name(imageInfo.getName());
			imgResponse.setImage_format(imageInfo.getImage_format());

			String trust_policy = "<div id=\"trust_policy_vm_column" + imageInfo.id +"\">";
			if (imageInfo.getTrust_policy_draft_id() == null
					&& imageInfo.getTrust_policy_id() == null) {

				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createPolicy('"
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "')\"></span></a>";

			}

			if (imageInfo.getTrust_policy_draft_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicy('"
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "')\"></span></a>";

			} else if (imageInfo.getTrust_policy_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicy('"
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "')\"></span></a>";
			}

			if (imageInfo.getTrust_policy_id() != null) {
				trust_policy = trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicy('"
						+ imageInfo.getId() + "','"
						+ imageInfo.getTrust_policy_id() + "')\"></span></a>";
			}

			if (imageInfo.getTrust_policy_id() != null
					|| imageInfo.getTrust_policy_draft_id() != null) {
				trust_policy = trust_policy
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicyVM('"
						+ imageInfo.getId() + "','"
						+ imageInfo.getTrust_policy_id() + "','"
						+ imageInfo.getName() + "')\"></span></a>";
			}

			trust_policy = trust_policy + "</div>";
			imgResponse.setTrust_policy(trust_policy);


			String image_upload;
			if (imageInfo.getUploads_count() != 0) {
				image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-ok\" title=\"Uploaded Before\"></span></a>";
			} else {
				image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-minus\" title=\"Never Uploaded\"></span></a>";
			}

			image_upload += "&nbsp;"
					+ "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Upload\" onclick=\"uploadToImageStorePage('"
					+ imageInfo.getId() + "','" + imageInfo.getName() + "','"
					+ imageInfo.getTrust_policy_id() + "')\" ></span></a>";

			imgResponse.setImage_upload(image_upload);
			
			String display_name;

			display_name = getDisplayNameForImage(imageInfo.getId());

			imgResponse.setDisplay_name(display_name);

			imgResponse.setCreated_date(imageInfo.getCreated_date());

			imageListresponse.images.add(imgResponse);

		}

		return imageListresponse;
	}

	@Override
	public TrustPolicyDraft createPolicyDraftFromPolicy(String imageId,
			String image_action_id) throws DirectorException {

		ImageInfo imageInfo;
		try {
			imageInfo = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException e) {
			log.error("Cannot fetch imageid imageId::" + imageId, e);
			throw new DirectorException("Cannot fetch image by id", e);
		}
		TrustPolicy existingTrustPolicy = null;
		try {
			existingTrustPolicy = imagePersistenceManager
					.fetchPolicyById(imageInfo.getTrust_policy_id());
		} catch (DbException e) {
			log.error("Cannot get policy by id existingTrustPolicyId::"
					+ existingTrustPolicy, e);
			throw new DirectorException("Cannot get policy by id", e);
		}

		TrustPolicyDraft trustPolicyDraft = trustPolicyToTrustPolicyDraft(existingTrustPolicy);
		TrustPolicyDraft savePolicyDraft = null;
		try {
			trustPolicyDraft.setEdited_date(new Date());
			trustPolicyDraft.setEdited_by_user_id(ShiroUtil.subjectUsername());
			savePolicyDraft = imagePersistenceManager
					.savePolicyDraft(trustPolicyDraft);
		} catch (DbException e) {
			log.error("Unable to save policy draft", e);
			throw new DirectorException("Unable to save policy draft ", e);
		}

		try {
			imagePersistenceManager.destroyPolicy(existingTrustPolicy);
		} catch (DbException e) {
			log.error("Cannoot delete policy", e);
			throw new DirectorException("Cannot delete policy draft y", e);
		}
		if (image_action_id != null && !(image_action_id.length() == 0)) {
			try {
				imagePersistenceManager.deleteImageActionById(image_action_id);
			} catch (DbException e) {
				log.error("Cannot delete image action", e);
				throw new DirectorException("Cannot delete image action : "
						+ image_action_id, e);
			}
		}

		return savePolicyDraft;
	}

	public TrustPolicyDraft trustPolicyToTrustPolicyDraft(
			TrustPolicy existingTrustPolicy) {
		TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
		trustPolicyDraft.setCreated_by_user_id(existingTrustPolicy
				.getCreated_by_user_id());
		trustPolicyDraft.setCreated_date(existingTrustPolicy.getCreated_date());
		trustPolicyDraft.setEdited_by_user_id(existingTrustPolicy
				.getEdited_by_user_id());
		trustPolicyDraft.setEdited_date(existingTrustPolicy.getEdited_date());
		trustPolicyDraft.setName(existingTrustPolicy.getName());
		trustPolicyDraft.setImgAttributes(existingTrustPolicy
				.getImgAttributes());
		trustPolicyDraft.setTrust_policy_draft(existingTrustPolicy
				.getTrust_policy());
		trustPolicyDraft.setDisplay_name(existingTrustPolicy.getDisplay_name());
		return trustPolicyDraft;
	}

	private void updateOrCreateImageAction(
			ImageStoreUploadRequest imageStoreUploadRequest)
			throws DirectorException {
		try {
			ImageActionObject imageActionObject;
			ImageActionActions imageActionActions;
			log.debug("updateOrCreateImageAction imageStoreUploadRequest::"
					+ imageStoreUploadRequest);
			if (imageStoreUploadRequest.isCheck_image_action_id()
					&& imageStoreUploadRequest.getImage_action_id().length() != 0
					&& imageStoreUploadRequest.getImage_action_id() != null) {

				imageActionObject = imagePersistenceManager
						.fetchImageActionById(imageStoreUploadRequest
								.getImage_action_id());

			} else {

				TrustPolicy policy = imagePersistenceManager
						.fetchPolicyForImage(imageStoreUploadRequest
								.getImage_id());
				String id = createImageActionById(
						imageStoreUploadRequest.getImage_id(), policy, false);
				imageActionObject = imagePersistenceManager
						.fetchImageActionById(id);
			}

			List<ImageActionActions> taskList = imageActionObject.getAction();
			if (taskList == null) {
				taskList = new ArrayList<ImageActionActions>();
			}

			// User has selected as tarball
			if (imageStoreUploadRequest.getStore_name_for_tarball_upload() != null
					&& !imageStoreUploadRequest
							.getStore_name_for_tarball_upload().equals("0")) {
				imageActionActions = new ImageActionActions();
				imageActionActions.setTask_name(Constants.TASK_NAME_CREATE_TAR);
				imageActionActions.setStatus(Constants.INCOMPLETE);
				taskList.add(imageActionActions);
				imageActionActions = new ImageActionActions();
				imageActionActions.setTask_name(Constants.TASK_NAME_UPLOAD_TAR);
				imageActionActions.setStatus(Constants.INCOMPLETE);
				imageActionActions.setStorename(imageStoreUploadRequest
						.getStore_name_for_tarball_upload());
				taskList.add(imageActionActions);
				imageActionObject.setAction_count(imageActionObject
						.getAction_count() + 2);
			} else {
				// User has selected image store
				if (imageStoreUploadRequest.getStore_name_for_image_upload() != null
						&& !imageStoreUploadRequest
								.getStore_name_for_image_upload().equals("0")) {
					imageActionActions = new ImageActionActions();
					imageActionActions
							.setTask_name(Constants.TASK_NAME_UPLOAD_IMAGE);
					imageActionActions.setStatus(Constants.INCOMPLETE);
					imageActionActions.setStorename(imageStoreUploadRequest
							.getStore_name_for_image_upload());
					taskList.add(imageActionActions);
					imageActionObject.setAction_count(imageActionObject
							.getAction_count() + 1);
				}

				// User has selected policy store
				if (imageStoreUploadRequest.getStore_name_for_policy_upload() != null
						&& !imageStoreUploadRequest
								.getStore_name_for_policy_upload().equals("0")) {
					imageActionActions = new ImageActionActions();
					imageActionActions
							.setTask_name(Constants.TASK_NAME_UPLOAD_POLICY);
					imageActionActions.setStatus(Constants.INCOMPLETE);
					imageActionActions.setStorename(imageStoreUploadRequest
							.getStore_name_for_policy_upload());
					taskList.add(imageActionActions);
					imageActionObject.setAction_count(imageActionObject
							.getAction_count() + 1);
				}

			}

			if (taskList.size() > 0) {
				imageActionObject.setImage_id(imageStoreUploadRequest
						.getImage_id());
				imageActionObject.setAction(taskList);
				if (imageActionObject.getCurrent_task_name() == null) {
					ImageActionActions acts = taskList.get(0);
					imageActionObject.setCurrent_task_name(acts.getTask_name());
					imageActionObject.setCurrent_task_status(acts.getStatus());
				}

			}
			imagePersistenceManager.updateImageAction(
					imageActionObject.getId(), imageActionObject);
		} catch (Exception e) {
			throw new DirectorException("updateOrCreateImageAction fail", e);
		}

	}

	private Collection<File> getFirstLevelFiles(
			SearchFilesInImageRequest searchFilesInImageRequest)
			throws DirectorException {
		return getFirstLevelFiles(searchFilesInImageRequest.getDir(),
				searchFilesInImageRequest.id);

	}

	private Collection<File> getFirstLevelFiles(String dir, String imageId)
			throws DirectorException {
		Collection<File> files = null;
		if (!dir.endsWith(File.separator)) {
			dir += File.separator;
		}
		DirectoryAndFileUtil directoryAndFileUtil = new DirectoryAndFileUtil();
		DirectoryMeasurement dirMeasurement = new DirectoryMeasurement();
		dirMeasurement.setPath(dir);
		dirMeasurement.setRecursive(false);
		try {
			String filesAndDirectories = directoryAndFileUtil
					.getFilesAndDirectories(imageId, dirMeasurement);
			files = getFilesFromFileString(dir, filesAndDirectories);
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error in getting files for browser ", e);
		}
		return files;
	}

	private List<File> getFilesAndDirectories(String sDir, Set<String> dirs,
			String imageId) throws DirectorException {
		if (files == null) {
			files = new ArrayList<>();
		}
		if (dirs != null) {
			dirs.add(sDir);
		}
		Collection<File> faFiles = getFirstLevelFiles(sDir, imageId);// Arrays.asList(new
																		// File(sDir).listFiles());
		for (File file : faFiles) {
			files.add(file);
			
			if (new File(TdaasUtil.getMountPath(imageId)+file.getAbsolutePath()).isDirectory()) {
				getFilesAndDirectories(file.getAbsolutePath(), dirs, imageId);
			}
		}
		return files;
	}

	private Collection<File> getFilesAndDirectoriesWithFilter(
			SearchFilesInImageRequest searchFilesInImageRequest) {
		String mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);

		Collection<File> files = new HashSet<>();
		DirectoryAndFileUtil directoryAndFileUtil = new DirectoryAndFileUtil();
		DirectoryMeasurement dirMeasurement = new DirectoryMeasurement();
		dirMeasurement.setPath(searchFilesInImageRequest.getDir());
		dirMeasurement
				.setRecursive(searchFilesInImageRequest.include_recursive);
		dirMeasurement.setInclude(searchFilesInImageRequest.include);
		dirMeasurement.setExclude(searchFilesInImageRequest.exclude);
		try {
			String filesAndDirectories = directoryAndFileUtil
					.getFilesAndDirectories(searchFilesInImageRequest.id,
							dirMeasurement);
			files = getFilesFromFileString(mountPath
					+ searchFilesInImageRequest.getDir() + File.separator,
					filesAndDirectories);
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error in getting files for browser ", e);
		}
		return files;
	}

	private Collection<File> getFilesFromFileString(String baseDir,
			String fileStr) {
		String nl = System.getProperty("line.separator");
		Set<File> files = new HashSet<File>();
		String[] split = fileStr.split(nl);
		for (String string : split) {
			if (!string.isEmpty() && !string.equals(baseDir)) {
				files.add(new File(baseDir + string));
			}
		}
		return files;
	}

	@Override
	public String getDisplayNameForImage(String image_id)
			throws DirectorException {
		ImageInfo image_info;
		String policy_id;
		String display_name = new String();
		try {
			image_info = imagePersistenceManager.fetchImageById(image_id);
			if (image_info.getTrust_policy_draft_id() != null) {
				policy_id = image_info.getTrust_policy_draft_id();
				display_name = imagePersistenceManager.fetchPolicyDraftById(
						policy_id).getDisplay_name();
			} else if (image_info.getTrust_policy_id() != null) {
				policy_id = image_info.getTrust_policy_id();
				display_name = imagePersistenceManager.fetchPolicyById(
						policy_id).getDisplay_name();
			} else {
				display_name = image_info.name;
			}

		} catch (DbException e) {
			log.error("error in fetching image name", e);
			throw new DirectorException("Error in Fetching Image Name", e);
		}
		return display_name;
	}

	@Override
	public ImageListResponse getImages(List<ImageInfo> images,
			String deployment_type) throws DirectorException {
		if (deployment_type.equals(Constants.DEPLOYMENT_TYPE_VM)) {
			return getImagesForVM(images);
		} else if (deployment_type.equals(Constants.DEPLOYMENT_TYPE_BAREMETAL)) {
			return getImagesForBareMetal(images);
		} else {
			return getBareMetalLive(images);
		}
	}

	@Override
	public String getFilepathForImage(String imageId, boolean isModified)
			throws DbException {
		ImageInfo imageInfo = imagePersistenceManager.fetchImageById(imageId);
		if (isModified) {
			return imageInfo.getLocation() + "Modified_" + imageInfo.getName();
		} else {
			return imageInfo.getLocation() + imageInfo.getName();
		}

	}

	@Override
	public TrustPolicy getTrustPolicyByImageId(String imageId)
			throws DbException {
		String id = imagePersistenceManager.fetchImageById(imageId)
				.getTrust_policy_id();
		return imagePersistenceManager.fetchPolicyById(id);
	}

	@Override
	public CreateTrustPolicyMetaDataResponse importPolicyTemplate(String imageId)
			throws DirectorException {
		CreateTrustPolicyMetaDataResponse createTrustPolicyMetaDataResponse = null;
		ImageAttributes image = null;
		try {
			image = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException ex) {
			log.error("No image found durin import of policy", ex);
			throw new DirectorException("No image found with id: " + imageId,
					ex);
		}

		if (!(image.getImage_deployments().equals(
				Constants.DEPLOYMENT_TYPE_BAREMETAL) && image.getImage_format() == null)) {
			createTrustPolicyMetaDataResponse = new CreateTrustPolicyMetaDataResponse();
			createTrustPolicyMetaDataResponse.status = "Success";
			createTrustPolicyMetaDataResponse.details = "No need for import ";
			return createTrustPolicyMetaDataResponse;
		}

		// Get the draft
		TrustPolicyDraft policyDraftForImage = null;
		try {
			policyDraftForImage = imagePersistenceManager
					.fetchPolicyDraftForImage(imageId);
		} catch (DbException ex) {
			log.error("Error fetching trust policy in import policy", ex);
			throw new DirectorException(
					"Error fetching trust policy in import policy: " + imageId,
					ex);
		}

		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = null;
		try {
			policy = TdaasUtil.getPolicy(policyDraftForImage
					.getTrust_policy_draft());
		} catch (JAXBException e) {
			log.error("Error mapping trust policy string to object for image: "
					+ imageId);
			throw new DirectorException(
					"Error mapping trust policy string to object for image: "
							+ imageId, e);
		}

		// Check if mounted live BM has /opt/vrtm

		String idendifier = "NV";
		String dirPath = "/opt";
		Collection<File> firstLevelFiles = getFirstLevelFiles(dirPath, imageId);
		for (File f : firstLevelFiles) {
			if (f.getAbsolutePath().endsWith("/opt/vrtm")) {
				idendifier = "V";
			}
		}

		String content = null;
		Manifest manifest;
		List<PolicyTemplateInfo> fetchPolicyTemplateForDeploymentIdentifier;
		try {
			fetchPolicyTemplateForDeploymentIdentifier = imagePersistenceManager
					.fetchPolicyTemplateForDeploymentIdentifier(
							Constants.DEPLOYMENT_TYPE_BAREMETAL, idendifier);
			PolicyTemplateInfo policyTemplateInfo = fetchPolicyTemplateForDeploymentIdentifier
					.get(0);
			content = policyTemplateInfo.getContent();
		} catch (DbException e2) {
			log.error("Error converting manifest to object " + content, e2);
			throw new DirectorException("Error converting manifest to object ",
					e2);
		}

		try {
			manifest = TdaasUtil.convertStringToManifest(content);
		} catch (JAXBException e1) {
			log.error("Error converting manifest to object " + content, e1);
			throw new DirectorException("Error converting manifest to object ",
					e1);
		}

		Measurement measurement = null;
		Collection<Measurement> measurements = new ArrayList<>();
		for (MeasurementType measurementType : manifest.getManifest()) {
			if (measurementType instanceof DirectoryMeasurementType) {
				measurement = new DirectoryMeasurement();
			} else if (measurementType instanceof FileMeasurementType) {
				measurement = new FileMeasurement();
			}
			measurement.setPath(measurementType.getPath());
			measurements.add(measurement);
		}
		policy.getWhitelist().getMeasurements().addAll(measurements);
		String policyXmlWithImports = null;
		try {
			policyXmlWithImports = TdaasUtil.convertTrustPolicyToString(policy);
		} catch (JAXBException e) {
			log.error("Error converting imported policy into string");
			throw new DirectorException(
					"Error converting imported policy into string", e);
		}

		policyDraftForImage.setTrust_policy_draft(policyXmlWithImports);
		policyDraftForImage.setEdited_date(new Date());
		policyDraftForImage.setEdited_by_user_id(ShiroUtil.subjectUsername());
		policyDraftForImage.setCreated_date(new Date());
		policyDraftForImage.setCreated_by_user_id(ShiroUtil.subjectUsername());

		try {
			imagePersistenceManager.savePolicyDraft(policyDraftForImage);
		} catch (DbException e) {
			log.error("Error saving policy draft for image after adding imports for IMAGE:  "
					+ imageId);
			throw new DirectorException(
					"Error saving policy draft for image after adding imports for IMAGE:  "
							+ imageId, e);
		}
		createTrustPolicyMetaDataResponse = new CreateTrustPolicyMetaDataResponse();
		createTrustPolicyMetaDataResponse.setTrustPolicy(policyXmlWithImports);
		createTrustPolicyMetaDataResponse.setStatus("Success");
		return createTrustPolicyMetaDataResponse;
	}

	private void createlistOfFilesForRegexReset(
			SearchFilesInImageRequest searchFilesInImageRequest,
			Set<String> patchFilesRemoveSet) throws DirectorException {
		TrustPolicyDraft trustPolicyDraft = null;
		try {
			trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(searchFilesInImageRequest.id);
		} catch (DbException dbe) {
			log.error("Error fetching policy draft", dbe);
			throw new DirectorException(
					"Error fetching policy draft for image: "
							+ searchFilesInImageRequest.id, dbe);
		}

		boolean start = false;
		try {
			com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil
					.getPolicy(trustPolicyDraft.getTrust_policy_draft());
			List<Measurement> measurements = policy.getWhitelist()
					.getMeasurements();
			for (Measurement measurement : measurements) {
				if (measurement instanceof DirectoryMeasurement) {
					if (measurement.getPath().equals(
							searchFilesInImageRequest.getDir())) {
						start = true;
					} else {
						start = false;
					}

				}

				if (measurement instanceof FileMeasurement) {
					if (start) {
						patchFilesRemoveSet.add(measurement.getPath());
					}
				}
			}
		} catch (JAXBException je) {

		}
	}

	@Override
	public void deleteTrustPolicy(String imageId) throws DirectorException {
		try {
			ImageInfo image = imagePersistenceManager.fetchImageById(imageId);
			if (image.getTrust_policy_draft_id() != null) {
				TrustPolicyDraft policyDraft = imagePersistenceManager
						.fetchPolicyDraftById(image.getTrust_policy_draft_id());
				if (policyDraft != null) {
					imagePersistenceManager.destroyPolicyDraft(policyDraft);
				}
			}
			if (image.getTrust_policy_id() != null) {
				TrustPolicy policy = imagePersistenceManager
						.fetchPolicyById(image.getTrust_policy_id());
				if (policy != null) {
					imagePersistenceManager.destroyPolicy(policy);
				}
			}
		} catch (DbException e) {
			log.error("Error in Deleting TrustPolicy or TrustPolicyDraft", e);
			throw new DirectorException(
					"Error in Deleting TrustPolicy or TrustPolicyDraft", e);
		}

	}

	@Override
	public File createTarballOfPolicyAndManifest(String imageId) {

		TrustPolicy policyForImage;
		try {
			policyForImage = imagePersistenceManager
					.fetchPolicyForImage(imageId);
		} catch (DbException e) {
			log.error("Unable to fetch policy for image : " + imageId, e);
			return null;
		}
		String artifactsPath = Constants.TARBALL_PATH + imageId;
		File tarballDirForImage = new File(artifactsPath);
		if (!tarballDirForImage.exists()) {
			if (!tarballDirForImage.mkdir()) {
				return null;
			}
		}
		FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();

		String trustPolicyFilePath = artifactsPath + File.separator
				+ "trustpolicy.xml";
		if (!fileUtilityOperation.createNewFile(trustPolicyFilePath)) {
			return null;
		}
		String manifestFilePath = artifactsPath + File.separator
				+ "manifest.xml";
		if (!fileUtilityOperation.createNewFile(manifestFilePath)) {
			return null;
		}
		String manifest = null;
		try {
			manifest = TdaasUtil.getManifestForPolicy(policyForImage
					.getTrust_policy());
		} catch (JAXBException e) {
			log.error(
					"Cannot create manifest for policy for image: " + imageId,
					e);
			return null;
		}

		// Write policy
		fileUtilityOperation.writeToFile(trustPolicyFilePath,
				policyForImage.getTrust_policy());
		// Write manifest
		fileUtilityOperation.writeToFile(manifestFilePath, manifest);
		String tarName = StringUtils.isNotEmpty(policyForImage
				.getDisplay_name()) ? policyForImage.getDisplay_name()
				+ ".tar.gz" : "policyandmanifest.tar.gz";
		try {

			DirectorUtil.createTar(artifactsPath, "trustpolicy.xml",
					"manifest.xml", artifactsPath + File.separator , tarName);
		} catch (IOException e) {
			log.error("Error while creating TAR for policy and manifest", e);
			return null;
		}

		File tarFile = new File(artifactsPath + File.separator + tarName);
		if (tarFile.exists()) {
			return tarFile;
		} else {
			return null;
		}
	}

}
