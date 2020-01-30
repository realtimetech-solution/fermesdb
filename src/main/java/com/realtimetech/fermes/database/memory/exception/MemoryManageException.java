package com.realtimetech.fermes.database.memory.exception;

import com.realtimetech.fermes.exception.FermesException;

public class MemoryManageException extends FermesException {
	private static final long serialVersionUID = 4589143502109416158L;

	public MemoryManageException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public MemoryManageException(String message) {
		super(message);
	}
}