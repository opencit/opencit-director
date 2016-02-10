/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 * @author soakx
 */
@JsonInclude(Include.NON_NULL)
public class SearchFilesInImageResponse {
	@JsonIgnore
	public List<String> files;
	@JsonIgnore
	public List<String> getFiles() {
		return files;
	}



	public List<String> patchXml = null;
	public String treeContent = null;
	public String error;
}
