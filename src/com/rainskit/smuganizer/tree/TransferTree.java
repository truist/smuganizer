package com.rainskit.smuganizer.tree;

import javax.swing.JTree;

public abstract class TransferTree extends JTree {

	public abstract int getSourceActions();
	
	public abstract boolean canImport();

	public abstract boolean canInsertAtSpecificLocation();
}
