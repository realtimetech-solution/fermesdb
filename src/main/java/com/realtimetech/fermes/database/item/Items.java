package com.realtimetech.fermes.database.item;

import java.util.Collection;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.page.exception.PageIOException;

@SuppressWarnings("unchecked")
public abstract class Items<T extends Item> extends SelfItem<T> {
	public Link<T> addItem(T item) throws PageIOException {
		return this.current().createChildLink(item);
	}

	public boolean removeItem(Link<T> link) throws PageIOException {
		return this.current().removeChildLink(link);
	}

	public Link<T> getItemByGid(long gid) throws PageIOException {
		return (Link<T>) this.current().getDatabase().getLinkByGid(gid);
	}

	public Link<T> getItem(int index) throws PageIOException {
		return (Link<T>) this.current().getChildLinkItem(index);
	}

	public Collection<Long> getItemGids() {
		return this.current().getChildLinks();
	}

	public int getItemCount() {
		return this.current().getChildLinks().size();
	}

	@Deprecated
	public Link<T>[] getItemArray() {
		Collection<Long> childLinks = this.current().getChildLinks();

		synchronized (childLinks) {
			Link<T>[] items = new Link[childLinks.size()];

			int index = 0;

			for (Long gid : childLinks) {
				items[index] = (Link<T>) this.current().getDatabase().getLinkByGid(gid);
			}

			return items;
		}
	}
}
