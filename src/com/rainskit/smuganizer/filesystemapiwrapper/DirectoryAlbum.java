package com.rainskit.smuganizer.filesystemapiwrapper;

import com.rainskit.smuganizer.settings.FileSettings;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.TreeableGalleryContainer;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryContainer;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask.HandleDuplicate;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask.ModifiedItemAttributes;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

public class DirectoryAlbum extends TreeableGalleryContainer implements WriteableTreeableGalleryContainer, FileGalleryItem {
	private File myFile;
	private ArrayList<DirectoryAlbum> subDirs;
	private ArrayList<FileImage> subFiles;
	private boolean loaded;
	
	public DirectoryAlbum(DirectoryAlbum parent, File myDirectory) {
		super(parent);
		this.myFile = myDirectory;
		subDirs = new ArrayList<DirectoryAlbum>();
		subFiles = new ArrayList<FileImage>();
	}

	@Override
	public List<? extends TreeableGalleryItem> loadChildren() throws IOException {
		if (!loaded) {
			loaded = true;
            File[] children = myFile.listFiles();
            if (children != null) {
                for (File each : myFile.listFiles()) {
                    if (each.isDirectory()) {
                        subDirs.add(new DirectoryAlbum(this, each));
                    } else {
                        subFiles.add(new FileImage(this, each));
                    }
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
	public void childRemoved(TreeableGalleryItem child) {
		subDirs.remove(child);
		subFiles.remove(child);
	}

	@Override
	public boolean canMoveLocally(TreeableGalleryItem newChild, int childIndex) {
		return (ItemType.ROOT != newChild.getType() && checkAncestry(this, newChild) && this != newChild.getParent());
	}

	private boolean checkAncestry(TreeableGalleryItem child, TreeableGalleryItem ancestor) {
		return (child != ancestor) && (child == null || checkAncestry(child.getParent(), ancestor));
	}

	public void moveItemLocally(TreeableGalleryItem newChild, int childIndex, ModifiedItemAttributes modifiedItemAttributes) throws SmugException {
        String newFileName = cleanUpName(constructFileName(newChild.getFileName(), null), modifiedItemAttributes.handleDuplicate, true);
		File newFilePath = new File(myFile, newFileName);
		if (HandleDuplicate.OVERWRITE == modifiedItemAttributes.handleDuplicate) {
			newFilePath.delete();
		}
		if (((FileGalleryItem)newChild).moveSelfTo(newFilePath)) {
			((WriteableTreeableGalleryContainer)newChild.getParent()).childRemoved(newChild);
			if (ItemType.IMAGE == newChild.getType()) {
				subFiles.add((FileImage)newChild);
			} else {
				subDirs.add((DirectoryAlbum)newChild);
			}
			newChild.setParent(this);
		} else {
			throw new SmugException("Error moving " + newChild.getFileName() + " to " + newFilePath.toString(), null);
		}
	}

	@Override
	public boolean canImport(TreeableGalleryItem newItem) {
		return true;
	}
	
	@Override
	public boolean willChildBeDuplicate(String fileName, String caption) throws SmugException {
        return new File(myFile, cleanUpName(constructFileName(fileName, caption), null, true)).exists();
	}
	
	public boolean allowsDuplicateChildren() {
		return false;
	}
	
	@Override
	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedItemAttributes) throws SmugException {
		if (ItemType.IMAGE == newItem.getType()) {
			return importImage(newItem, modifiedItemAttributes);
		} else {
			return importNonImage(newItem, modifiedItemAttributes);
		}
	}

	private TreeableGalleryItem importImage(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedItemAttributes) throws SmugException {
        String fileName = cleanUpName(constructFileName(newItem.getFileName(), modifiedItemAttributes.caption), modifiedItemAttributes.handleDuplicate, true);
		File newFile = new File(myFile, fileName);
		FileOutputStream fileOutput = null;
		try {
			fileOutput = new FileOutputStream(newFile);
			IOUtils.write(modifiedItemAttributes.imageData, fileOutput);
			if (HandleDuplicate.OVERWRITE == modifiedItemAttributes.handleDuplicate) {
				for (FileImage each : subFiles) {
					if (fileName.equalsIgnoreCase(each.getFileName())) {
						return each;
					}
				}
				throw new RuntimeException("Instruction was to overwrite, but existing file was not found - this should never happen");
			} else {
				FileImage newImage = new FileImage(this, newFile);
				subFiles.add(newImage);
				return newImage;
			}
		} catch (IOException ex) {
			Logger.getLogger(DirectoryAlbum.class.getName()).log(Level.SEVERE, null, ex);
			throw new SmugException("Unable to write file " + newFile.getAbsolutePath(), ex);
		} finally {
			if (fileOutput != null) {
				try {
					fileOutput.close();
				} catch (IOException ex1) {
					Logger.getLogger(DirectoryAlbum.class.getName()).log(Level.SEVERE, null, ex1);
				}
			}
		}
	}

	private TreeableGalleryItem importNonImage(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedItemAttributes) throws SmugException {
        String fileName = cleanUpName(constructFileName(newItem.getFileName(), modifiedItemAttributes.caption), modifiedItemAttributes.handleDuplicate, true);
		File newFile = new File(myFile, fileName);
		if (newFile.mkdir()) {
			DirectoryAlbum newAlbum = new DirectoryAlbum(this, newFile);
			subDirs.add(newAlbum);
			return newAlbum;
		} else {
			throw new SmugException("Failed to make sub-album named " + fileName, null);
		}
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
		for (DirectoryAlbum each : subDirs) {
			each.delete();
		}
		for (FileImage each : subFiles) {
			each.delete();
		}
		if (!myFile.delete()) {
			throw new SmugException("Error deleting " + myFile.toString(), null);
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
		throw new UnsupportedOperationException("Not supported.");
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
		Desktop.getDesktop().open(myFile);
	}

	public int compareTo(TreeableGalleryItem other) {
		if (ItemType.IMAGE == other.getType()) {
			return -1;
		} else {
			return toString().compareToIgnoreCase(other.toString());
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

	private String constructFileName(String fileName, String caption) {
        if (caption == null || !FileSettings.getPreserveCaptions()) {
            return fileName;
        } else {
            return caption + getExtension(fileName);
        }
	}
}
