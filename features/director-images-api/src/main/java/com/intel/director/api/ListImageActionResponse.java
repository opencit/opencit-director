package com.intel.director.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ListImageActionResponse extends GenericResponse{
	
	 @JsonProperty("image_action_history_list")
	List<ImageActionHistoryResponse> imageActionResponseList = new ArrayList<ImageActionHistoryResponse>();

	public List<ImageActionHistoryResponse> getImageActionResponseList() {
		return imageActionResponseList;
	}

	public void setImageActionResponseList(
			List<ImageActionHistoryResponse> imageActionResponseList) {
		this.imageActionResponseList = imageActionResponseList;
	}


	
	
	
}
