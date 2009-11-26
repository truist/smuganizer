package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.settings.SmugMugSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

class AsynchronousTreeLoader {
	private static final int TREE_LOADER_THREADS = 15;
	private static final int QUEUE_TIMEOUT = 4;
	private static final TimeUnit QUEUE_TIMEOUT_UNITS = TimeUnit.SECONDS;
	
	private Main main;
	private JTree tree;
	private DefaultMutableTreeNode rootNode;
	private TreeableGalleryItem rootItem;
	
	private boolean sort;
	
	private int errorCount;
	
	public AsynchronousTreeLoader(Main main, 
									JTree tree, 
									DefaultMutableTreeNode rootNode, 
									TreeableGalleryItem rootItem,
									boolean sort) {
		this.main = main;
		this.tree = tree;
		this.rootNode = rootNode;
		this.rootItem = rootItem;
		this.sort = sort;
	}
	
	public void start(HttpConnectionManagerParams params, HostConfiguration hostConfig) throws IOException {
		params.setMaxConnectionsPerHost(hostConfig, TREE_LOADER_THREADS);
		ThreadPoolExecutor executorService = (ThreadPoolExecutor)Executors.newFixedThreadPool(TREE_LOADER_THREADS);
		CompletionService<List<LoadTask>> completionService
			= new ExecutorCompletionService<List<LoadTask>>(executorService);
		new LoadEngine(executorService, completionService).execute();
	}
	
	
	private class LoadEngine extends SwingWorker<String, LoadTask> {
		private ThreadPoolExecutor executorService;
		private CompletionService<List<LoadTask>> completionService;
		
		public LoadEngine(ThreadPoolExecutor executorService, CompletionService<List<LoadTask>> completionService) {
			this.executorService = executorService;
			this.completionService = completionService;
		}
		
		@Override
		protected String doInBackground() throws Exception {
			handleResults(new LoadTask(null, rootItem).call());	//initialize the queue
			do {
				Future<List<LoadTask>> result;
				while ((result = completionService.poll(QUEUE_TIMEOUT, QUEUE_TIMEOUT_UNITS)) != null) {
					handleResults(result.get());
				}
			} while (executorService.getQueue().size() > 0 || executorService.getActiveCount() > 0);
			executorService.shutdown();
			executorService.awaitTermination(QUEUE_TIMEOUT * 10, QUEUE_TIMEOUT_UNITS);
			return null;
		}
		
		private void handleResults(List<LoadTask> results) {
			for (LoadTask each : results) {
				publish(each);
				completionService.submit(each);
			}
		}

		@Override
		protected void process(List<LoadTask> chunks) {
			for (LoadTask each : chunks) {
				each.updateTree();
			}
		}

		@Override
		protected void done() {
			main.clearStatus();
			if (errorCount > 0) {
				JOptionPane.showMessageDialog(main, "Encountered " + errorCount + " errors while loading tree - data may be missing.  Please check the log for details.", "Error loading data", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	
	private class LoadTask implements Callable<List<LoadTask>> {
		private DefaultMutableTreeNode parentNode;
		private TreeableGalleryItem childItem;
		private DefaultMutableTreeNode childNode;
		
		public LoadTask(DefaultMutableTreeNode parentNode, TreeableGalleryItem childItem) {
			this.parentNode = parentNode;
			this.childItem = childItem;
			if (parentNode == null) {
				this.childNode = rootNode;
			} else {
				this.childNode = new DefaultMutableTreeNode(childItem);
			}
		}
		
		public List<LoadTask> call() throws Exception {
			try {
				main.setSubStatus(childItem.getFullPathLabel());
				List<LoadTask> grandChildTasks = new ArrayList<LoadTask>();
				if (childItem instanceof TreeableGalleryContainer) {
					List<? extends TreeableGalleryItem> grandChildren = ((TreeableGalleryContainer)childItem).loadChildren();
					if (grandChildren != null) {
						if (sort) {
							Collections.sort(grandChildren);
						}
						for (TreeableGalleryItem each : grandChildren) {
							if (each != null) {
								grandChildTasks.add(new LoadTask(childNode, each));
							}
						}
					}
				}
				return grandChildTasks;
			} catch (Exception ex) {
				Logger.getLogger(AsynchronousTreeLoader.class.getName()).log(Level.SEVERE, null, ex);
				errorCount++;
				throw ex;
			}
		}
		
		public void updateTree() {
			if (parentNode != null) {
				((DefaultTreeModel)tree.getModel()).insertNodeInto(childNode, parentNode, parentNode.getChildCount());
				if (parentNode.getParent() == null) {
					tree.expandPath(new TreePath(parentNode.getPath()));
				}
			}
		}
	}
}
