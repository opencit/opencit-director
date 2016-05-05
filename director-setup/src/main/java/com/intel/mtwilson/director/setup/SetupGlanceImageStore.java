package com.intel.mtwilson.director.setup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.intel.director.api.ConnectorKey;
import com.intel.director.api.ConnectorProperties;
import com.intel.director.api.ImageStoreDetailsTransferObject;
import com.intel.director.api.ImageStoreFilter;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorPropertiesCache;
import com.intel.director.service.ImageStoresService;
import com.intel.director.service.impl.ImageStoresServiceImpl;
import com.intel.mtwilson.setup.AbstractSetupTask;

public class SetupGlanceImageStore extends AbstractSetupTask {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupGlanceImageStore.class);
	private String glanceUrl;
	private String authUrl;

	private String tenant;
	private String user;
	private String password;
	private static final String defaultStoreName = "GLANCE_STORE_FROM_3_0";
	private ImageStoresService imageStoresService = new ImageStoresServiceImpl();

	@Override
	protected void configure() throws Exception {
		glanceUrl = DirectorPropertiesCache.getValue(Constants.GLANCE_API_ENDPOINT);
		authUrl = DirectorPropertiesCache.getValue(Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT);
		user = DirectorPropertiesCache.getValue(Constants.GLANCE_IMAGE_STORE_USERNAME);
		password = DirectorPropertiesCache.getValue(Constants.GLANCE_IMAGE_STORE_PASSWORD);
		tenant = DirectorPropertiesCache.getValue(Constants.GLANCE_TENANT_NAME);
	 
		if (isAnyGlancePropertyNotSet()) {
			log.debug("Some props for glance are not set");
			configuration("Some of the Glance properties are not set. Not importing");
			return;
		}
	}

	@Override
	protected void validate() throws Exception {
		log.debug("Set api={}, auth={}, user={}, paswd={}, tenant={}", glanceUrl, authUrl, user, password, tenant);
		ImageStoreFilter imageStoreFilter = new ImageStoreFilter();
		imageStoreFilter.setName(defaultStoreName);
		List<ImageStoreTransferObject> imageStores = imageStoresService.getImageStores(imageStoreFilter);
		if (imageStores.size() == 0) {
			validation("Default image store not yet set. Would be creating it now ");
		}
	}

	@Override
	protected void execute() throws Exception {
		log.debug("Default store {} not present. Creating new", defaultStoreName);
		ImageStoreTransferObject imageStoreTransferObject = new ImageStoreTransferObject();
		imageStoreTransferObject.setName(defaultStoreName);
		imageStoreTransferObject.setConnector(Constants.CONNECTOR_GLANCE);
		ConnectorProperties connectorByName = ConnectorProperties.getConnectorByName(Constants.CONNECTOR_GLANCE);
		String[] arrayOfArtifacts = connectorByName.getSupported_artifacts().keySet().toArray(new String[3]);
		imageStoreTransferObject.setArtifact_types(arrayOfArtifacts);
		ConnectorKey[] properties = connectorByName.getProperties();
		List<ImageStoreDetailsTransferObject> defaultProperties = new ArrayList<>(properties.length);
		imageStoreTransferObject.setImage_store_details(defaultProperties);
		for (ConnectorKey connectorKey : properties) {
			ImageStoreDetailsTransferObject detailsTransferObject = new ImageStoreDetailsTransferObject();
			defaultProperties.add(detailsTransferObject);
			detailsTransferObject.setKey(connectorKey.getKey());
			switch (connectorKey.getKey()) {
			case Constants.GLANCE_API_ENDPOINT:
				detailsTransferObject.setValue(glanceUrl);
				break;
			case Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT:
				detailsTransferObject.setValue(authUrl);
				break;
			case Constants.GLANCE_IMAGE_STORE_USERNAME:
				detailsTransferObject.setValue(user);
				break;
			case Constants.GLANCE_IMAGE_STORE_PASSWORD:
				detailsTransferObject.setValue(password);
				break;
			case Constants.GLANCE_TENANT_NAME:
				detailsTransferObject.setValue(tenant);
				break;
			case Constants.GLANCE_VISIBILITY:
				detailsTransferObject.setValue("public");
				break;
			}

		}

		imageStoreTransferObject = imageStoresService.createImageStore(imageStoreTransferObject);
		log.info("Successfully created default glance image store {}", imageStoreTransferObject.id);
	}

	private boolean isAnyGlancePropertyNotSet() {
		if (StringUtils.isBlank(glanceUrl) || StringUtils.isBlank(authUrl) || StringUtils.isBlank(user)
				|| StringUtils.isBlank(password) || StringUtils.isBlank(tenant)) {

			return true;
		}
		return false;

	}

}
