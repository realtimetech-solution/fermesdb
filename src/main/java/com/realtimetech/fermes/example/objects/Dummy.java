package com.realtimetech.fermes.example.objects;

import com.realtimetech.fermes.database.item.Item;

public class Dummy implements Item {
	private byte[] dummys;

	public Dummy(int size) {
		this.dummys = new byte[size];
	}

	public byte[] getDummys() {
		return dummys;
	}
}