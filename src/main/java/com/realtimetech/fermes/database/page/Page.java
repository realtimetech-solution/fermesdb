package com.realtimetech.fermes.database.page;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.Link.LinkSerializer;
import com.realtimetech.fermes.database.io.StoreSerializable;
import com.realtimetech.fermes.database.io.StoreSerializer;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.page.exception.BlockReadException;
import com.realtimetech.fermes.database.page.exception.BlockWriteException;
import com.realtimetech.fermes.database.page.file.FileWriter;
import com.realtimetech.fermes.database.page.file.impl.BlockMemoryFileWriter;
import com.realtimetech.fermes.database.page.file.impl.MemoryFileWriter;
import com.realtimetech.fermes.database.page.file.impl.RandomAccessFileWriter;

public class Page extends StoreSerializable {
	public static class PageSerializer extends StoreSerializer<Page> {
		public PageSerializer(Database database) {
			super(database);
		}

		@Override
		public long getWriteLength(Page value) {
			long length = 0;

			length += 4;
			length += 4;

			length += value.emptyBlockIds.size() * 4;

			for (Link<? extends Item> link : value.links) {
				if (link == null) {
					length += 4;
				} else {
					length += value.linkSerializer.getWriteLength(link);
				}
			}

			length += 4;

			return length;
		}

		@Override
		public void onWrite(Page value, MemoryFileWriter pageBuffer) throws IOException {
			pageBuffer.reset();

			pageBuffer.writeInteger(value.maxBlockId);
			pageBuffer.writeInteger(value.emptyBlockIds.size());

			for (Integer emptyBlockId : value.emptyBlockIds) {
				pageBuffer.writeInteger(emptyBlockId);
			}

			for (Link<? extends Item> link : value.links) {
				if (link == null) {
					pageBuffer.writeInteger(0xFF00FF00);
				} else {
					value.linkSerializer.write(link, pageBuffer);
				}
			}
			pageBuffer.writeInteger(0x00FF00FF);
		}

		@Override
		public void onRead(Page value, MemoryFileWriter pageBuffer) throws IOException {
			pageBuffer.reset();
			value.maxBlockId = pageBuffer.readInteger();

			int maxEmptyBlockIdCount = pageBuffer.readInteger();

			for (int index = 0; index < maxEmptyBlockIdCount; index++) {
				value.emptyBlockIds.offer(pageBuffer.readInteger());
			}

			int index = 0;
			int emptyIndex = 0;

			while (true) {
				long rollBack = pageBuffer.get();

				int read = pageBuffer.readInteger();

				if (read == 0xFF00FF00) {
					value.links[index] = null;
				} else {
					if (emptyIndex < index) {
						getDatabase().addEmptyPagePointer(new EmptyPagePointer(value, emptyIndex, index - 1));
					}

					if (read == 0x00FF00FF) {
						break;
					} else {
						pageBuffer.set(rollBack);
						Link<? extends Item> link = new Link<Item>(getDatabase());

						value.linkSerializer.read(link, pageBuffer);

						value.links[index] = link;

						emptyIndex = index + 1;
					}
				}

				index++;
			}
		}
	}

	private int id;

	private Link<? extends Item>[] links;

	private File pageFile;
	private File blockFile;

	private FileWriter blockFileWriter;
	private FileWriter blockFileWriterRollback;

	private LinkSerializer linkSerializer;

	private int blockSize;

	private int maxBlockId;
	private Queue<Integer> emptyBlockIds;

	public Page(Database database) {
		super(database);

		this.linkSerializer = new LinkSerializer(this.getDatabase(), this);
		this.emptyBlockIds = new LinkedList<Integer>();
	}

	public Page(Database database, int id, int pageSize, int blockSize) throws IOException {
		this(database);

		this.id = id;
		this.links = new Link<?>[pageSize];

		this.maxBlockId = 0;

		this.blockSize = blockSize;

		this.pageFile = new File(this.getDatabase().getDatabaseDirectory().getPath() + File.separator + this.id + ".page");
		this.blockFile = new File(this.getDatabase().getDatabaseDirectory().getPath() + File.separator + this.id + ".block");

		this.blockFileWriter = new RandomAccessFileWriter(blockFile);
		this.blockFileWriterRollback = null;
	}

