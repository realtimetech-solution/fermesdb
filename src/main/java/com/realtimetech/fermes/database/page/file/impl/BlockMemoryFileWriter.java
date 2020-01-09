package com.realtimetech.fermes.database.page.file.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.realtimetech.fermes.database.page.file.FileWriter;

public class BlockMemoryFileWriter extends FileWriter {
	private List<byte[]> blocks;

	private long pointer;

	private int blockSize;

	public BlockMemoryFileWriter(int blockSize, File file) {
		super(file);

		this.pointer = 0;
		this.blockSize = blockSize;
		this.blocks = new LinkedList<byte[]>();
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
		if (this.pointer < 0) {
			throw new IOException("Index is out of range.");
		}

		int index = (int) (pointer / blockSize);

		expendBlocks(index);

		this.blocks.get(index)[(int) (pointer % blockSize)] = value;

		pointer++;
	}

	private void expendBlocks(int index) {
		if (index >= this.blocks.size()) {
			while (!(index < this.blocks.size())) {
				this.blocks.add(new byte[this.blockSize]);
			}
		}
	}

	@Override
	public void writeBytes(byte[] value) throws IOException {
		writeBytes(value, 0, value.length);
	}

	@Override
	public void writeBytes(byte[] value, int offset, int length) throws IOException {
		expendBlocks((int) ((pointer + length - 1) / blockSize));

		for (int index = 0; index < length; index++) {
			long virtualPointer = pointer + index;
			this.blocks.get((int) (virtualPointer / blockSize))[(int) (virtualPointer % blockSize)] = value[offset + index];
		}

		pointer += length;
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
		if (this.pointer < 0 || this.pointer >= this.blockSize) {
			throw new IOException("Index is out of range.");
		}

		int index = (int) (pointer / blockSize);

		if (index >= this.blocks.size()) {
			throw new IOException("Index is out of range.");
		}

		byte value = this.blocks.get(index)[(int) (pointer % blockSize)];

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
		BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(getFile(), false));
		for (byte[] bytes : this.blocks) {
			fileOutputStream.write(bytes);
		}

		fileOutputStream.close();
	}

	public void load() throws IOException {
		createFileIfNotExist();

		File file = getFile();
		FileInputStream fileInputStream = new FileInputStream(file);

		int index = (int) Math.ceil(file.length() / this.blockSize);

		expendBlocks(index);

		for (byte[] bytes : this.blocks) {
			int goal = bytes.length;

			while (goal > 0) {
				int read = fileInputStream.read(bytes);
				if (read != -1) {
					goal -= read;
				} else {
					break;
				}
			}
		}

		fileInputStream.close();
	}
}
