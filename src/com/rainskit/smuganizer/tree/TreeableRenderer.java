package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.tree.TreeableGalleryItem.ItemType;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeableRenderer extends DefaultTreeCellRenderer {
	private static ImageIcon categoryIcon = new ImageIcon("lib/images/images_stack.png");
	private static ImageIcon albumIcon = new ImageIcon("lib/images/camera.png");
	private static ImageIcon imageIcon = new ImageIcon("lib/images/image.png");
	
	private TreeableGalleryItem currentItem;
	
	private Icon getIconForType() {
		if (currentItem != null) {
			switch (currentItem.getType()) {
				case CATEGORY: return categoryIcon;
				case ALBUM: return albumIcon;
				case IMAGE: return imageIcon;
			}
		}
		return null;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (value != null) {
			Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
			if (userObject instanceof TreeableGalleryItem) {
				currentItem = (TreeableGalleryItem)userObject;	//yeah, it's a hack
				
				String newValue = currentItem.getLabel() + currentItem.getMetaLabel();
				Component superComponent = super.getTreeCellRendererComponent(tree, newValue, sel, expanded, leaf, row, hasFocus);
				setCellColor(superComponent, currentItem);
				return superComponent;
			} 
		}
		return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	}

	@Override
	public Icon getClosedIcon() {
		return getIconForType();
	}

	@Override
	public Icon getLeafIcon() {
		return getIconForType();
	}

	@Override
	public Icon getOpenIcon() {
		return getIconForType();
	}

	private void setCellColor(Component rendererComponent, TreeableGalleryItem currentItem) {
		if (currentItem.isSending() || currentItem.isReceiving()) {
			if (currentItem.isSending()) {
				rendererComponent.setForeground(Color.RED);
				rendererComponent.setFont(rendererComponent.getFont().deriveFont(Font.ITALIC));
			} else if (currentItem.isReceiving()) {
				rendererComponent.setForeground(Color.ORANGE);
				rendererComponent.setFont(rendererComponent.getFont().deriveFont(Font.BOLD));
			}
		} else {
			if (currentItem.isProtected() || currentItem.isParentProtected()) {
				rendererComponent.setForeground(Color.GRAY);
			} else if (currentItem.hasBeenSent()) {
				rendererComponent.setForeground(Color.BLUE);
			}
			rendererComponent.setFont(rendererComponent.getFont().deriveFont(Font.PLAIN));
		}
	}
}
