package com.intel.mtwilson.director.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.ConnectorKey;
import com.intel.director.api.ConnectorProperties;
import com.intel.director.api.ImageStoreDetailsTransferObject;
import com.intel.director.api.ImageStoreFilter;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorPropertiesCache;
import com.intel.director.service.ImageStoresService;
import com.intel.director.service.impl.ImageStoresServiceImpl;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.setup.AbstractSetupTask;

public class SetupGlanceImageStore extends AbstractSetupTask {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupGlanceImageStore.class);
	private String glanceUrl;
	private String authUrl;

	private String tenant;
	private String user;
	private String password;
	private Map<String, String> envMap = new HashMap<String, String>();
	private static final String defaultStoreName = "GLANCE_STORE_FROM_3_0";
	private ImageStoresService imageStoresService = new ImageStoresServiceImpl();

	@Override
	protected void configure() throws Exception {
		glanceUrl = DirectorPropertiesCache.getValue(Constants.GLANCE_API_ENDPOINT);
		authUrl = DirectorPropertiesCache.getValue(Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT);
		user = DirectorPropertiesCache.getValue(Constants.GLANCE_IMAGE_STORE_USERNAME);
		password = DirectorPropertiesCache.getValue(Constants.GLANCE_IMAGE_STORE_PASSWORD);
		tenant = DirectorPropertiesCache.getValue(Constants.GLANCE_TENANT_NAME);
		
		String userHome = System.getProperty("user.home");
		if (StringUtils.isBlank(userHome)) {
			userHome = "/root";
			log.info("Default User home = {}", userHome);
		} else {
			log.info("User home is found from Sys.getProp {}", userHome);
		}

		String envPath = userHome + File.separator + "director.env";
		log.info("Env path = {}", envPath);
		File envFile = new File(envPath);
		ConfigurationProvider provider = ConfigurationFactory.createConfigurationProvider(envFile);
		Configuration loadedConfiguration = provider.load();
		log.info("reading director.env, Properties count {}", loadedConfiguration.keys().size());
		for (String key : loadedConfiguration.keys()) {
			String value = loadedConfiguration.get(key);
			envMap.put(key, value);
		}
	}

	@Override
	protected void validate() throws Exception {
		if (!(StringUtils.isNotBlank(envMap.get("GLANCE_API_ENDPOINT"))
				&& StringUtils.isNotBlank(envMap.get("GLANCE_KEYSTONE_PUBLIC_ENDPOINT"))
				&& StringUtils.isNotBlank(envMap.get("GLANCE_IMAGE_STORE_USERNAME"))
				&& StringUtils.isNotBlank(envMap.get("GLANCE_IMAGE_STORE_PASSWORD"))
				&& StringUtils.isNotBlank(envMap.get("TENANT_NAME")))) {
			log.info("Some props for glance are not set");
			configuration("Some of the Glance properties are not set. Not importing");
			return;
		}
		glanceUrl = envMap.get("GLANCE_API_ENDPOINT");
		authUrl = envMap.get("GLANCE_KEYSTONE_PUBLIC_ENDPOINT");
		user = envMap.get("GLANCE_IMAGE_STORE_USERNAME");
		password = envMap.get("GLANCE_IMAGE_STORE_PASSWORD");
		tenant = envMap.get("TENANT_NAME");
		log.info("Set api={}, auth={}, user={}, paswd={}, tenant={}", glanceUrl, authUrl, user, password, tenant);
		ImageStoreFilter imageStoreFilter = new ImageStoreFilter();
		imageStoreFilter.setName(defaultStoreName);
		List<ImageStoreTransferObject> imageStores = imageStoresService.getImageStores(imageStoreFilter);
		if (imageStores.size() == 0) {
			validation("Default image store not yet set. Would be creating it now ");
		}
		log.info("End of validate");
	}

	@Override
	protected void execute() throws Exception {
		log.info("Default store {} not present. Creating new", defaultStoreName);
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
				log.info("{} = {}", Constants.GLANCE_API_ENDPOINT, glanceUrl);
				break;
			case Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT:
				log.info("{} = {}", Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT, authUrl);
				detailsTransferObject.setValue(authUrl);
				break;
			case Constants.GLANCE_IMAGE_STORE_USERNAME:
				detailsTransferObject.setValue(user);
				log.info("{} = {}", Constants.GLANCE_IMAGE_STORE_USERNAME, user);
				break;
			case Constants.GLANCE_IMAGE_STORE_PASSWORD:
				detailsTransferObject.setValue(password);
				log.info("{} = {}", Constants.GLANCE_IMAGE_STORE_PASSWORD, password);
				break;
			case Constants.GLANCE_TENANT_NAME:
				detailsTransferObject.setValue(tenant);
				log.info("{} = {}", Constants.GLANCE_TENANT_NAME, tenant);
				break;
			case Constants.GLANCE_VISIBILITY:
				detailsTransferObject.setValue("Public");
				log.info("{} = {}", Constants.GLANCE_VISIBILITY, "Public");
				break;
			}

		}

		imageStoreTransferObject = imageStoresService.createImageStore(imageStoreTransferObject);
		log.info("Successfully created default glance image store {}", imageStoreTransferObject.id);
	}

}
