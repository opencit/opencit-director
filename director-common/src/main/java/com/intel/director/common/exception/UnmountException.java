package com.intel.director.common.exception;

/**
 * 
 * @author boskisha
 */
public class UnmountException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
