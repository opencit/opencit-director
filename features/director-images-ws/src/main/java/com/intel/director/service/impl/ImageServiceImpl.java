package com.intel.director.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageStoreManager;
import com.intel.director.api.ImageStoreResponse;
import com.intel.director.api.ImageStoreUploadRequest;
import com.intel.director.api.MountImageResponse;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.TrustDirectorImageUploadRequest;
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
import com.intel.director.service.ImageService;
import com.intel.director.util.DirectorUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;
import com.intel.mtwilson.trustpolicy.xml.DirectoryMeasurement;
import com.intel.mtwilson.trustpolicy.xml.FileMeasurement;
import com.intel.mtwilson.trustpolicy.xml.LaunchControlPolicy;
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
			throws ImageMountException {
		MountImageResponse mountImageResponse = null;
		ImageAttributes image;
		try {
			image = imagePersistenceManager.fetchImageById(imageId);
		} catch (DbException ex) {
			throw new ImageMountException("No image found with id: " + imageId,
					ex);
		}
		// Check if the image is already mounted. If so, return error
		if (image.mounted_by_user_id != null) {
			throw new ImageMountException(
					"Unable to mount image. Image is already in use by user: "
							+ image.mounted_by_user_id);
		}
		String mountPath = DirectorUtil.computeVMMountPath(image.id);

		try {
			// Mount the image
			MountVMImage.mountImage(image.location, mountPath);
			mountImageResponse = DirectorUtil
					.mapImageAttributesToMountImageResponse(image);
		} catch (Exception ex) {
			throw new ImageMountException("Unable to mount image", ex);
		}

		// Mark the image mounted by the user
		try {
			image.mounted_by_user_id = user;
			imagePersistenceManager.updateImage(image);
			mountImageResponse.mounted_by_user_id = user;
		} catch (DbException ex) {
			try {
				// unmount the image
				MountVMImage.unmountImage(mountPath);
			} catch (Exception ex1) {
				throw new ImageMountException(
						"Failed to unmoubt image. The attempt was made after the DB update for mounted_by_user failed. ",
						ex1);
			}

		}

		return mountImageResponse;
	}

	@Override
	public UnmountImageResponse unMountImage(String imageId, String user)
			throws ImageMountException {
		UnmountImageResponse unmountImageResponse = null;
		try {
			ImageAttributes image = imagePersistenceManager
					.fetchImageById(imageId);

			// Throw an exception if different user than the mounted_by user
			// tries to unmount
			if (!image.mounted_by_user_id.equalsIgnoreCase(user)) {
				throw new ImageMountException(
						"Image cannot be unmounted by a differnt user");
			}

			// Mark the image unmounted
			image.mounted_by_user_id = null;
			imagePersistenceManager.updateImage(image);

			// Unmount the image
			String mountPath = DirectorUtil.computeVMMountPath(image.id);
			MountVMImage.unmountImage(mountPath);
			unmountImageResponse = DirectorUtil
					.mapImageAttributesToUnMountImageResponse(image);
		} catch (Exception ex) {
			throw new ImageMountException("Unable to unmount image", ex);
		}
		return unmountImageResponse;
	}

	@Override
	public TrustDirectorImageUploadResponse uploadImageMetaDataToTrustDirector(
			TrustDirectorImageUploadRequest trustDirectorImageUploadRequest)
			throws DbException {
		// Check if the file with the same name has been uploaded earlier
		// If so, append "_1" to the file name and then save
		File image = new File(Constants.vmImagesPath
				+ trustDirectorImageUploadRequest.imageAttributes.name);
		if (image.exists()) {
			trustDirectorImageUploadRequest.imageAttributes.name += "_1";
		}

		// Save image meta data to the database
		ImageAttributes imageAttributes = imagePersistenceManager
				.saveImageMetadata(trustDirectorImageUploadRequest.imageAttributes);
		return DirectorUtil
				.mapImageAttributesToTrustDirectorImageUploadResponse(imageAttributes);
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
	public TrustDirectorImageUploadResponse uploadImageToTrustDirector(
			String imageId, HttpServletRequest request) throws DbException,
			IOException {

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

		String parameter = request.getParameter("imgFile");
		System.out.println("Image file : " + parameter);

		// Parse the request to get file items.

		try {
			List fileItems = upload.parseRequest(request);

			// Process the uploaded file items
			@SuppressWarnings("rawtypes")
			Iterator i = fileItems.iterator();
			String filePath = "C:/temp/";
			while (i.hasNext()) {
				FileItem fi = (FileItem) i.next();
				// System.out.print(fi);
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
					// System.out.print(fi);
				}
			}
		} catch (Exception e) {
			throw new IOException("Cannot write the uploaded file", e);
		}

		// Get the image details saved in the earlier step
		ImageAttributes imageAttributes = imagePersistenceManager
				.fetchImageById(imageId);
		imageAttributes.location = file.getCanonicalPath();

		// Write image to file

		// Save image meta data to the database
		imagePersistenceManager.updateImage(imageAttributes);

		return DirectorUtil
				.mapImageAttributesToTrustDirectorImageUploadResponse(imageAttributes);
	}

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
	public SearchFilesInImageResponse searchFilesInImage(
			SearchFilesInImageRequest searchFilesInImageRequest) {
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
			trustPolicyElementsList = new ArrayList<String>();
			patchDirAddSet.add(searchFilesInImageRequest.getDir());
			patchFileAddSet.add("C:/Temp/Test/TEST_File.txt");
			patchFileAddSet
					.add("C:/Temp/Test/AChild2/AChild2_1/AChild2_1_file.txt");
			trustPolicyElementsList.add("C:/Temp/Test/TEST_File.txt");
			trustPolicyElementsList
					.add("C:/Temp/Test/AChild2/AChild2_1/AChild2_1_file.txt");
		}
		if (searchFilesInImageRequest.recursive) {
			filesInImageResponse.patchXML = new ArrayList<>();
			StringBuilder builder = new StringBuilder();
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
			metadata.setLaunch_control_policy(policy.getLaunchControlPolicy().value());
			// /metadata.setSelected_image_format(selected_image_format);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			throw new DirectorException(e);
		} catch (JAXBException e) {
			throw new DirectorException(e);
		}
		return metadata;
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

				createPolicyMetadataResponse.setId(policyDraftCreated.getId());
				createPolicyMetadataResponse.setTrustPolicy(policyDraftCreated
						.getTrust_policy_draft());
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
		if (!searchFilesInImageRequest.init) {
			trustPolicyElementsList = null;
			return;
		}

		// Fetch the files from the draft
		TrustPolicyDraft trustPolicyDraft = null;
		try {
			trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(searchFilesInImageRequest.id);
			JAXBContext jaxbContext;

			try {
				jaxbContext = JAXBContext.newInstance(TrustPolicy.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				StringReader reader = new StringReader(
						trustPolicyDraft.getTrust_policy_draft());
				com.intel.mtwilson.trustpolicy.xml.TrustPolicy trustPolicyDraftObj = (com.intel.mtwilson.trustpolicy.xml.TrustPolicy) unmarshaller
						.unmarshal(reader);
				if (trustPolicyDraftObj.getWhitelist().getMeasurements().size() > 0) {
					for (Measurement measurement : trustPolicyDraftObj
							.getWhitelist().getMeasurements()) {
						trustPolicyElementsList.add(measurement.getPath());
						if (measurement instanceof FileMeasurement) {
							DirectorUtil.getParentDirectory(
									measurement.getPath(),
									searchFilesInImageRequest.getDir(),
									directoryListContainingPolicyFiles, true);
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

			jaxbContext = JAXBContext.newInstance(TrustPolicy.class);
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
				}else{
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
		File trustPolicyFile = null;
		ImageStoreResponse imgResponse = null;
		try {
			ImageInfo imageInfo = imagePersistenceManager
					.fetchImageById(imageStoreUploadRequest.getImage_id());

			String diskFormat = null, containerFormat = null;

			switch (imageInfo.image_format) {
			case "ami":
				diskFormat = "ami";
				containerFormat = "ami";
				break;
			case "qcow2":
				diskFormat = "qcow2";
				containerFormat = "bare";
				break;
			case "vhd":
				diskFormat = "vhd";
				containerFormat = "bare";
				break;
			case "raw":
				diskFormat = "raw";
				containerFormat = "bare";
				break;
			}

			Map<String, String> imageProperties = new HashMap<>();
			imageProperties.put(Constants.NAME, "test_upload");
			imageProperties.put(Constants.DISK_FORMAT, diskFormat);
			imageProperties.put(Constants.CONTAINER_FORMAT, containerFormat);
			imageProperties.put(Constants.IS_PUBLIC, "true");

			String imageLocation = imageInfo.getLocation();

			TrustPolicy tp = imagePersistenceManager
					.fetchPolicyForImage(imageStoreUploadRequest.getImage_id());
			if (imageStoreUploadRequest.getStore_name_for_tarball_upload() != null
					|| imageStoreUploadRequest
							.getStore_name_for_policy_upload() != null) {
				if (tp != null) {
					if (tp.getName() == null) {
						tp.setName("upload_policy_" + tp.getId());
					}
					trustPolicyFile = new File(tp.getName());

					// if file doesnt exists, then create it
					if (!trustPolicyFile.exists()) {
						trustPolicyFile.createNewFile();
					}

					FileWriter fw = new FileWriter(
							trustPolicyFile.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(tp.getTrust_policy());
					bw.close();
				}
			}
			if (imageStoreUploadRequest.getStore_name_for_tarball_upload() != null
					&& !"".equalsIgnoreCase(imageStoreUploadRequest
							.getStore_name_for_tarball_upload())) {
				// ////TODO:- Persistence layer call to get className
				String className = "GlanceImageStoreManager.java";
				ImageStoreManager imgStoremanager = getImageStoreImpl(className);

				String tarballLocation = DirectorUtil
						.createImageTrustPolicyTar(
								trustPolicyFile.getAbsolutePath(),
								imageLocation);
				File tarballfile = new File(tarballLocation);
				imgStoremanager.upload(tarballfile, imageProperties);

			} else {

				if (imageStoreUploadRequest.getStore_name_for_policy_upload() != null
						&& !"".equalsIgnoreCase(imageStoreUploadRequest
								.getStore_name_for_policy_upload())) {
					// ////TODO:- Persistence layer call to get className
					String className = "GlanceImageStoreManager.java";
					ImageStoreManager imgStoremanager = getImageStoreImpl(className);
					imgStoremanager.upload(trustPolicyFile, imageProperties);

					// System.out.println("Done writing to " + fileName); //For
					// testing

				}

				if (imageStoreUploadRequest.getStore_name_for_image_upload() != null
						&& !"".equalsIgnoreCase(imageStoreUploadRequest
								.getStore_name_for_image_upload())) {

					// ////TODO:- Persistence layer call to get className
					String className = "GlanceImageStoreManager.java";
					ImageStoreManager imgStoremanager = getImageStoreImpl(className);
					File imageFile = new File(imageLocation);
					//imgStoremanager.upload(imageFile, imageProperties);
				}

			}

		} catch (Exception e) {
			throw new DirectorException(e);
		} finally {
			if (trustPolicyFile != null && trustPolicyFile.exists()) {
				trustPolicyFile.delete();
			}
		}
		return imgResponse;
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

}
