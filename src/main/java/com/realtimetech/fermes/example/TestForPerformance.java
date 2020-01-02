package com.realtimetech.fermes.example;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.item.Items;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForPerformance {
	public static class DummyManager extends Items<Dummy> {
	}

	public static class Dummy implements Item {
		private byte[] dummys;

		public Dummy(int size) {
			this.dummys = new byte[size];
		}

		public byte[] getDummys() {
			return dummys;
		}
	}

	public static class ThreadWork implements Runnable {
		private Link<DummyManager> dummyManager;
		private int volume;

		public ThreadWork(Link<DummyManager> dummyManager, int volume) {
			this.dummyManager = dummyManager;
			this.volume = volume;
		}

		@Override
		public void run() {
			for(int i = 0; i < volume; i++) {
				try {
					dummyManager.get().addItem(new Dummy(100));
				} catch (PageIOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws FermesDatabaseException, PageIOException, InterruptedException {
		Database database;
		database = FermesDB.get(new File("performance_db/"), 8192, 256, Long.MAX_VALUE);

		Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

		List<Thread> threads = new LinkedList<Thread>();
		
		
		int volume = 1000000;
		int threadCount = 8;
		
		for (int i = 0; i < threadCount; i++) {
			threads.add(new Thread(new ThreadWork(dummyManager, volume / threadCount)));
		}
		
		Long startTime = System.currentTimeMillis();
		for(Thread thread : threads) {
			thread.start();
		}
		
		for(Thread thread : threads) {
			thread.join();
		}
		
		System.out.println("총 " + volume + "건 레코드 Insert 소요시간: " + (System.currentTimeMillis() - startTime));
		System.out.println("스레드 수: " + threadCount);

		database.save();
	}
}
