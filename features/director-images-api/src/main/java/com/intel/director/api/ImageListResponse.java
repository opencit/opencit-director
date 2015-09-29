package com.intel.director.api;

import com.intel.director.api.ImageListResponseInfo;
import java.util.List;

public class ImageListResponse {

	public List<ImageListResponseInfo> images;

	public List<ImageListResponseInfo> getImages() {
		return images;
	}

	public void setImages(List<ImageListResponseInfo> images) {
		this.images = images;
	}
	
	
}
