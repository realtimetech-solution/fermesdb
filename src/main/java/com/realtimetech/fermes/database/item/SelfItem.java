package com.realtimetech.fermes.database.item;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.kson.annotation.Ignore;

public abstract class SelfItem<T extends Item> implements Item {
	@Ignore
	private Link<? extends Item> currentLink;

	@Override
	public void onLoad(Link<? extends Item> currentLink) {
		this.currentLink = currentLink;
	}

	public Link<? extends Item> current() {
		return currentLink;
	}
}
