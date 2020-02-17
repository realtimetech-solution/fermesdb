package com.realtimetech.fermes.viewer;

import javax.swing.tree.DefaultMutableTreeNode;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.kson.element.JsonObject;
import com.realtimetech.kson.element.JsonValue;

public class ItemTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -1843767551189127242L;

	private Link<? extends Item> link;
	
	private JsonObject jsonObject;
	private String name;

	public ItemTreeNode(int index, Link<? extends Item> link) {
		super("");

		this.link = link;

		JsonObject jsonObject = (JsonObject) FermesViewer.readForcely(link);
		
		this.jsonObject = (JsonObject) jsonObject.get("item");
		
		String name = (String) jsonObject.get("class");
		String[] strings = name.split("\\.");
		String[] value = strings[strings.length - 1].split("\\$");
		
		this.name = value[value.length - 1];
		
		this.setUserObject(this.name + " [ " + link.getGid() + " ]");
	}

	public Iterable<Long> getChildLinks() {
		return this.link.getChildLinks();
	}

	public JsonValue getObject() {
		return this.jsonObject;
	}
}
