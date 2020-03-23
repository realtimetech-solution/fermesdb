package com.realtimetech.fermes.example;

import java.io.File;
import java.util.Random;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.FermesDB;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.example.objects.Dummy;
import com.realtimetech.fermes.example.objects.DummyManager;

public class TestForMemory {
	public static void main(String[] args) throws Exception {
		Database database;
		database = FermesDB.get(new File("memory_db/"), 1024, 512, 1024);

		Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

		while(true) {
			dummyManager.get().addDummy(new Dummy(new Random().nextInt(384) + 128));
			System.out.println(database.getCurrentMemory());
		}
	}
}
