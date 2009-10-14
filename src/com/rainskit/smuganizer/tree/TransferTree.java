package com.rainskit.smuganizer.tree;

import com.kallasoft.smugmug.api.APIConstants;
import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.menu.TreeMenuManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public abstract class TransferTree extends JTree {
	protected Main main;
	protected DefaultMutableTreeNode rootNode;
	protected DefaultTreeModel model;

	public TransferTree(Main main) {
		super();
		this.main = main;
		this.model = (DefaultTreeModel)getModel();
		this.rootNode = new DefaultMutableTreeNode();
		model.setRoot(rootNode);
		
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setCellRenderer(new TreeableRenderer());
		setDragEnabled(true);
		
		new TreeMenuManager(main, this);
	}

	public abstract int getSourceActions();
	
	public abstract boolean supportsImport();

	public abstract boolean supportsInsertAtSpecificLocation();
	
	public abstract void loadTree(TreeableGalleryItem root) throws IOException;
	
	protected void loadTreeImpl(TreeableGalleryItem root, boolean sort) throws IOException {
		rootNode.setUserObject(root);
		
		main.setStatus("Loading data...");
		rootNode.removeAllChildren();
		model.nodeStructureChanged(rootNode);
		
		HttpConnectionManagerParams params = APIConstants.HTTP_CLIENT.getHttpConnectionManager().getParams();
		HostConfiguration hostConfig = APIConstants.HTTP_CLIENT.getHostConfiguration();
		new AsynchronousTreeLoader(main, this, rootNode, root, sort).start(params, hostConfig);
	}
	
	public static void sortTree(DefaultMutableTreeNode parentNode, boolean sortDescendants) {
		ArrayList<TreeableGalleryItem> childItems = new ArrayList<TreeableGalleryItem>();
		Map<TreeableGalleryItem, DefaultMutableTreeNode> itemToNode = new HashMap<TreeableGalleryItem, DefaultMutableTreeNode>();
		
		Enumeration children = parentNode.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)children.nextElement();
			TreeableGalleryItem childItem = (TreeableGalleryItem)childNode.getUserObject();
			if (sortDescendants) {
				sortTree(childNode, sortDescendants);
			}
			childItems.add(childItem);
			itemToNode.put(childItem, childNode);
		}
		
		parentNode.removeAllChildren();
		Collections.sort(childItems);
		for (TreeableGalleryItem each : childItems) {
			parentNode.add(itemToNode.get(each));
		}
	}
}
