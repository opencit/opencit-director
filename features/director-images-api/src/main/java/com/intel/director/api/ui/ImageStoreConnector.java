package com.intel.director.api.ui;

import java.util.List;
import java.util.Map;

import com.intel.director.api.ConnectorCompositeItem;
import com.intel.director.api.ConnectorKey;

public class ImageStoreConnector {
	private String name;
	private String driver;
	private List<ConnectorKey> properties;
	private Map<String, String> supported_artifacts;
	private List<ConnectorCompositeItem> connectorCompositeItem;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public List<ConnectorKey> getProperties() {
		return properties;
	}
	public void setProperties(List<ConnectorKey> properties) {
		this.properties = properties;
	}
	public Map<String, String> getSupported_artifacts() {
		return supported_artifacts;
	}
	public void setSupported_artifacts(Map<String, String> supported_artifacts) {
		this.supported_artifacts = supported_artifacts;
	}
	public List<ConnectorCompositeItem> getConnectorCompositeItem() {
		return connectorCompositeItem;
	}
	public void setConnectorCompositeItem(
			List<ConnectorCompositeItem> connectorCompositeItem) {
		this.connectorCompositeItem = connectorCompositeItem;
	}
	

}
