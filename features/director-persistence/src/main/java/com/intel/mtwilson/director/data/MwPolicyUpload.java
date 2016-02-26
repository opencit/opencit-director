package com.intel.mtwilson.director.data;



import java.sql.Date;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;



@Entity
@Table(name = "MW_POLICY_UPLOAD")
public class MwPolicyUpload {
		
		@Id
		@UuidGenerator(name="UUID")
		@GeneratedValue(generator="UUID")
	  	@Column(name = "ID", length = 36)
		private String id;
		
		@ManyToOne(optional = false)
		@JoinColumn(name = "POLICY_ID", referencedColumnName = "ID")
		private MwTrustPolicy trustPolicy;
		
		@Column(name = "DATE")
		private Date date;
		
		@Column(name = "POLICY_URI")
		private Character[] policyUri;
		
		@Column(name = "STATUS", length = 20)
		private String status;
		
		@ManyToOne(optional = false)
		@JoinColumn(name = "STORE_ID", referencedColumnName = "id")
		private MwImageStore store;
		
		@Column(name = "STORE_ARTIFACT_ID", length = 36)
		private String storeArtifactId;
		
		@Column(name = "IS_DELETED")
		private boolean isDeleted; 
		
		@Column(name = "UPLOAD_VARIABLES_MD5", length = 32)
		private String uploadVariablesMd5;
		
		public MwPolicyUpload(){
			super();
		}
		
		
	

		public MwPolicyUpload(String id, MwTrustPolicy trustPolicy, Date date,
				Character[] policyUri, String status, MwImageStore store,
				String storeArtifactId, boolean isDeleted) {
			super();
			this.id = id;
			this.trustPolicy = trustPolicy;
			this.date = date;
			this.policyUri = policyUri;
			this.status = status;
			this.store = store;
			this.storeArtifactId = storeArtifactId;
			this.isDeleted = isDeleted;
		}




		public String getUploadVariablesMd5() {
			return uploadVariablesMd5;
		}




		public void setUploadVariablesMd5(String uploadVariablesMd5) {
			this.uploadVariablesMd5 = uploadVariablesMd5;
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




		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public MwTrustPolicy getTrustPolicy() {
			return trustPolicy;
		}

		public void setTrustPolicy(MwTrustPolicy trustPolicy) {
			this.trustPolicy = trustPolicy;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

	

		public Character[] getPolicyUri() {
			return policyUri;
		}


		public void setPolicyUri(Character[] policyUri) {
			this.policyUri = policyUri;
		}


		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		
		

		public MwImageStore getStore() {
			return store;
		}


		public void setStore(MwImageStore store) {
			this.store = store;
		}


		@Override
		public String toString() {
			return "MwPolicyUpload [id=" + id + ", trustPolicy=" + trustPolicy
					+ ", date=" + date + ", policyUri="
					+ Arrays.toString(policyUri) + ", status=" + status + "]";
		}
		
		
}
