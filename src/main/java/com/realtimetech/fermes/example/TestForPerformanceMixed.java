package com.realtimetech.fermes.example;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.FermesDB;
import com.realtimetech.fermes.example.objects.DummyManager;
import com.realtimetech.fermes.example.objects.ThreadWorkCreate;
import com.realtimetech.fermes.example.objects.ThreadWorkSelect;

public class TestForPerformanceMixed {
	

	public static void main(String[] args) throws Exception {
		Database database;
		File databaseDirectory = new File("performance_mixed_db/");

		FermesDB.deleteDatabase(databaseDirectory);
		
		database = FermesDB.get(databaseDirectory, 8192, 512, Long.MAX_VALUE);
 
		List<Thread> threads = new LinkedList<Thread>();

		int volume = 10000000;
		int threadCount = 16;
		int managerCount = 4;

		int localThreadCount = threadCount / 2;
		for (int i = 0; i < localThreadCount; i++) {
			threads.add(new Thread(new ThreadWorkCreate(database.getLink("dummy_manager" + (i % managerCount), () -> new DummyManager()), volume / localThreadCount)));
		}
		for (int i = 0; i < localThreadCount; i++) {
			threads.add(new Thread(new ThreadWorkSelect(database.getLink("dummy_manager" + (i % managerCount), () -> new DummyManager()), volume / localThreadCount)));
		}

		Long startTime = System.currentTimeMillis();
		for (Thread thread : threads) {
			thread.start();
		}

		for (Thread thread : threads) {
			thread.join();
		}

		System.out.println("총 " + volume + "건 레코드 Select/Insert 소요시간: " + (System.currentTimeMillis() - startTime));
		System.out.println("스레드 수: " + threadCount);

	}
}
