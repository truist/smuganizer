package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeableRenderer extends DefaultTreeCellRenderer {
	private static ImageIcon categoryIcon = createIcon("/images/images_stack.png");
	private static ImageIcon albumIcon = createIcon("/images/camera.png");
	private static ImageIcon imageIcon = createIcon("/images/image.png");

    private static ImageIcon createIcon(String path) {
        URL imageURL = TreeableRenderer.class.getResource(path);
        if (imageURL != null) {
            return new ImageIcon(imageURL, "");
        } else {
            System.err.println("Couldn't find image: " + path);
            return null;
        }
    }
	
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
				
				String newValue;
				try {
					newValue = currentItem.getLabel() + currentItem.getMetaLabel();
				} catch (SmugException ex) {
					Logger.getLogger(TreeableRenderer.class.getName()).log(Level.SEVERE, null, ex);
					newValue = "ERROR: " + ex.getMessage();
				}
				Component superComponent = super.getTreeCellRendererComponent(tree, newValue, sel, expanded, leaf, row, hasFocus);
				setCellAttributes(superComponent, currentItem, sel);
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

	private void setCellAttributes(Component rendererComponent, TreeableGalleryItem currentItem, boolean isSelected) {
		if (currentItem.isSending() || currentItem.isReceiving()) {
			rendererComponent.setFont(rendererComponent.getFont().deriveFont(Font.BOLD));
			if (!isSelected) {
				rendererComponent.setForeground(Color.ORANGE);
			}
		} else {
			if (currentItem.isProtected() || currentItem.isParentProtected()) {
				rendererComponent.setFont(rendererComponent.getFont().deriveFont(Font.ITALIC));
			} else {
				rendererComponent.setFont(rendererComponent.getFont().deriveFont(Font.PLAIN));
			}
			if (currentItem.hasBeenSent() && !isSelected) {
				rendererComponent.setForeground(Color.BLUE);
			}
		}
	}
}
