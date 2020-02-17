package com.realtimetech.fermes.example;

import java.io.File;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.FermesDB;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.item.Items;
import com.realtimetech.fermes.database.item.Items.ItemIterable;
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

		public Iterable<Long> getUserGids() {
			return super.getItemGids();
		}

		public Iterable<Link<User>> getUsers() {
			return super.getItems();
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

		public Iterable<Long> getGameItemGids() {
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
		{
			Link<User> user = userManager.get().addUser(new User("홍길동"));

			user.get().getInventory().get().addGameItem(new GameItem(100, "할루1"));
			user.get().getInventory().get().addGameItem(new GameItem(200, "할루2"));
			user.get().getInventory().get().addGameItem(new GameItem(300, "할루3"));
			user.get().getInventory().get().addGameItem(new GameItem(400, "할루4"));
			user.get().getInventory().get().addGameItem(new GameItem(500, "할루5"));
			user.get().getInventory().get().addGameItem(new GameItem(600, "할루6"));
		}
		
		userManager.get().addUser(new User("개길똥"));

		{
			Link<User> user = userManager.get().addUser(new User("박정환"));

			user.get().getInventory().get().addGameItem(new GameItem(100, "활"));
			user.get().getInventory().get().addGameItem(new GameItem(200, "검"));
			user.get().getInventory().get().addGameItem(new GameItem(300, "모자"));
			user.get().getInventory().get().addGameItem(new GameItem(400, "신발"));
			user.get().getInventory().get().addGameItem(new GameItem(500, "반지"));
			user.get().getInventory().get().addGameItem(new GameItem(600, "지갑"));
		}

		{
			Link<User> user = userManager.get().addUser(new User("진홍석"));

			user.get().getInventory().get().addGameItem(new GameItem(100, "활"));
			user.get().getInventory().get().addGameItem(new GameItem(100, "하자"));
		}

		{
			Link<User> user = userManager.get().addUser(new User("김소연"));

			user.get().getInventory().get().addGameItem(new GameItem(100, "활"));
			user.get().getInventory().get().addGameItem(new GameItem(200, "검"));
			user.get().getInventory().get().addGameItem(new GameItem(300, "모자"));
			user.get().getInventory().get().addGameItem(new GameItem(400, "신발"));
		}


		for (Link<User> user : userManager.get().getUsers()) {
			System.out.println(user.get());
		}
		
		{
			System.out.println("   ------- Where ------- ");
			System.out.println();
			{
				ItemIterable<User> where = userManager.get().where((value) -> value.getName().length() == 3).where((value) -> value.getInventory().get().getGameItemCount() >= 2);

				int count = 1;
				for (Link<User> link : where) {
					System.out.println("  " + (count++) + ". " + link.get().getName() + " \t " + link.get().getInventory().getChildCount() + " Items");
				}
			}
			System.out.println();
			System.out.println("   ------- Where ------- ");
		}
		// Save database
		database.saveAndBackup(new File("test.zip"));
	}
}
