package com.realtimetech.fermes.database.io;

import com.realtimetech.fermes.database.Database;

public abstract class StoreSerializable {
	private Database database;

	public StoreSerializable(Database database) {
		this.database = database;
	}

	public Database getDatabase() {
		return database;
	}
}
