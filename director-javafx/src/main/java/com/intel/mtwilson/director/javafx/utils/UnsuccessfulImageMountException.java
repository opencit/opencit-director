/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.javafx.utils;

/**
 *
 * @author boskisha
 */
public class UnsuccessfulImageMountException extends RuntimeException {

    public UnsuccessfulImageMountException() {
        super();
    }

    public UnsuccessfulImageMountException(Throwable e) {
        super(e);
    }

    public UnsuccessfulImageMountException(String message) {
        super(message);
    }

    public UnsuccessfulImageMountException(String message, Throwable e) {
        super(message, e);
    }
}
