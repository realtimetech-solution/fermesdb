package com.realtimetech.fermes.database.item;

import java.util.Collection;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.kson.annotation.Ignore;

@SuppressWarnings("unchecked")
public abstract class Items<T extends Item> implements Item {
	@Ignore
	private Link<? extends Item> currentLink;

	@Override
	public void onLoad(Link<? extends Item> currentLink) {
		this.currentLink = currentLink;
	}

	public Link<T> addItem(T item) throws PageIOException {
		return this.currentLink.createChildLink(item);
	}

	public boolean removeItem(Link<T> link) throws PageIOException {
		return this.currentLink.removeChildLink(link);
	}

	public Link<T> getItemByGid(long gid) throws PageIOException {
		return (Link<T>) this.currentLink.getDatabase().getLinkByGid(gid);
	}

	public Link<T> getItem(int index) throws PageIOException {
		return (Link<T>) this.currentLink.getChildLinkItem(index);
	}

	public Collection<Long> getItemGids() {
		return this.currentLink.getChildLinks();
	}

	public int getItemCount() {
		return this.currentLink.getChildLinks().size();
	}

	@Deprecated
	public Link<T>[] getItemArray() {
		Collection<Long> childLinks = this.currentLink.getChildLinks();
		
		synchronized (childLinks) {
			Link<T>[] items = new Link[childLinks.size()];

			int index = 0;
			
			for (Long gid : childLinks) {
				items[index] = (Link<T>) this.currentLink.getDatabase().getLinkByGid(gid);
			}

			return items;
		}
	}
}
