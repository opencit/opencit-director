package com.intel.mtwilson.director.dbservice;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.api.ui.ImageInfoFilter;
import com.intel.director.api.ui.ImageInfoOrderBy;
import com.intel.director.api.ui.ImageStoreUploadFilter;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftFilter;
import com.intel.director.api.ui.TrustPolicyDraftOrderBy;
import com.intel.director.api.ui.TrustPolicyFilter;
import com.intel.director.api.ui.TrustPolicyOrderBy;
import com.intel.director.api.User;
import com.intel.director.api.ui.UserFilter;
import com.intel.director.api.ui.UserOrderBy;
import com.intel.mtwilson.director.dao.ImageDao;
import com.intel.mtwilson.director.dao.ImageStoreUploadDao;
import com.intel.mtwilson.director.dao.TrustPolicyDao;
import com.intel.mtwilson.director.dao.TrustPolicyDraftDao;
import com.intel.mtwilson.director.dao.UserDao;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.data.MwImageUpload;
import com.intel.mtwilson.director.data.MwTrustPolicy;
import com.intel.mtwilson.director.data.MwTrustPolicyDraft;
import com.intel.mtwilson.director.data.MwUser;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class DbServiceImpl implements IPersistService {

    ImageDao imgDao;
    UserDao userDao;
    TrustPolicyDao policyDao;
    TrustPolicyDraftDao policyDraftDao;
    ImageStoreUploadDao imgUploadDao;
    Mapper mapper = new Mapper();

    public DbServiceImpl() {
        EntityManagerFactory emf = Persistence
                .createEntityManagerFactory("DirectorDataPU");
        imgDao = new ImageDao(emf);
        userDao = new UserDao(emf);
        policyDao = new TrustPolicyDao(emf);
        policyDraftDao = new TrustPolicyDraftDao(emf);
        imgUploadDao = new ImageStoreUploadDao(emf);
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#saveImageMetadata(com.intel.director.api.ImageAttributes)
     */
    public ImageAttributes saveImageMetadata(ImageAttributes img) throws DbException {
        MwImage mwImage = mapper.toData(img);
        MwImage createdImage = imgDao.createImage(mwImage);
        return mapper.toTransferObject(createdImage);
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#updateImage(com.intel.director.api.ImageAttributes)
     */
    public void updateImage(ImageAttributes img) throws DbException {
        MwImage mwImage = mapper.toData(img);
        imgDao.updateImage(mwImage);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImages(com.intel.director.api.ImageInfoFilter, com.intel.director.api.ImageInfoOrderBy)
     */
    public List<ImageInfo> fetchImages(
            ImageInfoFilter imgFilter,
            ImageInfoOrderBy orderBy) throws DbException {
        return imgDao.findMwImageEntities(imgFilter,
                orderBy);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImages(com.intel.director.api.ImageInfoFilter, com.intel.director.api.ImageInfoOrderBy, int, int)
     */
    public List<ImageInfo> fetchImages(
            ImageInfoFilter imgFilter,
            ImageInfoOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        return imgDao.findMwImageEntities(firstRecord,
                maxRecords, imgFilter, orderBy);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImageById(java.lang.String)
     */
    public ImageInfo fetchImageById(String id) throws DbException {
        return imgDao.findMwImage(id);
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#destroyImage(com.intel.director.api.ImageAttributes)
     */
    public void destroyImage(ImageAttributes img) throws DbException {
        MwImage mwImage = mapper.toData(img);
        imgDao.destroyImage(mwImage);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalImagesCount()
     */
    public int getTotalImagesCount() throws DbException {
        return imgDao.getMwImageCount();
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalImagesCount(com.intel.director.api.ImageInfoFilter)
     */
    public int getTotalImagesCount(ImageInfoFilter imgFilter) throws DbException {
        return imgDao.getMwImageCount(imgFilter);
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImages(com.intel.director.api.ImageInfoOrderBy)
     */
    public List<ImageInfo> fetchImages(ImageInfoOrderBy orderBy) throws DbException {
        return imgDao.findMwImageEntities(
                null, orderBy);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImages(com.intel.director.api.ImageInfoOrderBy, int, int)
     */
    public List<ImageInfo> fetchImages(ImageInfoOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        return imgDao.findMwImageEntities(
                firstRecord, maxRecords, null, orderBy);

    }

    public TrustPolicy fetchPolicyForImage(String imageId) throws DbException {
        TrustPolicyFilter trustPolicyFilter = new TrustPolicyFilter();
        trustPolicyFilter.setImage_id(imageId);
        List<MwTrustPolicy> mwTrustPolicyList = policyDao.findMwTrustPolicyEntities(trustPolicyFilter,
                null);
        return mapper.toTransferObject(mwTrustPolicyList.get(0));
    }

    public TrustPolicyDraft fetchPolicyDraftForImage(String imageId)
            throws DbException {
        TrustPolicyDraftFilter trustPolicydraftFilter = new TrustPolicyDraftFilter();
        trustPolicydraftFilter.setImage_id(imageId);
        List<MwTrustPolicyDraft> mwTrustPolicyDraftList = policyDraftDao.findMwTrustPolicyDraftEntities(trustPolicydraftFilter,
                null);
        return mapper.toTransferObject(mwTrustPolicyDraftList.get(0));
    }

    public List<ImageStoreUploadTransferObject> fetchPolicyUploadsForImage(
            String imageId) throws DbException {
        ImageStoreUploadFilter imgUploadFilter = new ImageStoreUploadFilter();
        imgUploadFilter.setImage_id(imageId);
        List<MwImageUpload> mwImageUploadList = imgUploadDao.findMwImageUploadEntities(imgUploadFilter,
                null);
        List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

        for (MwImageUpload MwImageUpload : mwImageUploadList) {
            imgUploadList.add(mapper.toTransferObject(MwImageUpload));
        }
        return imgUploadList;

    }

    /*############################################################################################################################
     ################################################### USER ############################################################
     #############################################################################################################################*/
    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#saveUser(com.intel.director.api.User)
     */
    public User saveUser(User user) throws DbException {
        MwUser mwUser = mapper.toData(user);
        MwUser createdUser = userDao.createUser(mwUser);
        return mapper.toTransferObject(createdUser);
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#updateUser(com.intel.director.api.User)
     */
    public void updateUser(User user) throws DbException {
        MwUser mwUser = mapper.toData(user);
        userDao.updateUser(mwUser);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchUsers(com.intel.director.api.UserFilter, com.intel.director.api.UserOrderBy)
     */
    public List<User> fetchUsers(
            UserFilter userFilter,
            UserOrderBy orderBy) throws DbException {
        List<MwUser> mwuserList = userDao.findMwUserEntities(userFilter,
                orderBy);
        List<User> userList = new ArrayList<User>();

        for (MwUser mwUser : mwuserList) {
            userList.add(mapper.toTransferObject(mwUser));
        }
        return userList;
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchUsers(com.intel.director.api.UserFilter, com.intel.director.api.UserOrderBy, int, int)
     */
    public List<User> fetchUsers(
            UserFilter userFilter,
            UserOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        List<MwUser> MwuserList = userDao.findMwUserEntities(firstRecord,
                maxRecords, userFilter, orderBy);
        List<User> userList = new ArrayList<User>();

        for (MwUser mwUser : MwuserList) {
            userList.add(mapper.toTransferObject(mwUser));
        }
        return userList;

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchUserById(java.lang.String)
     */
    public User fetchUserById(String id) throws DbException {
        return mapper.toTransferObject(userDao.findMwUser(id));
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#destroyUser(com.intel.director.api.User)
     */
    public void destroyUser(User User) throws DbException {
        MwUser MwUser = mapper.toData(User);
        userDao.destroyUser(MwUser);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalUsersCount()
     */
    public int getTotalUsersCount() throws DbException {
        return userDao.getMwUserCount();
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalUsersCount(com.intel.director.api.UserFilter)
     */
    public int getTotalUsersCount(UserFilter userFilter) throws DbException {
        return userDao.getMwUserCount(userFilter);
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchUsers(com.intel.director.api.UserOrderBy)
     */
    public List<User> fetchUsers(UserOrderBy orderBy) throws DbException {
        List<MwUser> MwuserList = userDao.findMwUserEntities(
                null, orderBy);
        List<User> userList = new ArrayList<User>();

        for (MwUser mwUser : MwuserList) {
            userList.add(mapper.toTransferObject(mwUser));
        }
        return userList;
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchUsers(com.intel.director.api.UserOrderBy, int, int)
     */
    public List<User> fetchUsers(UserOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        List<MwUser> mwuserList = userDao.findMwUserEntities(
                firstRecord, maxRecords, null, orderBy);
        List<User> userList = new ArrayList<User>();

        for (MwUser mwUser : mwuserList) {
            userList.add(mapper.toTransferObject(mwUser));
        }
        return userList;
    }

    /*############################################################################################################################
     ################################################### TRUST POLICY############################################################
     #############################################################################################################################*/
    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#savePolicy(com.intel.director.api.TrustPolicy)
     */
    public TrustPolicy savePolicy(TrustPolicy trustPolicy) throws DbException {
        MwTrustPolicy mwTrustPolicy = mapper.toData(trustPolicy);

        MwTrustPolicy createdPolicy = policyDao.createTrustPolicy(mwTrustPolicy);
        return mapper.toTransferObject(createdPolicy);
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#updatePolicy(com.intel.director.api.TrustPolicy)
     */
    public void updatePolicy(TrustPolicy trustPolicy) throws DbException {
        MwTrustPolicy mwTrustPolicy = mapper.toData(trustPolicy);
        policyDao.updateTrustPolicy(mwTrustPolicy);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicies(com.intel.director.api.TrustPolicyFilter, com.intel.director.api.TrustPolicyOrderBy)
     */
    public List<TrustPolicy> fetchPolicies(
            TrustPolicyFilter trustPolicyFilter,
            TrustPolicyOrderBy orderBy) throws DbException {
        List<MwTrustPolicy> mwTrustPolicyList = policyDao.findMwTrustPolicyEntities(trustPolicyFilter,
                orderBy);
        List<TrustPolicy> trustPolicyList = new ArrayList<TrustPolicy>();

        for (MwTrustPolicy mwTP : mwTrustPolicyList) {
            trustPolicyList.add(mapper.toTransferObject(mwTP));
        }
        return trustPolicyList;
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicies(com.intel.director.api.TrustPolicyFilter, com.intel.director.api.TrustPolicyOrderBy, int, int)
     */
    public List<TrustPolicy> fetchPolicies(
            TrustPolicyFilter trustPolicyFilter,
            TrustPolicyOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        List<MwTrustPolicy> MwTrustPolicyList = policyDao.findMwTrustPolicyEntities(firstRecord,
                maxRecords, trustPolicyFilter, orderBy);
        List<TrustPolicy> trustPolicyList = new ArrayList<TrustPolicy>();

        for (MwTrustPolicy mwTP : MwTrustPolicyList) {
            trustPolicyList.add(mapper.toTransferObject(mwTP));
        }
        return trustPolicyList;

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyById(java.lang.String)
     */
    public TrustPolicy fetchPolicyById(String id) throws DbException {
        return mapper.toTransferObject(policyDao.findMwTrustPolicy(id));
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#destroyPolicy(com.intel.director.api.TrustPolicy)
     */
    public void destroyPolicy(TrustPolicy trustPolicy) throws DbException {
        MwTrustPolicy MwTrustPolicy = mapper.toData(trustPolicy);
        policyDao.destroyTrustPolicy(MwTrustPolicy);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalPoliciesCount()
     */
    public int getTotalPoliciesCount() throws DbException {
        return policyDao.getMwTrustPolicyCount();
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalPoliciesCount(com.intel.director.api.TrustPolicyFilter)
     */
    public int getTotalPoliciesCount(TrustPolicyFilter trustPolicyFilter) throws DbException {
        return policyDao.getMwTrustPolicyCount(trustPolicyFilter);
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicies(com.intel.director.api.TrustPolicyOrderBy)
     */
    public List<TrustPolicy> fetchPolicies(TrustPolicyOrderBy orderBy) throws DbException {
        List<MwTrustPolicy> MwTrustPolicyList = policyDao.findMwTrustPolicyEntities(
                null, orderBy);
        List<TrustPolicy> trustPolicyList = new ArrayList<TrustPolicy>();

        for (MwTrustPolicy mwTP : MwTrustPolicyList) {
            trustPolicyList.add(mapper.toTransferObject(mwTP));
        }
        return trustPolicyList;
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicies(com.intel.director.api.TrustPolicyOrderBy, int, int)
     */
    public List<TrustPolicy> fetchPolicies(TrustPolicyOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        List<MwTrustPolicy> MwTrustPolicyList = policyDao.findMwTrustPolicyEntities(
                firstRecord, maxRecords, null, orderBy);
        List<TrustPolicy> trustPolicyList = new ArrayList<TrustPolicy>();

        for (MwTrustPolicy mwTP : MwTrustPolicyList) {
            trustPolicyList.add(mapper.toTransferObject(mwTP));
        }
        return trustPolicyList;
    }

    /*############################################################################################################################
     ################################################### TRUST POLICY DRAFT############################################################
     #############################################################################################################################*/
    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#savePolicyDraft(com.intel.director.api.TrustPolicyDraft)
     */
    public TrustPolicyDraft savePolicyDraft(TrustPolicyDraft trustPolicyDraft) throws DbException {
        MwTrustPolicyDraft mwTrustPolicyDraft = mapper.toData(trustPolicyDraft);
        MwTrustPolicyDraft createdPolicyDraft = policyDraftDao.createTrustPolicyDraft(mwTrustPolicyDraft);
        return mapper.toTransferObject(createdPolicyDraft);
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#updatePolicyDraft(com.intel.director.api.TrustPolicyDraft)
     */
    public void updatePolicyDraft(TrustPolicyDraft trustPolicyDraft) throws DbException {
        MwTrustPolicyDraft mwTrustPolicyDraft = mapper.toData(trustPolicyDraft);
        policyDraftDao.updateTrustPolicyDraft(mwTrustPolicyDraft);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDrafts(com.intel.director.api.TrustPolicyDraftFilter, com.intel.director.api.TrustPolicyDraftOrderBy)
     */
    public List<TrustPolicyDraft> fetchPolicyDrafts(
            TrustPolicyDraftFilter trustPolicyDraftFilter,
            TrustPolicyDraftOrderBy orderBy) throws DbException {
        List<MwTrustPolicyDraft> mwTrustPolicyDraftList = policyDraftDao.findMwTrustPolicyDraftEntities(trustPolicyDraftFilter,
                orderBy);
        List<TrustPolicyDraft> trustPolicyDraftList = new ArrayList<TrustPolicyDraft>();

        for (MwTrustPolicyDraft mwTPD : mwTrustPolicyDraftList) {
            trustPolicyDraftList.add(mapper.toTransferObject(mwTPD));
        }
        return trustPolicyDraftList;
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDrafts(com.intel.director.api.TrustPolicyDraftFilter, com.intel.director.api.TrustPolicyDraftOrderBy, int, int)
     */
    public List<TrustPolicyDraft> fetchPolicyDrafts(
            TrustPolicyDraftFilter trustPolicyDraftFilter,
            TrustPolicyDraftOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        List<MwTrustPolicyDraft> MwTrustPolicyDraftList = policyDraftDao.findMwTrustPolicyDraftEntities(firstRecord,
                maxRecords, trustPolicyDraftFilter, orderBy);
        List<TrustPolicyDraft> trustPolicyDraftList = new ArrayList<TrustPolicyDraft>();

        for (MwTrustPolicyDraft mwTPD : MwTrustPolicyDraftList) {
            trustPolicyDraftList.add(mapper.toTransferObject(mwTPD));
        }
        return trustPolicyDraftList;

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDraftById(java.lang.String)
     */
    public TrustPolicyDraft fetchPolicyDraftById(String id) throws DbException {
        return mapper.toTransferObject(policyDraftDao.findMwTrustPolicyDraft(id));
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#destroyPolicyDraft(com.intel.director.api.TrustPolicyDraft)
     */
    public void destroyPolicyDraft(TrustPolicyDraft trustPolicyDraft) throws DbException {
        MwTrustPolicyDraft MwTrustPolicyDraft = mapper.toData(trustPolicyDraft);
        policyDraftDao.destroyPolicyDraft(MwTrustPolicyDraft);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalPolicyDraftsCount()
     */
    public int getTotalPolicyDraftsCount() throws DbException {
        return policyDraftDao.getMwTrustPolicyDraftCount();
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalPolicyDraftsCount(com.intel.director.api.TrustPolicyDraftFilter)
     */
    public int getTotalPolicyDraftsCount(TrustPolicyDraftFilter trustPolicyDraftFilter) throws DbException {
        return policyDraftDao.getMwTrustPolicyDraftCount(trustPolicyDraftFilter);
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDrafts(com.intel.director.api.TrustPolicyDraftOrderBy)
     */
    public List<TrustPolicyDraft> fetchPolicyDrafts(TrustPolicyDraftOrderBy orderBy) throws DbException {
        List<MwTrustPolicyDraft> MwTrustPolicyDraftList = policyDraftDao.findMwTrustPolicyDraftEntities(
                null, orderBy);
        List<TrustPolicyDraft> trustPolicyDraftList = new ArrayList<TrustPolicyDraft>();

        for (MwTrustPolicyDraft mwTPD : MwTrustPolicyDraftList) {
            trustPolicyDraftList.add(mapper.toTransferObject(mwTPD));
        }
        return trustPolicyDraftList;
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchPolicyDrafts(com.intel.director.api.TrustPolicyDraftOrderBy, int, int)
     */
    public List<TrustPolicyDraft> fetchPolicyDrafts(TrustPolicyDraftOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        List<MwTrustPolicyDraft> MwTrustPolicyDraftList = policyDraftDao.findMwTrustPolicyDraftEntities(
                firstRecord, maxRecords, null, orderBy);
        List<TrustPolicyDraft> trustPolicyDraftList = new ArrayList<TrustPolicyDraft>();

        for (MwTrustPolicyDraft mwTPD : MwTrustPolicyDraftList) {
            trustPolicyDraftList.add(mapper.toTransferObject(mwTPD));
        }
        return trustPolicyDraftList;
    }

    /*############################################################################################################################
     ################################################### IMAGE UPLOADS############################################################
     #############################################################################################################################*/
    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#saveImageUpload(com.intel.director.api.ImageStoreUploadTransferObject)
     */
    public ImageStoreUploadTransferObject saveImageUpload(ImageStoreUploadTransferObject imgUpload) throws DbException {
        MwImageUpload MwImageUpload = mapper.toData(imgUpload);
        MwImageUpload createdImageUpload = imgUploadDao.createImageUpload(MwImageUpload);
        return mapper.toTransferObject(createdImageUpload);
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#updateImageUpload(com.intel.director.api.ImageStoreUploadTransferObject)
     */
    public void updateImageUpload(ImageStoreUploadTransferObject imgUpload) throws DbException {
        MwImageUpload MwImageUpload = mapper.toData(imgUpload);
        imgUploadDao.updateImageUpload(MwImageUpload);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploads(com.intel.director.api.ImageStoreUploadFilter, com.intel.director.api.ImageStoreUploadOrderBy)
     */
    public List<ImageStoreUploadTransferObject> fetchImageUploads(
            ImageStoreUploadFilter imgUploadFilter,
            ImageStoreUploadOrderBy orderBy) throws DbException {
        List<MwImageUpload> mwImageUploadList = imgUploadDao.findMwImageUploadEntities(imgUploadFilter,
                orderBy);
        List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

        for (MwImageUpload MwImageUpload : mwImageUploadList) {
            imgUploadList.add(mapper.toTransferObject(MwImageUpload));
        }
        return imgUploadList;
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploads(com.intel.director.api.ImageStoreUploadFilter, com.intel.director.api.ImageStoreUploadOrderBy, int, int)
     */
    public List<ImageStoreUploadTransferObject> fetchImageUploads(
            ImageStoreUploadFilter imgUploadFilter,
            ImageStoreUploadOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        List<MwImageUpload> mwImageUploadList = imgUploadDao.findMwImageUploadEntities(firstRecord,
                maxRecords, imgUploadFilter, orderBy);
        List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

        for (MwImageUpload MwImageUpload : mwImageUploadList) {
            imgUploadList.add(mapper.toTransferObject(MwImageUpload));
        }
        return imgUploadList;

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploadById(java.lang.String)
     */
    public ImageStoreUploadTransferObject fetchImageUploadById(String id) throws DbException {
        return mapper.toTransferObject(imgUploadDao.findMwImageUpload(id));
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#destroyImageUpload(com.intel.director.api.ImageStoreUploadTransferObject)
     */
    public void destroyImageUpload(ImageStoreUploadTransferObject ImageStoreUploadTransferObject) throws DbException {
        MwImageUpload MwImageUpload = mapper.toData(ImageStoreUploadTransferObject);
        imgUploadDao.destroyImageUpload(MwImageUpload);

    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalImageUploadsCount()
     */
    public int getTotalImageUploadsCount() throws DbException {
        return imgUploadDao.getMwImageUploadCount();
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#getTotalImageUploadsCount(com.intel.director.api.ImageStoreUploadFilter)
     */
    public int getTotalImageUploadsCount(ImageStoreUploadFilter imgUploadFilter) throws DbException {
        return imgUploadDao.getMwImageUploadCount(imgUploadFilter);
    }


    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploads(com.intel.director.api.ImageStoreUploadOrderBy)
     */
    public List<ImageStoreUploadTransferObject> fetchImageUploads(ImageStoreUploadOrderBy orderBy) throws DbException {
        List<MwImageUpload> mwImageUploadList = imgUploadDao.findMwImageUploadEntities(
                null, orderBy);
        List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

        for (MwImageUpload MwImageUpload : mwImageUploadList) {
            imgUploadList.add(mapper.toTransferObject(MwImageUpload));
        }
        return imgUploadList;
    }

    /* (non-Javadoc)
     * @see com.intel.mtwilson.director.dbservice.IPersistService#fetchImageUploads(com.intel.director.api.ImageStoreUploadOrderBy, int, int)
     */
    public List<ImageStoreUploadTransferObject> fetchImageUploads(ImageStoreUploadOrderBy orderBy, int firstRecord, int maxRecords) throws DbException {
        List<MwImageUpload> mwImageUploadList = imgUploadDao.findMwImageUploadEntities(
                firstRecord, maxRecords, null, orderBy);
        List<ImageStoreUploadTransferObject> imgUploadList = new ArrayList<ImageStoreUploadTransferObject>();

        for (MwImageUpload mwImageStoreUploadTransferObject : mwImageUploadList) {
            imgUploadList.add(mapper.toTransferObject(mwImageStoreUploadTransferObject));
        }
        return imgUploadList;
    }

}
