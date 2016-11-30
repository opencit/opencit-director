/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.store;

import java.net.URL;
import java.util.List;
import java.util.Map;

import com.intel.director.api.GenericResponse;
import com.intel.director.api.StoreResponse;
import com.intel.director.store.exception.StoreException;


/**
 *
 * @author Aakash
 */
public interface StoreManager {

	  public String upload() throws StoreException;
	  public  <T extends StoreResponse> T  fetchDetails() throws StoreException ;
	  public void build(Map<String, String> map) throws StoreException;
	  public void addCustomProperties(Map<String, Object> map) throws StoreException;
	  public void update() throws StoreException;
	  public void delete(URL url) throws StoreException;
	  public <T extends StoreResponse> List<T> fetchAllImages() throws StoreException;
	  public GenericResponse validate() throws StoreException;
}
