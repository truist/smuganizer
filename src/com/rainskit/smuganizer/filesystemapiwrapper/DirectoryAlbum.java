package com.rainskit.smuganizer.filesystemapiwrapper;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectoryAlbum extends TreeableGalleryItem {
	private File myDirectory;
	private ArrayList<DirectoryAlbum> subDirs;
	private ArrayList<FileImage> subFiles;
	
	public DirectoryAlbum(DirectoryAlbum parent, File myDirectory) {
		super(parent);
		this.myDirectory = myDirectory;
	}

	@Override
	public List<? extends TreeableGalleryItem> loadChildren() throws IOException {
		if (subDirs == null) {
			subDirs = new ArrayList<DirectoryAlbum>();
			subFiles = new ArrayList<FileImage>();
			
			for (File each : myDirectory.listFiles()) {
				if (each.isDirectory()) {
					subDirs.add(new DirectoryAlbum(this, each));
				} else {
					subFiles.add(new FileImage(this, each));
				}
			}
			Collections.sort(subDirs);
			Collections.sort(subFiles);
		}
		return getChildren();
	}

	@Override
	public List<? extends TreeableGalleryItem> getChildren() {
		ArrayList<TreeableGalleryItem> children = new ArrayList<TreeableGalleryItem>();
		children.addAll(subDirs);
		children.addAll(subFiles);
		return children;
	}

	@Override
	public ItemType getType() {
		return ItemType.ALBUM;
	}

	@Override
	public void removeChild(TreeableGalleryItem child) {
		subDirs.remove(child);
		subFiles.remove(child);
	}

	@Override
	public boolean canMove(TreeableGalleryItem parent, int childIndex) {
		return (ItemType.ALBUM == parent.getType() || ItemType.ROOT == parent.getType());
	}

	@Override
	public void moveItem(TreeableGalleryItem parent, int childIndex, TransferInterruption previousInterruption) throws SmugException {
		throw new UnsupportedOperationException("Not supported yet.");
//		File newPath = new File(((DirectoryAlbum)parent).myDirectory, myDirectory.getName());
//		if (myDirectory.renameTo(newPath)) {
//			myDirectory = newPath;
//		} else {
//			throw new SmugException("Error moving " + getFullPathLabel() + " to " + newPath.toString(), null);
//		}
	}

	@Override
	public boolean canImport(TreeableGalleryItem newItem) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, TransferInterruption previousInterruption) throws IOException, TransferInterruption {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getLabel() {
		return getFileName();
	}

	@Override
	public String getMetaLabel() {
		return "";
	}

	@Override
	public boolean canBeRelabeled() {
		return true;
	}

	@Override
	public void reLabel(String newLabel) throws SmugException {
		File newName = new File(myDirectory.getParentFile(), newLabel);
		if (myDirectory.renameTo(newName)) {
			myDirectory = newName;
		} else {
			throw new SmugException("Unable to rename " + myDirectory.toString() + " to " + newLabel, null);
		}
	}

	@Override
	public String getFileName() {
		return myDirectory.getName();
	}

	@Override
	public String getURLName() {
		return getFileName();
	}

	@Override
	public String getCaption() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public boolean canBeDeleted() {
		return true;
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isHidden() {
		return myDirectory.isHidden();
	}

	@Override
	public boolean canChangeHiddenStatus(boolean newState) {
		return false;
	}

	@Override
	public void setHidden(boolean hidden) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean hasPassword() {
		return false;
	}

	@Override
	public boolean canChangePassword(boolean newState) {
		return false;
	}

	@Override
	public void setPassword(String password, String passwordHint) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public URL getDataURL() throws MalformedURLException {
		return null;
	}

	@Override
	public URL getPreviewURL() throws MalformedURLException {
		return null;
	}

	@Override
	public boolean canBeLaunched() {
		return true;
	}

	@Override
	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().open(myDirectory);
	}

	public int compareTo(TreeableGalleryItem other) {
		return toString().compareToIgnoreCase(other.toString());
	}

}
