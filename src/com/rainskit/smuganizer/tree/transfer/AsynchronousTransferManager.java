package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

public class AsynchronousTransferManager implements StatusListener {
	private TransferHandler transferHandler;
	private LinkedBlockingDeque<AbstractTransferTask> taskDeque;
	private TransferTableModel visibleTable;
	private Thread transferThread;
	private volatile boolean paused;
	
	public AsynchronousTransferManager(TransferTableModel visibleTable) {
		this.transferHandler = new SmugTransferHandler(this);
		this.taskDeque = new LinkedBlockingDeque<AbstractTransferTask>();
		this.visibleTable = visibleTable;
		
		startTransferThread();
	}
	private void startTransferThread() {
		transferThread = new Thread(new Runnable(){
			public void run() {
				try {
					AbstractTransferTask nextTask = null;
					while ((nextTask = taskDeque.takeFirst()) != null) {
						if (paused) {
							synchronized (transferThread) {
								transferThread.wait();
							}
						}
						TreeableGalleryItem newItem = nextTask.doInBackground();
						if (nextTask.isDone()) {	//it could have been 'interrupted'
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

	public void pause() {
		paused = true;
	}
	
	public void resume() {
		paused = false;
		synchronized (transferThread) {
			transferThread.notify();
		}
	}

	public void submit(AbstractTransferTask task) {
		task.addStatusListener(this);
		visibleTable.addTask(task);
		taskDeque.addLast(task);
	}
	
	private void submitCleanUpTasks(List<AbstractTransferTask> tasks, AbstractTransferTask parentTask) {
		for (AbstractTransferTask each : tasks) {
			each.addStatusListener(this);
			visibleTable.addTaskAfter(each, parentTask);
			parentTask = each;
		}
		addTasksToFrontOfQueue(tasks);
	}
	
	public void retry(List<AbstractTransferTask> tasks) {
		for (AbstractTransferTask each : tasks) {
			each.prepareForRetry();
		}
		addTasksToFrontOfQueue(tasks);
	}
	
	private void addTasksToFrontOfQueue(List<AbstractTransferTask> tasks) {
		//it gets tricky to insert all the new items at the front of the queue,
		//while also ensuring that they are added in the correct order.  to solve this,
		//I empty the queue first, then just add the new items onto the "end" of the 
		//empty queue, then re-add the emptied items
		ArrayList<AbstractTransferTask> tempCache = new ArrayList<AbstractTransferTask>();
		taskDeque.drainTo(tempCache);	
		taskDeque.addAll(tasks);
		taskDeque.addAll(tempCache);
	}

	public void cancel(AbstractTransferTask task) {
		if (task.isInterrupted() || task.isErrored() || taskDeque.remove(task)) {
			task.cancel();
			removeVisibleRow(task);
		}
	}

	public void cancel(List<AbstractTransferTask> tasks) {
		for (AbstractTransferTask each : tasks) {
			cancel(each);
		}
	}
	
	public void statusChanged(AbstractTransferTask task) {
		repaintVisibleRow(task);
	}
	
	private void cleanUp(final AbstractTransferTask task, final TreeableGalleryItem newItem) throws InterruptedException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					List<AbstractTransferTask> followUpTasks = task.cleanUp(newItem);
					if (followUpTasks != null) {
						submitCleanUpTasks(followUpTasks, task);
					}
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

	public TransferHandler getTransferHandler() {
		return transferHandler;
	}
}
