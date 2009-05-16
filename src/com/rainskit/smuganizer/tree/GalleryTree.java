package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.*;
import com.rainskit.smuganizer.galleryapiwrapper.Gallery;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class GalleryTree extends JTree {
	private Main main;
	private DefaultTreeModel model;
	private DefaultMutableTreeNode rootNode;

	public GalleryTree(Main main) {
		super();
		this.main = main;
		this.model = (DefaultTreeModel)getModel();
		this.rootNode = new DefaultMutableTreeNode();
		model.setRoot(rootNode);
		
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setCellRenderer(new TreeableRenderer());
//		setTransferHandler(new SmugTransferHandler(main));
		setDragEnabled(true);
	}

	public void loadTree(Gallery gallery) throws IOException {
		rootNode.setUserObject(gallery);
		
		main.setStatus("Loading gallery data...");
		rootNode.removeAllChildren();
		model.nodeStructureChanged(rootNode);
		
		HttpConnectionManagerParams params = gallery.getHttpClient().getHttpConnectionManager().getParams();
		HostConfiguration hostConfig = gallery.getHttpClient().getHostConfiguration();
		new AsynchronousTreeLoader(main, this, rootNode, gallery).start(params, hostConfig);
	}
}
