package com.intel.director.images.mount.impl;

import com.intel.director.api.ImageAttributes;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.images.mount.MountService;

public class GenericMountServiceImpl implements MountService {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GenericMountServiceImpl.class);

	private MountService delegate = null;
	private ImageAttributes imageInfo = null;

	public GenericMountServiceImpl(MountService mountService, ImageAttributes imageInfo) {
		delegate = mountService;
		this.imageInfo = imageInfo;
	}

	@Override
	public int mount() throws DirectorException {
		log.info("Mounting start for image {} with deployment type {}", imageInfo.id, imageInfo.image_deployments);
		int exitCode = delegate.mount();
		if (exitCode != 0) {
			throw new DirectorException("Error mounting image " + imageInfo.id);
		}
		log.info("Mounting end  for image {} with deployment type {}", imageInfo.id, imageInfo.image_deployments);
		return exitCode;
	}

	@Override
	public int unmount() throws DirectorException {
		log.info("Unounting start for image {} with deployment type {}", imageInfo.id, imageInfo.image_deployments);
		int exitCode = delegate.unmount();
		if (exitCode != 0) {
			throw new DirectorException("Error unmounting image " + imageInfo.id);
		}

		log.info("Unounting end  for image {} with deployment type {}", imageInfo.id, imageInfo.image_deployments);
		return exitCode;
	}

}
