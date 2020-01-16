package com.realtimetech.fermes.viewer;

import javax.swing.tree.DefaultMutableTreeNode;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.exception.FermesItemException;
import com.realtimetech.fermes.database.item.Item;

public class ItemTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -1843767551189127242L;

	private Link<? extends Item> link;

	public ItemTreeNode(int index, Link<? extends Item> link) throws FermesItemException {
		super(link.isLoaded() ? link.get().getClass().getSimpleName() + " [ " + link.getGid() + " ]"
				: "[ " + link.getGid() + " ]");

		this.link = link;
	}

	public Link<? extends Item> getLink() {
		return link;
	}
}
