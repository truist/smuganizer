package com.rainskit.smuganizer.smugmugapiwrapper;

import com.kallasoft.smugmug.api.util.APIUtils;
import com.rainskit.smuganizer.SmugMugSettings;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.DeleteException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.MoveException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class SmugCategory extends TreeableGalleryItem {
	private static final int NON_CATEGORY = 0;
	
	protected com.kallasoft.smugmug.api.json.entity.Category apiCategory;
	private ArrayList<SmugSubCategory> subCategories;
	private ArrayList<SmugAlbum> albums;
	private String reName;

	public SmugCategory(TreeableGalleryItem parent, com.kallasoft.smugmug.api.json.entity.Category apiCategory) {
		super(parent);
		this.apiCategory = apiCategory;
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

	public String getLabel() {
		return getName();
	}

	public String getName() {
		return (reName != null) ? reName : apiCategory.getName();
	}

	public boolean canBeRelabeled() {
		return apiCategory.getID().intValue() > 1000;
	}

	public void reLabel(String newName) throws RenameException {
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Rename rename 
			= new com.kallasoft.smugmug.api.json.v1_2_0.categories.Rename();
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Rename.RenameResponse response 
			= rename.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiCategory.getID(), newName);
		if (response.isError()) {
			throw new RenameException(this, newName, response.getError());
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

	public void delete() throws DeleteException {
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Delete delete
			= new com.kallasoft.smugmug.api.json.v1_2_0.categories.Delete();
		com.kallasoft.smugmug.api.json.v1_2_0.categories.Delete.DeleteResponse response
			= delete.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, apiCategory.getID());
		if (response.isError()) {
			throw new DeleteException(this, response.getError());
		}
		parent.removeChild(this);
	}

	public void removeAlbum(SmugAlbum album) {
		albums.remove(album);
	}

	public boolean canMove(TreeableGalleryItem newChild, int childIndex) {
		return (childIndex == -1 && ALBUM.equals(newChild.getType()));
	}

	public void moveItem(TreeableGalleryItem childItem, int childIndex) {
//		if (CATEGORY.equals(childItem.getType())) {
//			throw new UnsupportedOperationException("Not supported yet.");
//		} else {	//must be album, then
			Integer category = getCategoryID();
			Integer subCategory = getSubCategoryID();
			SmugAlbum album = (SmugAlbum)childItem;
			com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings changeSettings
				= new com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings();
			String[] arguments = new String[changeSettings.ARGUMENTS.length];
			arguments[0] = SmugMug.API_KEY;
			arguments[1] = SmugMug.sessionID;
			arguments[2] = APIUtils.toString(album.getAlbumID());
			arguments[6] = APIUtils.toString(category);
			//it became necessary to work around this stupid API
			arguments[7] = (subCategory == null ? "null" : APIUtils.toString(subCategory));
//			arguments[17] = APIUtils.toString(Integer.valueOf(childIndex - subCategories.size() + 1));
			com.kallasoft.smugmug.api.json.v1_2_0.albums.ChangeSettings.ChangeSettingsResponse response
				= changeSettings.execute(SmugMug.API_URL, arguments);
			if (response.isError()) {
				throw new MoveException(album, response.getError());
			}
			((SmugCategory)album.getParent()).removeAlbum(album);
			albums.add(albums.size(), album);
			album.setParent(this);
//		}
	}
	
	public boolean canImport(TreeableGalleryItem newItem, int childIndex) {
		return false;
	}

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, int childIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	protected Integer getCategoryID() {
		return apiCategory.getID();
	}
	
	protected Integer getSubCategoryID() {
		return new Integer(NON_CATEGORY);
	}
	
	public String getType() {
		return CATEGORY;
	}

	public int compareTo(TreeableGalleryItem o) {
		TreeableGalleryItem other = (TreeableGalleryItem)o;
		if (SmugMugSettings.getTreeCategorySort()&& !CATEGORY.equals(other.getType())) {
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
	public void removeChild(TreeableGalleryItem child) {
		albums.remove(child);
		subCategories.remove(child);
	}

	@Override
	public URL getDataURL() throws MalformedURLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public URL getPreviewURL() throws MalformedURLException {
		return null;
	}
}
