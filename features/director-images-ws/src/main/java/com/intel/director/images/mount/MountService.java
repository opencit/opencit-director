package com.intel.director.images.mount;

import com.intel.director.images.exception.DirectorException;

public interface MountService {

	public int mount() throws DirectorException;
	public int unmount()  throws DirectorException;
}
