/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.exception;

/**
 *
 * @author GS-0681
 */
public class ImageMountException extends DirectorException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageMountException() {
    }

    public ImageMountException(String message) {
        super(message);
    }

    public ImageMountException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageMountException(Throwable cause) {
        super(cause);
    }

}
