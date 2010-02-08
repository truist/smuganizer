package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.settings.SmugMugSettings;
import com.rainskit.smuganizer.tree.TreeableGalleryContainer;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.TreeableGalleryItem.ItemType;
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
import org.json.JSONArray;
import org.json.JSONException;

public class SmugMug extends TreeableGalleryContainer implements WriteableTreeableGalleryContainer, SmugAPIConstants {
	private static String userNickName;
	
	private ArrayList<SmugCategory> categories;

	public SmugMug() throws IOException {
		super(null);

		SmugAPIMethod login = new SmugAPIMethod(LOGIN_WITH_PASSWORD);
		login.addParameter(USER_NAME, SmugMugSettings.getUsername());
		login.addParameter(PASSWORD, String.valueOf(SmugMugSettings.getPassword()));
		SmugAPIResponse response = login.execute();
		SmugAPIMethod.sessionID = response.getValue(new String[] {JSON_LOGIN, JSON_LOGIN_SESSION, JSON_LOGIN_SESSION_ID});
		userNickName = response.getValue(new String[] {JSON_LOGIN, JSON_LOGIN_USER, JSON_LOGIN_USER_NICKNAME});
	}

	public List<? extends TreeableGalleryItem> loadChildren() throws IOException {
		if (categories == null) {
			categories = new ArrayList<SmugCategory>();

			SmugAPIResponse response = new SmugAPIMethod(GET_TREE).execute();
			JSONArray jsonCategories = response.getArray(new String[] {JSON_CATEGORIES});
			for (int i = 0; i < jsonCategories.length(); i++) {
				try {
					SmugCategory category = new SmugCategory(this, jsonCategories.getJSONObject(i), false);
					if (category.hasSubItems()) {
						categories.add(category);
					}
				} catch (JSONException ex) {
					throw new IOException("Unable to parse JSON tree", ex);
				}
			}
		}
		return categories;
	}
	
	public List<? extends TreeableGalleryItem> getChildren() {
		return categories;
	}

	public ItemType getType() {
		return ItemType.ROOT;
	}

	public String getLabel() {
		return getBaseURL();
	}

	public String getCaption() {
		return null;
	}
	
	public String getDescription() {
		return null;
	}
	
	public boolean canBeRelabeled() {
		return false;
	}

	public void reLabel(String newName) {
		throw new UnsupportedOperationException("Cannot rename SmugMug");
	}

	public boolean canMoveLocally(TreeableGalleryItem newChild, int childIndex) {
		return false;
	}

	public void moveItemLocally(TreeableGalleryItem childItem, int childIndex, ModifiedItemAttributes modifiedItemAttributes) {
		throw new UnsupportedOperationException("Not supported.");
	}
	
	public boolean canImport(TreeableGalleryItem newItem) throws IOException {
		if (ItemType.ALBUM == newItem.getType()) {
			TreeableGalleryContainer newContainer = (TreeableGalleryContainer)newItem;
			return (newContainer.getSubAlbumDepth() > 0 && newContainer.getSubAlbumDepth() < 3);
		} else {
			return false;
		}
	}

	public boolean willChildBeDuplicate(String fileName, String caption) throws IOException {
		for (SmugCategory each : categories) {
			if (each.getLabel().equalsIgnoreCase(fileName) || each.getLabel().equalsIgnoreCase(caption)) {
				return true;
			}
		}
		return false;
	}

	public boolean allowsDuplicateChildren() {
		return false;
	}

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedItemAttributes) throws IOException {
		return importAlbumAsCategory((TreeableGalleryContainer)newItem, modifiedItemAttributes);
	}
	
	private TreeableGalleryItem importAlbumAsCategory(TreeableGalleryContainer sourceAlbum, ModifiedItemAttributes modifiedItemAttributes) throws IOException {
		if (sourceAlbum.hasImageChildren()) {
			throw new MixedAlbumException(sourceAlbum);
		}

		String categoryName = sourceAlbum.getLabel();
		if (modifiedItemAttributes.handleDuplicate == HandleDuplicate.OVERWRITE) {
			for (TreeableGalleryItem each : categories) {
				if (each.getLabel().equals(categoryName)) {
					return each;
				}
			}
		}

		categoryName = cleanUpName(categoryName, modifiedItemAttributes.handleDuplicate, false);

		SmugAPIMethod createCategory = new SmugAPIMethod(CREATE_CATEGORY);
		createCategory.addParameter(CATEGORY_NAME, categoryName);
		SmugAPIResponse response = createCategory.execute();

		SmugCategory newCategory = new SmugCategory(this, response.getNestedObject(new String[] {CATEGORY_OBJECT}), true);
		categories.add(newCategory);
		return newCategory;
	}
	
	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(new URI("http", getBaseURL(), "/", null));
	}
	
	public static String getBaseURL() {
		return userNickName + ".smugmug.com";
	}

	public boolean canBeDeleted() {
		return false;
	}

	public void delete() throws IOException {
		throw new UnsupportedOperationException("Cannot delete SmugMug");
	}

	public int compareTo(TreeableGalleryItem o) {
		return 0;
	}

	@Override
	public String getMetaLabel() {
		return "";
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
	public void childRemoved(TreeableGalleryItem child) {
		categories.remove((SmugCategory)child);
	}

	@Override
	public String getFileName() {
		return getURLName();
	}
	
	public String getURLName() {
		return getBaseURL();
	}

	@Override
	public URL getDataURL() throws MalformedURLException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public URL getPreviewURL() throws MalformedURLException {
		return null;
	}
}
