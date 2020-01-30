package com.realtimetech.fermes.database.exception;

import com.realtimetech.fermes.exception.FermesException;

public class DatabaseCloseException extends FermesException {
	private static final long serialVersionUID = 4589143502109416158L;

	public DatabaseCloseException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public DatabaseCloseException(String message) {
		super(message);
	}
}
