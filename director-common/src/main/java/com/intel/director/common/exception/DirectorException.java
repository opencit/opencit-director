/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.common.exception;

/**
 *
 * @author GS-0681
 */
public class DirectorException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DirectorException() {
    }

    public DirectorException(String message) {
        super(message);
    }

    public DirectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public DirectorException(Throwable cause) {
        super(cause);
    }

}
