package com.realtimetech.fermes.example;

import java.io.File;
import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.example.objects.DummyManager;
import com.realtimetech.fermes.example.objects.ThreadWorkCreate;
import com.realtimetech.fermes.example.objects.ThreadWorkSelect;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForSaveLock {
	public static void main(String[] args) throws FermesDatabaseException, PageIOException, FermesItemException, InterruptedException {
		Thread.sleep(15000);
		Database database;
		File databaseDirectory = new File("save_lock_db/");
		FermesDB.deleteDatabase(databaseDirectory);
		database = FermesDB.get(databaseDirectory, 65536, 512, Long.MAX_VALUE);

		int volume = 10000;

		Thread insertThread = new Thread(new ThreadWorkCreate(database.getLink("dummy_manager", () -> new DummyManager()), volume));
		Thread selectThread = new Thread(new ThreadWorkSelect(database.getLink("dummy_manager", () -> new DummyManager()), volume));
		
		System.out.println("Insert 시작!");
		insertThread.start();
		insertThread.join();
		System.out.println("Insert 종료!");

		Long startTime = System.currentTimeMillis();
		System.out.println("Select 시작!");
		selectThread.start();
		System.out.println("Save 시작!");
		database.save();
		selectThread.join();
		System.out.println("Select 종료!");
		Long endTime = System.currentTimeMillis();
		
		
		System.out.println("Time: " + (endTime - startTime));
	}
}
