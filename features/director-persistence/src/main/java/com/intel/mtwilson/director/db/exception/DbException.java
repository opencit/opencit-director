package com.intel.mtwilson.director.db.exception;

/**
 *
 * @author aakashmX
 *
 */
public class DbException extends Exception {

    private static final long serialVersionUID = 1L;

    public DbException() {
    }

    public DbException(String message) {
        super(message);
    }

    public DbException(String message, Throwable cause) {
        super(message, cause);
    }

    public DbException(Throwable cause) {
        super(cause);
    }

    public DbException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
