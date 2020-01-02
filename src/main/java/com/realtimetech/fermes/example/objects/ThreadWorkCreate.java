package com.realtimetech.fermes.example.objects;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.page.exception.PageIOException;

public class ThreadWorkCreate implements Runnable {
	private Link<DummyManager> dummyManager;
	private int volume;

	public ThreadWorkCreate(Link<DummyManager> dummyManager, int volume) {
		this.dummyManager = dummyManager;
		this.volume = volume;
	}

	@Override
	public void run() {
		for (int i = 0; i < volume; i++) {
			try {
				dummyManager.get().addItem(new Dummy(100));
			} catch (PageIOException e) {
				e.printStackTrace();
			}
		}
	}
}