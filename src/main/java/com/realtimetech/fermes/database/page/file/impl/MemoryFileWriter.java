package com.realtimetech.fermes.database.page.file.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.realtimetech.fermes.database.page.file.FileWriter;

public class MemoryFileWriter extends FileWriter {
	private byte[][] buffers;

	private long pointer;

	private long size;

	public MemoryFileWriter(File file) {
		this(file.length(), file);
	}

	public MemoryFileWriter(long size, File file) {
		super(file);

		this.pointer = 0;
		this.size = size;

		long currentSize = size;
		int bufferCount = (int) Math.ceil((float) size / (float) Integer.MAX_VALUE);
		int index = 0;

		this.buffers = new byte[bufferCount][];
		while (currentSize > 0) {
			int bufferSize = -1;

			if (currentSize > Integer.MAX_VALUE) {
				bufferSize = Integer.MAX_VALUE;
			} else {
				bufferSize = (int) currentSize;
			}

			this.buffers[index++] = new byte[bufferSize];

			currentSize -= bufferSize;
		}
	}

	@Override
	public void reset() throws IOException {
		this.pointer = 0;
	}

	@Override
	public long get() throws IOException {
		return this.pointer;
	}

	@Override
	public void set(long pointer) throws IOException {
		this.pointer = pointer;
	}

	@Override
	public void writeByte(byte value) throws IOException {
		if (this.pointer < 0 || this.pointer >= this.size) {
			throw new IOException("Index is out of range.");
		}

		this.buffers[(int) (pointer / Integer.MAX_VALUE)][(int) (pointer % Integer.MAX_VALUE)] = value;

		pointer++;
	}

	@Override
	public void writeBytes(byte[] value) throws IOException {
		for (byte byteValue : value) {
			this.writeByte(byteValue);
		}
	}

	@Override
	public void writeBytes(byte[] value, int offset, int length) throws IOException {
		for (int index = 0; index < length; index++) {
			this.writeByte(value[offset + index]);
		}
	}

	@Override
	public void writeInteger(int value) throws IOException {
		this.writeByte((byte) (value >> 24));
		this.writeByte((byte) (value >> 16));
		this.writeByte((byte) (value >> 8));
		this.writeByte((byte) (value));
	}

	@Override
	public void writeLong(long value) throws IOException {
		this.writeByte((byte) (value >> 56));
		this.writeByte((byte) (value >> 48));
		this.writeByte((byte) (value >> 40));
		this.writeByte((byte) (value >> 32));
		this.writeByte((byte) (value >> 24));
		this.writeByte((byte) (value >> 16));
		this.writeByte((byte) (value >> 8));
		this.writeByte((byte) (value));
	}

	@Override
	public byte readByte() throws IOException {
		if (this.pointer < 0 || this.pointer >= this.size) {
			throw new IOException("Index is out of range.");
		}

		byte value = this.buffers[(int) (pointer / Integer.MAX_VALUE)][(int) (pointer % Integer.MAX_VALUE)];

		pointer++;

		return value;
	}

	@Override
	public int readBytes(byte[] bytes) throws IOException {
		int index = 0;

		for (; index < bytes.length; index++) {
			bytes[index] = readByte();
		}

		return index;
	}

	@Override
	public int readBytes(byte[] bytes, int offset, int length) throws IOException {
		int index = 0;

		for (; index < length; index++) {
			bytes[offset + index] = readByte();
		}

		return index;
	}

	@Override
	public int readInteger() throws IOException {
		int value;

		value = (((int) readByte() & 0xFF) << 24);
		value |= (((int) readByte() & 0xFF) << 16);
		value |= (((int) readByte() & 0xFF) << 8);
		value |= (((int) readByte() & 0xFF));

		return value;
	}

	@Override
	public long readLong() throws IOException {
		long value = 0;

		value = (((long) readByte() & 0xFF) << 56);
		value |= (((long) readByte() & 0xFF) << 48);
		value |= (((long) readByte() & 0xFF) << 40);
		value |= (((long) readByte() & 0xFF) << 32);
		value |= (((long) readByte() & 0xFF) << 24);
		value |= (((long) readByte() & 0xFF) << 16);
		value |= (((long) readByte() & 0xFF) << 8);
		value |= (((long) readByte() & 0xFF));

		return value;
	}

	public void save() throws IOException {
		createFileIfNotExist();

		FileOutputStream fileOutputStream = new FileOutputStream(getFile(), false);

		for (byte[] bytes : this.buffers) {
			fileOutputStream.write(bytes);
		}

		fileOutputStream.close();
	}

	public void load() throws IOException {
		createFileIfNotExist();

		FileInputStream fileInputStream = new FileInputStream(getFile());

		for (byte[] bytes : this.buffers) {
			int goal = bytes.length;

			while (goal > 0) {
				goal -= fileInputStream.read(bytes);
			}
		}

		fileInputStream.close();
	}

	@Override
	public void close() throws IOException {
		
	}
}
