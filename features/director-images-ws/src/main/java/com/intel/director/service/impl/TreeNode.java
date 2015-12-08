package com.intel.director.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TreeNode implements Comparable{
	Tree parent;
	List<TreeNode> childs;
	List<TreeNode> leafs;
	String data;
	String incrementalPath;
	boolean ulBeginBool = false;
	boolean ulEndBool = false;
	final String ulBegin = "<ul class=\"jqueryFileTree\" style=\"display: none;\">";
	final String ulEnd = "</ul>";
	private boolean isDirectory = false;
	public boolean firstChildOrLeaf = false;
	public int renderedCount = 0;
	public String checked = "";
	public String rootDirWithRegex = null;
	private final String lockHover = "To unselect files/dirs click on the icon to Reset";
	private final String unlockHover = "To lock the directory and specific files click on the icon to apply regex";

	public TreeNode(String nodeValue, String incrementalPath) {
		childs = new ArrayList<TreeNode>();
		leafs = new ArrayList<TreeNode>();
		data = nodeValue;
		this.incrementalPath = incrementalPath;
		firstChildOrLeaf = false;
		renderedCount = 0;
	}

	public void setParent(Tree tree) {
		this.parent = tree;
	}

	public void checkNode() {
		this.checked = "checked=\"true\"";
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

	public void addElement(String currentPath, String[] list) {

		// Avoid first element that can be an empty string if you split a string
		// that has a starting slash as /sd/card/
		while (StringUtils.isBlank(list[0])){
			list = Arrays.copyOfRange(list, 1, list.length);
		}
		if (!currentPath.endsWith("/")) {
			currentPath = currentPath + "/";
		}

		TreeNode currentChild = new TreeNode(list[0], currentPath + list[0]);
		if (parent.trustPolicyElementsList != null) {
			if (parent.trustPolicyElementsList
					.contains(currentChild.incrementalPath)) {
				currentChild.checkNode();
			}
		} else {
			currentChild.checked = parent.checked;
		}
		if (new File(parent.mountPath + currentChild.incrementalPath)
				.isDirectory()) {
			currentChild.isDirectory = true;
		}
		
		if(parent.dirPathsForEdit.contains(parent.mountPath + currentChild.incrementalPath)){
			currentChild.checkNode();
		}

		currentChild.parent = parent;
		if (list.length == 1) {
			if ((parent.explodedView && currentChild.isDirectory)
					|| (currentChild.isDirectory && StringUtils
							.isNotEmpty(currentChild.checked))
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
				currentChild.addElement(currentChild.incrementalPath,
						Arrays.copyOfRange(list, 1, list.length));
			} else {
				TreeNode nextChild = childs.get(index);
				nextChild.addElement(currentChild.incrementalPath,
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

	private void addToTree(String text, boolean toAdd) {
		if (!toAdd) {
			return;
		}
		parent.treeElementsHtml.add(text);
	}

	public void printNode(boolean ulBeginBool, boolean ulEndBool) {

		boolean add = true;
		StringBuilder builder = new StringBuilder();
		if (incrementalPath.equals(parent.root.incrementalPath)) {
			add = false;
		}
		if (ulBeginBool) {
			addToTree(ulBegin, add);
			builder.append(ulBegin);

		}

		String checkbox = null;
		String liClass = null;
		String liColorClass = "";
		String toggleIcon = "";
		String toggleStyle = "";
		String regexIdentifier = "";
		String iconName = "unlocked.png";
		String hoverText = null;
		if (StringUtils.isNotBlank(checked)) {
			liColorClass = "selected";
		}
		
		if(parent.root.rootDirWithRegex != null){
			regexIdentifier = " rootRegexDir=\""+ parent.root.rootDirWithRegex +"\"";
		}
		
		//in case of init we have a different flow for setting regex
		for(String rootRegexPath : parent.directoryListContainingRegex){
			if(incrementalPath.startsWith(rootRegexPath)){
				regexIdentifier = " rootRegexDir=\""+ rootRegexPath +"\"";
				break;
			}
		}
		
		hoverText = unlockHover;
		if(StringUtils.isNotEmpty(regexIdentifier)){
			iconName = "locked.png";
			hoverText = lockHover;
		}
		
		if (isDirectory) {
			checkbox = "<input type=\"checkbox\" name=\"directory_"
					+ incrementalPath + "\" id=\"" + incrementalPath + "\""
					+ checked + regexIdentifier + " style=\"float:left;\"/>";
			
			liClass = "directory "
					+ ((!checked.isEmpty() ? "expanded"
							: parent.directoryCollapsed));
			toggleIcon = "<img src=\"/v1/html5/public/director-html5/images/"+iconName+"\" title=\""
					+ hoverText
					+ "\"  id=\"toggle_"
					+ incrementalPath
					+ "\"   onclick=\"toggleState(this)\" />";
			toggleStyle = " style=\"float:left;\" ";

		} else {
			checkbox = "<input type=\"checkbox\" name=\"file_"
					+ incrementalPath + "\" id=\"" + incrementalPath + "\""
					+ checked + regexIdentifier + " style=\"float:left;\"/>";

			liClass = "file";

		}
		addToTree("<li class=\"" + liClass + " " + liColorClass + "\">", add);
		addToTree(checkbox, add);
		addToTree("<a href=\"#\" " + toggleStyle + "rel=\"", add);
		addToTree(incrementalPath, add);
		addToTree("/\">", add);
		addToTree(data, add);
		addToTree("</a>", add);
		addToTree(toggleIcon, add);

		if (isLeaf()) {
			addToTree("</li>", add);
		}

		if (ulEndBool) {
			builder.append(ulEnd);
			addToTree(ulEnd, add);
		}
//		int noOfChildren = childs.size();
//		int childCnt = 0;
		List<TreeNode> combined = new ArrayList<>();
		combined.addAll(childs);
		combined.addAll(leafs);
		Collections.sort(combined);
		for (TreeNode n : combined) {
			if(childs.contains(n)){
				boolean showULBegin = false;
				if (!firstChildOrLeaf) {
					firstChildOrLeaf = true;
					showULBegin = true;
					;
				}
				renderedCount++;
				n.printNode(showULBegin, false);
				addToTree("</li>", true);
				// if (++childCnt == noOfChildren) {
				if (haveAllElementsOfNodeRendered()) {
					builder.append(ulEnd);
					addToTree(ulEnd, add);
				}

			}else{
//				int noOfLeafElements = leafs.size();
//				int leaftCnt = 0;

				boolean showULBegin = false;
				boolean showULEnd = false;
				if (!firstChildOrLeaf) {
					firstChildOrLeaf = true;
					showULBegin = true;
				}

				renderedCount++;

				if (haveAllElementsOfNodeRendered()) {
					showULEnd = true;
				}
				n.printNode(showULBegin, showULEnd);

			}
		}		

	}

	public boolean isDirectory() {
		return (childs.size() > 0 || leafs.size() > 0);
	}

	@Override
	public String toString() {
		return data;
	}

	@Override
	public int compareTo(Object o) {
		TreeNode other = (TreeNode)o;
		return this.incrementalPath.compareTo(other.incrementalPath);		
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return incrementalPath.hashCode();
	}
	
	

}