package com.intel.mtwilson.director.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageStoreSettings;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.User;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageInfoFields;
import com.intel.director.api.ui.ImageInfoFilter;
import com.intel.director.api.ui.ImageInfoOrderBy;
import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadFilter;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.SearchImageByPolicyCriteria;
import com.intel.director.api.ui.SearchImageByUploadCriteria;
import com.intel.director.api.ui.TrustPolicyDraftFields;
import com.intel.director.api.ui.TrustPolicyDraftFilter;
import com.intel.director.api.ui.TrustPolicyDraftOrderBy;
import com.intel.director.api.ui.TrustPolicyFields;
import com.intel.director.api.ui.TrustPolicyFilter;
import com.intel.director.api.ui.TrustPolicyOrderBy;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.dbservice.DbServiceImpl;
import com.intel.mtwilson.director.dbservice.IPersistService;

public class DbServiceTest {

	IPersistService dBServiceImpl = new DbServiceImpl();
	List<ImageAttributes> imgAttributesList ;
	List<ImageAttributes> persistedImgAttributesList ;
	User myUser,myUser2,myUser3;
	Date currentDate,threeDaysBackDate,oneDaysBackDate,sevenDaysBackDate;
	String createdUserId,createdUserId2,createdUserId3;
	
	 @Before
	    public void setup() throws Exception {
		 removeImageupload();
		 removeAllImagesEntries();
		 removeAllImageStoreSettingsEntries();
		
		 imgAttributesList=new ArrayList<ImageAttributes>();
		 persistedImgAttributesList=new ArrayList<ImageAttributes>();
		User user = new User();
		user.setDisplayname("Aakash");
		user.setUsername("admin");
		user.setEmail("aa@gmail.com");
		myUser = dBServiceImpl.saveUser(user);
		createdUserId = myUser.getId();
	
		User user2 = new User();
		user2.setDisplayname("Anish");
		user2.setUsername("admin");
		user2.setEmail("aa@gmail.com");
		myUser2 = dBServiceImpl.saveUser(user2);
		createdUserId2 = myUser2.getId();
		User user3 = new User();
		user3.setDisplayname("Alok");
		user3.setUsername("admin");
		user3.setEmail("aa@gmail.com");
		myUser3 = dBServiceImpl.saveUser(user3);
		createdUserId3 = myUser3.getId();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -3);
		 currentDate = new Date();
		 threeDaysBackDate = c.getTime();

		c.add(Calendar.DATE, 2);
		 oneDaysBackDate = c.getTime();

