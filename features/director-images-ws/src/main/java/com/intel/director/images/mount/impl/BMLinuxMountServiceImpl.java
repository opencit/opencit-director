package com.intel.director.images.mount.impl;

import com.github.dnault.xmlpatch.internal.Log;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.SshSettingInfo;
import com.intel.director.common.MountImage;
import com.intel.director.images.exception.DirectorException;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class BMLinuxMountServiceImpl extends MountServiceImpl {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BMLinuxMountServiceImpl.class);

	public BMLinuxMountServiceImpl(ImageAttributes imageInfo) {
		super(imageInfo);
	}

	@Override
	public int mount() throws DirectorException {
		Log.info("Mounting live host - Linux");
		IPersistService persistService = new DbServiceImpl();
		SshSettingInfo info;
		try {
			info = persistService.fetchSshByImageId(imageInfo.id);
		} catch (DbException e) {
			String msg = "Error fetching ssh settings for BM image";
			log.error(msg, e);
			throw new DirectorException(msg, e);
		}
		log.info("BM Live host : " + info.toString());
		persistService = null;
		return MountImage.mountRemoteSystem(info.getIpAddress(), info.getUsername(), info.getSshPassword().getKey(),
				mountPath);
	}

	@Override
	public int unmount() {
		log.info("unmounting image mounted at {}", mountPath);
		return MountImage.unmountRemoteSystem(mountPath);
	}

}
