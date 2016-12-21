package com.intel.director.service.impl;

import com.intel.director.api.CreateTrustPolicyMetaDataRequest;
import com.intel.director.api.CreateTrustPolicyMetaDataResponse;
import com.intel.director.api.HashTypeObject;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageInfoDetailedResponse;
import com.intel.director.api.ImageInfoResponse;
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
import com.intel.director.api.SshSettingResponse;
import com.intel.director.api.TrustDirectorImageUploadResponse;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftEditRequest;
import com.intel.director.api.TrustPolicyResponse;
import com.intel.director.api.UnmountImageResponse;
import com.intel.director.api.UpdateTrustPolicyRequest;
import com.intel.director.api.UpgradeTrustPolicyResponse;
import com.intel.director.api.ui.ImageActionFields;
import com.intel.director.api.ui.ImageActionFilter;
import com.intel.director.api.ui.ImageActionOrderBy;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageInfoFilter;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.TreeElement;
import com.intel.director.api.ui.TrustPolicyDraftFilter;
import com.intel.director.api.ui.TrustPolicyDraftResponse;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.DockerUtil;
import com.intel.director.common.FileUtilityOperation;
import com.intel.director.common.ImageDeploymentHashTypeCache;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.exception.ImageStoreException;
import com.intel.director.images.async.DirectorAsynchExecutor;
import com.intel.director.images.async.DockerPullTask;
import com.intel.director.images.mount.MountService;
import com.intel.director.images.mount.MountServiceFactory;
import com.intel.director.service.DockerActionService;
import com.intel.director.service.ImageService;
import com.intel.director.service.TrustPolicyService;
import com.intel.director.store.StoreManager;
import com.intel.director.util.TdaasUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;
import com.intel.mtwilson.director.trust.policy.DirectoryAndFileUtil;
import com.intel.mtwilson.manifest2.xml.DirectoryMeasurementType;
import com.intel.mtwilson.manifest2.xml.FileMeasurementType;
import com.intel.mtwilson.manifest2.xml.Manifest;
import com.intel.mtwilson.manifest2.xml.MeasurementType;
import com.intel.mtwilson.shiro.ShiroUtil;
import com.intel.mtwilson.trustpolicy2.xml.DirectoryMeasurement;
import com.intel.mtwilson.trustpolicy2.xml.FileMeasurement;
import com.intel.mtwilson.trustpolicy2.xml.Measurement;
import com.intel.mtwilson.trustpolicy2.xml.SymlinkMeasurement;
import com.intel.mtwilson.util.exec.EscapeUtil;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImageServiceImpl.class);

    private List<File> files = null;

    private IPersistService imagePersistenceManager;

    private DockerActionService dockerActionService;

    public ImageServiceImpl() {
	imagePersistenceManager = new DbServiceImpl();
	dockerActionService = new DockerActionImpl();
    }

    public ImageInfo fetchImageById(String imageId) throws DirectorException {
	ImageInfo image;
	try {
	    image = imagePersistenceManager.fetchImageById(imageId);
	} catch (DbException ex) {
	    log.error("Error in mounting image  ", ex);
	    throw new DirectorException("No image found with id: " + imageId, ex);
	}
	return image;
    }

    @Override
    public MountImageResponse mountImage(String imageId, String backendUser) throws DirectorException {
	String loggedInUser = ShiroUtil.subjectUsername();
	if (StringUtils.isBlank(loggedInUser)) {
	    loggedInUser = backendUser;
	    log.info("Setting user for mounting to {}", loggedInUser);
	}

	log.info("inside mounting image in service");
	log.info("***** Logged in user : " + ShiroUtil.subjectUsername());
	ImageAttributes image;
	try {
	    image = imagePersistenceManager.fetchImageById(imageId);
	    if (image == null) {
		throw new DirectorException("No image found for id " + imageId);
	    }
	} catch (Exception ex) {
	    log.error("Error in mounting image  ", ex);
	    throw new DirectorException("No image found with id: " + imageId, ex);
	}

	if (image.deleted || (image.status != null && !image.status.equals(Constants.COMPLETE))) {
	    String msg = "Cannot launch an image marked as deleted or incomplete image " + imageId;
	    log.error(msg);
	    throw new DirectorException(msg);
	}

	MountImageResponse mountImageResponse = TdaasUtil.mapImageAttributesToMountImageResponse(image);

	// Check if the image is already mounted. If so, return error

	if (image.mounted_by_user_id != null && !image.mounted_by_user_id.equals(ShiroUtil.subjectUsername())) {
	    log.info("Image already mounted by user : " + image.mounted_by_user_id);
	    throw new DirectorException(
		    "Unable to mount image. Image is already in use by user: " + image.mounted_by_user_id);

	}

	if (image.mounted_by_user_id != null && image.mounted_by_user_id.equals(ShiroUtil.subjectUsername())) {
	    File f = new File(TdaasUtil.getMountPath(imageId));
	    if (f.exists()) {
		log.info("Not mounting image again");
		return mountImageResponse;
	    }
	}

	MountService mountService = MountServiceFactory.getMountService(image);
	mountService.mount();

	// Mark the image mounted by the user
	try {
	    image.setMounted_by_user_id(loggedInUser);
	    Calendar currentDate = Calendar.getInstance();
	    image.setEdited_date(currentDate);
	    image.setEdited_by_user_id(ShiroUtil.subjectUsername());
	    imagePersistenceManager.updateImage(image);
	    mountImageResponse = TdaasUtil.mapImageAttributesToMountImageResponse(image);

	    log.info("Update mounted_by_user = {} for image in DB for image at location : " + image.getLocation(),
		    loggedInUser);
	} catch (DbException ex) {
	    log.error("Error while saving mount data to database: " + ex.getMessage());
	    mountService.unmount();
	    throw new DirectorException("Unable to reset mounted_by_user_id field", ex);
	}
	log.debug("Image mounted succesfully imageId:" + imageId);
	return mountImageResponse;
    }

    @Override
    public UnmountImageResponse unMountImage(String imageId, String user) throws DirectorException {
	log.info("inside unmounting image in service");
	UnmountImageResponse unmountImageResponse = null;
	ImageAttributes image;
	try {
	    image = imagePersistenceManager.fetchImageById(imageId);
	} catch (DbException e) {
	    log.error("Error fetchign image " + imageId, e);
	    throw new DirectorException("Error fetching image:" + imageId);
	}
	log.info("Unmounting image : " + image.id + " with name : " + image.image_name);

	MountService mountService = MountServiceFactory.getMountService(image);
	mountService.unmount();

	image.setMounted_by_user_id(null);
	Calendar currentDate = Calendar.getInstance();
	image.setEdited_date(currentDate);
	image.setEdited_by_user_id(ShiroUtil.subjectUsername());
	try {
	    imagePersistenceManager.updateImage(image);
	    unmountImageResponse = TdaasUtil.mapImageAttributesToUnMountImageResponse(image);

	} catch (DbException e) {
	    log.error("Error updating image " + imageId, e);
	    throw new DirectorException("Error updating image:" + imageId);
	}
	return unmountImageResponse;
    }

    /**
     * 
     * @param searchImagesRequest
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */

    @Override
    public SearchImagesResponse searchImages(SearchImagesRequest searchImagesRequest) throws DirectorException {
	SearchImagesResponse searchImagesResponse = new SearchImagesResponse();
	try {
	    List<ImageInfo> fetchImages;
	    List<ImageInfoDetailedResponse> imageInfoDetailedResponseList = new ArrayList<ImageInfoDetailedResponse>();
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
	    if (fetchImages != null) {
		for (int i = 0; i < fetchImages.size(); i++) {
		    fetchImages.get(i).setPolicy_name(getDisplayNameForImage(fetchImages.get(i).id));
		    Mapper mapper = new DozerBeanMapper();
		    ImageInfoDetailedResponse imageInfoDetailedResponse = mapper.map(fetchImages.get(i),
			    ImageInfoDetailedResponse.class);
		    imageInfoDetailedResponse.setCreated_date(fetchImages.get(i).getCreated_date());
		    imageInfoDetailedResponse
			    .setActionEntryCreated(checkNonObsoleteActionEntryCreated(fetchImages.get(i).getId()));
		    imageInfoDetailedResponseList.add(imageInfoDetailedResponse);

		}
	    }

	    searchImagesResponse.images = imageInfoDetailedResponseList;
	} catch (DbException de) {
	    log.error("Error while retrieving list of images", de);
	    throw new DirectorException("Error while retrieving list of images", de);
	}
	return searchImagesResponse;
    }

    private boolean checkNonObsoleteActionEntryCreated(String imageId) {
	List<ImageActionObject> imageActionObjectList = new ArrayList<ImageActionObject>();
	ImageActionFilter imageActionFilter = new ImageActionFilter();
	ImageActionOrderBy imageActionOrderBy = new ImageActionOrderBy();
	imageActionOrderBy.setImageActionFields(ImageActionFields.DATE);
	imageActionOrderBy.setOrderBy(OrderByEnum.DESC);
	imageActionFilter.setImage_id(imageId);

	try {
	    imageActionObjectList = imagePersistenceManager.fetchImageActions(imageActionFilter, imageActionOrderBy);
	} catch (DbException e) {
	    log.error("unable to fetch image upload entries , checkActionEntryCreated : " + imageActionFilter);

	}
	if (imageActionObjectList != null && imageActionObjectList.size() > 0) {
	    for (ImageActionObject imageActionObject : imageActionObjectList) {
		if (!imageActionObject.getCurrent_task_status().equalsIgnoreCase(Constants.OBSOLETE)) {
		    return true;
		}
	    }

	}
	return false;
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
    public TrustDirectorImageUploadResponse createUploadImageMetadataImpl(String image_deployments, String image_format,
	    String fileName, long fileSize, String repository, String tag) throws DirectorException {

	String loggedinUser = ShiroUtil.subjectUsername();

	ImageAttributes imageAttributes = new ImageAttributes();
	imageAttributes.image_name = fileName;
	imageAttributes.image_deployments = image_deployments;
	imageAttributes.setCreated_by_user_id(loggedinUser);
	imageAttributes.setEdited_by_user_id(loggedinUser);
	imageAttributes.setStatus(Constants.IN_PROGRESS);
	imageAttributes.setDeleted(false);
	long sizeInBytes = fileSize;
	imageAttributes.setSent(0L);
	imageAttributes.setImage_size(sizeInBytes);
	imageAttributes.location = Constants.defaultUploadPath;
	if (image_deployments != null && image_deployments.equals(Constants.DEPLOYMENT_TYPE_DOCKER)) {
	    imageAttributes.location = Constants.DEFAULT_DOCKER_UPLOAD_PATH;
	} else if (image_deployments != null && image_deployments.equals(Constants.DEPLOYMENT_TYPE_VM)) {
	    imageAttributes.location = Constants.DEFAULT_VM_UPLOAD_PATH;
	}
	imageAttributes.image_format = image_format;
	Calendar currentDate = Calendar.getInstance();
	imageAttributes.setCreated_date(currentDate);
	imageAttributes.setEdited_date(currentDate);
	imageAttributes.setEdited_by_user_id(ShiroUtil.subjectUsername());
	imageAttributes.setCreated_by_user_id(ShiroUtil.subjectUsername());
	if (image_deployments.equalsIgnoreCase(Constants.DEPLOYMENT_TYPE_DOCKER)) {
	    imageAttributes.setRepository(repository);
	    imageAttributes.setTag(tag);
	}

	ImageAttributes createdImageMetadata;
	try {
	    log.debug("Saving metadata of uploaded file");
	    createdImageMetadata = imagePersistenceManager.saveImageMetadata(imageAttributes);
	    log.info("Image is hidden :: " + createdImageMetadata.isDeleted());
	} catch (DbException e) {
	    log.error("Error while saving metadata for uploaded file : " + e.getMessage());
	    throw new DirectorException("Cannot save image meta data", e);
	}
	log.info("Saved Image upload metadata.");

	return TdaasUtil.mapImageAttributesToTrustDirectorImageUploadResponse(createdImageMetadata);
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
    public TrustDirectorImageUploadResponse uploadImageToTrustDirector(String image_id, InputStream fileInputStream)
	    throws DirectorException {
	double oneMB = 1024 * 1024;
	double flushSize = oneMB * 10; // 10MB
	ImageInfo imageInfo;
	try {
	    imageInfo = imagePersistenceManager.fetchImageById(image_id);
	} catch (DbException e) {
	    log.error("Error while getting metadata for the image with id " + image_id + ": " + e.getMessage());
	    throw new DirectorException("Failed to load image metdata", e);
	}

	if (imageInfo == null) {
	    log.error("image with id " + image_id + "does not exist");
	    throw new DirectorException("image with id " + image_id + "does not exist");

	}
	log.info("Obtained image upload metadata for the id " + image_id + " and imageName "
		+ imageInfo.getImage_name());

	if (imageInfo.getStatus().equals(Constants.COMPLETE)) {
	    TrustDirectorImageUploadResponse trustDirectorImageUploadResponse = new TrustDirectorImageUploadResponse();
	    trustDirectorImageUploadResponse.setStatus("Error: given image has already been uploaded");
	    return trustDirectorImageUploadResponse;
	}
	// Update the status of image to IN PROGRESS to mark the upload process
	// initiation
	if (imageInfo.getSent().intValue() == 0) {
	    imageInfo.setStatus(Constants.IN_PROGRESS);
	    try {
		imagePersistenceManager.updateImage(imageInfo);
	    } catch (DbException e) {
		log.error("Error while updating metadata for uploaded image : " + e.getMessage());
	    }
	}
	int bytesread = 0;
	OutputStream out = null;
	/// We append every chunk to file
	try {
	    int read;
	    byte[] bytes = new byte[1024];

	    out = new FileOutputStream(new File(imageInfo.getLocation() + imageInfo.getImage_name()), true);
	    double bufferForFlush = 0;
	    while ((read = fileInputStream.read(bytes)) != -1) {
		bytesread += read;
		bufferForFlush += read;
		out.write(bytes, 0, read);
		if (bufferForFlush >= flushSize) { // flush after 10MB
		    bufferForFlush = 0;
		    out.flush();
		}
	    }
	} catch (IOException e) {
	    log.error("Error while writing uploaded image: " + e.getMessage());
	    throw new DirectorException("Cannot write the uploaded image", e);
	} finally {
	    if (out != null) {
		try {
		    out.flush();
		} catch (IOException e) {
		    log.error("Unable to close file output stream while uploading image to TD", e);
		} finally {
		    try {
			out.close();
		    } catch (IOException e) {
			log.error("Unable to close file output stream while uploading image to TD", e);
		    }
		}
	    }
	    try {
		fileInputStream.close();
	    } catch (IOException e) {
		log.error("Error in closing stream: ", e);
	    }
	}
	Calendar edited_date = Calendar.getInstance();
	imageInfo.setSent(imageInfo.getSent() + bytesread);
	imageInfo.setEdited_date(edited_date);
	log.debug("Sent in bytes New: " + imageInfo.getSent());
	log.debug("image size: " + imageInfo.getImage_size());
	if (imageInfo.getSent().intValue() == imageInfo.getImage_size().intValue()) {
	    imageInfo.setStatus(Constants.COMPLETE);
	    imageInfo.setDeleted(false);
	    log.info("Image upload COMPLETE..");
	} else {
	    imageInfo.setStatus(Constants.IN_PROGRESS);
	}
	try {
	    imagePersistenceManager.updateImage(imageInfo);
	    log.debug("Image is hidden :: " + imageInfo.isDeleted());
	} catch (DbException e) {
	    log.error("Error while updating metadata for uploaded image : " + e.getMessage());
	    throw new DirectorException("Cannot update image meta data", e);
	}
	log.debug("Updated Image upload metadata.");

	return TdaasUtil.mapImageAttributesToTrustDirectorImageUploadResponse(imageInfo);
    }

    
    
    public UpgradeTrustPolicyResponse upgradePolicyForImage(String imageId)
			throws DirectorException {
		UpgradeTrustPolicyResponse resp = new UpgradeTrustPolicyResponse();
		String policyDraftXml = null;
		log.debug("Inside upgradePolicyForImage imageId:" + imageId);
		String user = ShiroUtil.subjectUsername();
		String mountPath = TdaasUtil.getMountPath(imageId);
		TrustPolicy existingTrustPolicy = null;
		TrustPolicyDraft trustPolicyDraft = null;
		try {
			trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(imageId);
		} catch (DbException e1) {
			throw new DirectorException("Unable to fetch policy draft", e1);
		}
		String draftContent = null;
		String draftId = null;
		if (trustPolicyDraft == null) {
			ImageInfo imageInfo;
			try {
				imageInfo = imagePersistenceManager.fetchImageById(imageId);
			} catch (DbException e) {
				log.error("Cannot fetch imageid imageId::" + imageId, e);
				throw new DirectorException("Cannot fetch image by id", e);
			}
			if (imageInfo == null) {
				throw new DirectorException("Image does not exist");
			}

			try {
				existingTrustPolicy = imagePersistenceManager
						.fetchPolicyById(imageInfo.getTrust_policy_id());
			} catch (DbException e) {
				log.error("Cannot get policy by id existingTrustPolicyId::"
						+ imageInfo.getTrust_policy_id(), e);
				throw new DirectorException("Cannot get policy by id", e);
			}
			draftContent = existingTrustPolicy.getTrust_policy();

		} else {
			draftId = trustPolicyDraft.getId();
			draftContent = trustPolicyDraft.getTrust_policy_draft();
		}

		String policyVersion = null;
		try {
			policyVersion = TdaasUtil.getPolicyVersion(draftContent);
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			throw new DirectorException("Unable to get policy version", e1);
		}
		log.info("#####  policyVersion" + policyVersion);
		if (StringUtils.isNotBlank(policyVersion)) {
			if (!(Constants.TRUST_POLICY_VERSION_2.equals(policyVersion) || Constants.TRUST_POLICY_VERSION_1.equals(policyVersion))) {
				throw new DirectorException(
						"Unidentified policy version , policyVersion:"
								+ policyVersion);
			}
		}

		if (Constants.TRUST_POLICY_VERSION_1.equals(policyVersion)) {

			try {

				mountImage(imageId, user);
				com.intel.mtwilson.trustpolicy1.xml.TrustPolicy trustPolicy1DraftObj = null;
				try {
					trustPolicy1DraftObj = TdaasUtil.getPolicy1(draftContent);
				} catch (JAXBException e) {
					throw new DirectorException(
							"Unable to get unmarshal policy draft", e);
				}
				// /com.intel.mtwilson.trustpolicy2.xml.TrustPolicy
				// trustPolicy2DraftObj= new
				// com.intel.mtwilson.trustpolicy2.xml.TrustPolicy();
				log.debug("Before mapper");
				Mapper mapper = new DozerBeanMapper();
				com.intel.mtwilson.trustpolicy2.xml.TrustPolicy trustPolicy2DraftObj = mapper
						.map(trustPolicy1DraftObj,
								com.intel.mtwilson.trustpolicy2.xml.TrustPolicy.class);
				if(trustPolicy2DraftObj.getWhitelist()!=null){
				log.debug(" trustPolicy2DraftObj before clearing measurments: "
						+ trustPolicy2DraftObj.getWhitelist().getMeasurements()
								.toString());
				trustPolicy2DraftObj.getWhitelist().getMeasurements().clear();

				if (trustPolicy1DraftObj.getWhitelist().getMeasurements()
						.size() > 0) {
					for (com.intel.mtwilson.trustpolicy1.xml.Measurement policy1Measurement : trustPolicy1DraftObj
							.getWhitelist().getMeasurements()) {

						if (policy1Measurement instanceof com.intel.mtwilson.trustpolicy1.xml.FileMeasurement) {
							log.debug("file upgradation element: "
									+ policy1Measurement.getPath());
							String elementPath = mountPath
									+ policy1Measurement.getPath();
							Path path = Paths.get(elementPath);
							if (Files.isSymbolicLink(path)) {
								log.debug("Element is Symlink path: {}", path);
								com.intel.mtwilson.trustpolicy2.xml.SymlinkMeasurement symlinkMeasurement = new com.intel.mtwilson.trustpolicy2.xml.SymlinkMeasurement();
								symlinkMeasurement.setPath(policy1Measurement
										.getPath());
								trustPolicy2DraftObj.getWhitelist()
										.getMeasurements()
										.add(symlinkMeasurement);
							} else {
								com.intel.mtwilson.trustpolicy2.xml.FileMeasurement fileMeasurement = new com.intel.mtwilson.trustpolicy2.xml.FileMeasurement();
								fileMeasurement.setPath(policy1Measurement
										.getPath());
								trustPolicy2DraftObj.getWhitelist()
										.getMeasurements().add(fileMeasurement);
							}

						}

						if (policy1Measurement instanceof com.intel.mtwilson.trustpolicy1.xml.DirectoryMeasurement) {
							log.info("Dir upgradation element: "
									+ policy1Measurement.getPath());

							Collection<File> regexDirectories = new HashSet<>();
							com.intel.mtwilson.trustpolicy1.xml.DirectoryMeasurement oldPolicyDirMesurment = (com.intel.mtwilson.trustpolicy1.xml.DirectoryMeasurement) policy1Measurement;
							// / Upgrading Dir as per 1.2
							log.debug(" oldPolicyDirMesurment.isRecursive().booleanValue():"
									+ oldPolicyDirMesurment.isRecursive()
											.booleanValue());
							if ((oldPolicyDirMesurment.isRecursive()
									.booleanValue())) {

								log.info("Is  Recursive");
								regexDirectories = getAllSubDirectories(
										mountPath, policy1Measurement.getPath());
							}
							regexDirectories.add(new File(mountPath
									+ policy1Measurement.getPath()));
							log.info("######### All Regex directories: "
									+ regexDirectories);
							for (File file : regexDirectories) {

								if (file.isDirectory()) {

									log.debug("######### file.getAbsolutePath() "
											+ file.getAbsolutePath());
									String newPolicyDirPath = file
											.getAbsolutePath().replace(
													mountPath, "");

									com.intel.mtwilson.trustpolicy2.xml.DirectoryMeasurement dirMeasurement = new com.intel.mtwilson.trustpolicy2.xml.DirectoryMeasurement();
									dirMeasurement
											.setExclude(oldPolicyDirMesurment
													.getExclude());
									dirMeasurement
											.setInclude(oldPolicyDirMesurment
													.getInclude());
									dirMeasurement.setFilterType("regex");
									dirMeasurement.setPath(newPolicyDirPath);
									trustPolicy2DraftObj.getWhitelist()
											.getMeasurements()
											.add(dirMeasurement);

								}

							}

						}
					}

				}
				}

				try {
					policyDraftXml = TdaasUtil
							.getPolicyXml(trustPolicy2DraftObj);
				} catch (JAXBException e) {
					throw new DirectorException(
							"Unable to get nmarshal policy draft", e);
				}

				try {

					if (trustPolicyDraft == null) {
						TrustPolicyDraft createTrustPolicyDraft = new TrustPolicyDraft();
						createTrustPolicyDraft.setCreated_date(Calendar
								.getInstance());
						createTrustPolicyDraft.setEdited_date(Calendar
								.getInstance());
						createTrustPolicyDraft.setEdited_by_user_id(user);
						createTrustPolicyDraft.setCreated_by_user_id(user);

						createTrustPolicyDraft.setName(existingTrustPolicy
								.getName());
						createTrustPolicyDraft
								.setImgAttributes(existingTrustPolicy
										.getImgAttributes());
						createTrustPolicyDraft
								.setTrust_policy_draft(policyDraftXml);
						createTrustPolicyDraft.setDisplay_name(TdaasUtil
								.getVersionedName(existingTrustPolicy
										.getDisplay_name()));
						createTrustPolicyDraft
								.setTrust_policy_draft(policyDraftXml);
						TrustPolicyDraft savePolicyDraft = imagePersistenceManager
								.savePolicyDraft(createTrustPolicyDraft);
						draftId = savePolicyDraft.getId();
					} else {
						trustPolicyDraft
								.setCreated_date(Calendar.getInstance());
						trustPolicyDraft.setEdited_date(Calendar.getInstance());
						trustPolicyDraft.setEdited_by_user_id(user);
						trustPolicyDraft.setCreated_by_user_id(user);
						trustPolicyDraft.setTrust_policy_draft(policyDraftXml);
						imagePersistenceManager
								.updatePolicyDraft(trustPolicyDraft);

					}
					log.debug("trustPolicy2DraftObj final: " + policyDraftXml);

				} catch (DbException e) {
					throw new DirectorException(
							"Unable to update policy draft", e);
				}
				resp.status = "success";
				resp.message = "Policy Draft Succesfully upgraded to 1.2";
				resp.policyDraft = policyDraftXml;
				resp.draftId = draftId;

			} finally {
				unMountImage(imageId, user);
			}

		} else {
			resp.status = "success";
			resp.message = "Policy Draft already of version 1.2, no action done";
			resp.policyDraft = draftContent;
			resp.draftId = draftId;
			log.info(" Policy is of 1.2 version");
		}

		return resp;
	}
    
    
	@Override
	public SearchFilesInImageResponse searchFilesInImage(
			SearchFilesInImageRequest searchFilesInImageRequest)
			throws DirectorException {
		searchFilesInImageRequest.setDir(EscapeUtil
				.doubleQuoteEscapeShellArgument(searchFilesInImageRequest.getDir()));
		log.info("Browsing files for dir: {}", searchFilesInImageRequest.getDir());
		String mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);
		log.info("Browsing files for on image mounted at : {}", mountPath);

		SearchFilesInImageResponse filesInImageResponse = new SearchFilesInImageResponse();
		Collection<File> listFilesAndDirs = getFirstLevelFiles(searchFilesInImageRequest);
		List<Measurement> measurements = getMeasurements(searchFilesInImageRequest.id);
		Map<String, Boolean> directoryListContainingPolicyFiles = new HashMap<>();
		Set<TreeNodeDetail> directoryListContainingRegex = new HashSet<>();
		Set<String> preSelectedPaths = new HashSet<>();
		Set<String> trustPolicyElementsList = new HashSet<>();
		Set<String> fileNames = new HashSet<>();
		Set<String> patchFileAddSet = new HashSet<>();
		Set<String> patchFileRemoveSet = new HashSet<>();
		Set<String> patchDirAddSet = new HashSet<>();
		Set<String> patchDirRemoveSet = new HashSet<>();
		Set<String> patchSymLinkAddSet = new HashSet<>();
		Set<String> patchSymLinkRemoveSet = new HashSet<>();
		Collection<File> treeFiles = new ArrayList<>();
		/* Init will read trust policy draft file to get whitelist directories
		 and files needs to be checked/regex purpose. Useful in scenarios like edit
		 mode*/
		if (searchFilesInImageRequest.init) {
			try {
				TrustPolicyDraft fetchPolicyDraftForImage = imagePersistenceManager
						.fetchPolicyDraftForImage(searchFilesInImageRequest.id);
				fetchPolicyDraftForImage.setEdited_date(Calendar.getInstance());
				imagePersistenceManager
						.updatePolicyDraft(fetchPolicyDraftForImage);
				/*
				 * init will populate :- directoryListContainingPolicyFiles: all
				 * directories for a policy file item till root. Only for
				 * FileMeasurement . Used in edit mode.
				 * directoryListContainingRegex:- all directory in
				 * DirectoryMeasurement
				 */
				init(trustPolicyElementsList,
						directoryListContainingPolicyFiles,
						directoryListContainingRegex, preSelectedPaths, searchFilesInImageRequest);
			} catch (DbException e) {
				log.error("Error updating policy draft for image {}",
						searchFilesInImageRequest.id, e);
			}
		} else{
			populateTrustPolicyElements(trustPolicyElementsList, searchFilesInImageRequest);
		}

		log.info("after init");

		// Fetch the files
		if (searchFilesInImageRequest.recursive) {
			treeFiles = getFilesAndDirectories(
					searchFilesInImageRequest.getDir(), null,
					searchFilesInImageRequest.id);
		} else {
			if (listFilesAndDirs != null) {
				treeFiles = listFilesAndDirs;
			}
		}

		/*
		 * populate filenames with the leaf nodes of the root folder treefiles
		 * has fetched files from tree structure to be opened without
		 * considering edit scenario files(with or without recursion depending
		 * on recursion flag) Add files which are present in treefile but not in
		 * directoryListContainingPolicyFiles to fileNames
		 */
		createListOfFileNamesForTree(searchFilesInImageRequest, treeFiles,
				fileNames, directoryListContainingPolicyFiles.keySet());
		log.info("create list of file name for tree");

		// Now add the exploded file view in case of edit to filenames
		Set<String> dirsForEdit = new HashSet<String>();

		for (String dirPath : directoryListContainingPolicyFiles.keySet()) {
			log.debug("calc tree  files for dir : {}", dirPath);

			if (directoryListContainingPolicyFiles.get(dirPath)) {
				/*
				 * filenames is a set not list. filesAndDirectories currently
				 * being fetched has has all files and directories of basic tree
				 * structure(without edit) and those to be opened in edit mode.
				 * TODO:- Refactor getFilesAndDirectoriesForEdit which is using
				 * common file list with createListOfFileNamesForTree function
				 * to return. It should only return filesorDirectories in edit
				 * mode . As it being used in nextline to get filenames which is
				 * a set so there is no duplication in filenames which we will
				 * be using later to build patch and create
				 * trustpolicyelelementlist
				 */
				List<File> filesAndDirectories = getFilesAndDirectoriesForEdit(
						dirPath, dirsForEdit, searchFilesInImageRequest.id);
				createListOfFileNamesForTree(searchFilesInImageRequest,
						filesAndDirectories, fileNames,
						directoryListContainingPolicyFiles.keySet());
				log.info("calc tree files in recursive edit mode");
			}
		}

		/* Regex/wildcard file search */
		if(!StringUtils.isEmpty(searchFilesInImageRequest.include) ||
				!StringUtils.isEmpty(searchFilesInImageRequest.exclude)) {
			/* If Request is to reset regex/wildcard recursively */
			if (searchFilesInImageRequest.recursive && searchFilesInImageRequest.reset_regex) {
				createRemovalPatches(searchFilesInImageRequest, mountPath, trustPolicyElementsList,
						patchFileRemoveSet, patchSymLinkRemoveSet, patchDirRemoveSet);
			}

			/* If Request is to reset regex/wildcard for single directory */
			if (!searchFilesInImageRequest.recursive && searchFilesInImageRequest.reset_regex) {
				createRemovalPatches(searchFilesInImageRequest, mountPath, trustPolicyElementsList,
						patchFileRemoveSet, patchSymLinkRemoveSet, patchDirRemoveSet);
			}
			/* If Request is to apply regex/wildcard recursively */
			if (searchFilesInImageRequest.recursive && !searchFilesInImageRequest.reset_regex) {
				createAdditionPatches(searchFilesInImageRequest, mountPath, trustPolicyElementsList,
						patchFileAddSet, patchSymLinkAddSet, patchDirAddSet);
			}

			/* If Request is to apply regex/wildcard for single directory */
			if (!searchFilesInImageRequest.recursive && !searchFilesInImageRequest.reset_regex) {
				createAdditionPatches(searchFilesInImageRequest, mountPath, trustPolicyElementsList,
						patchFileAddSet, patchSymLinkAddSet, patchDirAddSet);
			}
		}

		/* Selected/Deselected Directory request. Select/Deselect all files under directory */
		if (searchFilesInImageRequest.recursive && searchFilesInImageRequest.init) {
			filesInImageResponse.patchXml = new ArrayList<>();
			/*
			 * files_for_policy(true):- when checkbox is checked on directory
			 * Recursive is true in two cases:- 1. Select All or deselect all 2.
			 * Regex applied
			 */
			if ( searchFilesInImageRequest.files_for_policy) {
				// This means that the user has checked the parent directory checkbox
				createAdditionPatches(searchFilesInImageRequest, mountPath, trustPolicyElementsList,
						patchFileAddSet, patchSymLinkAddSet, patchDirAddSet);
			} else {
				createRemovalPatches(searchFilesInImageRequest, mountPath, trustPolicyElementsList,
						patchFileRemoveSet, patchSymLinkRemoveSet, patchDirRemoveSet);
			}
		}
		Tree tree = new Tree(searchFilesInImageRequest, mountPath,
				directoryListContainingRegex, trustPolicyElementsList,
				dirsForEdit, fileNames);
		TreeElement treeCreated = tree.createTree();
		buildPatch(patchDirAddSet, patchDirRemoveSet, patchFileAddSet, patchFileRemoveSet,
				patchSymLinkAddSet, patchSymLinkRemoveSet,
				measurements, searchFilesInImageRequest, filesInImageResponse);

		filesInImageResponse.setGeneratedTree(treeCreated);
		return filesInImageResponse;
	}

	private void createRemovalPatches(SearchFilesInImageRequest searchFilesInImageRequest,
			String mountPath,	Set<String> trustPolicyElementsList, Set<String> patchFileRemoveSet,
			Set<String> patchSymLinkRemoveSet, Set<String> patchDirRemoveSet){
		Collection<File> regexFiles = getFilesAndDirectoriesWithFilter(searchFilesInImageRequest);
		for (File rFile : regexFiles) {
			if(trustPolicyElementsList.contains(rFile.getAbsolutePath().replace(mountPath, ""))) {
				if (Files.isSymbolicLink(rFile.toPath())) {
					patchSymLinkRemoveSet.add(rFile.getAbsolutePath().replace(mountPath, ""));
				}
				if (rFile.isFile()) {
					patchFileRemoveSet.add(rFile.getAbsolutePath().replace(mountPath, ""));
				}
				if (rFile.isDirectory() && !Files.isSymbolicLink(rFile.toPath())) {
					patchDirRemoveSet.add(rFile.getAbsolutePath().replace(mountPath, ""));
				}
				trustPolicyElementsList.remove(rFile.getAbsolutePath().replace(mountPath, ""));
			}
		}
		if(trustPolicyElementsList.contains(searchFilesInImageRequest.getDir())) {
			patchDirRemoveSet.add(searchFilesInImageRequest.getDir());
		}
	}

	private void createAdditionPatches(SearchFilesInImageRequest searchFilesInImageRequest,
			String mountPath,	Set<String> trustPolicyElementsList, Set<String> patchFileAddSet,
			Set<String> patchSymLinkAddSet, Set<String> patchDirAddSet){
		Collection<File> regexFiles = getFilesAndDirectoriesWithFilter(searchFilesInImageRequest);
		for (File rFile : regexFiles) {
			if (!trustPolicyElementsList.contains(rFile.getAbsolutePath().replace(mountPath, ""))) {
				if (Files.isSymbolicLink(rFile.toPath())) {
					patchSymLinkAddSet.add(rFile.getAbsolutePath().replace(mountPath, ""));
				}
				if (rFile.isFile()) {
					patchFileAddSet.add(rFile.getAbsolutePath().replace(mountPath, ""));
				}
				if (rFile.isDirectory() && !Files.isSymbolicLink(rFile.toPath())) {
					patchDirAddSet.add(rFile.getAbsolutePath().replace(mountPath, ""));
				}
				trustPolicyElementsList.add(rFile.getAbsolutePath().replace(mountPath, ""));
			}
		}
		if (!trustPolicyElementsList.contains(searchFilesInImageRequest.getDir())) {
			patchDirAddSet.add(searchFilesInImageRequest.getDir());
		}
	}

    /**
     * 
     * @param imageId
     * @return
     * @throws DirectorException
     * @throws ImageStoreException
     */

    @Override
    public String getTrustPolicyForImage(String imageId) {
	String policyDraftXml = null;
	try {
	    TrustPolicyDraft fetchPolicyDraftForImage = imagePersistenceManager.fetchPolicyDraftForImage(imageId);
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
    public TrustPolicyDraftResponse editTrustPolicyDraft(TrustPolicyDraftEditRequest trustpolicyDraftEditRequest)
	    throws DirectorException {
	TrustPolicyDraft trustPolicyDraft;
	try {
	    trustPolicyDraft = imagePersistenceManager
		    .fetchPolicyDraftById(trustpolicyDraftEditRequest.trust_policy_draft_id);
	} catch (DbException e) {
	    log.error("Error in editTrustPolicyDraft()", e);
	    throw new DirectorException("Error in fetching policy draft", e);
	}

	if (trustPolicyDraft == null) {
	    log.error("No trust Policy draft exist for draft id " + trustpolicyDraftEditRequest.trust_policy_draft_id);
	    throw new DirectorException(
		    "No trust Policy draft exist for draft id " + trustpolicyDraftEditRequest.trust_policy_draft_id);
	}

	String draft = trustPolicyDraft.getTrust_policy_draft();

	draft = TdaasUtil.patch(draft, trustpolicyDraftEditRequest.patch);
	trustPolicyDraft.setEdited_date(Calendar.getInstance());
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
	Mapper mapper = new DozerBeanMapper();
	TrustPolicyDraftResponse trustPolicyDraftResponse = mapper.map(trustPolicyDraft,
		TrustPolicyDraftResponse.class);
	return trustPolicyDraftResponse;
    }

    public CreateTrustPolicyMetaDataResponse getPolicyMetadata(String draftid) throws DirectorException {
	CreateTrustPolicyMetaDataResponse metadata = new CreateTrustPolicyMetaDataResponse();
	try {
	    TrustPolicyDraft trustPolicyDraft = imagePersistenceManager.fetchPolicyDraftById(draftid);
	    if (trustPolicyDraft == null) {
		return null;
	    }
	    String trustPolicyDraftxml = trustPolicyDraft.getTrust_policy_draft();
	    
			String policyVersion = null;

			policyVersion = TdaasUtil.getPolicyVersion(trustPolicyDraftxml);

			JAXBContext jaxbContext = null;
			if (Constants.TRUST_POLICY_VERSION_1.equals(policyVersion)) {

				com.intel.mtwilson.trustpolicy1.xml.TrustPolicy policy = TdaasUtil
						.getPolicy1(trustPolicyDraftxml);
				metadata.setImage_name(trustPolicyDraft.getImgAttributes()
						.getImage_name());
				metadata.setDisplay_name(trustPolicyDraft.getDisplay_name());
				if (policy.getEncryption() != null) {
					metadata.setEncrypted(true);
				}
				// /metadata.setIsEncrypted(isEncrypted);
				metadata.setLaunch_control_policy(policy
						.getLaunchControlPolicy().value());
			} else {

				com.intel.mtwilson.trustpolicy2.xml.TrustPolicy policy = TdaasUtil
						.getPolicy(trustPolicyDraftxml);
				metadata.setImage_name(trustPolicyDraft.getImgAttributes()
						.getImage_name());
				metadata.setDisplay_name(trustPolicyDraft.getDisplay_name());
				if (policy.getEncryption() != null) {
					metadata.setEncrypted(true);
				}
				// /metadata.setIsEncrypted(isEncrypted);
				metadata.setLaunch_control_policy(policy
						.getLaunchControlPolicy().value());
			}
	     
	    metadata.setTrustPolicy(trustPolicyDraftxml);
	    // /metadata.setSelected_image_format(selected_image_format);
	} catch (DbException e) {
	    String errorMsg = "Unable to get policy metadata draftid::" + draftid;
	    log.error(errorMsg, e);
	    throw new DirectorException(errorMsg, e);
	} catch (ParserConfigurationException | SAXException | IOException|JAXBException e) {
	    String errorMsg = "Unable to get policy metadata draftid::" + draftid;
	    log.error(errorMsg, e);
	    throw new DirectorException(errorMsg, e);
	}
	
	return metadata;
    }

    public CreateTrustPolicyMetaDataResponse getPolicyMetadataForImage(String image_id) throws DirectorException {

	CreateTrustPolicyMetaDataResponse metadata = new CreateTrustPolicyMetaDataResponse();
	try {

	    com.intel.mtwilson.trustpolicy2.xml.TrustPolicy policy;
	    String policyXml;
	    TrustPolicyDraft trustPolicyDraft = imagePersistenceManager.fetchPolicyDraftForImage(image_id);
	    if (trustPolicyDraft == null) {
		TrustPolicy trustPolicy = imagePersistenceManager.fetchActivePolicyForImage(image_id);
		if (trustPolicy == null) {
		    throw new DirectorException("Neither Policy draft nor Polci exists do not exist");
		}
		policyXml = trustPolicy.getTrust_policy();
		metadata.setImage_name(trustPolicy.getImgAttributes().getImage_name());
		metadata.setDisplay_name(trustPolicy.getDisplay_name());
		metadata.setTrustPolicy(policyXml);
	    } else {
		policyXml = trustPolicyDraft.getTrust_policy_draft();
		metadata.setImage_name(trustPolicyDraft.getImgAttributes().getImage_name());
		metadata.setDisplay_name(trustPolicyDraft.getDisplay_name());
		metadata.setTrustPolicy(policyXml);
	    }

	    policy = TdaasUtil.getPolicy(policyXml);

	    // /metadata.setIsEncrypted(isEncrypted);
	    metadata.setLaunch_control_policy(policy.getLaunchControlPolicy().value());
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

    public String fetchImageIdByDraftOrPolicy(String draftOrPolicyId) {
	String imageId = null;
	TrustPolicyDraft existingDraft = null;
	try {
	    existingDraft = imagePersistenceManager.fetchPolicyDraftById(draftOrPolicyId);
	} catch (DbException e) {
	    log.error("Unable to fetch draft for draft id::", draftOrPolicyId);
	}
	if (existingDraft == null) {
	    TrustPolicy existingPolicy = null;
	    try {
		existingPolicy = imagePersistenceManager.fetchPolicyById(draftOrPolicyId);
	    } catch (DbException e) {
		log.error("Unable to fetch draft for policy id::", draftOrPolicyId);
	    }
	    if (existingPolicy != null) {

		imageId = existingPolicy.getImgAttributes().getId();

	    }
	} else {
	    imageId = existingDraft.getImgAttributes().getId();

	}
	return imageId;
    }

    public String createTrustPolicy(String draftOrTrustPolicyId) throws DirectorException {

	ImageInfo image;
	String imageId = null;
	TrustPolicyDraft existingDraft;
	TrustPolicy existingPolicy;
	String policyXml = null;

	boolean policyFound = true;
	// We first check does draft exist with that id and if it exists we get
	// policyXml from draft
	try {
	    existingDraft = imagePersistenceManager.fetchPolicyDraftById(draftOrTrustPolicyId);
	    if (existingDraft == null) {
		policyFound = false;
	    } else {
		imageId = existingDraft.getImgAttributes().getId();
		policyXml = existingDraft.getTrust_policy_draft();
		policyFound = true;
	    }
	} catch (DbException e1) {
	    log.error("Unable to fetch draft for draft id::", draftOrTrustPolicyId);
	    policyFound = false;
	}

	if (!policyFound) {
	    // We draft does not exist with that id we check policy exists and
	    // if it exists we get policyXml from policy
	    try {
		existingPolicy = imagePersistenceManager.fetchPolicyById(draftOrTrustPolicyId);
		if (existingPolicy == null) {
		    policyFound = false;
		} else {
		    imageId = existingPolicy.getImgAttributes().getId();
		    policyXml = existingPolicy.getTrust_policy();
		    policyFound = true;
		}
	    } catch (DbException e1) {
		log.error("Unable to fetch policy for  id::", draftOrTrustPolicyId);
		policyFound = false;
	    }
	}

	if (!policyFound) {
	    throw new DirectorException("no policy found for the id " + draftOrTrustPolicyId);
	}

	try {
	    image = imagePersistenceManager.fetchImageById(imageId);
	} catch (DbException ex) {
	    log.error("Error in fetching image  ", ex);
	    throw new DirectorException("No image found with id: " + imageId, ex);
	}

	TrustPolicyService trustPolicyService = new TrustPolicyServiceImpl(image.id);

	com.intel.mtwilson.trustpolicy2.xml.TrustPolicy policy;
	// Get TrustPolicy from policyXml
	try {
	    policy = TdaasUtil.getPolicy(policyXml);
	} catch (JAXBException e1) {
	    throw new DirectorException("Unable to convert policy xml into object");
	}

	// Calculate file, dir and cumulative hashes and add to encryption tag
	trustPolicyService.calculateHashes(policy);

	// Calculate image hash inside encryption tag
	trustPolicyService.addEncryption(policy);

	if (policy.getImage() != null && policy.getImage().getImageId() == null) {
	    policy.getImage().setImageId(imageId);
	}
	log.debug("### Inside createTrustPolicy method policy xml insert uuid::" + policy.getImage().getImageId());

	try {
	    policyXml = TdaasUtil.convertTrustPolicyToString(policy);
	} catch (JAXBException e) {
	    log.error("Unable to convert policy object to string", e);
	    throw new DirectorException("Unable to convert policy object to string", e);
	}
	log.info("Convert policy in string format");

	// Sign the policy with MtWilson
	String signedPolicyXml = trustPolicyService.signTrustPolicy(policyXml);

	// in case of live host, add the file to the host
	trustPolicyService.copyTrustPolicyAndManifestToHost(signedPolicyXml);

	// Save the policy to DB
	TrustPolicy trustPolicy = trustPolicyService.archiveAndSaveTrustPolicy(signedPolicyXml);

	/// Setting uploadVariabelMD5 in MwImage table
	String dekUrl = DirectorUtil.fetchDekUrl(trustPolicy);
	String uploadVariableMD5 = DirectorUtil.computeUploadVar(policy.getImage().getImageId(), dekUrl);
	image.setUploadVariableMD5(uploadVariableMD5);
	try {
	    imagePersistenceManager.updateImage(image);
	} catch (DbException e) {
	    log.error("Unable to update uploadVariableMD5 in image with id:" + image.getId(), e);
	    throw new DirectorException("Unable to update uploadVariableMD5 in image with id:" + image.getId(), e);
	}

	return trustPolicy.getId();
    }

    public CreateTrustPolicyMetaDataResponse saveTrustPolicyMetaData(
	    CreateTrustPolicyMetaDataRequest createTrustPolicyMetaDataRequest) throws DirectorException {
	CreateTrustPolicyMetaDataResponse createPolicyMetadataResponse = new CreateTrustPolicyMetaDataResponse();
	try {
	    String imageid = createTrustPolicyMetaDataRequest.getImage_id();
	    ImageAttributes img = imagePersistenceManager.fetchImageById(imageid);
	    if (Constants.DEPLOYMENT_TYPE_BAREMETAL.equals(img.getImage_deployments())) {
		TdaasUtil.checkInstalledComponents(imageid);
	    }

	    Calendar currentDate = Calendar.getInstance();

	    TrustPolicyDraft existingDraft = imagePersistenceManager.fetchPolicyDraftForImage(imageid);

	    if (doesPolicyNameExist(createTrustPolicyMetaDataRequest.getDisplay_name(), imageid)) {
		throw new DirectorException("Policy Name Already Exists");
	    }
	    if (existingDraft == null) {
		TrustPolicy existingPolicy = imagePersistenceManager.fetchActivePolicyForImage(imageid);
		ImageInfo imageInfo = imagePersistenceManager.fetchImageById(imageid);

		if (existingPolicy == null) {
		    TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
		    trustPolicyDraft.setCreated_date(currentDate);

		    trustPolicyDraft.setEdited_date(currentDate);
		    createTrustPolicyMetaDataRequest.setDeploymentType(imageInfo.image_deployments);
		    String trust_policy_draft = TdaasUtil.generateInitialPolicyDraft(createTrustPolicyMetaDataRequest);
		    trustPolicyDraft.setTrust_policy_draft(trust_policy_draft);
		    trustPolicyDraft.setDisplay_name(createTrustPolicyMetaDataRequest.getDisplay_name());

		    TrustPolicyDraft policyDraftCreated;
		    ImageAttributes imgAttrs = new ImageAttributes();
		    imgAttrs.setId(imageid);
		    trustPolicyDraft.setImgAttributes(imgAttrs);
		    trustPolicyDraft.setCreated_date(Calendar.getInstance());
		    trustPolicyDraft.setEdited_date(Calendar.getInstance());
		    trustPolicyDraft.setEdited_by_user_id(ShiroUtil.subjectUsername());
		    trustPolicyDraft.setCreated_by_user_id(ShiroUtil.subjectUsername());

		    policyDraftCreated = imagePersistenceManager.savePolicyDraft(trustPolicyDraft);

		    createPolicyMetadataResponse.setId(policyDraftCreated.getId());
		    createPolicyMetadataResponse.setTrustPolicy(policyDraftCreated.getTrust_policy_draft());
		    createPolicyMetadataResponse.status = Constants.SUCCESS;
		} else {
		    // Signed policy exists. We need to copy it over sans the
		    // signature and hashes

		    log.debug("policy draft already exists");
		    TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
		    trustPolicyDraft.setCreated_date(currentDate);

		    trustPolicyDraft.setEdited_date(currentDate);
		    String policyXml = existingPolicy.getTrust_policy();
		    trustPolicyDraft.setDisplay_name(createTrustPolicyMetaDataRequest.getDisplay_name());

		    com.intel.mtwilson.trustpolicy2.xml.TrustPolicy policy = TdaasUtil.getPolicy(policyXml);
		    policy.setSignature(null);
		    List<Measurement> measurements = policy.getWhitelist().getMeasurements();
		    for (Measurement measurement : measurements) {
			measurement.setValue(null);
		    }

		    policyXml = TdaasUtil.convertTrustPolicyToString(policy);

		    String unsigned_trust_policy = policyXml;// / Need to be
		    // removed

		    unsigned_trust_policy = TdaasUtil.updatePolicyDraft(unsigned_trust_policy,
			    createTrustPolicyMetaDataRequest);
		    trustPolicyDraft.setTrust_policy_draft(unsigned_trust_policy);

		    ImageAttributes imgAttrs = new ImageAttributes();
		    imgAttrs.setId(imageid);
		    trustPolicyDraft.setImgAttributes(imgAttrs);
		    TrustPolicyDraft policyDraftCreated;
		    trustPolicyDraft.setCreated_date(Calendar.getInstance());
		    trustPolicyDraft.setEdited_date(Calendar.getInstance());
		    trustPolicyDraft.setEdited_by_user_id(ShiroUtil.subjectUsername());
		    trustPolicyDraft.setCreated_by_user_id(ShiroUtil.subjectUsername());

		    policyDraftCreated = imagePersistenceManager.savePolicyDraft(trustPolicyDraft);
		    log.debug("policy draft created from existing policy, policyDraftCreatedId::"
			    + policyDraftCreated.getId());
		    createPolicyMetadataResponse.setId(policyDraftCreated.getId());
		    createPolicyMetadataResponse.setTrustPolicy(policyDraftCreated.getTrust_policy_draft());

		}

	    } else {

		existingDraft.setEdited_date(currentDate);
		String existingTrustPolicyDraftxml = existingDraft.getTrust_policy_draft();
		String trust_policy_draft = TdaasUtil.updatePolicyDraft(existingTrustPolicyDraftxml,
			createTrustPolicyMetaDataRequest);
		existingDraft.setTrust_policy_draft(trust_policy_draft);
		existingDraft.setDisplay_name(createTrustPolicyMetaDataRequest.getDisplay_name());

		ImageAttributes imgAttrs = new ImageAttributes();
		imgAttrs.setId(imageid);
		existingDraft.setImgAttributes(imgAttrs);
		existingDraft.setEdited_date(Calendar.getInstance());
		existingDraft.setEdited_by_user_id(ShiroUtil.subjectUsername());
		imagePersistenceManager.updatePolicyDraft(existingDraft);

		createPolicyMetadataResponse.setId(existingDraft.getId());
		createPolicyMetadataResponse.setTrustPolicy(existingDraft.getTrust_policy_draft());
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
     * Setters and getters
     * 
     * @param searchFilesInImageRequest
     * @param treeFiles
		 * @param fileNames
     * @return
     */

    private void createListOfFileNamesForTree(SearchFilesInImageRequest searchFilesInImageRequest,
	    Collection<File> treeFiles, Set<String> fileNames) {
	String mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);
   
	for (File file : treeFiles) {
	    String _file = file.getAbsolutePath().replace("\\", "/").replace(mountPath, "");
	    _file = _file.replaceFirst(searchFilesInImageRequest.getDir(), "");
	    fileNames.add(_file);
	}
    }

	private void createListOfFileNamesForTree(SearchFilesInImageRequest searchFilesInImageRequest,
			Collection<File> treeFiles, Set<String> fileNames,
			Set<String> directoryListContainingPolicyFiles) {
		String mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);
		List<String> directoryList = new ArrayList<String>();
		directoryList.addAll(directoryListContainingPolicyFiles);
		Collections.sort(directoryList);
		for (File file : treeFiles) {
			String _file = file.getAbsolutePath().replace(mountPath, "");
			_file = _file.replaceFirst(searchFilesInImageRequest.getDir(), "");
			////if (!directoryListContainingPolicyFiles.contains(mountPath + File.separator + _file)) {
			//String filePath = Paths.get(searchFilesInImageRequest.dir, _file).toString();
			//int index = Collections.binarySearch(directoryList, filePath);
			//if (index < 0) {
				fileNames.add(_file);
			//}
		}
	}

	private void populateTrustPolicyElements(Set<String> trustPolicyElementsList,
			SearchFilesInImageRequest searchFilesInImageRequest) {
		try {
			TrustPolicyDraft trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(searchFilesInImageRequest.id);
				com.intel.mtwilson.trustpolicy2.xml.TrustPolicy trustPolicyDraftObj = TdaasUtil
						.getPolicy(trustPolicyDraft.getTrust_policy_draft());
				if (trustPolicyDraftObj.getWhitelist().getMeasurements().size() > 0) {
					for (Measurement measurement : trustPolicyDraftObj.getWhitelist().getMeasurements()) {
						trustPolicyElementsList.add(measurement.getPath());
					}
				}
		} catch (JAXBException  | DbException ex) {
			log.error("Error while fetching current policy draft ", ex);
		}
	}

	private void init(Set<String> trustPolicyElementsList,
			Map<String, Boolean> directoryListContainingPolicyFiles,
			Set<TreeNodeDetail> directoryListContainingRegex, Set<String> preSelectedPaths,
			SearchFilesInImageRequest searchFilesInImageRequest) {
		/*
		 * if (!searchFilesInImageRequest.init) { trustPolicyElementsList =
		 * null; return; }
	 	 */
		// Fetch the files from the draft
		String mountPath = TdaasUtil.getMountPath(searchFilesInImageRequest.id);
		try {
			TrustPolicyDraft trustPolicyDraft = imagePersistenceManager
					.fetchPolicyDraftForImage(searchFilesInImageRequest.id);

			try {
				com.intel.mtwilson.trustpolicy2.xml.TrustPolicy trustPolicyDraftObj = TdaasUtil
						.getPolicy(trustPolicyDraft.getTrust_policy_draft());
				if (trustPolicyDraftObj.getWhitelist().getMeasurements().size() > 0) {
					for (Measurement measurement : trustPolicyDraftObj.getWhitelist().getMeasurements()) {
						log.info(measurement.getPath() + " **** " + searchFilesInImageRequest.getDir());
						trustPolicyElementsList.add(measurement.getPath());
						if (searchFilesInImageRequest.init
								&& measurement.getPath().startsWith(searchFilesInImageRequest.getDir())) {
							if (measurement instanceof FileMeasurement) {
								TdaasUtil.getParentDirectory(searchFilesInImageRequest.id,
										mountPath + measurement.getPath(),
										mountPath + searchFilesInImageRequest.getDir(),
										directoryListContainingPolicyFiles, true);
								preSelectedPaths.add(measurement.getPath());
							}
							if (measurement instanceof SymlinkMeasurement) {
								preSelectedPaths.add(measurement.getPath());
							}
							if (measurement instanceof DirectoryMeasurement) {
								TreeNodeDetail detail = new TreeNodeDetail();
								detail.regexPath = measurement.getPath();
								detail.regexExclude = ((DirectoryMeasurement) measurement).getExclude();
								detail.regexInclude = ((DirectoryMeasurement) measurement).getInclude();
								detail.filterType = ((DirectoryMeasurement) measurement).getFilterType();
								detail.isRegexRecursive = searchFilesInImageRequest.recursive;
								directoryListContainingRegex.add(detail);
								preSelectedPaths.add(measurement.getPath());
							}
						}
					}
				}
			} catch (JAXBException e) {
				log.error("Error while reading JAXB creating file list from policy draft  ", e);
			}
		} catch (DbException ex) {
			log.error("Error while fetching current policy draft ", ex);
		}
	}

    private List<Measurement> getMeasurements(String imageID) {
	try {
	    TrustPolicyDraft trustPolicyDraft = imagePersistenceManager.fetchPolicyDraftForImage(imageID);
	    com.intel.mtwilson.trustpolicy2.xml.TrustPolicy trustPolicyDraftObj = TdaasUtil
		    .getPolicy(trustPolicyDraft.getTrust_policy_draft());
	    if (trustPolicyDraftObj.getWhitelist().getMeasurements().size() > 0) {
		return trustPolicyDraftObj.getWhitelist().getMeasurements();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

	private void buildPatch(Set<String> patchDirAddSet, Set<String> patchDirRemoveSet,
			Set<String> patchFileAddSet, Set<String> patchFileRemoveSet,
			Set<String> patchSymLinkAddSet, Set<String> patchSymLinkRemoveSet,
			List<Measurement> measurements, SearchFilesInImageRequest searchFilesInImageRequest,
			SearchFilesInImageResponse filesInImageResponse) {
		// Build the PATCH
		// Add Dir patch
		if (filesInImageResponse.patchXml == null) {
			filesInImageResponse.patchXml = new ArrayList<>();
		}

		for (String patchFile : patchFileAddSet) {
			filesInImageResponse.patchXml.add(
					"<add sel='//*[local-name()=\"Whitelist\"]'><File Path=\"" + patchFile + "\"/></add>");
		}

		for (String patchFile : patchSymLinkAddSet) {
				filesInImageResponse.patchXml.add(
						"<add sel='//*[local-name()=\"Whitelist\"]'><Symlink Path=\"" + patchFile + "\"/></add>");
		}

		for (String patchFile : patchDirAddSet) {
			String filterType = searchFilesInImageRequest.filter_type == null
					? ""
					: searchFilesInImageRequest.filter_type;

			String filterTypeAttribute = "FilterType=\"" + filterType	+ "\"";
			filesInImageResponse.patchXml
					.add("<add sel='//*[local-name()=\"Whitelist\"]'><Dir Path=\""
							+ patchFile
							+ "\" Include=\""
							+ (searchFilesInImageRequest.include == null ? ""
							: searchFilesInImageRequest.include)
							+ "\" Exclude=\""
							+ (searchFilesInImageRequest.exclude == null ? ""
							: searchFilesInImageRequest.exclude)
							+ "\" "
							+ filterTypeAttribute
							+ "/></add>");
		}

		for (String patchFile : patchFileRemoveSet) {
			filesInImageResponse.patchXml
					.add("<remove sel='//*[local-name()=\"Whitelist\"]/*[local-name()=\"File\"][@Path=\""
							+ patchFile + "\"]'></remove>");
		}

		for (String patchFile : patchSymLinkRemoveSet) {
			filesInImageResponse.patchXml
					.add("<remove sel='//*[local-name()=\"Whitelist\"]/*[local-name()=\"Symlink\"][@Path=\""
							+ patchFile + "\"]'></remove>");
		}

		for (String patchFile : patchDirRemoveSet) {
			filesInImageResponse.patchXml
					.add("<remove sel='//*[local-name()=\"Whitelist\"]/*[local-name()=\"Dir\"][@Path=\""
							+ patchFile + "\"]'></remove>");
		}
	}

    public void updateTrustPolicy(UpdateTrustPolicyRequest updateTrustPolicyRequest, String policyId)
	    throws DirectorException {

	try {
	    if (isPolicyNameNotUnique(updateTrustPolicyRequest.getDisplay_name(), policyId)) {
		throw new DirectorException("Policy Name Already Exists");
	    }
	} catch (DbException e1) {
	    throw new DirectorException(
		    "Unable to check uniqueness of policy name : " + updateTrustPolicyRequest.display_name);
	}
	log.debug("Inside  imageStoreUploadRequest::" + updateTrustPolicyRequest);

	if (StringUtils.isBlank(updateTrustPolicyRequest.display_name)) {
	    log.error("No policy name provided");
	    throw new DirectorException("Policy name is empty");
	}
	// Updating Name in case name is changed

	TrustPolicy trustPolicy;
	try {
	    trustPolicy = imagePersistenceManager.fetchPolicyById(policyId);
	} catch (DbException e) {
	    throw new DirectorException("Unable to fetch policy with id : " + policyId);
	}
	trustPolicy.setDisplay_name(updateTrustPolicyRequest.getDisplay_name());
	trustPolicy.setEdited_date(Calendar.getInstance());
	trustPolicy.setEdited_by_user_id(ShiroUtil.subjectUsername());
	try {
	    imagePersistenceManager.updatePolicy(trustPolicy);
	} catch (DbException e) {
	    throw new DirectorException("Unable to update policy with id : " + policyId);
	}

    }

    public StoreManager getImageStoreImpl(String className)
	    throws ClassNotFoundException, InstantiationException, IllegalAccessException {

	StoreManager imgManager;
	Class c;

	c = Class.forName(className);

	imgManager = (StoreManager) c.newInstance();

	return imgManager;

    }

    @Override
    public ImageListResponse getBareMetalLive(List<ImageInfo> images) throws DirectorException {

	List<ImageInfo> imageList = images;
	ImageListResponse imageListresponse = new ImageListResponse();

	imageListresponse.images = new ArrayList<ImageListResponseInfo>();
	for (ImageInfo imageInfo : imageList) {
	    if (imageInfo.getImage_format() != null) {
		continue;
	    }

	    ImageListResponseInfo imgResponse = new ImageListResponseInfo();
	    imgResponse.setImage_name(imageInfo.getImage_name());

	    String trust_policy = "<div id=\"trust_policy_column" + imageInfo.id + "\">";
	    if (imageInfo.getTrust_policy_draft_id() == null && imageInfo.getTrust_policy_id() == null) {

		continue;
	    }

	    if (imageInfo.getTrust_policy_draft_id() != null) {
		trust_policy = trust_policy
			+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicyForBMLive('"
			+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "')\"></span></a>";

	    } else if (imageInfo.getTrust_policy_id() != null) {
		trust_policy = trust_policy
			+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicyForBMLive('"
			+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "')\"></span></a>";
	    }

	    if (imageInfo.getTrust_policy_id() != null) {
		trust_policy = trust_policy
			+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicyAndManifest('"
			+ imageInfo.getId() + "','" + imageInfo.getTrust_policy_id() + "')\"></span></a>";
	    }

	    if (imageInfo.getTrust_policy_id() != null || imageInfo.getTrust_policy_draft_id() != null) {
		trust_policy = trust_policy
			+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicy('"
			+ imageInfo.getId() + "','" + imageInfo.getTrust_policy_id() + "','" + imageInfo.getImage_name()
			+ "')\"></span></a>";
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
		    + imageInfo.getId() + "','" + imageInfo.getImage_name() + "','" + imageInfo.getTrust_policy_id()
		    + "')\" ></span></a>";

	    imgResponse.setImage_upload(image_upload);

	    imgResponse.setCreated_date(imageInfo.getCreated_date());

	    imageListresponse.images.add(imgResponse);

	}

	return imageListresponse;
    }

    @Override
    public ImageListResponse getImagesForBareMetal(List<ImageInfo> images) throws DirectorException {

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

	    String trust_policy = "<div id=\"trust_policy_column" + imageInfo.id + "\">";
	    if (imageInfo.getTrust_policy_draft_id() == null && imageInfo.getTrust_policy_id() == null) {

		trust_policy = trust_policy
			+ "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createPolicyForBMImage('"
			+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "')\"></span></a>";

	    }

	    if (imageInfo.getTrust_policy_draft_id() != null) {
		trust_policy = trust_policy
			+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicyForBMImage('"
			+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "')\"></span></a>";

	    } else if (imageInfo.getTrust_policy_id() != null) {
		trust_policy = trust_policy
			+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicyForBMImage('"
			+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "')\"></span></a>";
	    }

	    if (imageInfo.getTrust_policy_id() != null) {
		trust_policy = trust_policy
			+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicy('"
			+ imageInfo.getId() + "','" + imageInfo.getTrust_policy_id() + "')\"></span></a>";
	    }

	    if (imageInfo.getTrust_policy_id() != null || imageInfo.getTrust_policy_draft_id() != null) {
		trust_policy = trust_policy
			+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicy('"
			+ imageInfo.getId() + "','" + imageInfo.getTrust_policy_id() + "','" + imageInfo.getImage_name()
			+ "')\"></span></a>";
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
		    TrustPolicy trustPolicy = imagePersistenceManager.fetchPolicyById(tpid);
		    if (trustPolicy != null && trustPolicy.getCreated_date() != null) {
			imgResponse.setCreated_date(trustPolicy.getCreated_date());
		    }
		} catch (DbException e) {
		    log.error("unable to get trust policy by id, getBareMetalLive", e);

		}
	    }

	    imageListresponse.images.add(imgResponse);

	}

	return imageListresponse;
    }

    @Override
    public ImageListResponse getImagesForVM(List<ImageInfo> images) throws DirectorException {

	List<ImageInfo> imageList = images;
	ImageListResponse imageListresponse = new ImageListResponse();

	imageListresponse.images = new ArrayList<ImageListResponseInfo>();
	for (ImageInfo imageInfo : imageList) {
	    if (imageInfo.deleted) {
		continue;
	    }
	    ImageListResponseInfo imgResponse = new ImageListResponseInfo();
	    imgResponse.setImage_name(imageInfo.getImage_name());
	    imgResponse.setImage_format(imageInfo.getImage_format());

	    String image_delete = "<a href=\"#\"><span class=\"glyphicon glyphicon-remove\" title=\"Delete Image\" onclick=\"deleteImage('"
		    + imageInfo.getId() + "')\"/></a>";
	    imgResponse.setImage_delete(image_delete);

	    String trust_policy = "<div id=\"trust_policy_vm_column" + imageInfo.id + "\">";

	    if (imageInfo.getTrust_policy_draft_id() == null && imageInfo.getTrust_policy_id() == null) {

		trust_policy = trust_policy
			+ "<a href=\"#\" title=\"Create Policy\" ><span class=\"glyphicon glyphicon-plus-sign\"  title=\"Create Policy\" onclick=\"createPolicy('"
			+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "')\"></span></a>";

	    }

	    if (imageInfo.getTrust_policy_draft_id() != null) {
		trust_policy = trust_policy
			+ "<a href=\"#\" title=\"Edit Policy\"><span class=\"glyphicon glyphicon-edit\" title=\"Edit Policy\"  onclick=\"editPolicy('"
			+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "')\"></span></a>";

	    } else if (imageInfo.getTrust_policy_id() != null) {
		trust_policy = trust_policy
			+ "<a href=\"#\" title=\"Edit Policy\" ><span class=\"glyphicon glyphicon-edit\"  title=\"Edit Policy\" onclick=\"editPolicy('"
			+ imageInfo.getId() + "','" + imageInfo.getImage_name() + "')\"></span></a>";
	    }

	    if (imageInfo.getTrust_policy_id() != null) {
		trust_policy = trust_policy
			+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-download-alt\"   title=\"Download\" onclick=\"downloadPolicy('"
			+ imageInfo.getId() + "','" + imageInfo.getTrust_policy_id() + "')\"></span></a>";
	    }

	    if (imageInfo.getTrust_policy_id() != null || imageInfo.getTrust_policy_draft_id() != null) {
		trust_policy = trust_policy
			+ "&nbsp;<a href=\"#\"><span class=\"glyphicon glyphicon-trash\"   title=\"Delete Policy\" onclick=\"deletePolicyVM('"
			+ imageInfo.getId() + "','" + imageInfo.getTrust_policy_id() + "','" + imageInfo.getImage_name()
			+ "')\"></span></a>";
	    }

	    trust_policy = trust_policy + "</div>";
	    imgResponse.setTrust_policy(trust_policy);

	    String image_upload;
	    if (imageInfo.getImage_uploads_count() != 0) {
		image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-ok\" title=\"Uploaded Before\"></span></a>";
	    } else {
		image_upload = "<a href=\"#\"><span class=\"glyphicon glyphicon-minus\" title=\"Never Uploaded\"></span></a>";
	    }

	    image_upload += "&nbsp;"
		    + "<a href=\"#\" title=\"Upload\" ><span class=\"glyphicon glyphicon-open\" title=\"Upload\" onclick=\"uploadToImageStorePage('"
		    + imageInfo.getId() + "','" + imageInfo.getImage_name() + "','" + imageInfo.getTrust_policy_id()
		    + "')\" ></span></a>";

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
    public TrustPolicyDraftResponse createPolicyDraftFromPolicy(String imageId) throws DirectorException {

	ImageInfo imageInfo;
	try {
	    imageInfo = imagePersistenceManager.fetchImageById(imageId);
	} catch (DbException e) {
	    log.error("Cannot fetch imageid imageId::" + imageId, e);
	    throw new DirectorException("Cannot fetch image by id", e);
	}
	if (imageInfo == null) {
	    throw new DirectorException("Image does not exist");
	}

	TrustPolicy existingTrustPolicy;
	try {
	    existingTrustPolicy = imagePersistenceManager.fetchPolicyById(imageInfo.getTrust_policy_id());
	} catch (DbException e) {
	    log.error("Cannot get policy by id existingTrustPolicyId::" + imageInfo.getTrust_policy_id(), e);
	    throw new DirectorException("Cannot get policy by id", e);
	}
	TrustPolicyDraft trustPolicyDraft;
	try {
	    trustPolicyDraft = trustPolicyToTrustPolicyDraft(existingTrustPolicy);
	} catch (JAXBException e) {
	    log.error("Unable convert policy to draft", e);
	    throw new DirectorException("Unable convert policy to draft ", e);
	}
	TrustPolicyDraft savePolicyDraft;
	try {
	    trustPolicyDraft.setEdited_date(Calendar.getInstance());
	    trustPolicyDraft.setEdited_by_user_id(ShiroUtil.subjectUsername());
	    savePolicyDraft = imagePersistenceManager.savePolicyDraft(trustPolicyDraft);
	} catch (DbException e) {
	    log.error("Unable to save policy draft", e);
	    throw new DirectorException("Unable to save policy draft ", e);
	}

	Mapper mapper = new DozerBeanMapper();
	TrustPolicyDraftResponse trustPolicyDraftResponse = mapper.map(savePolicyDraft, TrustPolicyDraftResponse.class);
	/*
	 * try { imagePersistenceManager.destroyPolicy(existingTrustPolicy); }
	 * catch (DbException e) { log.error("Cannoot delete policy", e); throw
	 * new DirectorException("Cannot delete policy draft y", e); }
	 */
	return trustPolicyDraftResponse;
    }

    public TrustPolicyDraft trustPolicyToTrustPolicyDraft(TrustPolicy existingTrustPolicy) throws JAXBException {
	TrustPolicyDraft trustPolicyDraft = new TrustPolicyDraft();
	String policyXml = existingTrustPolicy.getTrust_policy();

	com.intel.mtwilson.trustpolicy2.xml.TrustPolicy policy = TdaasUtil.getPolicy(policyXml);
	policy.setSignature(null);
	List<Measurement> measurements = policy.getWhitelist().getMeasurements();
	for (Measurement measurement : measurements) {
	    measurement.setValue(null);
	}

	policyXml = TdaasUtil.convertTrustPolicyToString(policy);

	trustPolicyDraft.setCreated_by_user_id(existingTrustPolicy.getCreated_by_user_id());
	trustPolicyDraft.setCreated_date(existingTrustPolicy.getCreated_date());
	trustPolicyDraft.setEdited_by_user_id(existingTrustPolicy.getEdited_by_user_id());
	trustPolicyDraft.setEdited_date(existingTrustPolicy.getEdited_date());
	trustPolicyDraft.setName(existingTrustPolicy.getName());
	trustPolicyDraft.setImgAttributes(existingTrustPolicy.getImgAttributes());
	trustPolicyDraft.setTrust_policy_draft(policyXml);
	trustPolicyDraft.setDisplay_name(TdaasUtil.getVersionedName(existingTrustPolicy.getDisplay_name()));
	return trustPolicyDraft;
    }

    private Collection<File> getFirstLevelFiles(SearchFilesInImageRequest searchFilesInImageRequest)
	    throws DirectorException {
	return getFirstLevelFiles(searchFilesInImageRequest.getDir(), searchFilesInImageRequest.id);

    }

    private Collection<File> getFirstLevelFiles(String dir, String imageId) throws DirectorException {
	Collection<File> files = null;
	if (!dir.endsWith(File.separator)) {
	    dir += File.separator;
	}
	DirectoryAndFileUtil directoryAndFileUtil = new DirectoryAndFileUtil(imageId);
	DirectoryMeasurement dirMeasurement = new DirectoryMeasurement();
	dirMeasurement.setPath(dir);
	try {
	    String filesAndDirectories = directoryAndFileUtil.getFilesAndDirectories(imageId,
					dirMeasurement, true);
	    files = getFilesFromFileString(dir, filesAndDirectories);
	} catch (IOException e) {
	    // TODO Handle Error
	    log.error("Error in getting files for browser ", e);
	}
	return files;
    }

    private List<File> getFilesAndDirectories(String sDir, Set<String> dirs, String imageId) throws DirectorException {
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

	    if (new File(TdaasUtil.getMountPath(imageId) + file.getAbsolutePath()).isDirectory()) {
		getFilesAndDirectories(file.getAbsolutePath(), dirs, imageId);
	    }
	}
	return files;
    }

    private List<File> getFilesAndDirectoriesForEdit(String sDir, Set<String> dirs, String imageId)
	    throws DirectorException {
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
		DirectoryAndFileUtil directoryAndFileUtil =
				new DirectoryAndFileUtil(searchFilesInImageRequest.id);
		DirectoryMeasurement dirMeasurement = new DirectoryMeasurement();
		dirMeasurement.setPath(searchFilesInImageRequest.getDir());
		dirMeasurement.setFilterType(searchFilesInImageRequest.filter_type);
		dirMeasurement.setInclude(searchFilesInImageRequest.include);
		dirMeasurement.setExclude(searchFilesInImageRequest.exclude);
		try {
			String filesAndDirectories =
					directoryAndFileUtil.getFilesAndDirectories(searchFilesInImageRequest.id,
							dirMeasurement, true);
			files =
					getFilesFromFileString(mountPath + searchFilesInImageRequest.getDir() + File.separator,
							filesAndDirectories);

			/* Go deep in sub directories and try to find the matching files*/
			/* Only if recursive flag is set */
			if(searchFilesInImageRequest.recursive){
				Collection<File> directoryTree =
						getAllSubDirectories(mountPath, searchFilesInImageRequest.getDir());
				for(File dir : directoryTree) {
					files.add(dir);
					DirectoryMeasurement subDirMeasurement = new DirectoryMeasurement();
					subDirMeasurement.setPath(dir.getAbsolutePath());
					subDirMeasurement.setFilterType(searchFilesInImageRequest.filter_type);
					subDirMeasurement.setInclude(searchFilesInImageRequest.include);
					subDirMeasurement.setExclude(searchFilesInImageRequest.exclude);
					String subFilesAndDirectories =
							directoryAndFileUtil.getFilesAndDirectories(searchFilesInImageRequest.id,
									subDirMeasurement, false);
					Collection<File> subFiles =
							getFilesFromFileString(dir.getAbsolutePath() + File.separator,
									subFilesAndDirectories);
					files.addAll(subFiles);
				}
			}

		} catch (IOException e) {
			// TODO Handle Error
			log.error("Error in getting files for browser ", e);
		}
		return files;
	}

	private Collection<File> getFilesFromFileString(String baseDir, String fileStr) {
		String nl = System.getProperty("line.separator");
		Set<File> files = new HashSet<File>();
		String[] split = fileStr.split(nl);
		for (String string : split) {
			if (!string.isEmpty() && !string.equals(baseDir)) {
				File file = new File(baseDir + string);
				files.add(file);
			}
		}
		return files;
	}

	/**
	 * Get All sub directories for given mount path and base directory
	 * @param mountPath
	 * @param baseDir
	 * @return
	 */
	private Collection<File> getAllSubDirectories(String mountPath, String baseDir){
		File baseFile = new File(mountPath + baseDir);
		Collection<File> empty = new HashSet<>();
		if(baseFile.exists() && baseFile.isDirectory()){
			return getSubDirectories(baseFile);
		}
		return empty;
	}

	/**
	 * Get Subdirectories recursively and return in collection
	 * @param baseDirectory Base Directory
	 * @return
	 */
	private Collection<File> getSubDirectories(File baseDirectory) {
		File[] listFiles = baseDirectory.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory() && !Files.isSymbolicLink(f.toPath());
			}
		});
		if(null != listFiles) {
			List<File> subDirectories = Arrays.asList(listFiles);
			subDirectories = new ArrayList<>(subDirectories);
			List<File> deepSubDirectories = new ArrayList<>();
			for (File dir : subDirectories) {
				deepSubDirectories.addAll(getSubDirectories(dir));
			}
			subDirectories.addAll(deepSubDirectories);
			return subDirectories;
		}
		return new ArrayList<>(0);
	}

    @Override
    public String getDisplayNameForImage(String image_id) throws DirectorException {
	ImageInfo image_info;
	String policy_id;
	String display_name = "";
	try {
	    image_info = imagePersistenceManager.fetchImageById(image_id);
	    if (image_info.getTrust_policy_id() != null) {
		policy_id = image_info.getTrust_policy_id();
		display_name += imagePersistenceManager.fetchPolicyById(policy_id).getDisplay_name();
	    } else if (image_info.getTrust_policy_draft_id() != null) {
		policy_id = image_info.getTrust_policy_draft_id();
		display_name += imagePersistenceManager.fetchPolicyDraftById(policy_id).getDisplay_name();
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
    public ImageListResponse getImages(List<ImageInfo> images, String deployment_type) throws DirectorException {
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
	if (isModified) {
	    return imageInfo.getLocation() + "Modified_" + imageInfo.getImage_name();
	} else {
	    return imageInfo.getLocation() + imageInfo.getImage_name();
	}

    }

    @Override
    public TrustPolicy getTrustPolicyByImageId(String imageId) throws DirectorException {
	String id;
	try {
	    id = imagePersistenceManager.fetchImageById(imageId).getTrust_policy_id();
	} catch (DbException e) {
	    String msg = "Unable to fetch trust policy id for image id : " + imageId;
	    log.error(msg, e);
	    throw new DirectorException(msg, e);
	}
	try {
	    return imagePersistenceManager.fetchPolicyById(id);
	} catch (DbException e) {
	    String msg = "Unable to fetch trust policy instance for policy id : " + id;
	    log.error(msg, e);
	    throw new DirectorException(msg, e);
	}
    }

    @Override
    public ImportPolicyTemplateResponse importPolicyTemplate(String imageId) throws DirectorException {
	ImportPolicyTemplateResponse importPolicyTemplateResponse;
	ImageAttributes image;

	try {
	    image = imagePersistenceManager.fetchImageById(imageId);
	} catch (DbException e3) {
	    log.error("Internal error occurred in fetching image");
	    throw new DirectorException("Internal error occurred in fetching image " + imageId, e3);
	}
	if (image == null) {
	    log.info("No image found during import of policy");
	    throw new DirectorException("No image found with id: " + imageId);

	}
	if (image != null && !(image.getImage_deployments().equals(Constants.DEPLOYMENT_TYPE_BAREMETAL)
		&& image.getImage_format() == null)) {
	    importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
	    // / importPolicyTemplateResponse.status = "Success";
	    // / importPolicyTemplateResponse.details = "No need for import ";
	    return importPolicyTemplateResponse;
	}

	// Get the draft
	TrustPolicyDraft policyDraftForImage;
	try {
	    policyDraftForImage = imagePersistenceManager.fetchPolicyDraftForImage(imageId);
	} catch (DbException ex) {
	    log.error("Error fetching trust policy in import policy", ex);
	    throw new DirectorException("Error fetching trust policy in import policy: " + imageId, ex);
	}

	com.intel.mtwilson.trustpolicy2.xml.TrustPolicy policy;
	try {
	    policy = TdaasUtil.getPolicy(policyDraftForImage.getTrust_policy_draft());
	} catch (JAXBException e) {
	    log.error("Error mapping trust policy string to object for image: " + imageId);
	    throw new DirectorException("Error mapping trust policy string to object for image: " + imageId, e);
	}

	List<Measurement> existingMeasurements = policy.getWhitelist().getMeasurements();

	if (existingMeasurements != null && existingMeasurements.size() > 0) {
	    importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
	    importPolicyTemplateResponse.setTrust_policy(policyDraftForImage.getTrust_policy_draft());
	    // importPolicyTemplateResponse.setStatus("Success");
	    return importPolicyTemplateResponse;
	}
	// Check if mounted live BM has /opt/vrtm
	String idendifier = TdaasUtil.checkInstalledComponents(imageId);

	String content = null;
	Manifest manifest;

	try {
	    List<PolicyTemplateInfo> fetchPolicyTemplateForDeploymentIdentifier = imagePersistenceManager
		    .fetchPolicyTemplateForDeploymentIdentifier(Constants.DEPLOYMENT_TYPE_BAREMETAL, idendifier);
	    if (fetchPolicyTemplateForDeploymentIdentifier != null
		    && fetchPolicyTemplateForDeploymentIdentifier.size() > 0) {
		PolicyTemplateInfo policyTemplateInfo = fetchPolicyTemplateForDeploymentIdentifier.get(0);
		content = policyTemplateInfo.getContent();
	    }
	} catch (DbException e2) {
	    log.error("Error converting manifest to object ", e2);
	    throw new DirectorException("Error converting manifest to object ", e2);
	}

	if (StringUtils.isBlank(content)) {
	    importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
	    importPolicyTemplateResponse.setTrust_policy(policyDraftForImage.getTrust_policy_draft());
	    // / importPolicyTemplateResponse.setStatus("Success");
	    return importPolicyTemplateResponse;
	}
	try {
	    manifest = TdaasUtil.convertStringToManifest(content);
	} catch (JAXBException e1) {
	    log.error("Error converting manifest to object " + content, e1);
	    throw new DirectorException("Error converting manifest to object ", e1);
	}

	Collection<Measurement> measurements = new ArrayList<>();
	for (MeasurementType measurementType : manifest.getManifest()) {
	    if (measurementType instanceof DirectoryMeasurementType) {
		DirectoryMeasurement measurement = new DirectoryMeasurement();
		measurement.setPath(measurementType.getPath());
		measurement.setExclude(((DirectoryMeasurementType) measurementType).getExclude());
		measurement.setInclude(((DirectoryMeasurementType) measurementType).getInclude());
		measurement.setFilterType(((DirectoryMeasurementType) measurementType).getFilterType());
		measurements.add(measurement);
	    } else if (measurementType instanceof FileMeasurementType) {
		FileMeasurement measurement = new FileMeasurement();
		measurement.setPath(measurementType.getPath());
		measurements.add(measurement);
	    }
	}
	policy.getWhitelist().getMeasurements().addAll(measurements);
	String policyXmlWithImports;
	try {
	    policyXmlWithImports = TdaasUtil.convertTrustPolicyToString(policy);
	} catch (JAXBException e) {
	    log.error("Error converting imported policy into string");
	    throw new DirectorException("Error converting imported policy into string", e);
	}

	policyDraftForImage.setTrust_policy_draft(policyXmlWithImports);
	policyDraftForImage.setEdited_date(Calendar.getInstance());
	policyDraftForImage.setEdited_by_user_id(ShiroUtil.subjectUsername());
	policyDraftForImage.setCreated_date(Calendar.getInstance());
	policyDraftForImage.setCreated_by_user_id(ShiroUtil.subjectUsername());

	try {
	    imagePersistenceManager.updatePolicyDraft(policyDraftForImage);
	} catch (DbException e) {
	    log.error("Error saving policy draft for image after adding imports for IMAGE:  " + imageId);
	    throw new DirectorException(
		    "Error saving policy draft for image after adding imports for IMAGE:  " + imageId, e);
	}
	importPolicyTemplateResponse = new ImportPolicyTemplateResponse();
	importPolicyTemplateResponse.setTrust_policy(policyXmlWithImports);
	// / importPolicyTemplateResponse.setStatus(Constants.SUCCESS);
	return importPolicyTemplateResponse;
    }

    private void createlistOfFilesForRegexReset(SearchFilesInImageRequest searchFilesInImageRequest,
	    Set<String> patchFilesRemoveSet) throws DirectorException {
	TrustPolicyDraft trustPolicyDraft;
	try {
	    trustPolicyDraft = imagePersistenceManager.fetchPolicyDraftForImage(searchFilesInImageRequest.id);
	} catch (DbException dbe) {
	    log.error("Error fetching policy draft", dbe);
	    throw new DirectorException("Error fetching policy draft for image: " + searchFilesInImageRequest.id, dbe);
	}

	if (trustPolicyDraft == null) {
	    log.error("No policy draft exists for trust policy draft id " + searchFilesInImageRequest.id);
	    throw new DirectorException(
		    "No policy draft exists for trust policy draft id " + searchFilesInImageRequest.id);
	}

	boolean start = false;
	try {
	    com.intel.mtwilson.trustpolicy2.xml.TrustPolicy policy = TdaasUtil
		    .getPolicy(trustPolicyDraft.getTrust_policy_draft());
	    List<Measurement> measurements = policy.getWhitelist().getMeasurements();
	    for (Measurement measurement : measurements) {
		if (measurement instanceof DirectoryMeasurement) {
		    if (measurement.getPath().equals(searchFilesInImageRequest.getDir())) {
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
    public void deleteTrustPolicy(String trust_policy_id) throws DirectorException {

	TrustPolicy policy;
	try {
	    policy = imagePersistenceManager.fetchPolicyById(trust_policy_id);
	} catch (DbException e) {
	    log.error("Error fetching policy for id " + trust_policy_id);
	    throw new DirectorException("Error fetching policy for id " + trust_policy_id, e);
	}
	if (policy != null) {
	    policy.setArchive(true);
	    try {
		imagePersistenceManager.updatePolicy(policy);
	    } catch (DbException e1) {
		log.error("Unable in archiving policy, deleteTrustPolicy", e1);
		throw new DirectorException("Unable in archiving policy, deleteTrustPolicy", e1);
	    }

	    ImageInfo image;
	    try {
		image = imagePersistenceManager.fetchImageById(policy.getImgAttributes().id);
	    } catch (DbException e) {
		log.error("Error fetching image for policy with id " + trust_policy_id);
		throw new DirectorException("Error fetching image for policy with id " + trust_policy_id, e);
	    }
	    if (image.getImage_format() == null) {
		SshSettingInfo existingSsh;
		try {
		    existingSsh = imagePersistenceManager.fetchSshByImageId(image.id);
		} catch (DbException e) {
		    log.error("Error fetching SSH Settings for image id " + image.id);
		    throw new DirectorException("Error fetching SSH Settings for image id " + image.id, e);
		}
		if (existingSsh != null && existingSsh.getId() != null && StringUtils.isNotEmpty(existingSsh.getId())) {
		    try {
			imagePersistenceManager.destroySshById(existingSsh.getId());
		    } catch (DbException e) {
			log.error("Error deleting SSH Settings for image id " + image.id);
			throw new DirectorException("Error deleting SSH Settings for image id " + image.id, e);
		    }
		}
	    }
	}
    }

    @Override
    public void deleteTrustPolicyDraft(String trust_policy_draft_id) throws DirectorException {
	TrustPolicyDraft policyDraft;
	try {
	    policyDraft = imagePersistenceManager.fetchPolicyDraftById(trust_policy_draft_id);
	} catch (DbException e) {
	    log.error("Error fetching policy draft for id " + trust_policy_draft_id);
	    throw new DirectorException("Error fetching policy draft for id " + trust_policy_draft_id, e);
	}
	if (policyDraft != null) {
	    try {
		imagePersistenceManager.destroyPolicyDraft(policyDraft);
	    } catch (DbException e) {
		log.error("Error deleting policy draft with id " + trust_policy_draft_id);
		throw new DirectorException("Error deleting policy draft with id " + trust_policy_draft_id, e);
	    }
	}
	ImageAttributes imgAttributes = policyDraft.getImgAttributes();
	if (imgAttributes != null) {
	    String id = imgAttributes.getId();
	    deleteImageShhSettings(id);
	}
    }

    private void deleteImageShhSettings(String imageId) throws DirectorException {
	ImageInfo image;
	try {
	    image = imagePersistenceManager.fetchImageById(imageId);
	} catch (DbException e) {
	    log.error("Error fetching image  with id " + imageId);
	    throw new DirectorException("Error fetching image  with id " + imageId, e);
	}
	if (image.getImage_format() == null) {
	    SshSettingInfo existingSsh;
	    try {
		existingSsh = imagePersistenceManager.fetchSshByImageId(image.id);
	    } catch (DbException e) {
		log.error("Error fetching SSH Settings for image id " + image.id);
		throw new DirectorException("Error fetching SSH Settings for image id " + image.id, e);
	    }
	    if (existingSsh != null && existingSsh.getId() != null && StringUtils.isNotEmpty(existingSsh.getId())) {
		try {
		    imagePersistenceManager.destroySshById(existingSsh.getId());
		} catch (DbException e) {
		    log.error("Error deleting SSH Settings for image id " + image.id);
		    throw new DirectorException("Error deleting SSH Settings for image id " + image.id, e);
		}
	    }
	}

    }

    @Override
    public File createTarballOfPolicyAndManifest(String imageId) {

	TrustPolicy policyForImage;
	try {
	    policyForImage = imagePersistenceManager.fetchActivePolicyForImage(imageId);
	} catch (DbException e) {
	    log.error("Unable to fetch policy for image : " + imageId, e);
	    return null;
	}
	if (policyForImage == null) {
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

	String trustPolicyFilePath = artifactsPath + File.separator + "trustpolicy.xml";
	if (!fileUtilityOperation.createNewFile(trustPolicyFilePath)) {
	    return null;
	}
	String manifestFilePath = artifactsPath + File.separator + "manifest.xml";
	if (!fileUtilityOperation.createNewFile(manifestFilePath)) {
	    return null;
	}
	String manifest;
	try {
	    manifest = TdaasUtil.getManifestForPolicy(policyForImage.getTrust_policy());
	} catch (JAXBException e) {
	    log.error("Cannot create manifest for policy for image: " + imageId, e);
	    return null;
	}

	// Write policy
	fileUtilityOperation.writeToFile(trustPolicyFilePath, policyForImage.getTrust_policy());
	// Write manifest
	fileUtilityOperation.writeToFile(manifestFilePath, manifest);
	String tarName = StringUtils.isNotEmpty(policyForImage.getDisplay_name())
		? policyForImage.getDisplay_name() + ".tar.gz" : "policyandmanifest.tar.gz";
	try {

	    DirectorUtil.createTar(artifactsPath, "trustpolicy.xml", "manifest.xml", artifactsPath + File.separator,
		    tarName);
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
    public SshSettingRequest getBareMetalMetaData(String image_id) throws DirectorException {
	SshSettingRequest settingRequest = new SshSettingRequest();
	try {
	    SshSettingInfo ssh = imagePersistenceManager.fetchSshByImageId(image_id);
	    settingRequest.setId(ssh.getId());
	    settingRequest.setName(ssh.getName());
	    // //
	    // settingRequest.setPolicy_name(getDisplayNameForImage(image_id));
	    settingRequest.setIpAddress(ssh.getIpAddress());
	    settingRequest.setUsername(ssh.getUsername());
	} catch (DbException e) {
	    log.error("Error Fetching ssh from DB in getBareMetalMetaData", e);
	    throw new DirectorException("Error Fetching ssh from DB in getBareMetalMetaData", e);
	}
	return settingRequest;
    }

    @Override
    public void deletePasswordForHost(String image_id) throws DirectorException {
	try {
	    ImageInfo imageInfo = imagePersistenceManager.fetchImageById(image_id);
	    if (Constants.DEPLOYMENT_TYPE_VM.equals(imageInfo.getImage_deployments())
		    || imageInfo.getImage_format() != null) {
		return;
	    }

	    SshSettingInfo ssh = imagePersistenceManager.fetchSshByImageId(image_id);
	    log.info("Setting passowrd to null for SSH : " + ssh.id);
	    SshPassword password = ssh.getPassword();
	    ssh.setPassword(null);
	    imagePersistenceManager.updateSsh(ssh);
	    log.info("complete update host with null for password");
	    imagePersistenceManager.destroySshPassword(password.getId());
	    log.info("Destroying passowrd to null for SSH : " + ssh.id);
	} catch (DbException e) {
	    log.error("Error in deleting SSh Password", e);
	    throw new DirectorException("Error in deleting SSh Password", e);
	}

    }

    @Override
    public void deleteImage(String imageId) throws DirectorException {
	try {
	    ImageInfo imageInfo = imagePersistenceManager.fetchImageById(imageId);

	    log.debug("image fetched for id: " + imageId);
	    if (Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(imageInfo.image_deployments)) {
		try {
		    log.info("Performing docker rmi for image ::" + imageInfo.id);
		    DockerUtil.dockerRMI(imageInfo.repository, imageInfo.tag + "_source");
		} catch (Exception e) {
		    log.error("Error in docker rmi: " + imageId, e);
		    throw new DirectorException("Error in docker rmi", e);
		}
	    }
	    imageInfo.setDeleted(true);
	    imagePersistenceManager.updateImage(imageInfo);

	    // Now delete the image from disk
	    File toBeDeletedFile = new File(imageInfo.getLocation() + File.separator + imageInfo.getImage_name());
	    boolean deleteFlag = toBeDeletedFile.delete();
	    log.info("Image deleted from disk = " + deleteFlag);
	    log.info("Image " + imageId + " deleted successfully");
	} catch (DbException e) {
	    log.error("Error deleteing image: " + imageId, e);
	    throw new DirectorException("Error deleting image", e);
	}
    }

    @Override
    public boolean doesPolicyNameExist(String display_name, String image_id) throws DirectorException {
	try {
	    List<TrustPolicyDraft> trustPolicyDraftList = imagePersistenceManager.fetchPolicyDrafts(null);
	    if (trustPolicyDraftList != null) {
		for (TrustPolicyDraft tpd : trustPolicyDraftList) {
		    if (!tpd.getImgAttributes().isDeleted() && tpd.getDisplay_name() != null
			    && tpd.getDisplay_name().equalsIgnoreCase(display_name)
			    && !tpd.getImgAttributes().getId().equals(image_id)) {
			return true;
		    }
		}
	    }
	} catch (DbException e) {
	    throw new DirectorException("Error in fetching trustpolicydrafts list", e);
	}
	try {
	    List<TrustPolicy> trustPolicyList = imagePersistenceManager.fetchPolicies(null);
	    if (trustPolicyList != null) {
		for (TrustPolicy tpd : trustPolicyList) {
		    if (!tpd.getImgAttributes().isDeleted() && tpd.getDisplay_name() != null
			    && tpd.getDisplay_name().equalsIgnoreCase(display_name)
			    && !tpd.getImgAttributes().getId().equals(image_id)) {
			return true;
		    }
		}
	    }
	} catch (DbException e) {
	    throw new DirectorException("Error in fetching trustpolicy list", e);
	}

	return false;
    }

    private boolean isPolicyNameNotUnique(String display_name, String trustPolicyId)
	    throws DbException, DirectorException {
	TrustPolicy fetchPolicyById = imagePersistenceManager.fetchPolicyById(trustPolicyId);
	String imageId = fetchPolicyById.getImgAttributes().id;
	return doesPolicyNameExist(display_name, imageId);
    }

    @Override
    public boolean doesImageNameExist(String fileName, String deploymentType) throws DirectorException {
	List<String> statusToBeCheckedList = new ArrayList<>(2);
	statusToBeCheckedList.add(Constants.IN_PROGRESS);
	statusToBeCheckedList.add(Constants.COMPLETE);
	try {
	    // Fetch images for the deployment type
	    ImageInfoFilter filter = new ImageInfoFilter();
	    filter.image_deployments = deploymentType;
	    List<ImageInfo> imagesList = imagePersistenceManager.fetchImages(filter, null);
	    for (ImageInfo image : imagesList) {
		if (!image.isDeleted() && statusToBeCheckedList.contains(image.status)
			&& StringUtils.isNotBlank(image.getImage_name())
			&& image.getImage_name().equalsIgnoreCase(fileName)) {
		    return true;
		}
	    }
	} catch (DbException e) {
	    throw new DirectorException("Unable to fetch Images", e);
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
	    throw new DirectorException("Unable To fetch Policy with policy Id :: " + trust_policy_id, e);
	}
	if (trustPolicy == null || trustPolicy.getTrust_policy() == null) {
	    log.error("Trust Policy is null for :: " + trust_policy_id);
	    throw new DirectorException("Trust Policy is null  :: " + trust_policy_id);
	}

	try {
	  
		return TdaasUtil.convertTrustPolicyToTrustPolicyResponse(trustPolicy);
		
	} catch (ParserConfigurationException | SAXException | IOException|JAXBException e) {
	    log.error("Unable to convert Trust Policy to TrustPolicyResponse", e);
	    throw new DirectorException("Unable to convert Trust Policy to TrustPolicyResponse", e);
	}
    }

    @Override
    public String getImageByTrustPolicyDraftId(String trustPolicydraftId) throws DirectorException {
	TrustPolicyDraft existingDraft;
	try {
	    existingDraft = imagePersistenceManager.fetchPolicyDraftById(trustPolicydraftId);
	} catch (DbException e1) {
	    String errorMsg = "Unable to fetch draft for trust policy draft id " + trustPolicydraftId;
	    log.error(errorMsg);
	    throw new DirectorException(errorMsg, e1);
	}
	return existingDraft.getImgAttributes().id;

    }

    public ImageInfoResponse getImageDetails(String imageId) throws DirectorException {
	ImageInfoResponse imageInfoResponse;
	try {
	    ImageInfo imageInfo = imagePersistenceManager.fetchImageById(imageId);
	    if (imageInfo == null) {
		return null;
	    }
	    Mapper mapper = new DozerBeanMapper();
	    imageInfoResponse = mapper.map(imageInfo, ImageInfoResponse.class);

	} catch (DbException e1) {

	    log.error("Error in fetchImageById for imageId::" + imageId);
	    throw new DirectorException("unable to fetchImageById", e1);
	}

	if (StringUtils.isNotBlank(imageInfoResponse.getImage_deployments())
		&& imageInfoResponse.getImage_deployments().equals(Constants.DEPLOYMENT_TYPE_BAREMETAL)) {
	    try {
		SshSettingRequest bareMetalMetaData = getBareMetalMetaData(imageId);
		imageInfoResponse.setUsername(bareMetalMetaData.getUsername());
		imageInfoResponse.setIp_address(bareMetalMetaData.getIpAddress());
	    } catch (DirectorException e) {
		log.error("Error in getImageDetails for imageId::" + imageId);
		throw new DirectorException("unable to get image details", e);

	    }
	}
	return imageInfoResponse;
    }

    @Override
    public List<TrustPolicyDraft> getTrustPolicyDrafts(TrustPolicyDraftFilter trustPolicyDraftFilter)
	    throws DirectorException {
	List<TrustPolicyDraft> fetchPolicyDrafts = null;
	try {
	    if (trustPolicyDraftFilter == null) {
		fetchPolicyDrafts = imagePersistenceManager.fetchPolicyDrafts(null, null);
	    }
	    return fetchPolicyDrafts;
	} catch (DbException e) {
	    throw new DirectorException("Error Fetching drafts", e);
	}
    }

    public List<SshSettingRequest> sshData() throws DirectorException {
	TdaasUtil tdaasUtil = new TdaasUtil();
	List<SshSettingInfo> fetchSsh;
	List<SshSettingRequest> responseSsh = new ArrayList<SshSettingRequest>();

	try {

	    fetchSsh = imagePersistenceManager.showAllSsh();

	    for (SshSettingInfo sshSettingInfo : fetchSsh) {

		responseSsh.add(tdaasUtil.toSshSettingRequest(sshSettingInfo));
	    }

	} catch (Exception e) {
	    log.error("ssddata method failed", e);
	    throw new DirectorException(e);
	}

	return responseSsh;

    }

    public void postSshData(SshSettingRequest sshSettingRequest) throws DirectorException {

	TdaasUtil tdaasUtil = new TdaasUtil();
	SshSettingInfo sshSetingInfo = tdaasUtil.fromSshSettingRequest(sshSettingRequest);

	TdaasUtil.addSshKey(sshSettingRequest.getIpAddress(), sshSettingRequest.getUsername(),
		sshSettingRequest.getPassword());

	log.debug("Going to save sshSetting info in database");
	try {
	    imagePersistenceManager.saveSshMetadata(sshSetingInfo);
	} catch (DbException e) {
	    log.error("unable to save ssh info in database", e);
	    throw new DirectorException("Unable to save sshsetting info in database", e);
	}

    }

    public SshSettingResponse addHost(SshSettingRequest sshSettingRequest) throws DirectorException {

	TdaasUtil tdaasUtil = new TdaasUtil();
	sshSettingRequest.setIpAddress(EscapeUtil.doubleQuoteEscapeShellArgument(sshSettingRequest.getIpAddress()));
	sshSettingRequest.setName(EscapeUtil.doubleQuoteEscapeShellArgument(sshSettingRequest.getName()));
	/// sshSettingRequest.setPassword(EscapeUtil.doubleQuoteEscapeShellArgument(sshSettingRequest.getPassword()));
	SshSettingInfo sshSettingInfo = tdaasUtil.fromSshSettingRequest(sshSettingRequest);
	log.info("Inside addHost, going to addSshKey ");
	TdaasUtil.addSshKey(sshSettingRequest.getIpAddress(), sshSettingRequest.getUsername(),
		sshSettingRequest.getPassword());
	log.debug("Inside addHost,After execution of addSshKey ");
	log.debug("Going to save sshSetting info in database");
	SshSettingInfo info;
	if (StringUtils.isNotBlank(sshSettingRequest.getImage_id())) {
	    log.info("AddHost can't take image_id as parameter");
	    throw new DirectorException("AddHost can't take image_id as parameter");
	} else {
	    try {

		info = imagePersistenceManager.saveSshMetadata(sshSettingInfo);

	    } catch (DbException e) {
		log.error("unable to save ssh info in database", e);
		throw new DirectorException("Unable to create policy draft", e);
	    }
	}
	return TdaasUtil.convertSshInfoToResponse(info);

    }

    public SshSettingResponse updateSshData(SshSettingRequest sshSettingRequest) throws DirectorException {
	// SshSettingInfo updateSsh=new SshSettingInfo();
	sshSettingRequest.setIpAddress(EscapeUtil.doubleQuoteEscapeShellArgument(sshSettingRequest.getIpAddress()));
	/// sshSettingRequest.setName(EscapeUtil.doubleQuoteEscapeShellArgument(sshSettingRequest.getName()));
	TdaasUtil tdaasUtil = new TdaasUtil();

	// sshPersistenceManager.destroySshById(sshSettingRequest.getId());

	log.info("Inside updateSshData, going to addSshKey ");
	TdaasUtil.addSshKey(sshSettingRequest.getIpAddress(), sshSettingRequest.getUsername(),
		sshSettingRequest.getPassword());
	log.debug("Inside addHost,After execution of addSshKey ");
	log.info("Inside updateSshData,After execution of addSshKey ");
	try {
	    SshSettingInfo existingSsh = imagePersistenceManager.fetchSshByImageId(sshSettingRequest.getImage_id());

	    SshSettingInfo sshSettingInfo = tdaasUtil.fromSshSettingRequest(sshSettingRequest, existingSsh);
	    // if (existingSsh.getId() != null
	    // && StringUtils.isNotBlank(existingSsh.getId())) {
	    // sshSettingInfo.setId(existingSsh.getId());
	    // ImageAttributes image = existingSsh.getImage();
	    // sshSettingInfo.setImage(image);
	    // }
	    imagePersistenceManager.updateSsh(sshSettingInfo);
	    return TdaasUtil.convertSshInfoToResponse(sshSettingInfo);
	} catch (DbException e) {
	    log.error("unable to update ssh info in database", e);
	    throw new DirectorException("Unable to update sshsetting info in database", e);
	} catch (Exception e) {
	    throw new DirectorException("updateSshdata failed", e);
	}

    }

    public void updateSshDataById(String sshId) throws DirectorException {
	try {
	    imagePersistenceManager.updateSshById(sshId);
	} catch (Exception e) {
	    throw new DirectorException("Unable to updateSshDataById", e);
	}
    }

    public void deleteSshSetting(String sshId) throws DirectorException {
	try {
	    imagePersistenceManager.destroySshById(sshId);
	} catch (DbException e) {
	    log.error("unable to delete ssh info in database", e);
	    throw new DirectorException("Unable to delete sshsetting info in database", e);
	}

    }

    public SshSettingRequest fetchSshInfoByImageId(String image_id) throws DirectorException {
	try {
	    TdaasUtil tdaasUtil = new TdaasUtil();
	    SshSettingInfo sshInfo = imagePersistenceManager.fetchSshByImageId(image_id);

	    return tdaasUtil.toSshSettingRequest(sshInfo);
	} catch (Exception e) {
	    throw new DirectorException("Unable to updateSshDataById", e);
	}
    }

    @Override
    public TrustPolicyDraft fetchTrustpolicydraftById(String trustPolicyDraftId) {
	try {
	    return imagePersistenceManager.fetchPolicyDraftById(trustPolicyDraftId);
	} catch (DbException e) {
	    return null;
	}
    }

    @Override
    public void pullDockerImageFromRepository(String imageId) throws DirectorException {
	ImageInfo image;
	try {
	    image = imagePersistenceManager.fetchImageById(imageId);
	} catch (DbException e) {
	    log.error("Error in fetching image  ", e);
	    throw new DirectorException("Error in fetching image");
	}
	if (image == null) {
	    log.error("DockerPullTask, no image found for imageId::" + imageId);
	    throw new DirectorException("No image found for imageId");
	}

	if (!Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(image.getImage_deployments())
		|| StringUtils.isBlank(image.getRepository()) || StringUtils.isBlank(image.getTag())) {
	    log.error("Cannot Perform Docker Pull Operation in this Image ::" + imageId);
	    throw new DirectorException("Cannot Perform Docker Setup Operation in this Image");
	}
	DockerPullTask dockerPullTask = new DockerPullTask(imageId);
	/// We pull docker image asynchronously in seperate thread
	DirectorAsynchExecutor.submitTask(dockerPullTask);
    }

    @Override
    public void processUploadedDockerImage(String imageId) throws DirectorException {

	ImageInfo image = null;

	try {
	    image = imagePersistenceManager.fetchImageById(imageId);
	} catch (DbException e) {
	    log.error("Error in fetching image  ", e);

	}
	if (image == null) {
	    log.error("dockerSetup, no image found for imageId::" + imageId);
	    throw new DirectorException("dockerSetup, no image found for imageId::" + imageId);
	}

	if (!Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(image.getImage_deployments())) {
	    log.error("Image must be of docker type ::" + imageId);
	    throw new DirectorException("Image must be of docker deployment type");
	}

	if (!Constants.DEPLOYMENT_TYPE_DOCKER.equalsIgnoreCase(image.getImage_deployments())
		|| StringUtils.isBlank(image.getRepository()) || StringUtils.isBlank(image.getTag())) {
	    log.error("Cannot Perform Docker Setup Operation in this Image ::" + imageId);
	    throw new DirectorException("Cannot Perform Docker Setup Operation in this Image");
	}

	String repository = image.getRepository();
	String tag = image.getTag();

	log.info("Inside dockerSetup, repository::" + repository + " tag::" + tag);

	boolean dockerSetupOperation = false;

	String newTag = tag + Constants.SOURCE_TAG;
	try {
	    // We load docker image to engine using docker laod comand
	    dockerActionService.dockerLoad(imageId);
	    /// In order to identify orginal image later on , we tag it by
	    /// appending _source. For example
	    // busybox:latest tagged to--> busybox:latest_source
	    dockerActionService.dockerTag(imageId, repository, newTag);
	    dockerSetupOperation = true;

	} catch (DirectorException e) {
	    image.setStatus(Constants.ERROR);
	    log.error(" DockerSetup failed", e);
	} finally {
	    if (dockerSetupOperation) {
		// We finally remove orginal image before tagging i.e
		// busybox:latest
		dockerActionService.dockerRMI(imageId);
		image.setStatus(Constants.COMPLETE);
	    }
	}

	try {
	    imagePersistenceManager.updateImage(image);
	} catch (DbException e) {
	    log.error("Unable to update image action, set status in DockerSetup", e);
	}

	if (image.getStatus().equals(Constants.ERROR)) {
	    throw new DirectorException("Error while performing docker setup");
	}

    }

    @Override
    public List<ImageInfo> getStalledImages() throws DirectorException {
	List<ImageInfo> fetchImages;
	try {
	    fetchImages = imagePersistenceManager.fetchImages(null, null);
	} catch (DbException e) {
	    log.error("Error while retrieving list of images", e);
	    throw new DirectorException("Error while retrieving list of images", e);
	}
	List<ImageInfo> stalledImages = new ArrayList<ImageInfo>();
	for (ImageInfo imageInfo : fetchImages) {
	    Calendar edited_date = imageInfo.getEdited_date();
	    Calendar current_date = Calendar.getInstance();

	    long diffInMilli = current_date.getTime().getTime() - edited_date.getTime().getTime();
	    long diffInMinutes = (diffInMilli / (1000 * 60)) % 60;

	    if (ShiroUtil.subjectUsernameEquals(imageInfo.getCreated_by_user_id())
		    && Constants.IN_PROGRESS.equals(imageInfo.getStatus()) && diffInMinutes > 2) {
		if (Constants.DEPLOYMENT_TYPE_DOCKER.equals(imageInfo.image_deployments) && imageInfo.image_size == 0) {
		    continue;
		}
		stalledImages.add(imageInfo);
	    }
	}
	return stalledImages;
    }

    @Override
    public List<HashTypeObject> getImageHashType(String deploymentType) throws DirectorException {

	List<HashTypeObject> hashTypeObjects = new ArrayList<HashTypeObject>();
	HashTypeObject hashTypeObject;

	Map<String, String> allHashTypes = ImageDeploymentHashTypeCache.getAllHashTypes();
	Set<String> keySet = allHashTypes.keySet();
	for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
	    String key = (String) iterator.next();
	    hashTypeObject = new HashTypeObject(key, allHashTypes.get(key));
	    if (StringUtils.isNotBlank(deploymentType)) {
		if (deploymentType.equals(key)) {
		    hashTypeObjects.add(hashTypeObject);
		    break;
		}
	    } else {
		hashTypeObjects.add(hashTypeObject);
	    }
	}
	return hashTypeObjects;
    }

	/**
	 * Update ImageInformation
	 * @param imageInfo updated image information
	 * @throws DirectorException
	 */
	public void updateImageMetadata(ImageInfo imageInfo)
			throws DirectorException {
		try {
			imagePersistenceManager.updateImage(imageInfo);
		} catch (DbException e) {
			log.error("Error while updating metadata for uploaded image : " + e);
			throw new DirectorException("Cannot update image meta data", e);
		}
	}

}
