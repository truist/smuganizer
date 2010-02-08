package com.rainskit.smuganizer.filesystemapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryItem;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class FileImage extends TreeableGalleryItem implements WriteableTreeableGalleryItem, FileGalleryItem {
	private File myFile;
	
	public FileImage(DirectoryAlbum parent, File myFile) {
		super(parent);
		this.myFile = myFile;
	}

	@Override
	public ItemType getType() {
		return ItemType.IMAGE;
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
	public void reLabel(String newLabel) throws IOException {
		File newName = new File(myFile.getParentFile(), newLabel);
		if (myFile.renameTo(newName)) {
			myFile = newName;
		} else {
			throw new IOException("Unable to rename " + myFile.toString() + " to " + newLabel, null);
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
	public void delete() throws IOException {
		if (!myFile.delete()) {
			throw new IOException("Unable to delete " + myFile.toString(), null);
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
		if (ItemType.IMAGE == other.getType()) {
			return toString().compareToIgnoreCase(other.toString());
		} else {
			return 1;
		}
	}

	public boolean moveSelfTo(File newLocation) {
		if (myFile.renameTo(newLocation)) {
			myFile = newLocation;
			return true;
		} else {
			return false;
		}
	}
}
