package com.intel.mtwilson.director.data;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;



@Entity
@Table(name = "MW_SSH_KEY")
public class MwSshKey {
		
		@Id
		@UuidGenerator(name="UUID")
		@GeneratedValue(generator="UUID")
	  	@Column(name = "ID" , length = 36)
		private String id;
		
		@Column(name = "SSH_KEY")
		private char[] sshKey;
		
		@OneToMany(mappedBy = "sshKey")
		private Collection<MwHost> tblMwHostCollection;

		public MwSshKey(char[] sshKey, Collection<MwHost> tblMwHostCollection) {
			super();
			this.sshKey = sshKey;
			this.tblMwHostCollection = tblMwHostCollection;
		}

		public MwSshKey() {
			super();
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public char[] getSshKey() {
			return sshKey;
		}

		public void setSshKey(char[] sshKey) {
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
			return "MwSshKey [id=" + id + ", sshKey=" + Arrays.toString(sshKey)
					+ ", tblMwHostCollection=" + tblMwHostCollection + "]";
		}
		
		
}
