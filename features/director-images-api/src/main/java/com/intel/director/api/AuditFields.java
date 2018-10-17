package com.intel.director.api;

import java.util.Calendar;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class AuditFields {

	protected String created_by_user_id;
	
	@JsonSerialize(using = DirectorCalenderSerializer.class)
	protected Calendar created_date;

	protected String edited_by_user_id;
	
	@JsonSerialize(using = DirectorCalenderSerializer.class)
	protected Calendar edited_date;

	public AuditFields(String created_by_user_id, Calendar created_date,
			String edited_by_user_id, Calendar edited_date) {
		super();
		this.created_by_user_id = created_by_user_id;
		this.created_date = created_date;
		this.edited_by_user_id = edited_by_user_id;
		this.edited_date = edited_date;
	}

	public AuditFields() {
	}

	public String getCreated_by_user_id() {
		return created_by_user_id;
	}

	public void setCreated_by_user_id(String created_by_user_id) {
		this.created_by_user_id = created_by_user_id;
	}

	public Calendar getCreated_date() {
		return created_date;
	}

	public void setCreated_date(Calendar created_date) {
		this.created_date = created_date;
	}

	public String getEdited_by_user_id() {
		return edited_by_user_id;
	}

	public void setEdited_by_user_id(String edited_by_user_id) {
		this.edited_by_user_id = edited_by_user_id;
	}

	public Calendar getEdited_date() {
		return edited_date;
	}

	public void setEdited_date(Calendar edited_date) {
		this.edited_date = edited_date;
	}

}
