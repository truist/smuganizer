package com.rainskit.smuganizer.smugmugapiwrapper;

import com.kallasoft.smugmug.api.APIConstants;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.DeleteException;
import com.kallasoft.smugmug.api.json.v1_2_0.login.WithPassword;
import com.kallasoft.smugmug.api.json.v1_2_0.users.GetTree;
import com.rainskit.smuganizer.SmugMugSettings;
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

public class SmugMug extends TreeableGalleryItem {
	public static final String TYPE = "SmugMug";
	static final String API_URL = "https://api.smugmug.com/hack/json/1.2.0/";
	static final String API_KEY = "aR8ks0WWmboWAcclI9poAboELIqNj3wW";

	static String sessionID;
	private static String userNickName;
	
	private ArrayList<SmugCategory> categories;

	public SmugMug() {
		WithPassword withPassword = new WithPassword();
		
		WithPassword.WithPasswordResponse response 
			= withPassword.execute(API_URL, 
									API_KEY, 
									SmugMugSettings.getUsername(), 
									String.valueOf(SmugMugSettings.getPassword()));

		if (response.isError()) {
			throw new SmugException("Login failed", response.getError());
		}
		
		sessionID = response.getSessionID();
		userNickName = response.getNickName();
	}

	public List<? extends TreeableGalleryItem> loadChildren() {
		
		if (categories == null) {
			categories = new ArrayList<SmugCategory>();
			
			GetTree getTree = new GetTree();
			GetTree.GetTreeResponse response 
				= getTree.execute(API_URL, API_KEY, sessionID, Boolean.FALSE);
			if (response.isError()) {
				throw new SmugException("GetTree failed", response.getError());
			}

			List<com.kallasoft.smugmug.api.json.entity.Category> smCategories = response.getCategoryList();
			for (com.kallasoft.smugmug.api.json.entity.Category each : smCategories) {
				if (each.getAlbumList().size() > 0 || each.getSubCategoryList().size() > 0) {
					categories.add(new SmugCategory(null, each));
				}
			}
		}
		return categories;
	}

	public String getType() {
		return TYPE;
	}

	public String getLabel() {
		return getBaseURL();
	}

	public boolean canBeRelabeled() {
		return false;
	}

	public void reLabel(String newName) throws RenameException {
		throw new UnsupportedOperationException("Cannot rename SmugMug");
	}

	public boolean canAccept(TreeableGalleryItem newChild, int childIndex) {
		return false;
	}

	public void receiveChild(TreeableGalleryItem childItem, int childIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(new URI("http", getBaseURL(), "/", null));
	}
	
	public static String getBaseURL() {
		return SmugMug.userNickName + ".smugmug.com";
	}

	public boolean canBeDeleted() {
		return false;
	}

	public void delete() throws DeleteException {
		throw new UnsupportedOperationException("Cannot delete SmugMug");
	}

	public TreeableGalleryItem getParent() {
		return null;
	}

	public int compareTo(TreeableGalleryItem o) {
		return 0;
	}

	public URL getPreviewURL() throws MalformedURLException {
		return null;
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
}
