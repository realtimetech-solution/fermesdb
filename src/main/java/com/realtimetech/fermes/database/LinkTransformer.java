package com.realtimetech.fermes.database;

import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.kson.KsonContext;
import com.realtimetech.kson.transform.Transformer;

public class LinkTransformer implements Transformer<Link<? extends Item>> {
	private Database database;

	public LinkTransformer(Database database) {
		this.database = database;
	}

	@Override
	public Object serialize(KsonContext ksonContext, Link<? extends Item> value) {
		return value.gid;
	}

	@Override
	public Link<? extends Item> deserialize(KsonContext ksonContext, Class<?> object, Object value) {
		return database.getLinkByGid((long) value);
	}
}