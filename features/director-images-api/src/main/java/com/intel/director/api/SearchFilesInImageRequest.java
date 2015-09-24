/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 *
 * @author soakx
 */
public class SearchFilesInImageRequest {

    public String dir;
    public String include;
    public String exclude;
    public boolean recursive;
    public boolean filesForPolicy;
    public boolean init;
    public String id;
	public String getDir()  {
		try {
			return URLDecoder.decode(dir, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
    
    
}
