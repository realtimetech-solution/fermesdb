package com.realtimetech.fermes.database.page.exception;

public class BlockIOException extends Exception {
	private static final long serialVersionUID = -6165941734151748058L;

	private String message;

	public BlockIOException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return "** Fermes BlockIO Exception " + System.lineSeparator() + "\t" + message + "";
	}
}
