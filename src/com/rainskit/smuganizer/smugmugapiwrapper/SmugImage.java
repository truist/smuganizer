package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryContainer;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryItem;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.json.JSONObject;

public class SmugImage extends TreeableGalleryItem implements WriteableTreeableGalleryItem, SmugAPIConstants {
	private static final int HIDE_RETRY_DELAY = 1000;
	private static final int HIDE_RETRY_LIMIT = 60000;
	
	private boolean loaded;
	private JSONObject imageJSON;
	private String reLabel;
	private Boolean hidden;

	public SmugImage(SmugAlbum parent, JSONObject imageJSON) {
		super(parent);
		this.imageJSON = imageJSON;
	}

	public SmugImage(SmugAlbum parent, String imageID, String imageKey) throws IOException {
		this(parent, loadImageDetails(imageID, imageKey));
		loaded = true;
	}

	public void setParent(SmugAlbum newParent) throws IOException {
		super.setParent(newParent);
		loadImageDetails();
	}

	private void loadImageDetails() throws IOException {
		this.imageJSON = loadImageDetails(getImageID().toString(), getImageKey());
		loaded = true;
	}

	private static JSONObject loadImageDetails(String ID, String key) throws IOException {
		SmugAPIMethod getInfo = new SmugAPIMethod(GET_IMAGE_INFO);
		getInfo.addParameter(IMAGE_ACTION_ID, ID);
		getInfo.addParameter(IMAGE_ACTION_KEY, key);
		return getInfo.execute().getNestedObject(new String[] {IMAGE_OBJECT});
	}

	public URL getPreviewURL() throws IOException {
		if (!loaded) {
			loadImageDetails();
		}
		return new URL(SmugAPIUtils.getStringSafely(imageJSON, IMAGE_MEDIUM_URL));
	}

	public URL getDataURL() throws IOException {
		if (!loaded) {
			loadImageDetails();
		}
		return new URL(SmugAPIUtils.getStringSafely(imageJSON, IMAGE_ORIGINAL_URL));
	}
	
	protected Integer getImageID() throws IOException {
		return SmugAPIUtils.getIntegerSafely(imageJSON, IMAGE_ID);
	}

	private String getImageKey() throws IOException {
		return SmugAPIUtils.getStringSafely(imageJSON, IMAGE_KEY);
	}

	public String getLabel() throws IOException {
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
	
	public String getCaption() throws IOException {
		String caption = SmugAPIUtils.getStringSafely(imageJSON, IMAGE_CAPTION);
		if (!"".equals(caption)) {
			return caption;
		} else {
			return null;
		}
	}
	
	public String getDescription() {
		return null;
	}
	
	public String getFileName() throws IOException {
		return SmugAPIUtils.getStringSafely(imageJSON, IMAGE_FILENAME);
	}
	
	public String getURLName() throws IOException {
		return getFileName();
	}
	
	public int getPosition() throws IOException {
		if (!loaded) {
			loadImageDetails();
		}
		return SmugAPIUtils.getIntegerSafely(imageJSON, IMAGE_POSITION).intValue();
	}

	public boolean canBeRelabeled() {
		return true;
	}

	public void reLabel(String newName) throws IOException {
		SmugAPIMethod changeSettings = new SmugAPIMethod(CHANGE_IMAGE_SETTINGS);
		changeSettings.addParameter(IMAGE_ACTION_ID, getImageID().toString());
		changeSettings.addParameter(IMAGE_CAPTION, newName);
		changeSettings.execute();
		reLabel = newName;
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException  {
		Desktop.getDesktop().browse(new URI(SmugAPIUtils.getStringSafely(imageJSON, IMAGE_ALBUM_URL)));
	}

	public boolean canBeDeleted() {
		return true;
	}

	public void delete() throws IOException {
		SmugAPIMethod delete = new SmugAPIMethod(IMAGE_DELETE);
		delete.addParameter(IMAGE_ACTION_ID, getImageID().toString());

		delete.execute();
		
		((WriteableTreeableGalleryContainer)parent).childRemoved(this);
		parent = null;
	}

	public int compareTo(TreeableGalleryItem o) {
		return 0;
	}
	
	public boolean isHidden() throws IOException {
		if (hidden != null) {
			return hidden.booleanValue();
		} else {
			Boolean value = SmugAPIUtils.getBooleanSafely(imageJSON, IMAGE_HIDDEN);
			if (value != null) {
				return value.booleanValue();
			} else {
				return false;
			}
		}
	}

	@Override
	public String getMetaLabel() throws IOException {
		return (isHidden() ? " [hidden]" : "");
	}

	@Override
	public boolean canChangeHiddenStatus(boolean newState) throws IOException {
		return (newState != isHidden());
	}

	@Override
	public void setHidden(boolean hidden) throws IOException {
		SmugAPIMethod changeSettings = new SmugAPIMethod(CHANGE_IMAGE_SETTINGS);
		changeSettings.addParameter(IMAGE_ACTION_ID, getImageID().toString());
		changeSettings.addParameter(IMAGE_HIDDEN, (Boolean.valueOf(hidden) ? "1" : "0"));

		//if we just uploaded an image into SmugMug, the 'hide' will fail until
		//SmugMug finishes processing the image.  So we retry the action, repeatedly,
		//in the hope that it succeeds (within a time limit).
		IOException lastFailure = null;
		long startTime = System.currentTimeMillis();
		while ((System.currentTimeMillis() - startTime) < HIDE_RETRY_LIMIT) {
			try {
				changeSettings.execute();
				this.hidden = Boolean.valueOf(hidden);
				return;
			} catch (IOException ex) {
				lastFailure = ex;
				try {
					Thread.sleep(HIDE_RETRY_DELAY);
				} catch (InterruptedException iex) {
					throw new IOException(iex);
				}
			}
		}
		throw lastFailure;
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
	public ItemType getType() {
		return ItemType.IMAGE;
	}
}
