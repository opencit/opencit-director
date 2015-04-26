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
public class UnmountException extends RuntimeException {

    public UnmountException() {
        super();
    }

    public UnmountException(Throwable e) {
        super(e);
    }

    public UnmountException(String message) {
        super(message);
    }

    public UnmountException(String message, Throwable e) {
        super(message, e);
    }
}
