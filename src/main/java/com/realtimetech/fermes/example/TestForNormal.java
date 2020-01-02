package com.realtimetech.fermes.example;

import java.io.File;
import java.util.LinkedList;
import java.util.Random;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.page.Page;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.example.objects.Dummy;
import com.realtimetech.fermes.example.objects.DummyManager;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class TestForNormal {
	public static void main(String[] args) throws FermesDatabaseException, PageIOException, FermesItemException {
		int loop = 32;
		int size = 256;

		Random random = new Random();
		int seed = random.nextInt(400) + 10;
		{
			Database database;
			database = FermesDB.get(new File("normal_db/"), 128, 32, Long.MAX_VALUE);

			Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

			for (int index = 0; index < loop; index++) {

				for (int i = 0; i < size; i++) {
					dummyManager.get().addItem(new Dummy(seed));
				}
				System.out.println("******* Creation " + index + " batch done!");
			}

			System.out.println("Memory " + database.getCurrentMemory());
			database.save();
			System.out.println("Saved! Memory " + database.getCurrentMemory());
		}

		{

			Database database;
			database = FermesDB.get(new File("normal_db/"), 128, 32, 1024);

			Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

			for (int index = 0; index < loop; index++) {
				for (Long gid : dummyManager.getChildLinks()) {
					if (dummyManager.get().getItemByGid(gid).get().getDummys().length != seed) {
						System.err.println("틀린데?");
					}
				}
				System.out.println("******* Select " + index + " batch done!");
			}

			for (Long gid : new LinkedList<Long>(dummyManager.getChildLinks())) {
				dummyManager.get().removeItem(dummyManager.get().getItemByGid(gid));
			}
			System.out.println("******* Remove all batch done!");
			System.out.println("Memory " + database.getCurrentMemory());
			database.save();
			System.out.println("Saved! Memory " + database.getCurrentMemory());
		}
		{

			Database database;
			database = FermesDB.get(new File("normal_db/"), 128, 32, 1024);

			Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

			for (int index = 0; index < loop; index++) {
				if(dummyManager.get().getItemCount() != 0) {
					System.err.println("있는데?");
				}
			}
			System.out.println("******* Validation done!");
			
			for(Page page : database.getPages()) {
				System.out.println(" --- " + page.getId() +  "_page length " + page.getLinks().length);
				int linkId = 0;
				for(Link<? extends Item> link : page.getLinks()) {
					if(link != null) {
						System.err.println("\t" + linkId + "(" + link.getGid() + ") is not null!");
					}
				}
			}

			System.out.println("Memory " + database.getCurrentMemory());
			database.save();
			System.out.println("Saved! Memory " + database.getCurrentMemory());
			
		}
	}
}
