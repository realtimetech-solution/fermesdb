package com.realtimetech.fermes.database.page.exception;

import com.realtimetech.fermes.exception.FermesException;

public class BlockWriteException extends FermesException {
	private static final long serialVersionUID = 4589143502109416158L;

	public BlockWriteException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public BlockWriteException(String message) {
		super(message);
	}
}
