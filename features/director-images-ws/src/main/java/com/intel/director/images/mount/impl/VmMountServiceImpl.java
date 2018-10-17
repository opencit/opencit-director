package com.intel.director.images.mount.impl;

import com.intel.director.api.ImageAttributes;
import com.intel.director.common.MountImage;

public class VmMountServiceImpl extends MountServiceImpl {

	public VmMountServiceImpl(ImageAttributes imageInfo) {
		super(imageInfo);
	}

	@Override
	public int mount()  {
		return MountImage.mountImage(imageInfo.getLocation() + imageInfo.getImage_name(), mountPath);		
	}

	@Override
	public int unmount() {
		return MountImage.unmountImage(mountPath);
	}

}
