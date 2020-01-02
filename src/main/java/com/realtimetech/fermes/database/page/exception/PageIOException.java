package com.realtimetech.fermes.database.page.exception;

public class PageIOException extends Exception {
	private static final long serialVersionUID = -6165941734151748058L;

	private String message;

	public PageIOException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return "** Fermes PageIO Exception " + System.lineSeparator() + "\t" + message + "";
	}
}
