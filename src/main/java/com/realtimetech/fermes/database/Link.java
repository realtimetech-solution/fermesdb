package com.realtimetech.fermes.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.io.StoreSerializable;
import com.realtimetech.fermes.database.io.StoreSerializer;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.page.Page;
import com.realtimetech.fermes.database.page.exception.BlockIOException;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.database.page.file.impl.MemoryFileWriter;

public class Link<R extends Item> extends StoreSerializable {
	public static class LinkSerializer extends StoreSerializer<Link<? extends Item>> {
		private Page page;

		public LinkSerializer(Database database, Page page) {
			super(database);

			this.page = page;
		}

		@Override
		public long getWriteLength(Link<? extends Item> value) {
			long length = 0;

			length += 8;
			length += 8; 
			length += 8;

			length += 4;
			length += 4;

			length += value.childLinks == null ? 0 : value.childLinks.size() * 8;

			length += value.blockIds.length * 4;

			return length;
		}

		@Override
		public void onWrite(Link<? extends Item> value, MemoryFileWriter pageBuffer) throws IOException {
			pageBuffer.writeLong(value.gid);
			pageBuffer.writeLong(value.parentLink);

			if (value.childLinks == null) {
				pageBuffer.writeLong(0);
			} else {
				pageBuffer.writeLong(value.childLinks.size());
			}

			pageBuffer.writeInteger(value.blockIds.length);
			pageBuffer.writeInteger(value.itemLength);

			if (value.childLinks != null) {
				for (Long linkGid : value.childLinks) {
					pageBuffer.writeLong(linkGid);
				}
			}

			for (Integer blockId : value.blockIds) {
				pageBuffer.writeInteger(blockId);
			}
		}

		@Override
		public void onRead(Link<? extends Item> value, MemoryFileWriter pageBuffer) throws IOException {
			long gid = pageBuffer.readLong();
			long parentLink = pageBuffer.readLong();
			long childLinkCount = pageBuffer.readLong();

			int blockIdLength = pageBuffer.readInteger();
			int itemLength = pageBuffer.readInteger();

			value.page = page;
			value.parentLink = parentLink;
			value.gid = gid;

			for (int index = 0; index < childLinkCount; index++) {
				value.createChildLinksIfNotExist();
				value.childLinks.add(pageBuffer.readLong());
			}

			value.blockIds = new int[(int) blockIdLength];
			for (int index = 0; index < blockIdLength; index++) {
				value.blockIds[index] = pageBuffer.readInteger();
			}

			value.itemLength = itemLength;
		}
	}

	protected Link<? extends Item> nextObject;
	protected Link<? extends Item> prevObject;
	
	protected boolean accessed;
	protected boolean removed = false;
	protected boolean froze = false;

	private Page page;

	protected R item;

	protected long gid;
	protected long parentLink;

	protected ArrayList<Long> childLinks;

	protected int[] blockIds;

	protected int itemLength;

	public Link(Database database) {
		super(database);

		this.item = null;
		this.itemLength = -1;

		this.childLinks = null;
		this.blockIds = new int[0];
	}

	public Link(Database database, Page page, long parentGid, long gid) {
		this(database);

		this.page = page;
		this.gid = gid;
		this.parentLink = parentGid;
	}

	public Collection<Long> getChildLinks() {
		this.createChildLinksIfNotExist();

		synchronized (this.childLinks) {
			return childLinks;
		}
	}

	protected void createChildLinksIfNotExist() {
		if (this.childLinks == null) {
			synchronized (this) {
				if (this.childLinks != null) {
					return; // DOUBLE CHECK
				}

				this.childLinks = new ArrayList<Long>();
			}
		}
	}

	protected Page getPage() {
		return page;
	}

	public long getGid() {
		return gid;
	}

	public boolean isLoaded() {
		return item != null;
	}

	public synchronized void lock() {
		this.froze = true;
	}

	public synchronized void unlock() {
		this.froze = false;
	}

	public R get() throws FermesItemException {
		if (this.removed) {
			throw new FermesItemException("Can't access this link, already removed!");
		}

		synchronized (this) {
			this.accessed = true;

			if (!isLoaded()) {
				try {
					this.getDatabase().loadLink(this);
				} catch (BlockIOException | FermesItemException e) {
					e.printStackTrace();
					// TODO CRASHED DB...
				}
			}

			this.accessed = true;

			return item;
		}
	}

	public Link<? extends Item> getChildLinkItem(int index) throws PageIOException {
		if (this.childLinks == null) {
			return null;
		}

		synchronized (this.childLinks) {
			return (Link<? extends Item>) this.getDatabase().getLinkByGid(this.childLinks.get(index));
		}
	}

	public <V extends Item> Link<V> createChildLink(V item) throws PageIOException {
		return getDatabase().createLink(this, item);
	}

	public boolean removeChildLink(Link<? extends Item> link) throws PageIOException {
		return getDatabase().removeLink(link);
	}
}
