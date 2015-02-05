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
public class SetupException extends RuntimeException {
    public SetupException() {
        super();
    }
    public SetupException(Throwable e) {
        super(e);
    }
    public SetupException(String message) {
        super(message);
    }
    public SetupException(String message, Throwable e) {
        super(message, e);
    }
}
