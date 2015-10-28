/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.director.attestation.server;

/**
 *
 * @author boskisha
 */
public class MtwConnectionException extends RuntimeException {

    public MtwConnectionException() {
        super();
    }

    public MtwConnectionException(Throwable e) {
        super(e);
    }

    public MtwConnectionException(String message) {
        super(message);
    }

    public MtwConnectionException(String message, Throwable e) {
        super(message, e);
    }
}
