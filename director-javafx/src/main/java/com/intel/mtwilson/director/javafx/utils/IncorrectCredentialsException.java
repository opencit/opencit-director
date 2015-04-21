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
public class IncorrectCredentialsException extends RuntimeException {

    public IncorrectCredentialsException() {
        super();
    }

    public IncorrectCredentialsException(Throwable e) {
        super(e);
    }

    public IncorrectCredentialsException(String message) {
        super(message);
    }

    public IncorrectCredentialsException(String message, Throwable e) {
        super(message, e);
    }
}
