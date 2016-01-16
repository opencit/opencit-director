package com.intel.director.api;

public class TrustPolicyResponse extends TrustPolicy {
	
	public boolean encrypted;
	public String image_launch_policy;
	
	
	public boolean isEncrypted() {
		return encrypted;
	}
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	public String getImage_launch_policy() {
		return image_launch_policy;
	}
	public void setImage_launch_policy(String image_launch_policy) {
		this.image_launch_policy = image_launch_policy;
	}
	

}
