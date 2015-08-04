package com.intel.mtwilson.director.dbservice;

import java.util.List;

import com.intel.director.api.ImageAttributes;
import com.intel.director.api.ImageAttributesFilter;
import com.intel.director.api.ImageAttributesOrderBy;
import com.intel.director.api.ImageStoreUploadFilter;
import com.intel.director.api.ImageStoreUploadOrderBy;
import com.intel.director.api.ImageStoreUploadTransferObject;
import com.intel.director.api.TrustPolicy;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftFilter;
import com.intel.director.api.TrustPolicyDraftOrderBy;
import com.intel.director.api.TrustPolicyFilter;
import com.intel.director.api.TrustPolicyOrderBy;
import com.intel.director.api.User;
import com.intel.director.api.UserFilter;
import com.intel.director.api.UserOrderBy;
import com.intel.mtwilson.director.db.exception.DbException;

/**
 * @author gs-0835
 *
 */
public interface IPersistService {

    public abstract ImageAttributes saveImageMetadata(ImageAttributes img)
            throws DbException;

    public abstract void updateImage(ImageAttributes img) throws DbException;

    public abstract List<ImageAttributes> fetchImages(
            ImageAttributesFilter imgFilter, ImageAttributesOrderBy orderBy)
            throws DbException;

    public abstract List<ImageAttributes> fetchImages(
            ImageAttributesFilter imgFilter, ImageAttributesOrderBy orderBy,
            int firstRecord, int maxRecords) throws DbException;

    public abstract ImageAttributes fetchImageById(String id)
            throws DbException;

    public abstract void destroyImage(ImageAttributes img) throws DbException;

    public abstract int getTotalImagesCount() throws DbException;

    public abstract int getTotalImagesCount(ImageAttributesFilter imgFilter)
            throws DbException;

    public abstract List<ImageAttributes> fetchImages(
            ImageAttributesOrderBy orderBy) throws DbException;

    public abstract List<ImageAttributes> fetchImages(
            ImageAttributesOrderBy orderBy, int firstRecord, int maxRecords)
            throws DbException;

    public abstract TrustPolicy fetchPolicyForImage(String imageId)
            throws DbException;

    public abstract TrustPolicyDraft fetchPolicyDraftForImage(String imageId)
            throws DbException;

    public abstract List<ImageStoreUploadTransferObject> fetchPolicyUploadsForImage(
            String imageId) throws DbException;

    public abstract User saveUser(User user) throws DbException;

    public abstract void updateUser(User user) throws DbException;

    public abstract List<User> fetchUsers(UserFilter userFilter,
            UserOrderBy orderBy) throws DbException;

    public abstract List<User> fetchUsers(UserFilter userFilter,
            UserOrderBy orderBy, int firstRecord, int maxRecords)
            throws DbException;

    public abstract User fetchUserById(String id) throws DbException;

    public abstract void destroyUser(User User) throws DbException;

    public abstract int getTotalUsersCount() throws DbException;

    public abstract int getTotalUsersCount(UserFilter userFilter)
            throws DbException;

    public abstract List<User> fetchUsers(UserOrderBy orderBy)
            throws DbException;

    public abstract List<User> fetchUsers(UserOrderBy orderBy, int firstRecord,
            int maxRecords) throws DbException;

    public abstract TrustPolicy savePolicy(TrustPolicy trustPolicy)
            throws DbException;

    public abstract void updatePolicy(TrustPolicy trustPolicy)
            throws DbException;

    public abstract List<TrustPolicy> fetchPolicies(
            TrustPolicyFilter trustPolicyFilter, TrustPolicyOrderBy orderBy)
            throws DbException;

    public abstract List<TrustPolicy> fetchPolicies(
            TrustPolicyFilter trustPolicyFilter, TrustPolicyOrderBy orderBy,
            int firstRecord, int maxRecords) throws DbException;

    public abstract TrustPolicy fetchPolicyById(String id) throws DbException;

    public abstract void destroyPolicy(TrustPolicy trustPolicy)
            throws DbException;

    public abstract int getTotalPoliciesCount() throws DbException;

    public abstract int getTotalPoliciesCount(
            TrustPolicyFilter trustPolicyFilter) throws DbException;

    public abstract List<TrustPolicy> fetchPolicies(TrustPolicyOrderBy orderBy)
            throws DbException;

    public abstract List<TrustPolicy> fetchPolicies(TrustPolicyOrderBy orderBy,
            int firstRecord, int maxRecords) throws DbException;

    public abstract TrustPolicyDraft savePolicyDraft(
            TrustPolicyDraft trustPolicyDraft) throws DbException;

    public abstract void updatePolicyDraft(TrustPolicyDraft trustPolicyDraft)
            throws DbException;

    public abstract List<TrustPolicyDraft> fetchPolicyDrafts(
            TrustPolicyDraftFilter trustPolicyDraftFilter,
            TrustPolicyDraftOrderBy orderBy) throws DbException;

    public abstract List<TrustPolicyDraft> fetchPolicyDrafts(
            TrustPolicyDraftFilter trustPolicyDraftFilter,
            TrustPolicyDraftOrderBy orderBy, int firstRecord, int maxRecords)
            throws DbException;

    public abstract TrustPolicyDraft fetchPolicyDraftById(String id)
            throws DbException;

    public abstract void destroyPolicyDraft(TrustPolicyDraft trustPolicyDraft)
            throws DbException;

    public abstract int getTotalPolicyDraftsCount() throws DbException;

    public abstract int getTotalPolicyDraftsCount(
            TrustPolicyDraftFilter trustPolicyDraftFilter) throws DbException;

    public abstract List<TrustPolicyDraft> fetchPolicyDrafts(
            TrustPolicyDraftOrderBy orderBy) throws DbException;

    public abstract List<TrustPolicyDraft> fetchPolicyDrafts(
            TrustPolicyDraftOrderBy orderBy, int firstRecord, int maxRecords)
            throws DbException;

    public abstract ImageStoreUploadTransferObject saveImageUpload(
            ImageStoreUploadTransferObject imgUpload) throws DbException;

    public abstract void updateImageUpload(
            ImageStoreUploadTransferObject imgUpload) throws DbException;

    public abstract List<ImageStoreUploadTransferObject> fetchImageUploads(
            ImageStoreUploadFilter imgUploadFilter,
            ImageStoreUploadOrderBy orderBy) throws DbException;

    public abstract List<ImageStoreUploadTransferObject> fetchImageUploads(
            ImageStoreUploadFilter imgUploadFilter,
            ImageStoreUploadOrderBy orderBy, int firstRecord, int maxRecords)
            throws DbException;

    public abstract ImageStoreUploadTransferObject fetchImageUploadById(
            String id) throws DbException;

    public abstract void destroyImageUpload(
            ImageStoreUploadTransferObject ImageStoreUploadTransferObject)
            throws DbException;

    public abstract int getTotalImageUploadsCount() throws DbException;

    public abstract int getTotalImageUploadsCount(
            ImageStoreUploadFilter imgUploadFilter) throws DbException;

    public abstract List<ImageStoreUploadTransferObject> fetchImageUploads(
            ImageStoreUploadOrderBy orderBy) throws DbException;

    public abstract List<ImageStoreUploadTransferObject> fetchImageUploads(
            ImageStoreUploadOrderBy orderBy, int firstRecord, int maxRecords)
            throws DbException;

}
