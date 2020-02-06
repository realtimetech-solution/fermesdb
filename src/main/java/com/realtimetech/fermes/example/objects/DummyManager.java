package com.realtimetech.fermes.example.objects;

import java.util.Collection;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.item.Items;
import com.realtimetech.fermes.database.link.exception.LinkCreateException;
import com.realtimetech.fermes.database.link.exception.LinkRemoveException;

public class DummyManager extends Items<Dummy> {
	public Link<Dummy> addDummy(Dummy item) throws LinkCreateException {
		return super.addItem(item);
	}

	public boolean removeDummy(Link<Dummy> link) throws LinkRemoveException {
		return super.removeItem(link);
	}

	public Link<Dummy> getDummy(int index) {
		return super.getItem(index);
	}

	public Link<Dummy> getDummyByGid(long gid) {
		return super.getItemByGid(gid);
	}

	public Collection<Long> getDummyGids() {
		return super.getItemGids();
	}

	public int getDummyCount() {
		return super.getItemCount();
	}
}