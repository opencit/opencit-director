package com.intel.director.api;

import java.util.List;

public class ImageActionRequest {
	public String image_id;
	public String action_id;
	List<ImageActionActions> actions ;
	public String getImage_id() {
		return image_id;
	}
	public void setImage_id(String image_id) {
		this.image_id = image_id;
	}
	public String getAction_id() {
		return action_id;
	}
	public void setAction_id(String action_id) {
		this.action_id = action_id;
	}
	public List<ImageActionActions> getActions() {
		return actions;
	}
	public void setActions(List<ImageActionActions> actions) {
		this.actions = actions;
	}
	
}
