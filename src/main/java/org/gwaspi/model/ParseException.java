package org.gwaspi.model;

public class ParseException extends RuntimeException {

	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Throwable t) {
		super(message, t);
	}
}
