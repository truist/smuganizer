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

public class SmugCategory extends TreeableGalleryContainer implements WriteableTreeableGalleryContainer, SmugAPIConstants {
	private static final int NON_CATEGORY = 0;
	private static final int SMUGMUG_FIXED_CATEGORY_MAX = 10000;

	protected JSONObject categoryJSON;
	private ArrayList<SmugSubCategory> subCategories;
	private ArrayList<SmugAlbum> albums;
	private String reName;

	public SmugCategory(TreeableGalleryContainer parent, JSONObject categoryJSON, boolean loadDetails) throws IOException {
		super(parent);
		this.categoryJSON = categoryJSON;
		if (loadDetails) {
			this.categoryJSON = categoryJSON = reloadDetails();
		}

		try {
			albums = new ArrayList<SmugAlbum>();
			if (!categoryJSON.isNull(JSON_ALBUMS)) {
				JSONArray albumArray = categoryJSON.getJSONArray(JSON_ALBUMS);
				if (albumArray != null) {
					for (int i = 0; i < albumArray.length(); i++) {
						albums.add(new SmugAlbum(this, albumArray.getJSONObject(i), false));
					}
				}
			}

			subCategories = new ArrayList<SmugSubCategory>();
			if (!categoryJSON.isNull(JSON_SUBCATEGORIES)) {
				JSONArray subCategoryArray = categoryJSON.getJSONArray(JSON_SUBCATEGORIES);
				if (subCategoryArray != null) {
					for (int i = 0; i < subCategoryArray.length(); i++) {
						subCategories.add(new SmugSubCategory(this, subCategoryArray.getJSONObject(i), false));
					}
				}
			}
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}

	private JSONObject reloadDetails() throws IOException {
		SmugAPIMethod get = new SmugAPIMethod(GET_CATEGORIES);
		SmugAPIResponse response = get.execute();
		JSONArray categories = response.getArray(new String[] {CATEGORIES_ARRAY});
		try {
			for (int i = 0; i < categories.length(); i++) {
				JSONObject each = categories.getJSONObject(i);
				if (getCategoryID().equals(SmugAPIUtils.getIntegerSafely(each, CATEGORY_ID))) {
					return each;
				}
			}
		} catch (JSONException ex) {
			Logger.getLogger(SmugCategory.class.getName()).log(Level.SEVERE, null, ex);
		}
		throw new IOException("Unable to find matching category for ID: " + getCategoryID());
	}

	public List<SmugSubCategory> getSubCategories() {
		return subCategories;
	}

	public List<SmugAlbum> getAlbums() {
		return albums;
	}

	public boolean hasSubItems() {
		return (getSubCategories().size() > 0) || (getAlbums().size() > 0);
	}
	
	public List<? extends TreeableGalleryItem> loadChildren() {
		ArrayList<TreeableGalleryItem> children = new ArrayList<TreeableGalleryItem>();
		children.addAll(getSubCategories());
		children.addAll(getAlbums());
		return children;
	}

	public List<? extends TreeableGalleryItem> getChildren() {
		return loadChildren();
	}
	
	public String getLabel() throws IOException {
		return getFileName();
	}

	public String getFileName() throws IOException {
		if (reName != null) {
			return reName;
		}
		String name = SmugAPIUtils.getStringSafely(categoryJSON, CATEGORY_NAME);
		if (name != null) {
			return name;
		}
		return SmugAPIUtils.getStringSafely(categoryJSON, CATEGORY_TITLE);
	}
	
    @SuppressWarnings("empty-statement")
	public String getURLName() throws IOException {
        String start = getLabel().replaceAll("[^A-Za-z0-9\\s-]", "").replaceAll("[\\s]", "-");
        while (!start.equals(start = start.replace("--", "-")));
		return start;
	}

	public String getCaption() {
		return null;
	}
	
	public String getDescription() {
		return null;
	}
	
	public boolean canBeRelabeled() throws IOException {
		return getCategoryID().intValue() > 1000;
	}

	public void reLabel(String newName) throws IOException {
		SmugAPIMethod rename = new SmugAPIMethod(RENAME_CATEGORY);
		rename.addParameter(CATEGORY_ACTION_ID, getCategoryID().toString());
		rename.addParameter(CATEGORY_NAME, newName);
		rename.execute();
		reName = newName;
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(new URI("http", SmugMug.getBaseURL(), "/" + getURLName(), null));
	}

	public boolean canBeDeleted() throws IOException {
		return (getCategoryID().intValue() > SMUGMUG_FIXED_CATEGORY_MAX);
	}

	public void delete() throws IOException {
		SmugAPIMethod delete = new SmugAPIMethod(DELETE_CATEGORY);
		delete.addParameter(CATEGORY_ACTION_ID, getCategoryID().toString());
		delete.execute();
		((WriteableTreeableGalleryContainer)parent).childRemoved(this);
	}

	public void removeAlbum(SmugAlbum album) {
		albums.remove(album);
	}

	public boolean canMoveLocally(TreeableGalleryItem newChild, int childIndex) {
		return (childIndex == -1 && ItemType.ALBUM == newChild.getType());
	}
	
	public boolean willChildBeDuplicate(String fileName, String caption) throws IOException {
        for (SmugSubCategory each : subCategories) {
            if (each.getLabel().equalsIgnoreCase(fileName) || each.getLabel().equalsIgnoreCase(caption)) {
                return true;
            }
        }
        for (SmugAlbum each : albums) {
            if (each.getLabel().equalsIgnoreCase(fileName) || each.getLabel().equalsIgnoreCase(caption)) {
                return true;
            }
        }
        return false;
	}

	public boolean allowsDuplicateChildren() {
		return true;
	}

	public void moveItemLocally(TreeableGalleryItem childItem, int childIndex, ModifiedItemAttributes modifiedItemAttributes) throws IOException {
		Integer category = getCategoryID();
		Integer subCategory = getSubCategoryID();
		SmugAlbum album = (SmugAlbum)childItem;

		SmugAPIMethod changeSettings = new SmugAPIMethod(CHANGE_ALBUM_SETTINGS);
		changeSettings.addParameter(ALBUM_ACTION_ID, album.getAlbumID().toString());
		changeSettings.addParameter(CATEGORY_ACTION_ID, category.toString());
		changeSettings.addParameter(SUBCATEGORY_ACTION_ID, (subCategory == null ? "null" : subCategory.toString()));
		changeSettings.execute();

		((SmugCategory)album.getParent()).removeAlbum(album);
		albums.add(albums.size(), album);
		album.setParent(this);
	}
	
	public boolean canImport(TreeableGalleryItem newItem) throws IOException {
		if (ItemType.ALBUM == newItem.getType()) {
			return (((TreeableGalleryContainer)newItem).getSubAlbumDepth() <= acceptableSubAlbumImportDepth());
		} else {
			return false;
		}
	}
	
	protected int acceptableSubAlbumImportDepth() {
		return 1;
	}

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedAttributes) throws IOException {
		TreeableGalleryContainer newContainer = (TreeableGalleryContainer)newItem;
		int subAlbumDepth = newContainer.getSubAlbumDepth();
		if (0 == subAlbumDepth) {
			return importAlbumAsAlbum(newItem, modifiedAttributes);
		} else if (1 == subAlbumDepth) {	//this new album has child albums under it
			if (newContainer.hasImageChildren()) {	//but it also has images under it
				throw new MixedAlbumException(newItem);
			} else {
				return importAlbumAsSubCategory(newItem, modifiedAttributes);
			}
		} else {
			throw new IllegalStateException("This shoudln't ever happen");
		}
	}
	
