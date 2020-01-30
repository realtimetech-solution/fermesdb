package com.realtimetech.fermes.database.link.exception;

import com.realtimetech.fermes.exception.FermesException;

public class LinkCreateException extends FermesException {
	private static final long serialVersionUID = 4589143502109416158L;

	public LinkCreateException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public LinkCreateException(String message) {
		super(message);
	}
}