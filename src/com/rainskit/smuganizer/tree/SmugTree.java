package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.tree.transfer.SmugTransferHandler;
import com.rainskit.smuganizer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DropMode;
import javax.swing.tree.DefaultMutableTreeNode;

public class SmugTree extends TransferTree implements SettingsListener {
	
	public SmugTree(Main main) {
		super(main);
		setDropMode(DropMode.INSERT);
	}

	public void settingChanged(String settingName) {
		if (SmugMugSettings.TREE_SORT.equals(settingName) || SmugMugSettings.TREE_CATEGORY_SORT.equals(settingName)) {
			sortTree(rootNode);
			model.nodeStructureChanged(rootNode);
		}
	}

	public void loadTree(TreeableGalleryItem root) throws IOException {
		SmugMugSettings.setSettingsListener(this);
		loadTreeImpl(root, SmugMugSettings.getTreeSort());
	}
	
	private void sortTree(DefaultMutableTreeNode parentNode) {
		ArrayList<TreeableGalleryItem> childItems = new ArrayList<TreeableGalleryItem>();
		Map<TreeableGalleryItem, DefaultMutableTreeNode> itemToNode = new HashMap<TreeableGalleryItem, DefaultMutableTreeNode>();
		
		Enumeration children = parentNode.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)children.nextElement();
			TreeableGalleryItem childItem = (TreeableGalleryItem)childNode.getUserObject();
			sortTree(childNode);
			childItems.add(childItem);
			itemToNode.put(childItem, childNode);
		}
		
		parentNode.removeAllChildren();
		Collections.sort(childItems);
		for (TreeableGalleryItem each : childItems) {
			parentNode.add(itemToNode.get(each));
		}
	}

	@Override
	public int getSourceActions() {
		return SmugTransferHandler.COPY_OR_MOVE;
	}

	@Override
	public boolean canImport() {
		return true;
	}

	@Override
	public boolean canInsertAtSpecificLocation() {
		return true;
	}
}
