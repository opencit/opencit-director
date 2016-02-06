package com.intel.mtwilson.director.data;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Cacheable;

import org.eclipse.persistence.annotations.UuidGenerator;


@Entity
@Cacheable(false)
@Table(name = "MW_TRUST_POLICY")
public class MwTrustPolicy extends MwAuditable{
		

		
	
	    @OneToOne(mappedBy="trustPolicy",optional = false)
	    public MwImage image;

	    
		@OneToMany(mappedBy = "trustPolicy")
		private Collection<MwPolicyUpload> tblMwPolicyUploadCollection;
		
		
		@ManyToOne
		@JoinColumn(name = "HOST_ID", referencedColumnName = "ID")
		private MwHost host;
		
		
		
		@Column(name = "NAME")
		private String name;
		
		 @Column(name = "TRUST_POLICY")
		private  Character[] trustPolicy;
		 
		 @Column(name = "DESCRIPTION")
		 private String description;

		 @Column(name = "DISPLAY_NAME")
		 private String display_name;
		
		public MwTrustPolicy(){
			super();
		}

	

		public String getDisplay_name() {
			return display_name;
		}



		public void setDisplay_name(String display_name) {
			this.display_name = display_name;
		}



		public MwImage getImage() {
			return image;
		}

		public void setImage(MwImage image) {
			this.image = image;
		}


		public Collection<MwPolicyUpload> getTblMwPolicyUploadCollection() {
			return tblMwPolicyUploadCollection;
		}


		public void setTblMwPolicyUploadCollection(
				Collection<MwPolicyUpload> tblMwPolicyUploadCollection) {
			this.tblMwPolicyUploadCollection = tblMwPolicyUploadCollection;
		}



		public MwHost getHost() {
			return host;
		}



		public void setHost(MwHost host) {
			this.host = host;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Character[] getTrustPolicy() {
			return trustPolicy;
		}

		public void setTrustPolicy(Character[] trustPolicy) {
			this.trustPolicy = trustPolicy;
		}



		public String getDescription() {
			return description;
		}



		public void setDescription(String description) {
			this.description = description;
		}




		
		
}
