package com.intel.mtwilson.director.data;



import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;



@Entity
@Table(name = "MW_IMAGE_UPLOAD")
public class MwImageUpload {
		
		@Id
		@UuidGenerator(name="UUID")
		@GeneratedValue(generator="UUID")
	  	@Column(name = "ID", length = 36)
		private String id;
		
		@ManyToOne(optional = false)
		@JoinColumn(name = "IMAGE_ID", referencedColumnName = "ID")
		private MwImage image;

	    
		@Column(name = "IMAGE_URI")
		private Character[] imageUri;
		
		@Column(name = "TMP_LOCATION")
		private String tmpLocation;
		
		@Column(name = "CHECKSUM")
		private String checksum;
		
		@Column(name = "STATUS", length = 20)
		private String status;
		
		@Column(name = "DATE")
		private Date date;
		
		@Column(name = "UPLOAD_VARIABLES_MD5", length = 32)
		private String uploadVariablesMd5;
		
	  	@Column(name = "POLICY_UPLOAD_ID", length = 36)
		private String policyUploadId;
	
	  	@ManyToOne(optional = false)
		@JoinColumn(name = "STORE_ID", referencedColumnName = "id")
		private MwImageStore store;
	  	
		@Column(name = "CONTENT_LENGTH")
		private Long contentlength;
		
		@Column(name = "SENT")
		private Long sent;

		@Column(name = "STORE_ARTIFACT_ID", length = 36)
		private String storeArtifactId;
		
		@Column(name = "IS_DELETED")
		private boolean isDeleted; 
		
		
		public MwImageUpload(){
			super();
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public MwImage getImage() {
			return image;
		}


		public void setImage(MwImage image) {
			this.image = image;
		}


		

		public MwImageStore getStore() {
			return store;
		}

		public void setStore(MwImageStore store) {
			this.store = store;
		}

		public String getUploadVariablesMd5() {
			return uploadVariablesMd5;
		}

		public void setUploadVariablesMd5(String uploadVariablesMd5) {
			this.uploadVariablesMd5 = uploadVariablesMd5;
		}

		public String getPolicyUploadId() {
			return policyUploadId;
		}

		public void setPolicyUploadId(String policyUploadId) {
			this.policyUploadId = policyUploadId;
		}

		public Character[] getImageUri() {
			return imageUri;
		}

		public void setImageUri(Character[] imageUri) {
			this.imageUri = imageUri;
		}

		public String getTmpLocation() {
			return tmpLocation;
		}

		public void setTmpLocation(String tmpLocation) {
			this.tmpLocation = tmpLocation;
		}

		public String getChecksum() {
			return checksum;
		}

		public void setChecksum(String checksum) {
			this.checksum = checksum;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

	
		public Long getContentlength() {
			return contentlength;
		}

		public void setContentlength(Long contentlength) {
			this.contentlength = contentlength;
		}

		public Long getSent() {
			return sent;
		}

		public void setSent(Long sent) {
			this.sent = sent;
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

		
		

	}
