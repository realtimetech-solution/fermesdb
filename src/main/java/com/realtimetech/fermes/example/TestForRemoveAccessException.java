package com.realtimetech.fermes.example;

import java.io.File;
import java.util.Random;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.example.objects.Dummy;
import com.realtimetech.fermes.example.objects.DummyManager;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForRemoveAccessException {
	public static void main(String[] args) throws FermesDatabaseException, PageIOException, FermesItemException {

		Random random = new Random();
		int seed = random.nextInt(400) + 10;
		{
			FermesDB.deleteDatabase(new File("exception1_db/"));
			Database database;
			database = FermesDB.get(new File("exception1_db/"), 1024, 512, Long.MAX_VALUE);

			Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

			Link<Dummy> dummy = dummyManager.get().addItem(new Dummy(seed));
			
			dummyManager.removeChildLink(dummy);
			
			dummy.get();
			
			System.out.println("Saved! Memory " + database.getCurrentMemory());
		}

	}
}