	public int getId() {
		return id;
	}

	public Link<? extends Item>[] getLinks() {
		return links;
	}

	public Link<? extends Item> getLinkByIndex(int index) {
		return this.links.length > index ? this.links[index] : null;
	}

	public void setLinkByIndex(int index, Link<? extends Item> link) {
		this.links[index] = link;
	}

	public synchronized void removeLinkByIndex(int[] blockIds, int index) {
		if (this.links[index] != null) {
			for (int i = 0; i < blockIds.length; i++) {
				synchronized (this.emptyBlockIds) {
					this.emptyBlockIds.offer(blockIds[i]);
				}

				blockIds[i] = -1;
			}

			this.links[index] = null;
		}
	}

	public File getPageFile() {
		return pageFile;
	}

	public FileWriter getBlockFileWriter() {
		return blockFileWriter;
	}

	public int[] fitBlockIds(int[] blockIds, int length) {
		int needIndexies = (int) Math.ceil((float) length / (float) blockSize);

		int manage = needIndexies - blockIds.length;

		if (manage == 0) {
			return blockIds;
		} else {
			int[] newIds = new int[needIndexies];

			if (manage > 0) {
				for (int index = 0; index < newIds.length; index++) {
					if (index >= blockIds.length) {
						synchronized (this.emptyBlockIds) {
							if (!this.emptyBlockIds.isEmpty()) {
								newIds[index] = this.emptyBlockIds.poll();
							} else {
								newIds[index] = this.maxBlockId++;
							}
						}
					} else {
						newIds[index] = blockIds[index];
					}
				}
			} else if (manage < 0) {
				manage *= -1;

				for (int index = 0; index < blockIds.length; index++) {
					if (index >= newIds.length) {
						synchronized (this.emptyBlockIds) {
							this.emptyBlockIds.offer(blockIds[index]);
						}
					} else {
						newIds[index] = blockIds[index];
					}
				}
			}

			return newIds;
		}
	}

	public void writeBlocks(int[] blockIds, byte[] bytes) throws BlockWriteException {
		int index = 0;
		int writeSize = bytes.length;
		for (Integer blockId : blockIds) {
			try {
				synchronized (this.blockFileWriter) {
					this.blockFileWriter.set(blockId * this.blockSize);
					this.blockFileWriter.writeBytes(bytes, index * this.blockSize, writeSize > this.blockSize ? this.blockSize : writeSize);
				}

				index++;
				writeSize -= this.blockSize;
			} catch (IOException e) {
				throw new BlockWriteException(e, "Can't write blocks.");
			}
		}
	}

	public synchronized void enableBlocksDirectly() {
		if (this.blockFileWriterRollback == null) {
			this.blockFileWriterRollback = this.blockFileWriter;
			BlockMemoryFileWriter blockMemoryFileWriter = new BlockMemoryFileWriter(this.blockSize, this.blockFile);
			this.blockFileWriter = blockMemoryFileWriter;
			try {
				blockMemoryFileWriter.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void disableBlocksDirectly() throws IOException {
		if (this.blockFileWriterRollback != null) {
			if (this.blockFileWriter instanceof BlockMemoryFileWriter) {
				BlockMemoryFileWriter blockMemoryFileWriter = (BlockMemoryFileWriter) this.blockFileWriter;

				blockMemoryFileWriter.save();

				this.blockFileWriter = this.blockFileWriterRollback;
				this.blockFileWriterRollback = null;
			}
		}
	}

	public byte[] readBlocks(int[] blockIds, int itemLength) throws BlockReadException {
		byte[] bytes = new byte[itemLength];

		int index = 0;
		int writeSize = bytes.length;
		for (Integer blockId : blockIds) {
			try {
				synchronized (this.blockFileWriter) {
					this.blockFileWriter.set(blockId * this.blockSize);

					this.blockFileWriter.readBytes(bytes, (index++) * this.blockSize, writeSize > this.blockSize ? this.blockSize : writeSize);
				}

				writeSize -= this.blockSize;
			} catch (IOException e) {
				throw new BlockReadException(e, "Can't read blocks.");
			}
		}

		return bytes;
	}
}
