package com.intel.mtwilson.director.dbservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang.StringUtils;

import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageStoreSettings;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.PolicyTemplateInfo;
import com.intel.director.api.SshSettingInfo;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.User;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageInfoFilter;
import com.intel.director.api.ui.ImageInfoOrderBy;
import com.intel.director.api.ui.ImageStoreUploadFilter;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.TrustPolicyDraftFilter;
import com.intel.director.api.ui.TrustPolicyDraftOrderBy;
import com.intel.director.api.ui.TrustPolicyFilter;
import com.intel.director.api.ui.TrustPolicyOrderBy;
import com.intel.director.api.ui.UserFilter;
import com.intel.director.api.ui.UserOrderBy;
import com.intel.director.common.Constants;
import com.intel.director.common.SettingFileProperties;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.director.dao.ImageActionDao;
import com.intel.mtwilson.director.dao.ImageDao;
import com.intel.mtwilson.director.dao.ImageStoreSettingsDao;
import com.intel.mtwilson.director.dao.ImageStoreUploadDao;
import com.intel.mtwilson.director.dao.PolicyTemplateDao;
import com.intel.mtwilson.director.dao.SshSettingDao;
import com.intel.mtwilson.director.dao.TrustPolicyDao;
import com.intel.mtwilson.director.dao.TrustPolicyDraftDao;
import com.intel.mtwilson.director.dao.UserDao;
import com.intel.mtwilson.director.data.MwHost;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.data.MwImageAction;
import com.intel.mtwilson.director.data.MwImageStoreSettings;
import com.intel.mtwilson.director.data.MwImageUpload;
import com.intel.mtwilson.director.data.MwPolicyTemplate;
import com.intel.mtwilson.director.data.MwTrustPolicy;
import com.intel.mtwilson.director.data.MwTrustPolicyDraft;
import com.intel.mtwilson.director.data.MwUser;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class DbServiceImpl implements IPersistService {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DbServiceImpl.class);

	ImageDao imgDao;
	UserDao userDao;
	TrustPolicyDao policyDao;
	TrustPolicyDraftDao policyDraftDao;
	ImageStoreUploadDao imgUploadDao;
	ImageStoreSettingsDao imgStoreSettingsDao;
	Mapper mapper = new Mapper();
	ImageActionDao imageActionDao;
	SshSettingDao sshDao;
	PolicyTemplateDao policyTemplateDao;
	SettingFileProperties settingFileProperties;

	public DbServiceImpl() {
		String filePath=Folders.configuration() + File.separator + "director.properties";
		log.info("Inside DbServiceImpl filePath::"+filePath);
		File configfile = new File(filePath);

		FileReader reader=null;
		try {
			reader = new FileReader(configfile);
		} catch (FileNotFoundException e) {
			// TODO Handle Error
			log.error("Unable to find director properties",e);
		}

		Properties prop = new Properties();

		try {
			prop.load(reader);
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Unable to read director properties",e);
		}
		
		Properties jpaProperties=new Properties();
		jpaProperties.put("javax.persistence.jdbc.driver", prop.get(Constants.DIRECTOR_DB_DRIVER));
		jpaProperties.put("javax.persistence.jdbc.url",prop.get(Constants.DIRECTOR_DB_URL) );
		jpaProperties.put("javax.persistence.jdbc.user" ,prop.get(Constants.DIRECTOR_DB_USERNAME));
		jpaProperties.put("javax.persistence.jdbc.password", prop.get(Constants.DIRECTOR_DB_PASSWORD));
		
		
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("director_data_pu",jpaProperties);
		
		imgDao = new ImageDao(emf);
		userDao = new UserDao(emf);
		policyDao = new TrustPolicyDao(emf);
		policyDraftDao = new TrustPolicyDraftDao(emf);
		imgUploadDao = new ImageStoreUploadDao(emf);
		imgStoreSettingsDao = new ImageStoreSettingsDao(emf);
		imageActionDao = new ImageActionDao(emf);
		sshDao = new SshSettingDao(emf);
		settingFileProperties = new SettingFileProperties();
		policyTemplateDao = new PolicyTemplateDao(emf);
		try {
			if(reader != null)
			{
				reader.close();
			}			
		} catch (IOException e) {
			// TODO Handle Error
			log.error("Unable to close reader",e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#saveImageMetadata
	 * (com.intel.director.api.ImageAttributes)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#saveImageMetadata(com
	 * .intel.director.api.ImageAttributes)
	 */
	public ImageAttributes saveImageMetadata(ImageAttributes img)
			throws DbException {
		MwImage mwImage = mapper.toData(img);
		MwImage createdImage = imgDao.createImage(mwImage);
		return mapper.toTransferObject(createdImage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#updateImage(com
	 * .intel.director.api.ImageAttributes)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#updateImage(com.intel
	 * .director.api.ImageAttributes)
	 */
	public void updateImage(ImageAttributes img) throws DbException {
		MwImage mwImage = imgDao.getMwImage(img.getId());
		if (img.getCreated_date() != null) {
			mwImage.setCreatedDate(new java.sql.Date(img.getCreated_date()
					.getTime()));
		}
		if (img.getCreated_by_user_id() != null) {
			mwImage.setCreatedByUserId(img.getCreated_by_user_id());
		}
		if (img.getStatus() != null) {
			mwImage.setStatus(img.getStatus());
		}
		mwImage.setMountedByUserId(img.getMounted_by_user_id());
		if (img.getEdited_by_user_id() != null) {
			mwImage.setEditedByUserId(img.getEdited_by_user_id());
		}
		if (img.getEdited_date() != null) {
			mwImage.setEditedDate(new java.sql.Date(img.getEdited_date()
					.getTime()));
		}
		if (img.getLocation() != null) {
			mwImage.setLocation(img.getLocation());
		}
		if (img.getSent() != null) {
			mwImage.setSent(img.getSent());
		}
		if (img.getName() != null) {
			mwImage.setName(img.getName());
		}
		if (img.getImage_size() != null) {
			mwImage.setContentlength(img.getImage_size());
		}
		if (img.getImage_format() != null) {
			mwImage.setImageFormat(img.getImage_format());
		}
		mwImage.setDeleted(img.isDeleted());

		imgDao.updateImage(mwImage);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImages(com
	 * .intel.director.api.ImageInfoFilter,
	 * com.intel.director.api.ImageInfoOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImages(com.intel
	 * .director.api.ImageInfoFilter, com.intel.director.api.ImageInfoOrderBy)
	 */
	public List<ImageInfo> fetchImages(ImageInfoFilter imgFilter,
			ImageInfoOrderBy orderBy) throws DbException {
		return imgDao.findMwImageEntities(imgFilter, orderBy);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImages(com
	 * .intel.director.api.ImageInfoFilter,
	 * com.intel.director.api.ImageInfoOrderBy, int, int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImages(com.intel
	 * .director.api.ImageInfoFilter, com.intel.director.api.ImageInfoOrderBy,
	 * int, int)
	 */
	public List<ImageInfo> fetchImages(ImageInfoFilter imgFilter,
			ImageInfoOrderBy orderBy, int firstRecord, int maxRecords)
			throws DbException {
		return imgDao.findMwImageEntities(firstRecord, maxRecords, imgFilter,
				orderBy);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImageById(
	 * java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImageById(java
	 * .lang.String)
	 */
	public ImageInfo fetchImageById(String id) throws DbException {
		return imgDao.findMwImage(id);
	}

	public ImageInfo fetchImage(String id) throws DbException {
		return mapper.toTransferObject(imgDao.findMwImageById(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#destroyImage(com
	 * .intel.director.api.ImageAttributes)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#destroyImage(com.intel
	 * .director.api.ImageAttributes)
	 */
	public void destroyImage(ImageAttributes img) throws DbException {
		MwImage mwImage = mapper.toData(img);
		imgDao.destroyImage(mwImage);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#getTotalImagesCount
	 * ()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalImagesCount()
	 */
	public int getTotalImagesCount() throws DbException {
		return imgDao.getMwImageCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#getTotalImagesCount
	 * (com.intel.director.api.ImageInfoFilter)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalImagesCount
	 * (com.intel.director.api.ImageInfoFilter)
	 */
	public int getTotalImagesCount(ImageInfoFilter imgFilter)
			throws DbException {
		return imgDao.getMwImageCount(imgFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImages(com
	 * .intel.director.api.ImageInfoOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImages(com.intel
	 * .director.api.ImageInfoOrderBy)
	 */
	public List<ImageInfo> fetchImages(ImageInfoOrderBy orderBy)
			throws DbException {
		return imgDao.findMwImageEntities(null, orderBy);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImages(com
	 * .intel.director.api.ImageInfoOrderBy, int, int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImages(com.intel
	 * .director.api.ImageInfoOrderBy, int, int)
	 */
	public List<ImageInfo> fetchImages(ImageInfoOrderBy orderBy,
			int firstRecord, int maxRecords) throws DbException {
		return imgDao.findMwImageEntities(firstRecord, maxRecords, null,
				orderBy);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicyForImage
	 * (java.lang.String)
	 */
	public TrustPolicy fetchPolicyForImage(String imageId) throws DbException {

		TrustPolicyFilter trustPolicyFilter = new TrustPolicyFilter();
		trustPolicyFilter.setImage_id(imageId);
		List<MwTrustPolicy> mwTrustPolicyList = policyDao
				.findMwTrustPolicyEntities(trustPolicyFilter, null);
		if (mwTrustPolicyList.size() == 0) {
			return null;
		}
		return mapper.toTransferObject(mwTrustPolicyList.get(0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicyDraftForImage
	 * (java.lang.String)
	 */
	public TrustPolicyDraft fetchPolicyDraftForImage(String imageId)
			throws DbException {
		TrustPolicyDraftFilter trustPolicydraftFilter = new TrustPolicyDraftFilter();
		trustPolicydraftFilter.setImage_id(imageId);
		List<MwTrustPolicyDraft> mwTrustPolicyDraftList = policyDraftDao
				.findMwTrustPolicyDraftEntities(trustPolicydraftFilter, null);
		if (mwTrustPolicyDraftList.size() == 0) {
			return null;
		}
		return mapper.toTransferObject(mwTrustPolicyDraftList.get(0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicyUploadsForImage
	 * (java.lang.String)
	 */
	public List<ImageStoreUploadTransferObject> fetchPolicyUploadsForImage(
			String imageId) throws DbException {
		ImageStoreUploadFilter imgUploadFilter = new ImageStoreUploadFilter();
		imgUploadFilter.setImage_id(imageId);
		List<MwImageUpload> mwImageUploadList = imgUploadDao
				.findMwImageUploadEntities(imgUploadFilter, null);
		List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

		for (MwImageUpload MwImageUpload : mwImageUploadList) {
			imgUploadList.add(mapper.toTransferObject(MwImageUpload));
		}
		return imgUploadList;

	}

	/*
	 * ##########################################################################
	 * ##################################################
	 * ################################################### USER
	 * ############################################################
	 * #############
	 * #############################################################
	 * ###################################################
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#saveUser(com.intel
	 * .director.api.User)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#saveUser(com.intel.
	 * director.api.User)
	 */
	public User saveUser(User user) throws DbException {
		MwUser mwUser = mapper.toData(user);
		MwUser createdUser = userDao.createUser(mwUser);
		return mapper.toTransferObject(createdUser);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#updateUser(com.
	 * intel.director.api.User)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#updateUser(com.intel
	 * .director.api.User)
	 */
	public void updateUser(User user) throws DbException {
		MwUser mwUser = mapper.toData(user);
		userDao.updateUser(mwUser);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchUsers(com.
	 * intel.director.api.UserFilter, com.intel.director.api.UserOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchUsers(com.intel
	 * .director.api.UserFilter, com.intel.director.api.UserOrderBy)
	 */
	public List<User> fetchUsers(UserFilter userFilter, UserOrderBy orderBy)
			throws DbException {
		List<MwUser> mwuserList = userDao.findMwUserEntities(userFilter,
				orderBy);
		List<User> userList = new ArrayList<User>();

		for (MwUser mwUser : mwuserList) {
			userList.add(mapper.toTransferObject(mwUser));
		}
		return userList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchUsers(com.
	 * intel.director.api.UserFilter, com.intel.director.api.UserOrderBy, int,
	 * int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchUsers(com.intel
	 * .director.api.UserFilter, com.intel.director.api.UserOrderBy, int, int)
	 */
	public List<User> fetchUsers(UserFilter userFilter, UserOrderBy orderBy,
			int firstRecord, int maxRecords) throws DbException {
		List<MwUser> MwuserList = userDao.findMwUserEntities(firstRecord,
				maxRecords, userFilter, orderBy);
		List<User> userList = new ArrayList<User>();

		for (MwUser mwUser : MwuserList) {
			userList.add(mapper.toTransferObject(mwUser));
		}
		return userList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchUserById(java
	 * .lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchUserById(java.
	 * lang.String)
	 */
	public User fetchUserById(String id) throws DbException {
		return mapper.toTransferObject(userDao.findMwUser(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#destroyUser(com
	 * .intel.director.api.User)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#destroyUser(com.intel
	 * .director.api.User)
	 */
	public void destroyUser(User User) throws DbException {
		MwUser MwUser = mapper.toData(User);
		userDao.destroyUser(MwUser);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#getTotalUsersCount
	 * ()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalUsersCount()
	 */
	public int getTotalUsersCount() throws DbException {
		return userDao.getMwUserCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#getTotalUsersCount
	 * (com.intel.director.api.UserFilter)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalUsersCount(
	 * com.intel.director.api.UserFilter)
	 */
	public int getTotalUsersCount(UserFilter userFilter) throws DbException {
		return userDao.getMwUserCount(userFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchUsers(com.
	 * intel.director.api.UserOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchUsers(com.intel
	 * .director.api.UserOrderBy)
	 */
	public List<User> fetchUsers(UserOrderBy orderBy) throws DbException {
		List<MwUser> MwuserList = userDao.findMwUserEntities(null, orderBy);
		List<User> userList = new ArrayList<User>();

		for (MwUser mwUser : MwuserList) {
			userList.add(mapper.toTransferObject(mwUser));
		}
		return userList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchUsers(com.
	 * intel.director.api.UserOrderBy, int, int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchUsers(com.intel
	 * .director.api.UserOrderBy, int, int)
	 */
	public List<User> fetchUsers(UserOrderBy orderBy, int firstRecord,
			int maxRecords) throws DbException {
		List<MwUser> mwuserList = userDao.findMwUserEntities(firstRecord,
				maxRecords, null, orderBy);
		List<User> userList = new ArrayList<User>();

		for (MwUser mwUser : mwuserList) {
			userList.add(mapper.toTransferObject(mwUser));
		}
		return userList;
	}

	/*
	 * ##########################################################################
	 * ##################################################
	 * ################################################### TRUST
	 * POLICY############################################################
	 * #######
	 * ###################################################################
	 * ###################################################
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#savePolicy(com.
	 * intel.director.api.TrustPolicy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#savePolicy(com.intel
	 * .director.api.TrustPolicy)
	 */
	public TrustPolicy savePolicy(TrustPolicy trustPolicy) throws DbException {
		MwTrustPolicy mwTrustPolicy = mapper.toData(trustPolicy);

		MwTrustPolicy createdPolicy = policyDao
				.createTrustPolicy(mwTrustPolicy);
		return mapper.toTransferObject(createdPolicy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#updatePolicy(com
	 * .intel.director.api.TrustPolicy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#updatePolicy(com.intel
	 * .director.api.TrustPolicy)
	 */
	public void updatePolicy(TrustPolicy trustPolicy) throws DbException {
		MwTrustPolicy mwTrustPolicy = mapper.toData(trustPolicy);
		policyDao.updateTrustPolicy(mwTrustPolicy);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicies(com
	 * .intel.director.api.TrustPolicyFilter,
	 * com.intel.director.api.TrustPolicyOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicies(com.intel
	 * .director.api.TrustPolicyFilter,
	 * com.intel.director.api.TrustPolicyOrderBy)
	 */
	public List<TrustPolicy> fetchPolicies(TrustPolicyFilter trustPolicyFilter,
			TrustPolicyOrderBy orderBy) throws DbException {
		List<MwTrustPolicy> mwTrustPolicyList = policyDao
				.findMwTrustPolicyEntities(trustPolicyFilter, orderBy);
		List<TrustPolicy> trustPolicyList = new ArrayList<TrustPolicy>();

		for (MwTrustPolicy mwTP : mwTrustPolicyList) {
			trustPolicyList.add(mapper.toTransferObject(mwTP));
		}
		return trustPolicyList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicies(com
	 * .intel.director.api.TrustPolicyFilter,
	 * com.intel.director.api.TrustPolicyOrderBy, int, int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicies(com.intel
	 * .director.api.TrustPolicyFilter,
	 * com.intel.director.api.TrustPolicyOrderBy, int, int)
	 */
	public List<TrustPolicy> fetchPolicies(TrustPolicyFilter trustPolicyFilter,
			TrustPolicyOrderBy orderBy, int firstRecord, int maxRecords)
			throws DbException {
		List<MwTrustPolicy> MwTrustPolicyList = policyDao
				.findMwTrustPolicyEntities(firstRecord, maxRecords,
						trustPolicyFilter, orderBy);
		List<TrustPolicy> trustPolicyList = new ArrayList<TrustPolicy>();

		for (MwTrustPolicy mwTP : MwTrustPolicyList) {
			trustPolicyList.add(mapper.toTransferObject(mwTP));
		}
		return trustPolicyList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyById
	 * (java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicyById(java
	 * .lang.String)
	 */
	public TrustPolicy fetchPolicyById(String id) throws DbException {
		return mapper.toTransferObject(policyDao.findMwTrustPolicy(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#destroyPolicy(com
	 * .intel.director.api.TrustPolicy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#destroyPolicy(com.intel
	 * .director.api.TrustPolicy)
	 */
	public void destroyPolicy(TrustPolicy trustPolicy) throws DbException {
		MwTrustPolicy MwTrustPolicy = mapper.toData(trustPolicy);
		policyDao.destroyTrustPolicy(MwTrustPolicy);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#getTotalPoliciesCount
	 * ()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalPoliciesCount()
	 */
	public int getTotalPoliciesCount() throws DbException {
		return policyDao.getMwTrustPolicyCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#getTotalPoliciesCount
	 * (com.intel.director.api.TrustPolicyFilter)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalPoliciesCount
	 * (com.intel.director.api.TrustPolicyFilter)
	 */
	public int getTotalPoliciesCount(TrustPolicyFilter trustPolicyFilter)
			throws DbException {
		return policyDao.getMwTrustPolicyCount(trustPolicyFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicies(com
	 * .intel.director.api.TrustPolicyOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicies(com.intel
	 * .director.api.TrustPolicyOrderBy)
	 */
	public List<TrustPolicy> fetchPolicies(TrustPolicyOrderBy orderBy)
			throws DbException {
		List<MwTrustPolicy> MwTrustPolicyList = policyDao
				.findMwTrustPolicyEntities(null, orderBy);
		List<TrustPolicy> trustPolicyList = new ArrayList<TrustPolicy>();

		for (MwTrustPolicy mwTP : MwTrustPolicyList) {
			trustPolicyList.add(mapper.toTransferObject(mwTP));
		}
		return trustPolicyList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicies(com
	 * .intel.director.api.TrustPolicyOrderBy, int, int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicies(com.intel
	 * .director.api.TrustPolicyOrderBy, int, int)
	 */
	public List<TrustPolicy> fetchPolicies(TrustPolicyOrderBy orderBy,
			int firstRecord, int maxRecords) throws DbException {
		List<MwTrustPolicy> MwTrustPolicyList = policyDao
				.findMwTrustPolicyEntities(firstRecord, maxRecords, null,
						orderBy);
		List<TrustPolicy> trustPolicyList = new ArrayList<TrustPolicy>();

		for (MwTrustPolicy mwTP : MwTrustPolicyList) {
			trustPolicyList.add(mapper.toTransferObject(mwTP));
		}
		return trustPolicyList;
	}

	/*
	 * ##########################################################################
	 * ##################################################
	 * ################################################### TRUST POLICY
	 * DRAFT############################################################
	 * ########
	 * ##################################################################
	 * ###################################################
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#savePolicyDraft
	 * (com.intel.director.api.TrustPolicyDraft)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#savePolicyDraft(com
	 * .intel.director.api.TrustPolicyDraft)
	 */
	public TrustPolicyDraft savePolicyDraft(TrustPolicyDraft trustPolicyDraft)
			throws DbException {
		MwTrustPolicyDraft mwTrustPolicyDraft = mapper.toData(trustPolicyDraft);
		MwTrustPolicyDraft createdPolicyDraft = policyDraftDao
				.createTrustPolicyDraft(mwTrustPolicyDraft);
		return mapper.toTransferObject(createdPolicyDraft);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#updatePolicyDraft
	 * (com.intel.director.api.TrustPolicyDraft)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#updatePolicyDraft(com
	 * .intel.director.api.TrustPolicyDraft)
	 */
	public void updatePolicyDraft(TrustPolicyDraft trustPolicyDraft)
			throws DbException {
		MwTrustPolicyDraft mwTrustPolicyDraft = mapper.toData(trustPolicyDraft);
		policyDraftDao.updateTrustPolicyDraft(mwTrustPolicyDraft);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDrafts
	 * (com.intel.director.api.TrustPolicyDraftFilter,
	 * com.intel.director.api.TrustPolicyDraftOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicyDrafts(com
	 * .intel.director.api.TrustPolicyDraftFilter,
	 * com.intel.director.api.TrustPolicyDraftOrderBy)
	 */
	public List<TrustPolicyDraft> fetchPolicyDrafts(
			TrustPolicyDraftFilter trustPolicyDraftFilter,
			TrustPolicyDraftOrderBy orderBy) throws DbException {
		List<MwTrustPolicyDraft> mwTrustPolicyDraftList = policyDraftDao
				.findMwTrustPolicyDraftEntities(trustPolicyDraftFilter, orderBy);
		List<TrustPolicyDraft> trustPolicyDraftList = new ArrayList<TrustPolicyDraft>();

		for (MwTrustPolicyDraft mwTPD : mwTrustPolicyDraftList) {
			trustPolicyDraftList.add(mapper.toTransferObject(mwTPD));
		}
		return trustPolicyDraftList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDrafts
	 * (com.intel.director.api.TrustPolicyDraftFilter,
	 * com.intel.director.api.TrustPolicyDraftOrderBy, int, int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicyDrafts(com
	 * .intel.director.api.TrustPolicyDraftFilter,
	 * com.intel.director.api.TrustPolicyDraftOrderBy, int, int)
	 */
	public List<TrustPolicyDraft> fetchPolicyDrafts(
			TrustPolicyDraftFilter trustPolicyDraftFilter,
			TrustPolicyDraftOrderBy orderBy, int firstRecord, int maxRecords)
			throws DbException {
		List<MwTrustPolicyDraft> MwTrustPolicyDraftList = policyDraftDao
				.findMwTrustPolicyDraftEntities(firstRecord, maxRecords,
						trustPolicyDraftFilter, orderBy);
		List<TrustPolicyDraft> trustPolicyDraftList = new ArrayList<TrustPolicyDraft>();

		for (MwTrustPolicyDraft mwTPD : MwTrustPolicyDraftList) {
			trustPolicyDraftList.add(mapper.toTransferObject(mwTPD));
		}
		return trustPolicyDraftList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDraftById
	 * (java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicyDraftById
	 * (java.lang.String)
	 */
	public TrustPolicyDraft fetchPolicyDraftById(String id) throws DbException {
		return mapper.toTransferObject(policyDraftDao
				.findMwTrustPolicyDraft(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#destroyPolicyDraft
	 * (com.intel.director.api.TrustPolicyDraft)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#destroyPolicyDraft(
	 * com.intel.director.api.TrustPolicyDraft)
	 */
	public void destroyPolicyDraft(TrustPolicyDraft trustPolicyDraft)
			throws DbException {
		MwTrustPolicyDraft MwTrustPolicyDraft = mapper.toData(trustPolicyDraft);
		policyDraftDao.destroyPolicyDraft(MwTrustPolicyDraft);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.intel.mtwilson.director.dbservice.IPersistService#
	 * getTotalPolicyDraftsCount()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalPolicyDraftsCount
	 * ()
	 */
	public int getTotalPolicyDraftsCount() throws DbException {
		return policyDraftDao.getMwTrustPolicyDraftCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.intel.mtwilson.director.dbservice.IPersistService#
	 * getTotalPolicyDraftsCount(com.intel.director.api.TrustPolicyDraftFilter)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalPolicyDraftsCount
	 * (com.intel.director.api.TrustPolicyDraftFilter)
	 */
	public int getTotalPolicyDraftsCount(
			TrustPolicyDraftFilter trustPolicyDraftFilter) throws DbException {
		return policyDraftDao
				.getMwTrustPolicyDraftCount(trustPolicyDraftFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDrafts
	 * (com.intel.director.api.TrustPolicyDraftOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicyDrafts(com
	 * .intel.director.api.TrustPolicyDraftOrderBy)
	 */
	public List<TrustPolicyDraft> fetchPolicyDrafts(
			TrustPolicyDraftOrderBy orderBy) throws DbException {
		List<MwTrustPolicyDraft> MwTrustPolicyDraftList = policyDraftDao
				.findMwTrustPolicyDraftEntities(null, orderBy);
		List<TrustPolicyDraft> trustPolicyDraftList = new ArrayList<TrustPolicyDraft>();

		for (MwTrustPolicyDraft mwTPD : MwTrustPolicyDraftList) {
			trustPolicyDraftList.add(mapper.toTransferObject(mwTPD));
		}
		return trustPolicyDraftList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDrafts
	 * (com.intel.director.api.TrustPolicyDraftOrderBy, int, int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchPolicyDrafts(com
	 * .intel.director.api.TrustPolicyDraftOrderBy, int, int)
	 */
	public List<TrustPolicyDraft> fetchPolicyDrafts(
			TrustPolicyDraftOrderBy orderBy, int firstRecord, int maxRecords)
			throws DbException {
		List<MwTrustPolicyDraft> MwTrustPolicyDraftList = policyDraftDao
				.findMwTrustPolicyDraftEntities(firstRecord, maxRecords, null,
						orderBy);
		List<TrustPolicyDraft> trustPolicyDraftList = new ArrayList<TrustPolicyDraft>();

		for (MwTrustPolicyDraft mwTPD : MwTrustPolicyDraftList) {
			trustPolicyDraftList.add(mapper.toTransferObject(mwTPD));
		}
		return trustPolicyDraftList;
	}

	/*
	 * ##########################################################################
	 * ##################################################
	 * ################################################### IMAGE
	 * UPLOADS############################################################
	 * ######
	 * ####################################################################
	 * ###################################################
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#saveImageUpload
	 * (com.intel.director.api.ImageStoreUploadTransferObject)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#saveImageUpload(com
	 * .intel.director.api.ImageStoreUploadTransferObject)
	 */
	public ImageStoreUploadTransferObject saveImageUpload(
			ImageStoreUploadTransferObject imgUpload) throws DbException {
		MwImageUpload MwImageUpload = mapper.toData(imgUpload);
		MwImageUpload createdImageUpload = imgUploadDao
				.createImageUpload(MwImageUpload);
		return mapper.toTransferObject(createdImageUpload);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#updateImageUpload
	 * (com.intel.director.api.ImageStoreUploadTransferObject)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#updateImageUpload(com
	 * .intel.director.api.ImageStoreUploadTransferObject)
	 */
	public void updateImageUpload(ImageStoreUploadTransferObject imgUpload)
			throws DbException {
		MwImageUpload MwImageUpload = mapper.toData(imgUpload);
		imgUploadDao.updateImageUpload(MwImageUpload);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploads
	 * (com.intel.director.api.ImageStoreUploadFilter,
	 * com.intel.director.api.ImageStoreUploadOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImageUploads(com
	 * .intel.director.api.ImageStoreUploadFilter,
	 * com.intel.director.api.ImageStoreUploadOrderBy)
	 */
	public List<ImageStoreUploadTransferObject> fetchImageUploads(
			ImageStoreUploadFilter imgUploadFilter,
			ImageStoreUploadOrderBy orderBy) throws DbException {
		List<MwImageUpload> mwImageUploadList = imgUploadDao
				.findMwImageUploadEntities(imgUploadFilter, orderBy);
		List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

		for (MwImageUpload MwImageUpload : mwImageUploadList) {
			imgUploadList.add(mapper.toTransferObject(MwImageUpload));
		}
		return imgUploadList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploads
	 * (com.intel.director.api.ImageStoreUploadFilter,
	 * com.intel.director.api.ImageStoreUploadOrderBy, int, int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImageUploads(com
	 * .intel.director.api.ImageStoreUploadFilter,
	 * com.intel.director.api.ImageStoreUploadOrderBy, int, int)
	 */
	public List<ImageStoreUploadTransferObject> fetchImageUploads(
			ImageStoreUploadFilter imgUploadFilter,
			ImageStoreUploadOrderBy orderBy, int firstRecord, int maxRecords)
			throws DbException {
		List<MwImageUpload> mwImageUploadList = imgUploadDao
				.findMwImageUploadEntities(firstRecord, maxRecords,
						imgUploadFilter, orderBy);
		List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

		for (MwImageUpload MwImageUpload : mwImageUploadList) {
			imgUploadList.add(mapper.toTransferObject(MwImageUpload));
		}
		return imgUploadList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploadById
	 * (java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImageUploadById
	 * (java.lang.String)
	 */
	public ImageStoreUploadTransferObject fetchImageUploadById(String id)
			throws DbException {
		return mapper.toTransferObject(imgUploadDao.findMwImageUpload(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#destroyImageUpload
	 * (com.intel.director.api.ImageStoreUploadTransferObject)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#destroyImageUpload(
	 * com.intel.director.api.ImageStoreUploadTransferObject)
	 */
	public void destroyImageUpload(
			ImageStoreUploadTransferObject ImageStoreUploadTransferObject)
			throws DbException {
		MwImageUpload MwImageUpload = mapper
				.toData(ImageStoreUploadTransferObject);
		imgUploadDao.destroyImageUpload(MwImageUpload);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.intel.mtwilson.director.dbservice.IPersistService#
	 * getTotalImageUploadsCount()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalImageUploadsCount
	 * ()
	 */
	public int getTotalImageUploadsCount() throws DbException {
		return imgUploadDao.getMwImageUploadCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.intel.mtwilson.director.dbservice.IPersistService#
	 * getTotalImageUploadsCount(com.intel.director.api.ImageStoreUploadFilter)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#getTotalImageUploadsCount
	 * (com.intel.director.api.ImageStoreUploadFilter)
	 */
	public int getTotalImageUploadsCount(ImageStoreUploadFilter imgUploadFilter)
			throws DbException {
		return imgUploadDao.getMwImageUploadCount(imgUploadFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploads
	 * (com.intel.director.api.ImageStoreUploadOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImageUploads(com
	 * .intel.director.api.ImageStoreUploadOrderBy)
	 */
	public List<ImageStoreUploadTransferObject> fetchImageUploads(
			ImageStoreUploadOrderBy orderBy) throws DbException {
		List<MwImageUpload> mwImageUploadList = imgUploadDao
				.findMwImageUploadEntities(null, orderBy);
		List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

		for (MwImageUpload MwImageUpload : mwImageUploadList) {
			imgUploadList.add(mapper.toTransferObject(MwImageUpload));
		}
		return imgUploadList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploads
	 * (com.intel.director.api.ImageStoreUploadOrderBy, int, int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImageUploads(com
	 * .intel.director.api.ImageStoreUploadOrderBy, int, int)
	 */
	public List<ImageStoreUploadTransferObject> fetchImageUploads(
			ImageStoreUploadOrderBy orderBy, int firstRecord, int maxRecords)
			throws DbException {
		List<MwImageUpload> mwImageUploadList = imgUploadDao
				.findMwImageUploadEntities(firstRecord, maxRecords, null,
						orderBy);
		List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

		for (MwImageUpload mwImageStoreUploadTransferObject : mwImageUploadList) {
			imgUploadList.add(mapper
					.toTransferObject(mwImageStoreUploadTransferObject));
		}
		return imgUploadList;
	}

	/*
	 * ##########################################################################
	 * ##################################################
	 * ################################################### IMAGE STORE SETTINGS
	 * ############################################################
	 * #############
	 * #############################################################
	 * ###################################################
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#saveImageUpload
	 * (com.intel.director.api.ImageStoreUploadTransferObject)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#saveImageStoreSettings
	 * (com.intel.director.api.ImageStoreSettings)
	 */
	public ImageStoreSettings saveImageStoreSettings(
			ImageStoreSettings imgStoreSettings) throws DbException {
		MwImageStoreSettings mwImageStoreSettings = mapper
				.toData(imgStoreSettings);
		MwImageStoreSettings createdImageStoreSettings = imgStoreSettingsDao
				.createImageStoreSettings(mwImageStoreSettings);
		return mapper.toTransferObject(createdImageStoreSettings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#updateImageUpload
	 * (com.intel.director.api.ImageStoreUploadTransferObject)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#updateImageStoreSettings
	 * (com.intel.director.api.ImageStoreSettings)
	 */
	public void updateImageStoreSettings(ImageStoreSettings imgStoreSettings)
			throws DbException {
		MwImageStoreSettings mwImageStoreSettings = mapper
				.toData(imgStoreSettings);
		imgStoreSettingsDao.updateImageStoreSettings(mwImageStoreSettings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploads
	 * (com.intel.director.api.ImageStoreUploadFilter,
	 * com.intel.director.api.ImageStoreUploadOrderBy)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImageStoreSettings
	 * ()
	 */
	public List<ImageStoreSettings> fetchImageStoreSettings()
			throws DbException {
		List<MwImageStoreSettings> mwImageStoreSettingList = imgStoreSettingsDao
				.fetchImageStoreSettings();
		List<ImageStoreSettings> imgStoreSettingsList = new ArrayList<ImageStoreSettings>();

		for (MwImageStoreSettings mwImageStoreSettings : mwImageStoreSettingList) {
			imgStoreSettingsList.add(mapper
					.toTransferObject(mwImageStoreSettings));
		}
		return imgStoreSettingsList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploadById
	 * (java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#fetchImageStoreSettingsById
	 * (java.lang.String)
	 */
	public ImageStoreSettings fetchImageStoreSettingsById(String id)
			throws DbException {
		return mapper.toTransferObject(imgStoreSettingsDao
				.fetchImageStoreSettingsById(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploadById
	 * (java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.intel.mtwilson.director.dbservice.IPersistDao#
	 * fetchImageStoreSettingsByName(java.lang.String)
	 */
	public ImageStoreSettings fetchImageStoreSettingsByName(String name)
			throws DbException {
		return mapper.toTransferObject(imgStoreSettingsDao
				.fetchImageStoreSettingsByName(name));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistService#destroyImageUpload
	 * (com.intel.director.api.ImageStoreUploadTransferObject)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.intel.mtwilson.director.dbservice.IPersistDao#destroyImageStoreSettings
	 * (com.intel.director.api.ImageStoreSettings)
	 */
	public void destroyImageStoreSettings(ImageStoreSettings imgStoreSettings)
			throws DbException {
		MwImageStoreSettings mwImageStoreSettings = mapper
				.toData(imgStoreSettings);
		imgStoreSettingsDao.destroyImageStoreSettings(mwImageStoreSettings);
	}

	@Override
	public ImageActionObject createImageAction(
			ImageActionObject imageactionobject) throws DbException {
		MwImageAction mwImageAction = mapper.toData(imageactionobject);
		return mapper.toTransferObject(imageActionDao
				.createImageAction(mwImageAction));
	}

	@Override
	public void updateImageAction(String id, ImageActionObject imageactionobject)
			throws DbException {
		MwImageAction mwImageAction = mapper.toDataUpdate(imageactionobject);
		mwImageAction.setId(id);
		imageActionDao.updateImageAction(mwImageAction);
	}

	@Override
	public void updateImageAction(ImageActionObject imageactionobject)
			throws DbException {
		MwImageAction mwImageAction = mapper.toDataUpdate(imageactionobject);
		imageActionDao.updateImageAction(mwImageAction);
	}

	@Override
	public void deleteImageAction(ImageActionObject imageactionobject)
			throws DbException {
		MwImageAction mwImageAction = mapper.toData(imageactionobject);
		imageActionDao.deleteImageAction(mwImageAction);

	}

	@Override
	public List<ImageActionObject> searchByAction() throws DbException {
		List<ImageActionObject> imageActionObject = new ArrayList<ImageActionObject>();
		List<MwImageAction> mwImageAction = imageActionDao.showAllAction();

		for (int index = 0; index < mwImageAction.size(); index++) {

			ImageActionObject actionObject = mapper
					.toTransferObject(mwImageAction.get(index));
			imageActionObject.add(actionObject);
		}
		return imageActionObject;
	}

	@Override
	public void deleteImageActionById(String image_action_id)
			throws DbException {
		imageActionDao.deleteImageActionByID(image_action_id);

	}

	@Override
	public ImageActionObject fetchImageActionById(String image_action_id)
			throws DbException {
		return mapper.toTransferObject(imageActionDao
				.getImageActionByID(image_action_id));
	}

	/*
	 * ##########################################################################
	 * ##################################################
	 * ################################################### MWHOST
	 * ############################################################
	 * #############
	 * #############################################################
	 * ###################################################
	 */

	public SshSettingInfo saveSshMetadata(SshSettingInfo ssh)
			throws DbException {
		MwHost mwHost = mapper.toData(ssh);
		MwHost createdSsh = sshDao.createSshSetting(mwHost);
		return mapper.toTransferObject(createdSsh);
	}

	public void updateSsh(SshSettingInfo ssh) throws DbException {
		MwImage mwImage = imgDao.getMwImage(ssh.getImage_id().getId());
		mwImage.setName(ssh.getIpAddress());
		
		MwHost mwSsh = mapper.toDataUpdate(ssh);
		mwSsh.setImageId(mwImage);
		sshDao.updateSshSetting(mwSsh);

	}

	public void updateSshById(String sshId) throws DbException {
		MwHost mwSsh = sshDao.fetchSshSettingById(sshId);
		// MwHost mwSsh = mapper.toData(mw);
		sshDao.updateSshSetting(mwSsh);

	}

	public SshSettingInfo fetchSshById(String id) throws DbException {
		MwHost mwHost = sshDao.getMwHost(id);
		return mapper.toTransferObject(mwHost);
	}

	public SshSettingInfo fetchSshByImageId(String image_id) throws DbException {
		if(image_id == null){
			throw new DbException("No image id provided");
		}
		MwHost mwHost = sshDao.getMwHostByImageId(image_id);
		if (mwHost != null && mwHost.getId() != null
				&& StringUtils.isNotBlank(mwHost.getId())) {
			return mapper.toTransferObject(mwHost);
		} else {
			return new SshSettingInfo();
		}
	}

	public void destroySsh(SshSettingInfo ssh) throws DbException {
		MwHost mwHost = mapper.toDataUpdate(ssh);
		sshDao.destroySshSetting(mwHost);

	}

	public void destroySshById(String sshId) throws DbException {

		sshDao.destroySshSettingById(sshId);

	}

	public List<SshSettingInfo> showAllSsh() throws DbException {

		List<MwHost> sshList;
		sshList = sshDao.showAll();
		int i = 0;
		List<SshSettingInfo> ssh = new ArrayList<SshSettingInfo>();
		int length = sshList.size();
		while (i < length) {
			ssh.add(mapper.toTransferObject(sshList.get(i)));
			i++;
		}
		return ssh;

	}

	@Override
	public String editProperties(String path, Map<String, String> data)
			throws IOException {
		return settingFileProperties.writePropertiesToConfig(path, data);
	}

	@Override
	public String getProperties(String path) throws IOException {
		return settingFileProperties.readPropertiesFromConfig(path);
	}

	@Override
	public PolicyTemplateInfo savePolicyTemplate(
			PolicyTemplateInfo policyTemplate) throws DbException {
		return mapper.toTransferObject(policyTemplateDao
				.createPolicyTemplate(mapper.toData(policyTemplate)));
	}

	@Override
	public List<PolicyTemplateInfo> fetchPolicyTemplate(String deployment_type)
			throws DbException {
		List<MwPolicyTemplate> mwPolicyTemplateList = policyTemplateDao
				.findDeploymentType(deployment_type);
		List<PolicyTemplateInfo> policyTemplateList = new ArrayList<PolicyTemplateInfo>();
		if (mwPolicyTemplateList != null) {
			for (MwPolicyTemplate mwPolicyTemplate : mwPolicyTemplateList) {
				PolicyTemplateInfo policyTemplateInfo = mapper.toTransferObject(mwPolicyTemplate);
				policyTemplateList.add(policyTemplateInfo);
			}
		}
		return policyTemplateList;
	}

	@Override
	public List<PolicyTemplateInfo> fetchPolicyTemplateForDeploymentIdentifier(
			String deployment_type, String deployment_type_identifier)
			throws DbException {
		List<MwPolicyTemplate> mwPolicyTemplateList = policyTemplateDao
				.findDeploymentType(deployment_type);
		List<PolicyTemplateInfo> policyTemplateList = new ArrayList<PolicyTemplateInfo>();
		if (mwPolicyTemplateList != null) {
			for (MwPolicyTemplate mwPolicyTemplate : mwPolicyTemplateList) {
				if (deployment_type_identifier.equals(mwPolicyTemplate
						.getDeployment_type_identifier())) {
					PolicyTemplateInfo policyTemplateInfo = mapper
							.toTransferObject(mwPolicyTemplate);
					policyTemplateList.add(policyTemplateInfo);
				}

			}
		}
		return policyTemplateList;
	}

	@Override
	public void deletePolicyTemplate(PolicyTemplateInfo policyTemplate)
			throws DbException {
		policyTemplateDao.destroyPolicyTemplate(mapper.toData(policyTemplate));
	}

	@Override
	public List<PolicyTemplateInfo> fetchPolicyTemplate(
			PolicyTemplateInfo filter) throws DbException {
		List<MwPolicyTemplate> mwPolicyTemplateList = policyTemplateDao
				.findDeploymentTypeByFilter(filter);
		List<PolicyTemplateInfo> policyTemplateList = new ArrayList<PolicyTemplateInfo>();
		if (mwPolicyTemplateList != null) {
			for (MwPolicyTemplate mwPolicyTemplate : mwPolicyTemplateList) {
				PolicyTemplateInfo policyTemplateInfo = mapper.toTransferObject(mwPolicyTemplate);
				policyTemplateList.add(policyTemplateInfo);

			}
		}
		return policyTemplateList;
	}

	@Override
	public void destroySshPassword(String id) throws DbException {
		sshDao.destroyPassword(id);
	}

}
