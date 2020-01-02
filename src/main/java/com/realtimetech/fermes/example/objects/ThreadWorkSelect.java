package com.realtimetech.fermes.example.objects;

import java.util.Random;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.page.exception.PageIOException;

public class ThreadWorkSelect implements Runnable {
	private Link<DummyManager> dummyManager;
	private int volume;

	public ThreadWorkSelect(Link<DummyManager> dummyManager, int volume) {
		this.dummyManager = dummyManager;
		this.volume = volume;
	}

	@Override
	public void run() {
		Random random = new Random();

		for (int i = 0; i < volume; i++) {
			try {
				int itemCount = dummyManager.get().getItemCount();

				try {
					if (itemCount == 0) {
						i--;
						System.out.println("데이터가 없어서 롤백..!");
					} else {
						DummyManager dummyManager2 = dummyManager.get();
						try {
							int nextInt = random.nextInt(itemCount);
							Link<Dummy> itemByGid = dummyManager2.getItem(nextInt);
							Dummy dummy = itemByGid.get();
							dummy.getDummys();
						} catch (NullPointerException e) {
							e.printStackTrace();

						}
					}
				} catch (PageIOException e) {
					e.printStackTrace();
				}
			} catch (FermesItemException e) {
				e.printStackTrace();
			}
		}
	}
}