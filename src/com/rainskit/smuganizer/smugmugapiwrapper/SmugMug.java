package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.kallasoft.smugmug.api.json.v1_2_0.login.WithPassword;
import com.kallasoft.smugmug.api.json.v1_2_0.users.GetTree;
import com.rainskit.smuganizer.settings.SmugMugSettings;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.MixedAlbumException;
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

public class SmugMug extends TreeableGalleryContainer implements WriteableTreeableGalleryContainer {
	static final String API_URL = "https://api.smugmug.com/hack/json/1.2.0/";
	static final String API_UPLOAD_URL = "http://upload.smugmug.com/";
	static final String API_KEY = "aR8ks0WWmboWAcclI9poAboELIqNj3wW";

	static String sessionID;
	private static String userNickName;
	
	private ArrayList<SmugCategory> categories;

	public SmugMug() throws SmugException {
		super(null);
		
		WithPassword withPassword = new WithPassword();
		
		WithPassword.WithPasswordResponse response 
			= withPassword.execute(API_URL, 
									API_KEY, 
									SmugMugSettings.getUsername(), 
									String.valueOf(SmugMugSettings.getPassword()));

		if (response.isError()) {
			throw new SmugException("Login failed", SmugException.convertError(response.getError()));
		}
		
		sessionID = response.getSessionID();
		userNickName = response.getNickName();
	}

	public List<? extends TreeableGalleryItem> loadChildren() throws SmugException {
		if (categories == null) {
			categories = new ArrayList<SmugCategory>();
			
			GetTree getTree = new GetTree();
			GetTree.GetTreeResponse response 
				= getTree.execute(API_URL, API_KEY, sessionID, Boolean.FALSE);
			if (response.isError()) {
				throw new SmugException("GetTree failed", SmugException.convertError(response.getError()));
			}

			List<com.kallasoft.smugmug.api.json.entity.Category> smCategories = response.getCategoryList();
			for (com.kallasoft.smugmug.api.json.entity.Category each : smCategories) {
				if (each.getAlbumList().size() > 0 || each.getSubCategoryList().size() > 0) {
					categories.add(new SmugCategory(this, each));
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

	public void reLabel(String newName) throws RenameException {
		throw new UnsupportedOperationException("Cannot rename SmugMug");
	}

	public boolean canMoveLocally(TreeableGalleryItem newChild, int childIndex) {
		return false;
	}

	public void moveItemLocally(TreeableGalleryItem childItem, int childIndex, ModifiedItemAttributes modifiedItemAttributes) {
		throw new UnsupportedOperationException("Not supported.");
	}
	
	public boolean canImport(TreeableGalleryItem newItem) {
		if (ItemType.ALBUM == newItem.getType()) {
			TreeableGalleryContainer newContainer = (TreeableGalleryContainer)newItem;
			return (newContainer.getSubAlbumDepth() > 0 && newContainer.getSubAlbumDepth() < 3);
		} else {
			return false;
		}
	}

	public boolean willChildBeDuplicate(String fileName, String caption) {
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

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedItemAttributes) throws SmugException {
		return importAlbumAsCategory((TreeableGalleryContainer)newItem, modifiedItemAttributes);
	}
	
	private TreeableGalleryItem importAlbumAsCategory(TreeableGalleryContainer sourceAlbum, ModifiedItemAttributes modifiedItemAttributes) throws SmugException {
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

        com.kallasoft.smugmug.api.json.v1_2_0.categories.Create createCategory
            = new com.kallasoft.smugmug.api.json.v1_2_0.categories.Create();
        com.kallasoft.smugmug.api.json.v1_2_0.categories.Create.CreateResponse createCategoryResponse
            = createCategory.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, categoryName);
        if (createCategoryResponse.isError()) {
            throw new SmugException("Error importing " + sourceAlbum.getFullPathLabel(), SmugException.convertError(createCategoryResponse.getError()));
        }
        SmugCategory newCategory = new SmugCategory(this, createCategoryResponse.getCategoryID());
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

	public void delete() throws SmugException {
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
		categories.remove(child);
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
