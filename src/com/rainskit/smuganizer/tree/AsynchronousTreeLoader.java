package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.Main;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class AsynchronousTreeLoader {
	private static final int TREE_LOADER_THREADS = 15;
	private static final int QUEUE_TIMEOUT = 2;
	private static final TimeUnit QUEUE_TIMEOUT_UNITS = TimeUnit.SECONDS;
	
	private Main main;
	private JTree tree;
	private DefaultMutableTreeNode rootNode;
	private TreeableGalleryItem rootItem;
	
	private int errorCount;
	
	public AsynchronousTreeLoader(Main main, 
								JTree tree, 
								DefaultMutableTreeNode rootNode, 
								TreeableGalleryItem rootItem) {
		this.main = main;
		this.tree = tree;
		this.rootNode = rootNode;
		this.rootItem = rootItem;
	}
	
	public void start(HttpConnectionManagerParams params, HostConfiguration hostConfig) throws IOException {
		attachStatusFinalizer(startLoadingFromQueue(initializeQueue(), params, hostConfig));
		expandInitialTreeRows();
	}
	
	private LinkedBlockingDeque<LoadTask> initializeQueue() throws IOException {
		LinkedBlockingDeque<LoadTask> loadTaskQueue = new LinkedBlockingDeque<LoadTask>();
		loadTaskQueue.add(new LoadTask(rootNode, rootItem.loadChildren()));
		return loadTaskQueue;
	}
	
	private Thread[] startLoadingFromQueue(LinkedBlockingDeque<LoadTask> loadTaskQueue,
											HttpConnectionManagerParams params,
											HostConfiguration hostConfig) {
//		HttpConnectionManagerParams params = APIConstants.HTTP_CLIENT.getHttpConnectionManager().getParams();
//		HostConfiguration hostConfig = APIConstants.HTTP_CLIENT.getHostConfiguration();
		params.setMaxConnectionsPerHost(hostConfig, TREE_LOADER_THREADS);
		Thread[] threads = new Thread[TREE_LOADER_THREADS];
		for (int i = 0; i < TREE_LOADER_THREADS; i++) {
			threads[i] = new Thread(new TreeLoader(loadTaskQueue));
			threads[i].start();
		}
		return threads;
	}
	
	private void attachStatusFinalizer(final Thread[] threads) {
		new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < threads.length; i++) {
					try {
						threads[i].join();
					} catch (InterruptedException ex) {
						Logger.getLogger(SmugTree.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				main.clearStatus();
				if (errorCount > 0) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(main, "Encountered " + errorCount + " errors while loading tree - data may be missing.  Please check the log for details.", "Error loading data", JOptionPane.ERROR_MESSAGE);
						}
					});
				}
			}
		}).start();
	}
	
	private void expandInitialTreeRows() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {
			Logger.getLogger(SmugTree.class.getName()).log(Level.SEVERE, null, ex);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tree.expandPath(tree.getPathForRow(0));
			}
		});
	}
	
	
	private class LoadTask {
		private DefaultMutableTreeNode parentNode;
		private List<? extends TreeableGalleryItem> items;
		private ArrayList<LoadTask> grandChildrenTasks;
		
		public LoadTask(DefaultMutableTreeNode parentNode, List<? extends TreeableGalleryItem> items) {
			this.parentNode = parentNode;
			this.items = items;
			this.grandChildrenTasks = new ArrayList<LoadTask>();
		}

		public void load() throws IOException {
			Collections.sort(items);
			for (TreeableGalleryItem child : items) {
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
				SwingUtilities.invokeLater(new InsertTask(childNode));
				createTasksForGrandChildren(child, childNode);
				main.setSubStatus(child.getFullPathLabel());
			}
		}
		
		private void createTasksForGrandChildren(TreeableGalleryItem child, DefaultMutableTreeNode childNode) throws IOException {
			List<? extends TreeableGalleryItem> grandChildren = child.loadChildren();
			if (grandChildren != null && grandChildren.size() > 0) {
				grandChildrenTasks.add(new LoadTask(childNode, grandChildren));
			}
		}
		
		public List<LoadTask> getGrandChildrenTasks() {
			return grandChildrenTasks;
		}
		
		
		private class InsertTask implements Runnable {
			private DefaultMutableTreeNode childNode;
			
			public InsertTask(DefaultMutableTreeNode childNode) {
				this.childNode = childNode;
			}
			
			public void run() {
				((DefaultTreeModel)tree.getModel()).insertNodeInto(childNode, parentNode, parentNode.getChildCount());
			}
		}
	}
	
	private class TreeLoader implements Runnable {
		private LinkedBlockingDeque<LoadTask> loadTaskQueue;
		
		public TreeLoader (LinkedBlockingDeque<LoadTask> loadTaskQueue) {
			this.loadTaskQueue = loadTaskQueue;
		}
		
		public void run() {
			try {
				LoadTask next;
				while ((next = loadTaskQueue.poll(QUEUE_TIMEOUT, QUEUE_TIMEOUT_UNITS)) != null) {
					try {
						next.load();
					} catch (Exception e) {
						errorCount++;
						Logger.getLogger(SmugTree.class.getName()).log(Level.SEVERE, "Exception caught, prevented thread death.  Some children may be missing from the tree.", e);
					}
					loadTaskQueue.addAll(next.getGrandChildrenTasks());
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(SmugTree.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
