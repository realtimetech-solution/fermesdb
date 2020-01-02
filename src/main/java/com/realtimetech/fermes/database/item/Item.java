package com.realtimetech.fermes.database.item;

import com.realtimetech.fermes.database.Link;

public interface Item {
	default public void onCreate(Link<? extends Item> link) {
		
	};

	default public void onLoad(Link<? extends Item> link) {
		
	};
}
