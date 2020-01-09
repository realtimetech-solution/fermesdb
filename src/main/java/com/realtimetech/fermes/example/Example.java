package com.realtimetech.fermes.example;

import java.io.File;
import java.io.IOException;

import com.realtimetech.fermes.FermesDB;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.item.Items;
import com.realtimetech.fermes.database.page.exception.PageIOException;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class Example {
	public static class UserManager extends Items<User> {
	}

	public static class User implements Item {
		private String name;
		private Link<Inventory> inventory;

		public User(String name) {
			this.name = name;
		}

		@Override
		public void onCreate(Link<? extends Item> link) {
			try {
				this.inventory = link.createChildLink(new Inventory());
			} catch (PageIOException e) {
				e.printStackTrace();
			}
		}

		public Link<Inventory> getInventory() {
			return inventory;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class Inventory extends Items<GameItem> {
	}

	public static class GameItem implements Item {
		private int price;

		private String name;

		public GameItem(int price, String name) {
			this.price = price;
			this.name = name;
		}

		public int getPrice() {
			return price;
		}

		public String getName() {
			return name;
		}
	}

	public static void main(String[] args) throws FermesDatabaseException, PageIOException, FermesItemException, IOException {
		Database database;

		// Create or
//		database = FermesDB.createDatabase(new File("example_db/"), 1024, 512, Long.MAX_VALUE);

		// Load or
//		database = FermesDB.loadDatabase(new File("example_db/"));

		// Get(If not exist create, or not just load)
		database = FermesDB.get(new File("example_db/"), 1024, 512, Long.MAX_VALUE);

		// Create root managers (not only one)
		Link<UserManager> userManager = database.getLink("user_manager", () -> new UserManager());

		// Add items
		userManager.get().addItem(new User("홍길동"));
		userManager.get().addItem(new User("개길똥"));
		Link<User> user = userManager.get().addItem(new User("박정환"));
		
		// Add items in item
		user.get().getInventory().get().addItem(new GameItem(1000, "초심자의 검"));
		
		// Add items in item (more accurate)
		Link<Inventory> inventory = user.get().getInventory();
		inventory.get().addItem(new GameItem(1000, "초심자의 활"));
		
		// Save database
		database.saveAndBackup(new File("test.zip"));
	}
}
