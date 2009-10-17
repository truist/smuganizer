package com.rainskit.smuganizer.smugmugapiwrapper;

import com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings;
import com.kallasoft.smugmug.api.util.APIUtils;
import com.rainskit.smuganizer.SmugMugSettings;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.MixedAlbumException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.TreeableGalleryContainer;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryContainer;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask.ModifiedItemAttributes;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SmugCategory extends TreeableGalleryContainer implements WriteableTreeableGalleryContainer {
	private static final int NON_CATEGORY = 0;
	
	protected com.kallasoft.smugmug.api.json.entity.Category apiCategory;
	private ArrayList<SmugSubCategory> subCategories;
	private ArrayList<SmugAlbum> albums;
	private String reName;

	public SmugCategory(TreeableGalleryContainer parent, com.kallasoft.smugmug.api.json.entity.Category apiCategory) {
		super(parent);
		this.apiCategory = apiCategory;
	}

	public SmugCategory(SmugMug parent, Integer categoryID) throws SmugException {
		this(parent, reloadDetails(categoryID, parent.getFullPathLabel()));
		loadChildren();
	}
	
	private static com.kallasoft.smugmug.api.json.entity.Category reloadDetails(Integer categoryID, String parentPathLabel) throws SmugException {
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Get get
			= new com.kallasoft.smugmug.api.json.v1_2_0.categories.Get();
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Get.GetResponse response
			= get.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID);
		if (response.isError()) {
			throw new SmugException("Error loading sub-category details in category \"" + parentPathLabel + "\"", SmugException.convertError(response.getError()));
		}
		for (com.kallasoft.smugmug.api.json.entity.Category each : response.getCategoryList()) {
			if (categoryID.equals(each.getID())) {
				return each;
			}
		}
		throw new RuntimeException("Error loading category details for category \"" + categoryID + "\"");
	}
	
	public List<SmugSubCategory> getSubCategories() {
		if (subCategories == null) {
			subCategories = new ArrayList<SmugSubCategory>();
			for (com.kallasoft.smugmug.api.json.entity.Category each : apiCategory.getSubCategoryList()) {
				subCategories.add(new SmugSubCategory(this, each));
			}
		}
		return subCategories;
	}

	public List<SmugAlbum> getAlbums() {
		if (albums == null) {
			albums = new ArrayList<SmugAlbum>();
			for (com.kallasoft.smugmug.api.json.entity.Album each : apiCategory.getAlbumList()) {
				albums.add(new SmugAlbum(this, each));
			}
		}
		return albums;
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
	
	public String getLabel() {
		return getFileName();
	}

	public String getFileName() {
		return (reName != null) ? reName : apiCategory.getName();
	}
	
	public String getURLName() {
		return getFileName();
	}

	public String getCaption() {
		return null;
	}
	
	public String getDescription() {
		return null;
	}
	
	public boolean canBeRelabeled() {
		return apiCategory.getID().intValue() > 1000;
	}

	public void reLabel(String newName) throws SmugException {
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Rename rename 
			= new com.kallasoft.smugmug.api.json.v1_2_0.categories.Rename();
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Rename.RenameResponse response 
			= rename.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiCategory.getID(), newName);
		if (response.isError()) {
			throw new RenameException(this, newName, SmugException.convertError(response.getError()));
		}
		reName = newName;
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(new URI("http", SmugMug.getBaseURL(), "/" + getLabel(), null));
	}

	public boolean canBeDeleted() {
		return false;
	}

	public void delete() throws SmugException {
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Delete delete
			= new com.kallasoft.smugmug.api.json.v1_2_0.categories.Delete();
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Delete.DeleteResponse response
			= delete.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiCategory.getID());
		if (response.isError()) {
			throw new SmugException("Error deleting " + getFullPathLabel(), SmugException.convertError(response.getError()));
		}
		((WriteableTreeableGalleryContainer)parent).childRemoved(this);
	}

	public void removeAlbum(SmugAlbum album) {
		albums.remove(album);
	}

	public boolean canMoveLocally(TreeableGalleryItem newChild, int childIndex) {
		return (childIndex == -1 && ItemType.ALBUM == newChild.getType() && !willChildBeDuplicate(newChild.getFileName(), null));
	}
	
	public boolean willChildBeDuplicate(String fileName, String caption) {
		for (SmugAlbum each : albums) {
			if (each.getFileName().equalsIgnoreCase(fileName)) {
				return true;
			}
		}
		return false;
	}

	public boolean allowsDuplicateChildren() {
		return false;
	}

	public void moveItemLocally(TreeableGalleryItem childItem, int childIndex, ModifiedItemAttributes modifiedItemAttributes) throws SmugException {
		Integer category = getCategoryID();
		Integer subCategory = getSubCategoryID();
		SmugAlbum album = (SmugAlbum)childItem;
		com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings changeSettings
			= new com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings();
		String[] arguments = new String[ChangeSettings.ARGUMENTS.length];
		arguments[0] = SmugMug.API_KEY;
		arguments[1] = SmugMug.sessionID;
		arguments[2] = APIUtils.toString(album.getAlbumID());
		arguments[6] = APIUtils.toString(category);
		//it became necessary to work around this stupid API
		arguments[7] = (subCategory == null ? "null" : APIUtils.toString(subCategory));
		com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings.ChangeSettingsResponse response
			= changeSettings.execute(SmugMug.API_URL, arguments);
		if (response.isError()) {
			throw new SmugException("Error moving " + album.getFullPathLabel(), SmugException.convertError(response.getError()));
		}
		((SmugCategory)album.getParent()).removeAlbum(album);
		albums.add(albums.size(), album);
		album.setParent(this);
	}
	
	public boolean canImport(TreeableGalleryItem newItem) {
		if (ItemType.ALBUM == newItem.getType()) {
			return (((TreeableGalleryContainer)newItem).getSubAlbumDepth() <= acceptableSubAlbumImportDepth() && willChildBeDuplicate(newItem.getFileName(), null) );
		} else {
			return false;
		}
	}
	
	protected int acceptableSubAlbumImportDepth() {
		return 1;
	}

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedAttributes) throws SmugException {
		TreeableGalleryContainer newContainer = (TreeableGalleryContainer)newItem;
		int subAlbumDepth = newContainer.getSubAlbumDepth();
		if (0 == subAlbumDepth) {
			return importAlbumAsAlbum(newItem);
		} else if (1 == subAlbumDepth) {	//this new album has child albums under it
			if (newContainer.hasImageChildren()) {	//but it also has images under it
				throw new MixedAlbumException(newItem);
			} else {
				return importAlbumAsSubCategory(newItem);
			}
		} else {
			throw new IllegalStateException("This shoudln't ever happen");
		}
	}
	
	private TreeableGalleryItem importAlbumAsAlbum(TreeableGalleryItem sourceAlbum) throws SmugException {
		com.kallasoft.smugmug.api.json.v1_2_0.albums.Create createAlbum 
			= new com.kallasoft.smugmug.api.json.v1_2_0.albums.Create();
		com.kallasoft.smugmug.api.json.v1_2_0.albums.Create.CreateResponse createAlbumResponse 
			= createAlbum.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, 
									sourceAlbum.getCaption(), sourceAlbum.getDescription(), null, 
									getCategoryID(), getSubCategoryID(), 
									null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		if (createAlbumResponse.isError()) {
			throw new SmugException("Error moving " + getFullPathLabel(), SmugException.convertError(createAlbumResponse.getError()));
		}
		SmugAlbum newAlbum = new SmugAlbum(this, createAlbumResponse.getAlbumID(), createAlbumResponse.getAlbumKey());
		albums.add(newAlbum);

		return newAlbum;
	}

	private TreeableGalleryItem importAlbumAsSubCategory(TreeableGalleryItem sourceAlbum) throws SmugException {
		for (TreeableGalleryItem each : getSubCategories()) {
			if (each.getFileName().equals(sourceAlbum.getCaption())) {
				return each;
			}
		}
		com.kallasoft.smugmug.api.json.v1_2_0.subcategories.Create createSubCategory 
			= new com.kallasoft.smugmug.api.json.v1_2_0.subcategories.Create();
		com.kallasoft.smugmug.api.json.v1_2_0.subcategories.Create.CreateResponse createSubCategoryResponse 
			= createSubCategory.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, 
			sourceAlbum.getCaption(), getCategoryID());
		if (createSubCategoryResponse.isError()) {
			throw new SmugException("Error moving " + getFullPathLabel(), SmugException.convertError(createSubCategoryResponse.getError()));
		}
		SmugSubCategory newSubCategory = new SmugSubCategory(this, createSubCategoryResponse.getSubCategoryID());
		subCategories.add(newSubCategory);
		return newSubCategory;
	}
	
	protected Integer getCategoryID() {
		return apiCategory.getID();
	}
	
	protected Integer getSubCategoryID() {
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
