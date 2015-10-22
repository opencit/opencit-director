package com.intel.mtwilson.director.data;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table(name = "MW_HOST")
public class MwHost extends MwAuditable {

	@UuidGenerator(name = "UUID")
	@GeneratedValue(generator = "UUID")
	@Column(name = "ID", length = 36)
	private String id;

	@Column(name = "NAME", length = 100)
	private String name;

	@Column(name = "IP_ADDRESS", length = 15)
	private String ipAdsress;

	@Column(name = "USERNAME", length = 50)
	private String username;

	@ManyToOne(optional = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "SSH_KEY_ID", referencedColumnName = "ID")
	private MwSshKey sshKey;

	@OneToOne(optional = false, cascade = CascadeType.ALL)
	@JoinColumn(name = "SSH_PASSWORD_ID", referencedColumnName = "ID")
	private MwSshPassword sshPassword;

	@OneToOne(optional = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "IMAGE_ID", referencedColumnName = "ID")
	private MwImage imageId;

	public MwImage getImageId() {
		return imageId;
	}

	public void setImageId(MwImage imageId) {
		this.imageId = imageId;
	}

	@OneToMany(mappedBy = "host")
	private Collection<MwTrustPolicy> tblMwTrustPolicyCollection;

	public MwHost() {
		super();
	}

	public MwHost(String name, String ipAdsress, String username,
			MwSshKey sshKey, MwSshPassword sshPassword,
			Collection<MwTrustPolicy> tblMwTrustPolicyCollection) {
		super();
		this.name = name;
		this.ipAdsress = ipAdsress;
		this.username = username;
		this.sshKey = sshKey;
		this.sshPassword = sshPassword;
		this.tblMwTrustPolicyCollection = tblMwTrustPolicyCollection;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIpAdsress() {
		return ipAdsress;
	}

	public void setIpAdsress(String ipAdsress) {
		this.ipAdsress = ipAdsress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public MwSshKey getSshKey() {
		return sshKey;
	}

	public void setSshKey(MwSshKey sshKey) {
		this.sshKey = sshKey;
	}

	public MwSshPassword getSshPassword() {
		return sshPassword;
	}

	public void setSshPassword(MwSshPassword sshPassword) {
		this.sshPassword = sshPassword;
	}

	public Collection<MwTrustPolicy> getTblMwTrustPolicyCollection() {
		return tblMwTrustPolicyCollection;
	}

	public void setTblMwTrustPolicyCollection(
			Collection<MwTrustPolicy> tblMwTrustPolicyCollection) {
		this.tblMwTrustPolicyCollection = tblMwTrustPolicyCollection;
	}

	@Override
	public String toString() {
		return "MwHost [id=" + id + ", name=" + name + ", ipAdsress="
				+ ipAdsress + ", username=" + username + ", sshKey=" + sshKey
				+ ", sshPassword=" + sshPassword + ", imageId=" + imageId
				+ ", tblMwTrustPolicyCollection=" + tblMwTrustPolicyCollection
				+ "]";
	}

}