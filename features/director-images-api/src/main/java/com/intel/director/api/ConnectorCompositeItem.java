package com.intel.director.api;



public class ConnectorCompositeItem {


	public String key;
	public ConnectorItem[] optionList;
	public String value;
	public String placeholder;
	public String id;
	
	
	
	
	public ConnectorCompositeItem() {
		super();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPlaceholder() {
		return placeholder;
	}
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	

	
	public ConnectorItem[] getOptionList() {
		return optionList;
	}
	public void setOptionList(ConnectorItem[] optionList) {
		this.optionList = optionList;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	
	public ConnectorCompositeItem(String key, ConnectorItem[] optionList) {
		super();
		this.key = key;
		this.optionList = optionList;
	}

	
	
}
