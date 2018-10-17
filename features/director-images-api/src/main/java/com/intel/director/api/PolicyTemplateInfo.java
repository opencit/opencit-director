package com.intel.director.api;

public class PolicyTemplateInfo {

	public String id;
	public String name;
	public String deployment_type;
	public String policy_type;
	public String content;
	public boolean active;
	public String deployment_type_identifier;

	public String getDeployment_type_identifier() {
		return deployment_type_identifier;
	}
	public void setDeployment_type_identifier(String deployment_type_identifier) {
		this.deployment_type_identifier = deployment_type_identifier;
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	
	
	
}
