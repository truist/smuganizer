package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public abstract class AbstractTransferTask {
	private enum TaskStatus { 
		QUEUED, QUEUED_FOR_RETRY, STARTED, INTERRUPTED, ERRORED, CANCELED, DONE;

		@Override
		public String toString() {
			switch (this) {
				case QUEUED:
				case STARTED:
				case CANCELED:
				case DONE:
					return super.toString();
				case INTERRUPTED:
					return "Input required";
				case ERRORED:
					return "ERROR";
				case QUEUED_FOR_RETRY:
					return "RETRY";
				default:
					throw new IllegalArgumentException("Impossible status: " + this.name());
			}
		}
	}
	
	protected TreeableGalleryItem srcItem;
	protected JTree destTree;
	protected TreePath destParentPath;
	protected TreeableGalleryItem destParentItem;
	protected int destChildIndex;
	
	protected TransferException transferException;
	
	private TaskStatus status;
	private ArrayList<StatusListener> listeners;

	public AbstractTransferTask(TreeableGalleryItem srcItem, 
								JTree destTree, 
								TreePath destParentPath, 
								int destChildIndex) {
		super();
		this.srcItem = srcItem;
		this.destTree = destTree;
		this.destParentPath = destParentPath;
		this.destParentItem = (TreeableGalleryItem)((DefaultMutableTreeNode)destParentPath.getLastPathComponent()).getUserObject();
		this.destChildIndex = destChildIndex;
		
		this.listeners = new ArrayList<StatusListener>();
		setStatus(TaskStatus.QUEUED);
	}

	public void prepareForRetry() {
		this.transferException = null;
		setStatus(TaskStatus.QUEUED_FOR_RETRY);
	}

	public final TreeableGalleryItem doInBackground() {
		if (TaskStatus.QUEUED != status && TaskStatus.QUEUED_FOR_RETRY != status) {
			return null;
		}
		setStatus(TaskStatus.STARTED);
		try {
			TreeableGalleryItem newItem = doInBackgroundImpl();
			setStatus(TaskStatus.DONE);
			return newItem;
		} catch (TransferException te) {
			setStatus(TaskStatus.INTERRUPTED);
			this.transferException = te;
			return null;
		} catch (Exception ex) {
			Logger.getLogger(AbstractTransferTask.class.getName()).log(Level.SEVERE, null, ex);
			this.transferException = new UnexpectedTransferException(ex);
			setStatus(TaskStatus.ERRORED);
			return null;
		}
	}
	
	protected abstract TreeableGalleryItem doInBackgroundImpl() throws TransferException, IOException;

	protected abstract void cleanUp(TreeableGalleryItem newItem);
	
	private void setStatus(TaskStatus status) {
		this.status = status;
		switch (status) {
			case QUEUED:
				srcItem.transferStarted(false);
				destParentItem.transferStarted(true);
				break;
			case CANCELED:
				srcItem.transferEnded(false, false);
				destParentItem.transferEnded(true, false);
				break;
			case DONE:
				srcItem.transferEnded(false, true);
				destParentItem.transferEnded(true, true);
				break;
		}
		
		for (StatusListener each : listeners) {
			each.statusChanged(this);
		}
	}
	
	public void addStatusListener(StatusListener listener) {
		listeners.add(listener);
	}

	public void removeStatusListener(StatusListener listener) {
		listeners.remove(listener);
	}
	
	public String getStatus() {
		return status.toString();
	}

	public void cancel() {
		setStatus(TaskStatus.CANCELED);
	}

	public boolean isActive() {
		return (TaskStatus.STARTED == status);
	}
	
	public boolean isInterrupted() {
		return (TaskStatus.INTERRUPTED == status);
	}
	
	public boolean isErrored() {
		return (TaskStatus.ERRORED == status);
	}
	
	public String getErrorMessage() {
		return transferException.getLocalizedMessage();
	}
	
	public String getErrorText() {
		return transferException.getErrorText();
	}
}
