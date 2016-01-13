package com.intel.director.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.dnault.xmlpatch.internal.Log;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.SearchImagesRequest;
import com.intel.director.api.SearchImagesResponse;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.MountImage;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.ImageService;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class UnmountImageHandler {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(UnmountImageHandler.class);

	private ImageService imageService = new ImageServiceImpl();
	private IPersistService persistService = new DbServiceImpl();

	public void unmountUnusedImages() {
		log.info("MAIN : Fetching remote hosts that are mounted");
		List<String> mountedImageIds = fetchMountedImages();
		if (mountedImageIds == null || mountedImageIds.isEmpty()) {
			log.info("MAIN : No mouted remote hosts found. Returning.");
			return;
		}
		log.info("MAIN : Found " + mountedImageIds.size() + " images");
		String timeout = fetchSessionTimeout();
		if (StringUtils.isBlank(timeout)) {
			log.info("MAIN : No timeout found. returning");
			return;
		}
		log.info("MAIN : timeout = " + timeout);
		List<String> imagesToBeUnmounted = fetchImagesToBeUnmounted(
				mountedImageIds, timeout);
		if (imagesToBeUnmounted.isEmpty()) {
			log.info("MAIN : No remote hosts to unmount. Returning");
			return;
		}
		log.info("MAIN : Number of images to be unmounted = "
				+ imagesToBeUnmounted.size());
		for (String imageId : imagesToBeUnmounted) {
			String mountPath = TdaasUtil.getMountPath(imageId);
			log.info("MAIN : Unmounting image: " + imageId);
			int exitCode = MountImage.unmountRemoteSystem(mountPath);
			if (exitCode == 0) {
				//update the db too
				try {
					ImageInfo imageInfo = persistService.fetchImageById(imageId);
					imageInfo.setMounted_by_user_id(null);
					persistService.updateImage(imageInfo);
				} catch (DbException e) {
					log.error("Unable to set the mounted by user to null for image "+imageId, e);
				}
				log.info("MAIN : Successfuly unmounted image " + imageId);
			} else {
				log.info("MAIN : Error unmounting image " + imageId);
			}
		}

	}

	private List<String> fetchMountedImages() {
		SearchImagesRequest searchImagesRequest = new SearchImagesRequest();
		searchImagesRequest.deploymentType = Constants.DEPLOYMENT_TYPE_BAREMETAL;
		SearchImagesResponse searchImagesResponse = null;
		try {
			searchImagesResponse = imageService
					.searchImages(searchImagesRequest);
		} catch (DirectorException e) {
			log.error("Error fetching images", e);
		}
		if (searchImagesResponse == null) {
			return null;
		}
		List<String> mountedImageIds = new ArrayList<>();
		List<ImageInfo> images = searchImagesResponse.images;
		for (ImageInfo imageInfo : images) {
			// Check if the image is mounted
			if (StringUtils.isNotBlank(imageInfo.mounted_by_user_id)) {
				mountedImageIds.add(imageInfo.id);
			}
		}
		return mountedImageIds;
	}

	private String fetchSessionTimeout() {
		String timeout = null;
		try {
			Configuration configuration = ConfigurationFactory
					.getConfiguration();
			timeout = configuration.get("login.token.expires.minutes", "30");
			Log.info("timeout from config is " + timeout);
			if (StringUtils.isBlank(timeout)) {
				Log.info("Setting timeout to default");
				timeout = "30";
			}
		} catch (IOException e) {
			log.error("Unable to fetch configuration", e);
		}
		return timeout;
	}

	private List<String> fetchImagesToBeUnmounted(List<String> mountedImageIds,
			String timeout) {
		List<String> imagesToBeUnmounted = new ArrayList<>();

		for (String imageId : mountedImageIds) {
			try {
				TrustPolicyDraft trustPolicyDraft = persistService
						.fetchPolicyDraftForImage(imageId);
				Date trustPolicyDraftEditDate = trustPolicyDraft
						.getEdited_date();
				Date currentDate = new Date();
				long diffMinutes = (currentDate.getTime() - trustPolicyDraftEditDate
						.getTime()) / (60 * 1000) % 60;
				
				log.info("DIFF : "+diffMinutes +" Timeout = "+new Long(timeout).longValue());
				if (diffMinutes > new Long(timeout).longValue()) {
					imagesToBeUnmounted.add(imageId);
				}
			} catch (DbException e) {
				log.error("Error fetching trust policy draft for image : "
						+ imageId, e);
			}
		}
		return imagesToBeUnmounted;
	}
}
