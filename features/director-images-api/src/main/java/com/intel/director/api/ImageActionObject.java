package com.intel.director.api;

import java.util.List;

public class ImageActionObject {
	
	private String id;
	private String image_id;
	private int action_count;
	private int action_completed;
	private int action_size;
	private int action_size_max;
	private List<ImageActionActions> action;
	private String current_task_status;
	private String current_task_name;
	
	
	public List<ImageActionActions> getAction() {
		return action;
	}
	public void setAction(List<ImageActionActions> action) {
		this.action = action;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getImage_id() {
		return image_id;
	}
	public void setImage_id(String image_id) {
		this.image_id = image_id;
	}
	public int getAction_count() {
		return action_count;
	}
	public void setAction_count(int action_count) {
		this.action_count = action_count;
	}
	public int getAction_completed() {
		return action_completed;
	}
	public void setAction_completed(int action_completed) {
		this.action_completed = action_completed;
	}
	public int getAction_size() {
		return action_size;
	}
	public void setAction_size(int action_size) {
		this.action_size = action_size;
	}
	public int getAction_size_max() {
		return action_size_max;
	}
	public void setAction_size_max(int action_size_max) {
		this.action_size_max = action_size_max;
	}
	public String getCurrent_task_status() {
		return current_task_status;
	}
	public void setCurrent_task_status(String current_task_status) {
		this.current_task_status = current_task_status;
	}
	public String getCurrent_task_name() {
		return current_task_name;
	}
	public void setCurrent_task_name(String current_task_name) {
		this.current_task_name = current_task_name;
	}
	@Override
	public String toString() {
		return "ImageActionObject [id=" + id + ", image_id=" + image_id
				+ ", action_count=" + action_count + ", action_completed="
				+ action_completed + ", action_size=" + action_size
				+ ", action_size_max=" + action_size_max + ", action=" + action
				+ ", current_task_status=" + current_task_status
				+ ", current_task_name=" + current_task_name + "]";
	}


}
