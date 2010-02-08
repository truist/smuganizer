package com.rainskit.smuganizer.tree.transfer.tasks;

import com.rainskit.smuganizer.ExifHandler;
import com.rainskit.smuganizer.settings.TransferSettings;
import com.rainskit.smuganizer.tree.TreeableGalleryContainer;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.TreeableGalleryItem.ItemType;
import com.rainskit.smuganizer.tree.transfer.interruptions.DuplicateFileNameInterruption;
import com.rainskit.smuganizer.tree.transfer.interruptions.InterruptionSet;
import com.rainskit.smuganizer.tree.transfer.interruptions.UnexpectedCaptionInterruption;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.io.IOUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;

public class CopyRemote extends AbstractTransferTask {
	
	public CopyRemote(JTree srcTree,
						TreeableGalleryItem srcItem, 
						JTree destTree, 
						TreePath destParentPath, 
						int destChildIndex) {
		super(srcTree, srcItem, destTree, destParentPath, destChildIndex);
	}

	protected TreeableGalleryItem doInBackgroundImpl(InterruptionSet previousInterruptions) throws TransferInterruption, IOException {
		ModifiedItemAttributes finalAttributes = new ModifiedItemAttributes();
		finalAttributes.caption = srcItem.getCaption();
		if (ItemType.IMAGE == srcItem.getType()) {
			finalAttributes = handleExifDescriptions(finalAttributes, previousInterruptions);
		}
		finalAttributes = handleDuplicates(finalAttributes, previousInterruptions);

		TreeableGalleryItem newItem = destParentItem.importItem(srcItem, finalAttributes);
		newItem.transferStarted(false);
		newItem.transferEnded(false, true);
		return newItem;
	}

	private ModifiedItemAttributes handleExifDescriptions(ModifiedItemAttributes imageAttributes, InterruptionSet previousInterruptions) throws IOException, UnexpectedCaptionInterruption {
		if (previousInterruptions.hasInterruption(UnexpectedCaptionInterruption.class)) {
			UnexpectedCaptionInterruption uci = (UnexpectedCaptionInterruption)previousInterruptions.getInterruption(UnexpectedCaptionInterruption.class);
			imageAttributes.imageData = uci.getImageData();
			imageAttributes.caption = uci.getFixedCaption();
		} else {
			InputStream sourceInputStream = srcItem.getDataURL().openStream();
			try {
				imageAttributes.imageData = IOUtils.toByteArray(sourceInputStream);
			} finally {
				sourceInputStream.close();
			}
			
			if (imageAttributes.caption == null) {
				String fileName = srcItem.getFileName();
				try {
					String description = ExifHandler.getExifDescription(imageAttributes.imageData, fileName);
					if (description != null && description.length() > 0) {
						if (TransferSettings.getRemoveExifDescriptions()) {
							imageAttributes.imageData = ExifHandler.removeExifDescription(imageAttributes.imageData, fileName);
						} else if (!TransferSettings.getIgnoreExifDescriptions()) {
							throw new UnexpectedCaptionInterruption(imageAttributes.imageData, fileName, description);
						}
					}
				} catch (ImageWriteException ex) {
					Logger.getLogger(CopyRemote.class.getName()).log(Level.SEVERE, null, ex);
					throw new IOException("Error handling Exif tags in " + fileName, ex);
				} catch (ImageReadException ex) {
					Logger.getLogger(CopyRemote.class.getName()).log(Level.SEVERE, null, ex);
					throw new IOException("Error handling Exif tags in " + fileName, ex);
				}
			}
		}
		return imageAttributes;
	}
	
	public List<AbstractTransferTask> cleanUp(TreeableGalleryItem newItem) throws IOException {
		DefaultTreeModel destModel = (DefaultTreeModel)destTree.getModel();
		DefaultMutableTreeNode destParentNode = (DefaultMutableTreeNode)destParentPath.getLastPathComponent();
		DefaultMutableTreeNode newNode = checkIfItemIsAlreadyInSubTree(newItem, destParentNode);
		if (newNode == null) {
			newNode = new DefaultMutableTreeNode(newItem);
			destModel.insertNodeInto(newNode, destParentNode, Math.min(destChildIndex, destParentNode.getChildCount()));
		}
		destTree.makeVisible(destParentPath.pathByAddingChild(newNode));

		ArrayList<AbstractTransferTask> followUpTasks = new ArrayList<AbstractTransferTask>();
		if (destParentItem.canMoveLocally(newItem, destChildIndex)) {
			followUpTasks.add(new MoveLocal(destTree, newNode, destTree, destParentPath, destChildIndex));
		}
		if (srcItem.isProtected() && !newItem.isProtected()) {
			followUpTasks.add(new ProtectItem(destTree, newItem, destTree, newNode));
		}
		
		if (srcItem instanceof TreeableGalleryContainer) {
			List<? extends TreeableGalleryItem> children = ((TreeableGalleryContainer)srcItem).getChildren();
			if (children != null) {
				int childIndex = 0;
				for (TreeableGalleryItem childItem : children) {
					followUpTasks.add(new CopyRemote(srcTree, childItem, destTree, new TreePath(newNode.getPath()), childIndex++));
				}
			}
		}
		
		return followUpTasks;
	}

	private DefaultMutableTreeNode checkIfItemIsAlreadyInSubTree(TreeableGalleryItem newItem, DefaultMutableTreeNode destParentNode) {
		for (int i = 0; i < destParentNode.getChildCount(); i++) {
			DefaultMutableTreeNode each = (DefaultMutableTreeNode)destParentNode.getChildAt(i);
			if (each.getUserObject() == newItem) {
				return each;
			}
		}
		return null;
	}

	@Override
	public String getActionString() {
		return "COPY";
	}
}
