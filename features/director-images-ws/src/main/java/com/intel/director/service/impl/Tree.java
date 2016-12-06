package com.intel.director.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.ui.TreeElement;
import com.intel.director.common.exception.DirectorException;

public class Tree {

	TreeNode root;
	public boolean checked = false;
	public List<String> treeElementsHtml = null;

	public boolean explodedView = false;
	public Set<String> dirPathsForEdit = null;
	public String mountPath = null;
	public Set<TreeNodeDetail> directoryListContainingRegex = null;
	public Set<String> fileNames = null;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Tree.class);
	public void setDirPathsForEdit(Set<String> dirPathsForEdit) {
		this.dirPathsForEdit = dirPathsForEdit;
	}

	public Set<String> trustPolicyElementsList = null;

	public Tree() {
		super();
	}

	public Tree(SearchFilesInImageRequest searchFilesInImageRequest,
			String mountPath, Set<TreeNodeDetail> directoryListContainingRegex,
			Set<String> trustPolicyElementsList, Set<String> dirPathsForEdit,
			Set<String> fileNames) {

		this.mountPath = mountPath;
		this.directoryListContainingRegex = directoryListContainingRegex;
		this.trustPolicyElementsList = trustPolicyElementsList;
		this.dirPathsForEdit = dirPathsForEdit;
		this.fileNames = fileNames;
		String actionDir = searchFilesInImageRequest.getDir();
		String pattern = File.separator;
		String[] list = actionDir.split(pattern);
		if (list.length > 1) {
			root = new TreeNode(list[list.length - 1], actionDir, mountPath);
		} else {
			root = new TreeNode("/", actionDir, mountPath);
		}
		///rootDirWithRegex will be there for create mode if either include or exclude flag are set , null for edit mode
	
		if (!searchFilesInImageRequest.reset_regex && (searchFilesInImageRequest.include != null
				|| searchFilesInImageRequest.exclude != null)) {
			TreeNodeDetail detail = new TreeNodeDetail();
			detail.regexPath = actionDir;
			detail.regexExclude = searchFilesInImageRequest.exclude;
			detail.regexInclude = searchFilesInImageRequest.include;
			detail.isRegexRecursive = searchFilesInImageRequest.include_recursive;
			root.rootDirWithRegex = detail;
		}
		root.parent = this;

		if (searchFilesInImageRequest.recursive) {
			explodedView = true;
		}

		if (searchFilesInImageRequest.files_for_policy) {
			checked = true;
		}
		treeElementsHtml = new ArrayList<String>();
	}

	public TreeElement createTree()  {
		for (String data : fileNames) {

			String pattern = File.separator;
			String[] list = data.split(pattern);
			
			root.createNode(root.incrementalPath, list);
			
		}

		return root.generateTree();
	}

}