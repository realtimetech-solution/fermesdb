package com.realtimetech.fermes.database.io;

import java.io.IOException;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.page.file.impl.MemoryFileWriter;

public abstract class StoreSerializer<V extends StoreSerializable> {
	private Database database;
	
	public StoreSerializer(Database database) {
		this.database = database;
	}
	
	public Database getDatabase() {
		return database;
	}

	public abstract long getWriteLength(V value);

	public void write(V value, MemoryFileWriter pageBuffer) throws IOException {
		this.onWrite(value, pageBuffer);
	}
	
	public abstract void onWrite(V value, MemoryFileWriter pageBuffer) throws IOException;
	
	public void read(V value, MemoryFileWriter pageBuffer) throws IOException {
		this.onRead(value, pageBuffer);
	}
	
	public abstract void onRead(V value, MemoryFileWriter pageBuffer) throws IOException;
}
