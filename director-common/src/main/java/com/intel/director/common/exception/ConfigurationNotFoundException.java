package com.intel.director.common.exception;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * 
 * @author boskisha
 */
public class ConfigurationNotFoundException extends Exception {
	public ConfigurationNotFoundException() {
		super();
	}

	public ConfigurationNotFoundException(String message) {
		super(message);
	}

	public ConfigurationNotFoundException(Throwable cause) {
		super(cause);
	}
}
