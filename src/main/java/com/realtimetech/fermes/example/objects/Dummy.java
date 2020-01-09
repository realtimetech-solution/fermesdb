package com.realtimetech.fermes.example.objects;

import com.realtimetech.fermes.database.item.Item;

public class Dummy implements Item {
	private String dummyString;

	public Dummy(int size) {
		this.dummyString = new String(new byte[size]);
	}

	public String getDummyString() {
		return dummyString;
	}

	public void setDummyString(String dummyString) {
		this.dummyString = dummyString;
	}
}