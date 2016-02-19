package com.intel.director.api;

import java.util.List;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;

public class ImageActionRequest {
	public String image_id;
	public String action_id;
	List<ImageActionTask> actions ;
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
	public List<ImageActionTask> getActions() {
		return actions;
	}
	public void setActions(List<ImageActionTask> actions) {
		this.actions = actions;
	}
	
	public String vaidate(){
		String error=null;
		if(!ValidationUtil.isValidWithRegex(getImage_id(),RegexPatterns.UUID)){
		error = "Image Id is empty or not in uuid format";
			return error;
		}
		boolean hasActions=false;
		if(actions!=null ){
			if(actions.size()>0){
				hasActions=true;
			}
		}
		if(!hasActions){
			error = "Actions are empty";
		}
		return error;
	}
	
}
