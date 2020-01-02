package com.realtimetech.fermes.database.page.file;

import java.io.File;
import java.io.IOException;

public abstract class FileWriter {
	private File file;

	public FileWriter(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public abstract void reset() throws IOException;

	public abstract long get() throws IOException;

	public abstract void set(long pointer) throws IOException;

	public abstract void writeByte(byte value) throws IOException;

	public abstract void writeBytes(byte[] value) throws IOException;

	public abstract void writeBytes(byte[] value, int offset, int length) throws IOException;

	public abstract void writeInteger(int value) throws IOException;

	public abstract void writeLong(long value) throws IOException;

	public abstract byte readByte() throws IOException;

	public abstract int readBytes(byte[] bytes) throws IOException;

	public abstract int readBytes(byte[] bytes, int offset, int length) throws IOException;

	public abstract int readInteger() throws IOException;

	public abstract long readLong() throws IOException;
	
	public void createFileIfNotExist() throws IOException {
		if (!this.file.exists()) {
			this.file.createNewFile();
		}
	}
}
