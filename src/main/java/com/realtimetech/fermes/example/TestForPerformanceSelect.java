package com.realtimetech.fermes.example;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.example.objects.DummyManager;
import com.realtimetech.fermes.example.objects.ThreadWorkSelect;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForPerformanceSelect {
	

	public static void main(String[] args) throws FermesDatabaseException, PageIOException, InterruptedException {
		Database database;
		database = FermesDB.get(new File("performance_db/"), 8192, 256, Long.MAX_VALUE);

		List<Thread> threads = new LinkedList<Thread>();

		int volume = 10000000;
		int threadCount = 4;
		int managerCount = 4;

		for (int i = 0; i < threadCount; i++) {
			threads.add(new Thread(new ThreadWorkSelect(database.getLink("dummy_manager" + (i % managerCount), () -> new DummyManager()), volume / threadCount)));
		}

		Long startTime = System.currentTimeMillis();
		for (Thread thread : threads) {
			thread.start();
		}

		for (Thread thread : threads) {
			thread.join();
		}

		System.out.println("총 " + volume + "건 레코드 Select 소요시간: " + (System.currentTimeMillis() - startTime));
		System.out.println("스레드 수: " + threadCount);

	}
}
