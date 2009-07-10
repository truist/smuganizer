package com.rainskit.smuganizer.tree.transfer.tasks;

import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import com.rainskit.smuganizer.tree.transfer.*;
import com.rainskit.smuganizer.menu.gui.TransferErrorDialog;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
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
	
	protected JTree srcTree;
	protected TreeableGalleryItem srcItem;
	protected JTree destTree;
	protected TreePath destParentPath;
	protected TreeableGalleryItem destParentItem;
	protected int destChildIndex;
	
	protected TransferInterruption transferInterruption;
	protected Throwable transferError;
	
	private TaskStatus status;
	private ArrayList<StatusListener> listeners;

	public AbstractTransferTask(JTree srcTree,
								TreeableGalleryItem srcItem, 
								JTree destTree, 
								TreePath destParentPath, 
								int destChildIndex) {
		super();
		this.srcTree = srcTree;
		this.srcItem = srcItem;
		this.destTree = destTree;
		this.destParentPath = destParentPath;
		if (destParentPath != null) {
			this.destParentItem = (TreeableGalleryItem)((DefaultMutableTreeNode)destParentPath.getLastPathComponent()).getUserObject();
		}
		this.destChildIndex = destChildIndex;
		
		this.listeners = new ArrayList<StatusListener>();
		setStatus(TaskStatus.QUEUED);
	}

	public void prepareForRetry() {
		setStatus(TaskStatus.QUEUED_FOR_RETRY);
	}

	public final TreeableGalleryItem doInBackground() {
		if (TaskStatus.QUEUED != status && TaskStatus.QUEUED_FOR_RETRY != status) {
			return null;
		}
		setStatus(TaskStatus.STARTED);
		try {
			TreeableGalleryItem newItem = doInBackgroundImpl(transferInterruption);
			setStatus(TaskStatus.DONE);
			return newItem;
		} catch (TransferInterruption te) {
			setStatus(TaskStatus.INTERRUPTED);
			this.transferInterruption = te;
			return null;
		} catch (Exception ex) {
			Logger.getLogger(AbstractTransferTask.class.getName()).log(Level.SEVERE, null, ex);
			transferError = ex;
			setStatus(TaskStatus.ERRORED);
			return null;
		}
	}
	
	protected abstract TreeableGalleryItem doInBackgroundImpl(TransferInterruption previousInterruption) throws TransferInterruption, IOException;

	public abstract List<AbstractTransferTask> cleanUp(TreeableGalleryItem newItem);

	public abstract String getActionString();
	
	private void setStatus(TaskStatus status) {
		this.status = status;
		TreeableGalleryItem recipientItem = (destParentItem != null ? destParentItem : srcItem);
		switch (status) {
			case QUEUED:
				srcItem.transferStarted(false);
				recipientItem.transferStarted(true);
				break;
			case CANCELED:
				srcItem.transferEnded(false, false);
				recipientItem.transferEnded(true, false);
				break;
			case DONE:
				srcItem.transferEnded(false, true);
				recipientItem.transferEnded(true, true);
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

	public String getSourceLabel() {
		return srcItem.getFullPathLabel();
	}
	
	public String getDestLabel() {
		if (destParentItem != null) {
			return destParentItem.getFullPathLabel();
		} else {
			return "";
		}
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

	public boolean isDone() {
		return (TaskStatus.DONE == status);
	}
	
	public TransferInterruption getInterruption() {
		return transferInterruption;
	}
	
	public String getStatusTooltip() {
		if (isInterrupted()) {
			return transferInterruption.getLocalizedMessage();
		} else if (isErrored()) {
			return transferError.getLocalizedMessage();
		} else {
			return null;
		}
	}
	
	public String getErrorText() {
		String errorText = "An unexpected error occurred. The description of the original error is below, followed by a list of actions the system was attempting to take when the error occurred.\n\n";
		StringWriter stackTraceWriter = new StringWriter();
		transferError.printStackTrace(new PrintWriter(stackTraceWriter));
		return errorText + stackTraceWriter.toString();
	}
	
	public JTree getSourceTree() {
		return srcTree;
	}
	
	public TreeableGalleryItem getSourceItem() {
		return srcItem;
	}
	
	public JTree getDestTree() {
		return destTree;
	}
	
	public TreeableGalleryItem getDestParentItem() {
		return destParentItem;
	}
}
