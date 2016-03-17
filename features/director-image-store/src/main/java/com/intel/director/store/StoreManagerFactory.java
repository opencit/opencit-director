package com.intel.director.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.intel.director.api.ConnectorProperties;
import com.intel.director.api.ImageStoreDetailsTransferObject;
import com.intel.director.api.ImageStoreTransferObject;
import com.intel.director.store.exception.StoreException;
import com.intel.director.store.util.ImageStorePasswordUtil;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class StoreManagerFactory {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(StoreManagerFactory.class);

	/**
	 * Returns null if no store exists for the ID
	 * 
	 * @param storeId
	 * @return
	 * @throws StoreException
	 */
	public static StoreManager getStoreManager(String storeId)
			throws StoreException {
		IPersistService persistService = new DbServiceImpl();
		StoreManager storeManager = null;
		ImageStoreTransferObject imageStoreDTO = null;
		try {
			imageStoreDTO = persistService.fetchImageStorebyId(storeId);
		} catch (DbException e) {
			log.error("No store exists for id {}", storeId);
			persistService = null;
			return null;
		}
		String connectorName = imageStoreDTO.getConnector();
		ConnectorProperties connector = ConnectorProperties
				.getConnectorByName(connectorName);
		if (connector == null) {
			persistService = null;
			return null;
		}

		try {
			storeManager = (StoreManager) Class.forName(connector.getDriver())
					.newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			log.error("Error instatiating connector for {}",
					connector.getDriver());
			persistService = null;

			throw new StoreException("Error instatiating connector for "
					+ connector.getDriver(), e);
		}


		ImageStoreDetailsTransferObject passwordConfiguration = imageStoreDTO.getPasswordConfiguration();
		ImageStorePasswordUtil imageStorePasswordUtil = new ImageStorePasswordUtil(passwordConfiguration.id);
		if(StringUtils.isBlank(passwordConfiguration.getValue())){
			throw new StoreException("No password set for store "+ storeId);
		}
		Collection<ImageStoreDetailsTransferObject> image_store_details = imageStoreDTO
				.getImage_store_details();
		Map<String, String> map = new HashMap<>();

		for (ImageStoreDetailsTransferObject imageStoreDetailsTransferObject : image_store_details) {
			map.put(imageStoreDetailsTransferObject.getKey(),
					imageStoreDetailsTransferObject.getValue());
			if (imageStoreDetailsTransferObject.equals(passwordConfiguration)) {
				log.info("Encrypted password is : {}",
						imageStoreDetailsTransferObject.getValue());
				map.put(imageStoreDetailsTransferObject.getKey(),
						imageStorePasswordUtil
								.decryptPasswordForImageStore(imageStoreDetailsTransferObject
										.getValue()));
				log.info("Decrypted password is : {}",
						map.get(imageStoreDetailsTransferObject.getKey()));
			}
		}

		storeManager.build(map);
		persistService = null;
		return storeManager;
	}

}
