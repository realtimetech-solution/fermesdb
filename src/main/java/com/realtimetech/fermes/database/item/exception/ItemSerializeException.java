package com.realtimetech.fermes.database.item.exception;

import com.realtimetech.fermes.exception.FermesException;

public class ItemSerializeException extends FermesException {
	private static final long serialVersionUID = 7019046866027384876L;

	public ItemSerializeException(Exception reasonException, String message) {
		super(reasonException, message);
	}

	public ItemSerializeException(String message) {
		super(message);
	}
}
