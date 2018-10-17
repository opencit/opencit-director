package com.intel.director.api;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author GS-0681
 */
public class ImageStoreRequest {
	
	public String name;
	public String disk_format;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisk_format() {
		return disk_format;
	}
	public void setDisk_format(String disk_format) {
		this.disk_format = disk_format;
	}
	
	
	
}
