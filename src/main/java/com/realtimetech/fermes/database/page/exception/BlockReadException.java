package com.realtimetech.fermes.database.page.exception;

import com.realtimetech.fermes.exception.FermesException;

public class BlockReadException extends FermesException {
	private static final long serialVersionUID = 4589143502109416158L;

	public BlockReadException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public BlockReadException(String message) {
		super(message);
	}
}
