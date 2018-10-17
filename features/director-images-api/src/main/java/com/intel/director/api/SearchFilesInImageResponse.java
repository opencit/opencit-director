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
import com.intel.director.api.ui.TreeElement;

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
	public TreeElement generatedTree= new TreeElement();
	
	public String error;
	public List<String> getPatchXml() {
		return patchXml;
	}
	public void setPatchXml(List<String> patchXml) {
		this.patchXml = patchXml;
	}
	public String getTreeContent() {
		return treeContent;
	}
	public void setTreeContent(String treeContent) {
		this.treeContent = treeContent;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public void setFiles(List<String> files) {
		this.files = files;
	}
	public TreeElement getGeneratedTree() {
		return generatedTree;
	}
	public void setGeneratedTree(TreeElement generatedTree) {
		this.generatedTree = generatedTree;
	}
	
	
}
