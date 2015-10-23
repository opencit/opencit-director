package com.intel.director.api;

public class CreateTrustPolicyMetaDataRequest {

	public String imageid;
	
	public String hostid;
	
	public String launch_control_policy;
	
	public String asset_tag_policy;
	
	public String selected_image_format;
	
	public String deployment_type;
	
public boolean encrypted=false;

    public String image_name;

    public String display_name;

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getImageid() {
		return imageid;
	}
	
	public String getHostid() {
		return hostid;
	}

	public void setImageid(String imageid) {
		this.imageid = imageid;
	}
	
	public void setHostid(String hostid) {
		this.hostid = hostid;
	}

	public String getLaunch_control_policy() {
		return launch_control_policy;
	}

	public void setLaunch_control_policy(String launch_control_policy) {
		this.launch_control_policy = launch_control_policy;
	}

	public String getAsset_tag_policy() {
		return asset_tag_policy;
	}

	public void setAsset_tag_policy(String asset_tag_policy) {
		this.asset_tag_policy = asset_tag_policy;
	}

	public String getSelected_image_format() {
		return selected_image_format;
	}

	public void setSelected_image_format(String selected_image_format) {
		this.selected_image_format = selected_image_format;
	}



	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public String getImage_name() {
		return image_name;
	}

	public void setImage_name(String image_name) {
		this.image_name = image_name;
	}

	public String getDeploymentType() {
		return deployment_type;
	}

	public void setDeploymentType(String deploymentType) {
		this.deployment_type = deploymentType;
	}
	
	
	
	
	
}
