package com.realtimetech.fermes.example.objects;

import com.realtimetech.fermes.database.Link;

public class ThreadWorkSelect implements Runnable {
	private Link<DummyManager> dummyManager;
	private int volume;

	public ThreadWorkSelect(Link<DummyManager> dummyManager, int volume) {
		this.dummyManager = dummyManager;
		this.volume = volume;
	}

	@Override
	public void run() {
		int count = volume;

		for (Link<Dummy> dummy : dummyManager.get().getDummyItems()) {
			if ((count--) == 0) {
				break;
			}
			
			dummy.get();
		}
	}
}