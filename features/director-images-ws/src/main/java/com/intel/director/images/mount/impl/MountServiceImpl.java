package com.intel.director.images.mount.impl;

import com.intel.director.api.ImageAttributes;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.images.mount.MountService;
import com.intel.director.util.TdaasUtil;

public abstract class MountServiceImpl implements MountService {
	protected ImageAttributes imageInfo = null;
	protected String mountPath = null;
	
	public MountServiceImpl(ImageAttributes imageInfo) {
		super();
		this.imageInfo = imageInfo;
		mountPath = getMountPath();
	}
	
	private String getMountPath() {
		if (imageInfo.getImage_format() == null) {
			mountPath = TdaasUtil.getMountPath(imageInfo.id);
		} else if (Constants.DEPLOYMENT_TYPE_DOCKER.equals(imageInfo.getImage_deployments())) {
			mountPath = TdaasUtil.getMountPath(imageInfo.id);
		} else {
			mountPath = DirectorUtil.getMountPath(imageInfo.id);
		}
		return mountPath;
	}

}
