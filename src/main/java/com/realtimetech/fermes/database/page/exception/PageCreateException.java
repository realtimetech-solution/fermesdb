package com.realtimetech.fermes.database.page.exception;

import com.realtimetech.fermes.exception.FermesException;

public class PageCreateException extends FermesException {
	private static final long serialVersionUID = 4589143502109416158L;

	public PageCreateException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public PageCreateException(String message) {
		super(message);
	}
}
