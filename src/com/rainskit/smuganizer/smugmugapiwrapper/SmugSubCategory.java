package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.DeleteException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SmugSubCategory extends SmugCategory {
	public SmugSubCategory(SmugCategory parent, com.kallasoft.smugmug.api.json.entity.Category apiCategory) {
		super(parent, apiCategory);
	}
	
	@Override
	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(new URI("http", SmugMug.getBaseURL(), "/" + parent.getLabel() + "/" + apiCategory.getID(), null));
	}
	
	public void delete() throws DeleteException {
		super.delete();
		parent.removeChild(this);
	}
	
	protected Integer getCategoryID() {
		return apiCategory.getParentCategoryID();
	}
	
	protected Integer getSubCategoryID() {
		return apiCategory.getID();
	}
}
