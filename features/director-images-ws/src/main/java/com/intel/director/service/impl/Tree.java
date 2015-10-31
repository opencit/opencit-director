package com.intel.director.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Tree {

	TreeNode root;
	TreeNode commonRoot;
	public String directoryCollapsed = "collapsed";
	public String checked = "";
	public List<String> treeElementsHtml = null;
	public boolean explodedView = false;
	public Set<String> dirPathsForEdit = null;
	public String mountPath = null; 
	public Set<String> directoryListContainingRegex = null;
	
	
	public void setDirPathsForEdit(Set<String> dirPathsForEdit) {
		this.dirPathsForEdit = dirPathsForEdit;
	}

	public List<String> trustPolicyElementsList = null;
	public Tree(TreeNode root, boolean recursive, boolean filesForPolicy) {
		this.root = root;
		
		commonRoot = root;
		
		if(recursive){
			directoryCollapsed = "expanded";
			explodedView = true;
		}	
		
		if(filesForPolicy){
			checked = " checked=\"true\"";
		}else{
			checked = "";
		}
		treeElementsHtml = new ArrayList<String>();
	}
	
	public void setTrustPolicyElementsList(List<String> trustPolicyElementsList){
		this.trustPolicyElementsList = trustPolicyElementsList;
	}

	
	public void addElement(String elementValue) {
		String pattern = File.separator;
		String[] list = elementValue.split(pattern);

		// latest element of the list is the filename.extrension
		root.addElement(root.incrementalPath, list);

	}

	public List<String> printTree() {
		// I move the tree common root to the current common root because I
		// don't mind about initial folder
		// that has only 1 child (and no leaf)
		List<TreeNode> nodesList = getCommonRoot();
		List<String> treeHtmlElementsList = null;
		TreeNode treeNode = nodesList.get(0) ;
		treeNode.printNode(false, false);
		
		return treeHtmlElementsList;
	}

	public List<TreeNode> getCommonRoot() {
		List<TreeNode> list = new ArrayList<TreeNode>();		
		if (commonRoot != null) {
			list.add(commonRoot);
			return list;
		} else {
			TreeNode current = root;

			if (current.leafs.size() <= 0) {
				list = current.childs;
			} else{
				list.add(current);
			}
			return list;
		}

	}


}