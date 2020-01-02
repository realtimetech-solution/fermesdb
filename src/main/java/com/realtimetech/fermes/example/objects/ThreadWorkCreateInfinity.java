package com.realtimetech.fermes.example.objects;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.page.exception.PageIOException;

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
			} catch (PageIOException e) {
				e.printStackTrace();
			} catch (FermesItemException e) {
				e.printStackTrace();
			}
		}
	}
}