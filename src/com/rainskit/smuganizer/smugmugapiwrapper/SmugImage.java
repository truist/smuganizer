package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.DeleteException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.HideException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmugImage extends TreeableGalleryItem {
	private static final int HIDE_RETRY_DELAY = 1000;
	private static final int HIDE_RETRY_LIMIT = 60000;
	
	private boolean loaded;
	private com.kallasoft.smugmug.api.json.entity.Image apiImage;
	private String reLabel;
	private Boolean hidden;

	public SmugImage(SmugAlbum parent, com.kallasoft.smugmug.api.json.entity.Image apiImage) {
		super(parent);
		this.apiImage = apiImage;
	}

	public void setParent(SmugAlbum newParent) {
		this.parent = newParent;
		loadImageDetails();
	}
	
	public List<? extends TreeableGalleryItem> loadChildren() {
		loadImageDetails();
		return null;
	}

	private void loadImageDetails() {
		com.kallasoft.smugmug.api.json.v1_2_0.images.GetInfo getInfo
			= new com.kallasoft.smugmug.api.json.v1_2_0.images.GetInfo();
		com.kallasoft.smugmug.api.json.v1_2_0.images.GetInfo.GetInfoResponse response 
			= getInfo.execute(SmugMug.API_URL, 
								SmugMug.API_KEY, 
								SmugMug.sessionID, 
								apiImage.getID(), 
								apiImage.getImageKey());
		if (response.isError()) {
			throw new SmugException("Error loading image details for image with ID \"" + apiImage.getID() + "\" in album \"" + parent.getFullPathLabel() + "\"", response.getError());
		}
		apiImage = response.getImage();
		loaded = true;
	}

	public URL getPreviewURL() throws MalformedURLException {
		if (!loaded) {
			loadImageDetails();
		}
		return new URL(apiImage.getMediumURL());
	}

	public URL getDataURL() throws MalformedURLException {
		if (!loaded) {
			loadImageDetails();
		}
		return new URL(apiImage.getOriginalURL());
	}
	
	public Integer getImageID() {
		return apiImage.getID();
	}

	public String getLabel() {
		if (!loaded) {
			loadImageDetails();
		}
		if (reLabel != null) {
			return reLabel;
		}
		String caption = getCaption();
		if (caption != null) {
			return caption;
		} else {
			return getFileName();
		}
	}
	
	public String getCaption() {
		String caption = apiImage.getCaption();
		if (!"".equals(caption)) {
			return caption;
		} else {
			return null;
		}
	}
	
	public String getFileName() {
		return apiImage.getFileName();
	}
	
	public String getURLName() {
		return getFileName();
	}
	
	public int getPosition() {
		if (!loaded) {
			loadImageDetails();
		}
		return apiImage.getPosition().intValue();
	}

	public boolean canBeRelabeled() {
		return true;
	}

	public void reLabel(String newName) throws RenameException {
		com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings changeSettings = new com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings();
		com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings.ChangeSettingsResponse response 
			= changeSettings.execute(SmugMug.API_URL, 
									SmugMug.API_KEY, 
									SmugMug.sessionID, 
									apiImage.getID(), 
									newName, 
									null, 
									null);
		if (response.isError()) {
			throw new RenameException(this, newName, response.getError());
		}
		reLabel = newName;
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException  {
		Desktop.getDesktop().browse(new URI(apiImage.getAlbumURL()));
	}

	public boolean canBeDeleted() {
		return true;
	}

	public void delete() throws DeleteException {
		com.kallasoft.smugmug.api.json.v1_2_0.images.Delete delete = new com.kallasoft.smugmug.api.json.v1_2_0.images.Delete();
		com.kallasoft.smugmug.api.json.v1_2_0.images.Delete.DeleteResponse response = delete.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiImage.getID());
		if (response.isError()) {
			throw new DeleteException(this, response.getError());
		}
		parent.removeChild(this);
		parent = null;
	}

	public boolean canMove(TreeableGalleryItem newChild, int childIndex) {
		return false;
	}

	public void moveItem(TreeableGalleryItem childItem, int childIndex, TransferInterruption previousInterruption) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean canImport(TreeableGalleryItem newItem) {
		return false;
	}

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, TransferInterruption previousInterruption) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public ItemType getType() {
		return ItemType.IMAGE;
	}

	public int compareTo(TreeableGalleryItem o) {
		return 0;
	}
	
	public boolean isHidden() {
		if (hidden != null) {
			return hidden.booleanValue();
		} else {
			return apiImage.isHidden().booleanValue();
		}
	}

	@Override
	public String getMetaLabel() {
		return (isHidden() ? " [hidden]" : "");
	}

	@Override
	public boolean canChangeHiddenStatus(boolean newState) {
		return (newState != isHidden());
	}

	@Override
	public void setHidden(boolean hidden) {
		//if we just uploaded an image into SmugMug, the 'hide' will fail until
		//SmugMug finishes processing the image.  So we retry the action, repeatedly
		//in the hope that it succeeds (within a time limit).
		long startTime = System.currentTimeMillis();
		com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings.ChangeSettingsResponse response = null;
		while ((System.currentTimeMillis() - startTime) < HIDE_RETRY_LIMIT) {
			com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings changeSettings = new com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings();
			response = changeSettings.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiImage.getID(), null, null, hidden);
			if (response.isError()) {
				try {
					Thread.sleep(HIDE_RETRY_DELAY);
				} catch (InterruptedException ex) {
					Logger.getLogger(SmugImage.class.getName()).log(Level.SEVERE, null, ex);
					throw new HideException(this, response.getError());
				}
			} else {
				this.hidden = Boolean.valueOf(hidden);
				return;
			}
		}
		throw new HideException(this, response.getError());
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
	public void setPassword(String password,String passwordHint) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeChild(TreeableGalleryItem child) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
