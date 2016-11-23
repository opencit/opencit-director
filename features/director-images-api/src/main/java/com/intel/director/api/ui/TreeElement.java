package com.intel.director.api.ui;

import java.util.Set;
import java.util.TreeSet;

public class TreeElement implements Comparable<TreeElement> {
	private Set<TreeElement> childElements = new TreeSet<TreeElement>();
	private String include;
	private String exclude;
	private Boolean regexRecursive;


	private String elementType;
	private Boolean isLocked = false;
	private Boolean isSelected = false;
	private String filterType=null;
	///private Boolean isExpanded = false;

	private String path;
	private String value;

	
	public String getElementType() {
		return elementType;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Set<TreeElement> getChildElements() {
		return childElements;
	}

	public void setChildElements(Set<TreeElement> childElements) {
		this.childElements = childElements;
	}

	public void addChildElements(TreeElement treeElement) {
		if (childElements == null) {
			childElements = new TreeSet<TreeElement>();
		}
		childElements.add(treeElement);
	}

	public String getInclude() {
		return include;
	}

	public void setInclude(String include) {
		this.include = include;
	}

	public String getExclude() {
		return exclude;
	}

	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	public Boolean getRegexRecursive() {
		return regexRecursive;
	}

	public void setRegexRecursive(Boolean regexRecursive) {
		this.regexRecursive = regexRecursive;
	}

	



	public Boolean getIsLocked() {
		if (isLocked == null) {
			return false;
		}
		return isLocked;
	}

	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}

	public Boolean getIsSelected() {
		if (isSelected == null) {
			return false;
		}

		return isSelected;
	}

	public void setIsSelected(Boolean isSelected) {
		this.isSelected = isSelected;
	}

/*	public Boolean getIsExpanded() {
		return isExpanded;
	}

	public void setIsExpanded(Boolean isExpanded) {
		this.isExpanded = isExpanded;
	}*/

	public String getFilterType() {
		return filterType;
	}

	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

	@Override
	public String toString() {
		return "TreeElement [childElements=" + childElements + ", include="
				+ include + ", exclude=" + exclude + ", regexRecursive="
				+ regexRecursive 
				+ ", isLocked=" + isLocked + ", isSelected=" + isSelected
				 + ", path=" + path + ", value="
				+ value + "]";
	}

	@Override
	public int compareTo(TreeElement o) {
		return this.path.compareTo(o.path);
	}

}
