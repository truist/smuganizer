package com.rainskit.smuganizer.smugmugapiwrapper;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SmugSubCategory extends SmugCategory {
	public SmugSubCategory(SmugCategory parent, JSONObject subCategoryJSON, boolean loadDetails) throws IOException {
		super(parent, (loadDetails ? reloadDetails(parent, subCategoryJSON) : subCategoryJSON), false);
	}

	private static JSONObject reloadDetails(SmugCategory parent, JSONObject baseDetails) throws IOException {
		SmugAPIMethod get = new SmugAPIMethod(GET_SUBCATEGORIES);
		get.addParameter(CATEGORY_ACTION_ID, parent.getCategoryID().toString());
		SmugAPIResponse response = get.execute();
		Integer thisID = SmugAPIUtils.getIntegerSafely(baseDetails, SUBCATEGORY_ID);
		JSONArray subCategories = response.getArray(new String[] {SUBCATEGORIES_ARRAY});
		try {
			for (int i = 0; i < subCategories.length(); i++) {
				JSONObject each = subCategories.getJSONObject(i);
				if (thisID.equals(SmugAPIUtils.getIntegerSafely(each, SUBCATEGORY_ID))) {
					return each;
				}
			}
		} catch (JSONException ex) {
			Logger.getLogger(SmugSubCategory.class.getName()).log(Level.SEVERE, null, ex);
		}
		throw new IOException("Unable to find matching subcategory for ID: " + thisID);
	}

	@Override
	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(new URI("http", SmugMug.getBaseURL(), "/" + parent.getURLName() + "/" + getSubCategoryID(), null));
	}
	
	@Override
	protected Integer getCategoryID() throws IOException {
		return ((SmugCategory)parent).getCategoryID();
	}
	
	@Override
	protected Integer getSubCategoryID() throws IOException {
		return SmugAPIUtils.getIntegerSafely(categoryJSON, CATEGORY_ID);
	}

	@Override
	protected int acceptableSubAlbumImportDepth() {
		return 0;
	}
}
