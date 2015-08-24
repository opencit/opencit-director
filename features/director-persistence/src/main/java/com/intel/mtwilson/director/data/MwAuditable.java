package com.intel.mtwilson.director.data;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

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
	
	@Column(name = "CREATED_DATE" , length = 36)
	protected Date createdDate;
	
	@Column(name = "EDITED_BY_USER_ID" , length = 36)
	protected String editedByUserId;
	
	@Column(name = "EDITED_DATE" , length = 36)
	protected Date editedDate;
	
	
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

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getEditedByUserId() {
		return editedByUserId;
	}

	public void setEditedByUserId(String editedByUserId) {
		this.editedByUserId = editedByUserId;
	}

	public Date getEditedDate() {
		return editedDate;
	}

	public void setEditedDate(Date editedDate) {
		this.editedDate = editedDate;
	} 
	
	
	
}
