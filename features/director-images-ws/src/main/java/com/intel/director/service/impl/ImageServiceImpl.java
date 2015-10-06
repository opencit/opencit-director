package com.intel.director.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.io.FilenameUtils;
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
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageInfoFilter;
import com.intel.director.common.Constants;
import com.intel.director.common.MountVMImage;
import com.intel.director.exception.ImageStoreException;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.images.exception.ImageMountException;
import com.intel.director.imagestore.ImageStoreManager;
import com.intel.director.service.ImageService;
import com.intel.director.util.DirectorUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;
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
	private final int MaxFileSize = 50 * 1024 * 1024 * 1024;
	private final int MaxMemSize = 4 * 1024;

	private List<File> files = null;

	@Autowired
	private IPersistService imagePersistenceManager;

	@Autowired
	private ImageStoreManager imageStoreManager;

	public ImageServiceImpl() {
		imagePersistenceManager = new DbServiceImpl();
	}

	@Override
	public MountImageResponse mountImage(String imageId, String user)
			throws DirectorException {
		MountImageResponse mountImageResponse = new MountImageResponse();
		ImageAttributes image;
		try {
			image = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException ex) {
			throw new DirectorException("No image found with id: " + imageId,
					ex);
		}
		// Check if the image is already mounted. If so, return error
		/*
		 * if (image.mounted_by_user_id != null) { throw new
		 * ImageMountException(
		 * "Unable to mount image. Image is already in use by user: " +
		 * image.mounted_by_user_id); }
		 */
		String mountPath = DirectorUtil.computeVMMountPath(image.id);

		try {
			// Mount the image
			/* MountVMImage.mountImage(image.location, mountPath); */
		} catch (Exception ex) {
			throw new DirectorException("Unable to mount image", ex);
		}

		// Mark the image mounted by the user
		try {
			// /image.mounted_by_user_id = user;
			image.setMounted_by_user_id(user);
			image.setLocation(Constants.vmImagesPath);
			imagePersistenceManager.updateImage(image);
			mountImageResponse = DirectorUtil
					.mapImageAttributesToMountImageResponse(image);
		} catch (DbException ex) {
			throw new DirectorException(
					"Unable to reset mounted_by_user_id field", ex);
		} finally {
			try {
				// unmount the image
				MountVMImage.unmountImage(mountPath);
			} catch (Exception ex1) {
				throw new DirectorException(
						"Failed to unmount image. The attempt was made after the DB update for mounted_by_user failed. ",
						ex1);
			}

		}

		return mountImageResponse;
	}

	@Override
	public UnmountImageResponse unMountImage(String imageId, String user)
			throws DirectorException {
		UnmountImageResponse unmountImageResponse = null;
		try {
			ImageAttributes image = imagePersistenceManager
					.fetchImageById(imageId);

			// Throw an exception if different user than the mounted_by user
			// tries to unmount
			if (user != null) {
				if (!image.mounted_by_user_id.equalsIgnoreCase(user)) {
					throw new DirectorException(
							"Image cannot be unmounted by a differnt user");
				}
			}

			/*
			 * String mountPath = DirectorUtil.computeVMMountPath(image.id);
			 * MountVMImage.unmountImage(mountPath);
			 */

			image.setMounted_by_user_id(null);
			imagePersistenceManager.updateImage(image);

			// Unmount the image

			unmountImageResponse = DirectorUtil
					.mapImageAttributesToUnMountImageResponse(image);
		} catch (Exception ex) {
			throw new ImageMountException("Unable to unmount image", ex);
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
			fetchImages = imagePersistenceManager.fetchImages(filter, null);
		}
		searchImagesResponse.images = fetchImages;
		return searchImagesResponse;
	}

	private List<File> getFiles(String sDir, Set<String> dirs) {
		if (files == null) {
			files = new ArrayList<>();
		}
		if (dirs != null) {
			dirs.add(sDir);
		}
		List<File> faFiles = Arrays.asList(new File(sDir).listFiles());
		for (File file : faFiles) {
			files.add(file);
			if (file.isDirectory()) {
				if (dirs != null) {
					dirs.add(file.getAbsolutePath().replace("\\", "/"));
				}
				getFiles(file.getAbsolutePath(), dirs);
			}
		}
		return files;
	}

	@Override
	public TrustDirectorImageUploadResponse uploadImageToTrustDirectorSingle(
			String image_deployments, String image_format,
			HttpServletRequest request) throws DirectorException {

		DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(MaxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File("c:/temp/"));

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum file size to be uploaded.
		upload.setSizeMax(MaxFileSize);
		File file = null;

		// Parse the request to get file items.

		try {
			List fileItems = upload.parseRequest(request);

			// Process the uploaded file items
			@SuppressWarnings("rawtypes")
			Iterator i = fileItems.iterator();
			String filePath = "C:/temp/";
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
			throw new DirectorException("Cannot write the uploaded file", e);
		}
		ImageAttributes imageAttributes = new ImageAttributes();
		imageAttributes.name = file.getName();
		imageAttributes.image_deployments = image_deployments;
		try {
			imageAttributes.location = file.getCanonicalPath();
		} catch (IOException e) {
			throw new DirectorException("Cannot get location of file", e);
		}
		imageAttributes.image_format = image_format;
		Date currentDate = new Date();
		imageAttributes.setCreated_date(currentDate);
		imageAttributes.setEdited_date(currentDate);
		try {
			imagePersistenceManager.saveImageMetadata(imageAttributes);
		} catch (DbException e) {
			throw new DirectorException("Cannot save image meta data", e);
		}
		return DirectorUtil
				.mapImageAttributesToTrustDirectorImageUploadResponse(imageAttributes);
	}

	@Override
	public SearchFilesInImageResponse searchFilesInImage(
			SearchFilesInImageRequest searchFilesInImageRequest)
			throws DirectorException {
		List<String> trustPolicyElementsList = new ArrayList<String>();
		Set<String> fileNames = new HashSet<String>();
		Set<String> patchFileAddSet = new HashSet<String>();
		Set<String> patchFileRemoveSet = new HashSet<String>();
		Set<String> patchDirAddSet = new HashSet<String>();
		Collection<File> treeFiles = new ArrayList<>();

		SearchFilesInImageResponse filesInImageResponse = new SearchFilesInImageResponse();
		File dir = new File(searchFilesInImageRequest.getDir());
		Collection<File> listFilesAndDirs = new ArrayList<File>(
				Arrays.asList(dir.listFiles()));

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
			treeFiles = getFiles(searchFilesInImageRequest.getDir(), null);
		} else {
			treeFiles = listFilesAndDirs;
		}

		// In case of edit mode we want to show the exploded view
		// so that the selected file is visible in the initial screen.
		// For that we use the directoryListContainingPolicyFiles to find the
		// directories
		// that contain that file/s

		// populate filenames with the leaf nodes of the root folder

		createListOfFileNamesForTree(treeFiles, fileNames,
				searchFilesInImageRequest.getDir());

		// Now add the exploded file view in case of edit
		Set<String> dirsForEdit = new HashSet<String>();

		for (String dirPath : directoryListContainingPolicyFiles.keySet()) {
			if (directoryListContainingPolicyFiles.get(dirPath)) {
				createListOfFileNamesForTree(getFiles(dirPath, dirsForEdit),
						fileNames, searchFilesInImageRequest.getDir());

			} else {
				createListOfFileNamesForTree(
						(Collection<File>) new ArrayList<File>(
								Arrays.asList(new File(dirPath).listFiles())),
						fileNames, searchFilesInImageRequest.getDir());
			}
		}

		String parent = searchFilesInImageRequest.getDir().replace("\\", "/");
		TreeNode root = new TreeNode(parent, parent);

		Tree tree = new Tree(root, searchFilesInImageRequest.recursive,
				searchFilesInImageRequest.filesForPolicy);
		root.parent = tree;

		// In case of regex, find the list of files and add it here and then set
		// it in
		if (searchFilesInImageRequest.include != null) {
			List<File> regexFiles = new ArrayList<File>();
			if (searchFilesInImageRequest.includeRecursive) {
				regexFiles = getFiles(searchFilesInImageRequest.getDir(), null);
			} else {
				regexFiles = new ArrayList<File>(Arrays.asList(new File(
						searchFilesInImageRequest.getDir()).listFiles()));
				;
			}
			trustPolicyElementsList = new ArrayList<String>();
			for (File file : regexFiles) {
				try {
					if (FilenameUtils.getExtension(file.getCanonicalPath())
							.endsWith(
									searchFilesInImageRequest.include.replace(
											"*.", ""))) {
						patchFileAddSet.add(file.getCanonicalPath().replace(
								"\\", "/"));
						if (!trustPolicyElementsList.contains(file
								.getCanonicalPath().replace("\\", "/"))) {
							trustPolicyElementsList.add(file.getCanonicalPath()
									.replace("\\", "/"));
						}
					}
				} catch (IOException ioe) {
					throw new DirectorException(
							"Exception while fetchinf files in regex", ioe);
				}
			}

			patchDirAddSet.add(searchFilesInImageRequest.getDir());
		}
		if (searchFilesInImageRequest.recursive) {
			filesInImageResponse.patchXML = new ArrayList<>();
			String parentDir = searchFilesInImageRequest.getDir();
			if (searchFilesInImageRequest.filesForPolicy) {
				// This means that the user has checked the parent directory
				// checkbox
				for (String fileName : fileNames) {
					if (new File(parentDir + fileName).isFile()) {
						patchFileAddSet.add(parentDir + fileName);
					}
					trustPolicyElementsList.add(parentDir + fileName);
				}
			} else {
				for (String fileName : fileNames) {
					if (new File(parentDir + fileName).isFile()) {
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
			 fetchPolicy = imagePersistenceManager
					.fetchPolicyById(trustId);
		///	policyXml = fetchPolicy.getTrust_policy();
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
			draft = DirectorUtil
					.patch(draft, trustpolicyDraftEditRequest.patch);
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
			// TODO Auto-generated catch block
			throw new DirectorException(e);
		} catch (JAXBException e) {
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

			} else {
				policyXml = trustPolicyDraft.getTrust_policy_draft();
				metadata.setImage_name(trustPolicyDraft.getImgAttributes()
						.getName());
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
				metadata.setIsEncrypted(true);
			}

		} catch (DbException e) {
			// TODO Auto-generated catch block
			throw new DirectorException(e);
		} catch (JAXBException e) {
			throw new DirectorException(e);
		}
		return metadata;

	}

	public ImageActionObject createTrustPolicy(String image_id)
			throws DirectorException {

		TrustPolicy existingTrustpolicy = null;
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

			/*
			 * TODO write signing code String signed_trust_policy=
			 * DirectorUtil.signPolicy(policyXml);
			 */

			String signed_trust_policy = policyXml;// / Need to be removed

			trustPolicy.setTrust_policy(signed_trust_policy);
			ImageAttributes imgAttrs = new ImageAttributes();
			imgAttrs.setId(image_id);
			trustPolicy.setImgAttributes(imgAttrs);
			imagePersistenceManager.savePolicy(trustPolicy);
			imagePersistenceManager.destroyPolicyDraft(existingDraft);

			// Creating an ImageAction
			return createImageActionById(image_id, policyXml, true);

		} catch (DbException e) {
			if (existingTrustpolicy != null) {
				// /TODO update archive column to false
			}
			throw new DirectorException(e);
		} finally {

		}

	}

	private ImageActionObject createImageActionById(String image_id,
			String existingPolicy, boolean isFlowUpload)
			throws DirectorException {
		// TODO Auto-generated method stub
		try {
			List<ImageActionActions> actions = new ArrayList<ImageActionActions>();
			ImageActionObject imageActionObject = new ImageActionObject();
			ImageActionObject createdImageActionObject = new ImageActionObject();
			ImageActionActions imageActionActions = new ImageActionActions();

			if (isFlowUpload
					&& DirectorUtil.isImageEncryptStatus(existingPolicy)) {
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
				imageActionObject.setImage_id(image_id);
				createdImageActionObject = imagePersistenceManager
						.createImageAction(imageActionObject);
			}

			return createdImageActionObject;
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
				if (existingPolicy == null) {
					TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
					trustPolicyDraft.setCreated_date(currentDate);

					trustPolicyDraft.setEdited_date(currentDate);

					String trust_policy_draft = DirectorUtil
							.generateInitialPolicyDraft(createTrustPolicyMetaDataRequest);
					trustPolicyDraft.setTrust_policy_draft(trust_policy_draft);

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
					TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
					trustPolicyDraft.setCreated_date(currentDate);

					trustPolicyDraft.setEdited_date(currentDate);
					String policyXml = existingPolicy.getTrust_policy();

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

				}
			} else {

				existingDraft.setEdited_date(currentDate);
				String existingTrustPolicyDraftxml = existingDraft
						.getTrust_policy_draft();
				String trust_policy_draft = DirectorUtil.updatePolicyDraft(
						existingTrustPolicyDraftxml,
						createTrustPolicyMetaDataRequest);
				existingDraft.setTrust_policy_draft(trust_policy_draft);

				ImageAttributes imgAttrs = new ImageAttributes();
				imgAttrs.setId(imageid);
				existingDraft.setImgAttributes(imgAttrs);
				imagePersistenceManager.updatePolicyDraft(existingDraft);

				createPolicyMetadataResponse.setId(existingDraft.getId());
				createPolicyMetadataResponse.setTrustPolicy(existingDraft
						.getTrust_policy_draft());

			}
		} catch (DbException e) {
			// TODO Auto-generated catch block
			throw new DirectorException(e);
		} catch (JAXBException e) {
			throw new DirectorException(e);
		}
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

	private void createListOfFileNamesForTree(Collection<File> treeFiles,
			Set<String> fileNames, String rootDir) {
		for (File file : treeFiles) {
			try {
				String _file = file.getCanonicalPath().replace("\\", "/")
						.replace(rootDir, "");
				fileNames.add(_file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
						trustPolicyElementsList.add(measurement.getPath());
						if (searchFilesInImageRequest.init) {
							if (measurement instanceof FileMeasurement) {
								DirectorUtil.getParentDirectory(
										measurement.getPath(),
										searchFilesInImageRequest.getDir(),
										directoryListContainingPolicyFiles,
										true);
							}
						}
					}
				}
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (DbException ex) {
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
					filesInImageResponse.patchXML
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
					filesInImageResponse.patchXML
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

				filesInImageResponse.patchXML.add("<add " + pos
						+ " sel='//*[local-name()=\"Whitelist\"]" + dirPath
						+ "'><File Path=\"" + patchFile + "\"/></add>");
			}
		}
	}

	public ImageStoreResponse uploadImageToImageStore(
			ImageStoreUploadRequest imageStoreUploadRequest)
			throws DirectorException {
		try {
			// Updating Or Creating ImageAction
			updateOrCreateImageAction(imageStoreUploadRequest);

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

	public ImageListResponse getImages(String deployment_type)
			throws DirectorException {

		List<ImageInfo> imageList;
		ImageListResponse imageListresponse = new ImageListResponse();

		try {
			imageList = imagePersistenceManager.fetchImages(null);
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
							+ imageInfo.getTrust_policy_id()
							+ "')\"></span></a>";
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
						+ imageInfo.getId() + "','" + imageInfo.getName()
						+ "','" + imageInfo.getTrust_policy_id()
						+ "')\" ></span></a>";

				imgResponse.setImage_upload(image_upload);

				imgResponse.setCreated_date(imageInfo.getCreated_date());

				imageListresponse.images.add(imgResponse);

			}
		} catch (DbException e) {

			throw new DirectorException(e);
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
			throw new DirectorException("Cannot fetch image by id", e);
		}
		TrustPolicy existingTrustPolicy;
		try {
			existingTrustPolicy = imagePersistenceManager
					.fetchPolicyById(imageInfo.getTrust_policy_id());
		} catch (DbException e) {
			throw new DirectorException("Cannot get policy by id", e);
		}
		TrustPolicyDraft trustPolicyDraft = trustPolicyToTrustPolicyDraft(existingTrustPolicy);
		try {
			imagePersistenceManager.destroyPolicy(existingTrustPolicy);
		} catch (DbException e) {
			throw new DirectorException(
					"Cannot delete policy draft before createing a signed policy",
					e);
		}
		if (image_action_id != null) {
			try {
				imagePersistenceManager.deleteImageActionById(image_action_id);
			} catch (DbException e) {
				throw new DirectorException("Cannot delete image action : "
						+ image_action_id, e);
			}
		}

		TrustPolicyDraft savePolicyDraft = null;
		try {
			savePolicyDraft = imagePersistenceManager
					.savePolicyDraft(trustPolicyDraft);
		} catch (DbException e) {
			throw new DirectorException("Unable to save policy draft ", e);
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
		return trustPolicyDraft;
	}

	private void updateOrCreateImageAction(
			ImageStoreUploadRequest imageStoreUploadRequest)
			throws DirectorException {
		// TODO Auto-generated method stub
		try {
			ImageActionObject imageActionObject;
			ImageActionActions imageActionActions;
			if (imageStoreUploadRequest.isCheck_image_action_id()
					&& imageStoreUploadRequest.getImage_action_id() != null) {
				// Stepwise Upload

				imageActionObject = imagePersistenceManager
						.fetchImageActionById(imageStoreUploadRequest
								.getImage_action_id());

			} else {

				// Direct Upload
				imageActionObject = createImageActionById(
						imageStoreUploadRequest.getImage_id(), null, false);

			}

			List<ImageActionActions> taskList = imageActionObject.getAction();
			if (taskList == null) {
				taskList = new ArrayList<ImageActionActions>();
			}

			// User has selected as tarball
			if (!imageStoreUploadRequest.getStore_name_for_tarball_upload()
					.equals("0")) {
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
				if (!imageStoreUploadRequest.getStore_name_for_image_upload()
						.equals("0")) {
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
				if (!imageStoreUploadRequest.getStore_name_for_policy_upload()
						.equals("0")) {
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
			
			if(taskList.size()>0){
			imageActionObject.setImage_id(imageStoreUploadRequest.getImage_id());
			imageActionObject.setAction(taskList);
			if(imageActionObject.getCurrent_task_name()==null){
				ImageActionActions acts=taskList.get(0);
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

}