	private TreeableGalleryItem importAlbumAsAlbum(TreeableGalleryItem sourceAlbum, ModifiedItemAttributes modifiedAttributes) throws IOException {
		String newTitle = cleanUpName(sourceAlbum.getLabel(), modifiedAttributes.handleDuplicate, false);

		SmugAPIMethod createAlbum = new SmugAPIMethod(CREATE_ALBUM);
		createAlbum.addParameter(ALBUM_TITLE, newTitle);
		if (sourceAlbum.getDescription() != null) {
			createAlbum.addParameter(ALBUM_DESCRIPTION, sourceAlbum.getDescription());
		}
		createAlbum.addParameter(CATEGORY_ACTION_ID, getCategoryID().toString());
		createAlbum.addParameter(SUBCATEGORY_ACTION_ID, getSubCategoryID().toString());
		SmugAPIResponse response = createAlbum.execute();

		SmugAlbum newAlbum = new SmugAlbum(this, response.getNestedObject(new String[] {ALBUM_OBJECT}), true);
		albums.add(newAlbum);
		return newAlbum;
	}

	private TreeableGalleryItem importAlbumAsSubCategory(TreeableGalleryItem sourceAlbum, ModifiedItemAttributes modifiedAttributes) throws IOException {
		String subCategoryName = sourceAlbum.getLabel();
		if (modifiedAttributes.handleDuplicate == HandleDuplicate.OVERWRITE) {
			for (TreeableGalleryItem each : subCategories) {
				if (each.getLabel().equals(subCategoryName)) {
					return each;
				}
			}
		} else if (modifiedAttributes.handleDuplicate == HandleDuplicate.RENAME) {
			subCategoryName = cleanUpName(subCategoryName, null, false);
		}
		SmugAPIMethod createSubCategory = new SmugAPIMethod(CREATE_SUBCATEGORY);
		createSubCategory.addParameter(CATEGORY_ACTION_ID, getCategoryID().toString());
		createSubCategory.addParameter(SUBCATEGORY_NAME, subCategoryName);
		SmugAPIResponse response = createSubCategory.execute();
		SmugSubCategory newSubCategory = new SmugSubCategory(this, response.getNestedObject(new String[] {SUBCATEGORY_OBJECT}), true);
		subCategories.add(newSubCategory);
		return newSubCategory;
	}
	
	protected Integer getCategoryID() throws IOException {
		return SmugAPIUtils.getIntegerSafely(categoryJSON, CATEGORY_ID);
	}
	
	protected Integer getSubCategoryID() throws IOException {
		return new Integer(NON_CATEGORY);
	}
	
	public ItemType getType() {
		return ItemType.CATEGORY;
	}

	public int compareTo(TreeableGalleryItem o) {
		TreeableGalleryItem other = (TreeableGalleryItem)o;
		if (SmugMugSettings.getTreeCategorySort() && ItemType.CATEGORY != other.getType()) {
			return -1;
		}
		
		return toString().compareToIgnoreCase(other.toString());
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
		albums.remove(child);
		subCategories.remove(child);
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
