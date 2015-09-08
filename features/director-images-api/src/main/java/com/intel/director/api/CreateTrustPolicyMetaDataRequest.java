package com.intel.director.api;

public class CreateTrustPolicyMetaDataRequest {

	public String imageid;
	
	public String launch_control_policy;
	
	public String asset_tag_policy;
	
	public String selected_image_format;
	

	public Boolean isEncrypted;

    public String image_name;
    


	public String getImageid() {
		return imageid;
	}

	public void setImageid(String imageid) {
		this.imageid = imageid;
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



	public Boolean getIsEncrypted() {
		return isEncrypted;
	}

	public void setIsEncrypted(Boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}

	public String getImage_name() {
		return image_name;
	}

	public void setImage_name(String image_name) {
		this.image_name = image_name;
	}


	
	
	
	
	
}
