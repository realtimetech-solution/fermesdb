package com.realtimetech.fermes.database.item.exception;

public class FermesItemAccessException extends Exception {
	private static final long serialVersionUID = -6165941734151748058L;

	private String message;

	public FermesItemAccessException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return "** Fermes Item Access Exception " + System.lineSeparator() + "\t" + message + "";
	}
}
