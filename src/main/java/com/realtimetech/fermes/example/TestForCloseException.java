package com.realtimetech.fermes.example;

import java.io.File;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.example.objects.Dummy;
import com.realtimetech.fermes.example.objects.DummyManager;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForCloseException {
	public static void main(String[] args) throws FermesDatabaseException, PageIOException, FermesItemException {

		FermesDB.deleteDatabase(new File("close_db/"));
		Database database;
		database = FermesDB.get(new File("close_db/"), 1024, 512, Long.MAX_VALUE);

		Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

		dummyManager.get().addItem(new Dummy(1234));
		dummyManager.get().addItem(new Dummy(1234));

		System.out.println(dummyManager.get().getItem(0).get().getDummyString().length());

		database.save();

		System.out.println(dummyManager.get().getItem(0).get().getDummyString().length());
		dummyManager.get().getItem(0).get().setDummyString("123");
		System.out.println(dummyManager.get().getItem(0).get().getDummyString().length());

		database.close();

		try {
			System.out.println(dummyManager.get().getItem(0).get().getDummyString().length());
		} catch (Exception e) {
			System.out.println("OK!");
		}

		database = FermesDB.get(new File("close_db/"), 1024, 512, Long.MAX_VALUE);
		dummyManager = database.getLink("dummy_manager", () -> new DummyManager());
		System.out.println(dummyManager.get().getItem(0).get().getDummyString());

	}
}
