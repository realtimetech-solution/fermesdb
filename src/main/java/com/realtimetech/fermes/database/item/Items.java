package com.realtimetech.fermes.database.item;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

	public Link<T> getItem(int index) throws PageIOException {
		return (Link<T>) this.currentLink.getDatabase().getLinkByGid(this.currentLink.getChildLinks().get(index));
	}

	public List<Long> getItems() {
		return new ArrayList<Long>(this.currentLink.getChildLinks());
	}

	public int getItemCount() {
		return this.currentLink.getChildLinks().size();
	}

	@Deprecated
	public Link<T>[] getItemArray() {
		LinkedList<Long> childLinks = this.currentLink.getChildLinks();
		
		synchronized (childLinks) {
			Link<T>[] items = new Link[childLinks.size()];

			for (int index = 0; index < childLinks.size(); index++) {
				items[index] = (Link<T>) this.currentLink.getDatabase().getLinkByGid(childLinks.get(index));
			}

			return items;
		}
	}
}
