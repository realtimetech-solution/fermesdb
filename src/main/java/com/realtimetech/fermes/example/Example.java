package com.realtimetech.fermes.example;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

		public Iterable<Link<GameItem>> getGameItems() {
			return super.getItems();
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

	public static String randomHangulName() {
		List<String> firstNames = Arrays.asList("김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송", "류", "전", "홍", "고", "문", "양", "손", "배", "조", "백", "허", "유", "남", "심", "노", "정", "하", "곽", "성", "차", "주", "우", "구", "신", "임", "나", "전", "민", "유", "진", "지", "엄", "채", "원", "천", "방", "공", "강", "현", "함", "변", "염", "양", "변", "여", "추", "노", "도", "소", "신", "석", "선", "설", "마", "길", "주", "연", "방", "위", "표", "명", "기", "반", "왕", "금", "옥", "육", "인", "맹", "제", "모", "장", "남", "탁", "국", "여",
				"진", "어", "은", "편", "구", "용");
		List<String> lastNames = Arrays.asList("가", "강", "건", "경", "고", "관", "광", "구", "규", "근", "기", "길", "나", "남", "노", "누", "다", "단", "달", "담", "대", "덕", "도", "동", "두", "라", "래", "로", "루", "리", "마", "만", "명", "무", "문", "미", "민", "바", "박", "백", "범", "별", "병", "보", "빛", "사", "산", "상", "새", "서", "석", "선", "설", "섭", "성", "세", "소", "솔", "수", "숙", "순", "숭", "슬", "승", "시", "신", "아", "안", "애", "엄", "여", "연", "영", "예", "오", "옥", "완", "요", "용", "우", "원", "월", "위", "유", "윤", "율", "으", "은", "의", "이", "익", "인", "일", "잎",
				"자", "잔", "장", "재", "전", "정", "제", "조", "종", "주", "준", "중", "지", "진", "찬", "창", "채", "천", "철", "초", "춘", "충", "치", "탐", "태", "택", "판", "하", "한", "해", "혁", "현", "형", "혜", "호", "홍", "화", "환", "회", "효", "훈", "휘", "희", "운", "모", "배", "부", "림", "봉", "혼", "황", "량", "린", "을", "비", "솜", "공", "면", "탁", "온", "디", "항", "후", "려", "균", "묵", "송", "욱", "휴", "언", "령", "섬", "들", "견", "추", "걸", "삼", "열", "웅", "분", "변", "양", "출", "타", "흥", "겸", "곤", "번", "식", "란", "더", "손", "술", "훔", "반", "빈", "실", "직", "흠", "흔", "악",
				"람", "뜸", "권", "복", "심", "헌", "엽", "학", "개", "롱", "평", "늘", "늬", "랑", "얀", "향", "울", "련");

		Collections.shuffle(firstNames);
		Collections.shuffle(lastNames);

		return firstNames.get(0) + lastNames.get(0) + lastNames.get(1);
	}

	public static String randomItemName() {
		List<String> effectNames = Arrays.asList("영롱한", "엄청난", "강인한", "마법의", "신비한", "강력한", "붉은");
		List<String> itemNames = Arrays.asList("검", "활", "반지", "모자", "신발", "갑옷", "너클", "양날 검", "짜파게티", "인형");

		Collections.shuffle(effectNames);
		Collections.shuffle(itemNames);

		return effectNames.get(0) + " " + itemNames.get(0);
	}

	public static void main(String[] args) throws Exception {
		File databaseDirectory = new File("example_db/");

		FermesDB.deleteDatabase(databaseDirectory);

		Database database = FermesDB.get(databaseDirectory, 1024, 512, Long.MAX_VALUE);

		Link<UserManager> userManager = database.getLink("user_manager", () -> new UserManager());

		Random random = new Random();

		for (int i = 0; i < 10 + random.nextInt(1000); i++) {
			Link<User> user = userManager.get().addUser(new User(randomHangulName()));

			for (int j = 0; j < 2 + random.nextInt(50); j++) {
				user.get().getInventory().get().addGameItem(new GameItem(random.nextInt(500) + 100, randomItemName()));
			}
		}

		{
			System.out.println("   ------- Where ------- ");
			System.out.println();
			{
				ItemIterable<User> where = userManager.get().where((value) -> value.getName().length() == 3).where((value) -> value.getInventory().get().getGameItemCount() >= 2);

				int count = 1;
				for (Link<User> link : where) {
					System.out.println("  " + (count++) + ". " + link.get().getName() + " \t " + link.get().getInventory().getChildCount() + " Items");

					for (Link<GameItem> gameItem : link.get().getInventory().get().getGameItems()) {
						System.out.println("       " + gameItem.get().getName() + "\t(" + gameItem.get().getPrice() + ")");
					}
					System.out.println("");
				}
			}
			System.out.println();
			System.out.println("   --------------------- ");
		}

		database.save();
	}
}
