package com.intel.director.api;

import com.intel.director.api.ui.ImageInfo;

public class ImageInfoDetailedResponse extends ImageInfo {

	boolean isActionEntryCreated =false;

	public boolean isActionEntryCreated() {
		return isActionEntryCreated;
	}

	public void setActionEntryCreated(boolean isActionEntryCreated) {
		this.isActionEntryCreated = isActionEntryCreated;
	}
	
	
}
