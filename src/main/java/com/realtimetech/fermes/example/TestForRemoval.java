package com.realtimetech.fermes.example;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.example.objects.Dummy;
import com.realtimetech.fermes.example.objects.DummyManager;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForRemoval {
	public static void main(String[] args) throws FermesDatabaseException, PageIOException, FermesItemException {
		Database database;
		database = FermesDB.get(new File("removal_db/"), 128, 32, 1024);

		Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

		List<Link<Dummy>> dummies = new LinkedList<Link<Dummy>>();

		int loop = 32;
		int step = 4;
		int size = 256;

		for (int index = 0; index < loop; index++) {
			for (int stepIndex = 0; stepIndex < step; stepIndex++) {
				Random random = new Random();
				int seed = random.nextInt(400) + 10;

				for (int i = 0; i < size; i++) {
					dummies.add(dummyManager.get().addItem(new Dummy(seed)));
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
