package com.realtimetech.fermes.example;

import java.io.File;
import java.util.Collection;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.FermesDB;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.item.Items;
import com.realtimetech.fermes.database.link.exception.LinkCreateException;

public class Example {
	public static class UserManager extends Items<User> {
		public Link<User> addUser(User item) throws LinkCreateException {
			return super.addItem(item);
		}
		
		public Link<User> getUser(int index) {
			return super.getItem(index);
		}
		
		public Link<User> getUserByGid(long gid) {
			return super.getItemByGid(gid);
		}
		
		public Collection<Long> getUserGids() {
			return super.getItemGids();
		}
		
		public int getUserCount() {
			return super.getItemCount();
		}
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
			} catch (LinkCreateException e) {
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

		public Link<GameItem> addGameItem(GameItem item) throws LinkCreateException {
			return super.addItem(item);
		}
		
		public Link<GameItem> getGameItem(int index) {
			return super.getItem(index);
		}
		
		public Link<GameItem> getGameItemByGid(long gid) {
			return super.getItemByGid(gid);
		}
		
		public Collection<Long> getGameItemGids() {
			return super.getItemGids();
		}
		
		public int getGameItemCount() {
			return super.getItemCount();
		}
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

	public static void main(String[] args) throws Exception {
		Database database;
		File databaseDirectory = new File("example_db/");

		FermesDB.deleteDatabase(databaseDirectory);
		// Create or
//		database = FermesDB.createDatabase(new File("example_db/"), 1024, 512, Long.MAX_VALUE);

		// Load or
//		database = FermesDB.loadDatabase(new File("example_db/"));

		// Get(If not exist create, or not just load)
		database = FermesDB.get(databaseDirectory, 1024, 512, Long.MAX_VALUE);
 
		// Create root managers (not only one)
		Link<UserManager> userManager = database.getLink("user_manager", () -> new UserManager());

		// Add items
		userManager.get().addUser(new User("홍길동"));
		userManager.get().addUser(new User("개길똥"));
		Link<User> user = userManager.get().addUser(new User("박정환"));
		
		// Add items in item
		user.get().getInventory().get().addGameItem(new GameItem(1000, "초심자의 검"));
		
		// Add items in item (more accurate)
		Link<Inventory> inventory = user.get().getInventory();
		inventory.get().addGameItem(new GameItem(1000, "초심자의 활"));
		
		// Save database
		database.saveAndBackup(new File("test.zip"));
	}
}
