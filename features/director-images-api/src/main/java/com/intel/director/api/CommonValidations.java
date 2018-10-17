package com.intel.director.api;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.ValidationUtil;

public class CommonValidations {

	public static boolean validateImageDeployments(String image_deployments) {
		if(StringUtils.isBlank(image_deployments)){
			return true;
		}
		return ValidationUtil.isValidWithRegex(image_deployments, "BareMetal|VM|Docker") ;			
	}
	
	
}
