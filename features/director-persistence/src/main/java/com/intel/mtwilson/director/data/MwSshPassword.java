package com.intel.mtwilson.director.data;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.OneToMany;

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
		
		@OneToMany(mappedBy = "sshPassword")
		private Collection<MwHost> tblMwHostCollection;
		
		public MwSshPassword(){
			super();	
		}

		public MwSshPassword(Character[] sshKey,
				Collection<MwHost> tblMwHostCollection) {
			super();
			this.sshKey = sshKey;
			this.tblMwHostCollection = tblMwHostCollection;
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

		public Collection<MwHost> getTblMwHostCollection() {
			return tblMwHostCollection;
		}

		public void setTblMwHostCollection(Collection<MwHost> tblMwHostCollection) {
			this.tblMwHostCollection = tblMwHostCollection;
		}

		@Override
		public String toString() {
			return "MwSshPassword [id=" + id + ", sshKey="
					+ Arrays.toString(sshKey) + ", tblMwHostCollection="
					+ tblMwHostCollection + "]";
		}
		
		
}
