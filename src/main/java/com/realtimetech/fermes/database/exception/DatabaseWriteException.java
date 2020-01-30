package com.realtimetech.fermes.database.exception;

import com.realtimetech.fermes.exception.FermesException;

public class DatabaseWriteException extends FermesException {
	private static final long serialVersionUID = -318731495422633466L;

	public DatabaseWriteException(Exception reasonException, String message) {
		super(reasonException, message);
	}
	
	public DatabaseWriteException(String message) {
		super(message);
	}
}
