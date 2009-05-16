package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.DeleteException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.HideException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class SmugImage extends TreeableGalleryItem {
	private boolean loaded;
	private com.kallasoft.smugmug.api.json.entity.Image apiImage;
	private SmugAlbum parent;
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

	public URL getSmallImageURL() throws MalformedURLException {
		if (!loaded) {
			loadImageDetails();
		}
		return new URL(apiImage.getSmallURL());
	}

	public URL getPreviewURL() throws MalformedURLException {
		if (!loaded) {
			loadImageDetails();
		}
		return new URL(apiImage.getMediumURL());
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
		String caption = apiImage.getCaption();
		if ("".equals(apiImage.getCaption())) {
			return apiImage.getFileName();
		} else {
			return caption;
		}
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
	}

	public boolean canAccept(TreeableGalleryItem newChild, int childIndex) {
		return false;
	}

	public void receiveChild(TreeableGalleryItem childItem, int childIndex) {
		throw new RuntimeException("Shouldn't be able to get here");
	}

	public String getType() {
		return IMAGE;
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
		com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings changeSettings = new com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings();
		com.kallasoft.smugmug.api.json.v1_2_0.images.ChangeSettings.ChangeSettingsResponse response 
			= changeSettings.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiImage.getID(), null, null, hidden);
		if (response.isError()) {
			throw new HideException(this, response.getError());
		}
		this.hidden = Boolean.valueOf(hidden);
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
