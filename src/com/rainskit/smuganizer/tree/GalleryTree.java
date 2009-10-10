package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.*;
import com.rainskit.smuganizer.galleryapiwrapper.Gallery;
import com.rainskit.smuganizer.menu.TreeMenuManager;
import com.rainskit.smuganizer.tree.transfer.SmugTransferHandler;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class GalleryTree extends TransferTree {
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
		setDragEnabled(true);
		
		new TreeMenuManager(main, this);
	}
	
	public void loadTree(Gallery gallery) throws IOException {
		rootNode.setUserObject(gallery);
		
		main.setStatus("Loading data...");
		rootNode.removeAllChildren();
		model.nodeStructureChanged(rootNode);
		
		HttpConnectionManagerParams params = gallery.getHttpClient().getHttpConnectionManager().getParams();
		HostConfiguration hostConfig = gallery.getHttpClient().getHostConfiguration();
		new AsynchronousTreeLoader(main, this, rootNode, gallery, true).start(params, hostConfig);
	}

	@Override
	public int getSourceActions() {
		return SmugTransferHandler.COPY;
	}

	@Override
	public boolean canImport() {
		return false;
	}

	@Override
	public boolean canInsertAtSpecificLocation() {
		return false;
	}
}
