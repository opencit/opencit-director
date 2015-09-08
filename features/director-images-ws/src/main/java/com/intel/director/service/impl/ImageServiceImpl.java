package com.intel.director.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageStoreManager;
import com.intel.director.api.ImageStoreRequest;
import com.intel.director.api.ImageStoreResponse;
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

	@Override
	public SearchFilesInImageResponse searchFilesInImage(
			SearchFilesInImageRequest searchFilesInImageRequest) {
		SearchFilesInImageResponse filesInImageResponse = new SearchFilesInImageResponse();
		Collection<File> listFilesAndDirs = new ArrayList<File>(
				Arrays.asList(new File(searchFilesInImageRequest.getDir())
						.listFiles()));
		

		List<String> files = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		files.add("<ul class=\"jqueryFileTree\" style=\"display: none;\">");
		int counter = 0;
		for (File file : listFilesAndDirs) {
			// if(counter++ == 0){
			// continue;
			// }
			builder.delete(0, builder.length());
			try {
				String checkbox = null;
				String liClass = null;
				String toggleIcon=null;
			
				
				if (file.isDirectory()) {
					checkbox = "<input type=\"checkbox\" name=\"directory_"
							+ file.getCanonicalPath() + "\" id=\""
							+ file.getCanonicalPath() + "\"  style=\"float:left;\" />";
					liClass = "directory collapsed";
					toggleIcon=	"<img src=\"images/Actions-arrow-right-icon-small.png\" title=\""+file.getName()+"\"  id=\"toggle_"+ file.getCanonicalPath() +"\"   onclick=\"toggleState(this)\" />";
				} else {
					checkbox = "<input type=\"checkbox\" name=\"file_"
							+ file.getCanonicalPath() + "\" id=\""
							+ file.getCanonicalPath() + "\" style=\"float:left;\" />";
					liClass = "file";
					
				}

				builder.append("<li class=\""+liClass+"\">");
				builder.append(checkbox);
				builder.append("<a href=\"#\" rel=\"");
				builder.append(file.getCanonicalPath());
				if(file.isDirectory()){
					builder.append("/\"  style=\"float:left;\">");
				}else{
					builder.append("/\"\">");
				}
				/////builder.append("/\"  style=\"float:left;\">");
				builder.append(file.getName());
				builder.append("</a>");
				if(toggleIcon!=null){
				builder.append(toggleIcon);	
				}
				builder.append("</li>");

				files.add(builder.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		files.add("</ul>");
		filesInImageResponse.files = files;
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
	public ImageStoreResponse uploadImageToImageStore(
			ImageStoreRequest imageStoreUploadRequest)
			throws DirectorException, ImageStoreException {
		ImageStoreResponse uploadImage = imageStoreManager
				.uploadImage(imageStoreUploadRequest);
		// save image upload details to mw_image_upload
		return uploadImage;
	}

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
			trustPolicyDraft = imagePersistenceManager.fetchPolicyDraftForImage(trustpolicyDraftEditRequest.imageId);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String draft = trustPolicyDraft.getTrust_policy_draft();
		
		try {
			draft = DirectorUtil.patch(draft, trustpolicyDraftEditRequest.patch);
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
	public ImageServiceImpl(IPersistService imagePersistenceManager,
			ImageStoreManager imageStoreManager) {
		this.imagePersistenceManager = imagePersistenceManager;
		this.imageStoreManager = imageStoreManager;
	}

}
