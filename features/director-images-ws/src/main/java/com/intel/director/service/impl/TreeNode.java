package com.intel.director.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.intel.director.common.Constants;
import com.intel.director.api.ui.TreeElement;
import com.intel.mtwilson.director.trust.policy.DirectoryAndFileUtil;

public class TreeNode implements Comparable<TreeNode> {
	Tree parent;
	List<TreeNode> childs;
	List<TreeNode> leafs;
	String data;
	String incrementalPath;
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TreeNode.class);
	private boolean isDirectory = false;
	public boolean firstChildOrLeaf = false;
	public int renderedCount = 0;
	public boolean  checked = false;
	public boolean isSymlink = false;
	public TreeNodeDetail rootDirWithRegex = null;
	public String mountPath=null;

	public TreeNode(String nodeValue, String incrementalPath) {
		childs = new ArrayList<TreeNode>();
		leafs = new ArrayList<TreeNode>();
		data = nodeValue;
		this.incrementalPath = incrementalPath;
		firstChildOrLeaf = false;
		renderedCount = 0;

	}

	public TreeNode(String nodeValue, String incrementalPath, String mountPath) {
		childs = new ArrayList<TreeNode>();
		leafs = new ArrayList<TreeNode>();
		data = nodeValue;
		this.incrementalPath = incrementalPath;
		firstChildOrLeaf = false;
		renderedCount = 0;
		if (new File(mountPath + incrementalPath).isDirectory()) {
			isDirectory = true;
		}
		this.mountPath=mountPath;

	}

  

	public void setParent(Tree tree) {
		this.parent = tree;
	}

	public void checkNode() {
		this.checked =true;
	}

	public boolean isLeaf() {
		return childs.isEmpty() && leafs.isEmpty();
	}

	private int getChildAndLeafCount() {
		return (childs.size() + leafs.size());
	}

	public boolean haveAllElementsOfNodeRendered() {
		if (renderedCount == getChildAndLeafCount()) {
			return true;
		}
		return false;
	}

	public void createNode(String currentPath, String[] list) {

		// Avoid first element that can be an empty string if you split a string
		// that has a starting slash as /sd/card/
		DirectoryAndFileUtil directoryUtil= new DirectoryAndFileUtil();
		while (StringUtils.isBlank(list[0])){
			list = Arrays.copyOfRange(list, 1, list.length);
		}
		if (!currentPath.endsWith("/")) {
			currentPath = currentPath + "/";
		}

		TreeNode currentChild = new TreeNode(list[0], currentPath + list[0],mountPath);
		if (parent.trustPolicyElementsList != null) {
			if (parent.trustPolicyElementsList
					.contains(currentChild.incrementalPath)) {
				currentChild.checkNode();
			}
		} else {
			currentChild.checked = parent.checked;
		}
		String childPath=parent.mountPath + currentChild.incrementalPath;
		Path path = Paths.get(childPath);
		if (Files.isSymbolicLink(path)) {
			log.debug("Symlink path: {}", path);
			currentChild.data = directoryUtil.getSymlinkTarget(childPath,mountPath);
			log.debug("Symlink target: {}", currentChild.data);
			currentChild.isSymlink=true;
		}
		
		if (new File(parent.mountPath + currentChild.incrementalPath)
				.isDirectory()) {
			currentChild.isDirectory = true;
		}
		
		if(parent.dirPathsForEdit.contains(childPath)){
			currentChild.checkNode();
		}

		currentChild.parent = parent;
		if (list.length == 1) {
			if ((parent.explodedView && currentChild.isDirectory)
					//|| (currentChild.isDirectory && currentChild.checked)
					|| parent.dirPathsForEdit
							.contains(currentChild.incrementalPath)) {
				return;
			}
			leafs.add(currentChild);
			Collections.sort(leafs);
			return;
		} else {
			int index = childs.indexOf(currentChild);
			if (index == -1) {
				childs.add(currentChild);
				currentChild.createNode(currentChild.incrementalPath,
						Arrays.copyOfRange(list, 1, list.length));
			} else {
				TreeNode nextChild = childs.get(index);
				nextChild.createNode(currentChild.incrementalPath,
						Arrays.copyOfRange(list, 1, list.length));
			}
		}

	}

	@Override
	public boolean equals(Object obj) {
		TreeNode cmpObj = (TreeNode) obj;
		return incrementalPath.equals(cmpObj.incrementalPath)
				&& data.equals(cmpObj.data);
	}

	

	

	public TreeElement generateTree() {

		TreeElement currentTreeElement = new TreeElement();
		if (checked) {
			currentTreeElement.setIsSelected(true);
		}
		
		currentTreeElement.setPath(incrementalPath);
		currentTreeElement.setValue(data);
		
		initTreeElementForRegex(currentTreeElement);
		
		if(isSymlink){
			currentTreeElement.setElementType(Constants.TREE_ELEMENT_SYMLINK);
		}else if (isDirectory) {

			currentTreeElement.setElementType(Constants.TREE_ELEMENT_DIRECTORY);
			
		}else{
			currentTreeElement.setElementType(Constants.TREE_ELEMENT_FILE);
		}

		

		List<TreeNode> combined = new ArrayList<>();
		combined.addAll(childs);
		combined.addAll(leafs);
		Collections.sort(combined);
		for (TreeNode n : combined) {
			currentTreeElement.getChildElements().add(n.generateTree());
		}

		return currentTreeElement;
	}

	
	
	public void initTreeElementForRegex(TreeElement currentTreeElement) {
		if (parent.root.rootDirWithRegex != null) {
			String include = StringUtils
					.isBlank(parent.root.rootDirWithRegex.regexInclude) ? ""
					: parent.root.rootDirWithRegex.regexInclude;
			String exclude = StringUtils
					.isBlank(parent.root.rootDirWithRegex.regexExclude) ? ""
					: parent.root.rootDirWithRegex.regexExclude;
			currentTreeElement
					.setRegexRecursive(parent.root.rootDirWithRegex.isRegexRecursive);
			currentTreeElement.setIsLocked(checkIsLocked(currentTreeElement));
			currentTreeElement.setExclude(exclude);
			currentTreeElement.setInclude(include);
		}

		for (TreeNodeDetail nodeDetail : parent.directoryListContainingRegex) {
			if (incrementalPath.equals(nodeDetail.regexPath)) {
				String include = StringUtils.isBlank(nodeDetail.regexInclude) ? ""
						: nodeDetail.regexInclude;
				String exclude = StringUtils.isBlank(nodeDetail.regexExclude) ? ""
						: nodeDetail.regexExclude;
				currentTreeElement
						.setRegexRecursive(nodeDetail.isRegexRecursive);
				///currentTreeElement.setIsLocked(checkIsLocked(currentTreeElement));
				
				currentTreeElement.setExclude(exclude);
				currentTreeElement.setInclude(include);
        currentTreeElement.setFilterType(nodeDetail.filterType);
				break;
			}
		}

	}
	
	
	public boolean checkIsLocked(TreeElement currentTreeElement){
		boolean isLocked=false;
		if (isDirectory) {
			if (parent.root.rootDirWithRegex != null) {
				if (parent.root.rootDirWithRegex.isRegexRecursive
						|| (!(parent.root.rootDirWithRegex.isRegexRecursive) && (parent.root.incrementalPath
								.equals(currentTreeElement.getValue()
										.startsWith(File.separator) ? currentTreeElement
										.getValue()
										: (File.separator + currentTreeElement
												.getValue()))))) {
					isLocked=true;
				}

			}else{
				
				/// TO DO :- Need to add Condition for edit flow
				isLocked=true;
			}
		}
		return isLocked;
		
	}
	
	
	public boolean isDirectory() {
		return (childs.size() > 0 || leafs.size() > 0);
	}

	@Override
	public String toString() {
		return data;
	}

	@Override
	public int compareTo(TreeNode o) {
		return this.incrementalPath.compareTo(o.incrementalPath);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return incrementalPath.hashCode();
	}
	


}