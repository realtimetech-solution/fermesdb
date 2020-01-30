package com.realtimetech.fermes.example;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.example.objects.DummyManager;
import com.realtimetech.fermes.example.objects.ThreadWorkCreateInfinity;

public class TestForMemoryLock {
	public static void main(String[] args) throws Exception {
		Database database;
		File databaseDirectory = new File("memory_lock_db/");
		FermesDB.deleteDatabase(databaseDirectory);
		database = FermesDB.get(databaseDirectory, 128, 512, Long.MAX_VALUE);

		List<Thread> threads = new LinkedList<Thread>();

		int threadCount = 4;
		int managerCount = 4;

		for (int i = 0; i < threadCount; i++) {
			threads.add(new Thread(new ThreadWorkCreateInfinity(database.getLink("dummy_manager" + (i % managerCount), () -> new DummyManager()))));
		}
		for (Thread thread : threads) {
			thread.start();
		}
		while (true) {
			Thread.sleep(3000);

			System.out.println(database.getCurrentMemory() + "bytes used");

			System.gc();
		}
	}
}
