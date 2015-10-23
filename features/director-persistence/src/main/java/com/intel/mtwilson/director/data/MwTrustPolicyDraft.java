package com.intel.mtwilson.director.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;



@Entity
@Table(name = "MW_TRUST_POLICY_DRAFT")
public class MwTrustPolicyDraft  extends MwAuditable{
		
		
		
    	@OneToOne(mappedBy="trustPolicyDraft",optional = false)
	    private MwImage image;
	    
	    @Column(name = "TRUST_POLICY_DRAFT")
		private  Character[] trustPolicyDraft;

		
		@Column(name = "NAME")
		private String name;
		
		 @Column(name = "DISPLAY_NAME")
		 private String display_name;
	    
		public String getDisplay_name() {
			return display_name;
		}


		public void setDisplay_name(String display_name) {
			this.display_name = display_name;
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




		public Character[] getTrustPolicyDraft() {
			return trustPolicyDraft;
		}


		public void setTrustPolicyDraft(Character[] trustPolicyDraft) {
			this.trustPolicyDraft = trustPolicyDraft;
		}


		public String getName() {
			return name;
		}


		public void setName(String name) {
			this.name = name;
		}

		
}
