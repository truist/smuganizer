package com.rainskit.smuganizer.tree.transfer.interruptions;

import com.rainskit.smuganizer.menu.gui.TransferErrorDialog.RepairPanel;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.TreeableGalleryItem.ItemType;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class ConfirmAlbumConversionInterruption extends TransferInterruption {
	private TreeableGalleryItem rootAlbum;
	private TreeableGalleryItem newParent;
	
	private AlbumConversionRepairPanel repairPanel;
	private boolean shouldRemoveParent;
	
	public ConfirmAlbumConversionInterruption(TreeableGalleryItem rootAlbum, TreeableGalleryItem newParent) {
		super("Album has sub-albums and images");
		this.rootAlbum = rootAlbum;
		this.newParent = newParent;
	}

	@Override
	public String getErrorText() {
		return "This album has sub-albums and also has images.  SmugMug does not support this structure.";
	}

	@Override
	public RepairPanel getRepairPanel() {
		if (repairPanel == null) {
			repairPanel = new AlbumConversionRepairPanel();
		}
		return repairPanel;
	}
	
	public boolean shouldRemoveParent() {
		return shouldRemoveParent;
	}
	
	
	private enum RadioChoice { CONVERT_TO_SUB, REMOVE_PARENT }
	
	private class AlbumConversionRepairPanel extends RepairPanel {
		private JRadioButton duplicateRadio;
		private JRadioButton removeRadio;
		
		public AlbumConversionRepairPanel() {
			super(new BorderLayout());
			
			String header = getErrorText() + "\n\nThe trees below each represent an option for how to handle this, by showing what the final SmugMug structure will be.  (Note that the items may not be sorted correctly here.)  Please choose one of those options, or cancel this transfer.";
			JPanel headerPanel = new JPanel(new BorderLayout());
			headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
			headerPanel.add(makeMultiLineLabel(header, getBackground()), BorderLayout.CENTER);
			add(headerPanel, BorderLayout.NORTH);
			
			ButtonGroup buttonGroup = new ButtonGroup();
			
			duplicateRadio = new JRadioButton("Turn parent album into subcategory and album", true);
			duplicateRadio.setActionCommand(RadioChoice.CONVERT_TO_SUB.toString());
			buttonGroup.add(duplicateRadio);
			JTree duplicateTree = new JTree(createDuplicateTree());
			expandNonImageRows(duplicateTree);
			JPanel duplicatePanel = new JPanel(new BorderLayout());
			duplicatePanel.add(duplicateRadio, BorderLayout.NORTH);
			JScrollPane duplicateScroller = new JScrollPane(duplicateTree) {
				public Dimension getPreferredSize() {
					Dimension prefSize = super.getPreferredSize();
					prefSize.height = Math.min(prefSize.height, 200);
					return prefSize;
				}
			};
			duplicatePanel.add(duplicateScroller, BorderLayout.CENTER);
			
			removeRadio = new JRadioButton("Make child albums a sibling of the parent", true);
			removeRadio.setActionCommand(RadioChoice.REMOVE_PARENT.toString());
			buttonGroup.add(removeRadio);
			JTree removeTree = new JTree(createRemoveTree());
			expandNonImageRows(removeTree);
			JPanel removePanel = new JPanel(new BorderLayout());
			removePanel.add(removeRadio, BorderLayout.NORTH);
			JScrollPane removeScroller = new JScrollPane(removeTree) {
				public Dimension getPreferredSize() {
					Dimension prefSize = super.getPreferredSize();
					prefSize.height = Math.min(prefSize.height, 200);
					return prefSize;
				}
			};
			removePanel.add(removeScroller, BorderLayout.CENTER);
			
			JPanel treePanel = new JPanel(new GridLayout(1, 2));
			treePanel.add(duplicatePanel);
			treePanel.add(removePanel);
			
			add(treePanel, BorderLayout.CENTER);
		}
		
		private DefaultMutableTreeNode createDuplicateTree() {
			DemoTreeItem newItem = new DemoTreeItem(ItemType.CATEGORY.toString(), rootAlbum.getLabel());
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newItem);
			
			DemoTreeItem duplicateItem = new DemoTreeItem(ItemType.ALBUM.toString(), rootAlbum.getLabel());
			DefaultMutableTreeNode duplicateNode = new DefaultMutableTreeNode(duplicateItem);
			newNode.add(duplicateNode);
			
			for (TreeableGalleryItem each : rootAlbum.getChildren()) {
				if (ItemType.ALBUM == each.getType()) {
					newNode.add(buildMockSubTree(each, null));
				} else if (ItemType.IMAGE == each.getType()) {
					duplicateNode.add(buildMockSubTree(each, null));
				} else {
					throw new IllegalStateException("This should not be possible");
				}
			}
			
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new DemoTreeItem(newParent));
			rootNode.add(newNode);
			
			return rootNode;
		}
		
		private DefaultMutableTreeNode createRemoveTree() {
			DemoTreeItem replaceItem = new DemoTreeItem(ItemType.ALBUM.toString(), rootAlbum.getLabel());
			DefaultMutableTreeNode replaceNode = new DefaultMutableTreeNode(replaceItem);
			
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new DemoTreeItem(newParent));
			rootNode.add(replaceNode);
			
			for (TreeableGalleryItem each : rootAlbum.getChildren()) {
				if (ItemType.ALBUM == each.getType()) {
					rootNode.add(buildMockSubTree(each, null));
				} else if (ItemType.IMAGE == each.getType()) {
					replaceNode.add(buildMockSubTree(each, null));
				} else {
					throw new IllegalStateException("This should not be possible");
				}
			}
			
			return rootNode;
		}
		
		private DefaultMutableTreeNode buildMockSubTree(TreeableGalleryItem localRootItem, DefaultMutableTreeNode localRootNode) {
			if (localRootNode == null) {
				localRootNode = new DefaultMutableTreeNode(new DemoTreeItem(localRootItem));
			}
			List<? extends TreeableGalleryItem> children = localRootItem.getChildren();
			if (children != null) {
				for (TreeableGalleryItem each : children) {
					localRootNode.add(buildMockSubTree(each, null));
				}
			}
			return localRootNode;
		}

		private void expandNonImageRows(JTree duplicateTree) {
			for (int i = 0; i < duplicateTree.getRowCount(); i++) {
				DemoTreeItem rowItem = (DemoTreeItem)((DefaultMutableTreeNode)duplicateTree.getPathForRow(i).getLastPathComponent()).getUserObject();
				if (!rowItem.toString().startsWith(ItemType.ALBUM.toString())) {
					duplicateTree.expandRow(i);
				}
			}
		}
		
		@Override
		public String getDescription() {
			return rootAlbum.getFullPathLabel();
		}

		@Override
		public String getUniqueKey() {
			return getDescription();
		}

		@Override
		public void loadSettingsFrom(RepairPanel otherPanel) throws Exception {
			AlbumConversionRepairPanel realPanel = (AlbumConversionRepairPanel)otherPanel;
			duplicateRadio.setSelected(realPanel.duplicateRadio.isSelected());
			removeRadio.setSelected(realPanel.removeRadio.isSelected());
		}

		@Override
		public void post() throws Exception {
			shouldRemoveParent = removeRadio.isSelected();
		}
	}
	
	private class DemoTreeItem {
		private String type;
		private String label;
		
		public DemoTreeItem(String type, String label) {
			this.type = type;
			this.label = label;
		}
		
		public DemoTreeItem(TreeableGalleryItem realItem) {
			this(realItem.getType().toString(), realItem.getLabel());
		}

		@Override
		public String toString() {
			return type + ": " + label;
		}
	}
}
