/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.swift.rs;

/**
 *
 * @author Aakash
 */
public class SwiftException extends Exception {

    public SwiftException() {
    }

    public SwiftException(String message) {
        super(message);
    }

    public SwiftException(String message, Throwable cause) {
        super(message, cause);
    }

    public SwiftException(Throwable cause) {
        super(cause);
    }

    public SwiftException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
