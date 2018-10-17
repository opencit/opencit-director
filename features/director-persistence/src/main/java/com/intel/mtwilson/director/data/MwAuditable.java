package com.intel.mtwilson.director.data;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.UuidGenerator;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class MwAuditable {
	
	
	@Id
	@UuidGenerator(name="UUID")
	@GeneratedValue(generator="UUID")
  	@Column(name = "ID", length = 36)
	protected String id;
	
	@Column(name = "CREATED_BY_USER_ID" , length = 36)
	protected String createdByUserId;
	
	@Column(name = "CREATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	protected Calendar createdDate;
	
	@Column(name = "EDITED_BY_USER_ID" , length = 36)
	protected String editedByUserId;
	
	@Column(name = "EDITED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	protected Calendar editedDate;
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(String createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public Calendar getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Calendar createdDate) {
		this.createdDate = createdDate;
	}

	public String getEditedByUserId() {
		return editedByUserId;
	}

	public void setEditedByUserId(String editedByUserId) {
		this.editedByUserId = editedByUserId;
	}

	public Calendar getEditedDate() {
		return editedDate;
	}

	public void setEditedDate(Calendar editedDate) {
		this.editedDate = editedDate;
	} 
	
	
	
}
