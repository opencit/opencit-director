/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import com.intel.dcsg.cpg.validation.RegexPatterns;

import com.intel.dcsg.cpg.validation.ValidationUtil;
/**
 *
 * @author soakx
 */
public class UpdateTrustPolicyRequest {

	public String display_name;
	public String image_id;

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getImage_id() {
		return image_id;
	}

	public void setImage_id(String image_id) {
		this.image_id = image_id;
	}

	public String validate(String trustPolicyId) {
		String NAME_REGEX = "[a-zA-Z0-9,;. @_-]+";
		List<String> errors = new ArrayList<>();
		if(!ValidationUtil.isValidWithRegex(trustPolicyId,RegexPatterns.UUID)){
			errors.add("Trust policy ID is not in a UUID format");
		}
		
		if (!ValidationUtil.isValidWithRegex(getDisplay_name(), NAME_REGEX)) {
			errors.add("Display name is empty or improper format. Name can contain only numbers, alphabets, no spaces and following characters ,;.@_-");
		}
    	return StringUtils.join(errors, ",");
	}

}