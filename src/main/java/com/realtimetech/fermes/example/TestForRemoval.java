package com.realtimetech.fermes.example;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.item.Items;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForRemoval {
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
		database = FermesDB.get(new File("removal_db/"), 1024, 128, 1024);

		Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

		List<Link<Dummy>> dummies = new LinkedList<Link<Dummy>>();

		int loop = 4;
		int step = 2;
		int size = 128;

		for (int index = 0; index < loop; index++) {
			for (int stepIndex = 0; stepIndex < step; stepIndex++) {
				Random random = new Random();
				int seed = random.nextInt(400) + 10;

				for (int i = 0; i < size; i++) {
					dummyManager.get().addItem(new Dummy(seed));
				}

				for (Link<Dummy> dummy : dummies) {
					if(dummy.get().getDummys().length != seed) {
						System.out.println("틀렸는데!?");
					}
				}

				for (Link<Dummy> dummy : dummies) {
					dummyManager.get().removeItem(dummy);
				}

				dummies.clear();
			}
		}

		database.save();
	}
}
