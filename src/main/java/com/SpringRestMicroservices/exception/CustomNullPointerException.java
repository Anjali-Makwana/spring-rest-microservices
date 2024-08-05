package com.SpringRestMicroservices.exception;

public class CustomNullPointerException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomNullPointerException(String message) {
		super(message);
	}
}
