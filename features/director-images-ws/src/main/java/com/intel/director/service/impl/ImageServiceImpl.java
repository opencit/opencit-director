package com.intel.director.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import com.intel.director.api.PolicyToMountedImageRequest;
import com.intel.director.api.PolicyToMountedImageResponse;
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

	private final int MaxFileSize = 50 * 1024 * 1024 * 1024;
	private final int MaxMemSize = 4 * 1024;

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

		log.info("inside mounting image in service");

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

		if (image.mounted_by_user_id != null) {
			log.info("image already mounted by user : "
					+ image.mounted_by_user_id);
			throw new DirectorException(
					"Unable to mount image. Image is already in use by user: ");

		}

		
		log.info("Mounting image from location: " + image.location);

		String mountPath = DirectorUtil.getMountPath(image.id);
		log.info("mount path is "+mountPath);
		String mounteImageName = image.getName();
		log.info("mount image : "+mounteImageName);
		/*
		 * if(
		 * Constants.DEPLOYMENT_TYPE_BAREMETAL.equals(image.getImage_deployments
		 * ()) && image.getImage_format()==null){ //Case of Bare metal Image
		 * 
		 * 
		 * String modifiedImagePath=image.getLocation()+image.getName();
		 * DirectorUtil
		 * .createCopy(image.getLocation()+image.getName(),image.getLocation
		 * ()+"Modified_"+image.getName());
		 * 
		 * }
		 */
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
					log.info("Imagepath : "+modifiedImagePath);
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
				log.info("BM LIve host : "+info.toString());
				int exitCode = MountImage.mountRemoteSystem(
						info.getIpAddress(), info.getUsername(), info
								.getSshPassword().getKey(), mountPath);
				if(exitCode == 1){
					log.error("Error mounting remote host : "+info.toString());
					throw new DirectorException("Error mounting remote host with the credentials provided : "+info.toString());
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
			user = "admin";
			image.setMounted_by_user_id(user);
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

					int exitCode = MountImage.unmountRemoteSystem(mountPath);
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
		try {
			ImageAttributes image = imagePersistenceManager
					.fetchImageById(imageId);
			log.info("Unmounting image : " + image.id + " with name : "
					+ image.name);
			String mountPath = com.intel.director.common.DirectorUtil
					.getMountPath(imageId);
			user = "admin";
			if(image.getMounted_by_user_id() == null){
				unmountImageResponse = TdaasUtil
						.mapImageAttributesToUnMountImageResponse(image);
				return unmountImageResponse;
			}

			image.setMounted_by_user_id(null);
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
				int exitCode = MountImage.unmountRemoteSystem(mountPath);
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

	@Override
	public TrustDirectorImageUploadResponse uploadImageToTrustDirectorSingle(
			String image_deployments, String image_format,
			HttpServletRequest request) throws DirectorException {

		DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(MaxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File(Constants.defaultUploadPath));
		String filePath = Constants.defaultUploadPath;

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum file size to be uploaded.
		upload.setSizeMax(MaxFileSize);
		File file = null;

		// Parse the request to get file items.

		try {
			List fileItems = upload.parseRequest(request);
			log.info("File items parsed : " + fileItems.size());

			// Process the uploaded file items
			@SuppressWarnings("rawtypes")
			Iterator i = fileItems.iterator();
			while (i.hasNext()) {
				FileItem fi = (FileItem) i.next();
				if (!fi.isFormField()) {
					// Get the uploaded file parameters
					String fileName = fi.getName();
					// Write the file
					if (fileName.lastIndexOf("\\") >= 0) {
						file = new File(
								filePath
										+ fileName.substring(fileName
												.lastIndexOf("\\")));
					} else {
						file = new File(
								filePath
										+ fileName.substring(fileName
												.lastIndexOf("\\") + 1));
					}
					fi.write(file);
				}
			}
		} catch (Exception e) {
			log.error("Error while reading and writing uploaded file : "
					+ e.getMessage());
			throw new DirectorException("Cannot write the uploaded file", e);
		}
		log.debug("Completed writing file");
		ImageAttributes imageAttributes = new ImageAttributes();
		imageAttributes.name = file.getName();
		imageAttributes.image_deployments = image_deployments;
		imageAttributes.setCreated_by_user_id("admin");
		imageAttributes.setEdited_by_user_id("admin");
		imageAttributes.setStatus(Constants.COMPLETE);
		imageAttributes.setDeleted(false);
		int sizen_kb = (int) (file.length() / 1024);
		imageAttributes.setSent(sizen_kb);
		imageAttributes.setImage_size(sizen_kb);
		imageAttributes.location = Constants.defaultUploadPath;
		imageAttributes.image_format = image_format;
		Date currentDate = new Date();
		imageAttributes.setCreated_date(currentDate);
		imageAttributes.setEdited_date(currentDate);
		try {
			log.debug("Saving metadata of uploaded file");
			imagePersistenceManager.saveImageMetadata(imageAttributes);
		} catch (DbException e) {
			log.error("Error while saving metadata for uploaded file : "
					+ e.getMessage());
			throw new DirectorException("Cannot save image meta data", e);
		}
		log.info("Image uploaded to TDAAS at " + imageAttributes.getLocation());

		return TdaasUtil
				.mapImageAttributesToTrustDirectorImageUploadResponse(imageAttributes);
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

		SearchFilesInImageResponse filesInImageResponse = new SearchFilesInImageResponse();
		Collection<File> listFilesAndDirs = getFirstLevelFiles(searchFilesInImageRequest);

		List<Measurement> measurements = getMeasurements(searchFilesInImageRequest.id);
		// this map contains the directory paths as key. the value - boolean
		// indicates whether all
		// the sub files of these directory need to be fetched.
		// false - only first level files
		Map<String, Boolean> directoryListContainingPolicyFiles = new HashMap<>();
		init(trustPolicyElementsList, directoryListContainingPolicyFiles,
				searchFilesInImageRequest);

		// Fetch the files
		if (searchFilesInImageRequest.recursive) {
			treeFiles = getFilesAndDirectories(mountPath
					+ searchFilesInImageRequest.getDir(), null,
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
				fileNames);

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

		String parent = searchFilesInImageRequest.getDir().replace("\\", "/");
		TreeNode root = new TreeNode(parent, parent);

		Tree tree = new Tree(root, searchFilesInImageRequest.recursive,
				searchFilesInImageRequest.files_for_policy);
		root.parent = tree;
		tree.mountPath = mountPath;

		// In case of regex, find the list of files and add it here and then set
		// it in
		if (searchFilesInImageRequest.include != null
				|| searchFilesInImageRequest.exclude != null) {
			Collection<File> regexFiles = getFilesAndDirectoriesWithFilter(searchFilesInImageRequest);
			trustPolicyElementsList = new ArrayList<String>();
			for (File file : regexFiles) {
				patchFileAddSet.add(file.getAbsolutePath().replace(mountPath,
						""));
				if (!trustPolicyElementsList.contains(file.getAbsolutePath()
						.replace(mountPath, ""))) {
					trustPolicyElementsList.add(file.getAbsolutePath().replace(
							mountPath, ""));
				}
			}

			patchDirAddSet.add(searchFilesInImageRequest.getDir());
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return policyDraftXml;
	}

	@Override
	public TrustPolicy getTrustPolicyByTrustId(String trustId) {
		TrustPolicy fetchPolicy = null;
		try {
			fetchPolicy = imagePersistenceManager.fetchPolicyById(trustId);

		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String draft = trustPolicyDraft.getTrust_policy_draft();

		try {
			draft = TdaasUtil.patch(draft, trustpolicyDraftEditRequest.patch);
			trustPolicyDraft.setTrust_policy_draft(draft);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			imagePersistenceManager.updatePolicyDraft(trustPolicyDraft);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			throw new DirectorException(e);
		} catch (JAXBException e) {
			throw new DirectorException(e);
		}
		return metadata;

	}

	public String createTrustPolicy(String image_id) throws DirectorException {

		TrustPolicy existingTrustpolicy = null;
		ImageAttributes image=null;
		try {

			TrustPolicyDraft existingDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(image_id);
			if (existingDraft == null) {

				throw new DirectorException("Policy draft do not exist");
			}

			existingTrustpolicy = imagePersistenceManager
					.fetchPolicyForImage(image_id);
			if (existingTrustpolicy != null) {

				// ///TODO :- Archive older Trust Policy
				// / existingTrustpolicy update archive column to true

			}
			Date currentDate = new Date();

			TrustPolicy trustPolicy = new TrustPolicy();
			trustPolicy.setCreated_date(currentDate);

			trustPolicy.setEdited_date(currentDate);

			String policyXml = existingDraft.getTrust_policy_draft();
			String display_name = existingDraft.getDisplay_name();
		

		///	trustPolicy.setTrust_policy(policyXml);
			trustPolicy.setDisplay_name(display_name);
			ImageAttributes imgAttrs = new ImageAttributes();
			imgAttrs.setId(image_id);
			trustPolicy.setImgAttributes(imgAttrs);
			log.debug("Going to save trust policy for image_id::" + image_id);
			try {
				image = imagePersistenceManager.fetchImageById(image_id);
			} catch (DbException ex) {
				log.error("Error in mounting image  ", ex);
				throw new DirectorException("No image found with id: " + image_id,
						ex);
			}
			// Get the hash
			try {
				com.intel.mtwilson.trustpolicy.xml.TrustPolicy policy = TdaasUtil.getPolicy(policyXml);
				CreateTrustPolicy.createTrustPolicy(policy);
				policyXml = TdaasUtil.convertTrustPolicyToString(policy);
				
				String mountPath = DirectorUtil.getMountPath(image_id);
				if ((Constants.DEPLOYMENT_TYPE_BAREMETAL.equals(image
						.getImage_deployments()) && image.getImage_format() != null)
						|| (image.getImage_format() == null)) {

					// Writing inside bare metal modified image
					if (trustPolicy != null) {
						String remoteDirPath = mountPath + "/boot/trust";
						if (!Files.exists(Paths.get(remoteDirPath)))
							;
						DirectorUtil.callExec("mkdir -p " + remoteDirPath);
						// // String policyPath =
						// remoteDirPath+"/"+"trustpolicy.xml";
						String trustPolicyName = null;
						File trustPolicyFile = null;
						if (image.getImage_format() == null) {
							trustPolicyName = "policy.xml";
						} else {
							trustPolicyName = "policy"
									+ trustPolicy.getDisplay_name() + ".xml";
						}
						trustPolicyFile = new File(remoteDirPath
								+ File.separator + trustPolicyName);

						if (!trustPolicyFile.exists()) {
							trustPolicyFile.createNewFile();
						}

						FileWriter fw = new FileWriter(
								trustPolicyFile.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(policyXml);
						bw.close();

					}
				
				}
				
				
				trustPolicy.setTrust_policy(policyXml);
			} catch (IOException | JAXBException e) {
				log.error("Error while creating trust policy", e);			
				throw new DirectorException("Exception while creating trust policy from draft", e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("Error while creating trust policy", e);				
				throw new DirectorException("Exception while creating trust policy from draft", e);
			}

			TrustPolicy createdPolicy = imagePersistenceManager
					.savePolicy(trustPolicy);
			log.debug("deleting draft after policy created existingDraftId::"
					+ existingDraft.getId());
			imagePersistenceManager.destroyPolicyDraft(existingDraft);
			log.debug("trust policy succesfylly created , createdPolicyId::"
					+ createdPolicy.getId());
			
						
			// Creating an ImageAction
			if(TdaasUtil.isImageEncryptStatus(policyXml))
			{
				return createImageActionById(image_id, trustPolicy, true);
			}
			
		} catch (DbException  e) {
			log.error("Db exception thrown in create trust policy", e);
			if (existingTrustpolicy != null) {
				// /TODO update archive column to false
			}
		}catch (JAXBException  e) {
				log.error("JAXB exception thrown in create trust policy", e);
				if (existingTrustpolicy != null) {
					// /TODO update archive column to false
				}
			throw new DirectorException(e);
		} finally {

		}
		return "";
	}

	private String createImageActionById(String image_id,
			TrustPolicy existingPolicy, boolean isFlowUpload)
			throws DirectorException {
		// TODO Auto-generated method stub
		try {
			List<ImageActionActions> actions = new ArrayList<ImageActionActions>();
			ImageActionObject imageActionObject = new ImageActionObject();
			ImageActionObject createdImageActionObject = new ImageActionObject();
			ImageActionActions imageActionActions = new ImageActionActions();
			imageActionObject.setImage_id(image_id);
			if (existingPolicy!=null && isFlowUpload && TdaasUtil.isImageEncryptStatus(existingPolicy.getTrust_policy())) {
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
					policyDraftCreated = imagePersistenceManager
							.savePolicyDraft(trustPolicyDraft);

					createPolicyMetadataResponse.setId(policyDraftCreated
							.getId());
					createPolicyMetadataResponse
							.setTrustPolicy(policyDraftCreated
									.getTrust_policy_draft());
				} else {
					log.debug("policy draft already exists");
					TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
					trustPolicyDraft.setCreated_date(currentDate);

					trustPolicyDraft.setEdited_date(currentDate);
					String policyXml = existingPolicy.getTrust_policy();
					trustPolicyDraft
							.setDisplay_name(createTrustPolicyMetaDataRequest
									.getDisplay_name());
					/*
					 * TODO write unsigning code = String
					 * unsigned_trust_policy=DirectorUtil
					 * .unsignPolicy(policyXml);
					 */

					String unsigned_trust_policy = policyXml;// / Need to be
					// removed

					trustPolicyDraft
							.setTrust_policy_draft(unsigned_trust_policy);

					ImageAttributes imgAttrs = new ImageAttributes();
					imgAttrs.setId(imageid);
					trustPolicyDraft.setImgAttributes(imgAttrs);
					TrustPolicyDraft policyDraftCreated;
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

	public PolicyToMountedImageResponse pushPolicyToMountedImage(
			PolicyToMountedImageRequest pushPolicyToMountedImageRequest)
			throws DirectorException {
		PolicyToMountedImageResponse policyToMountedImageResponse = new PolicyToMountedImageResponse();
		ImageAttributes image;
		try {
			image = imagePersistenceManager
					.fetchImageById(pushPolicyToMountedImageRequest
							.getHost_id());
		} catch (DbException ex) {
			throw new DirectorException("No image found with id: "
					+ pushPolicyToMountedImageRequest.getHost_id(), ex);
		}
		TrustPolicy trustPolicy;
		try {
			trustPolicy = imagePersistenceManager
					.fetchPolicyForImage(pushPolicyToMountedImageRequest
							.getHost_id());
		} catch (DbException ex) {
			throw new DirectorException(
					"Unable to fetch the policy for image with Id: "
							+ pushPolicyToMountedImageRequest.getHost_id(), ex);
		}
		// push the policy to mounted host image
		// String mountPath = DirectorUtil
		// .computeVMMountPath(pushPolicyToMountedImageRequest
		// .getHost_id());

		try {
			// TODO: Push to image
			// MountVMImage.pushToImage(trustPolicy.getTrust_policy(),
			// mountPath);
		} catch (Exception ex) {
			throw new DirectorException("Unable to push the policy to image",
					ex);
		}

		// policyToMountedImageResponse = DirectorUtil
		// .mapImageAttributesToPolicyToMountedImageResponse(image);

		return policyToMountedImageResponse;
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
		String mountPath = DirectorUtil
				.getMountPath(searchFilesInImageRequest.id);
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
		String mountPath = DirectorUtil
				.getMountPath(searchFilesInImageRequest.id);
		for (File file : treeFiles) {
			String _file = file.getAbsolutePath().replace("\\", "/")
					.replace(mountPath, "");
			_file = _file.replaceFirst(searchFilesInImageRequest.getDir(), "");
			if (!directoryListContainingPolicyFiles.contains(mountPath
					+ File.separator + _file)) {
				fileNames.add(_file);
			}
		}
	}

	private void init(List<String> trustPolicyElementsList,
			Map<String, Boolean> directoryListContainingPolicyFiles,
			SearchFilesInImageRequest searchFilesInImageRequest) {
		/*
		 * if (!searchFilesInImageRequest.init) { trustPolicyElementsList =
		 * null; return; }
		 */
		// Fetch the files from the draft
		TrustPolicyDraft trustPolicyDraft = null;
		String mountPath = DirectorUtil
				.getMountPath(searchFilesInImageRequest.id);
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

		if (includeDir) {
			for (String patchFile : patchDirAddSet) {
				boolean found = false;
				if (measurements != null) {
					for (Measurement measurement : measurements) {
						if (measurement instanceof DirectoryMeasurement) {
							if (measurement.getPath().equals(patchFile)) {
								found = true;
								break;
							}
						}
					}
				}

				if (!found) {
					filesInImageResponse.patchXml
							.add("<add sel='//*[local-name()=\"Whitelist\"]'><Dir Path=\""
									+ patchFile
									+ "\" Include=\""
									+ searchFilesInImageRequest.include
									+ "\" Exclude=\" "
									+ searchFilesInImageRequest.exclude
									+ "\"/></add>");
				}
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
					imagePersistenceManager.updatePolicyDraft(trustPolicyDraft);
				} else if (image_info.getTrust_policy_id() != null) {
					TrustPolicy trustPolicy = imagePersistenceManager
							.fetchPolicyById(image_info.getTrust_policy_id());
					trustPolicy.setDisplay_name(imageStoreUploadRequest
							.getDisplay_name());
					imagePersistenceManager.updatePolicy(trustPolicy);
				}
			}
			/*
			 * File trustPolicyFile = null; ImageStoreResponse imgResponse =
			 * null; try { ImageInfo imageInfo = imagePersistenceManager
			 * .fetchImageById(imageStoreUploadRequest.getImage_id());
			 * 
			 * String diskFormat = null, containerFormat = null;
			 * ImageStoreUploadResponse uploadToImageStoreResponse = new
			 * ImageStoreUploadResponse(); ImageStoreUploadResponse
			 * uploadToPolicyStoreResponse = new ImageStoreUploadResponse();
			 * switch (imageInfo.image_format) { case "ami": diskFormat = "ami";
			 * containerFormat = "ami"; break; case "qcow2": diskFormat =
			 * "qcow2"; containerFormat = "bare"; break; case "vhd": diskFormat
			 * = "vhd"; containerFormat = "bare"; break; case "raw": diskFormat
			 * = "raw"; containerFormat = "bare"; break; }
			 * 
			 * Map<String, String> imageProperties = new HashMap<>();
			 * imageProperties.put(Constants.NAME, "test_upload");
			 * imageProperties.put(Constants.DISK_FORMAT, diskFormat);
			 * imageProperties.put(Constants.CONTAINER_FORMAT, containerFormat);
			 * imageProperties.put(Constants.IS_PUBLIC, "true");
			 * 
			 * String imageLocation = imageInfo.getLocation();
			 * 
			 * TrustPolicy tp = imagePersistenceManager
			 * .fetchPolicyForImage(imageStoreUploadRequest.getImage_id()); if
			 * (imageStoreUploadRequest.getStore_name_for_tarball_upload() !=
			 * null || imageStoreUploadRequest
			 * .getStore_name_for_policy_upload() != null) { if (tp != null) {
			 * if (tp.getName() == null) { tp.setName("upload_policy_" +
			 * tp.getId()); } trustPolicyFile = new File(tp.getName());
			 * 
			 * // if file doesnt exists, then create it if
			 * (!trustPolicyFile.exists()) { trustPolicyFile.createNewFile(); }
			 * 
			 * FileWriter fw = new FileWriter(
			 * trustPolicyFile.getAbsoluteFile()); BufferedWriter bw = new
			 * BufferedWriter(fw); bw.write(tp.getTrust_policy()); bw.close(); }
			 * } if (imageStoreUploadRequest.getStore_name_for_tarball_upload()
			 * != null && !"".equalsIgnoreCase(imageStoreUploadRequest
			 * .getStore_name_for_tarball_upload())) { // ////TODO:- Persistence
			 * layer call to get className String className =
			 * "GlanceImageStoreManager.java"; ImageStoreManager imgStoremanager
			 * = getImageStoreImpl(className);
			 * 
			 * String tarballLocation = DirectorUtil .createImageTrustPolicyTar(
			 * trustPolicyFile.getAbsolutePath(), imageLocation); File
			 * tarballfile = new File(tarballLocation); String uploadId =
			 * imgStoremanager.upload(tarballfile, imageProperties);
			 * uploadToImageStoreResponse = imgStoremanager.fetchDetails(
			 * imageProperties, uploadId);
			 * 
			 * } else {
			 * 
			 * if (imageStoreUploadRequest.getStore_name_for_policy_upload() !=
			 * null && !"".equalsIgnoreCase(imageStoreUploadRequest
			 * .getStore_name_for_policy_upload())) { // ////TODO:- Persistence
			 * layer call to get className String className =
			 * "GlanceImageStoreManager.java"; ImageStoreManager imgStoremanager
			 * = getImageStoreImpl(className);
			 * 
			 * String uploadId = imgStoremanager.upload(trustPolicyFile,
			 * imageProperties); uploadToPolicyStoreResponse =
			 * imgStoremanager.fetchDetails( imageProperties, uploadId);
			 * 
			 * }
			 * 
			 * if (imageStoreUploadRequest.getStore_name_for_image_upload() !=
			 * null && !"".equalsIgnoreCase(imageStoreUploadRequest
			 * .getStore_name_for_image_upload())) {
			 * 
			 * // ////TODO:- Persistence layer call to get className String
			 * className = "GlanceImageStoreManager.java"; ImageStoreManager
			 * imgStoremanager = getImageStoreImpl(className); File imageFile =
			 * new File(imageLocation); String uploadId =
			 * imgStoremanager.upload(trustPolicyFile, imageProperties);
			 * uploadToImageStoreResponse = imgStoremanager.fetchDetails(
			 * imageProperties, uploadId);
			 * 
			 * }
			 * 
			 * }
			 * 
			 * /* TODO:- Persist in database populate
			 * ImageStoreUploadTranfserObject
			 * imagePersistenceManager.saveImageUpload(imgUpload);
			 * 
			 * TODO:- Populate ImageStoreResponse from
			 * uploadToImageStoreResponse and uploadToPolicyStoreResponse
			 */

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

			String trust_policy = "";
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
						+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicy('"
						+ imageInfo.getId() + "','"
						+ imageInfo.getTrust_policy_id() + "')\"></span></a>";
			}

			imgResponse.setTrust_policy(trust_policy);

			String image_upload;
			if (imageInfo.getUploads_count() != 0) {
				image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-ok\" title=\"Uploaded Before\"></span></a>";
			} else {
				image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-minus\" title=\"Never Uploaded\"></span></a>";
			}

			image_upload += "&nbsp;"
					+ "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Upload\" onclick=\"pushPolicy('"
					+ imageInfo.getId() + "','" + imageInfo.getName() + "','"
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
			imgResponse.setImage_name(imageInfo.getName());
			imgResponse.setImage_format(imageInfo.getImage_format());

			String trust_policy = "";
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

			String trust_policy = "";
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

			imgResponse.setCreated_date(imageInfo.getCreated_date());

			imageListresponse.images.add(imgResponse);

		}

		return imageListresponse;
	}

	@Override
	public TrustPolicyDraft createPolicyDraftFromPolicy(String imageId,
			String image_action_id) throws DirectorException {
		// TODO Auto-generated method stub

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
			savePolicyDraft = imagePersistenceManager
					.savePolicyDraft(trustPolicyDraft);
		} catch (DbException e) {
			log.error("Unable to save policy draft" + savePolicyDraft.getId(),
					e);
			throw new DirectorException("Unable to save policy draft ", e);
		}

		try {
			imagePersistenceManager.destroyPolicy(existingTrustPolicy);
		} catch (DbException e) {
			log.error("Cannoot delete policy", e);
			throw new DirectorException("Cannot delete policy draft y", e);
		}
		if (image_action_id != null && !"".equals(image_action_id)) {
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
		// TODO Auto-generated method stub
		try {
			ImageActionObject imageActionObject;
			ImageActionActions imageActionActions;
			log.debug("updateOrCreateImageAction imageStoreUploadRequest::"
					+ imageStoreUploadRequest);
			if (imageStoreUploadRequest.isCheck_image_action_id()
					&& !imageStoreUploadRequest.getImage_action_id().equals("") && imageStoreUploadRequest.getImage_action_id() != null) {
				

				imageActionObject = imagePersistenceManager
						.fetchImageActionById(imageStoreUploadRequest
								.getImage_action_id());

			} else {

			
				TrustPolicy policy = imagePersistenceManager
						.fetchPolicyForImage(imageStoreUploadRequest
								.getImage_id());
				String id = createImageActionById(
						imageStoreUploadRequest.getImage_id(),
						policy, false);
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
		String mountPath = DirectorUtil
				.getMountPath(searchFilesInImageRequest.id);
		return getFirstLevelFiles(
				mountPath + searchFilesInImageRequest.getDir(),
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
			// TODO Auto-generated catch block
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
			if (file.isDirectory()) {
				if (dirs != null) {
					dirs.add(file.getAbsolutePath().replace("\\", "/"));
				}
				getFilesAndDirectories(file.getAbsolutePath(), dirs, imageId);
			}
		}
		return files;
	}

	private Collection<File> getFilesAndDirectoriesWithFilter(
			SearchFilesInImageRequest searchFilesInImageRequest) {
		String mountPath = DirectorUtil
				.getMountPath(searchFilesInImageRequest.id);

		Collection<File> files = new HashSet<>();
		DirectoryAndFileUtil directoryAndFileUtil = new DirectoryAndFileUtil();
		DirectoryMeasurement dirMeasurement = new DirectoryMeasurement();
		dirMeasurement.setPath(mountPath + searchFilesInImageRequest.getDir());
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
			// TODO Auto-generated catch block
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
		String policy_id, display_name;
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
	public String getFilepathForImage(String imageId, boolean isModified) throws DbException {
		ImageInfo imageInfo = imagePersistenceManager.fetchImageById(imageId);
		if(isModified)
		{
			return imageInfo.getLocation() + "Modified_"+imageInfo.getName();
		}
		else
		{
			return imageInfo.getLocation() + imageInfo.getName();
		}
		
	}

	@Override
	public TrustPolicy getTrustPolicyByImageId(String imageId) throws DbException {
		String id = imagePersistenceManager.fetchImageById(imageId).getTrust_policy_id();
		return imagePersistenceManager.fetchPolicyById(id);
	}
}