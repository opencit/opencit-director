package com.intel.director.api;

import java.util.Calendar;

public class PolicyUploadTransferObject {

    protected String id;

    protected String policy_uri;
    protected Calendar date;

    protected String status;

    protected String uploaded_by_user_id;
    
    protected TrustPolicy trust_policy;
    
    protected String storeId;	// required to set and send it for db Create,Update
    
    protected String storeName; // Not required to send it for db Create,Update operation
    
    protected String uploadVariableMD5;
 
    protected String storeArtifactId;
    
    protected boolean isDeleted;
    
 
	public String getUploadVariableMD5() {
		return uploadVariableMD5;
	}
	public void setUploadVariableMD5(String uploadVariableMD5) {
		this.uploadVariableMD5 = uploadVariableMD5;
	}
	public String getStoreArtifactId() {
		return storeArtifactId;
	}
	public void setStoreArtifactId(String storeArtifactId) {
		this.storeArtifactId = storeArtifactId;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	public String getStoreId() {
		return storeId;
	}
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPolicy_uri() {
		return policy_uri;
	}
	public void setPolicy_uri(String policy_uri) {
		this.policy_uri = policy_uri;
	}
	public Calendar getDate() {
		return date;
	}
	public void setDate(Calendar date) {
		this.date = date;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUploaded_by_user_id() {
		return uploaded_by_user_id;
	}
	public void setUploaded_by_user_id(String uploaded_by_user_id) {
		this.uploaded_by_user_id = uploaded_by_user_id;
	}
	public TrustPolicy getTrust_policy() {
		return trust_policy;
	}
	public void setTrust_policy(TrustPolicy trust_policy) {
		this.trust_policy = trust_policy;
	}
	@Override
	public String toString() {
		return "PolicyUploadTransferObject [id=" + id + ", policy_uri="
				+ policy_uri + ", date=" + date + ", status=" + status
				+ ", uploaded_by_user_id=" + uploaded_by_user_id
				+ ", trust_policy=" + trust_policy + ", storeId=" + storeId
				+ ", storeName=" + storeName + "]";
	}

    

	
}
