/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.rs;

/**
 *
 * @author GS-0681
 */
public class GlanceException extends Exception {

    public GlanceException() {
    }

    public GlanceException(String message) {
        super(message);
    }

    public GlanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public GlanceException(Throwable cause) {
        super(cause);
    }

    public GlanceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
