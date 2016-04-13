package com.intel.director.images.mount.impl;

import com.intel.director.api.ImageAttributes;
import com.intel.director.common.MountImage;

public class DockerMountServiceImpl extends MountServiceImpl {

	public DockerMountServiceImpl(ImageAttributes imageInfo) {
		super(imageInfo);
	}

	@Override
	public int mount() {
		return MountImage.mountDocker(mountPath, imageInfo.getRepository(), imageInfo.getTag() + "_source");
	}

	@Override
	public int unmount() {
		return MountImage.unmountDocker(mountPath, imageInfo.repository, imageInfo.tag + "_source");
	}

}
