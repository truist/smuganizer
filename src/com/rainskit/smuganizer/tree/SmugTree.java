package com.rainskit.smuganizer.tree;

import com.kallasoft.smugmug.api.APIConstants;
import com.rainskit.smuganizer.*;
import com.rainskit.smuganizer.smugmugapiwrapper.SmugMug;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class SmugTree extends JTree implements SettingsListener {
	private Main main;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel model;
	
	
	public SmugTree(Main main) {
		super();
		this.main = main;
		this.model = (DefaultTreeModel)getModel();
		this.rootNode = new DefaultMutableTreeNode();
		model.setRoot(rootNode);
		
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setCellRenderer(new TreeableRenderer());
		setTransferHandler(new SmugTransferHandler(main));
		setDragEnabled(true);
		setDropMode(DropMode.INSERT);
	}

	public void settingChanged(String settingName) {
		if (SmugMugSettings.TREE_SORT.equals(settingName) || SmugMugSettings.TREE_CATEGORY_SORT.equals(settingName)) {
			sortTree(rootNode);
			model.nodeStructureChanged(rootNode);
		}
	}

	public void loadTree(SmugMug smugMug) throws IOException {
		rootNode.setUserObject(smugMug);
		SmugMugSettings.setSettingsListener(this);
		
		main.setStatus("Loading SmugMug data into the tree...");
		rootNode.removeAllChildren();
		model.nodeStructureChanged(rootNode);
		
		HttpConnectionManagerParams params = APIConstants.HTTP_CLIENT.getHttpConnectionManager().getParams();
		HostConfiguration hostConfig = APIConstants.HTTP_CLIENT.getHostConfiguration();
		new AsynchronousTreeLoader(main, this, rootNode, smugMug).start(params, hostConfig);
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
}
