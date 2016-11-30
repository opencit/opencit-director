package com.intel.director.swift.api;


/**
*
* @author Aakash
*/
public class SwiftObject {

	public String name;
	public String hash;
///	public Date last_modified;
	public String content_type;
	public long bytes;
	
	
	
	@Override
	public String toString() {
		return "SwiftObject [name=" + name + ", hash=" + hash
				+ ", content_type="
				+ content_type + ", bytes=" + bytes + "]";
	}

	public SwiftObject() {
		super();
	}

	public SwiftObject(String name, String hash,
			String content_type, long bytes) {
		super();
		this.name = name;
		this.hash = hash;
	//	this.last_modified = last_modified;
		this.content_type = content_type;
		this.bytes = bytes;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
/*	public Date getLast_modified() {
		return last_modified;
	}
	public void setLast_modified(Date last_modified) {
		this.last_modified = last_modified;
	}*/
	public String getContent_type() {
		return content_type;
	}
	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}
	public long getBytes() {
		return bytes;
	}
	public void setBytes(long bytes) {
		this.bytes = bytes;
	}
	
	
	
	
	
}
