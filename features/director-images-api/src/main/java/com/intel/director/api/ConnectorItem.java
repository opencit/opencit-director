package com.intel.director.api;

public class ConnectorItem {
	
	public int id;
	public String value;
	public String displayName;
	
	
	
	
	public ConnectorItem() {
		super();
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public ConnectorItem(int id, String value, String displayName) {
		super();
		this.id = id;
		this.value = value;
		this.displayName = displayName;
	}
	
	
	
	
}