		c.add(Calendar.DATE, -5);
		 sevenDaysBackDate = c.getTime();
	       
	    }
	
	 @Test
	 public void integrationTest() throws DbException{
		
		 testImageDao();
		 testPolicyDao();
		 testPolicyDraftDao();
		 testImageUploadDao();
		 testMiscellaneous();
		 testImageStoreSettings();
	/*	 removeImageupload();
		 removeAllImagesEntries();
		 removeAllImageStoreSettingsEntries();*/
	 }
	 
	 
	 public void testMiscellaneous() throws DbException{
		 String imageId=persistedImgAttributesList.get(0).getId();
		 TrustPolicy trustPolicy=dBServiceImpl.fetchPolicyForImage(imageId);
		 TrustPolicyDraft trustPolicyDraft=dBServiceImpl.fetchPolicyDraftForImage(imageId);
		 List<ImageStoreUploadTransferObject> imageUploadList=dBServiceImpl.fetchPolicyUploadsForImage(imageId);
		System.out.println("trustPolicy::"+trustPolicy);
		Assert.assertTrue("fetchPolicyForImage fail",
				trustPolicy != null);
		System.out.println("trustPolicyDraft::"+trustPolicyDraft);
		Assert.assertTrue("fetchPolicyDraftForImage  fail",
				trustPolicyDraft != null);
		System.out.println("imageUploadList::"+imageUploadList.get(0));
		Assert.assertTrue("fetchPolicyUploadsForImage fail",
				imageUploadList != null);
	 }
	
	 
	 public void testImageStoreSettings() throws DbException{
		 ImageStoreSettings imageStoreSettings=new ImageStoreSettings("Glance","GlanceProviderClass");
		 ImageStoreSettings imageStoreSettingsCreated=dBServiceImpl.saveImageStoreSettings(imageStoreSettings);
		 Assert.assertTrue("ImageStoreSettings create fail",
				 imageStoreSettingsCreated.getId() != null);
		 ImageStoreSettings  imageStoreSettingsfetched= dBServiceImpl.fetchImageStoreSettingsByName("Glance");
		 Assert.assertTrue("fetchImageStoreSettings name",
				 imageStoreSettingsCreated.getId() == imageStoreSettingsfetched.getId());
		 
	 }
	 
	public void testImageDao() throws DbException {
		
	
	
		ImageAttributes imgAttrs = new ImageAttributes(createdUserId,
				new Date(), createdUserId, new Date(), "img1", "qcow", "VM",
				"ACTIVE", 512L, 24L, null, false, "C://temp");

	

		ImageAttributes img = dBServiceImpl.saveImageMetadata(imgAttrs);

		Assert.assertTrue("Create operation fail", img.getId() != null);

		String imageId = img.getId();

		img.setMounted_by_user_id((new UUID()).toString());
		dBServiceImpl.updateImage(img);
		ImageAttributes retrieveImage = dBServiceImpl.fetchImageById(imageId);
		Assert.assertTrue("Update operation fail",
				retrieveImage.getMounted_by_user_id() != null);

	

	
		imgAttributesList
				.add(new ImageAttributes(createdUserId, threeDaysBackDate,
						createdUserId, threeDaysBackDate, "img2", "qcow",
						"BareMetal", "ACTIVE", 512L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId, currentDate,
				createdUserId, currentDate, "img3", "qcow", "VM", "ACTIVE",
				512L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				oneDaysBackDate, createdUserId, oneDaysBackDate, "img4",
				"qcow", "VM", "ACTIVE", 512L, 24L, null, false, "C://temp"));

		imgAttributesList
				.add(new ImageAttributes(createdUserId, threeDaysBackDate,
						createdUserId, threeDaysBackDate, "img5", "qcow",
						"BareMetal", "ACTIVE", 512L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				threeDaysBackDate, createdUserId, threeDaysBackDate, "ubuntu1",
				"qcow", "VM", "ACTIVE", 512L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				threeDaysBackDate, createdUserId, threeDaysBackDate, "ubuntu2",
				"qcow", "VM", "ACTIVE", 512L, 24L, null, false, "C://temp"));

		imgAttributesList
				.add(new ImageAttributes(createdUserId, threeDaysBackDate,
						createdUserId, threeDaysBackDate, "ubuntu3", "qcow",
						"BareMetal", "ACTIVE", 512L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				threeDaysBackDate, createdUserId, threeDaysBackDate, "ubuntu4",
				"qcow", "VM", "ACTIVE", 512L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId, currentDate,
				createdUserId, currentDate, "ubuntu5", "qcow", "VM",
				"UPLOADED", 512L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId, currentDate,
				createdUserId, currentDate, "ubuntu6", "vhd", "VM", "UPLOADED",
				512L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				sevenDaysBackDate, createdUserId, sevenDaysBackDate, "ubuntu7",
				"vhd", "BareMetal", "UPLOADED", 512L, 24L, null, false,
				"C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				threeDaysBackDate, createdUserId, threeDaysBackDate, "ubuntu8",
				"qcow", "VM", "ACTIVE", 1024L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				sevenDaysBackDate, createdUserId, sevenDaysBackDate, "ubuntu9",
				"qcow", "VM", "ACTIVE", 512L, 24L, null, false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				threeDaysBackDate, createdUserId, threeDaysBackDate,
				"ubuntu10", "test", "BareMetal", "ACTIVE", 512L, 24L, null,
				false, "C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				threeDaysBackDate, createdUserId, threeDaysBackDate,
				"ubuntu11", "test", "VM", "ACTIVE", 512L, 24L, null, false,
				"C://temp"));

		imgAttributesList.add(new ImageAttributes(createdUserId,
				sevenDaysBackDate, createdUserId, sevenDaysBackDate,
				"ubuntu12", "qcow", "VM", "ACTIVE", 512L, 24L, null, false,
				"C://temp"));

		imgAttributesList
				.add(new ImageAttributes(createdUserId, threeDaysBackDate,
						createdUserId, threeDaysBackDate, "fedora1", "qcow",
						"BareMetal", "ACTIVE", 512L, 24L, null, false, "C://temp"));
		imgAttributesList.add(new ImageAttributes(createdUserId,
				threeDaysBackDate, createdUserId, threeDaysBackDate, "fedora2",
				"test", "VM", "UPLOADED", 512L, 24L, null, false, "C://temp"));
		imgAttributesList
				.add(new ImageAttributes(createdUserId, sevenDaysBackDate,
						createdUserId, sevenDaysBackDate, "fedora3", "qcow",
						"BareMetal", "ACTIVE", 512L, 24L, null, false, "C://temp"));
		imgAttributesList.add(new ImageAttributes(createdUserId, currentDate,
				createdUserId, currentDate, "fedora4", "qcow", "VM",
				"UPLOADED", 512L, 24L, null, false, "C://temp"));

	
		for(int i=0;i<imgAttributesList.size();i++){
			persistedImgAttributesList.add(dBServiceImpl.saveImageMetadata(imgAttributesList.get(i)));
		}

		ImageInfoFilter searchFilter = new ImageInfoFilter();
		searchFilter.setImage_name("ubun");

		searchFilter.setFrom_created_date(sevenDaysBackDate);
		searchFilter.setTo_created_date(oneDaysBackDate);
		searchFilter.setPolicyCriteria(SearchImageByPolicyCriteria.WITHOUT);
		searchFilter.setUploadCriteria(SearchImageByUploadCriteria.NOT_UPLOADED);
		searchFilter.setImage_deployments("VM");
		ImageInfoOrderBy imgOrderBy = new ImageInfoOrderBy();
		imgOrderBy.setImgFields(ImageInfoFields.CREATED_DATE);
		imgOrderBy.setOrderBy(OrderByEnum.DESC);
		List<ImageInfo> searchImageList = dBServiceImpl.fetchImages(
				searchFilter, imgOrderBy);

		System.out.println("#######################Search############"
				+ searchImageList.size());

		for (ImageInfo imginfo : searchImageList) {
			System.out.println("### imginfo::" + imginfo);
		}
		Assert.assertTrue(
				"fetchImages(searchFilter, imgOrderBy) fail",
				(searchImageList.size() == 7)
						&& (searchImageList.get(0).getImage_name()
								.equalsIgnoreCase("ubuntu8")));
		ImageInfoFilter searchFilterPolicyCriteria = new ImageInfoFilter();
		searchFilterPolicyCriteria.setPolicyCriteria(SearchImageByPolicyCriteria.WITH);
		List<ImageInfo> searchImageListPolicyCriteria = dBServiceImpl.fetchImages(
				searchFilterPolicyCriteria, imgOrderBy);
		Assert.assertTrue(
				"fetchImages(searchFilter, imgOrderBy) SearchImageByPolicyCriteria.WITHOUT fail",
				(searchImageListPolicyCriteria.size() == 0));

		int totalCount = dBServiceImpl.getTotalImagesCount();

		int serachTotalCount = dBServiceImpl.getTotalImagesCount(searchFilter);

		Assert.assertTrue("getTotalCount() method fail", totalCount == 21);
		Assert.assertTrue("getTotalCount(searchFilter) method fail",
				serachTotalCount == 7);

		List<ImageInfo> searchImagePaginatedList = dBServiceImpl
				.fetchImages(searchFilter, imgOrderBy, 2, 2);
		for (ImageAttributes imginfo : searchImagePaginatedList) {
			System.out.println("### imginfo::" + imginfo);
		}
		Assert.assertTrue(
				"fetchImages(searchFilter, imgOrderBy,firstElement,maxelements method fail",
				searchImagePaginatedList.size() == 2
						&& searchImagePaginatedList.get(0).getImage_name()
								.equalsIgnoreCase("ubuntu4"));

		

		dBServiceImpl.destroyImage(img);
		

	}
	
	
	
	
	
	public void removeAllImagesEntries() throws DbException {
		
		List<ImageInfo> imgInfoList = dBServiceImpl
				.fetchImages(null);
		for (ImageAttributes imgAttr : imgInfoList) {
			dBServiceImpl.destroyImage(imgAttr);
		}

	}
	
	
