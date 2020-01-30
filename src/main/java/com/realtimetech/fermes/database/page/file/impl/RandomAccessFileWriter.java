package com.realtimetech.fermes.database.page.file.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.realtimetech.fermes.database.page.file.FileWriter;

public class RandomAccessFileWriter extends FileWriter {
	private RandomAccessFile randomAccessFile;

	public RandomAccessFileWriter(File file) throws FileNotFoundException {
		super(file);

		this.randomAccessFile = new RandomAccessFile(file, "rw");
	}

	@Override
	public void reset() throws IOException {
		this.randomAccessFile.seek(0);
	}

	@Override
	public long get() throws IOException {
		return this.randomAccessFile.getFilePointer();
	}

	@Override
	public void set(long pointer) throws IOException {
		this.randomAccessFile.seek(pointer);
	}

	@Override
	public void writeByte(byte value) throws IOException {
		this.randomAccessFile.write(value);
	}

	@Override
	public void writeBytes(byte[] value) throws IOException {
		this.randomAccessFile.write(value);
	}

	@Override
	public void writeBytes(byte[] value, int offset, int length) throws IOException {
		this.randomAccessFile.write(value, offset, length);
	}

	@Override
	public void writeInteger(int value) throws IOException {
		this.randomAccessFile.writeInt(value);
	}

	@Override
	public void writeLong(long value) throws IOException {
		this.randomAccessFile.writeLong(value);
	}

	@Override
	public byte readByte() throws IOException {
		return this.randomAccessFile.readByte();
	}

	@Override
	public int readBytes(byte[] bytes) throws IOException {
		return this.randomAccessFile.read(bytes);
	}

	@Override
	public int readBytes(byte[] bytes, int offset, int length) throws IOException {
		return this.randomAccessFile.read(bytes, offset, length);
	}

	@Override
	public int readInteger() throws IOException {
		return this.randomAccessFile.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return this.randomAccessFile.readLong();
	}

	@Override
	public void close() throws IOException {
		this.randomAccessFile.close();		
	}
}
