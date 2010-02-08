package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.settings.SmugMugSettings;
import com.rainskit.smuganizer.tree.TreeableGalleryContainer;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryContainer;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask.HandleDuplicate;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask.ModifiedItemAttributes;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SmugAlbum extends TreeableGalleryContainer implements WriteableTreeableGalleryContainer, SmugAPIConstants {
	private static final int NO_IMAGES_FOUND_ERROR_CODE = 15;
	private static final String PASSWORD_SET_TO_EMPTY = "";

	private JSONObject albumJSON;
	private boolean childrenLoaded;
	private ArrayList<SmugImage> images;
	private String password;
	private String reName;

	public SmugAlbum(SmugCategory parent, JSONObject albumJSON, boolean loadDetails) throws IOException {
		super(parent);
		this.albumJSON = albumJSON;
		if (loadDetails) {
			this.albumJSON = reloadDetails();
		}
		images = new ArrayList<SmugImage>();
	}

	public void setParent(SmugCategory newParent) throws IOException {
		super.setParent(newParent);
		albumJSON = reloadDetails();
	}

	private JSONObject reloadDetails() throws IOException {
		SmugAPIMethod getInfo = new SmugAPIMethod(GET_ALBUM_INFO);
		getInfo.addParameter(ALBUM_ACTION_ID, getAlbumID().toString());
		getInfo.addParameter(ALBUM_ACTION_KEY, getAlbumKey());
		return getInfo.execute().getNestedObject(new String[] {ALBUM_OBJECT});
	}

	public List<? extends TreeableGalleryItem> loadChildren() throws IOException{
		try {
			albumJSON = reloadDetails();

			SmugAPIMethod imageGet = new SmugAPIMethod(GET_IMAGES);
			imageGet.addParameter(ALBUM_ACTION_ID, getAlbumID().toString());
			imageGet.addParameter(ALBUM_ACTION_KEY, getAlbumKey());

			try {
				SmugAPIResponse response = imageGet.execute();

				JSONArray imagesJSON = response.getArray(new String[]{"Images"});
				for (int i = 0; i < imagesJSON.length(); i++) {
					images.add(new SmugImage(this, imagesJSON.getJSONObject(i)));
				}
			} catch (SmugAPIResponse ex) {
				if (ex.getErrorCode() != NO_IMAGES_FOUND_ERROR_CODE) {
					throw new IOException(ex);
				}
			}

			childrenLoaded = true;
			return images;
		} catch (JSONException ex) {
			throw new IOException(ex);
		}
	}
	
	public List<? extends TreeableGalleryItem> getChildren() {
		return images;
	}
	
	public List<SmugImage> getImages() throws IOException {
		if (!childrenLoaded) {
			loadChildren();
		}
		return images;
	}

	public String getLabel() throws IOException {
		return getFileName();
	}

	public String getFileName() throws IOException {
		return (reName != null ? reName : SmugAPIUtils.getStringSafely(albumJSON, ALBUM_TITLE));
	}
	
	public String getURLName() throws IOException {
		return getFileName();
	}
	
	public String getCaption() {
		return null;
	}
	
	public String getDescription() throws IOException {
		return SmugAPIUtils.getStringSafely(albumJSON, ALBUM_DESCRIPTION);
	}

	public boolean canBeRelabeled() {
		return true;
	}

	public void reLabel(String newName) throws IOException {
		SmugAPIMethod changeSettings = new SmugAPIMethod(CHANGE_ALBUM_SETTINGS);
		changeSettings.addParameter(ALBUM_ACTION_ID, getAlbumID().toString());
		changeSettings.addParameter(ALBUM_NAME, newName);
		changeSettings.execute();
		reName = newName;
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException  {
		Desktop.getDesktop().browse(new URI("http", SmugMug.getBaseURL(), "/gallery/" + getAlbumID() + "_" + getAlbumKey(), null));
	}

	public boolean canBeDeleted() {
		return true;
	}

	public void delete() throws IOException {
		SmugAPIMethod delete = new SmugAPIMethod(DELETE_ALBUM);
		delete.addParameter(ALBUM_ACTION_ID, getAlbumID().toString());
		delete.execute();

		((WriteableTreeableGalleryContainer)parent).childRemoved(this);
	}

	public void childRemoved(TreeableGalleryItem image) {
		images.remove((SmugImage)image);
	}

	public boolean canMoveLocally(TreeableGalleryItem newChild, int childIndex) {
		return (ItemType.IMAGE == newChild.getType());// && (newChild.getParent() == this));
	}

	public void moveItemLocally(TreeableGalleryItem childItem, int childIndex, ModifiedItemAttributes modifiedItemAttributes) throws IOException {
		SmugImage childImage = (SmugImage)childItem;
		if (childItem.getParent() != this) {
			SmugAPIMethod changeSettings = new SmugAPIMethod(CHANGE_IMAGE_SETTINGS);
			changeSettings.addParameter(ALBUM_ACTION_ID, getAlbumID().toString());
			changeSettings.addParameter(IMAGE_ACTION_ID, childImage.getImageID().toString());
			changeSettings.execute();

			((WriteableTreeableGalleryContainer)childImage.getParent()).childRemoved(childImage);
			images.add(childImage);
			childImage.setParent(this);
		} 
		
		reorderItem(childImage, childIndex);
		
		if (HandleDuplicate.RENAME == modifiedItemAttributes.handleDuplicate) {
            reLabel(cleanUpName(childItem.getLabel(), modifiedItemAttributes.handleDuplicate, false));
		}
	}

	public boolean canImport(TreeableGalleryItem newItem) {
		return (ItemType.IMAGE == newItem.getType());
	}

	public boolean willChildBeDuplicate(String fileName, String caption) throws IOException {
        for (SmugImage each : images) {
            if (each.getLabel().equalsIgnoreCase(caption)) {
                return true;
            }
        }
        return false;
	}

	public boolean allowsDuplicateChildren() {
		return true;
	}

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedItemAttributes) throws IOException {
        String caption = (modifiedItemAttributes.caption == null ? newItem.getCaption() : modifiedItemAttributes.caption);
        caption = cleanUpName(caption, modifiedItemAttributes.handleDuplicate, false);

		SmugAPIMethod upload = new SmugAPIMethod(UPLOAD_IMAGE);
		upload.addParameter(UPLOAD_ALBUM_ID, getAlbumID().toString());
		upload.addParameter(UPLOAD_FILENAME, newItem.getFileName());
		if (caption != null) {
			upload.addParameter(UPLOAD_CAPTION, caption);
		}

		SmugAPIResponse uploadResponse = upload.executeUpload(modifiedItemAttributes.imageData, newItem.getFileName());

		SmugImage newImage = new SmugImage(this,
									uploadResponse.getValue(new String[] {IMAGE_OBJECT, UPLOAD_RESPONSE_ID}),
									uploadResponse.getValue(new String[] {IMAGE_OBJECT, UPLOAD_RESPONSE_KEY}));
		images.add(newImage);
		
		return newImage;
	}
	
	private void reorderItem(SmugImage childImage, int childIndex) throws IOException {
        if (childIndex > images.size()) {   //this can happen if we cancel intermediate transfers
            childIndex = images.size();
        }
		reorderItemInSmugMug(childImage, childIndex);
		
		boolean movingTowardEnd = (images.indexOf(childImage) < childIndex);
		images.remove(childImage);
		images.add(childIndex - (movingTowardEnd ? 1 : 0), childImage);
		
		//ok, at this point, if we just moved an image to position '0', smugmug
		//actually moved it to position '1', and there's no way around it.  so 
		//to fix smugmug, we take the image that we think is in position '1'
		//(but that smugmug actually has in position '0') and move it to
		//position '1' in smugmug, which sets everything right
		if (childIndex == 0 && images.size() > 1) {
			reorderItemInSmugMug(images.get(1), 1);
		}
	}
	
	private void reorderItemInSmugMug(SmugImage childImage, int childIndex) throws IOException {
		boolean movingTowardEnd = (images.indexOf(childImage) < childIndex);

		SmugAPIMethod changePosition = new SmugAPIMethod(CHANGE_IMAGE_POSITION);
		changePosition.addParameter(IMAGE_ACTION_ID, childImage.getImageID().toString());
		changePosition.addParameter(IMAGE_POSITION, "" + (childIndex + (movingTowardEnd ? 0 : 1)));

		changePosition.execute();
	}
	
	protected Integer getAlbumID() throws IOException {
		return SmugAPIUtils.getIntegerSafely(albumJSON, ALBUM_ID);
	}

	private String getAlbumKey() throws IOException {
		return SmugAPIUtils.getStringSafely(albumJSON, ALBUM_KEY);
	}
	
	private Integer getPosition() throws IOException {
		return SmugAPIUtils.getIntegerSafely(albumJSON, ALBUM_POSITION);
	}

	public ItemType getType() {
		return ItemType.ALBUM;
	}

	public int compareTo(TreeableGalleryItem o) {
		TreeableGalleryItem other = (TreeableGalleryItem)o;
		if (SmugMugSettings.getTreeCategorySort() && ItemType.CATEGORY == other.getType()) {
			return 1;
		}
		
		if (!SmugMugSettings.getTreeSort() && ItemType.ALBUM == other.getType()) {
			try {
				Integer leftPosition = getPosition();
				Integer rightPosition = ((SmugAlbum)other).getPosition();
				if (leftPosition != null && rightPosition != null) {
					return leftPosition.compareTo(rightPosition);
				} else {
					return 0;
				}
			} catch (IOException ex) {
				Logger.getLogger(SmugAlbum.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		return toString().compareToIgnoreCase(other.toString());
	}

	@Override
	public String getMetaLabel() throws IOException {
		return (hasPassword() ? " [password protected]" : "");
	}

	@Override
	public boolean canChangeHiddenStatus(boolean newState) {
		return false;
	}

	@Override
	public void setHidden(boolean hidden) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isHidden() {
		return false;
	}
	
	public boolean hasPassword() throws IOException {
		if (PASSWORD_SET_TO_EMPTY.equals(password)) {
			return false;
		}
		return (password != null || !"".equals(SmugAPIUtils.getStringSafely(albumJSON, ALBUM_PASSWORD)));
	}

	@Override
	public boolean canChangePassword(boolean newState) {
		return true;
	}

	@Override
	public void setPassword(final String password, final String passwordHint) throws IOException {
		SmugAPIMethod changeSettings = new SmugAPIMethod(CHANGE_ALBUM_SETTINGS);
		changeSettings.addParameter(ALBUM_ACTION_ID, getAlbumID().toString());
		changeSettings.addParameter(ALBUM_PASSWORD, (password == null ? "" : password));
		changeSettings.addParameter(ALBUM_PASSWORD_HINT, (passwordHint == null ? "" : passwordHint));

		changeSettings.execute();

		this.password = (password == null ? PASSWORD_SET_TO_EMPTY : password);
	}

	public URL getDataURL() throws MalformedURLException {
		throw new UnsupportedOperationException("Not supported.");
	}

	public URL getPreviewURL() throws MalformedURLException {
		return null;
	}
}
