package com.intel.director.glance.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.common.Constants;
import com.intel.director.images.GlanceImageStoreManager;
import com.intel.director.store.exception.StoreException;
import com.intel.mtwilson.director.db.exception.DbException;

public class GlanceTest {

	
	@Test
	public void glanceTest() throws DbException {
		  	String uuid=null;
	    	uuid = (new UUID()).toString();
			GlanceImageStoreManager imageStoreManager = new GlanceImageStoreManager();
			Map<String,String> configuration= new HashMap<String,String>();
			configuration.put(Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT, "http://10.35.35.239:5000");
		 	configuration.put(Constants.GLANCE_API_ENDPOINT, "http://10.35.35.239:9292");
	        configuration.put(Constants.GLANCE_IMAGE_STORE_USERNAME,"admin");
	        configuration.put(Constants.GLANCE_IMAGE_STORE_PASSWORD,"intelmh");
	        configuration.put(Constants.GLANCE_TENANT_NAME,"admin");
	        configuration.put(Constants.GLANCE_ID,uuid);
	        configuration.put(com.intel.director.constants.Constants.NAME, "test_upload");
	        configuration.put(com.intel.director.constants.Constants.DISK_FORMAT, "qcow2");
	        configuration.put(com.intel.director.constants.Constants.CONTAINER_FORMAT,"bare");
	        configuration.put(com.intel.director.constants.Constants.IS_PUBLIC, "true");
	        try {
				imageStoreManager.build(configuration);
			} catch (StoreException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
	        Map<String,Object> mp= new HashMap<String,Object>();
	        mp.put(com.intel.director.common.Constants.UPLOAD_TO_IMAGE_STORE_FILE, "C:/Temp/sss.txt");
	        imageStoreManager.addCustomProperties(mp);
	      
	        try {
				imageStoreManager.upload();
			} catch (StoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
		System.out.println("########################## UPDATING METADATA ################");
		
		try {
			GlanceImageStoreManager imageStoreManagerUpdate = new GlanceImageStoreManager();
		/*	Map<String,String> configurationUpdate= new HashMap<String,String>();
			configurationUpdate.put(Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT, "http://10.35.35.239:5000");
		 	configurationUpdate.put(Constants.GLANCE_API_ENDPOINT, "http://10.35.35.239:9292");
	        configurationUpdate.put(Constants.GLANCE_IMAGE_STORE_USERNAME,"admin");
	        configurationUpdate.put(Constants.GLANCE_IMAGE_STORE_PASSWORD,"intelmh");
	        configurationUpdate.put(Constants.GLANCE_TENANT_NAME,"admin");
	        configurationUpdate.put(Constants.GLANCE_ID,(new UUID()).toString());*/
		
			Map<String,String> configuration1= new HashMap<String,String>();
			configuration1.put(Constants.GLANCE_KEYSTONE_PUBLIC_ENDPOINT, "http://10.35.35.239:5000");
		 	configuration1.put(Constants.GLANCE_API_ENDPOINT, "http://10.35.35.239:9292");
	        configuration1.put(Constants.GLANCE_IMAGE_STORE_USERNAME,"admin");
	        configuration1.put(Constants.GLANCE_IMAGE_STORE_PASSWORD,"intelmh");
	        configuration1.put(Constants.GLANCE_TENANT_NAME,"admin");
	        configuration1.put("mtwilson_trustpolicy_location", "1245");
	        configuration1.put(com.intel.director.constants.Constants.GLANCE_ID, uuid);
		
			System.out.println("############################REPLACE IN UPDATE#########################::"+configuration1);
			imageStoreManagerUpdate.build(configuration1);
			/// configuration.put(com.intel.director.constants.Constants.MTWILSON_TRUST_POLICY_LOCATION, "1234");
			 imageStoreManagerUpdate.update();
			 System.out.println("########## FETCH DETAILS FINALLY############");
			 System.out.println("Fetchdetails ::"+imageStoreManagerUpdate.fetchDetails());
		} catch (StoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		
	//	imageStoreManager.uploadImageMetadata();
	}
	
	
}
