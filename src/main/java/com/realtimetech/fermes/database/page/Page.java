package com.realtimetech.fermes.database.page;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.Link.LinkSerializer;
import com.realtimetech.fermes.database.io.StoreSerializable;
import com.realtimetech.fermes.database.io.StoreSerializer;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.page.exception.BlockIOException;
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

		{
			this.pageFile = new File(this.getDatabase().getDatabaseDirectory().getPath() + File.separator + this.id + ".page");

			if (!this.pageFile.exists()) {
				this.pageFile.createNewFile();
			}
		}

		{
			this.blockFile = new File(this.getDatabase().getDatabaseDirectory().getPath() + File.separator + this.id + ".block");

			if (!this.blockFile.exists()) {
				this.blockFile.createNewFile();
			}
		}

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

	public synchronized void removeLinkByIndex(List<Integer> blockIds, int index) {
		if (this.links[index] != null) {
			for (int blockId : blockIds) {
				this.emptyBlockIds.offer(blockId);
			}

			blockIds.clear();

			this.links[index] = null;
		}
	}

	public File getPageFile() {
		return pageFile;
	}

	public synchronized void writeBlocks(List<Integer> blockIds, byte[] bytes) throws BlockIOException {
		int needIndexies = (int) Math.ceil((float) bytes.length / (float) blockSize);

		int manage = needIndexies - blockIds.size();

		if (manage > 0) {
			for (int index = 0; index < manage; index++) {
				if (!this.emptyBlockIds.isEmpty()) {
					blockIds.add(this.emptyBlockIds.poll());
				} else {
					blockIds.add(this.maxBlockId++);
				}
			}
		} else if (manage < 0) {
			for (int index = 0; index > manage; index--) {
				Integer removeIndex = blockIds.remove(0);

				this.emptyBlockIds.offer(removeIndex);
			}
		}

		int index = 0;
		int writeSize = bytes.length;
		for (Integer blockId : blockIds) {
			try {
				this.blockFileWriter.set(blockId * this.blockSize);

				this.blockFileWriter.writeBytes(bytes, (index++) * this.blockSize, writeSize > this.blockSize ? this.blockSize : writeSize);

				writeSize -= this.blockSize;
			} catch (IOException e) {
				e.printStackTrace();
				
				throw new BlockIOException("Can't write blocks.");
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
			if(this.blockFileWriter instanceof BlockMemoryFileWriter) {
				BlockMemoryFileWriter blockMemoryFileWriter = (BlockMemoryFileWriter) this.blockFileWriter;

				blockMemoryFileWriter.save();

				this.blockFileWriter = this.blockFileWriterRollback;
				this.blockFileWriterRollback = null;
			}
		}
	}

	public synchronized byte[] readBlocks(List<Integer> blockIds, int itemLength) throws BlockIOException {
		byte[] bytes = new byte[itemLength];

		int index = 0;
		int writeSize = bytes.length;
		for (Integer blockId : blockIds) {
			try {
				this.blockFileWriter.set(blockId * this.blockSize);

				this.blockFileWriter.readBytes(bytes, (index++) * this.blockSize, writeSize > this.blockSize ? this.blockSize : writeSize);

				writeSize -= this.blockSize;
			} catch (IOException e) {
				throw new BlockIOException("Can't write blocks.");
			}
		}

		return bytes;
	}
}
