package com.realtimetech.fermes.database.link.exception;

import com.realtimetech.fermes.exception.FermesException;

public class LinkRemoveException extends FermesException {
	private static final long serialVersionUID = 4589143502109416158L;

	public LinkRemoveException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public LinkRemoveException(String message) {
		super(message);
	}
}