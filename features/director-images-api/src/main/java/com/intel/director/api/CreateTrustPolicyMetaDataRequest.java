package com.intel.director.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.director.constants.Constants;

public class CreateTrustPolicyMetaDataRequest {

	public String image_id;

	public String hostid;

	public String launch_control_policy;

	public String asset_tag_policy;

	public String selected_image_format;

	public String deployment_type;

	public boolean encrypted = false;

	public String image_name;

	public String display_name;

	public String trust_policy_draft_id;

	public String getTrust_policy_draft_id() {
		return trust_policy_draft_id;
	}

	public void setTrust_policy_draft_id(String trust_policy_draft_id) {
		this.trust_policy_draft_id = trust_policy_draft_id;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getHostid() {
		return hostid;
	}

	public String getImage_id() {
		return image_id;
	}

	public void setImage_id(String image_id) {
		this.image_id = image_id;
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

	/*
	 * public String validate(){ String error =null;
	 * if(ValidationUtil.isValidWithRegex(image_id,RegexPatterns.UUID)){ error =
	 * "Imaged id is empty or not in uuid format"; return error; }
	 * if(ValidationUtil
	 * .isValidWithRegex(trust_policy_draft_id,RegexPatterns.UUID)){ error =
	 * "trust Policy Draft id is empty or not in uuid format";
	 * 
	 * } return error; }
	 */

	public String validate(String type) {
		String NAME_REGEX = "[a-zA-Z0-9,;.@ _-]+";
		List<String> errors = new ArrayList<>();
		if ("draft".equals(type)) {
			if (!ValidationUtil.isValidWithRegex(getDisplay_name(), NAME_REGEX)) {
				errors.add("Display name is empty or improper format");
			}
			if (StringUtils.isBlank(getLaunch_control_policy())) {
				errors.add("Launch Control Policy is empty");
			} else if (!(Constants.LAUNCH_CONTROL_POLICY_HASH_ONLY
					.equals(getLaunch_control_policy()))
					&& !(Constants.LAUNCH_CONTROL_POLICY_HASH_AND_ENFORCE
							.equals(getLaunch_control_policy()))) {
				errors.add("Incorrect launch control policy");
			}

			if (!ValidationUtil.isValidWithRegex(getImage_id(),
					RegexPatterns.UUID)) {
				errors.add("Image id is empty or not in uuid format");

			}

		} else if ("policy".equals(type)) {
			if (!ValidationUtil.isValidWithRegex(getImage_id(),
					RegexPatterns.UUID)) {
				errors.add("Image id is empty or not in uuid format");

			}
			if (!ValidationUtil.isValidWithRegex(getTrust_policy_draft_id(),
					RegexPatterns.UUID)) {
				errors.add("Trust Policy draft id is empty or not in uuid format");

			}
		}
		return StringUtils.join(errors, ",");

	}

}
