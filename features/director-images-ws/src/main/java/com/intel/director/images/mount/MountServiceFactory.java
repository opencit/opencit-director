package com.intel.director.images.mount;

import com.intel.director.api.ImageAttributes;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.images.mount.impl.BMLinuxMountServiceImpl;
import com.intel.director.images.mount.impl.DockerMountServiceImpl;
import com.intel.director.images.mount.impl.GenericMountServiceImpl;
import com.intel.director.images.mount.impl.VmMountServiceImpl;

public class MountServiceFactory {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MountServiceFactory.class);

	public static MountService getMountService(ImageAttributes imageInfo) throws DirectorException {
		MountService mountService = null;
		log.info("Finding mount service");
		if (imageInfo.getImage_deployments().equals(Constants.DEPLOYMENT_TYPE_VM)) {
			mountService = new VmMountServiceImpl(imageInfo);
			log.info("VM mount service");
		} else if (imageInfo.getImage_deployments().equals(Constants.DEPLOYMENT_TYPE_DOCKER)) {
			mountService = new DockerMountServiceImpl(imageInfo);
			log.info("Docker mount service");
		} else if (imageInfo.getImage_deployments().equals(
				Constants.DEPLOYMENT_TYPE_BAREMETAL)) {
			mountService = new BMLinuxMountServiceImpl(imageInfo);
			log.info("Linux BM mount service");
		}
		if (mountService == null) {
			throw new DirectorException("Unable to find mount service for image " + imageInfo.id);
		}
		MountService genericMountService = new GenericMountServiceImpl(mountService, imageInfo);
		return genericMountService;

	}
}
