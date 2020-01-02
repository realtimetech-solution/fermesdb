package com.realtimetech.fermes.example;

import java.io.File;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.item.Items;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForMemory {
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

	public static void main(String[] args) throws FermesDatabaseException, PageIOException {
		Database database;
		database = FermesDB.get(new File("memory_db/"), 1024, 128, 1024);

		Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

		dummyManager.get().addItem(new Dummy(100));
		System.out.println(database.getCurrentMemory());
		dummyManager.get().addItem(new Dummy(100));
		System.out.println(database.getCurrentMemory());

		database.save();
	}
}
