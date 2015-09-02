/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.director.api.SearchFilesInImageRequest;
import com.intel.director.api.SearchFilesInImageResponse;
import com.intel.director.service.ImageService;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author GS-0681
 */
public class PollerTest {

    public static void main(String[] args) {
    	ImageService imageService= new ImageServiceImpl();
    	SearchFilesInImageRequest searchFilesInImageRequest = new SearchFilesInImageRequest();
    	searchFilesInImageRequest.dir = "C:/temp";
    	File f = new File("C:\\temp");

    	ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));		
    	System.out.println("DONE");
    	
    }
}
