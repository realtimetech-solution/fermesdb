package com.realtimetech.fermes.example;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.example.objects.DummyManager;
import com.realtimetech.fermes.example.objects.ThreadWorkCreate;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForPerformanceInsert {
	public static void main(String[] args) throws FermesDatabaseException, PageIOException, InterruptedException, FermesItemException {
		File databaseDirectory = new File("performance_db/");
		FermesDB.deleteDatabase(databaseDirectory);
		
		Database database;
		database = FermesDB.get(databaseDirectory, 65536, 512, Long.MAX_VALUE);

		List<Thread> threads = new LinkedList<Thread>();

		int volume = 1000000;
		int threadCount = 8;
		int managerCount = 4;

		for (int i = 0; i < threadCount; i++) {
			threads.add(new Thread(new ThreadWorkCreate(database.getLink("dummy_manager" + (i % managerCount), () -> new DummyManager()), volume / threadCount)));
		}

		Long startTime = System.currentTimeMillis();
		for (Thread thread : threads) {
			thread.start();
		}

		for (Thread thread : threads) {
			thread.join();
		}

		System.out.println("총 " + volume + "건 레코드 Insert 소요시간: " + (System.currentTimeMillis() - startTime));
		System.out.println("스레드 수: " + threadCount);

		database.save();
	}
}
