package com.realtimetech.fermes.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

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
			length += 8;

			length += 4;

			length += value.childLinks.size() * 8;

			length += value.blockIds.size() * 4;

			return length;
		}

		@Override
		public void onWrite(Link<? extends Item> value, MemoryFileWriter pageBuffer) throws IOException {
			pageBuffer.writeLong(value.gid);
			pageBuffer.writeLong(value.parentLink);
			pageBuffer.writeLong(value.childLinks.size());
			pageBuffer.writeLong(value.blockIds.size());

			pageBuffer.writeInteger(value.itemLength);

			for (Long linkGid : value.childLinks) {
				pageBuffer.writeLong(linkGid);
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
			long blockIdCount = pageBuffer.readLong();

			int itemLength = pageBuffer.readInteger();

			value.page = page;
			value.parentLink = parentLink;
			value.gid = gid;

			for (int index = 0; index < childLinkCount; index++) {
				value.childLinks.add(pageBuffer.readLong());
			}

			for (int index = 0; index < blockIdCount; index++) {
				value.blockIds.add(pageBuffer.readInteger());
			}

			value.itemLength = itemLength;
		}
	}

	protected Link<? extends Item> nextObject;
	protected Link<? extends Item> prevObject;
	protected boolean accessed;

	private Page page;

	protected R item;

	protected long gid;

	protected long parentLink;

	protected LinkedList<Long> childLinks;

	protected ArrayList<Integer> blockIds;

	protected int itemLength;

	public Link(Database database) {
		super(database);

		this.item = null;
		this.itemLength = -1;

		this.childLinks = new LinkedList<Long>();
		this.blockIds = new ArrayList<Integer>();
	}

	public Link(Database database, Page page, long parentGid, long gid) {
		this(database);

		this.page = page;
		this.gid = gid;
		this.parentLink = parentGid;
	}

	public Collection<Long> getChildLinks() {
		synchronized (this.childLinks) {
			return childLinks;
		}
	}

	protected Page getPage() {
		return page;
	}

	protected long getGid() {
		return gid;
	}

	public boolean isLoaded() {
		return item != null;
	}

	public R get() {
		this.accessed = true;

		if (this.item == null) {
			synchronized (this) {
				try {
					this.getDatabase().loadLink(this);
				} catch (BlockIOException | FermesItemException e) {
					e.printStackTrace();

					// TODO CRASHED DB...
				}
			}
		}

		return item;
	}

	public Link<? extends Item> getChildLinkItem(int index) throws PageIOException {
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
