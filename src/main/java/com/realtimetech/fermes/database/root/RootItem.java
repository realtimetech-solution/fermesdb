package com.realtimetech.fermes.database.root;

import java.util.HashMap;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.page.exception.PageIOException;

public class RootItem implements Item {
	@FunctionalInterface
	public static interface ItemCreator<T extends Item> {
		public T build();
	}

	private Link<? extends Item> currentLink;
	private HashMap<String, Link<? extends Item>> linkMap;

	public RootItem() {
		this.linkMap = new HashMap<String, Link<? extends Item>>();
	}

	@SuppressWarnings("unchecked")
	public <T extends Item> Link<T> getLink(String name, ItemCreator<T> creator) throws PageIOException {
		if (!linkMap.containsKey(name)) {
			linkMap.put(name, this.currentLink.createChildLink(creator.build()));
		}

		return (Link<T>) linkMap.get(name);
	}

	@Override
	public void onCreate(Link<? extends Item> link) {
	}

	@Override
	public void onLoad(Link<? extends Item> link) {
		this.currentLink = link;
	}
}
