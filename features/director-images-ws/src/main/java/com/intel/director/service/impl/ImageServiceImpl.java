package com.intel.director.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.springframework.stereotype.Component;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageListResponse;
import com.intel.director.api.ImageListResponseInfo;
import com.intel.director.api.ImportPolicyTemplateResponse;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.PolicyTemplateInfo;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.SshPassword;
import com.intel.director.api.SshSettingInfo;
import com.intel.director.api.SshSettingRequest;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.TrustPolicyResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.api.UpdateTrustPolicyRequest;
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
import com.jcraft.jsch.JSchException;

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

	public ImageServiceImpl() {
		imagePersistenceManager = new DbServiceImpl();
	}

	
	@Override
	public MountImageResponse mountImage(String imageId, String user)
			throws DirectorException {
		int exitcode = 0;
		user = ShiroUtil.subjectUsername();
		log.info("inside mounting image in service");
		log.info("***** Logged in user : " + ShiroUtil.subjectUsername());
		ImageAttributes image;
		try {
			image = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException ex) {
			log.error("Error in mounting image  ", ex);
			throw new DirectorException("No image found with id: " + imageId,
					ex);
		}
		
		if(image.deleted){
			log.error("Cannot launch an image marked as deleted");
			throw new DirectorException("Cannot launch deleted image: " + imageId);
		}

		MountImageResponse mountImageResponse = TdaasUtil
				.mapImageAttributesToMountImageResponse(image);

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

		if (image.mounted_by_user_id != null
				&& image.mounted_by_user_id.equals(ShiroUtil.subjectUsername())) {
			File f = new File(TdaasUtil.getMountPath(imageId));
			if (f.exists()) {
				log.info("Not mounting image again");
				return mountImageResponse;
			}
		}

		// // log.info("Mounting image from location: " + image.location);

		String mountPath = null;
		if (image.getImage_format() == null) {
			// BML flow
			mountPath = TdaasUtil.getMountPath(image.id);
		} else {
			mountPath = DirectorUtil.getMountPath(image.id);
		}
		log.info("mount path is " + mountPath);
		String mounteImageName = image.getImage_name();
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
							+ image.getImage_name();
					log.info("Imagepath : " + modifiedImagePath);
					DirectorUtil
							.createCopy(
									image.getLocation() + image.getImage_name(),
									image.getLocation() + "Modified_"
											+ image.getImage_name());
					exitcode = MountImage.unmountImage(mountPath);
					if(exitcode != 0){
						log.error("Unable to mount image");
						throw new DirectorException("Unable to mount image");
					}
					log.info("Unmounting");
					exitcode = MountImage.mountImage(image.getLocation() + "Modified_"
							+ image.getImage_name(), mountPath);
					if(exitcode != 0){
						log.error("Unable to mount image");
						throw new DirectorException("Unable to mount image");
					}
					log.info("Mount BM image complete");
				} else {
					log.info("VM IMage mount");
					exitcode = MountImage.mountImage(
							image.getLocation() + mounteImageName, mountPath);
					if(exitcode != 0){
						log.error("Unable to mount image");
						throw new DirectorException("Unable to mount image");
					}
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
					exitcode = MountImage.unmountImage(mountPath);
					if(exitcode != 0){
						log.error("Unable to unmount image");
						throw new DirectorException("Unable to unmount image");
					}
					
				} else {

					exitcode = MountImage.unmountRemoteSystem(mountPath);
					if(exitcode != 0){
						log.error("Unable to mount image");
						throw new DirectorException("Unable to mount image");
					}
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
		UnmountImageResponse unmountImageResponse = null;
		ImageAttributes image = null;
		try {
			image = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException e) {
			log.error("Error fetchign image "+imageId, e);
			throw new DirectorException("Error fetching image:"+imageId);
		}
		log.info("Unmounting image : " + image.id + " with name : "
				+ image.image_name);
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
		try {
			imagePersistenceManager.updateImage(image);
		} catch (DbException e) {
			log.error("Error updating image "+imageId, e);
			throw new DirectorException("Error updating image:"+imageId);
		}
		
		int exitCode = 0;

		if (image.getImage_format() != null) {
			exitCode = MountImage.unmountImage(mountPath);
			if(exitCode != 0){
				log.error("Unable to mount image");
				throw new DirectorException("Unable to mount image");
			}
			log.info("*** unmount BM/VM complete");
		} else {
			exitCode = MountImage.unmountRemoteSystem(mountPath);
			if(exitCode != 0){
				log.error("Unable to mount image");
				throw new DirectorException("Unable to mount image");
			}
			log.info("*** unmount of BM LIVE complete");
		}
		unmountImageResponse = TdaasUtil
				.mapImageAttributesToUnMountImageResponse(image);

		unmountImageResponse.setStatus(Constants.SUCCESS);
		if(exitCode != 0){
			unmountImageResponse.setStatus(Constants.ERROR);
			unmountImageResponse.setDetails("Unmount script executed with errors");
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
			SearchImagesRequest searchImagesRequest) throws DirectorException {
		SearchImagesResponse searchImagesResponse = new SearchImagesResponse();
		try {
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
			if(fetchImages != null ){
				for(int i=0; i<fetchImages.size();i++){
						fetchImages.get(i).setPolicy_name(getDisplayNameForImage(fetchImages.get(i).id));
				}
			}
			searchImagesResponse.images = fetchImages;
		} catch (DbException de) {
			log.error("Error while retrieving list of images", de);
			throw new DirectorException(
					"Error while retrieving list of images", de);
		}
		return searchImagesResponse;
	}

	/**
	 * Method for creating new image metadata for the image being uploaded and
	 * store it in the DB. It will require image name, image size, image format,
	 * image deployment type as parameter. New image upload metadata will be
	 * created for this parameters along with the authenticated user information
	 * 
	 * @param image_deployments
	 * @param image_format
	 * @param fileName
	 * @param fileSize
	 * @return TrustDirectorImageUploadResponse object newly created image
	 *         metadata
	 * @throws DirectorException
	 */
	@Override
	public TrustDirectorImageUploadResponse createUploadImageMetadataImpl(
			String image_deployments, String image_format, String fileName,
			int fileSize) throws DirectorException {

		String loggedinUser = ShiroUtil.subjectUsername();
		
		ImageAttributes imageAttributes = new ImageAttributes();
		imageAttributes.image_name = fileName;
		imageAttributes.image_deployments = image_deployments;
		imageAttributes.setCreated_by_user_id(loggedinUser);
		imageAttributes.setEdited_by_user_id(loggedinUser);
		imageAttributes.setStatus(Constants.INCOMPLETE);
		imageAttributes.setDeleted(true);
		int sizen_kb = fileSize;
		imageAttributes.setSent(0);
		imageAttributes.setImage_size(sizen_kb);
		imageAttributes.location = Constants.defaultUploadPath;
		imageAttributes.image_format = image_format;
		Date currentDate = new Date();
		imageAttributes.setCreated_date(currentDate);
		imageAttributes.setEdited_date(currentDate);
		imageAttributes.setEdited_by_user_id(ShiroUtil.subjectUsername());
		imageAttributes.setCreated_by_user_id(ShiroUtil.subjectUsername());

		ImageAttributes createdImageMetadata;
		try {
			log.debug("Saving metadata of uploaded file");
			createdImageMetadata = imagePersistenceManager
					.saveImageMetadata(imageAttributes);
			log.info("Image is hidden :: " + createdImageMetadata.isDeleted());
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
	 * Method for uploading image data sent in chunks for the given image_id.
	 * Image name and image save location retrieved from DB using the given
	 * image id and given chunk is saved to that location
	 * 
	 * @param image_id
	 * @param fileInputStream
	 *            - image data sent in chunks
	 * @return TrustDirectorImageUploadResponse object updated image upload
	 *         metadata
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
					+ " and imageName " + imageInfo.getImage_name());
			if (imageInfo.getStatus().equals(Constants.COMPLETE)) {
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
			int read;
			byte[] bytes = new byte[1024];

			OutputStream out = new FileOutputStream(new File(
					imageInfo.getLocation() + imageInfo.getImage_name()), true);
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
			imageInfo.setDeleted(false);
			
			log.info("Image upload COMPLETE..");
		}
		try {
			imagePersistenceManager.updateImage(imageInfo);
			log.info("Image is hidden :: " +  imageInfo.isDeleted());
		} catch (DbException e) {
			log.error("Error while updating metadata for uploaded image : "
					+ e.getMessage());
			throw new DirectorException("Cannot update image meta data", e);
		}
		try {	
			fileInputStream.close();
		} catch (IOException e) {
			log.error("Error in closing stream: ", e);
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
		

		mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);
		log.info("Browsing files for on image mounted at : " + mountPath);

		SearchFilesInImageResponse filesInImageResponse = new SearchFilesInImageResponse();
		Collection<File> listFilesAndDirs = getFirstLevelFiles(searchFilesInImageRequest);
		log.info("Get first level files");

		List<Measurement> measurements = getMeasurements(searchFilesInImageRequest.id);
		log.info("Get existing measurements");
		// this map contains the directory paths as key. the value - boolean
		// indicates whether all
		// the sub files of these directory need to be fetched.
		// false - only first level files
		Map<String, Boolean> directoryListContainingPolicyFiles = new HashMap<>();
		Set<TreeNodeDetail> directoryListContainingRegex = new HashSet<TreeNodeDetail>();
		init(trustPolicyElementsList, directoryListContainingPolicyFiles,
				directoryListContainingRegex, searchFilesInImageRequest);
		log.info("after init");

		// Fetch the files
		if (searchFilesInImageRequest.recursive) {
			treeFiles = getFilesAndDirectories(
					searchFilesInImageRequest.getDir(), null,
					searchFilesInImageRequest.id);
			log.info("calc treefiles in recursive");
		} else {
			if (listFilesAndDirs != null) {
				treeFiles = listFilesAndDirs;
				log.info("calc treefiles w/o recursive");
			}
		}

		// In case of edit mode we want to show the exploded view
		// so that the selected file is visible in the initial screen.
		// For that we use the directoryListContainingPolicyFiles to find the
		// directories
		// that contain that file/s

		// populate filenames with the leaf nodes of the root folder

		createListOfFileNamesForTree(searchFilesInImageRequest, treeFiles,
				fileNames, directoryListContainingPolicyFiles.keySet());
		log.info("create list of file name for tree");

		// Now add the exploded file view in case of edit
		Set<String> dirsForEdit = new HashSet<String>();

		for (String dirPath : directoryListContainingPolicyFiles.keySet()) {
			log.info("calc treefiles for dir : "+dirPath);
			
			if (directoryListContainingPolicyFiles.get(dirPath)) {
				List<File> filesAndDirectories = getFilesAndDirectoriesForEdit(
						dirPath, dirsForEdit, searchFilesInImageRequest.id);
				createListOfFileNamesForTree(searchFilesInImageRequest,
						filesAndDirectories, fileNames,
						directoryListContainingPolicyFiles.keySet());
				log.info("calc treefiles in recursive edit mode");
			} else {

				Collection<File> firstLevelFiles = getFirstLevelFiles(dirPath,
						searchFilesInImageRequest.id);
				if (firstLevelFiles != null) {
					createListOfFileNamesForTree(searchFilesInImageRequest,
							firstLevelFiles, fileNames);
				}
				log.info("calc treefiles in recursive edit mode else");
			}
		}

		String parent = searchFilesInImageRequest.getDir();
		TreeNode root = new TreeNode(parent, parent);
		log.info("Create root node");
		Tree tree = new Tree(root, searchFilesInImageRequest.recursive,
				searchFilesInImageRequest.files_for_policy);
		log.info("Create tree");
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
			TreeNodeDetail detail = new TreeNodeDetail();
			detail.regexPath = searchFilesInImageRequest.getDir();
			detail.regexExclude = searchFilesInImageRequest.exclude;
			detail.regexInclude = searchFilesInImageRequest.include;
			detail.isRegexRecursive = searchFilesInImageRequest.include_recursive;

			root.rootDirWithRegex = detail;
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
		log.info("set seleceted files for tree");
		tree.setDirPathsForEdit(dirsForEdit);
		log.info("set dirs for edit");
		for (String data : fileNames) {
			tree.addElement(data);
		}

		tree.printTree();
		log.info("print tree");

		// Create patch to be sent in case of directory selection or regex
		buildPatch(patchDirAddSet, patchFileAddSet, patchFileRemoveSet,
				measurements, searchFilesInImageRequest, filesInImageResponse);
		log.info("build patch");
		filesInImageResponse.files = tree.treeElementsHtml;
		log.info("return");
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
	public TrustPolicyDraft editTrustPolicyDraft(
			TrustPolicyDraftEditRequest trustpolicyDraftEditRequest) throws DirectorException {
		TrustPolicyDraft trustPolicyDraft = null;
		try {
			trustPolicyDraft = imagePersistenceManager.fetchPolicyDraftById(trustpolicyDraftEditRequest.trust_policy_draft_id);
				
		} catch (DbException e) {
			// TODO Handle Error
			log.error("Error in editTrustPolicyDraft()", e);
		}

		

		String draft = trustPolicyDraft.getTrust_policy_draft();

		draft = TdaasUtil.patch(draft, trustpolicyDraftEditRequest.patch);
		Date currentDate = new Date();
		trustPolicyDraft.setEdited_date(currentDate);
		trustPolicyDraft.setEdited_by_user_id(ShiroUtil.subjectUsername());
		trustPolicyDraft.setTrust_policy_draft(draft);
		trustPolicyDraft.setEdited_by_user_id(ShiroUtil.subjectUsername());

		try {
			imagePersistenceManager.updatePolicyDraft(trustPolicyDraft);
		} catch (DbException e) {
			// TODO Handle Error
			log.error("Error in editTrustPolicyDraft()");
			throw new DirectorException("Error in editTrustPolicyDraft ", e);
		}
		return trustPolicyDraft;
	}

	public CreateTrustPolicyMetaDataResponse getPolicyMetadata(String draftid)
			throws DirectorException {
		CreateTrustPolicyMetaDataResponse metadata = new CreateTrustPolicyMetaDataResponse();
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
					.getImage_name());
			// /metadata.setIsEncrypted(isEncrypted);
			metadata.setLaunch_control_policy(policy.getLaunchControlPolicy()
					.value());
			// /metadata.setSelected_image_format(selected_image_format);
		} catch (DbException e) {
			String errorMsg = "Unable to get policy metadata draftid::" + draftid; 
			log.error(errorMsg, e);
			throw new DirectorException(errorMsg, e);
		} catch (JAXBException e) {
			String errorMsg = "Unable to get policy metadata draftid::" + draftid;
			log.error("Unable to get policy metadata draftid::" + draftid, e);
			throw new DirectorException(e);
		}
		return metadata;
	}

	public CreateTrustPolicyMetaDataResponse getPolicyMetadataForImage(
			String image_id) throws DirectorException {

		CreateTrustPolicyMetaDataResponse metadata = new CreateTrustPolicyMetaDataResponse();
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
				metadata.setImage_name(trustPolicy.getImgAttributes().getImage_name());
				metadata.setDisplay_name(trustPolicy.getDisplay_name());

			} else {
				policyXml = trustPolicyDraft.getTrust_policy_draft();
				metadata.setImage_name(trustPolicyDraft.getImgAttributes()
						.getImage_name());
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

	public String createTrustPolicy(String draft_id) throws DirectorException {

		ImageAttributes image;

		TrustPolicyDraft existingDraft = null;
		
		try {
			existingDraft = imagePersistenceManager.fetchPolicyDraftById(draft_id);
		} catch (DbException e1) {
			log.error("Unable to fetch draft for draft id::", draft_id);
			throw new DirectorException("Unable to fetch draft for drfat id:: "
					+ draft_id, e1);
		}

		Date currentDate = new Date();
		String image_id=existingDraft.getImgAttributes().getId();
		TrustPolicy trustPolicy = new TrustPolicy();
		String policyXml = existingDraft.getTrust_policy_draft();
		String display_name = existingDraft.getDisplay_name();
		trustPolicy.setDisplay_name(display_name);
		ImageAttributes imgAttrs = new ImageAttributes();
		imgAttrs.setId(image_id);
		trustPolicy.setImgAttributes(imgAttrs);
		log.info("Going to save trust policy for image_id::" + image_id);
		try {
			image = imagePersistenceManager.fetchImageById(image_id);
		} catch (DbException ex) {
			log.error("Error in mounting image  ", ex);
			throw new DirectorException("No image found with id: " + image_id,
					ex);
		}
		log.info("After saving trust policy for image_id::" + image_id);

		// Get the hash
		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = null;
		try {
			log.info("Going to convert trust policy into string");

			policy = TdaasUtil.getPolicy(policyXml);
			log.info("After convert trust policy into string");
		} catch (JAXBException e2) {
			log.error("Unable to convert string into policy object : ",
					policyXml);
			throw new DirectorException(
					"Unable to convert policy string into object ", e2);
		}
		try {
			log.info("Before calculating hashes");
			new CreateTrustPolicy(image_id).createTrustPolicy(policy);
			log.info("After calculating hashes");
		} catch (CryptographyException | IOException e1) {
			log.error("Unable to create trust policy- create hashes");
			throw new DirectorException(
					"Unable to create policy - create hashes", e1);
		}
		log.info("Got the hashes for the selected files ::" + image_id);
		// Calculate image hash and add to encryption tag
		if (policy != null && policy.getEncryption() != null) {
			if(!policy.getEncryption().getKey().getValue().contains("keys")){
				throw new DirectorException("Unable to fetch key from KMS");
			}
			if(!policy.getEncryption().getKey().getValue().contains("keys")){
				throw new DirectorException("Unable to fetch key from KMS");
			}
			File imgFile = new File(image.getLocation() + image.getImage_name());
			log.info("Calculating MD5 of file : " + image.getLocation()
					+ image.getImage_name());
			String computeHash = null;
			try {
				computeHash = TdaasUtil.computeHash(
						MessageDigest.getInstance("MD5"), imgFile);
			} catch (NoSuchAlgorithmException | IOException e) {
				log.error(
						"Unable to compute hash for image while creating policy",
						e);
				throw new DirectorException(
						"Unable to compute hash for image while creating policy",
						e);
			}
			policy.getEncryption().getChecksum().setValue(computeHash);

		}

		try {
			policyXml = TdaasUtil.convertTrustPolicyToString(policy);
		} catch (JAXBException e) {
			log.error("Unable to convert policy object to string", e);
			throw new DirectorException(
					"Unable to convert policy object to string", e);
		}
		log.info("Convert policy in string format");

		// Sign the policy with MtWilson
		Extensions
				.register(
						TlsPolicyCreator.class,
						com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);
		log.info("Register TlsPolicyCreator");

		Properties p = DirectorUtil
				.getPropertiesFile(Constants.MTWILSON_PROP_FILE);// My.configuration().getClientProperties();
		log.info("Get MTW prop file");

		TrustPolicySignature client = null;
		try {
			client = new TrustPolicySignature(p);
			log.info("MTW client init");

		} catch (Exception e) {
			log.error(
					"Unable to create client for signing the policy with MTW",
					e);
			throw new DirectorException(
					"Unable to create client for signing  policy with MTW", e);
		}
		String signedPolicyXml = null;
		try {
			signedPolicyXml = client.signTrustPolicy(policyXml);
		} catch (Exception e) {
			log.error("Unable to sign the policy with MTW", e);
			throw new DirectorException("Unable to sign the policy with MTW", e);
		}
		log.info("****** SIGN : " + signedPolicyXml);

		String trustPolicyName = null;
		String trustPolicyFile = null;
		String mountPath = TdaasUtil.getMountPath(image_id);
		FileUtilityOperation fileUtilityOperation = new FileUtilityOperation();

		if (Constants.DEPLOYMENT_TYPE_BAREMETAL.equals(image
				.getImage_deployments())) {
			// Writing inside bare metal modified image

			String localPathForPolicyAndManifest = "/tmp/" + image_id;
			trustPolicyName = "trustpolicy.xml";
			trustPolicyFile = localPathForPolicyAndManifest + File.separator
					+ trustPolicyName;

			String manifestFile = localPathForPolicyAndManifest
					+ File.separator + "manifest.xml";
			String manifest = null;
			try {
				manifest = TdaasUtil.getManifestForPolicy(policyXml);
			} catch (JAXBException e) {
				log.error("Unable to convert policy into manifest", e);
				throw new DirectorException(
						"Unable to convert policy into manifest", e);
			}
			File dirForPolicyAndManifest = new File(
					localPathForPolicyAndManifest);
			if (!dirForPolicyAndManifest.exists()) {
				dirForPolicyAndManifest.mkdir();
			}

			fileUtilityOperation.createNewFile(manifestFile);
			fileUtilityOperation.createNewFile(trustPolicyFile);

			fileUtilityOperation.writeToFile(trustPolicyFile, signedPolicyXml);
			fileUtilityOperation.writeToFile(manifestFile, manifest);

			// Push the policy and manifest to the remote host
			SshSettingInfo existingSsh = null;
			try {
				existingSsh = imagePersistenceManager
						.fetchSshByImageId(image_id);
			} catch (DbException e) {
				log.error("Unable to fetch SSH details for host", e);
				throw new DirectorException(
						"Unable to fetch SSH details for host", e);
			}
			String user = existingSsh.getUsername();
			String password = existingSsh.getPassword().getKey();
			String ip = existingSsh.getIpAddress();
			
			log.info("Connecting to remote host : "+ip +" with user "+user + " and paswd : "+password );

			SSHManager sshManager = new SSHManager(user, password, ip);
			try {
				List<String> files = new ArrayList<String>(2);
				files.add(manifestFile);
				files.add(trustPolicyFile);
				sshManager.sendFileToRemoteHost(files, "/boot/trust");
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				log.error("Unable to send trustPolicy /manifest  file to remote host ", e);
				throw new DirectorException(
						"Unable to send trustPolicy /manifest  file to remote host", e);
			}finally{
				File deleteFile = new File(manifestFile);
				deleteFile.delete();
				deleteFile = new File(trustPolicyFile);
				deleteFile.delete();
				deleteFile = new File(localPathForPolicyAndManifest);
				deleteFile.delete();
			}

		}

		trustPolicy.setTrust_policy(signedPolicyXml);
		trustPolicy.setCreated_date(currentDate);
		trustPolicy.setEdited_date(currentDate);
		trustPolicy.setEdited_by_user_id(ShiroUtil.subjectUsername());
		trustPolicy.setCreated_by_user_id(ShiroUtil.subjectUsername());

		TrustPolicy createdPolicy = null;
		try {
			createdPolicy = imagePersistenceManager.savePolicy(trustPolicy);
		} catch (DbException e) {
			log.error("Unable to save policy after signing", e);
			throw new DirectorException("Unable to save policy after signing",
					e);
		}
		log.info("deleting draft after policy created existingDraftId::"
				+ existingDraft.getId());
		try {
			imagePersistenceManager.destroyPolicyDraft(existingDraft);
		} catch (DbException e) {
			log.error("Unable to delete policy draft after creating policy", e);
		}
		log.info("trust policy succesfylly created , createdPolicyId::"
				+ createdPolicy.getId());		
		return createdPolicy.getId();
	}

	public CreateTrustPolicyMetaDataResponse saveTrustPolicyMetaData(
			CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest)
			throws DirectorException {
		CreateTrustPolicyMetaDataResponse createPolicyMetadataResponse = new CreateTrustPolicyMetaDataResponse();
		try {
				String imageid = createTrustPolicyMetaDataRequest.getImage_id();
			ImageAttributes img=imagePersistenceManager.fetchImageById(imageid);
			if (Constants.DEPLOYMENT_TYPE_BAREMETAL.equals(img
					.getImage_deployments())) {
				TdaasUtil.checkInstalledComponents(imageid);
			}
			
			Date currentDate = new Date();
		
			TrustPolicyDraft existingDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(imageid);

			if (doesPolicyNameExist(createTrustPolicyMetaDataRequest
					.getDisplay_name(),imageid)) {
				throw new DirectorException("Policy Name Already Exists");
			}
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
					createPolicyMetadataResponse.status = Constants.SUCCESS;
				} else {
					// Signed policy exists. We need to copy it over sans the
					// signature and hashes

					log.debug("policy draft already exists");
					TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
					trustPolicyDraft.setCreated_date(currentDate);

					trustPolicyDraft.setEdited_date(currentDate);
					String policyXml = existingPolicy.getTrust_policy();
					trustPolicyDraft
							.setDisplay_name(createTrustPolicyMetaDataRequest
									.getDisplay_name());

					com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil
							.getPolicy(policyXml);
					policy.setSignature(null);
					List<Measurement> measurements = policy.getWhitelist()
							.getMeasurements();
					for (Measurement measurement : measurements) {
						measurement.setValue(null);
					}

					policyXml = TdaasUtil.convertTrustPolicyToString(policy);

					String unsigned_trust_policy = policyXml;// / Need to be
					// removed

					unsigned_trust_policy = TdaasUtil.updatePolicyDraft(
							unsigned_trust_policy,
							createTrustPolicyMetaDataRequest);
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
					createPolicyMetadataResponse.setId(policyDraftCreated.getId());
					createPolicyMetadataResponse.setTrustPolicy(policyDraftCreated
							.getTrust_policy_draft());
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
				createPolicyMetadataResponse.setStatus(Constants.SUCCESS);
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
			Set<TreeNodeDetail> directoryListContainingRegex,
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
						log.info(measurement.getPath()+" **** "+searchFilesInImageRequest.getDir());
						if (searchFilesInImageRequest.init && measurement.getPath().startsWith(searchFilesInImageRequest.getDir())) {
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
								TreeNodeDetail detail = new TreeNodeDetail();
								detail.regexPath = measurement.getPath();
								detail.regexExclude = ((DirectoryMeasurement) measurement).getExclude();
								detail.regexInclude = ((DirectoryMeasurement) measurement).getInclude();
								if(((DirectoryMeasurement) measurement).isRecursive() == null ){
									log.info("--- Recursive flag not found for :" + measurement.getPath());
								}
								detail.isRegexRecursive = ((DirectoryMeasurement) measurement).isRecursive() != null ? ((DirectoryMeasurement) measurement).isRecursive():false;
								directoryListContainingRegex.add(detail);
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
		if (StringUtils.isNotBlank(searchFilesInImageRequest.include) || StringUtils.isNotBlank(searchFilesInImageRequest.exclude) ) {
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
								+ (searchFilesInImageRequest.include == null ? "":searchFilesInImageRequest.include)
								+ "\" Exclude=\""
								+ (searchFilesInImageRequest.exclude == null? "":searchFilesInImageRequest.exclude)
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

	public void updateTrustPolicy(
			UpdateTrustPolicyRequest updateTrustPolicyRequest, String policyId)
			throws DirectorException {

		try {
			if (isPolicyNameNotUnique(
					updateTrustPolicyRequest.getDisplay_name(), policyId)) {
				throw new DirectorException("Policy Name Already Exists");
			}
		} catch (DbException e1) {
			throw new DirectorException(
					"Unable to check uniqueness of policy name : "
							+ updateTrustPolicyRequest.display_name);
		}
		log.debug("Inside  imageStoreUploadRequest::"
				+ updateTrustPolicyRequest);

		if (StringUtils.isBlank(updateTrustPolicyRequest.display_name)) {
			log.error("No policy name provided");
			throw new DirectorException(
					"Policy name is empty");
		}
		// Updating Name in case name is changed

		TrustPolicy trustPolicy;
		try {
			trustPolicy = imagePersistenceManager.fetchPolicyById(policyId);
		} catch (DbException e) {
			throw new DirectorException("Unable to fetch policy with id : "
					+ policyId);
		}
		trustPolicy.setDisplay_name(updateTrustPolicyRequest.getDisplay_name());
		trustPolicy.setEdited_date(new Date());
		trustPolicy.setEdited_by_user_id(ShiroUtil.subjectUsername());
		try {
			imagePersistenceManager.updatePolicy(trustPolicy);
		} catch (DbException e) {
			throw new DirectorException("Unable to update policy with id : "
					+ policyId);
		}

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
			imgResponse.setImage_name(imageInfo.getImage_name());

			String trust_policy = "<div id=\"trust_policy_column"
					+ imageInfo.id + "\">";
			if (imageInfo.getTrust_policy_draft_id() == null
					&& imageInfo.getTrust_policy_id() == null) {

				continue;
			}

			if (imageInfo.getTrust_policy_draft_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicyForBMLive('"
						+ imageInfo.getId() + "','" + imageInfo.getImage_name()
						+ "')\"></span></a>";

			} else if (imageInfo.getTrust_policy_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicyForBMLive('"
						+ imageInfo.getId() + "','" + imageInfo.getImage_name()
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
						+ imageInfo.getImage_name() + "')\"></span></a>";
			}

			trust_policy = trust_policy + "</div>";
			imgResponse.setTrust_policy(trust_policy);

			String display_name = "<div id='policy_name'" + imageInfo.id + ">";
			String dname = getDisplayNameForImage(imageInfo.id);
			if (dname == null) {
				display_name = display_name + "NA";
			} else {
				display_name = display_name + dname;
			}

			display_name = display_name + "</div>";

			imgResponse.setDisplay_name(display_name);

			String image_upload = "";
			image_upload += "&nbsp;"
					+ "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Push To  Host\" onclick=\"pushPolicyToHost('"
					+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "','"
					+ imageInfo.getTrust_policy_id() + "')\" ></span></a>";

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
			imgResponse.setImage_name(imageInfo.getImage_name());
			imgResponse.setImage_format(imageInfo.getImage_format());

			String trust_policy = "<div id=\"trust_policy_column"
					+ imageInfo.id + "\">";
			if (imageInfo.getTrust_policy_draft_id() == null
					&& imageInfo.getTrust_policy_id() == null) {

				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createPolicyForBMImage('"
						+ imageInfo.getId() + "','" + imageInfo.getImage_name()
						+ "')\"></span></a>";

			}

			if (imageInfo.getTrust_policy_draft_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicyForBMImage('"
						+ imageInfo.getId() + "','" + imageInfo.getImage_name()
						+ "')\"></span></a>";

			} else if (imageInfo.getTrust_policy_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicyForBMImage('"
						+ imageInfo.getId() + "','" + imageInfo.getImage_name()
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
						+ imageInfo.getImage_name() + "')\"></span></a>";
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

			String tpid = imageInfo.getTrust_policy_id();
			if (tpid != null) {
				try {
					TrustPolicy trustPolicy = imagePersistenceManager
							.fetchPolicyById(tpid);
					if (trustPolicy != null
							&& trustPolicy.getCreated_date() != null) {
						imgResponse.setCreated_date(trustPolicy
								.getCreated_date());
					}
				} catch (DbException e) {
					log.error(
							"unable to get trust policy by id, getBareMetalLive",
							e);

				}
			}

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
			if(imageInfo.deleted){
				continue;
			}
			ImageListResponseInfo imgResponse = new ImageListResponseInfo();
			imgResponse.setImage_name(imageInfo.getImage_name());
			imgResponse.setImage_format(imageInfo.getImage_format());

			String image_delete = "<a href=\"#\"><span class=\"glyphicon glyphicon-remove\" title=\"Delete Image\" onclick=\"deleteImage('"
					+ imageInfo.getId() + "')\"/></a>";
			imgResponse.setImage_delete(image_delete);

			String trust_policy = "<div id=\"trust_policy_vm_column"
					+ imageInfo.id + "\">";

			if (imageInfo.getTrust_policy_draft_id() == null
					&& imageInfo.getTrust_policy_id() == null) {

				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createPolicy('"
						+ imageInfo.getId() + "','" + imageInfo.getImage_name()
						+ "')\"></span></a>";

			}

			if (imageInfo.getTrust_policy_draft_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicy('"
						+ imageInfo.getId() + "','" + imageInfo.getImage_name()
						+ "')\"></span></a>";

			} else if (imageInfo.getTrust_policy_id() != null) {
				trust_policy = trust_policy
						+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicy('"
						+ imageInfo.getId() + "','" + imageInfo.getImage_name()
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
						+ imageInfo.getImage_name() + "')\"></span></a>";
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
					+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "','"
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
	public TrustPolicyDraft createPolicyDraftFromPolicy(String imageId) throws DirectorException {

		ImageInfo imageInfo;
		try {
			imageInfo = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException e) {
			log.error("Cannot fetch imageid imageId::" + imageId, e);
			throw new DirectorException("Cannot fetch image by id", e);
		}

		TrustPolicy existingTrustPolicy;
		try {
			existingTrustPolicy = imagePersistenceManager
					.fetchPolicyById(imageInfo.getTrust_policy_id());
		} catch (DbException e) {
			log.error("Cannot get policy by id existingTrustPolicyId::"
					+ imageInfo.getTrust_policy_id(), e);
			throw new DirectorException("Cannot get policy by id", e);
		}
		TrustPolicyDraft trustPolicyDraft=null ;
		try {
		trustPolicyDraft = trustPolicyToTrustPolicyDraft(existingTrustPolicy);
		}catch(JAXBException e){
			log.error("Unable convert policy to draft", e);
			throw new DirectorException("Unable convert policy to draft ", e);
		}
		TrustPolicyDraft savePolicyDraft;
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
		return savePolicyDraft;
	}

	public TrustPolicyDraft trustPolicyToTrustPolicyDraft(
			TrustPolicy existingTrustPolicy) throws JAXBException {
		TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
		String policyXml = existingTrustPolicy.getTrust_policy();
		

		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil
				.getPolicy(policyXml);
		policy.setSignature(null);
		List<Measurement> measurements = policy.getWhitelist()
				.getMeasurements();
		for (Measurement measurement : measurements) {
			measurement.setValue(null);
		}

		policyXml = TdaasUtil.convertTrustPolicyToString(policy);

		trustPolicyDraft.setCreated_by_user_id(existingTrustPolicy
				.getCreated_by_user_id());
		trustPolicyDraft.setCreated_date(existingTrustPolicy.getCreated_date());
		trustPolicyDraft.setEdited_by_user_id(existingTrustPolicy
				.getEdited_by_user_id());
		trustPolicyDraft.setEdited_date(existingTrustPolicy.getEdited_date());
		trustPolicyDraft.setName(existingTrustPolicy.getName());
		trustPolicyDraft.setImgAttributes(existingTrustPolicy
				.getImgAttributes());
		trustPolicyDraft.setTrust_policy_draft(policyXml);
		trustPolicyDraft.setDisplay_name(existingTrustPolicy.getDisplay_name());
		return trustPolicyDraft;
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
		DirectoryAndFileUtil directoryAndFileUtil = new DirectoryAndFileUtil(imageId);
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
		Collection<File> faFiles = getFirstLevelFiles(sDir, imageId);

		if (faFiles == null) {
			return files;
		}
		for (File file : faFiles) {
			files.add(file);

			if (new File(TdaasUtil.getMountPath(imageId)
					+ file.getAbsolutePath()).isDirectory()) {
				getFilesAndDirectories(file.getAbsolutePath(), dirs, imageId);
			}
		}
		return files;
	}
	
	private List<File> getFilesAndDirectoriesForEdit(String sDir, Set<String> dirs,
			String imageId) throws DirectorException {
		if (files == null) {
			files = new ArrayList<>();
		}
		if (dirs != null) {
			dirs.add(sDir);
		}
		Collection<File> faFiles = getFirstLevelFiles(sDir, imageId);

		if (faFiles == null) {
			return files;
		}
		for (File file : faFiles) {
			files.add(file);
		}
		return files;
	}

	private Collection<File> getFilesAndDirectoriesWithFilter(
			SearchFilesInImageRequest searchFilesInImageRequest) {
		String mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);

		Collection<File> files = new HashSet<>();
		DirectoryAndFileUtil directoryAndFileUtil = new DirectoryAndFileUtil(searchFilesInImageRequest.id);
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
		String display_name = "";
		try {
			image_info = imagePersistenceManager.fetchImageById(image_id);
			if (image_info.getTrust_policy_draft_id() != null) {
				policy_id = image_info.getTrust_policy_draft_id();
				display_name += imagePersistenceManager.fetchPolicyDraftById(
						policy_id).getDisplay_name();
			} else if (image_info.getTrust_policy_id() != null) {
				policy_id = image_info.getTrust_policy_id();
				display_name += imagePersistenceManager.fetchPolicyById(
						policy_id).getDisplay_name();
			} else {
				display_name += "-";
			}
			return display_name;

		} catch (DbException e) {
			log.error("error in fetching image name", e);
			throw new DirectorException("Error in Fetching Image Name", e);
		}

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
			return imageInfo.getLocation() + "Modified_" + imageInfo.getImage_name();
		} else {
			return imageInfo.getLocation() + imageInfo.getImage_name();
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
	public ImportPolicyTemplateResponse importPolicyTemplate(String imageId)
			throws DirectorException {
		ImportPolicyTemplateResponse importPolicyTemplateResponse;
		ImageAttributes image;
		try {
			image = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException ex) {
			log.error("No image found during import of policy", ex);
			throw new DirectorException("No image found with id: " + imageId,
					ex);
		}

		if (image != null
				&& !(image.getImage_deployments().equals(
						Constants.DEPLOYMENT_TYPE_BAREMETAL) && image
						.getImage_format() == null)) {
			importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
			importPolicyTemplateResponse.status = "Success";
			importPolicyTemplateResponse.details = "No need for import ";
			return importPolicyTemplateResponse;
		}

		// Get the draft
		TrustPolicyDraft policyDraftForImage;
		try {
			policyDraftForImage = imagePersistenceManager
					.fetchPolicyDraftForImage(imageId);
		} catch (DbException ex) {
			log.error("Error fetching trust policy in import policy", ex);
			throw new DirectorException(
					"Error fetching trust policy in import policy: " + imageId,
					ex);
		}

		com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy;
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

		List<Measurement> existingMeasurements = policy.getWhitelist()
				.getMeasurements();

		if (existingMeasurements != null && existingMeasurements.size() > 0) {
			importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
			importPolicyTemplateResponse
					.setTrust_policy(policyDraftForImage.getTrust_policy_draft());
			importPolicyTemplateResponse.setStatus("Success");
			return importPolicyTemplateResponse;
		}

		// Check if mounted live BM has /opt/vrtm
		String idendifier=TdaasUtil.checkInstalledComponents(imageId);

		String content = null;
		Manifest manifest;

		try {
			List<PolicyTemplateInfo> fetchPolicyTemplateForDeploymentIdentifier = imagePersistenceManager
					.fetchPolicyTemplateForDeploymentIdentifier(
							Constants.DEPLOYMENT_TYPE_BAREMETAL, idendifier);
			if (fetchPolicyTemplateForDeploymentIdentifier != null
					&& fetchPolicyTemplateForDeploymentIdentifier.size() > 0) {
				PolicyTemplateInfo policyTemplateInfo = fetchPolicyTemplateForDeploymentIdentifier
						.get(0);
				content = policyTemplateInfo.getContent();
			}
		} catch (DbException e2) {
			log.error("Error converting manifest to object ", e2);
			throw new DirectorException("Error converting manifest to object ",
					e2);
		}

		if (StringUtils.isBlank(content)) {
			importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
			importPolicyTemplateResponse
					.setTrust_policy(policyDraftForImage.getTrust_policy_draft());
			importPolicyTemplateResponse.setStatus("Success");
			return importPolicyTemplateResponse;
		}
		try {
			manifest = TdaasUtil.convertStringToManifest(content);
		} catch (JAXBException e1) {
			log.error("Error converting manifest to object " + content, e1);
			throw new DirectorException("Error converting manifest to object ",
					e1);
		}

		Collection<Measurement> measurements = new ArrayList<>();
		for (MeasurementType measurementType : manifest.getManifest()) {
			if (measurementType instanceof DirectoryMeasurementType) {
				DirectoryMeasurement measurement = new DirectoryMeasurement();
				measurement.setPath(measurementType.getPath());
				measurement.setExclude(((DirectoryMeasurementType) measurementType).getExclude());
				measurement.setInclude(((DirectoryMeasurementType) measurementType).getInclude());
				measurement.setRecursive(((DirectoryMeasurementType) measurementType).isRecursive());				
				measurements.add(measurement);
			} else if (measurementType instanceof FileMeasurementType) {
				FileMeasurement measurement = new FileMeasurement();
				measurement.setPath(measurementType.getPath());
				measurements.add(measurement);
			}
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
			imagePersistenceManager.updatePolicyDraft(policyDraftForImage);
		} catch (DbException e) {
			log.error("Error saving policy draft for image after adding imports for IMAGE:  "
					+ imageId);
			throw new DirectorException(
					"Error saving policy draft for image after adding imports for IMAGE:  "
							+ imageId, e);
		}
		importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
		importPolicyTemplateResponse.setTrust_policy(policyXmlWithImports);
		importPolicyTemplateResponse.setStatus(Constants.SUCCESS);
		return importPolicyTemplateResponse;
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
			// TODO Handle Error
			log.error("error in createlistOfFilesForRegexReset()", je);
		}
	}

	@Override
	public void deleteTrustPolicy(String trust_policy_id)
			throws DirectorException {

		TrustPolicy policy;
		try {
			policy = imagePersistenceManager.fetchPolicyById(trust_policy_id);
		} catch (DbException e) {
			log.error("Error fetching policy for id " + trust_policy_id);
			throw new DirectorException("Error fetching policy for id "
					+ trust_policy_id, e);
		}
		if (policy != null) {
			try {
				imagePersistenceManager.destroyPolicy(policy);
			} catch (DbException e) {
				log.error("Error deleting policy with id " + trust_policy_id);
				throw new DirectorException("Error deleting policy with id "
						+ trust_policy_id, e);
			}
			ImageInfo image;
			try {
				image = imagePersistenceManager.fetchImageById(policy
						.getImgAttributes().id);
			} catch (DbException e) {
				log.error("Error fetching image for policy with id "
						+ trust_policy_id);
				throw new DirectorException(
						"Error fetching image for policy with id "
								+ trust_policy_id, e);
			}
			if (image.getImage_format() == null) {
				SshSettingInfo existingSsh = null;
				try {
					existingSsh = imagePersistenceManager
							.fetchSshByImageId(image.id);
				} catch (DbException e) {
					log.error("Error fetching SSH Settings for image id "
							+ image.id);
					throw new DirectorException(
							"Error fetching SSH Settings for image id "
									+ image.id, e);
				}
				if (existingSsh != null && existingSsh.getId() != null
						&& StringUtils.isNotEmpty(existingSsh.getId())) {
					try {
						imagePersistenceManager.destroySshById(existingSsh
								.getId());
					} catch (DbException e) {
						log.error("Error deleting SSH Settings for image id "
								+ image.id);
						throw new DirectorException(
								"Error deleting SSH Settings for image id "
										+ image.id, e);
					}
				}
			}
		}
	}

	@Override
	public void deleteTrustPolicyDraft(String trust_policy_draft_id)
			throws DirectorException {
		TrustPolicyDraft policyDraft;
		try {
			policyDraft = imagePersistenceManager
					.fetchPolicyDraftById(trust_policy_draft_id);
		} catch (DbException e) {
			log.error("Error fetching policy draft for id " + trust_policy_draft_id);
			throw new DirectorException("Error fetching policy draft for id "
					+ trust_policy_draft_id, e);
		}
		if (policyDraft != null) {
			try {
				imagePersistenceManager.destroyPolicyDraft(policyDraft);
			} catch (DbException e) {
				log.error("Error deleting policy draft with id " + trust_policy_draft_id);
				throw new DirectorException("Error deleting policy draft with id "
						+ trust_policy_draft_id, e);
			}
		}
		deleteImageShhSettings(policyDraft.getImgAttributes().id);
	}

	
	private void deleteImageShhSettings(String imageId) throws DirectorException{
		ImageInfo image;
		try {
			image = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException e) {
			log.error("Error fetching image  with id "
					+ imageId);
			throw new DirectorException(
					"Error fetching image  with id "
							+ imageId, e);
		}
		if (image.getImage_format() == null) {
			SshSettingInfo existingSsh = null;
			try {
				existingSsh = imagePersistenceManager
						.fetchSshByImageId(image.id);
			} catch (DbException e) {
				log.error("Error fetching SSH Settings for image id "
						+ image.id);
				throw new DirectorException(
						"Error fetching SSH Settings for image id "
								+ image.id, e);
			}
			if (existingSsh != null && existingSsh.getId() != null
					&& StringUtils.isNotEmpty(existingSsh.getId())) {
				try {
					imagePersistenceManager.destroySshById(existingSsh
							.getId());
				} catch (DbException e) {
					log.error("Error deleting SSH Settings for image id "
							+ image.id);
					throw new DirectorException(
							"Error deleting SSH Settings for image id "
									+ image.id, e);
				}
			}
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
		String manifest;
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
					"manifest.xml", artifactsPath + File.separator, tarName);
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

	@Override
	public SshSettingRequest getBareMetalMetaData(String image_id)
			throws DirectorException {
		SshSettingRequest settingRequest = new SshSettingRequest();
		try {
			SshSettingInfo ssh = imagePersistenceManager
					.fetchSshByImageId(image_id);
			settingRequest.setId(ssh.getId());
			settingRequest.setName(ssh.getName());
			settingRequest.setPolicy_name(getDisplayNameForImage(image_id));
			settingRequest.setIpAddress(ssh.getIpAddress());
			settingRequest.setUsername(ssh.getUsername());
		} catch (DbException e) {
			log.error("Error Fetching ssh from DB in getBareMetalMetaData", e);
			throw new DirectorException(
					"Error Fetching ssh from DB in getBareMetalMetaData", e);
		}
		return settingRequest;
	}

	@Override
	public void deletePasswordForHost(String image_id) throws DirectorException {
		SshSettingInfo ssh = null;
		try {
			ImageInfo imageInfo = imagePersistenceManager.fetchImageById(image_id);
			if(Constants.DEPLOYMENT_TYPE_VM.equals(imageInfo.getImage_deployments()) || imageInfo.getImage_format() != null){
				return;
			}
			
			ssh = imagePersistenceManager.fetchSshByImageId(image_id);
			log.info("Setting passowrd to null for SSH : "+ssh.id);
			SshPassword password = ssh.getPassword();
			ssh.setPassword(null);
			imagePersistenceManager.updateSsh(ssh);
			log.info("complete update host with null for password");
			imagePersistenceManager.destroySshPassword(password.getId());
			log.info("Destroying passowrd to null for SSH : "+ssh.id);
		} catch (DbException e) {
			log.error("Error in deleting SSh Password", e);
			throw new DirectorException("Error in deleting SSh Password", e);
		}

	}

	@Override
	public void deleteImage(String imageId) throws DirectorException {
		try {
			ImageInfo imageInfo = imagePersistenceManager
					.fetchImageById(imageId);
			log.debug("image fetched for id: " + imageId);
			imageInfo.setDeleted(true);
			imagePersistenceManager.updateImage(imageInfo);
			log.info("Image " + imageId + "deleted successfully");
		} catch (DbException e) {
			log.error("Error deleteing image: " + imageId, e);
			throw new DirectorException("Error deleting image", e);
		}
	}

	@Override
	public boolean doesPolicyNameExist(String display_name, String image_id)
			throws DirectorException {
		try {
			List<TrustPolicyDraft> trustPolicyDraftList = imagePersistenceManager
					.fetchPolicyDrafts(null);
			if (trustPolicyDraftList != null) {
				for (TrustPolicyDraft tpd : trustPolicyDraftList) {
					if (!tpd.getImgAttributes().isDeleted() && tpd.getDisplay_name() != null
							&& tpd.getDisplay_name().equalsIgnoreCase(
									display_name)
							&& !tpd.getImgAttributes().getId().equals(image_id)) {
						return true;
					}
				}
			}
		} catch (DbException e) {
			throw new DirectorException(
					"Error in fetching trustpolicydrafts list", e);
		}
		try {
			List<TrustPolicy> trustPolicyList = imagePersistenceManager
					.fetchPolicies(null);
			if (trustPolicyList != null) {
				for (TrustPolicy tpd : trustPolicyList) {
					if (!tpd.getImgAttributes().isDeleted() && tpd.getDisplay_name() != null
							&& tpd.getDisplay_name().equalsIgnoreCase(
									display_name)
							&& !tpd.getImgAttributes().getId().equals(image_id)) {
						return true;
					}
				}
			}
		} catch (DbException e) {
			throw new DirectorException(
					"Error in fetching trustpolicy list", e);
		}

		return false;
	}
	

	private boolean isPolicyNameNotUnique(String display_name, String trustPolicyId) throws DbException, DirectorException {
		TrustPolicy fetchPolicyById = imagePersistenceManager.fetchPolicyById(trustPolicyId);
		String imageId = fetchPolicyById.getImgAttributes().id;
		return doesPolicyNameExist(display_name, imageId);
	}

	
	@Override
	public boolean doesImageNameExist(String fileName) throws DirectorException {
		try {
			List<ImageInfo> imagesList = imagePersistenceManager.fetchImages(null);
			for(ImageInfo image : imagesList)
			{
				if(!image.isDeleted() && image.getImage_name() != null && image.getImage_name().equalsIgnoreCase(fileName))
				{
					return true;
				}
			}
		} catch (DbException e) {
			throw new DirectorException("Unable to fetch Images",e);
		}
		return false;
	}

	@Override
	public TrustPolicyResponse getTrustPolicyMetaData(String trust_policy_id) throws DirectorException {
		TrustPolicy trustPolicy;
		try {
			trustPolicy = imagePersistenceManager.fetchPolicyById(trust_policy_id);
		} catch (DbException e) {
			log.error("Unable To fetch Policy with policy Id :: " + trust_policy_id);
			throw new DirectorException("Unable To fetch Policy with policy Id :: " + trust_policy_id,e);
		}
		if(trustPolicy == null || trustPolicy.getTrust_policy() == null){
			log.error("Trust Policy is null for :: " + trust_policy_id);
			throw new DirectorException("Trust Policy is null  :: " + trust_policy_id);
		}
		
		try {
			return TdaasUtil.convertTrustPolicyToTrustPolicyResponse(trustPolicy);
		} catch (JAXBException e) {
			log.error("Unable to convert Trust Policy to TrustPolicyResponse",e);
			throw new DirectorException("Unable to convert Trust Policy to TrustPolicyResponse",e);
		}
	}

	@Override
	public String getImageByTrustPolicyDraftId(
			String trustPolicydraftId) throws DirectorException{
		TrustPolicyDraft existingDraft = null;
		try {
			existingDraft = imagePersistenceManager.fetchPolicyDraftById(trustPolicydraftId);
		} catch (DbException e1) {
			String errorMsg = "Unable to fetch draft for trust policy draft id "+ trustPolicydraftId;
			log.error(errorMsg);
			throw new DirectorException(errorMsg, e1);
		}
		return existingDraft.getImgAttributes().id;

	}

}
