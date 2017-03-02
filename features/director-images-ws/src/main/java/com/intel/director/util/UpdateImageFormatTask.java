package com.intel.director.util;

import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.common.exception.DirectorException;
import com.intel.director.service.ImageService;
import com.intel.director.service.impl.ImageServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by GS-0681 on 21-02-2017.
 */
public class UpdateImageFormatTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(UpdateImageFormatTask.class);
    private String imageId;
    private ImageInfo imageInfo;

    public UpdateImageFormatTask(String imageId) {
        this.imageId = imageId;
    }

    public UpdateImageFormatTask(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
        if(imageInfo != null){
            imageId = imageInfo.getId();
        }
    }

    @Override
    public void run() {
        ImageService imageService = new ImageServiceImpl();
        if (imageInfo == null) {
            try {
                imageInfo = imageService.getImageDetails(imageId);
                if (imageInfo == null) {
                    log.error("Invalid image id : {}", imageId);
                    return;
                }
            } catch (DirectorException e) {
                log.error("Error fetching image details for id: {}", imageId, e);
                return;
            }
        }


        String uploadedFile = imageInfo.getLocation() + imageInfo.getImage_name();
        log.info("Finding file type of file : {}", uploadedFile);
        String result = DirectorUtil.executeShellCommand("file " + uploadedFile);
        log.info("Result of the command: {} is : {}", "file " + uploadedFile, result);
        String imageFormat = null;
        if(StringUtils.isBlank(result)){
            log.error("No result for file command for file : {}", uploadedFile);
            return;
        }
		if (result.contains(Constants.IMAGE_FORMAT_RESULT_QCOW)) {
			log.info("qcow2 image");
			imageFormat = "qcow2";
		} else if (result.contains(Constants.IMAGE_FORMAT_RESULT_RAW)) {
			imageFormat = "raw";
		} else if (result.contains(Constants.IMAGE_FORMAT_RESULT_VDI)) {
			imageFormat = "vdi";
		} else if (result.contains(Constants.IMAGE_FORMAT_RESULT_VHD) || result.contains(Constants.IMAGE_FORMAT_RESULT_VHDX)){				
			imageFormat = "vhd";
		}

        if (StringUtils.isBlank(imageFormat)) {
            log.error("Could not find the image format from the result : {}", result);
            return;
        }

        imageInfo.setImage_format(imageFormat);
        try {
            imageService.updateImageMetadata(imageInfo);
            log.info("Successfully updated the image format for image id: {} to {}", imageId, imageFormat);
        } catch (DirectorException e) {
            log.error("Unable to update the image format for image id : {}", imageId, e);
        }
    }


}