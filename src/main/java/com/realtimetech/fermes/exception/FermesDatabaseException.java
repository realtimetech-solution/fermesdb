package com.realtimetech.fermes.exception;

public class FermesDatabaseException extends Exception {
	private static final long serialVersionUID = -6165941734151748058L;

	private String message;

	public FermesDatabaseException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return "** Fermes Database Exception " + System.lineSeparator() + "\t" + message + "";
	}
}