public void removeAllImageStoreSettingsEntries() throws DbException {
		
		List<ImageStoreSettings> imgStoreSettingsList = dBServiceImpl
				.fetchImageStoreSettings();
		for (ImageStoreSettings imgStoreSettings : imgStoreSettingsList) {
			dBServiceImpl.destroyImageStoreSettings(imgStoreSettings);
		}

	}
	
	 
	/*public void removeAllPoliciesEntries() throws DbException {
		
		List<TrustPolicy> policiesList = dBServiceImpl
				.fetchPolicies(null);
		for (TrustPolicy policy : policiesList) {
			dBServiceImpl.destroyPolicy(policy);
		}

	}*/
	
	
	public void removeImageupload() throws DbException {
		
		List<ImageStoreUploadTransferObject> imgUploadList = dBServiceImpl
				.fetchImageUploads(null);
		for (ImageStoreUploadTransferObject imgUpload : imgUploadList) {
			dBServiceImpl.destroyImageUpload(imgUpload);
		}

	}
	
	public void clearImageEntries() throws DbException {
		
		for (ImageAttributes imgAttr : imgAttributesList) {
			dBServiceImpl.destroyImage(imgAttr);
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	public void testPolicyDao() throws DbException {
		try{
		
			
		List<TrustPolicy> deleteTrustPoliciesList= new ArrayList<TrustPolicy>();
		TrustPolicy trustPolicy1= new TrustPolicy(createdUserId, sevenDaysBackDate, createdUserId, sevenDaysBackDate, "trustPolicy1desc","<abc>TrustPolicy xml1 field</abc>","TrustPolicy1_"+sevenDaysBackDate, persistedImgAttributesList.get(0));
		deleteTrustPoliciesList.add(trustPolicy1);
		TrustPolicy trustPolicyCreated=dBServiceImpl.savePolicy(trustPolicy1);
		Assert.assertTrue("Create operation fail", trustPolicyCreated.getId() != null);
		String trustPolicyId = trustPolicyCreated.getId();
		trustPolicyCreated.setDescription("Updated Description");
		dBServiceImpl.updatePolicy(trustPolicyCreated);
		TrustPolicy trustPolicyRetrieved = dBServiceImpl.fetchPolicyById(trustPolicyId);
		System.out.println("Image Name::"+trustPolicyRetrieved.getImgAttributes().getImage_name());
		System.out.println("trustPolicyRetrieved.getDescription()::"+trustPolicyRetrieved.getDescription());
		Assert.assertTrue("Update operation fail",
				(trustPolicyRetrieved.getDescription().equals("Updated Description")));
		List<TrustPolicy> trustPoliciesList= new ArrayList<TrustPolicy>();
		trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy2desc","<abc>TrustPolicy xml2 field</abc>","TrustPolicy2_"+sevenDaysBackDate, persistedImgAttributesList.get(1)));
		trustPoliciesList.add(new TrustPolicy(createdUserId, threeDaysBackDate, createdUserId, threeDaysBackDate, "trustPolicy3desc","<abc>TrustPolicy xml3 field</abc>","TrustPolicy3_"+sevenDaysBackDate, persistedImgAttributesList.get(2)));
		trustPoliciesList.add(new TrustPolicy(createdUserId2, oneDaysBackDate, createdUserId2, oneDaysBackDate, "trustPolicy4desc","<abc>TrustPolicy xml4 field</abc>","TrustPolicy4_"+sevenDaysBackDate, persistedImgAttributesList.get(3)));
		trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy5desc","<abc>TrustPolicy xml5 field</abc>","TrustPolicy5_"+sevenDaysBackDate, persistedImgAttributesList.get(4)));
		trustPoliciesList.add(new TrustPolicy(createdUserId, threeDaysBackDate, createdUserId, threeDaysBackDate, "trustPolicy6desc","<abc>TrustPolicy xml6 field</abc>","TrustPolicy6_"+sevenDaysBackDate, persistedImgAttributesList.get(5)));
		trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy7desc","<abc>TrustPolicy xml7 field</abc>","TrustPolicy7_"+sevenDaysBackDate, persistedImgAttributesList.get(6)));
		trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy8desc","<abc>TrustPolicy xml8 field</abc>","TrustPolicy8_"+sevenDaysBackDate, persistedImgAttributesList.get(7)));
		trustPoliciesList.add(new TrustPolicy(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate, "trustPolicy9desc","<abc>TrustPolicy xml9 field</abc>","TrustPolicy9_"+sevenDaysBackDate, persistedImgAttributesList.get(8)));
		
		for (TrustPolicy trustPolicy : trustPoliciesList) {
			
			dBServiceImpl.savePolicy(trustPolicy);
		}

		TrustPolicyFilter trustPolicyFilter = new TrustPolicyFilter();
		trustPolicyFilter.setCreated_by_user_id(createdUserId2);
	
		TrustPolicyOrderBy tpOrderBy = new TrustPolicyOrderBy();
		tpOrderBy.setTrustPolicyFields(TrustPolicyFields.CREATED_DATE);
	
		tpOrderBy.setOrderBy(OrderByEnum.DESC);
		List<TrustPolicy> searchPolicyList = dBServiceImpl.fetchPolicies(
				trustPolicyFilter, tpOrderBy);

		System.out.println("#######################Search Trust policies############"
				+ searchPolicyList.size());

		for (TrustPolicy tp : searchPolicyList) {
			System.out.println("### trust policy::" + tp);
			System.out.println("### searchPolicyList imageName::" + tp.getImgAttributes().getImage_name());
		}
		Assert.assertTrue(
				"fetchPolicies(searchFilter, OrderBy) fail",
				(searchPolicyList.size() == 6)
						&& (searchPolicyList.get(0).getDescription()
								.equalsIgnoreCase("trustPolicy4desc")));

		int totalCount = dBServiceImpl.getTotalPoliciesCount();

		int serachTotalCount = dBServiceImpl.getTotalPoliciesCount(trustPolicyFilter);
		System.out.println("#######################totalCount_ trust policy######::"+totalCount);
		Assert.assertTrue("getTotalPoliesCount() method fail", totalCount == 9);
		Assert.assertTrue("getTotalPoliesCount(searchFilter) method fail",
				serachTotalCount == 6);

		List<TrustPolicy> searchPoliciesPaginatedList = dBServiceImpl
				.fetchPolicies(trustPolicyFilter, tpOrderBy, 2, 2);
		for (TrustPolicy tp : searchPoliciesPaginatedList) {
			System.out.println("### trustPolicy after paging::" + tp);
		}
		Assert.assertTrue(
				"fetchPolicies(searchFilter, imgOrderBy,firstElement,maxelements method fail",
				searchPoliciesPaginatedList.size() == 2
						);
		TrustPolicyFilter trustPolicyFilterByImage=new TrustPolicyFilter();
		trustPolicyFilterByImage.setImage_format("qcow");
		TrustPolicyOrderBy tpOrderByImage = new TrustPolicyOrderBy();
		tpOrderByImage.setTrustPolicyFields(TrustPolicyFields.IMAGE_NAME);
	
		tpOrderByImage.setOrderBy(OrderByEnum.DESC);
		
		List<TrustPolicy> searchPolicyFilterByImageList = dBServiceImpl.fetchPolicies(
				trustPolicyFilterByImage, tpOrderByImage);
		for (TrustPolicy tp : searchPolicyFilterByImageList) {
			System.out.println("### trustPolicy after searchPolicyFilter orderByImageList desc::" + tp);
		}

		
		}catch(DbException e){
			e.printStackTrace();
		}

	}
	
	
	public void testPolicyDraftDao() throws DbException {
		try{
		
	
		List<TrustPolicyDraft> deleteTrustPoliciesList= new ArrayList<TrustPolicyDraft>();
		TrustPolicyDraft trustPolicyDraft1= new TrustPolicyDraft(createdUserId, sevenDaysBackDate, createdUserId, sevenDaysBackDate,"<abc>TrustPolicyDraft xml1 field</abc>","TrustPolicyDraft1_"+sevenDaysBackDate, persistedImgAttributesList.get(0));
		deleteTrustPoliciesList.add(trustPolicyDraft1);
		TrustPolicyDraft trustPolicyDraftCreated=dBServiceImpl.savePolicyDraft(trustPolicyDraft1);
		Assert.assertTrue("Create operation fail", trustPolicyDraftCreated.getId() != null);
		
		List<TrustPolicyDraft> trustPoliciesDraftList= new ArrayList<TrustPolicyDraft>();
		trustPoliciesDraftList.add(new TrustPolicyDraft(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate,"<abc>TrustPolicyDraft xml2 field</abc>","TrustPolicyDraft2_"+sevenDaysBackDate, persistedImgAttributesList.get(1)));
		trustPoliciesDraftList.add(new TrustPolicyDraft(createdUserId, threeDaysBackDate, createdUserId, threeDaysBackDate,"<abc>TrustPolicyDraft xml3 field</abc>","TrustPolicyDraft3_"+sevenDaysBackDate, persistedImgAttributesList.get(2)));
		trustPoliciesDraftList.add(new TrustPolicyDraft(createdUserId2, oneDaysBackDate, createdUserId2, oneDaysBackDate,"<abc>TrustPolicyDraft xml4 field</abc>","TrustPolicyDraft4_"+sevenDaysBackDate, persistedImgAttributesList.get(3)));
		trustPoliciesDraftList.add(new TrustPolicyDraft(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate,"<abc>TrustPolicyDraft xml5 field</abc>","TrustPolicyDraft5_"+sevenDaysBackDate, persistedImgAttributesList.get(4)));
		trustPoliciesDraftList.add(new TrustPolicyDraft(createdUserId, threeDaysBackDate, createdUserId, threeDaysBackDate,"<abc>TrustPolicyDraft xml6 field</abc>","TrustPolicyDraft6_"+sevenDaysBackDate, persistedImgAttributesList.get(5)));
		trustPoliciesDraftList.add(new TrustPolicyDraft(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate,"<abc>TrustPolicyDraft xml7 field</abc>","TrustPolicyDraft7_"+sevenDaysBackDate, persistedImgAttributesList.get(6)));
		trustPoliciesDraftList.add(new TrustPolicyDraft(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate,"<abc>TrustPolicyDraft xml8 field</abc>","TrustPolicyDraft8_"+sevenDaysBackDate, persistedImgAttributesList.get(7)));
		trustPoliciesDraftList.add(new TrustPolicyDraft(createdUserId2, sevenDaysBackDate, createdUserId2, sevenDaysBackDate,"<abc>TrustPolicyDraft xml9 field</abc>","TrustPolicyDraft9_"+sevenDaysBackDate, persistedImgAttributesList.get(8)));
		
		for (TrustPolicyDraft trustPolicyDraft : trustPoliciesDraftList) {
			
			dBServiceImpl.savePolicyDraft(trustPolicyDraft);
		}

		TrustPolicyDraftFilter trustPolicyDraftFilter = new TrustPolicyDraftFilter();
		trustPolicyDraftFilter.setCreated_by_user_id(createdUserId2);
	
		TrustPolicyDraftOrderBy tpOrderBy = new TrustPolicyDraftOrderBy();
		tpOrderBy.setTrustPolicyDraftFields(TrustPolicyDraftFields.CREATED_DATE);
	
		tpOrderBy.setOrderBy(OrderByEnum.DESC);
		List<TrustPolicyDraft> searchPolicyDraftList = dBServiceImpl.fetchPolicyDrafts(
				trustPolicyDraftFilter, tpOrderBy);

		System.out.println("#######################Search Trust policies############"
				+ searchPolicyDraftList.size());

		for (TrustPolicyDraft tpd : searchPolicyDraftList) {
			System.out.println("### trust policy draft::" + tpd);
			System.out.println("### searchPolicyDraftList imageName::" + tpd.getImgAttributes().getImage_name());
		}
		Assert.assertTrue(
				"fetchPolicyDrafts(searchFilter, OrderBy) fail",
				(searchPolicyDraftList.size() == 6)
						);

		int totalCount = dBServiceImpl.getTotalPoliciesCount();

		int serachTotalCount = dBServiceImpl.getTotalPolicyDraftsCount(trustPolicyDraftFilter);
		System.out.println("#######################totalCount_ trust policy######::"+totalCount);
		Assert.assertTrue("getTotalPolicyDraftsCount() method fail", totalCount == 9);
		Assert.assertTrue("getTotalPolicyDraftsCount(searchFilter) method fail",
				serachTotalCount == 6);

		List<TrustPolicyDraft> searchPoliciesPaginatedList = dBServiceImpl
				.fetchPolicyDrafts(trustPolicyDraftFilter, tpOrderBy, 2, 2);
		for (TrustPolicyDraft tp : searchPoliciesPaginatedList) {
			System.out.println("### trustPolicyDraft after paging::" + tp);
		}
		Assert.assertTrue(
				"fetchPolicydrafts(searchFilter, imgOrderBy,firstElement,maxelements method fail",
				searchPoliciesPaginatedList.size() == 2
						);
		TrustPolicyDraftFilter trustPolicyDraftFilterByImage=new TrustPolicyDraftFilter();
		trustPolicyDraftFilterByImage.setImage_format("qcow");
		TrustPolicyDraftOrderBy tpOrderByImage = new TrustPolicyDraftOrderBy();
		tpOrderByImage.setTrustPolicyDraftFields(TrustPolicyDraftFields.IMAGE_NAME);
	
		tpOrderByImage.setOrderBy(OrderByEnum.DESC);
		
		List<TrustPolicyDraft> searchPolicyDraftFilterByImageList = dBServiceImpl.fetchPolicyDrafts(
				trustPolicyDraftFilterByImage, tpOrderByImage);
		for (TrustPolicyDraft tpd : searchPolicyDraftFilterByImageList) {
			System.out.println("### trustPolicyDraft after searchPolicyDraftFilter orderByImageList desc::" + tpd);
		}

		
		}catch(DbException e){
			e.printStackTrace();
		}

	}
	
	
	public void testImageUploadDao() throws DbException {
		try{
		
	
		ImageStoreUploadTransferObject imgUpload1= new ImageStoreUploadTransferObject("http://imageuri1", sevenDaysBackDate, "C:/temp", "edfd", "ACTIVE", 2048, 1024, createdUserId, persistedImgAttributesList.get(0));
		
		
		ImageStoreUploadTransferObject imageStoreUploadCreated=dBServiceImpl.saveImageUpload(imgUpload1);
		Assert.assertTrue("Create image upload operation fail", imageStoreUploadCreated.getId() != null);
		
		List<ImageStoreUploadTransferObject> imageUploadList= new ArrayList<ImageStoreUploadTransferObject>();
		imageUploadList.add( new ImageStoreUploadTransferObject("http://imageuri2", sevenDaysBackDate, "C:/temp", "edfd", "ACTIVE", 2048, 1024, createdUserId2, persistedImgAttributesList.get(1)));
		imageUploadList.add( new ImageStoreUploadTransferObject("http://imageuri3", threeDaysBackDate, "C:/temp", "edfd", "ACTIVE", 2048, 1024, createdUserId2, persistedImgAttributesList.get(2)));
		imageUploadList.add( new ImageStoreUploadTransferObject("http://imageuri4", sevenDaysBackDate, "C:/temp", "edfd", "ACTIVE", 2048, 1024, createdUserId2, persistedImgAttributesList.get(3)));
		imageUploadList.add( new ImageStoreUploadTransferObject("http://imageuri5", oneDaysBackDate, "C:/temp", "edfd", "ACTIVE", 2048, 1024, createdUserId2, persistedImgAttributesList.get(4)));
		imageUploadList.add( new ImageStoreUploadTransferObject("http://imageuri6", sevenDaysBackDate, "C:/temp", "edfd", "ACTIVE", 2048, 1024, createdUserId2, persistedImgAttributesList.get(5)));
		
		for(ImageStoreUploadTransferObject imgU: imageUploadList){
			dBServiceImpl.saveImageUpload(imgU);
		}

		ImageStoreUploadFilter imgUpFilter= new ImageStoreUploadFilter();
	
		
		imgUpFilter.setImage_name("img");
		ImageStoreUploadOrderBy  imgOrder= new ImageStoreUploadOrderBy();
		imgOrder.setImgStoreUploadFields(ImageStoreUploadFields.DATE);

	
		imgOrder.setOrderBy(OrderByEnum.DESC);
		List<ImageStoreUploadTransferObject> searcImgList = dBServiceImpl.fetchImageUploads(imgUpFilter, imgOrder);

		System.out.println("#######################Search Trust policies############"
				+ searcImgList.size());

		for (ImageStoreUploadTransferObject tpd : searcImgList) {
			System.out.println("### ImageStoreUploadOrderBy::" + tpd);
			System.out.println("### ImageStoreUploadOrderBy imageName::" + tpd.getImg().getImage_name());
		}
		Assert.assertTrue(
				"fetchImageUploads(searchFilter, OrderBy) fail",
				(searcImgList.size()!=0)
						);

		int totalCount = dBServiceImpl.getTotalImageUploadsCount();

		int serachTotalCount = dBServiceImpl.getTotalImageUploadsCount(imgUpFilter);
		System.out.println("#######################totalCount_ trust policy######::"+totalCount);
		System.out.println("#######################serachTotalCount######::"+serachTotalCount);
		Assert.assertTrue("getTotalImageUploadsCount() method fail", totalCount == 6);
		Assert.assertTrue("getTotalImageUploadsCount(searchFilter) method fail",
				serachTotalCount == 4);

		
		
		}catch(DbException e){
			e.printStackTrace();
		}

	}
	
	
	
	


}
