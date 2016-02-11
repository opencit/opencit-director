/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;

/**
 * 
 * @author soakx
 */
public class MountImageRequest {

	public String id;

	public String validate() {
		String error = null;
		if (!ValidationUtil.isValidWithRegex(id, RegexPatterns.UUID)) {
			error = "Image Id is empty or is not in uuid format";
		}
		return error;
	}
}
