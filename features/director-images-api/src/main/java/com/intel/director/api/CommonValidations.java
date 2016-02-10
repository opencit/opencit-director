package com.intel.director.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class CommonValidations {

	public static boolean validateImageDeployments(String image_deployments) {
		boolean isValid = true;
		List<String> validDeploymentTypes = new ArrayList<>(2);
		validDeploymentTypes.add("Docker");
		validDeploymentTypes.add("VM");
		if (!(StringUtils.isNotBlank(image_deployments) && validDeploymentTypes
				.contains(image_deployments))) {
			isValid=false;
		}
		return isValid;
	}
	
	
}
