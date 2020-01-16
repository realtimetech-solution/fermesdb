package com.realtimetech.fermes.viewer;

import javax.swing.tree.DefaultMutableTreeNode;

import com.realtimetech.fermes.database.exception.FermesItemException;

public class JsonTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -1843767551189127242L;

	public JsonTreeNode(String title) throws FermesItemException {
		super(title);
	}
}
