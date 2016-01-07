package com.intel.mtwilson.director.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table(name = "MW_POLICY_TEMPLATE")
public class MwPolicyTemplate {
	@Id
	@UuidGenerator(name="UUID")
	@GeneratedValue(generator="UUID")
  	@Column(name = "ID", length = 36)
	private String id;
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "DEPLOYMENT_TYPE")
	private String deployment_type;
	
	@Column(name = "POLICY_TYPE")
	private String policy_type;
	
	@Column(name = "CONTENT")
	private  Character[] content;

	@Column(name = "ACTIVE")
	public boolean 	active;
	
	
	@Column(name = "DEPLOYMENT_TYPE_IDENTIFIER")
	private String deployment_type_identifier;



	public String getDeployment_type_identifier() {
		return deployment_type_identifier;
	}


	public void setDeployment_type_identifier(String deployment_type_identifier) {
		this.deployment_type_identifier = deployment_type_identifier;
	}


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
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


	public String getDeployment_type() {
		return deployment_type;
	}


	public void setDeployment_type(String deployment_type) {
		this.deployment_type = deployment_type;
	}


	public String getPolicy_type() {
		return policy_type;
	}


	public void setPolicy_type(String policy_type) {
		this.policy_type = policy_type;
	}


	public Character[] getContent() {
		return content;
	}


	public void setContent(Character[] content) {
		this.content = content;
	}






	

	

}
