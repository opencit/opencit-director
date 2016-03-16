package com.intel.mtwilson.director.data;


import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.UuidGenerator;


@Entity
@Cacheable(false)
@Table(name = "MW_IMAGE_ACTION")
public class MwImageAction {
	
	@Id
	@UuidGenerator(name="UUID")
	@GeneratedValue(generator="UUID")
  	@Column(name = "id", length = 36)
	protected String id;
	
	
	
	public MwImageAction(String image_id, String action, int action_count,
			int action_completed, long action_size, long action_size_max) {
		super();
		this.image_id = image_id;
		this.action = action;
		this.action_count = action_count;
		this.action_completed = action_completed;
		this.action_size = action_size;
		this.action_size_max = action_size_max;
	}

	public MwImageAction() {
		super();
	}

	@Column(name = "image_id")
	private String image_id;
	
	@Column(name = "action", length=1000)
	private String action;
	
	@Column(name = "action_count")
	private int action_count;

	@Column(name = "action_completed")
	private int action_completed;
	
	@Column(name = "action_size")
	private long action_size;

	@Column(name = "action_size_max")
	private long action_size_max;
	
	@Column(name= "current_task_name")
	private String current_task_name;
	
	@Column(name= "current_task_status")
	private String current_task_status;
	
	@Column(name = "EXECUTION_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Calendar executionTime;
	
	@Column(name = "CREATED_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Calendar createdTime;	
	

	

	public java.util.Calendar getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(java.util.Calendar executionTime) {
		this.executionTime = executionTime;
	}

	public long getAction_size() {
		return action_size;
	}

	public void setAction_size(long action_size) {
		this.action_size = action_size;
	}

	public long getAction_size_max() {
		return action_size_max;
	}

	public void setAction_size_max(long action_size_max) {
		this.action_size_max = action_size_max;
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

	public String getAction() {
		return action;
	}

	public void setAction(String actions) {
		this.action = actions;
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

	public String getCurrent_task_name() {
		return current_task_name;
	}

	public void setCurrent_task_name(String current_task_name) {
		this.current_task_name = current_task_name;
	}

	public String getCurrent_task_status() {
		return current_task_status;
	}

	public void setCurrent_task_status(String current_task_status) {
		this.current_task_status = current_task_status;
	}
	
	public java.util.Calendar getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(java.util.Calendar createdTime) {
		this.createdTime = createdTime;
	}

	@Override
	public String toString() {
		return "MwImageAction [id=" + id + ", image_id=" + image_id
				+ ", action=" + action + ", action_count=" + action_count
				+ ", action_completed=" + action_completed + ", action_size="
				+ action_size + ", action_size_max=" + action_size_max
				+ ", current_task_name=" + current_task_name
				+ ", current_task_status=" + current_task_status + ", date="
				+ executionTime + "]";
	}

	
	
}
