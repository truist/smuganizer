package com.rainskit.smuganizer.filesystemapiwrapper;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class FileImage extends TreeableGalleryItem {
	private File myFile;
	
	public FileImage(DirectoryAlbum parent, File myFile) {
		super(parent);
		this.myFile = myFile;
	}

	@Override
	public List<? extends TreeableGalleryItem> loadChildren() throws IOException {
		return null;
	}

	@Override
	public List<? extends TreeableGalleryItem> getChildren() {
		return null;
	}

	@Override
	public ItemType getType() {
		return ItemType.IMAGE;
	}

	@Override
	public void childRemoved(TreeableGalleryItem child) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean canMoveLocally(TreeableGalleryItem item, int childIndex) {
		return (ItemType.ALBUM == item.getType() || ItemType.ROOT == item.getType());
	}

	@Override
	public void moveItemLocally(TreeableGalleryItem item, int childIndex, TransferInterruption previousInterruption) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean canImport(TreeableGalleryItem newItem) {
		return false;
	}

	@Override
	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, TransferInterruption previousInterruption) throws IOException, TransferInterruption {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public String getLabel() {
		return getFileName();
	}

	@Override
	public String getMetaLabel() {
		return (isHidden() ? " [hidden]" : "");
	}

	@Override
	public boolean canBeRelabeled() {
		return true;
	}

	@Override
	public void reLabel(String newLabel) throws SmugException {
		File newName = new File(myFile.getParentFile(), newLabel);
		if (myFile.renameTo(newName)) {
			myFile = newName;
		} else {
			throw new SmugException("Unable to rename " + myFile.toString() + " to " + newLabel, null);
		}
	}

	@Override
	public String getFileName() {
		return myFile.getName();
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
	public void delete() throws SmugException {
		if (!myFile.delete()) {
			throw new SmugException("Unable to delete " + myFile.toString(), null);
		}
	}

	@Override
	public boolean isHidden() {
		return myFile.isHidden();
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
		return myFile.toURI().toURL();
	}

	@Override
	public URL getPreviewURL() throws MalformedURLException {
		return getDataURL();
	}

	@Override
	public boolean canBeLaunched() {
		return true;
	}

	@Override
	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().open(myFile);
	}

	public int compareTo(TreeableGalleryItem other) {
		return toString().compareToIgnoreCase(other.toString());
	}

}
