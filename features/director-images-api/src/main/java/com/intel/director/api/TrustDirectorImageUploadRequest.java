/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.ValidationUtil;


/**
 *
 * @author soakx
 */
public class TrustDirectorImageUploadRequest extends ImageAttributes{
	public static final String NAME_REGEX = "[a-zA-Z0-9,;. @_-]+";
    public String image_file;
    public String validate(){
    	List<String> errors = new ArrayList<>();
    	if(!ValidationUtil.isValidWithRegex(image_name,NAME_REGEX)){
    		errors.add("Invalid name for image. Image can contain number, aphabets and following characters: (- _ @ . ; ,)");
    	}
    	List<String> validDeploymentTypes = new ArrayList<>(2);
    	validDeploymentTypes.add("Docker");
    	validDeploymentTypes.add("VM");
    	if(!(StringUtils.isNotBlank(image_deployments) && validDeploymentTypes.contains(image_deployments))){
    		errors.add("Invalid deployment type for image");
    	}
    	
    	if(!((StringUtils.isNotBlank(image_format) && ValidationUtil.isValidWithRegex(image_format, "qcow2|vhd(vpc)|vmdk|raw|vdi|tar")))){
    		errors.add("Invalid format for image. Valid formats are: qcow2, vhd(vpc), vmdk, raw, vdi");
    	}
    	if(!(image_size != null && (image_size > 0))){
    		errors.add("Invalid image size image");
    	}
    	return StringUtils.join(errors, ",");
    }

}
