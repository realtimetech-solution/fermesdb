package com.realtimetech.fermes.database.exception;

import com.realtimetech.fermes.exception.FermesException;

public class DatabaseBackupException extends FermesException {
	private static final long serialVersionUID = 4589143502109416158L;

	public DatabaseBackupException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public DatabaseBackupException(String message) {
		super(message);
	}
}
