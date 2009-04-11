package com.rainskit.smuganizer.tree;

import java.awt.Color;
import java.awt.Component;
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
			if (TreeableGalleryItem.CATEGORY.equals(currentItem.getType())) {
				return categoryIcon;
			} else if (TreeableGalleryItem.ALBUM.equals(currentItem.getType())) {
				return albumIcon;
			} else if (TreeableGalleryItem.IMAGE.equals(currentItem.getType())) {
				return imageIcon;
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
				if (currentItem.isProtected() || currentItem.isParentProtected()) {
					superComponent.setForeground(Color.GRAY);
				}
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

}
