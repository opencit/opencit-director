package com.intel.director.images.mount.impl;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang.StringUtils;

import com.intel.director.api.ImageAttributes;
import com.intel.director.common.MountImage;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

public class DockerMountServiceImpl extends MountServiceImpl {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DockerMountServiceImpl.class);

    public DockerMountServiceImpl(ImageAttributes imageInfo) {
	super(imageInfo);
    }

    @Override
    public int mount() {
	int exitCode = 1;
	String command = "docker images --no-trunc | awk -v rep=\"" + imageInfo.getRepository()
		+ "\" '$1 == rep' | awk -v tag=\"" + imageInfo.getTag() + "_source" + "\" '$2 == tag {print $3}'";
	Result result = null;
	try {
	    result = ExecUtil.executeQuoted("/bin/sh", "-c", command);
	    exitCode = result.getExitCode();
	} catch (ExecuteException e) {
	    log.error("Error finding image id of docker image", e);
	    return 1;
	} catch (IOException e) {
	    log.error("Error finding image id of docker image", e);
	    return 1;
	}
	String imageId = null;
	if (result.getStderr() != null && StringUtils.isNotEmpty(result.getStderr())) {
	    log.error(result.getStderr());
	    exitCode = 1;
	    return exitCode;
	} else {
	    String imageIdStr = result.getStdout();
	    imageId = imageIdStr.substring(imageIdStr.indexOf(":") + 1);
	}

	exitCode = MountImage.mountDocker(mountPath, imageId);
	return exitCode;
    }

    @Override
    public int unmount() {
	return MountImage.unmountDocker(mountPath, imageInfo.repository, imageInfo.tag + "_source");
    }

}
