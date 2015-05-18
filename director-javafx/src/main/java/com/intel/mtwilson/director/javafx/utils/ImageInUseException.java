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
public class ImageInUseException extends Exception{
    public ImageInUseException() {
        super();
    }
    public ImageInUseException(String message) {
        super(message);
    }
    public ImageInUseException(Throwable cause) {
        super(cause);
    }
}
