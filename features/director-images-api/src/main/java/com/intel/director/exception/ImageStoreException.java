/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.exception;

/**
 *
 * @author GS-0681
 */
public class ImageStoreException extends Exception {

    public ImageStoreException() {
    }

    public ImageStoreException(String message) {
        super(message);
    }

    public ImageStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageStoreException(Throwable cause) {
        super(cause);
    }

    public ImageStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
