package com.realtimetech.fermes.exception;

public abstract class FermesException extends Exception {
	private static final long serialVersionUID = -8886328917611014182L;

	private Exception reasonException;

	private String message;

	public FermesException(String message) {
		this(null, message);
	}

	public FermesException(Exception reasonException, String message) {
		this.reasonException = reasonException;
		this.message = message;
	}

	@Override
	public String getMessage() {
		return "Fermes Exception '" + message + "'";
	}

	@Override
	public void printStackTrace() {
		super.printStackTrace();

		if (this.reasonException != null) {
			this.reasonException.printStackTrace();
		}
	}
}
