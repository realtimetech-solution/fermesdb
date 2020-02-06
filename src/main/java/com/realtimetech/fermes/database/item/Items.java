package com.realtimetech.fermes.database.item;

import java.util.Collection;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.link.exception.LinkCreateException;
import com.realtimetech.fermes.database.link.exception.LinkRemoveException;

@SuppressWarnings("unchecked")
public abstract class Items<T extends Item> extends SelfItem<T> {
	protected Link<T> addItem(T item) throws LinkCreateException {
		return this.current().createChildLink(item);
	}

	protected boolean removeItem(Link<T> link) throws LinkRemoveException {
		return this.current().removeChildLink(link);
	}

	protected Link<T> getItemByGid(long gid) {
		return (Link<T>) this.current().getDatabase().getLinkByGid(gid);
	}

	protected Link<T> getItem(int index) {
		return (Link<T>) this.current().getChildLinkItem(index);
	}

	protected Collection<Long> getItemGids() {
		return this.current().getChildLinks();
	}

	protected int getItemCount() {
		return this.current().getChildLinks().size();
	}

	@Deprecated
	protected Link<T>[] getItemArray() {
		Collection<Long> childLinks = this.current().getChildLinks();

		synchronized (childLinks) {
			Link<T>[] items = new Link[childLinks.size()];

			int index = 0;

			for (Long gid : childLinks) {
				items[index] = (Link<T>) this.current().getDatabase().getLinkByGid(gid);
				index++;
			}

			return items;
		}
	}
}
