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

	protected com.kallasoft.smugmug.api.json.entity.Category apiCategory;
	protected SmugCategory parent;
	protected ArrayList<SmugCategory> subCategories;
	protected ArrayList<SmugAlbum> albums;
	protected String reLabel;

	public SmugCategory(SmugCategory parent, com.kallasoft.smugmug.api.json.entity.Category apiCategory) {
		super();
		this.parent = parent;
		this.apiCategory = apiCategory;
	}

	public List<SmugCategory> getSubCategories() {
		if (subCategories == null) {
			subCategories = new ArrayList<SmugCategory>();
			for (com.kallasoft.smugmug.api.json.entity.Category each : apiCategory.getSubCategoryList()) {
				subCategories.add(new SmugCategory(this, each));
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
		return (reLabel != null) ? reLabel : apiCategory.getName();
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
		reLabel = newName;
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		if (parent != null) {
			Desktop.getDesktop().browse(new URI("http", SmugMug.getBaseURL(), "/" + parent.getLabel() + "/" + apiCategory.getID(), null));
		} else {
			Desktop.getDesktop().browse(new URI("http", SmugMug.getBaseURL(), "/" + getLabel(), null));
		}
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
		if (parent != null) {
			parent.subCategories.remove(this);
		}
	}

	public void removeAlbum(SmugAlbum album) {
		albums.remove(album);
	}

	public boolean canAccept(TreeableGalleryItem newChild, int childIndex) {
		if (childIndex != -1) {
			return false;
		} else if (ALBUM.equals(newChild.getType())) {
			return true;
		} else if (this.parent == null && newChild.getParent() != this) {
			return (CATEGORY.equals(newChild.getType()) 
				&& ((SmugCategory)newChild).getSubCategories().size() == 0 
				&& this != newChild);
		} else {
			return false;
		}
	}

	public void receiveChild(TreeableGalleryItem childItem, int childIndex) {
		if (CATEGORY.equals(childItem.getType())) {
			
			throw new UnsupportedOperationException("Not supported yet.");
			
		} else {	//must be album, then
			SmugAlbum album = (SmugAlbum)childItem;
			Integer category = (parent == null ? apiCategory.getID() : apiCategory.getParentCategoryID());
			Integer subCategory = (parent == null ? new Integer(0) : apiCategory.getID());
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
		}
	}

	public String getType() {
		return CATEGORY;
	}

	public TreeableGalleryItem getParent() {
		return parent;
	}

	public int compareTo(TreeableGalleryItem o) {
		TreeableGalleryItem other = (TreeableGalleryItem)o;
		if (SmugMugSettings.getTreeCategorySort()&& !CATEGORY.equals(other.getType())) {
			return -1;
		}
		
		return toString().compareToIgnoreCase(other.toString());
	}

	public URL getPreviewURL() throws MalformedURLException {
		return null;
	}

	@Override
	public String getMetaLabel() {
		return "";
	}

	@Override
	public boolean isProtected() {
		return false;
	}
}
