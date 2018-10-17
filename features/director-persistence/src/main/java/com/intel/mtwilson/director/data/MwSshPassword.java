package com.intel.mtwilson.director.data;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;



@Entity
@Table(name = "MW_SSH_PASSWORD")
public class MwSshPassword {
		
		@Id
		@UuidGenerator(name="UUID")
		@GeneratedValue(generator="UUID")
	  	@Column(name = "ID" , length = 36)
		private String id;
		
		@Column(name = "SSH_KEY")
		private Character[] sshKey;
		
		
		public MwSshPassword(){
			super();	
		}

		public MwSshPassword(Character[] sshKey) {
			super();
			this.sshKey = sshKey;
			
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

	

		public Character[] getSshKey() {
			return sshKey;
		}

		public void setSshKey(Character[] sshKey) {
			this.sshKey = sshKey;
		}

		
		@Override
		public String toString() {
			return "MwSshPassword [id=" + id + ", sshKey="
					+ Arrays.toString(sshKey)+"]";
		}
		
		
}
