package com.realtimetech.fermes.example.objects;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.link.exception.LinkCreateException;

public class ThreadWorkCreateInfinity implements Runnable {
	private Link<DummyManager> dummyManager;

	public ThreadWorkCreateInfinity(Link<DummyManager> dummyManager) {
		this.dummyManager = dummyManager;
	}

	@Override
	public void run() {
		while (true) {
			try {
				dummyManager.get().addItem(new Dummy(512));
			} catch (LinkCreateException e) {
				e.printStackTrace();
			}
		}
	}
}