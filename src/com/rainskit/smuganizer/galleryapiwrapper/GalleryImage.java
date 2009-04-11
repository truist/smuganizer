package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.commons.lang.StringEscapeUtils;

public class GalleryImage extends AbstractGalleryTreeable {
	private GalleryAlbum parent;
	private Properties imageDetails;
	private int imageRefNum;
	
	public GalleryImage(GalleryAlbum parent, Properties imageDetails, int imageRefNum) {
		this.parent = parent;
		this.imageDetails = imageDetails;
		this.imageRefNum = imageRefNum;
//		this.title = response.getProperty(Gallery.RESPONSE_IMAGE_TITLE_INDEXED + imageRefNum);
//		this.url = new URL(response.getProperty(Gallery.RESPONSE_BASEURL) + this.name);
	}

	public List<? extends TreeableGalleryItem> loadChildren() {
		return null;
	}

	public URL getPreviewURL() throws MalformedURLException {
		return new URL(imageDetails.getProperty(Gallery.RESPONSE_BASEURL) + imageDetails.getProperty(Gallery.RESPONSE_IMAGE_RESIZED_NAME_INDEXED + imageRefNum));
	}
	
	private String getName() {
		return imageDetails.getProperty(Gallery.RESPONSE_IMAGE_NAME_INDEXED + imageRefNum);
	}
	
	private String getUrlName() {
		String name = getName();
		name = name.replace(' ', '_');
		int period = name.lastIndexOf('.');
		if (period > 0) {
			name = name.substring(0, period);
		}
		return name;
	}

	public boolean isHidden() {
		return Gallery.ARGVAL_YES.equals(imageDetails.getProperty(Gallery.RESPONSE_IMAGE_HIDDEN_INDEXED + imageRefNum));
	}

	public String getLabel() {
		return StringEscapeUtils.unescapeHtml(imageDetails.getProperty(Gallery.RESPONSE_IMAGE_CAPTION_INDEXED + imageRefNum));
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(Gallery.generateUrlFor(parent.getUrlName(), getUrlName()));
	}

	public String getType() {
		return IMAGE;
	}

	public TreeableGalleryItem getParent() {
		return parent;
	}

	public int compareTo(TreeableGalleryItem other) {
		if (other instanceof GalleryImage) {
			return Integer.valueOf(imageRefNum).compareTo(Integer.valueOf(((GalleryImage)other).imageRefNum));
		} else {
			return 0;
		}
	}

	public String getMetaLabel() {
		return (isHidden() ? " [hidden]" : "");
	}

	@Override
	public boolean isProtected() {
		return isHidden();
	}
}
