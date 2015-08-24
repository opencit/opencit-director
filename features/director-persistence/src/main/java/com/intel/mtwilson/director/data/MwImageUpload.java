package com.intel.mtwilson.director.data;



import java.sql.Date;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

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
		
		@Column(name = "IS_TARBALL_UPLOAD")
		private boolean isTarballUpload;
		
		@Column(name = "CONTENT_LENGTH")
		public Integer contentlength;
		
		@Column(name = "SENT")
		public Integer sent;


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

		public boolean isTarballUpload() {
			return isTarballUpload;
		}

		public void setTarballUpload(boolean isTarballUpload) {
			this.isTarballUpload = isTarballUpload;
		}

		public Integer getContentlength() {
			return contentlength;
		}

		public void setContentlength(Integer contentlength) {
			this.contentlength = contentlength;
		}

		public Integer getSent() {
			return sent;
		}

		public void setSent(Integer sent) {
			this.sent = sent;
		}

		
		

	}
