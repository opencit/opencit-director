package com.intel.director.api;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;

public class TrustPolicyDraftEditRequest {
	public String imageId ;
	public String trust_policy_draft_id;	
	public String patch;
	
	public String validate(){
		String error= null;
	
		
		if(!ValidationUtil.isValidWithRegex(trust_policy_draft_id,RegexPatterns.UUID)){
			error = "Trust Policy Draft Id is empty or not in uuid format";
			return error;
		}
		if(StringUtils.isBlank(patch)){
			error = "Patch cannot be empty";
			
		}
		return error;
	}
}
