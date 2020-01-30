package com.realtimetech.fermes.database.exception;

import com.realtimetech.fermes.exception.FermesException;

public class DatabaseReadException extends FermesException {
	private static final long serialVersionUID = 4589143502109416158L;

	public DatabaseReadException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public DatabaseReadException(String message) {
		super(message);
	}
}
