package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SmugSubCategory extends SmugCategory {
	public SmugSubCategory(SmugCategory parent, com.kallasoft.smugmug.api.json.entity.Category apiCategory) {
		super(parent, apiCategory);
	}
	
	public SmugSubCategory(SmugCategory parent, Integer categoryID) throws SmugException {
		this(parent, reloadDetails(categoryID, parent.getCategoryID(), parent.getFullPathLabel()));
		loadChildren();
	}
	
	private static com.kallasoft.smugmug.api.json.entity.Category reloadDetails(Integer categoryID, Integer parentCategoryID, String parentPathLabel) throws SmugException {
		com.kallasoft.smugmug.api.json.v1_2_0.subcategories.Get get
			= new com.kallasoft.smugmug.api.json.v1_2_0.subcategories.Get();
		com.kallasoft.smugmug.api.json.v1_2_0.subcategories.Get.GetResponse response
			= get.execute(SmugMug.API_URL, SmugMug.API_KEY, SmugMug.sessionID, parentCategoryID);
		if (response.isError()) {
			throw new SmugException("Error loading sub-category details in category \"" + parentPathLabel + "\"", SmugException.convertError(response.getError()));
		}
		for (com.kallasoft.smugmug.api.json.entity.Category each : response.getSubCategoryList()) {
			if (categoryID.equals(each.getID())) {
				return each;
			}
		}
		throw new RuntimeException("Error loading sub-category details for sub-category \"" + categoryID + "\" in category \"" + parentPathLabel + "\"");
	}
	
	@Override
	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(new URI("http", SmugMug.getBaseURL(), "/" + parent.getLabel() + "/" + apiCategory.getID(), null));
	}
	
	@Override
	protected Integer getCategoryID() {
		return apiCategory.getParentCategoryID();
	}
	
	@Override
	protected Integer getSubCategoryID() {
		return apiCategory.getID();
	}

	@Override
	protected int acceptableSubAlbumImportDepth() {
		return 0;
	}
}
