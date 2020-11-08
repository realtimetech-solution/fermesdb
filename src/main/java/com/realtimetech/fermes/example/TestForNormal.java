package com.realtimetech.fermes.example;

import java.io.File;
import java.util.Random;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.FermesDB;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.example.objects.Dummy;
import com.realtimetech.fermes.example.objects.DummyManager;

public class TestForNormal {
	public static void main(String[] args) throws Exception {
		int loop = 32;
		int size = 256;

		Random random = new Random();
		int seed = random.nextInt(400) + 10;
		File databaseDirectory = new File("normal_db/");
		FermesDB.deleteDatabase(databaseDirectory);
		{
			Database database;
			database = FermesDB.get(databaseDirectory, 128, 512, Long.MAX_VALUE);

			Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

			dummyManager.get().setModifyTester("A");
			
			for (int index = 0; index < loop; index++) {

				for (int i = 0; i < size; i++) {
					dummyManager.get().addDummy(new Dummy(seed));
				}
				System.out.println("******* Creation " + index + " batch done!");
			}

			System.out.println("Memory " + database.getCurrentMemory());
			database.save();
			System.out.println("Saved! Memory " + database.getCurrentMemory());
		}

		{

			Database database;
			database = FermesDB.loadDatabase(databaseDirectory);

			Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());
			
			if(!dummyManager.get().getModifyTester().equals("A")){
				System.out.println("틀려");
			}
			dummyManager.get().setModifyTester("B");

			for (int index = 0; index < loop; index++) {
				for (Long gid : dummyManager.getChildLinks()) {
					if (dummyManager.get().getDummyByGid(gid).get().getDummyString().length() != seed) {
						System.err.println("틀린데?");
					}
				}
				System.out.println("******* Select " + index + " batch done!");
			}

			for (Long gid : dummyManager.getChildLinks()) {
				dummyManager.get().removeDummy(dummyManager.get().getDummyByGid(gid));
			}
			System.out.println("******* Remove all batch done!");
			System.out.println("Memory " + database.getCurrentMemory());
			database.save();
			System.out.println("Saved! Memory " + database.getCurrentMemory());
		}
		{

			Database database;
			database = FermesDB.loadDatabase(databaseDirectory);

			Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

			if(!dummyManager.get().getModifyTester().equals("B")){
				System.out.println("틀려2222");
			}
			for (int index = 0; index < loop; index++) {
				if(dummyManager.get().getDummyCount() != 0) {
					System.err.println("있는데?");
				}
			}
			System.out.println("******* Validation done!");
			
//			for(Page page : database.getPages()) {
//				System.out.println(" --- " + page.getId() +  "_page length " + page.getLinks().length);
//				int linkId = 0;
//				for(Link<? extends Item> link : page.getLinks()) {
//					if(link != null) {
//						System.err.println("\t" + linkId + "(" + link.getGid() + ") is not null!");
//					}
//				}
//			}

			System.out.println("Memory " + database.getCurrentMemory());
			database.save();
			System.out.println("Saved! Memory " + database.getCurrentMemory());
			
		}
	}
}
