package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class AsynchronousTransferManager implements StatusListener {
	private LinkedBlockingDeque<AbstractTransferTask> taskDeque;
	private TransferTableModel visibleTable;
	
	public AsynchronousTransferManager(TransferTableModel visibleTable) {
		this.taskDeque = new LinkedBlockingDeque<AbstractTransferTask>();
		this.visibleTable = visibleTable;
		
		startTransferThread();
	}
	
	private void startTransferThread() {
		Thread transferThread = new Thread(new Runnable(){
			public void run() {
				try {
					AbstractTransferTask nextTask = null;
					while ((nextTask = taskDeque.takeFirst()) != null) {
						TreeableGalleryItem newItem = nextTask.doInBackground();
						if (newItem != null) {
							cleanUp(nextTask, newItem);
							removeVisibleRow(nextTask);
						} else {
							repaintVisibleRow(nextTask);
						}
					}
				} catch (InterruptedException ex) {
					Logger.getLogger(AsynchronousTransferManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		transferThread.start();
	}

	public void submit(AbstractTransferTask transferTask) {
		transferTask.addStatusListener(this);
		visibleTable.addTask(transferTask);
		taskDeque.addLast(transferTask);
	}

	public void cancel(AbstractTransferTask task) {
		if (task.isInterrupted() || taskDeque.remove(task)) {
			task.cancel();
			removeVisibleRow(task);
		}
	}
	
	public void statusChanged(AbstractTransferTask task) {
		repaintVisibleRow(task);
	}
	
	private void cleanUp(final AbstractTransferTask task, final TreeableGalleryItem newItem) throws InterruptedException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					task.cleanUp(newItem);
				}
			});
		} catch (InvocationTargetException ex) {
			Logger.getLogger(AsynchronousTransferManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void repaintVisibleRow(final AbstractTransferTask task) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				visibleTable.taskUpdated(task);
			}
		});
	}
	
	private void removeVisibleRow(final AbstractTransferTask task) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				visibleTable.removeTask(task);
				task.removeStatusListener(AsynchronousTransferManager.this);
			}
		});
	}
}
